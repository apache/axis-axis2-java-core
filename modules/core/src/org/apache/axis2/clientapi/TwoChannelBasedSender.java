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
package org.apache.axis2.clientapi;

import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.TransportInDescription;
import org.apache.axis2.engine.AxisEngine;
import org.apache.axis2.engine.AxisFault;
import org.apache.axis2.soap.SOAPEnvelope;
import org.apache.axis2.transport.TransportUtils;


public class TwoChannelBasedSender {
    public static MessageContext send(MessageContext msgctx,TransportInDescription transportIn) throws AxisFault{
       
        AxisEngine engine = new AxisEngine(msgctx.getSystemContext());
       

        engine.send(msgctx);

        MessageContext response =
            new MessageContext(msgctx.getSystemContext(),
                msgctx.getSessionContext(),
                msgctx.getTransportIn(),
                msgctx.getTransportOut());
        response.setProperty(
            MessageContext.TRANSPORT_IN,
            msgctx.getProperty(MessageContext.TRANSPORT_IN));
        response.setServerSide(false);
        response.setOperationContext(msgctx.getOperationContext());
        response.setServiceContext(msgctx.getServiceContext());
        
        //If request is REST we assume the response is REST, so set the variable
        response.setDoingREST(msgctx.isDoingREST());
        
        SOAPEnvelope resenvelope = TransportUtils.createSOAPMessage(response);

        if (resenvelope != null) {
            response.setEnvelope(resenvelope);
            engine = new AxisEngine(msgctx.getSystemContext());
            engine.receive(response);

        } else {
            throw new AxisFault("Blocking invocation always expect a response");
        }
        return response;
    }
}
