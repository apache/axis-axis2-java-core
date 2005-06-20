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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import javax.xml.namespace.QName;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

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
import org.apache.axis.om.impl.llom.builder.StAXBuilder;
import org.apache.axis.soap.SOAPEnvelope;
import org.apache.axis.soap.impl.llom.builder.StAXSOAPModelBuilder;
import org.apache.axis.transport.TransportListener;
import org.apache.axis.transport.http.HTTPTransportReceiver;
import org.apache.wsdl.WSDLConstants;

/**
 * This Class capture handling the In-Out type Method invocations. this provides the 
 * methods to do blocking and non blocking invocation. The basic API is based on the 
 * MessageContext and the more convients API is provided by the Call
 */
public class InOutMEPClient extends MEPClient {
    protected TransportListener listener;
    /**
     * transport that should be used for sending and reciving the message
     */
    protected TransportOutDescription senderTransport;
    protected TransportInDescription listenerTransport;

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
     * This is used for the Receiving the Async Messages 
     */
    protected CallbackReceiver callbackReceiver;
    /**
     * This accepts a ServiceContext, and the ServiceContext should have all the parents set in to it right
     * Ideall this should be generated from a WSDL, we do not have it yet. 
     * 
     * Follwoing code works for the time been
     * <code>
     *  ConfigurationContextFactory efac = new ConfigurationContextFactory();
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
        if (useSeparateListener) {
            SyncCallBack callback = new SyncCallBack();
            invokeNonBlocking(axisop, msgctx, callback);
            int index = 0;
            while (!callback.isComplete()) {
                if (index < 20) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        throw new AxisFault(e);
                    }
                } else {
                    throw new AxisFault("Time out waiting for the response");
                }
            }
            if (callback.envelope != null) {
                MessageContext resMsgctx =
                    new MessageContext(serviceContext.getEngineContext());
                resMsgctx.setEnvelope(callback.envelope);
                return resMsgctx;
            } else {
                if (callback.error instanceof AxisFault) {
                    throw (AxisFault) callback.error;
                } else {
                    throw new AxisFault(callback.error);
                }
            }
        } else {
            msgctx.setTo(to);
            msgctx.setServiceContext(serviceContext);
            ConfigurationContext syscontext = serviceContext.getEngineContext();

            checkTransport(msgctx);

            ConfigurationContext sysContext = serviceContext.getEngineContext();
            AxisConfiguration registry = sysContext.getAxisConfiguration();

            msgctx.setOperationContext(
                OperationContextFactory.createMEPContext(
                    WSDLConstants.MEP_CONSTANT_IN_OUT,
                    
                    axisop,
                    serviceContext));
            MessageContext response = TwoChannelBasedSender.send(msgctx, listenerTransport);

            SOAPEnvelope resenvelope = response.getEnvelope();
            if (resenvelope.getBody().hasFault()) {
                throw new AxisFault(resenvelope.getBody().getFault().getException());
            }
            return response;
        }
    }

    public void invokeNonBlocking(
        final OperationDescription axisop,
        final MessageContext msgctx,
        final Callback callback)
        throws AxisFault {
        verifyInvocation(axisop);
        msgctx.setTo(to);
        try {
            final ConfigurationContext syscontext = serviceContext.getEngineContext();

            AxisEngine engine = new AxisEngine(syscontext);
            //TODO
            checkTransport(msgctx);

            if (useSeparateListener) {
                String messageID = String.valueOf(System.currentTimeMillis());
                msgctx.setMessageID(messageID);
                axisop.setMessageReciever(callbackReceiver);
                callbackReceiver.addCallback(messageID, callback);
                msgctx.setReplyTo(
                ListenerManager.replyToEPR(
                        serviceContext.getServiceConfig().getName().getLocalPart()
                            + "/"
                            + axisop.getName().getLocalPart(),listenerTransport.getName().getLocalPart()));
                msgctx.setOperationContext(
                    axisop.findOperationContext(msgctx, serviceContext));
                msgctx.setServiceContext(serviceContext);
                engine.send(msgctx);
            } else {
                Thread thread = new Thread(new Runnable() {
                    public void run() {
                        try {
                            msgctx.setOperationContext(
                                OperationContextFactory.createMEPContext(
                                    WSDLConstants.MEP_CONSTANT_IN_OUT,
                                    axisop,
                                    serviceContext));
                            msgctx.setServiceContext(serviceContext);
                            MessageContext response =
                                TwoChannelBasedSender.send(msgctx, listenerTransport);
                            SOAPEnvelope resenvelope = response.getEnvelope();
                            AsyncResult asyncResult = new AsyncResult();
                            asyncResult.setResult(resenvelope);
                            callback.onComplete(asyncResult);
                            callback.setComplete(true);
                        } catch (AxisFault e) {
                            callback.reportError(e);
                        }
                    }
                });
                thread.start();
            }

            //            //TODO start the server
            //            if (!useSeparateListener) {
            //                Runnable newThread = new Runnable() {
            //                    public void run() {
            //                        try {
            //                            MessageContext response =
            //                                new MessageContext(
            //                                    msgctx.getSessionContext(),
            //                                    msgctx.getTransportIn(),
            //                                    msgctx.getTransportOut(),
            //                                    msgctx.getSystemContext());
            //                            response.setServerSide(false);
            //                            response.setProperty(
            //                                MessageContext.TRANSPORT_READER,
            //                                msgctx.getProperty(
            //                                    MessageContext.TRANSPORT_READER));
            //                            response.setOperationContext(
            //                                msgctx.getOperationContext());
            //                            response.setServiceContext(
            //                                msgctx.getServiceContext());
            //
            //                            SOAPEnvelope resenvelope =
            //                                checkReturnChannel(response);
            //                            if (resenvelope != null) {
            //                                response.setEnvelope(resenvelope);
            //                                AxisEngine engine =
            //                                    new AxisEngine(
            //                                        serviceContext.getEngineContext());
            //                                engine.receive(response);
            //
            //                                resenvelope = response.getEnvelope();
            //                                AsyncResult asyncResult = new AsyncResult();
            //                                asyncResult.setResult(resenvelope);
            //                                callback.onComplete(asyncResult);
            //                            } else {
            //                                throw new AxisFault("Blocking invocation always expect a response");
            //                            }
            //                        } catch (AxisFault e) {
            //                            callback.reportError(e);
            //                        }
            //
            //                    }
            //                };
            //                (new Thread(newThread)).start();
            //            }

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
                && (Constants.TRANSPORT_HTTP.equals(senderTransport)
                    || Constants.TRANSPORT_TCP.equals(senderTransport)))) {

            this.useSeparateListener = useSeparateListener;
        } else {
            throw new AxisFault("useSeparateListener = false is only supports by the htpp transport set as the sender and receiver");
        }

        this.senderTransport = serviceContext.getEngineContext().getAxisConfiguration().getTransportOut(new QName(senderTransport));
        this.listenerTransport = serviceContext.getEngineContext().getAxisConfiguration().getTransportIn(new QName(listenerTransport));

        if (useSeparateListener == true) {
            if(!serviceContext.getEngineContext().getAxisConfiguration().isEngaged(new QName(Constants.MODULE_ADDRESSING))){
                throw new AxisFault("to do two Transport Channels the Addressing Modules must be engeged");
            }
            ListenerManager.makeSureStarted(listenerTransport, serviceContext.getEngineContext());
        }
    }

    private SOAPEnvelope checkReturnChannel(MessageContext response) throws AxisFault {
        SOAPEnvelope resenvelope = null;
        try {
            //TODO Fix this we support only the HTTP Sync cases, so we hardcode this
            if (Constants.TRANSPORT_HTTP.equals(listenerTransport)) {
                HTTPTransportReceiver receiver = new HTTPTransportReceiver();
                resenvelope = receiver.checkForMessage(response, serviceContext.getEngineContext());
            } else if (Constants.TRANSPORT_TCP.equals(listenerTransport)) {
                InputStream inStream = (InputStream) response.getProperty(MessageContext.TRANSPORT_IN);
                response.setProperty(MessageContext.TRANSPORT_IN,null);
                Reader in = new InputStreamReader(inStream);

                if (in != null) {
                    XMLStreamReader xmlreader =
                        XMLInputFactory.newInstance().createXMLStreamReader(in);
                    StAXBuilder builder = new StAXSOAPModelBuilder(xmlreader);
                    resenvelope = (SOAPEnvelope) builder.getDocumentElement();
                } else {
                    throw new AxisFault(
                        "Sync invocation expect a proeprty "
                            + MessageContext.TRANSPORT_IN
                            + " set ");
                }
            }
        } catch (XMLStreamException e) {
            throw new AxisFault(e);
        } catch (FactoryConfigurationError e) {
            throw new AxisFault(e);
        }
        return resenvelope;
    }

    private void checkTransport(MessageContext msgctx) throws AxisFault {
        if (senderTransport == null) {
            senderTransport = inferTransport(to);
        }
        if (listenerTransport == null) {
            listenerTransport =  serviceContext.getEngineContext().getAxisConfiguration().getTransportIn(
            senderTransport.getName());
        }

        if (msgctx.getTransportIn() == null) {
                msgctx.setTransportIn(listenerTransport);
        }
        if (msgctx.getTransportOut() == null) {
                msgctx.setTransportOut(senderTransport);
        }

    }

    public class SyncCallBack extends Callback {
        private SOAPEnvelope envelope;
        private Exception error;
        public void onComplete(AsyncResult result) {
            this.envelope = result.getResponseEnvelope();
        }
        public void reportError(Exception e) {
            error = e;
        }
//        public boolean hasResult() {
//            return envelope != null || error != null;
//        }
    }

    public void engageModule(QName moduleName) throws AxisFault {
        serviceContext.getEngineContext().getAxisConfiguration().engageModule(moduleName);
    }
    
    public void close() throws AxisFault{
        senderTransport.getSender().cleanUp();
        ListenerManager.stop(listenerTransport.getName().getLocalPart());
     }

}
