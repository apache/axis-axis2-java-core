package org.apache.axis2.client;

import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.RelatesTo;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.engine.MessageReceiver;

import java.util.HashMap;

/**
 * This is a MessageReceiver which is used on the client side to accept the 
 * messages (response) that come to the client. This correlates the incoming message to
 * the related messages and makes a call to the appropriate callback. 
 */

public class CallbackReceiver implements MessageReceiver {

    public static String SERVICE_NAME = "ClientService";

    private HashMap callbackstore;

    public CallbackReceiver() {
        callbackstore = new HashMap();
    }

    public void addCallback(String MsgID, Callback callback) {
        callbackstore.put(MsgID, callback);
    }

    public void receive(MessageContext messgeCtx) throws AxisFault {
        RelatesTo relatesTO = messgeCtx.getMessageInformationHeaders()
                .getRelatesTo();

        String messageID = relatesTO.getValue();
        Callback callback = (Callback) callbackstore.get(messageID);
        AsyncResult result = new AsyncResult(messgeCtx);
        if (callback != null) {
            callback.onComplete(result);
            callback.setComplete(true);
        } else {
            throw new AxisFault(
                    "The Callback realtes to MessageID " + messageID +
                    " is not found");
        }
    }

}
