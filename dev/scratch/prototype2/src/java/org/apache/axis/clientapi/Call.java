package org.apache.axis.clientapi;

import org.apache.axis.Constants;
import org.apache.axis.addressing.EndpointReference;
import org.apache.axis.context.MessageContext;
import org.apache.axis.description.AxisGlobal;
import org.apache.axis.engine.AxisEngine;
import org.apache.axis.engine.AxisFault;
import org.apache.axis.engine.EngineRegistry;
import org.apache.axis.engine.EngineRegistryImpl;
import org.apache.axis.om.OMException;
import org.apache.axis.om.SOAPEnvelope;
import org.apache.axis.transport.TransportReciver;
import org.apache.axis.transport.TransportReciverLocator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
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
 * <p/>
 * Dec 16, 2004
 * 12:28:00 PM
 */
public class Call {
    private EngineRegistry registry;
    protected Log log = LogFactory.getLog(getClass());
    private EndpointReference targetEPR;
    private boolean useSeparateListener;
    //only used in SendReciveAync , to get the response
    private String Listenertransport;
    //the type of transport that the request should be send throgh
    private String transport ;
    private String action;

    public Call() {
        //TODO look for the Client XML and creatre a Engine registy
        this.registry = new EngineRegistryImpl(new AxisGlobal());
        Listenertransport = null;
        transport= Constants.TRANSPORT_HTTP;
    }

    public void setTo(EndpointReference EPR) {
        this.targetEPR = EPR;

    }

    public void setTransportType(String transport) throws AxisFault {
        if((Constants.TRANSPORT_HTTP.equals(transport) || Constants.TRANSPORT_SMTP.equals(transport)
                || Constants.TRANSPORT_TCP.equals(transport) )) {
            this.transport = transport;
        } else {
            throw new AxisFault("Selected transport dose not suppot ( " + transport  + " )");
        }
    }

    /**
     * todo
     * inoder to have asyn support for tansport , it shoud call this method
     *
     * @param Listenertransport
     */
    public void setListenerTransport(String Listenertransport, boolean useSeparateListener) throws AxisFault {
        if((Constants.TRANSPORT_HTTP.equals(transport) || Constants.TRANSPORT_SMTP.equals(transport)
                || Constants.TRANSPORT_TCP.equals(transport) )) {
            this.Listenertransport = Listenertransport;
            this.useSeparateListener = useSeparateListener;
        }  else {
            throw new AxisFault("Selected transport dose not suppot ( " + transport  + " )");
        }


    }

    /**
     * todo
     *
     * @param action
     */
    public void setAction(String action) {
        this.action = action;
    }

    /**
     * Fire and forget MEP
     * todo
     *
     * @param envelope
     */
    public void sendAsync(SOAPEnvelope envelope) throws AxisFault {
        OutputStream out = null;
        try {
            URL url = new URL(targetEPR.getAddress());

            final URLConnection urlConnect;
            urlConnect = url.openConnection();
            final AxisEngine engine = new AxisEngine(registry);
            urlConnect.setDoOutput(true);

            MessageContext msgctx = new MessageContext(registry, null, null);
            msgctx.setEnvelope(envelope);

            out = urlConnect.getOutputStream();
            msgctx.setProperty(MessageContext.TRANSPORT_WRITER, out);
            msgctx.setProperty(
                    MessageContext.TRANSPORT_TYPE,
                    Constants.TRANSPORT_HTTP);
            msgctx.setTo(targetEPR);

            engine.send(msgctx);

        } catch (IOException e) {
            throw AxisFault.makeFault(e);
        } finally {
            try {
                //TODO This should be the Receiver.close();
                //Receiver is taken using the
                //TransportReciver reciver = TransportReciverLocator.locate(requestMessageContext);
                out.close();
            } catch (IOException e1) {
                throw new AxisFault();
            }
        }
    }

    public void send(SOAPEnvelope envelope) throws AxisFault {
        if (Constants.TRANSPORT_SMTP.equals(transport)){
            throw new AxisFault("This invocation support only for bi-directional transport");
        } else {
            MessageContext request = null;
            try {
                final AxisEngine engine = new AxisEngine(registry);
                request = new MessageContext(registry, null, null);
                request.setEnvelope(envelope);

                request.setProperty(
                        MessageContext.TRANSPORT_TYPE,
                        transport);
                request.setTo(targetEPR);
                engine.send(request);

                //todo dose the 202 response  come throgh the same connection
                //This is purely HTTP specific.
                //Handle the HTTP 202 respose
                /* MessageContext response =
                new MessageContext(
                registry,
                request.getProperties(),
                request.getSessionContext());*/

                request.setServerSide(false);
                request.setProperty(
                        MessageContext.TRANSPORT_TYPE,
                        transport);
                TransportReciver reciver = TransportReciverLocator.locate(request);
                reciver.invoke(request);
                if(request.getProperty(MessageContext.TRANSPORT_SUCCEED)!= null){
                    throw new AxisFault("Sent failed");
                } else if (request.getEnvelope().getBody().hasFault()){
                    throw  new AxisFault(request.getEnvelope().getBody().getFault().getFaultString());
                }
            } catch (IOException e) {
                throw AxisFault.makeFault(e);
            }finally{
                Writer writer = (Writer)request.getProperty(MessageContext.TRANSPORT_WRITER);
                if(writer != null){
                    try {
                        writer.close();
                    } catch (IOException e) {
                        throw  new AxisFault(e.getMessage());
                    }
                }
            }
        }

    }

    public SOAPEnvelope sendReceive(SOAPEnvelope envelope) throws AxisFault {
        if(Constants.TRANSPORT_SMTP.equals(transport)){
            throw new AxisFault("This invocation support only for bi-directional transport");
        }
        try {
            AxisEngine engine = new AxisEngine(registry);
            MessageContext msgctx = new MessageContext(registry, null, null);
            msgctx.setEnvelope(envelope);

            msgctx.setProperty(
                    MessageContext.TRANSPORT_TYPE,
                    transport);
            msgctx.setTo(targetEPR);

            engine.send(msgctx);

            MessageContext response =
                    new MessageContext(
                            registry,
                            msgctx.getProperties(),
                            msgctx.getSessionContext());

            response.setServerSide(false);
            response.setProperty(
                    MessageContext.TRANSPORT_TYPE,
                    transport);
            TransportReciver reciver = TransportReciverLocator.locate(response);
            reciver.invoke(response);
            SOAPEnvelope resenvelope = response.getEnvelope();

            //TODO if the resenvelope is a SOAPFault then throw an exception

            return resenvelope;
        } catch (OMException e) {
            throw AxisFault.makeFault(e);
        } catch (IOException e) {
            throw AxisFault.makeFault(e);
        }
    }

    /**
     * @param envelope
     * @param callback
     */
    public void sendReceiveAsync(
            SOAPEnvelope envelope,
            final Callback callback)
            throws AxisFault {
        try {
            AxisEngine engine = new AxisEngine(registry);
            final MessageContext msgctx =
                    new MessageContext(registry, null, null);
            msgctx.setEnvelope(envelope);

            msgctx.setProperty(MessageContext.TRANSPORT_TYPE,transport);
            msgctx.setTo(targetEPR);

            if (useSeparateListener) {
                if(Constants.TRANSPORT_SMTP.equals(transport)){
                    throw new AxisFault("This invocation support only for bi-directional transport");
                }
                Invoker invoker = new Invoker(msgctx, engine, registry, callback);
                Thread th = new Thread(invoker);
                th.start();
            } else {
                //TODO
                //start the Listener at the client side
                throw new UnsupportedOperationException("Unblocking transports are not supported yet");

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
