/*
 * Copyright 2001-2004 The Apache Software Foundation.
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

package org.apache.axis.impl.receivers;

import javax.xml.namespace.QName;

import org.apache.axis.context.MessageContext;
import org.apache.axis.description.HandlerMetaData;
import org.apache.axis.engine.AxisFault;
import org.apache.axis.engine.Provider;
import org.apache.axis.engine.Receiver;
import org.apache.axis.engine.Sender;
import org.apache.axis.impl.handlers.AbstractHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class InOutSyncReceiver extends AbstractHandler implements Receiver {
    protected Log log = LogFactory.getLog(getClass());
	public static final QName NAME = new QName("http://axis.ws.apache.org","InOutSyncReceiver");
	

	public InOutSyncReceiver(){
		init(new HandlerMetaData(NAME));
	}
    public void invoke(final MessageContext msgContext) throws AxisFault {
        if (msgContext.isNewThreadRequired()) {
            Runnable runner = new Runnable() {
                public void run() {
                    try {
                        invokeAndsend(msgContext);
                    } catch (AxisFault e) {
                        log.error("Exception occured in new thread starting response", e);
                    }

                }
            };
            Thread thread = new Thread(runner);
            thread.start();
        } else {
            invokeAndsend(msgContext);
        }
    }

    public void invokeAndsend(MessageContext msgContext) throws AxisFault {
        //org.TimeRecorder.BEFORE_INVOKE = System.currentTimeMillis();
		Provider provider = msgContext.getService().getProvider();
		log.info("start invoke the web service impl");
		MessageContext outMsgContext = provider.invoke(msgContext);
        //org.TimeRecorder.AFTER_INVOKE = System.currentTimeMillis();
		log.info("Invoked the Web Servivces impl");
        Sender sender = new Sender();
        sender.send(msgContext);
    }

}
