package org.apache.axis2.description;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.OperationContext;
import org.apache.wsdl.WSDLConstants;

import javax.xml.namespace.QName;
import java.util.ArrayList;

/**
 * Author: Deepal Jayasinghe
 * Date: Oct 3, 2005
 * Time: 2:06:31 PM
 */
public class InOnlyAxisOperation extends AxisOperation {
    private AxisMessage inFaultMessage;
    private AxisMessage inMessage;
    private AxisMessage outFaultMessage;

    // this is just to store the chain , we don't use it
    private ArrayList outPhase;

    public InOnlyAxisOperation() {
        super();
        createMessage();
    }

    public InOnlyAxisOperation(QName name) {
        super(name);
        createMessage();
    }

    public void addMessage(AxisMessage message, String label) {
        if (WSDLConstants.MESSAGE_LABEL_IN_VALUE.equals(label)) {
            inMessage = message;
        } else {
            throw new UnsupportedOperationException("Not yet implemented");
        }
    }

    public void addMessageContext(MessageContext msgContext, OperationContext opContext)
            throws AxisFault {
        if (!opContext.isComplete()) {
            opContext.getMessageContexts().put(MESSAGE_LABEL_IN_VALUE, msgContext);
            opContext.setComplete(true);
        } else {
            throw new AxisFault("Invalid messge addition , operation context completed");
        }
    }

    private void createMessage() {
        inMessage = new AxisMessage();
        inFaultMessage = new AxisMessage();
        outFaultMessage = new AxisMessage();
        outPhase = new ArrayList();
    }

    public AxisMessage getMessage(String label) {
        if (WSDLConstants.MESSAGE_LABEL_IN_VALUE.equals(label)) {
            return inMessage;
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
        return outPhase;
    }

    public ArrayList getRemainingPhasesInFlow() {
        return inMessage.getMessageFlow();
    }

    public void setPhasesInFaultFlow(ArrayList list) {
        inFaultMessage.setMessageFlow(list);
    }

    public void setPhasesOutFaultFlow(ArrayList list) {
        outFaultMessage.setMessageFlow(list);
    }

    public void setPhasesOutFlow(ArrayList list) {
        outPhase = list;
    }

    public void setRemainingPhasesInFlow(ArrayList list) {
        inMessage.setMessageFlow(list);
    }
}
