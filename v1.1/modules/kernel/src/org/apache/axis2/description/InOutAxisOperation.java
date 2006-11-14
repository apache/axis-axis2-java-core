/*
* Copyright 2004,2006 The Apache Software Foundation.
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
package org.apache.axis2.description;

import org.apache.axis2.AxisFault;
import org.apache.axis2.wsdl.WSDLConstants;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.OperationContext;
import org.apache.axis2.i18n.Messages;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.HashMap;

public class InOutAxisOperation extends AxisOperation {
    private AxisMessage inFaultMessage;
//    private AxisMessage inMessage;
    private AxisMessage outFaultMessage;
//    private AxisMessage outMessage;

    public InOutAxisOperation() {
        super();
        createMessages();
    }

    public InOutAxisOperation(QName name) {
        super(name);
        createMessages();
    }

    public void addMessage(AxisMessage message, String label) {
        if (WSDLConstants.MESSAGE_LABEL_OUT_VALUE.equals(label)) {
            addChild("outMessage", message);
        } else if (WSDLConstants.MESSAGE_LABEL_IN_VALUE.equals(label)) {
            addChild("inMessage", message);
        } else if (WSDLConstants.MESSAGE_LABEL_FAULT_VALUE.equals(label)) {
            addChild("faultMessage", message);
        } else {
            throw new UnsupportedOperationException("Not yet implemented");
        }
    }

    public void addMessageContext(MessageContext msgContext, OperationContext opContext)
            throws AxisFault {
        HashMap mep = opContext.getMessageContexts();
        MessageContext inMsgContext = (MessageContext) mep.get(MESSAGE_LABEL_IN_VALUE);
        MessageContext outmsgContext = (MessageContext) mep.get(MESSAGE_LABEL_OUT_VALUE);

        if ((inMsgContext != null) && (outmsgContext != null)) {
            throw new AxisFault(Messages.getMessage("mepcompleted"));
        }

        if (inMsgContext == null) {
            mep.put(MESSAGE_LABEL_IN_VALUE, msgContext);
        } else {
            mep.put(MESSAGE_LABEL_OUT_VALUE, msgContext);
            opContext.setComplete(true);
            opContext.cleanup();
        }
    }

    public void addFaultMessageContext(MessageContext msgContext, OperationContext opContext) throws AxisFault {
        HashMap mep = opContext.getMessageContexts();
        MessageContext faultMessageCtxt = (MessageContext) mep.get(MESSAGE_LABEL_FAULT_VALUE);

        if (faultMessageCtxt != null) {
             throw new AxisFault(Messages.getMessage("mepcompleted"));
        } else {
            mep.put(MESSAGE_LABEL_FAULT_VALUE, msgContext);
            opContext.setComplete(true);
            opContext.cleanup();
        }

    }

    private void createMessages() {
//        inMessage = new AxisMessage();
//        inMessage.setDirection(WSDLConstants.WSDL_MESSAGE_DIRECTION_IN);
//        inMessage.setParent(this);

        AxisMessage inMessage = new AxisMessage();
        inMessage.setDirection(WSDLConstants.WSDL_MESSAGE_DIRECTION_IN);
        inMessage.setParent(this);
        addChild("inMessage", inMessage);

        inFaultMessage = new AxisMessage();
        inFaultMessage.setParent(this);

        outFaultMessage = new AxisMessage();
        outFaultMessage.setParent(this);

//        outMessage = new AxisMessage();
//        outMessage.setDirection(WSDLConstants.WSDL_MESSAGE_DIRECTION_OUT);
//        outMessage.setParent(this);

        AxisMessage outMessage = new AxisMessage();
        outMessage.setDirection(WSDLConstants.WSDL_MESSAGE_DIRECTION_OUT);
        outMessage.setParent(this);
        addChild("outMessage", outMessage);


    }

    public AxisMessage getMessage(String label) {
        if (WSDLConstants.MESSAGE_LABEL_OUT_VALUE.equals(label)) {
            return (AxisMessage) getChild("outMessage");
        } else if (WSDLConstants.MESSAGE_LABEL_IN_VALUE.equals(label)) {
            return (AxisMessage) getChild("inMessage");
        } else {
            throw new UnsupportedOperationException("Not yet implemented");
        }
    }

    public ArrayList getPhasesInFaultFlow() {
        return inFaultMessage.getMessageFlow();
    }

    public ArrayList getPhasesOutFaultFlow() {
        return outFaultMessage.getMessageFlow();
    }

    public ArrayList getPhasesOutFlow() {
        return ((AxisMessage) getChild("outMessage")).getMessageFlow();
    }

    public ArrayList getRemainingPhasesInFlow() {
        return ((AxisMessage) getChild("inMessage")).getMessageFlow();
    }

    public void setPhasesInFaultFlow(ArrayList list) {
        inFaultMessage.setMessageFlow(list);
    }

    public void setPhasesOutFaultFlow(ArrayList list) {
        outFaultMessage.setMessageFlow(list);
    }

    public void setPhasesOutFlow(ArrayList list) {
        ((AxisMessage) getChild("outMessage")).setMessageFlow(list);
    }

    public void setRemainingPhasesInFlow(ArrayList list) {
        ((AxisMessage) getChild("inMessage")).setMessageFlow(list);
    }
}
