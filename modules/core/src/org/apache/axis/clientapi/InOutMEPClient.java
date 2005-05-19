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

import java.io.IOException;

import javax.xml.namespace.QName;

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
import org.apache.axis.transport.http.HTTPTransportReceiver;
import org.apache.wsdl.WSDLConstants;

/**
 * This Class capture handling the In-Out type Method invocations. this provides the 
 * methods to do blocking and non blocking invocation. The basic API is based on the 
 * MessageContext and the more convients API is provided by the Call
 */
public class InOutMEPClient extends MEPClient {
    /**
     * transport that should be used for sending and reciving the message
     */
    protected String senderTransport;
    protected String listenerTransport = Constants.TRANSPORT_HTTP;

    /** 
     * Should the two SOAPMessage are sent over same channel over seperate channels.
     * The value of this variable depends on the transport specified.
     * e.g. If the transports are different this is true by default. 
     *      HTTP transport support both cases
     *      SMTP transport support only two channel case
     */
    protected boolean useSeparateListener = false;
    
    /**
     * The address the message should be send
     */
    protected EndpointReference to;


    //variables use for internal implementations
    
    /**
     * The Listener Manager is tempory hack to make it work till will Generalize the Transport Layer More.
     */
    protected ListenerManager listenerManager;
    
    /**
     * This is used for the Receiving the Async Messages 
     */
    protected CallbackReceiver callbackReceiver;
    
    
    /**
     * This accepts a ServiceContext, and the ServiceContext should have all the parents set in to it right
     * Ideall this should be generated from a WSDL, we do not have it yet. 
     * 
     * Follwoing code works for the time been
     * <code>
     *  EngineContextFactory efac = new EngineContextFactory();
        ConfigurationContext sysContext = efac.buildClientEngineContext(null); 
        // above line "null" may be a file name if you know the client repssitory

        //create new service
        QName assumedServiceName = new QName("Your Service");
        ServiceDescription axisService = new ServiceDescription(assumedServiceName);
        sysContext.getEngineConfig().addService(axisService);
        ServiceContext service = sysContext.createServiceContext(assumedServiceName);
        return service;
     * 
     * </code>
     * 
     * @param serviceContext
     */    

    public InOutMEPClient(ServiceContext serviceContext) {
        super(serviceContext, WSDLConstants.MEP_URI_OUT_IN);
        //service context has the engine context set in to it ! 
        callbackReceiver = new CallbackReceiver();
    }

//    this method is commented out, till we implemented it     
//    public InOutMEPClient(String wsdlfile) {
//        super(null, WSDLConstants.MEP_URI_OUT_IN);
//        throw new UnsupportedOperationException();
//    }

    public MessageContext invokeBlocking(OperationDescription axisop, final MessageContext msgctx)
        throws AxisFault {
        verifyInvocation(axisop);

        msgctx.setTo(to);
        msgctx.setServiceContext(serviceContext);
        ConfigurationContext syscontext = serviceContext.getEngineContext();

        if (senderTransport == null) {
            senderTransport = inferTransport(to);
        }
        final TransportInDescription transportIn =
            syscontext.getEngineConfig().getTransportIn(new QName(senderTransport));
        final TransportOutDescription transportOut =
            syscontext.getEngineConfig().getTransportOut(new QName(listenerTransport));

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
            response.setProperty(
                MessageContext.TRANSPORT_READER,
                msgctx.getProperty(MessageContext.TRANSPORT_READER));
            response.setServerSide(false);
            response.setOperationContext(msgctx.getOperationContext());
            response.setServiceContext(msgctx.getServiceContext());

            //TODO Fix this we support only the HTTP Sync cases, so we hardcode this
            HTTPTransportReceiver receiver = new HTTPTransportReceiver();
            receiver.invoke(response, sysContext);
            SOAPEnvelope resenvelope = response.getEnvelope();
            if (resenvelope.getBody().hasFault()) {
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

            if (senderTransport == null) {
                senderTransport = inferTransport(to);
            }
            if(listenerTransport == null){
                listenerTransport = senderTransport;
            }


            if(msgctx.getTransportIn() == null){
                final TransportInDescription transportIn =
                    syscontext.getEngineConfig().getTransportIn(new QName(senderTransport));
                msgctx.setTransportIn(transportIn);
            }
            if(msgctx.getTransportOut() == null){
                final TransportOutDescription transportOut =
                    syscontext.getEngineConfig().getTransportOut(new QName(listenerTransport));
                msgctx.setTransportOut(transportOut);
            }


            msgctx.setOperationContext(axisop.findOperationContext(msgctx, serviceContext, false));
            msgctx.setServiceContext(serviceContext);

            if (useSeparateListener) {
                String messageID = String.valueOf(System.currentTimeMillis());
                msgctx.setMessageID(messageID);
                axisop.setMessageReciever(callbackReceiver);
                callbackReceiver.addCallback(messageID, callback);
                msgctx.setReplyTo(
                    listenerManager.replyToEPR(
                        serviceContext.getServiceConfig().getName().getLocalPart()
                            + "/"
                            + axisop.getName().getLocalPart(),listenerTransport));

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
                            response.setProperty(
                                MessageContext.TRANSPORT_READER,
                                msgctx.getProperty(MessageContext.TRANSPORT_READER));
                            response.setOperationContext(msgctx.getOperationContext());
                            response.setServiceContext(msgctx.getServiceContext());

                            HTTPTransportReceiver receiver = new HTTPTransportReceiver();
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

    /**
     * Set transport information to the the Call, for find how the each parameter acts see the commant at the instance 
     * variables. The senarios supoorted are as follows.
     * [senderTransport, listenerTransport, useSeparateListener]
     * http, http, true
     * http, http, false
     * http,smtp,true
     * smtp,http,true
     * smtp,smtp,true
     *  
     * @param senderTransport
     * @param listenerTransport
     * @param useSeparateListener
     * @throws AxisFault
     */

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
            ListenerManager.makeSureStarted(listenerTransport,serviceContext.getEngineContext());
        }
    }

}
