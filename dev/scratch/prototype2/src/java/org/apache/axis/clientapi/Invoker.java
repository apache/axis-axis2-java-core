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

import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

import org.apache.axis.context.MessageContext;
import org.apache.axis.engine.AxisEngine;

public class Invoker extends AbstractCall implements Runnable {

    private AxisEngine engine = null;

    private MessageContext reqMsgContext = null;

    private Callback callback = null;

    public Invoker(MessageContext msgContext, AxisEngine engine, Callback callback) {
        this.engine = engine;
        this.reqMsgContext = msgContext;
        this.callback = callback;

    }

    public void run() {
        final Correlator correlator = Correlator.getInstance();
        final String messageID = Long.toString(System.currentTimeMillis());
        try {
            final URL url = (URL) reqMsgContext.getProperty(MessageContext.REQUEST_URL);
            final URLConnection urlConnect = url.openConnection();
            urlConnect.setDoOutput(true);

            OutputStream out = urlConnect.getOutputStream();
            reqMsgContext.setProperty(MessageContext.TRANSPORT_DATA, out);
            reqMsgContext.setMessageID(messageID);

            engine.send(reqMsgContext);
            correlator.addCorrelationInfo(reqMsgContext.getMessageID(), callback);

            MessageContext resMsgContext = createIncomingMessageContext(
                    urlConnect.getInputStream(), engine);
            resMsgContext.setServerSide(false);
            engine.receive(resMsgContext);

            AsyncResult result = new AsyncResult();
            result.setResult(resMsgContext.getEnvelope());
            resMsgContext.setMessageID(messageID);
            callback = correlator.getCorrelationInfo(resMsgContext.getMessageID());
            callback.onComplete(result);

        } catch (Exception e) {
            callback.reportError(e);
        }

    }

}