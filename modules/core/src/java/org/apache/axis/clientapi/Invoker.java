package org.apache.axis.clientapi;

/**
 * Copyright 2001-2004 The Apache Software Foundation. <p/>Licensed under the
 * Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at <p/>
 * http://www.apache.org/licenses/LICENSE-2.0 <p/>Unless required by applicable
 * law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */

import org.apache.axis.context.MessageContext;
import org.apache.axis.engine.AxisEngine;
import org.apache.axis.engine.EngineRegistry;
import org.apache.axis.transport.TransportReciver;
import org.apache.axis.transport.TransportReciverLocator;

public class Invoker implements Runnable {

    private AxisEngine engine;
    private EngineRegistry registry;

    private MessageContext reqMsgContext;

    private Callback callback;

    public Invoker(MessageContext msgContext,
                   AxisEngine engine,
                   EngineRegistry reg,
                   Callback callback) {
        this.engine = engine;
        this.reqMsgContext = msgContext;
        this.callback = callback;
        this.registry = reg;

    }

    public void run() {
        final Correlator correlator = Correlator.getInstance();
        final String messageID = Long.toString(System.currentTimeMillis());
        try {

            reqMsgContext.setMessageID(messageID);

            engine.send(reqMsgContext);
            correlator.addCorrelationInfo(reqMsgContext.getMessageID(),
                    callback);

            MessageContext resMsgContext = new MessageContext(registry, reqMsgContext.getProperties(),reqMsgContext.getSessionContext());

            resMsgContext.setServerSide(false);
//            resMsgContext.setProperty(MessageContext.TRANSPORT_TYPE,
//            Constants.TRANSPORT_HTTP);
            TransportReciver reciver =
                    TransportReciverLocator.locate(resMsgContext);
            reciver.invoke(resMsgContext);

            AsyncResult result = new AsyncResult();
            result.setResult(resMsgContext.getEnvelope());
            resMsgContext.setMessageID(messageID);
            callback =
                    correlator.getCorrelationInfo(resMsgContext.getMessageID());
            callback.setComplete(true);
            callback.setResult(result);
            callback.onComplete(result);

        } catch (Exception e) {
            callback.reportError(e);
        }

    }

}