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
import org.apache.axis.context.MessageContext;
import org.apache.axis.context.OperationContextFactory;
import org.apache.axis.context.ServiceContext;
import org.apache.axis.context.SystemContext;
import org.apache.axis.description.AxisOperation;
import org.apache.axis.description.AxisTransportIn;
import org.apache.axis.description.AxisTransportOut;
import org.apache.axis.engine.AxisFault;
import org.apache.axis.engine.AxisSystem;
import org.apache.axis.engine.MessageSender;
import org.apache.axis.om.OMException;
import org.apache.axis.om.SOAPEnvelope;
import org.apache.axis.transport.TransportReceiver;
import org.apache.axis.util.Utils;
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
        listenerManager.getEngineContext().registerServiceContext(
            serviceContext.getServiceInstanceID(),
            serviceContext);
    }

    public MessageContext invokeBlocking(AxisOperation axisop, final MessageContext msgctx)
        throws AxisFault {
        msgctx.setTo(to);

        SystemContext sysContext = serviceContext.getEngineContext();
        AxisSystem registry = sysContext.getEngineConfig();

        try {
            MessageSender sender = new MessageSender(sysContext);

            msgctx.setOperationContext(
                OperationContextFactory.createMEPContext(
                    WSDLConstants.MEP_CONSTANT_IN_OUT,
                    false,
                    axisop,
                    null));

            sender.send(msgctx);

            MessageContext response = Utils.copyMessageContext(msgctx);
            response.setServerSide(false);

            TransportReceiver receiver = response.getTransportIn().getReciever();
            receiver.invoke(response, sysContext);
            SOAPEnvelope resenvelope = response.getEnvelope();

            // TODO if the resenvelope is a SOAPFault then throw an exception
            return response;
        } catch (OMException e) {
            throw AxisFault.makeFault(e);
        } catch (IOException e) {
            throw AxisFault.makeFault(e);
        }

    }

    public void invokeNonBlocking(
        AxisOperation axisop,
        final MessageContext msgctx,
        final Callback callback)
        throws AxisFault {
        msgctx.setTo(to);
        try {
            final SystemContext syscontext = serviceContext.getEngineContext();

            MessageSender sender = new MessageSender(syscontext);

            final AxisTransportIn transportIn =
                syscontext.getEngineConfig().getTransportIn(new QName(senderTransport));
            final AxisTransportOut transportOut =
                syscontext.getEngineConfig().getTransportOut(new QName(senderTransport));

            if (useSeparateListener) {
                String messageID = String.valueOf(System.currentTimeMillis());
                msgctx.setMessageID(messageID);
                callbackReceiver.addCallback(messageID, callback);
                msgctx.setReplyTo(
                    listenerManager.replyToEPR(
                        serviceContext.getServiceConfig().getName().getLocalPart()
                            + "/"
                            + axisop.getName().getLocalPart()));
                axisop.findOperationContext(msgctx, serviceContext, false);
            }

            sender.send(msgctx);

            //TODO start the server
            if (!useSeparateListener) {
                Runnable newThread = new Runnable() {
                    public void run() {
                        try {
                            MessageContext response = Utils.copyMessageContext(msgctx);
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
