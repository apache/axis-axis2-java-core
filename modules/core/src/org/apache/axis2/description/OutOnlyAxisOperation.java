package org.apache.axis2.description;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.OperationContext;
import org.apache.wsdl.WSDLConstants;
import org.apache.wsdl.WSDLOperation;

import javax.xml.namespace.QName;
import java.util.ArrayList;
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
*
*/

public class OutOnlyAxisOperation extends AxisOperation {

    private AxisMessage outMessage;
    private AxisMessage inFaultMessage;
    private AxisMessage outFaultMessage;

    //just to keep the inflow , there wont be any usage
    private ArrayList inPhases;

    public OutOnlyAxisOperation(WSDLOperation wsdloperation) {
        super(wsdloperation);
        createMessage();
    }

    private void createMessage() {
        outMessage = new AxisMessage();
        inFaultMessage = new AxisMessage();
        outFaultMessage = new AxisMessage();
        inPhases = new ArrayList();
    }

    public OutOnlyAxisOperation() {
        super();
        createMessage();
    }

    public OutOnlyAxisOperation(QName name) {
        super(name);
        createMessage();
    }

    public ArrayList getPhasesInFaultFlow() {
        return inFaultMessage.getMessageFlow();
    }

    public ArrayList getPhasesOutFaultFlow() {
        return outFaultMessage.getMessageFlow();
    }

    public ArrayList getPhasesOutFlow() {
        return outMessage.getMessageFlow();
    }

    public ArrayList getRemainingPhasesInFlow() {
        return inPhases;
    }

    public AxisMessage getMessage(String label) {
        if (WSDLConstants.MESSAGE_LABEL_OUT_VALUE.equals(label)) {
            return outMessage;
        } else {
            throw new UnsupportedOperationException("Not yet implemented");
        }
    }

    public void setPhasesInFaultFlow(ArrayList list) {
        inFaultMessage.setMessageFlow(list);
    }

    public void setPhasesOutFaultFlow(ArrayList list) {
        outFaultMessage.setMessageFlow(list);
    }

    public void setPhasesOutFlow(ArrayList list) {
        outMessage.setMessageFlow(list);
    }

    public void setRemainingPhasesInFlow(ArrayList list) {
        inPhases = list;
    }

    public void addMessage(AxisMessage message, String label) {
        if (WSDLConstants.MESSAGE_LABEL_OUT_VALUE.equals(label)) {
            outMessage = message;
        } else {
            throw new UnsupportedOperationException("Not yet implemented");
        }
    }

    public void addMessageContext(MessageContext msgContext, OperationContext opContext)
            throws AxisFault {
        if (!opContext.isComplete()) {
            opContext.getMessageContexts().put(MESSAGE_LABEL_OUT_VALUE, msgContext);
            opContext.setComplete(true);
        } else {
            throw new AxisFault("Invalid messge addition , operation context completed");
        }
    }
}