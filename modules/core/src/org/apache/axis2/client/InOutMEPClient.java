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

package org.apache.axis2.client;

import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.OperationContext;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.TransportInDescription;
import org.apache.axis2.description.TransportOutDescription;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.engine.AxisEngine;
import org.apache.axis2.i18n.Messages;
import org.apache.axis2.om.OMException;
import org.apache.axis2.soap.SOAPBody;
import org.apache.axis2.soap.SOAPEnvelope;
import org.apache.axis2.soap.SOAPFault;
import org.apache.axis2.transport.TransportListener;
import org.apache.axis2.util.UUIDGenerator;
import org.apache.wsdl.WSDLConstants;

import javax.xml.namespace.QName;

/**
 * This class captures the handling of In-Out type method invocations for both blocking 
 * and non-blocking calls. The basic API is based on MessageContext and 
 * provides more convenient APIs. 
 */
public class InOutMEPClient extends MEPClient {


    protected long timeOutInMilliSeconds = DEFAULT_TIMEOUT_MILLISECONDS;

    AxisEngine engine = null;

    protected TransportListener listener;
    /**
     * This is used for sending and receiving messages.
     */
    protected TransportOutDescription senderTransport;
    protected TransportInDescription listenerTransport;

    /**
     * Used to specify whether the two SOAP Messages are be sent over same channel 
     * or over separate channels.The value of this variable depends on the transport specified.
     * For e.g., if the transports are different this is true by default.
     * HTTP transport supports both cases while SMTP transport supports only two channel case.
     */
    protected boolean useSeparateListener = false;

    /**
     * The address the message should be send
     */

    //variables use for internal implementations

    /**
     * This is used for the receiving the asynchronous messages.
     */
    protected CallbackReceiver callbackReceiver;

    /**
     * timeout in ms unless stated
     * {@value}
     */
    private static final int DEFAULT_TIMEOUT_MILLISECONDS = 2000;

    /**
     * Constructs a InOutMEPClient from a ServiceContext.
     * Ideally this should be generated from a WSDL, we do not have it yet.
     * <p/>
     * Following code works for the time being. <p/>
     * <blockquote><pre>
     * ConfigurationContextFactory efac = new ConfigurationContextFactory(); 
     * // Replace the null with your client repository if any
     * ConfigurationContext sysContext = efac.buildClientConfigurationContext(null);
     * // above line "null" may be a file name if you know the client repssitory
     * 
     * //create new service
     * QName assumedServiceName = new QName("Your Service");
     * AxisService axisService = new AxisService(assumedServiceName);
     * sysContext.getEngineConfig().addService(axisService);
     * ServiceContext service = sysContext.createServiceContext(assumedServiceName);
     * return service;
     * </pre></blockquote>
     * </code>
     *
     * @param serviceContext
     */

    public InOutMEPClient(ServiceContext serviceContext) {
        super(serviceContext, WSDLConstants.MEP_URI_OUT_IN);
        //service context has the engine context set in to it !
        callbackReceiver = new CallbackReceiver();
    }


    /**
     * This method is used to make blocking calls. This is independent of the transport.
     * For e.g. invocation done with this method might
     * <ol>
     * <li>send request via http and receive the response at the same http connection.</li>
     * <li>send request via http and receive the response at a different http connection.</li>
     * <li>send request via an email smtp and receive the response via an email.</li>
     * </ol>
     */

