/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.axis2.util;

import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.RelatesTo;
import org.apache.axis2.client.async.AsyncResult;
import org.apache.axis2.client.async.Callback;
import org.apache.axis2.client.async.AxisCallback;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.OperationContext;
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

    public void addCallback(String msgID, AxisCallback callback) {
        callbackStore.put(msgID, callback);
    }

    public Object lookupCallback(String msgID) {
        return callbackStore.remove(msgID);
    }

    public void receive(MessageContext msgContext) throws AxisFault {
        RelatesTo relatesTO = msgContext.getOptions().getRelatesTo();
        if (relatesTO == null) {
            throw new AxisFault("Cannot identify correct Callback object. RelatesTo is null");
        }
        String messageID = relatesTO.getValue();

        Object callbackObj = callbackStore.remove(messageID);


        if (callbackObj == null) {
            throw new AxisFault("The Callback for MessageID " + messageID + " was not found");
        }

        if (callbackObj instanceof AxisCallback) {
            AxisCallback axisCallback = (AxisCallback)callbackObj;
            if (msgContext.isFault()) {
                axisCallback.onFault(msgContext);
            } else {
                axisCallback.onMessage(msgContext);
            }
            // Either way we're done.
            axisCallback.onComplete();
            return;
        }

        // THIS NEXT PART WILL EVENTUALLY GO AWAY WHEN Callback DOES

        // OK, this must be an old-style Callback
        Callback callback = (Callback)callbackObj;
        AsyncResult result = new AsyncResult(msgContext);

        // check whether the result is a fault.
        try {
            SOAPEnvelope envelope = result.getResponseEnvelope();

            OperationContext opContext = msgContext.getOperationContext();
            if (opContext != null && !opContext.isComplete()) {
                opContext.addMessageContext(msgContext);
            }

            if (envelope.getBody().hasFault()) {
                AxisFault axisFault =
                        Utils.getInboundFaultFromMessageContext(msgContext);
                callback.onError(axisFault);
            } else {
                callback.onComplete(result);
            }
        } catch (Exception e) {
            callback.onError(e);
        }  finally {
            callback.setComplete(true);
        }
    }

    //to get the pending request
    public HashMap getCallbackStore() {
        return callbackStore;
    }
}
