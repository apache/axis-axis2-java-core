/*
 * Copyright 2004,2005 The Apache Software Foundation.
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
package org.apache.axis.receivers;

import org.apache.axis.context.MessageContext;
import org.apache.axis.description.HandlerMetadata;
import org.apache.axis.engine.AxisFault;
import org.apache.axis.engine.Provider;
import org.apache.axis.engine.MessageReceiver;
import org.apache.axis.engine.MessageSender;
import org.apache.axis.handlers.AbstractHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.namespace.QName;

/**
 * This is takes care of the IN-OUT Async MEP in the server side
 */
public class InOutSyncReceiver extends AbstractHandler implements MessageReceiver {
    /**
     * Field log
     */
    protected Log log = LogFactory.getLog(getClass());

    /**
     * Field NAME
     */
    public static final QName NAME = new QName("http://axis.ws.apache.org",
                    "InOutSyncReceiver");

    /**
     * Constructor InOutSyncReceiver
     */
    public InOutSyncReceiver() {
        init(new HandlerMetadata(NAME));
    }

    /**
     * Method invoke
     *
     * @param msgContext
     * @throws AxisFault
     */
    public void invoke(final MessageContext msgContext) throws AxisFault {
        if (msgContext.isNewThreadRequired()) {
            Runnable runner = new Runnable() {
                public void run() {
                    try {
                        invokeAndsend(msgContext);
                    } catch (AxisFault e) {
                        log.error(
                                "Exception occured in new thread starting response",
                                e);
                    }
                }
            };
            Thread thread = new Thread(runner);
            thread.start();
        } else {
            invokeAndsend(msgContext);
        }
    }

    /**
         * Method invokeAndsend
         *
         * @param msgContext
         * @throws AxisFault
         */
    public void invokeAndsend(MessageContext msgContext) throws AxisFault {
        Provider provider = msgContext.getService().getProvider();
        log.info("start invoke the web service impl");
        MessageContext outMsgContext = provider.invoke(msgContext);

        log.info("Invoked the Web Servivces impl");
        MessageSender sender = new MessageSender();
        sender.send(outMsgContext);
    }
}
