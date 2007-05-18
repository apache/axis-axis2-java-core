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

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.OperationContext;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisMessage;
import org.apache.axis2.wsdl.WSDLConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class SynchronousMailListener {

    private static Log log = LogFactory.getLog(SynchronousMailListener.class);
    private boolean complete = false;
    //To store out going messageconext
    private MessageContext outMessageContext;
    private MessageContext inMessageContext;

    private long timeoutInMilliseconds = -1;


    public SynchronousMailListener(MessageContext outMessageContext,
                                   long timeoutInMilliseconds) {
        this.outMessageContext = outMessageContext;
        this.timeoutInMilliseconds = timeoutInMilliseconds;
    }

    public void setInMessageContext(MessageContext inMessageContext) throws AxisFault{
        OperationContext operationContext = outMessageContext.getOperationContext();
        MessageContext msgCtx =
                operationContext.getMessageContext(WSDLConstants.MESSAGE_LABEL_IN_VALUE);
        if(msgCtx==null){
            inMessageContext.setOperationContext(operationContext);
            inMessageContext.setServiceContext(outMessageContext.getServiceContext());
            if(!operationContext.isComplete()){
                operationContext.addMessageContext(inMessageContext);
            }
            AxisOperation axisOp = operationContext.getAxisOperation();
            //TODO need to handle fault case as well ,
            //TODO  need to check whether the message contains fault , if so we need to get the fault message
            AxisMessage inMessage = axisOp.getMessage(WSDLConstants.MESSAGE_LABEL_IN_VALUE);
            inMessageContext.setAxisMessage(inMessage);
            inMessageContext.setServerSide(false);
        } else {
            msgCtx.setOperationContext(operationContext);
            msgCtx.setServiceContext(outMessageContext.getServiceContext());
            AxisOperation axisOp = operationContext.getAxisOperation();
            AxisMessage inMessage = axisOp.getMessage(WSDLConstants.MESSAGE_LABEL_IN_VALUE);
            msgCtx.setAxisMessage(inMessage);
            msgCtx.setTransportIn(inMessageContext.getTransportIn());
            msgCtx.setTransportOut(inMessageContext.getTransportOut());
            msgCtx.setServerSide(false);
            msgCtx.setProperty(org.apache.axis2.transport.mail.Constants.CONTENT_TYPE,
                    inMessageContext.getProperty(org.apache.axis2.transport.mail.Constants.CONTENT_TYPE));
            msgCtx.setIncomingTransportName(org.apache.axis2.Constants.TRANSPORT_MAIL);
            msgCtx.setEnvelope(inMessageContext.getEnvelope());
            if(!operationContext.isComplete()){
                operationContext.addMessageContext(msgCtx);
            }
        }
        this.inMessageContext = inMessageContext;
        log.info(" SynchronousMailListener found the required message.");
        complete = true;
    }

    public long getTimeoutInMilliseconds() {
        return timeoutInMilliseconds;
    }

    public void setTimeoutInMilliseconds(long timeoutInMilliseconds) {
        this.timeoutInMilliseconds = timeoutInMilliseconds;
    }

    public boolean isComplete() {
        return complete;
    }

    public MessageContext getInMessageContext() {
        return inMessageContext;
    }
}
