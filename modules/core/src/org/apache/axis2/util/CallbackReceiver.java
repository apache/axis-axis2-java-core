package org.apache.axis2.util;

import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.RelatesTo;
import org.apache.axis2.addressing.AddressingConstants;
import org.apache.axis2.client.async.AsyncResult;
import org.apache.axis2.client.async.Callback;
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
    private HashMap callbackStore;

    public CallbackReceiver() {
        callbackStore = new HashMap();
    }

    public void addCallback(String MsgID, Callback callback) {
        callbackStore.put(MsgID, callback);
    }

    public void receive(MessageContext messageCtx) throws AxisFault {
        RelatesTo relatesTO = messageCtx.getOptions().getRelatesTo();
        String messageID = relatesTO.getValue();
        Callback callback = (Callback) callbackStore.get(messageID);
        AsyncResult result = new AsyncResult(messageCtx);

        if (callback != null) {
            callback.onComplete(result);
            callback.setComplete(true);
        } else {
            throw new AxisFault("The Callback realtes to MessageID " + messageID + " is not found");
        }
    }

    //to get the pending request
    public HashMap getCallbackStore() {
        return callbackStore;
    }
}
