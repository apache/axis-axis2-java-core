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

import java.util.Iterator;

import javax.xml.namespace.QName;

import org.apache.axis2.description.AxisMessage;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.ws.policy.Policy;
import org.apache.wsdl.Component;
import org.apache.wsdl.WSDLBinding;
import org.apache.wsdl.WSDLBindingMessageReference;
import org.apache.wsdl.WSDLBindingOperation;
import org.apache.wsdl.WSDLConstants;
import org.apache.wsdl.WSDLDescription;
import org.apache.wsdl.WSDLEndpoint;
import org.apache.wsdl.WSDLExtensibilityElement;
import org.apache.wsdl.WSDLService;
import org.apache.wsdl.extensions.ExtensionConstants;
import org.apache.wsdl.extensions.impl.ExtensionFactoryImpl;
import org.apache.wsdl.impl.WSDLProcessingException;

/**
 * @author Sanka Samaranayake (sanka@apache.org)
 */
public class PolicyUtil {
    public static void populatePolicy(WSDLDescription description,
            AxisService axisService) {
        WSDLService wsdlService = description.getService(new QName(axisService
                .getName()));
        populatePolicy(wsdlService, axisService);
    }

    public static void populatePolicy(WSDLService wsdlService,
            AxisService axisService) {
        Policy servicePolicy = axisService.getPolicyInclude().getPolicy();

        if (servicePolicy != null) {
            addPolicyToComponent(servicePolicy, wsdlService);
        }

        // TODO CHECK ME //////////////////////////////////

        Iterator wsdlEndpoints = wsdlService.getEndpoints().values().iterator();
        if (!wsdlEndpoints.hasNext()) {
            throw new WSDLProcessingException("should at least one endpoints");
        }

        WSDLEndpoint wsdlEndpoint = (WSDLEndpoint) wsdlEndpoints.next();
        WSDLBinding wsdlBinding = wsdlEndpoint.getBinding();

        ////////////////////////////////////////////////////

        Iterator wsdlOperations = wsdlBinding.getBindingOperations().values()
                .iterator();
        WSDLBindingOperation wsdlBindingOperation;

        while (wsdlOperations.hasNext()) {
            wsdlBindingOperation = (WSDLBindingOperation) wsdlOperations.next();
            populatePolicy(wsdlBindingOperation, axisService
                    .getOperation(wsdlBindingOperation.getName()));
        }

    }

    private static void populatePolicy(
            WSDLBindingOperation wsdlBindingOperation,
            AxisOperation axisOperation) {
        Policy operationPolicy = axisOperation.getPolicyInclude().getPolicy();

        if (operationPolicy != null) {
            addPolicyToComponent(operationPolicy, wsdlBindingOperation);
        }

        if (WSDLConstants.MEP_URI_IN_ONLY.equals(axisOperation
                .getMessageExchangePattern())) {
            populatePolicy(wsdlBindingOperation.getInput(), axisOperation
                    .getMessage(WSDLConstants.MESSAGE_LABEL_IN_VALUE));

        } else if (WSDLConstants.MEP_URI_IN_OUT.equals(axisOperation
                .getMessageExchangePattern())) {
            populatePolicy(wsdlBindingOperation.getInput(), axisOperation
                    .getMessage(WSDLConstants.MESSAGE_LABEL_IN_VALUE));
            populatePolicy(wsdlBindingOperation.getOutput(), axisOperation
                    .getMessage(WSDLConstants.MESSAGE_LABEL_OUT_VALUE));
        }
    }

    private static void populatePolicy(
            WSDLBindingMessageReference wsdlBindingMsgReference,
            AxisMessage axisMessage) {
        Policy messagePolicy = axisMessage.getPolicyInclude().getPolicy();

        if (messagePolicy != null) {
            addPolicyToComponent(messagePolicy, wsdlBindingMsgReference);
        }
    }

    private static void addPolicyToComponent(Policy policy, Component component) {
        component.addExtensibilityElement(getExtensibilityElement(policy));
    }

    private static WSDLExtensibilityElement getExtensibilityElement(
            Policy policy) {
        WSDLExtensibilityElement element = (new ExtensionFactoryImpl())
                .getExtensionElement(ExtensionConstants.POLICY);
        return element;
    }
}
