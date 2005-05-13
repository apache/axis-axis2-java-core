package org.apache.axis.clientapi;

import org.apache.axis.addressing.miheaders.RelatesTo;
import org.apache.axis.context.MessageContext;
import org.apache.axis.engine.AxisFault;
import org.apache.axis.engine.MessageReceiver;
import org.apache.axis.soap.SOAPEnvelope;

import java.util.HashMap;

public class CallbackReceiver implements MessageReceiver {

    public static String SERVIC_NAME = "ClientService";

    private HashMap callbackstore;

    public CallbackReceiver() {
        callbackstore = new HashMap();
    }

    public void addCallback(String MsgID, Callback callback) {
        callbackstore.put(MsgID, callback);
    }

    public void invoke(String MsgID, SOAPEnvelope result) {

    }
    /* (non-Javadoc)
     * @see org.apache.axis.engine.MessageReceiver#recieve(org.apache.axis.context.MessageContext)
     */
    public void recieve(MessageContext messgeCtx) throws AxisFault {
        //TODO find the related message ID and call the callback
        RelatesTo relatesTO = messgeCtx.getMessageInformationHeaders().getRelatesTo();

        String messageID = relatesTO.getValue();
        Callback callback = (Callback) callbackstore.get(messageID);
        AsyncResult result = new AsyncResult();
        result.setResult(messgeCtx.getEnvelope());

        if (callback != null) {
            callback.onComplete(result);
        }else{
            throw new AxisFault("The Callback realtes to MessageID "+ messageID + " is not found");
        }
    }

}
