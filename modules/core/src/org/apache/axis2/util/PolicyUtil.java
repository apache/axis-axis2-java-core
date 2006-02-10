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

package org.apache.axis2.util;

import org.apache.axis2.description.*;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.ws.policy.Policy;
import org.apache.ws.policy.PolicyConstants;
import org.apache.ws.policy.PolicyReference;
import org.apache.ws.policy.util.PolicyFactory;
import org.apache.ws.policy.util.PolicyWriter;
import org.apache.ws.policy.util.StAXPolicyWriter;
import org.apache.wsdl.*;
import org.apache.wsdl.extensions.ExtensionConstants;
import org.apache.wsdl.extensions.PolicyExtensibilityElement;
import org.apache.wsdl.extensions.impl.ExtensionFactoryImpl;
import org.apache.wsdl.impl.WSDLProcessingException;

import javax.xml.namespace.QName;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;

public class PolicyUtil {

	public static void populatePolicy(WSDLDescription description,
			AxisService axisService) {
		WSDLService wsdlService = description.getService(new QName(axisService
				.getName()));
		populatePolicy(description, wsdlService, axisService);
	}

	private static void populatePolicy(WSDLDescription description,
			WSDLService wsdlService, AxisService axisService) {

		AxisServiceGroup axisServiceGroup = null;
		AxisConfiguration axisConfiguration = null;

		axisServiceGroup = (AxisServiceGroup) axisService.getParent();

		if (axisServiceGroup != null) {
			axisConfiguration = (AxisConfiguration) axisServiceGroup
					.getParent();
		}

		PolicyInclude servicePolicyInclude = axisService.getPolicyInclude();

		List policyList;

		// Policies defined in Axis2.xml
		if (axisConfiguration != null) {
			policyList = axisConfiguration.getPolicyInclude()
					.getPolicyElements(PolicyInclude.AXIS_POLICY);
			addPolicyAsExtElements(description, policyList, wsdlService,
					servicePolicyInclude);
		}

		// Policies defined in wsdl:Service
		policyList = servicePolicyInclude
				.getPolicyElements(PolicyInclude.SERVICE_POLICY);
		addPolicyAsExtElements(description, policyList, wsdlService,
				servicePolicyInclude);

		Iterator wsdlEndpoints = wsdlService.getEndpoints().values().iterator();
		if (!wsdlEndpoints.hasNext()) {
			throw new WSDLProcessingException("should at least one endpoints");
		}

		WSDLEndpoint wsdlEndpoint = (WSDLEndpoint) wsdlEndpoints.next();
		populatePolicy(description, wsdlEndpoint, axisService);
	}

	private static void populatePolicy(WSDLDescription description,
			WSDLEndpoint wsdlEndpoint, AxisService axisService) {
		PolicyInclude policyInclude = axisService.getPolicyInclude();
		List policyList = policyInclude
				.getPolicyElements(PolicyInclude.PORT_POLICY);
		addPolicyAsExtElements(description, policyList, wsdlEndpoint,
				policyInclude);

		WSDLBinding wsdlBinding = wsdlEndpoint.getBinding();
		populatePolicy(description, wsdlBinding, axisService);
		WSDLInterface wsdlInterface = wsdlBinding.getBoundInterface();
		populatePolicy(description, wsdlInterface, axisService);

	}

	private static void populatePolicy(WSDLDescription description,
			WSDLInterface wsdlInterface, AxisService axisService) {
		PolicyInclude policyInclude = axisService.getPolicyInclude();
		List policyList = policyInclude
				.getPolicyElements(PolicyInclude.PORT_TYPE_POLICY);
		// addPolicyAsExtAttributes(description, policyList, wsdlInterface,
		// policyInclude);

		Iterator wsdlOperations = wsdlInterface.getOperations().values()
				.iterator();
		WSDLOperation wsdlOperation;

		while (wsdlOperations.hasNext()) {
			wsdlOperation = (WSDLOperation) wsdlOperations.next();
			populatePolicy(description, wsdlOperation, axisService
					.getOperation(wsdlOperation.getName()));

		}
	}

