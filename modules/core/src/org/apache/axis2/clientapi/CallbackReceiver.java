package org.apache.axis2.clientapi;

import java.util.HashMap;

import org.apache.axis2.addressing.miheaders.RelatesTo;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.engine.MessageReceiver;
import org.apache.axis2.AxisFault;

/**
 * This is a MessageReceiver that is used at the client side to accept the 
 * Messages (response) that comes in the Client. This one correlated the incomming Message to
 * the related Messages and Call the correct callback. 
 */

public class CallbackReceiver implements MessageReceiver {

    public static String SERVIC_NAME = "ClientService";

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
