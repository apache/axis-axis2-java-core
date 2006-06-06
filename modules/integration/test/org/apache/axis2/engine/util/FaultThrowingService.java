package org.apache.axis2.engine.util;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.OperationContext;
import org.apache.axis2.wsdl.WSDLConstants;

import javax.xml.namespace.QName;
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

public class FaultThrowingService {

    public static final String THROW_FAULT_AS_AXIS_FAULT = "ThrowFaultAsAxisFault";
    public static final String THROW_FAULT_WITH_MSG_CTXT = "ThrowFaultWithMsgCtxt";

    MessageContext inMessageContext;

    public void setOperationContext(OperationContext opContext) {
        try {
            inMessageContext = opContext.getMessageContext(WSDLConstants.MESSAGE_LABEL_IN_VALUE);
        } catch (AxisFault axisFault) {
            axisFault.printStackTrace();
        }
    }

    public OMElement echoWithFault(OMElement  echoOMElement) throws AxisFault {
        String text = echoOMElement.getText();
        if (THROW_FAULT_AS_AXIS_FAULT.equalsIgnoreCase(text)) {
            throw new AxisFault(new QName("http://test.org", "TestFault", "test"), "FaultReason", new Exception("This is a test Exception"));
        } else if (THROW_FAULT_WITH_MSG_CTXT.equalsIgnoreCase(text)) {

        } else {
           return echoOMElement;
        }
        return null;
    }
}
