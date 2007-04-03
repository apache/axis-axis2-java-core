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
package org.apache.axis2.transport.mail;

import edu.emory.mathcs.backport.java.util.concurrent.LinkedBlockingQueue;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.TransportInDescription;
import org.apache.axis2.wsdl.WSDLConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
/*
 * 
 */

public class SynchronousMailListener {

    private static Log log = LogFactory.getLog(SynchronousMailListener.class);

    private long timeoutInMilliseconds = -1;

    public SynchronousMailListener(long timeoutInMilliseconds) {
        this.timeoutInMilliseconds = timeoutInMilliseconds;
    }


    public void sendReceive(final MessageContext msgContext, final String msgId) throws AxisFault {
        /**
         * This will be bloked invocation
         */
        SimpleMailListener listener = new SimpleMailListener(new LinkedBlockingQueue()) {
            public void start() throws AxisFault {
                long timeStatus;
                LinkedBlockingQueue queue = getLinkedBlockingQueue();
                while (true) {
                    long startTime = System.currentTimeMillis();
                    try {
                        MessageContext msgCtx = (MessageContext) queue.take();
                        MailBasedOutTransportInfo transportInfo = (MailBasedOutTransportInfo) msgCtx
                                .getProperty(org.apache.axis2.Constants.OUT_TRANSPORT_INFO);
                        if (transportInfo.getInReplyTo() == null) {
                            String error = EMailSender.class.getName() +" Coudn't simulate request/response without In-Reply-To Mail header";
                            log.error(error);
                            throw new AxisFault(error);
                        }
                        if (transportInfo.getInReplyTo().equals(msgId)) {
                            msgContext.getOperationContext()
                                    .getMessageContext(WSDLConstants.MESSAGE_LABEL_IN_VALUE)
                                    .setEnvelope(msgCtx.getEnvelope());
                            log.info(SynchronousMailListener.class.getName() + " found the required message.");
                            break;
                        }

                    } catch (InterruptedException e) {
                        log.warn(e);
                        throw new AxisFault(e);
                    }
                    long endTime = System.currentTimeMillis();
                    timeStatus = endTime - startTime;
                    if (timeoutInMilliseconds != -1 && timeStatus > timeoutInMilliseconds) {
                        /*TODO What should be the best default value for timeoutInMilliseconds ?*/
                        /*log.info(SynchronousMailListener.class.getName() + " timeout");
                        break;*/
                    }

                }


            }
        };

        TransportInDescription transportIn = msgContext.getConfigurationContext()
                .getAxisConfiguration().getTransportIn(org.apache.axis2.Constants.TRANSPORT_MAIL);

        listener.init(msgContext.getConfigurationContext(), transportIn);
        msgContext.getConfigurationContext().getThreadPool().execute(listener);
        listener.start();

    }

    public long getTimeoutInMilliseconds() {
        return timeoutInMilliseconds;
    }

    public void setTimeoutInMilliseconds(long timeoutInMilliseconds) {
        this.timeoutInMilliseconds = timeoutInMilliseconds;
    }
}
