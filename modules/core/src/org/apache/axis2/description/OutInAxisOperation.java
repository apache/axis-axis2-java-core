package org.apache.axis2.description;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.OperationContext;

import javax.xml.namespace.QName;
import java.util.HashMap;

/**
 * Author: Deepal Jayasinghe
 * Date: Oct 3, 2005
 * Time: 6:01:33 PM
 */
public class OutInAxisOperation extends InOutAxisOperation {
    public OutInAxisOperation() {
        super();
    }

    public OutInAxisOperation(QName name) {
        super(name);
    }

    public void addMessageContext(MessageContext msgContext, OperationContext opContext)
            throws AxisFault {
        HashMap mep = opContext.getMessageContexts();
        MessageContext immsgContext = (MessageContext) mep.get(MESSAGE_LABEL_IN_VALUE);
        MessageContext outmsgContext = (MessageContext) mep.get(MESSAGE_LABEL_OUT_VALUE);

        if ((immsgContext != null) && (outmsgContext != null)) {
            throw new AxisFault("Invalid message addition , operation context completed");
        }

        if (outmsgContext == null) {
            mep.put(MESSAGE_LABEL_OUT_VALUE, msgContext);
        } else {
            mep.put(MESSAGE_LABEL_IN_VALUE, msgContext);
            opContext.setComplete(true);
        }
    }
}
