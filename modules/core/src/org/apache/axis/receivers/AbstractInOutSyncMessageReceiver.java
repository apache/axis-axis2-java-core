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

import org.apache.axis.addressing.AddressingConstants;
import org.apache.axis.addressing.MessageInformationHeadersCollection;
import org.apache.axis.addressing.miheaders.RelatesTo;
import org.apache.axis.context.MessageContext;
import org.apache.axis.engine.AxisEngine;
import org.apache.axis.engine.AxisFault;

/**
 * This is the Absract IN-OUT MEP MessageReciver. The
 * protected abstract methods are only for the sake of breaking down the logic
 */
public abstract class AbstractInOutSyncMessageReceiver extends AbstractMessageReceiver {
    public abstract void invokeBusinessLogic(
        MessageContext inMessage,
        MessageContext outMessage)
        throws AxisFault;

    public final void recieve(MessageContext messgeCtx) throws AxisFault {
        MessageContext newmsgCtx =
            new MessageContext(messgeCtx.getSystemContext(),
                messgeCtx.getSessionContext(),
                messgeCtx.getTransportIn(),
                messgeCtx.getTransportOut());

        newmsgCtx.setMessageInformationHeaders(new MessageInformationHeadersCollection());
        MessageInformationHeadersCollection oldMessageInfoHeaders =
        messgeCtx.getMessageInformationHeaders();
        MessageInformationHeadersCollection messageInformationHeaders =
            new MessageInformationHeadersCollection();
        messageInformationHeaders.setTo(oldMessageInfoHeaders.getReplyTo());
        messageInformationHeaders.setFaultTo(oldMessageInfoHeaders.getFaultTo());
        messageInformationHeaders.setFrom(oldMessageInfoHeaders.getTo());
        messageInformationHeaders.setRelatesTo(
            new RelatesTo(
                oldMessageInfoHeaders.getMessageId(),
                AddressingConstants.Submission.WSA_RELATES_TO_RELATIONSHIP_TYPE_DEFAULT_VALUE));
        newmsgCtx.setMessageInformationHeaders(messageInformationHeaders);                
        newmsgCtx.setOperationContext(messgeCtx.getOperationContext());
        newmsgCtx.setServiceContext(messgeCtx.getServiceContext());
        newmsgCtx.setProperty(MessageContext.TRANSPORT_OUT,messgeCtx.getProperty(MessageContext.TRANSPORT_OUT));
        newmsgCtx.setDoingREST(messgeCtx.isDoingREST());
        newmsgCtx.setDoingMTOM(messgeCtx.isDoingMTOM());
        
        invokeBusinessLogic(messgeCtx,newmsgCtx);

        AxisEngine engine =
            new AxisEngine(
                messgeCtx.getOperationContext().getServiceContext().getEngineContext());
        engine.send(newmsgCtx);
    }
}
