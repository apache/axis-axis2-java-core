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

import java.util.HashMap;
import java.util.Iterator;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.description.AxisModule;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.util.PolicyAttachmentUtil;
import org.apache.axis2.wsdl.codegen.CodeGenConfiguration;
import org.apache.ws.policy.AndCompositeAssertion;
import org.apache.ws.policy.Policy;
import org.apache.ws.policy.PrimitiveAssertion;
import org.apache.ws.policy.XorCompositeAssertion;
import org.apache.wsdl.WSDLBinding;
import org.apache.wsdl.WSDLDescription;
import org.apache.wsdl.WSDLEndpoint;
import org.apache.wsdl.WSDLInterface;
import org.apache.wsdl.WSDLOperation;
import org.apache.wsdl.WSDLService;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * 
 * @author Sanka Samaranayake (sanka@apache.org)
 *
 */
public class PolicyEvaluator implements CodeGenExtension {

	CodeGenConfiguration configuration;

	HashMap ns2modules = new HashMap();

	PolicyAttachmentUtil util;
	
	Element rootElement;
	
	public PolicyEvaluator() {
	}

	public void init(CodeGenConfiguration configuration) {
		this.configuration = configuration;
		util = new PolicyAttachmentUtil(configuration.getWom());
		
		String repository = configuration.getRepositoryPath();
		repository = "/home/sanka/jakarta-tomcat-4.1.30/webapps/axis2/WEB-INF"; // configuration.getRepository;

		try {
			ConfigurationContext configurationCtx = ConfigurationContextFactory
					.createConfigurationContextFromFileSystem(repository, null);
			AxisConfiguration axisConfiguration = configurationCtx
					.getAxisConfiguration();

			for (Iterator iterator = axisConfiguration.getModules().values()
					.iterator(); iterator.hasNext();) {
				AxisModule axisModule = (AxisModule) iterator.next();
				String[] namespaces = axisModule.getSupportedPolicyNamespaces();

				for (int i = 0; i < namespaces.length; i++) {
					ns2modules.put(namespaces[i], axisModule);
				}
			}

		} catch (Exception e) {
			System.err
					.println("cannot locate repository : policy will not be supported");
		}
	}

	public void engage() {
		WSDLDescription womDescription = configuration.getWom();

		String serviceName = configuration.getServiceName();
		WSDLService wsdlService = womDescription.getService(new QName(
				serviceName));

		String port = configuration.getPortName();
		WSDLEndpoint wsdlEndpoint = wsdlService.getEndpoint(new QName(port));

		WSDLBinding wsdlBinding = wsdlEndpoint.getBinding();
		WSDLInterface wsdlInterface = wsdlBinding.getBoundInterface();
		
		Element rootElement = getRootElement();

		for (Iterator iterator = wsdlInterface.getOperations().values()
				.iterator(); iterator.hasNext();) {
			WSDLOperation wsdlOperation = (WSDLOperation) iterator.next();
			Policy policy = util.getOperationPolicy(wsdlEndpoint.getName(),
					wsdlOperation.getName());
			
			processPolicies(policy, wsdlEndpoint, wsdlOperation, rootElement);
		}
	}

	private void processPolicies(Policy policy, WSDLEndpoint wsdlEndpoint,
			WSDLOperation operation, Element element) {

		HashMap map = new HashMap();

		XorCompositeAssertion XOR = (XorCompositeAssertion) policy.getTerms()
				.get(0);
		AndCompositeAssertion AND = (AndCompositeAssertion) XOR.getTerms().get(
				0);

		for (Iterator iterator = AND.getTerms().iterator(); iterator.hasNext();) {

			AndCompositeAssertion nAND = new AndCompositeAssertion();
			PrimitiveAssertion pa = (PrimitiveAssertion) iterator.next();

			String namespace = pa.getName().getNamespaceURI();

			while (iterator.hasNext()) {
				pa = (PrimitiveAssertion) iterator.next();

				if (namespace.equals(pa.getName().getNamespaceURI())) {
					nAND.addTerm(pa);
				}
			}

			map.put(namespace, nAND);
			AND.getTerms().removeAll(nAND.getTerms());
		}

		for (Iterator iterator = map.keySet().iterator(); iterator.hasNext();) {
			String namespace = (String) iterator.next();
			AxisModule axisModule = (AxisModule) ns2modules.get(namespace);

			if (axisModule == null) {
				System.err.println("cannot find a module to process "
						+ namespace + "type assertions");
				continue;
			}

			if (!(axisModule instanceof CodeGenPolicyExtension)) {
				System.err
						.println(axisModule.getName()
								+ " module doesnt provde a PolicyExtension to process policies");
				continue;
			}

			PolicyExtension policyExtension = ((CodeGenPolicyExtension) axisModule)
					.getPolicyExtension();

			Policy nPolicy = new Policy();
			XorCompositeAssertion nXOR = new XorCompositeAssertion();
			nPolicy.addTerm(nXOR);

			AndCompositeAssertion nAND = (AndCompositeAssertion) map
					.get(namespace);
			nXOR.addTerm(nAND);

			policyExtension.addMethodsToStub(nPolicy, element);
		}

		configuration.putProperty("stubMethods", element);
	}

	private Element getRootElement() {
		Document document = getEmptyDocument();
		return document.createElement("stubMethods");		
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
}
