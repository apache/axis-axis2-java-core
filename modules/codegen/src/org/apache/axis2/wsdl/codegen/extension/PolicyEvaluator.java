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
import org.apache.axis2.modules.Module;
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
		
		if (repository == null) {
			System.err.println("Warning: repository is not specified");
			System.err.println("policy will not be supported");
			return;			
		}
		

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

				for (int i = 0; i < namespaces.length; i++) {
					ns2modules.put(namespaces[i], axisModule);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			System.err
					.println("cannot create repository : policy will not be supported");
		}
		
		// 
		configuration.putProperty("policyExtensionTemplate", "/org/apache/axis2/wsdl/template/java/PolicyExtensionTemplate.xsl");
	}

	public void engage() {
		if (ns2modules.isEmpty()) {
			System.err.println("Any policy supported module not found");
			return;
		}
		WSDLDescription womDescription = configuration.getWom();
		String serviceName = configuration.getServiceName();

		Document document = getEmptyDocument();
		Element rootElement = document.createElement("stubMethods");

		WSDLService wsdlService = null;
		WSDLInterface wsdlInterface = null;

		if (serviceName != null) {
			wsdlService = womDescription.getService(new QName(serviceName));
		} else {
			for (Iterator iterator = womDescription.getServices().values()
					.iterator(); iterator.hasNext();) {
				wsdlService = (WSDLService) iterator.next();
				serviceName = wsdlService.getName().getLocalPart();
				configuration.setServiceName(serviceName);
				break;
			}
		}

		if (wsdlService != null) {

			String port = configuration.getPortName();
			WSDLEndpoint wsdlEndpoint = null;

			if (port == null) {
				for (Iterator iterator = wsdlService.getEndpoints().values()
						.iterator(); iterator.hasNext();) {
					wsdlEndpoint = (WSDLEndpoint) iterator.next();
					port = wsdlEndpoint.getName().getLocalPart();
					configuration.setPortName(port);
					break;

				}
			} else {
				wsdlEndpoint = wsdlService.getEndpoint(new QName(port));
			}
			if (wsdlEndpoint == null) {
				System.err.println("no wsdl:port found for the service");
				return;
			}

			WSDLBinding wsdlBinding = wsdlEndpoint.getBinding();
			wsdlInterface = wsdlBinding.getBoundInterface();

			for (Iterator iterator = wsdlInterface.getOperations().values()
					.iterator(); iterator.hasNext();) {
				WSDLOperation wsdlOperation = (WSDLOperation) iterator.next();
				Policy policy = util.getOperationPolicy(wsdlEndpoint.getName(),
						wsdlOperation.getName());

				if (policy != null) {
					processPolicies(document, rootElement, policy,
							wsdlEndpoint, wsdlOperation);
				}
			}

		}

		for (Iterator iterator = womDescription.getWsdlInterfaces().values()
				.iterator(); iterator.hasNext();) {
			wsdlInterface = (WSDLInterface) iterator.next();
			break;
		}

		if (wsdlInterface == null) {
			System.err.println("cannot find a wsdl:Service or a wsdl:portType");
			// TODO exception ?
			return;
		}
		
		// TODO wsdl:portType processing..
		
	}

	private void processPolicies(Document document, Element rootElement,
			Policy policy, WSDLEndpoint wsdlEndpoint, WSDLOperation operation) {
		if (!policy.isNormalized()) {
			policy = (Policy) policy.normalize();
		}

		HashMap map = new HashMap();

		XorCompositeAssertion XOR = (XorCompositeAssertion) policy.getTerms()
				.get(0);
		AndCompositeAssertion AND = (AndCompositeAssertion) XOR.getTerms().get(
				0);

		for (Iterator iterator = AND.getTerms().iterator(); iterator.hasNext();) {

			AndCompositeAssertion nAND = new AndCompositeAssertion();
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
			AxisModule axisModule = (AxisModule) ns2modules.get(namespace);

			if (axisModule == null) {
				System.err.println("cannot find a module to process "
						+ namespace + "type assertions");
				continue;
			}
			
			Module module = axisModule.getModule();

			if (!(module instanceof CodeGenPolicyExtension)) {
				System.err
						.println(axisModule.getName()
								+ " module doesnt provde a PolicyExtension to process policies");
				continue;
			}

			PolicyExtension policyExtension = ((CodeGenPolicyExtension) module)
					.getPolicyExtension();

			Policy nPolicy = new Policy();
			XorCompositeAssertion nXOR = new XorCompositeAssertion();
			nPolicy.addTerm(nXOR);

			AndCompositeAssertion nAND = (AndCompositeAssertion) map
					.get(namespace);
			nXOR.addTerm(nAND);

			policyExtension.addMethodsToStub(document, rootElement, nPolicy);
		}

		configuration.putProperty("stubMethods", rootElement);
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
