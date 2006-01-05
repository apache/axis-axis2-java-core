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

import org.apache.axis2.description.AxisDescWSDLComponentFactory;
import org.apache.axis2.description.AxisMessage;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.AxisServiceGroup;
import org.apache.axis2.description.PolicyInclude;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.ws.policy.PolicyConstants;
import org.apache.ws.policy.PolicyReference;
import org.apache.wsdl.Component;
import org.apache.wsdl.WSDLBinding;
import org.apache.wsdl.WSDLBindingOperation;
import org.apache.wsdl.WSDLConstants;
import org.apache.wsdl.WSDLDescription;
import org.apache.wsdl.WSDLEndpoint;
import org.apache.wsdl.WSDLExtensibilityAttribute;
import org.apache.wsdl.WSDLInterface;
import org.apache.wsdl.WSDLOperation;
import org.apache.wsdl.WSDLService;
import org.apache.wsdl.extensions.ExtensionConstants;
import org.apache.wsdl.extensions.PolicyExtensibilityElement;
import org.apache.wsdl.extensions.impl.ExtensionFactoryImpl;
import org.apache.wsdl.impl.WSDLProcessingException;

import javax.xml.namespace.QName;
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

        AxisServiceGroup axisServiceGroup = axisService.getParent();
        //TODO : Sanka please be carefull , your code given NPEs all over the places
        AxisConfiguration axisConfiguration = axisServiceGroup.getParent();

        PolicyInclude servicePolicyInclude = axisService.getPolicyInclude();

        List policyList;

        // Policies defined in Axis2.xml
        policyList = axisConfiguration.getPolicyInclude().getPolicyElements(
                PolicyInclude.AXIS_POLICY);
        addPolicyAsExtElements(description, policyList, wsdlService,
                servicePolicyInclude);

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
        addPolicyAsExtAttributes(description, policyList, wsdlInterface,
                policyInclude);

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
                .getPolicyElements(PolicyInclude.BINDING_OPERATOIN_POLICY);
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

    //    private static void addPolicyToComponent(Policy policy, Component
    // component) {
    //        component.addExtensibilityElement(getExtensibilityElement(policy));
    //    }

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
}