    public MessageContext invokeBlocking(AxisOperation axisop,
                                         final MessageContext msgctx)
            throws AxisFault {
        prepareInvocation(axisop, msgctx);

        // The message ID is sent all the time
        String messageID = String.valueOf("uuid:" + UUIDGenerator.getUUID());
        msgctx.setMessageID(messageID);
        //
        if (useSeparateListener) {
            //This mean doing a Request-Response invocation using two channel. If the
            //transport is two way transport (e.g. http) Only one channel is used (e.g. in http cases
            //202 OK is sent to say no repsone avalible). Axis2 get blocked return when the response is avalible.

            SyncCallBack callback = new SyncCallBack();
            //this method call two channel non blocking method to do the work and wait on the callbck
            invokeNonBlocking(axisop, msgctx, callback);
            long index = timeOutInMilliSeconds / 100;
            while (!callback.isComplete()) {
                //wait till the reponse arrives
                if (index-- >= 0) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        throw new AxisFault(e);
                    }
                } else {
                    throw new AxisFault(Messages.getMessage("responseTimeOut"));
                }
            }
            //process the resule of the invocation
            if (callback.envelope != null) {
                MessageContext resMsgctx =
                        new MessageContext(serviceContext.getConfigurationContext());
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
            //This is the Usual Request-Response Sync implemetation
            msgctx.setServiceContext(serviceContext);
            ConfigurationContext syscontext = serviceContext.getConfigurationContext();
            msgctx.setConfigurationContext(syscontext);

            checkTransport(msgctx);

            OperationContext operationContext = new OperationContext(axisop, serviceContext);
            axisop.registerOperationContext(msgctx, operationContext);

            //Send the SOAP Message and receive a response                
            MessageContext response =
                    TwoWayTransportBasedSender.send(msgctx, listenerTransport);

            //check for a fault and return the result
            SOAPEnvelope resenvelope = response.getEnvelope();
            if (resenvelope.getBody().hasFault()) {
                SOAPFault soapFault = resenvelope.getBody().getFault();
                Exception ex = soapFault.getException();

                if (isExceptionToBeThrownOnSOAPFault) {
                    //does the SOAPFault has a detail element for Excpetion
                    if (ex != null) {
                        throw new AxisFault(ex);
                    } else {
                        //if detail element not present create a new Exception from the detail
                        String message = "";
                        message = message + "Code =" + soapFault.getCode() == null ? "" :
                                soapFault.getCode().getValue() == null ? "" : soapFault.getCode().getValue().getText();
                        message = message + "Reason =" + soapFault.getReason() == null ? "" :
                                soapFault.getReason().getSOAPText() == null ? "" : soapFault.getReason().getSOAPText().getText();
                        throw new AxisFault(message);
                    }
                }
            }
            return response;
        }
    }

    /**
     * This method is used to make non-blocking calls and is independent of the transport.
     * For e.g. invocation done with this method might
     * <ol>
     * <li>send request via http and receive the response at the same http connection.</li>
     * <li>send request via http and receive the response at a different http connection.</li>
     * <li>send request via an email smtp and receive the response via an email.</li>
     * </ol>
     */
    public void invokeNonBlocking(final AxisOperation axisop,
                                  final MessageContext msgctx,
                                  final Callback callback)
            throws AxisFault {
        prepareInvocation(axisop, msgctx);
        try {
            final ConfigurationContext syscontext =
                    serviceContext.getConfigurationContext();

            engine = new AxisEngine(syscontext);
            checkTransport(msgctx);
            //Use message id all the time!
            String messageID = String.valueOf("uuid:" + UUIDGenerator.getUUID());
            msgctx.setMessageID(messageID);
            ////
            if (useSeparateListener) {
                //the invocation happen via a separate Channel, so we should set up the
                //information need to correlated the response message and invoke the call back

                axisop.setMessageReceiver(callbackReceiver);
                callbackReceiver.addCallback(messageID, callback);

                //set the replyto such that the response will arrive at the transport listener started
                // Note that this will only change the replyTo Address property in the replyTo EPR
                EndpointReference replyToFromTransport = ListenerManager.replyToEPR(serviceContext
                        .getAxisService()
                        .getName()
                        .getLocalPart()
                        + "/"
                        + axisop.getName().getLocalPart(),
                        listenerTransport.getName().getLocalPart());

                if (msgctx.getReplyTo() == null) {
                    msgctx.setReplyTo(replyToFromTransport);
                } else {
                    msgctx.getReplyTo().setAddress(replyToFromTransport.getAddress());
                }

                //create and set the Operation context
                msgctx.setOperationContext(axisop.findOperationContext(msgctx, serviceContext));
                msgctx.setServiceContext(serviceContext);

                //send the message
                engine.send(msgctx);
            } else {
                // here a bloking invocation happens in a new thread, so the
                // progamming model is non blocking
                serviceContext.getConfigurationContext().getThreadPool().execute(new NonBlockingInvocationWorker(callback, axisop, msgctx));
            }

        } catch (OMException e) {
            throw new AxisFault(e.getMessage(), e);
        } catch (Exception e) {
            throw new AxisFault(e.getMessage(), e);
        }

    }

    /**
     * Sets transport information to the call. The senarios supported are as follows: 
     * <blockquote><pre>
     * [senderTransport, listenerTransport, useSeparateListener]
     * http, http, true
     * http, http, false
     * http,smtp,true
     * smtp,http,true
     * smtp,smtp,true
     * </pre></blockquote>
     *
     * @param senderTransport
     * @param listenerTransport
     * @param useSeparateListener
     * @throws AxisFault
     */

    public void setTransportInfo(String senderTransport,
                                 String listenerTransport,
                                 boolean useSeparateListener)
            throws AxisFault {
        //here we check for a legal combination, for and example if the sendertransport is http and listner
        //transport is smtp the invocation must using seperate transport 
        if (!useSeparateListener) {
            boolean isTransportsEqual =
                    senderTransport.equals(listenerTransport);
            boolean isATwoWaytransport =
                    Constants.TRANSPORT_HTTP.equals(senderTransport)
                            || Constants.TRANSPORT_TCP.equals(senderTransport);
            if ((!isTransportsEqual || !isATwoWaytransport)) {
                throw new AxisFault(Messages.getMessage("useSeparateListenerLimited"));
            }
        } else {
            this.useSeparateListener = useSeparateListener;

        }

        //find and set the transport details
        AxisConfiguration axisConfig =
                serviceContext.getConfigurationContext().getAxisConfiguration();
        this.listenerTransport =
                axisConfig.getTransportIn(new QName(listenerTransport));
        this.senderTransport =
                axisConfig.getTransportOut(new QName(senderTransport));
        if (this.senderTransport == null) {
            throw new AxisFault(Messages.getMessage("unknownTransport", senderTransport));
        }
        if (this.listenerTransport == null) {
            throw new AxisFault(Messages.getMessage("unknownTransport", listenerTransport));
        }

        //if separate transport is used, start the required listeners
        if (useSeparateListener) {
            if (!serviceContext
                    .getConfigurationContext()
                    .getAxisConfiguration()
                    .isEngaged(new QName(Constants.MODULE_ADDRESSING))) {
                throw new AxisFault(Messages.getMessage("2channelNeedAddressing"));
            }
            ListenerManager.makeSureStarted(listenerTransport,
                    serviceContext.getConfigurationContext());
        }
    }

    /**
     * Checks if the transports are identified correctly.
     *
     * @param msgctx
     * @throws AxisFault
     */
    private void checkTransport(MessageContext msgctx) throws AxisFault {
        if (senderTransport == null) {
            senderTransport = inferTransport(msgctx.getTo());
        }
        if (listenerTransport == null) {
            listenerTransport =
                    serviceContext
                            .getConfigurationContext()
                            .getAxisConfiguration()
                            .getTransportIn(senderTransport.getName());
        }

        if (msgctx.getTransportIn() == null) {
            msgctx.setTransportIn(listenerTransport);
        }
        if (msgctx.getTransportOut() == null) {
            msgctx.setTransportOut(senderTransport);
        }

    }

    /**
     * This class acts as a callback that allows users to wait on the result.
     */
    public class SyncCallBack extends Callback {
        private SOAPEnvelope envelope;
        private Exception error;

        public void onComplete(AsyncResult result) {
            this.envelope = result.getResponseEnvelope();
        }

        public void reportError(Exception e) {
            error = e;
        }
    }

    /**
     * Closes the call initiated to the Transport Listeners. If there are multiple
     * requests sent, the call should be closed only when all are are done.
     */
    public void close() throws AxisFault {
        ListenerManager.stop(listenerTransport.getName().getLocalPart());
    }

    /**
     * This class is the workhorse for a non-blocking invocation that uses a
     * two way transport.
     */
    private class NonBlockingInvocationWorker implements Runnable {

        private Callback callback;
        private AxisOperation axisop;
        private MessageContext msgctx;

        public NonBlockingInvocationWorker(Callback callback,
                                           AxisOperation axisop,
                                           MessageContext msgctx) {
            this.callback = callback;
            this.axisop = axisop;
            this.msgctx = msgctx;
        }

        public void run() {
            try {
                OperationContext opcontxt = new OperationContext(axisop, serviceContext);
                msgctx.setOperationContext(opcontxt);
                msgctx.setServiceContext(serviceContext);
                //send the request and wait for reponse
                MessageContext response =
                        TwoWayTransportBasedSender.send(msgctx, listenerTransport);
                //call the callback                        
                SOAPEnvelope resenvelope = response.getEnvelope();
                SOAPBody body = resenvelope.getBody();
                if (body.hasFault()) {
                    Exception ex = body.getFault().getException();
                    if (ex != null) {
                        callback.reportError(ex);
                    } else {
                        //todo this needs to be fixed
                        callback.reportError(new Exception(body.getFault().getReason().getText()));
                    }
                } else {
                    AsyncResult asyncResult = new AsyncResult(response);
                    callback.onComplete(asyncResult);
                }

                callback.setComplete(true);
            } catch (Exception e) {
                callback.reportError(e);
            }
        }
    }

    /**
     * This is used in blocking scenario. Client will time out after waiting this amount of time.
     * The default is 2000 and must be provided in multiples of 100.
     *
     * @param timeOutInMilliSeconds
     */
    public void setTimeOutInMilliSeconds(long timeOutInMilliSeconds) {
        this.timeOutInMilliSeconds = timeOutInMilliSeconds;
    }

    /**
     * Gets the wait time after which a client times out in a blocking scenario.
     * The default is 2000.
     *
     * @return timeOutInMilliSeconds
     */
    public long getTimeOutInMilliSeconds() {
        return timeOutInMilliSeconds;
    }
}
