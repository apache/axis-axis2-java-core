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
import org.apache.axis.transport.TransportReceiver;
import org.apache.axis.transport.TransportReceiverLocator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.io.Writer;

/**
 * Class Call
 */
public class Call {
    /**
     * Field registry
     */
    private EngineRegistry registry;

    /**
     * Field log
     */
    protected Log log = LogFactory.getLog(getClass());

    /**
     * Field targetEPR
     */
    private EndpointReference targetEPR;

    /**
     * Field useSeparateListener
     */
    private boolean useSeparateListener;

    // only used in SendReciveAync , to get the response

    /**
     * Field Listenertransport
     */
    private String Listenertransport;

    // the type of transport that the request should be send throgh

    /**
     * Field transport
     */
    private String transport;

    /**
     * Field action
     */
    private String action;

    /**
     * Constructor Call
     */
    public Call() {
        // TODO look for the Client XML and create an Engine registy
        this.registry = new EngineRegistryImpl(new AxisGlobal());
        Listenertransport = null;
        transport = Constants.TRANSPORT_HTTP;
    }

    /**
     * Method setTo
     *
     * @param EPR
     */
    public void setTo(EndpointReference EPR) {
        this.targetEPR = EPR;
    }

    /**
     * Method setTransportType
     *
     * @param transport
     * @throws AxisFault
     */
    public void setTransportType(String transport) throws AxisFault {
        if ((Constants.TRANSPORT_HTTP.equals(transport)
                || Constants.TRANSPORT_MAIL.equals(transport)
                || Constants.TRANSPORT_TCP.equals(transport))) {
            this.transport = transport;
        } else {
            throw new AxisFault("Selected transport dose not suppot ( "
                    + transport + " )");
        }
    }

