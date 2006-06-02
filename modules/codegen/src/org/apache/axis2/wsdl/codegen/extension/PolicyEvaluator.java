/*
 * Copyright 2004,2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.axis2.wsdl.codegen.extension;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.description.AxisModule;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.PolicyInclude;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.modules.Module;
import org.apache.axis2.modules.ModulePolicyExtension;
import org.apache.axis2.modules.PolicyExtension;
import org.apache.axis2.wsdl.codegen.CodeGenConfiguration;
import org.apache.axis2.wsdl.util.XSLTConstants;
import org.apache.ws.policy.All;
import org.apache.ws.policy.ExactlyOne;
import org.apache.ws.policy.Policy;
import org.apache.ws.policy.PrimitiveAssertion;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.util.HashMap;
import java.util.Iterator;

public class PolicyEvaluator implements CodeGenExtension {

	private CodeGenConfiguration configuration;
    private AxisService axisService;
	private HashMap ns2Exts = new HashMap();

	private void init(CodeGenConfiguration configuration) {
		this.configuration = configuration;
        this.axisService = configuration.getAxisService();
        
       // adding default PolicyExtensions
       ns2Exts.put("http://schemas.xmlsoap.org/ws/2004/09/policy/optimizedmimeserialization", new MTOMPolicyExtension());
       ns2Exts.put("http://schemas.xmlsoap.org/ws/2004/09/policy/encoding", new EncodePolicyExtension());

       //set the policy handling template 
       configuration.putProperty("policyExtensionTemplate", "/org/apache/axis2/wsdl/template/java/PolicyExtensionTemplate.xsl");

       
		String repository = configuration.getRepositoryPath();
        
		if (repository == null) {    return;    }


		try {
            
			ConfigurationContext configurationCtx = ConfigurationContextFactory
					.createConfigurationContextFromFileSystem(repository, null);
			AxisConfiguration axisConfiguration = configurationCtx
					.getAxisConfiguration();

			for (Iterator iterator = axisConfiguration.getModules().values()
					.iterator(); iterator.hasNext();) {
		
                AxisModule axisModule = (AxisModule) iterator.next();
				String[] namespaces = axisModule.getSupportedPolicyNamespaces();

				if (namespaces == null) {
					continue;
				}
                
                Module module = axisModule.getModule();
                if (!(module instanceof ModulePolicyExtension)) {
                    continue;
                }
                
                PolicyExtension ext = ((ModulePolicyExtension) module).getPolicyExtension();

				for (int i = 0; i < namespaces.length; i++) {
					ns2Exts.put(namespaces[i], ext);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			System.err
					.println("cannot create repository : policy will not be supported");
		}
	}

	public void engage(CodeGenConfiguration configuration) {

         //initialize
        init(configuration);

        Document document = getEmptyDocument();
		Element rootElement = document.createElement("stubMethods");
        
        AxisOperation axisOperation;
        QName opName;
        PolicyInclude policyInclude;
        Policy policy;

        
        for (Iterator iterator = axisService.getOperations(); iterator.hasNext(); ) {
            axisOperation = (AxisOperation) iterator.next();
            opName = axisOperation.getName();
            
            policyInclude = axisOperation.getPolicyInclude();
            policy = policyInclude.getEffectivePolicy();
            
            if (policy != null) {
            	processPolicies(document, rootElement, policy, opName);
            }
        }
        
        configuration.putProperty("stubMethods", rootElement);
	}

	private void processPolicies(Document document, Element rootElement,
			Policy policy, QName opName) {
        
		if (!policy.isNormalized()) {
			policy = (Policy) policy.normalize();
		}

		HashMap map = new HashMap();

		ExactlyOne XOR = (ExactlyOne) policy.getTerms()
				.get(0);
		All AND = (All) XOR.getTerms().get(
				0);

		for (Iterator iterator = AND.getTerms().iterator(); iterator.hasNext();) {

			All nAND = new All();
			PrimitiveAssertion pa = (PrimitiveAssertion) iterator.next();

			String namespace = pa.getName().getNamespaceURI();
			nAND.addTerm(pa);

			while (iterator.hasNext()) {
				pa = (PrimitiveAssertion) iterator.next();

				if (namespace.equals(pa.getName().getNamespaceURI())) {
					nAND.addTerm(pa);
				}
			}

			map.put(namespace, nAND);
			AND.getTerms().removeAll(nAND.getTerms());

			iterator = AND.getTerms().iterator();
		}

		for (Iterator iterator = map.keySet().iterator(); iterator.hasNext();) {
			String namespace = (String) iterator.next();
            PolicyExtension policyExtension = (PolicyExtension) ns2Exts.get(namespace);
            
//			AxisModule axisModule = (AxisModule) ns2modules.get(namespace);

			if (policyExtension == null) {
				System.err.println("cannot find a PolicyExtension to process "
						+ namespace + "type assertions");
				continue;
			}

			Policy nPolicy = new Policy();
			ExactlyOne nXOR = new ExactlyOne();
			nPolicy.addTerm(nXOR);

			All nAND = (All) map
					.get(namespace);
			nXOR.addTerm(nAND);
            
			policyExtension.addMethodsToStub(document, rootElement, opName, nPolicy);
		}
	}

	private Document getEmptyDocument() {
		try {
			DocumentBuilder documentBuilder = DocumentBuilderFactory
					.newInstance().newDocumentBuilder();

			return documentBuilder.newDocument();
		} catch (ParserConfigurationException e) {
			throw new RuntimeException(e);
		}
	}
	
	class MTOMPolicyExtension implements PolicyExtension {
        
        private boolean setOnce = false;
        
		public void addMethodsToStub(Document document, Element element, QName operationName, Policy policy) {
            
            if (!setOnce) {
                 Object plainBase64PropertyMap = configuration.getProperty(XSLTConstants.PLAIN_BASE_64_PROPERTY_KEY);
                 configuration.putProperty(XSLTConstants.BASE_64_PROPERTY_KEY, plainBase64PropertyMap);
                
                 setOnce = true;
            }
                       
            Element optimizeContent = document.createElement("optimizeContent");
            Element opNameElement = document.createElement("opName");
            
            opNameElement.setAttribute("ns-url", operationName.getNamespaceURI());
            opNameElement.setAttribute("localName", operationName.getLocalPart());
            
            optimizeContent.appendChild(opNameElement);
            
            element.appendChild(optimizeContent);
		}
	}
    
    class EncodePolicyExtension implements PolicyExtension {
    	public void addMethodsToStub(Document document, Element element, QName operationName, Policy policy) {
            // TODO implement encoding
        }
    }
}
