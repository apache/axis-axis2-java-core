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
package org.apache.axis.clientapi;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.axis.Constants;
import org.apache.axis.context.MessageContext;
import org.apache.axis.description.TransportInDescription;
import org.apache.axis.engine.AxisEngine;
import org.apache.axis.engine.AxisFault;
import org.apache.axis.om.impl.llom.builder.StAXBuilder;
import org.apache.axis.soap.SOAPEnvelope;
import org.apache.axis.soap.impl.llom.builder.StAXSOAPModelBuilder;
import org.apache.axis.transport.http.HTTPTransportReceiver;


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

        
        SOAPEnvelope resenvelope = null;
                try {
                    //TODO Fix this we support only the HTTP Sync cases, so we hardcode this
                    if (Constants.TRANSPORT_HTTP.equals(transportIn.getName().getLocalPart())) {
                        HTTPTransportReceiver receiver = new HTTPTransportReceiver();
                        resenvelope =
                            receiver.checkForMessage(response,msgctx.getSystemContext());
                    } else if (Constants.TRANSPORT_TCP.equals(transportIn.getName().getLocalPart())) {
                        InputStream inStream = (InputStream) response.getProperty(MessageContext.TRANSPORT_IN);
                        response.setProperty(MessageContext.TRANSPORT_IN,null);
                        Reader in = new InputStreamReader(inStream);
                        if(in != null){
                            XMLStreamReader xmlreader = XMLInputFactory.newInstance().createXMLStreamReader(in);
                            StAXBuilder builder = new StAXSOAPModelBuilder(xmlreader);
                            resenvelope = (SOAPEnvelope) builder.getDocumentElement();
                        }else{
                            throw new AxisFault("Sync invocation expect a proeprty "+ MessageContext.TRANSPORT_IN + " set ");
                        }
                    }
                } catch (XMLStreamException e) {
                    throw new AxisFault(e);
                } catch (FactoryConfigurationError e) {
                    throw new AxisFault(e);
                }
                



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