    /**
     * todo
     * inoder to have asyn support for tansport , it shoud call this method
     *
     * @param Listenertransport
     * @param useSeparateListener
     * @throws AxisFault
     */
    public void setListenerTransport(
            String Listenertransport, boolean useSeparateListener)
            throws AxisFault {
        if ((Constants.TRANSPORT_HTTP.equals(transport)
                || Constants.TRANSPORT_MAIL.equals(transport)
                || Constants.TRANSPORT_TCP.equals(transport))) {
            this.Listenertransport = Listenertransport;
            this.useSeparateListener = useSeparateListener;
        } else {
            throw new AxisFault("Selected transport dose not suppot ( "
                    + transport + " )");
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
     * @throws AxisFault
     */
    public void sendAsync(SOAPEnvelope envelope) throws AxisFault {
        Writer out = null;
        try {
            final AxisEngine engine = new AxisEngine();
            MessageContext msgctx = new MessageContext(registry, null, null);
            msgctx.setEnvelope(envelope);
            msgctx.setTo(targetEPR);
            msgctx.setProperty(MessageContext.TRANSPORT_TYPE,
                    Constants.TRANSPORT_HTTP);
            msgctx.setTo(targetEPR);
            if(action != null) {
                msgctx.setProperty(MessageContext.SOAP_ACTION,action);
            }
            engine.send(msgctx);
        } catch (IOException e) {
            throw AxisFault.makeFault(e);
        } finally {
            try {

                // TODO This should be the Receiver.close();
                // Receiver is taken using the
                // TransportReceiver reciver = TransportReceiverLocator.locate(requestMessageContext);
                out.close();
            } catch (IOException e1) {
                throw new AxisFault();
            }
        }
    }

    /**
     * Method send
     *
     * @param envelope
     * @throws AxisFault
     */
    public void send(SOAPEnvelope envelope) throws AxisFault {
        if (Constants.TRANSPORT_MAIL.equals(transport)) {
            throw new AxisFault(
                    "This invocation support only for bi-directional transport");
        } else {
            MessageContext request = null;
            try {
                final AxisEngine engine = new AxisEngine();
                request = new MessageContext(registry, null, null);
                request.setEnvelope(envelope);
                request.setProperty(MessageContext.TRANSPORT_TYPE, transport);
                request.setTo(targetEPR);
                if(action != null) {
                    request.setProperty(MessageContext.SOAP_ACTION,action);
                }
                engine.send(request);

                // todo dose the 202 response  come throgh the same connection
                // This is purely HTTP specific.
                // Handle the HTTP 202 respose

                /*
                * MessageContext response =
                * new MessageContext(
                * registry,
                * request.getProperties(),
                * request.getSessionContext());
                */
                request.setServerSide(false);
                request.setProperty(MessageContext.TRANSPORT_TYPE, transport);
                TransportReceiver receiver =
                        TransportReceiverLocator.locate(request);
                receiver.invoke(request);
                if (request.getProperty(MessageContext.TRANSPORT_SUCCEED)
                        != null) {
                    throw new AxisFault("Sent failed");
                } else if (request.getEnvelope().getBody().hasFault()) {
                    throw new AxisFault(
                            request.getEnvelope().getBody().getFault().getFaultString());
                }
            } catch (IOException e) {
                throw AxisFault.makeFault(e);
            } finally {
                Writer writer = (Writer) request.getProperty(
                        MessageContext.TRANSPORT_WRITER);
                if (writer != null) {
                    try {
                        writer.close();
                    } catch (IOException e) {
                        throw new AxisFault(e.getMessage());
                    }
                }
            }
        }
    }

    /**
     * Method sendReceive
     *
     * @param envelope
     * @return
     * @throws AxisFault
     */
    public SOAPEnvelope sendReceive(SOAPEnvelope envelope) throws AxisFault {
        if (Constants.TRANSPORT_MAIL.equals(transport)) {
            throw new AxisFault(
                    "This invocation support only for bi-directional transport");
        }
        try {
            AxisEngine engine = new AxisEngine();
            MessageContext msgctx = new MessageContext(registry, null, null);
            msgctx.setEnvelope(envelope);
            msgctx.setProperty(MessageContext.TRANSPORT_TYPE, transport);
            msgctx.setTo(targetEPR);
            if(action != null) {
                msgctx.setProperty(MessageContext.SOAP_ACTION,action);
            }
            engine.send(msgctx);
            MessageContext response = new MessageContext(registry,
                    msgctx.getProperties(),
                    msgctx.getSessionContext());
            response.setServerSide(false);
            response.setProperty(MessageContext.TRANSPORT_TYPE, transport);
            TransportReceiver receiver =
                    TransportReceiverLocator.locate(response);
            receiver.invoke(response);
            SOAPEnvelope resenvelope = response.getEnvelope();

            // TODO if the resenvelope is a SOAPFault then throw an exception
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
     * @throws AxisFault
     */
    public void sendReceiveAsync(SOAPEnvelope envelope, final Callback callback)
            throws AxisFault {
        try {
            AxisEngine engine = new AxisEngine();
            final MessageContext msgctx = new MessageContext(registry, null,
                    null);
            msgctx.setEnvelope(envelope);
            msgctx.setProperty(MessageContext.TRANSPORT_TYPE, transport);
            msgctx.setTo(targetEPR);
            if(action != null) {
                msgctx.setProperty(MessageContext.SOAP_ACTION,action);
            }
            if (useSeparateListener) {
                if (Constants.TRANSPORT_MAIL.equals(transport)) {
                    throw new AxisFault(
                            "This invocation support only for bi-directional transport");
                }
                Invoker invoker = new Invoker(msgctx, engine, registry,
                        callback);
                Thread th = new Thread(invoker);
                th.start();
            } else {

                // TODO
                // start the Listener at the client side
                throw new UnsupportedOperationException(
                        "Unblocking transports are not supported yet");
            }
        } catch (OMException e) {
            throw AxisFault.makeFault(e);
        } catch (IOException e) {
            throw AxisFault.makeFault(e);
        }
    }

    /**
     * Method getTO
     *
     * @return
     */
    public Object getTO() {
        return this.targetEPR;
    }
}
