package org.apache.axis.clientapi;

import org.apache.axis.addressing.EndpointReference;
import org.apache.axis.context.MessageContext;
import org.apache.axis.description.AxisGlobal;
import org.apache.axis.engine.AxisEngine;
import org.apache.axis.engine.AxisFault;
import org.apache.axis.engine.EngineRegistry;
import org.apache.axis.engine.TransportSenderLocator;
import org.apache.axis.impl.engine.EngineRegistryImpl;
import org.apache.axis.om.OMException;
import org.apache.axis.om.SOAPEnvelope;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * Copyright 2001-2004 The Apache Software Foundation.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *         Dec 16, 2004
 *         12:28:00 PM
 */
public class Call extends AbstractCall {
    private EngineRegistry registry;
    protected Log log = LogFactory.getLog(getClass());
    private EndpointReference targetEPR;
    private boolean blocked;
    private String transport;
    private String action;


    public Call() {
        //TODO look for the Client XML and creatre a Engine registy
        this.registry = new EngineRegistryImpl(new AxisGlobal());
    }

    public void setTo(EndpointReference EPR){
        this.targetEPR = EPR;

    }

    /**
     * todo
     * inoder to have asyn support for tansport , it shoud call this method
     * @param transport
     */
    public void setListenerTransport(String transport, boolean blocked){
        this.blocked = blocked;
        this.transport = transport;
    }
    /**
     * todo
     * @param action
     */
    public void setAction(String action){
        this.action = action;
    }


    /**
     * Fire and forget MEP
     * todo
     * @param envelope
     */
    public void sendAsync(SOAPEnvelope envelope) throws AxisFault {
        try {
            URL url = new URL(targetEPR.getAddress());

            final URLConnection urlConnect;
            urlConnect = url.openConnection();
            final AxisEngine engine = new AxisEngine(registry);
            urlConnect.setDoOutput(true);


            MessageContext msgctx = new MessageContext(registry);
            msgctx.setEnvelope(envelope);

            OutputStream out = urlConnect.getOutputStream();
            msgctx.setProperty(MessageContext.TRANSPORT_DATA, out);
            msgctx.setProperty(MessageContext.TRANSPORT_TYPE, TransportSenderLocator.TRANSPORT_HTTP);
            msgctx.setTo(targetEPR);

            engine.send(msgctx);

        }catch (IOException e){
            throw AxisFault.makeFault(e);
        }
    }
    public void send(SOAPEnvelope envelope) throws AxisFault {
        try{
            URL url =new URL(targetEPR.getAddress());
            final URLConnection urlConnect;
            urlConnect = url.openConnection();
            final AxisEngine engine = new AxisEngine(registry);
            urlConnect.setDoOutput(true);


            MessageContext msgctx = new MessageContext(registry);
            msgctx.setEnvelope(envelope);

            OutputStream out = urlConnect.getOutputStream();
            msgctx.setProperty(MessageContext.TRANSPORT_DATA, out);
            msgctx.setProperty(MessageContext.TRANSPORT_TYPE, TransportSenderLocator.TRANSPORT_HTTP);
            msgctx.setTo(targetEPR);
            engine.send(msgctx);

            //todo dose the 202 response  come throgh the same connection
            //This is purely HTTP specific.
            MessageContext response = createIncomingMessageContext(urlConnect.getInputStream(), engine);
            response.setServerSide(false);
            engine.receive(response);

        }catch (IOException e){
            throw AxisFault.makeFault(e);
        }

    }

    public SOAPEnvelope  sendReceive(SOAPEnvelope envelope) throws AxisFault {
        try{

            AxisEngine engine = new AxisEngine(registry);
            MessageContext msgctx = new MessageContext(registry);
            msgctx.setEnvelope(envelope);

            // TODO : Below par is transport specific, for the time being its HTTP. It should be modified
            // so that correct transport sender should be created here
            URL url =new URL(targetEPR.getAddress());
            URLConnection urlConnect = url.openConnection();
            urlConnect.setDoOutput(true);
            OutputStream out = urlConnect.getOutputStream();
            
            msgctx.setProperty(MessageContext.TRANSPORT_DATA, out);
            msgctx.setProperty(MessageContext.TRANSPORT_TYPE, TransportSenderLocator.TRANSPORT_HTTP);
            msgctx.setTo(targetEPR);
            engine.send(msgctx);

            MessageContext response = createIncomingMessageContext(urlConnect.getInputStream(), engine);
            response.setServerSide(false);
            engine.receive(response);

            SOAPEnvelope resenvelope = response.getEnvelope();

            return resenvelope;
        } catch (OMException e) {
            throw AxisFault.makeFault(e);
        } catch (IOException e) {
            throw AxisFault.makeFault(e);
        }
    }

    /**
     *
     * @param envelope
     * @param callback
     */
    public void sendReceiveAsync(SOAPEnvelope envelope, final Callback callback) throws AxisFault {
        try{
           // URL url =new URL(targetEPR.getAddress());
            AxisEngine engine = new AxisEngine(registry);
            final MessageContext msgctx = new MessageContext(registry);
            msgctx.setEnvelope(envelope);

            msgctx.setProperty(MessageContext.TRANSPORT_TYPE, TransportSenderLocator.TRANSPORT_HTTP);
            msgctx.setTo(targetEPR);
           
           // only the transport blocked , client dose not hang
               if(blocked){
                //TODO This shoudld be taken from a pool of inovkers.
                Invoker invoker = new Invoker(msgctx,engine,callback);
                Thread th = new Thread(invoker);
                th.start();
            } else {
               //TODO
               //start the Listener at the client side
              
            }
        } catch (OMException e) {
            throw AxisFault.makeFault(e);
        } catch (IOException e) {
            throw AxisFault.makeFault(e);
        }
    }


    public Object getTO() {
        return this.targetEPR;
    }
}