	private static void populatePolicy(WSDLDescription description,
			WSDLOperation wsdlOperation, AxisOperation axisOperation) {

		PolicyInclude policyInclude = axisOperation.getPolicyInclude();

		// wsdl:PortType -> wsdl:Operation
		List policyList = policyInclude
				.getPolicyElements(PolicyInclude.OPERATION_POLICY);
		addPolicyAsExtElements(description, policyList, wsdlOperation,
				policyInclude);

		if (WSDLConstants.MEP_URI_IN_ONLY.equals(axisOperation
				.getMessageExchangePattern())) {
			AxisMessage input = axisOperation
					.getMessage(WSDLConstants.MESSAGE_LABEL_IN_VALUE);
			PolicyInclude policyInclude2 = input.getPolicyInclude();

			// wsdl:PortType -> wsdl:Operation -> wsdl:Input
			List policyList2 = policyInclude2
					.getPolicyElements(PolicyInclude.INPUT_POLICY);
			addPolicyAsExtAttributes(description, policyList2, wsdlOperation
					.getInputMessage(), policyInclude2);

		} else if (WSDLConstants.MEP_URI_IN_OUT.equals(axisOperation
				.getMessageExchangePattern())) {
			PolicyInclude policyInclude2;
			List policyList2;

			AxisMessage input = axisOperation
					.getMessage(WSDLConstants.MESSAGE_LABEL_IN_VALUE);
			policyInclude2 = input.getPolicyInclude();

			// wsdl:PortType -> wsdl:Operation -> wsdl:Input
			policyList2 = policyInclude2
					.getPolicyElements(PolicyInclude.INPUT_POLICY);
			addPolicyAsExtAttributes(description, policyList2, wsdlOperation
					.getInputMessage(), policyInclude2);

			AxisMessage output = axisOperation
					.getMessage(WSDLConstants.MESSAGE_LABEL_OUT_VALUE);
			policyInclude2 = output.getPolicyInclude();

			// wsdl:PortType -> wsdl:Operation -> wsdl:Output
			policyList2 = policyInclude2
					.getPolicyElements(PolicyInclude.OUTPUT_POLICY);
			addPolicyAsExtAttributes(description, policyList2, wsdlOperation
					.getOutputMessage(), policyInclude2);
		}
	}

	private static void populatePolicy(WSDLDescription description,
			WSDLBinding wsdlBinding, AxisService axisService) {
		PolicyInclude policyInclude = axisService.getPolicyInclude();

		List policyList = policyInclude
				.getPolicyElements(PolicyInclude.AXIS_SERVICE_POLICY);
		addPolicyAsExtElements(description, policyList, wsdlBinding,
				policyInclude);

		policyList = policyInclude
				.getPolicyElements(PolicyInclude.BINDING_POLICY);
		addPolicyAsExtElements(description, policyList, wsdlBinding,
				policyInclude);

		Iterator wsdlOperations = wsdlBinding.getBindingOperations().values()
				.iterator();
		WSDLBindingOperation wsdlBindingOperation;

		while (wsdlOperations.hasNext()) {
			wsdlBindingOperation = (WSDLBindingOperation) wsdlOperations.next();
			populatePolicy(description, wsdlBindingOperation, axisService
					.getOperation(wsdlBindingOperation.getName()));
		}
	}

	private static void populatePolicy(WSDLDescription description,
			WSDLBindingOperation wsdlBindingOperation,
			AxisOperation axisOperation) {

		PolicyInclude policyInclude = axisOperation.getPolicyInclude();
		List policyList = policyInclude
				.getPolicyElements(PolicyInclude.BINDING_OPERATION_POLICY);
		addPolicyAsExtElements(description, policyList, wsdlBindingOperation,
				policyInclude);

		//
		policyList = policyInclude
				.getPolicyElements(PolicyInclude.AXIS_OPERATION_POLICY);
		addPolicyAsExtElements(description, policyList, wsdlBindingOperation,
				policyInclude);

		if (WSDLConstants.MEP_URI_IN_ONLY.equals(axisOperation
				.getMessageExchangePattern())) {
			AxisMessage input = axisOperation
					.getMessage(WSDLConstants.MESSAGE_LABEL_IN_VALUE);
			PolicyInclude policyInclude2 = input.getPolicyInclude();

			// wsdl:Binding -> wsdl:Operation -> wsdl:Input
			List policyList2 = policyInclude2
					.getPolicyElements(PolicyInclude.INPUT_POLICY);
			addPolicyAsExtElements(description, policyList2,
					wsdlBindingOperation.getInput(), policyInclude2);

			//
			policyList2 = policyInclude2
					.getPolicyElements(PolicyInclude.AXIS_MESSAGE_POLICY);
			addPolicyAsExtElements(description, policyList2,
					wsdlBindingOperation.getInput(), policyInclude2);

		} else if (WSDLConstants.MEP_URI_IN_OUT.equals(axisOperation
				.getMessageExchangePattern())) {
			PolicyInclude policyInclude2;
			List policyList2;

			AxisMessage input = axisOperation
					.getMessage(WSDLConstants.MESSAGE_LABEL_IN_VALUE);
			policyInclude2 = input.getPolicyInclude();

			// wsdl:Binding -> wsdl:Operation -> wsdl:Input
			policyList2 = policyInclude2
					.getPolicyElements(PolicyInclude.INPUT_POLICY);
			addPolicyAsExtElements(description, policyList2,
					wsdlBindingOperation.getInput(), policyInclude2);
			// 
			policyList2 = policyInclude2
					.getPolicyElements(PolicyInclude.AXIS_MESSAGE_POLICY);
			addPolicyAsExtElements(description, policyList2,
					wsdlBindingOperation.getInput(), policyInclude2);

			AxisMessage output = axisOperation
					.getMessage(WSDLConstants.MESSAGE_LABEL_OUT_VALUE);
			policyInclude2 = output.getPolicyInclude();

			// wsdl:Binding -> wsdl:Operation -> wsdl:Output
			policyList2 = policyInclude2
					.getPolicyElements(PolicyInclude.OUTPUT_POLICY);
			addPolicyAsExtElements(description, policyList2,
					wsdlBindingOperation.getOutput(), policyInclude2);
			// 
			policyList2 = policyInclude2
					.getPolicyElements(PolicyInclude.AXIS_MESSAGE_POLICY);
			addPolicyAsExtElements(description, policyList2,
					wsdlBindingOperation.getInput(), policyInclude2);
		}
	}

