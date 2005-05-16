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
 *
 *  Runtime state of the engine
 */
package org.apache.axis.clientapi;

import org.apache.axis.Constants;
import org.apache.axis.addressing.EndpointReference;
import org.apache.axis.context.ConfigurationContext;
import org.apache.axis.context.MessageContext;
import org.apache.axis.context.OperationContextFactory;
import org.apache.axis.context.ServiceContext;
import org.apache.axis.description.OperationDescription;
import org.apache.axis.description.TransportInDescription;
import org.apache.axis.description.TransportOutDescription;
import org.apache.axis.engine.AxisConfiguration;
import org.apache.axis.engine.AxisEngine;
import org.apache.axis.engine.AxisFault;
import org.apache.axis.om.OMException;
import org.apache.axis.soap.SOAPEnvelope;
import org.apache.axis.transport.TransportReceiver;
import org.apache.axis.transport.http.HTTPTransportReceiver;
import org.apache.wsdl.WSDLConstants;

import javax.xml.namespace.QName;
import java.io.IOException;

/**
 * This Class capture handling the In-Out type Method invocations. this provides the 
 * methods to do blocking and non blocking invocation. The basic API is based on the 
 * MessageContext and the more convients API is provided by the Call
 */
public class InOutMEPClient extends MEPClient {
    /**
     * transport that should be used for sending and reciving the message
     */
    protected String senderTransport = Constants.TRANSPORT_HTTP;
    protected String listenertransport = Constants.TRANSPORT_HTTP;

    /** 
     * Should the two SOAPMessage are sent over same channel over seperate channels.
     * The value of this variable depends on the transport specified.
     * e.g. If the transports are different this is true by default. 
     *      HTTP transport support both cases
     *      SMTP transport support only two channel case
     */
    protected boolean useSeparateListener = false;

    //variables use for internal implementations
    protected ListenerManager listenerManager;
    protected CallbackReceiver callbackReceiver;
    protected EndpointReference to;

    public InOutMEPClient(ServiceContext serviceContext) {
        super(serviceContext);
        //service context has the engine context set in to it ! 
        callbackReceiver = new CallbackReceiver();
        listenerManager = new ListenerManager(serviceContext.getEngineContext());
        listenerManager.getSystemContext().registerServiceContext(
            serviceContext.getServiceInstanceID(),
            serviceContext);
    }

    public MessageContext invokeBlocking(OperationDescription axisop, final MessageContext msgctx)
        throws AxisFault {
        verifyInvocation(axisop);

        msgctx.setTo(to);
        msgctx.setServiceContext(serviceContext);
        ConfigurationContext syscontext = serviceContext.getEngineContext();
        final TransportInDescription transportIn =
            syscontext.getEngineConfig().getTransportIn(new QName(senderTransport));
        final TransportOutDescription transportOut =
            syscontext.getEngineConfig().getTransportOut(new QName(senderTransport));
        msgctx.setTransportIn(transportIn);    
        msgctx.setTransportOut(transportOut);
        
        ConfigurationContext sysContext = serviceContext.getEngineContext();
        AxisConfiguration registry = sysContext.getEngineConfig();

        try {

            AxisEngine engine = new AxisEngine(sysContext);
            msgctx.setOperationContext(
                OperationContextFactory.createMEPContext(
                    WSDLConstants.MEP_CONSTANT_IN_OUT,
                    false,
                    axisop,
                    serviceContext));

            engine.send(msgctx);

            MessageContext response =
                new MessageContext(
                    msgctx.getSessionContext(),
                    msgctx.getTransportIn(),
                    msgctx.getTransportOut(),
                    msgctx.getSystemContext());
            response.setProperty(MessageContext.TRANSPORT_READER,msgctx.getProperty(MessageContext.TRANSPORT_READER)) ;                   
            response.setServerSide(false);
            response.setOperationContext(msgctx.getOperationContext());
            response.setServiceContext(msgctx.getServiceContext());

            //TODO Fix this we support only the HTTP Sync cases, so we hardcode this
            TransportReceiver receiver = new HTTPTransportReceiver();
            receiver.invoke(response, sysContext);
            SOAPEnvelope resenvelope = response.getEnvelope();
            if(resenvelope.getBody().hasFault()){
                throw new AxisFault(resenvelope.getBody().getFault().getException());
            }
            return response;
        } catch (OMException e) {
            throw AxisFault.makeFault(e);
        } catch (IOException e) {
            throw AxisFault.makeFault(e);
        }

    }

    public void invokeNonBlocking(
        OperationDescription axisop,
        final MessageContext msgctx,
        final Callback callback)
        throws AxisFault {
        verifyInvocation(axisop);
        msgctx.setTo(to);
        try {
            final ConfigurationContext syscontext = serviceContext.getEngineContext();

            AxisEngine engine = new AxisEngine(syscontext);

            final TransportInDescription transportIn =
                syscontext.getEngineConfig().getTransportIn(new QName(senderTransport));
            final TransportOutDescription transportOut =
                syscontext.getEngineConfig().getTransportOut(new QName(senderTransport));

            if (useSeparateListener) {
                String messageID = String.valueOf(System.currentTimeMillis());
                msgctx.setMessageID(messageID);
                axisop.setMessageReciever(callbackReceiver);
                callbackReceiver.addCallback(messageID, callback);
                msgctx.setReplyTo(
                    listenerManager.replyToEPR(
                        serviceContext.getServiceConfig().getName().getLocalPart()
                            + "/"
                            + axisop.getName().getLocalPart()));
                axisop.findOperationContext(msgctx, serviceContext, false);
            }

            engine.send(msgctx);

            //TODO start the server
            if (!useSeparateListener) {
                Runnable newThread = new Runnable() {
                    public void run() {
                        try {
                            MessageContext response =
                                new MessageContext(
                                    msgctx.getSessionContext(),
                                    msgctx.getTransportIn(),
                                    msgctx.getTransportOut(),
                                    msgctx.getSystemContext());
                            response.setServerSide(false);

                            TransportReceiver receiver = response.getTransportIn().getReciever();
                            receiver.invoke(response, syscontext);
                            SOAPEnvelope resenvelope = response.getEnvelope();
                            AsyncResult asyncResult = new AsyncResult();
                            asyncResult.setResult(resenvelope);
                            callback.onComplete(asyncResult);
                        } catch (AxisFault e) {
                            callback.reportError(e);
                        }

                    }
                };
                (new Thread(newThread)).start();
            }

        } catch (OMException e) {
            throw AxisFault.makeFault(e);
        } catch (IOException e) {
            throw AxisFault.makeFault(e);
        }

    }

    /**
      * @param to
      */
    public void setTo(EndpointReference to) {
        this.to = to;
    }

    public void setTransportInfo(
        String senderTransport,
        String listenerTransport,
        boolean useSeparateListener)
        throws AxisFault {

        if (useSeparateListener
            || (senderTransport.equals(listenerTransport)
                && Constants.TRANSPORT_HTTP.equals(senderTransport))) {

            this.useSeparateListener = useSeparateListener;
        } else {
            throw new AxisFault("useSeparateListener = false is only supports by the htpp transport set as the sender and receiver");
        }

        if (useSeparateListener == true) {
            listenerManager.makeSureStarted();
        }
    }

}