	// private static void addPolicyToComponent(Policy policy, Component
	// component) {
	// component.addExtensibilityElement(getExtensibilityElement(policy));
	// }

	private static PolicyExtensibilityElement getExtensibilityElement(
			Object policyElement) {
		PolicyExtensibilityElement element = (PolicyExtensibilityElement) (new ExtensionFactoryImpl())
				.getExtensionElement(ExtensionConstants.POLICY);
		element.setPolicyElement(policyElement);
		return element;
	}

	private static WSDLExtensibilityAttribute getExtensibilitiyAttribute(
			PolicyReference policyReference) {
		WSDLExtensibilityAttribute extensibilityAttribute = new AxisDescWSDLComponentFactory()
				.createWSDLExtensibilityAttribute();
		extensibilityAttribute.setKey(new QName(
				PolicyConstants.WSU_NAMESPACE_URI, "PolicyURIs"));
		extensibilityAttribute.setValue(new QName(policyReference
				.getPolicyURIString()));
		return extensibilityAttribute;
	}

	private static void addPolicyAsExtElements(WSDLDescription description,
			List policyList, Component component, PolicyInclude policyInclude) {
		Iterator policyElementIterator = policyList.iterator();
		Object policyElement;

		while (policyElementIterator.hasNext()) {
			policyElement = policyElementIterator.next();

			if (policyElement instanceof PolicyReference) {
				String policyURIString = ((PolicyReference) policyElement)
						.getPolicyURIString();
				description
						.addExtensibilityElement(getExtensibilityElement(policyInclude
								.getPolicy(policyURIString)));
			}

			component
					.addExtensibilityElement(getExtensibilityElement(policyElement));
		}
	}

	private static void addPolicyAsExtAttributes(WSDLDescription description,
			List policyList, Component component, PolicyInclude policyInclude) {
		Iterator policyElementIterator = policyList.iterator();
		Object policyElement;

		while (policyElementIterator.hasNext()) {
			policyElement = policyElementIterator.next();

			if (policyElement instanceof PolicyReference) {
				String policyURIString = ((PolicyReference) policyElement)
						.getPolicyURIString();
				component
						.addExtensibleAttributes(getExtensibilitiyAttribute((PolicyReference) policyElement));
				description
						.addExtensibilityElement(getExtensibilityElement(policyInclude
								.getPolicy(policyURIString)));

			}
		}
	}

	public static void writePolicy(PolicyInclude policy, OutputStream out) {
		if (policy != null) {
			Policy pl = policy.getEffectivePolicy();
			if (pl != null) {
				PolicyWriter write = PolicyFactory
						.getPolicyWriter(PolicyFactory.StAX_POLICY_WRITER);
				write.writePolicy(pl, out);
				try {
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else {
				PrintWriter write = new PrintWriter(out);
				write.write("<policy>no policy found</policy>");
				write.flush();
				write.close();
			}
		} else {
			PrintWriter write = new PrintWriter(out);
			write.write("<policy>no policy found</policy>");
			write.flush();
			write.close();
		}
	}

	public static String getPolicyAsString(Policy policy) {
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		StAXPolicyWriter pwtr = (StAXPolicyWriter) PolicyFactory
				.getPolicyWriter(PolicyFactory.StAX_POLICY_WRITER);
		
		pwtr.writePolicy(policy, baos);
		return getSafeString(baos.toString());
	}
	
	private static String getSafeString(String unsafeString) {
		StringBuffer sbuf = new StringBuffer();
		
		char[] chars = unsafeString.toCharArray();
		
		for (int i = 0; i < chars.length; i++) {
			char c = chars[i];
			
			switch (c) {
				case '\\' :
					sbuf.append('\\'); sbuf.append('\\');
					break;
				case '"' :
					sbuf.append('\\'); sbuf.append('"');
					break;
				case '\n':
					sbuf.append('\\'); sbuf.append('n'); 
					break;
				case '\r':
					sbuf.append('\\'); sbuf.append('r'); 
					break;
				default :
					sbuf.append(c);					
			}			
		}
		
		return sbuf.toString();
	}
}
