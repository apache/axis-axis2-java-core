/*
* Copyright 2004,2006 The Apache Software Foundation.
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
package org.apache.axis2.description;

import org.apache.axiom.om.util.UUIDGenerator;
import org.apache.axiom.soap.SOAPBody;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFault;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.OperationClient;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.async.AsyncResult;
import org.apache.axis2.client.async.Callback;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ContextFactory;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.OperationContext;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.engine.AxisEngine;
import org.apache.axis2.i18n.Messages;
import org.apache.axis2.transport.TransportUtils;
import org.apache.axis2.util.CallbackReceiver;
import org.apache.axis2.util.Utils;
import org.apache.axis2.wsdl.WSDLConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.namespace.QName;
import java.util.HashMap;

public class OutInAxisOperation extends TwoChannelAxisOperation {
    public OutInAxisOperation() {
        super();
        //setup a temporary name
        QName tmpName = new QName(this.getClass().getName() + "_" + UUIDGenerator.getUUID());
        this.setName(tmpName);
        setMessageExchangePattern(WSDL2Constants.MEP_URI_OUT_IN);
    }

    public OutInAxisOperation(QName name) {
        super(name);
        setMessageExchangePattern(WSDL2Constants.MEP_URI_OUT_IN);
    }

    public void addMessageContext(MessageContext msgContext,
                                  OperationContext opContext) throws AxisFault {
        HashMap mep = opContext.getMessageContexts();
        MessageContext immsgContext = (MessageContext) mep
                .get(MESSAGE_LABEL_IN_VALUE);
        MessageContext outmsgContext = (MessageContext) mep
                .get(MESSAGE_LABEL_OUT_VALUE);

        if ((immsgContext != null) && (outmsgContext != null)) {
            throw new AxisFault(Messages.getMessage("mepcompleted"));
        }

        if (outmsgContext == null) {
            mep.put(MESSAGE_LABEL_OUT_VALUE, msgContext);
        } else {
            mep.put(MESSAGE_LABEL_IN_VALUE, msgContext);
            opContext.setComplete(true);
             opContext.cleanup();
        }
    }

    /**
     * Returns a MEP client for an Out-IN operation. This client can be used to
     * interact with a server which is offering an In-Out operation. To use the
     * client, you must call addMessageContext() with a message context and then
     * call execute() to execute the client.
     *
     * @param sc      The service context for this client to live within. Cannot be
     *                null.
     * @param options Options to use as defaults for this client. If any options are
     *                set specifically on the client then those override options
     *                here.
     */
    public OperationClient createClient(ServiceContext sc, Options options) {
        return new OutInAxisOperationClient(this, sc, options);
    }
}

/**
 * MEP client for moi.
 */
class OutInAxisOperationClient extends OperationClient {

    private static Log log = LogFactory.getLog(OutInAxisOperationClient.class);

    OutInAxisOperationClient(OutInAxisOperation axisOp, ServiceContext sc,
                             Options options) {
        super(axisOp, sc, options);
    }

    /**
     * Adds message context to operation context , so that it will handle the
     * logic correctly if the OperationContext is null then new one will be
     * created , and Operation Context will become null when some one calls reset().
     *
     * @param mc
     * @throws AxisFault
     */
    public void addMessageContext(MessageContext mc) throws AxisFault {
        mc.setServiceContext(sc);
        if (mc.getMessageID() == null) {
            setMessageID(mc);
        }
        axisOp.registerOperationContext(mc, oc);
    }

    /**
     * Returns the message context for a given message label.
     *
     * @param messageLabel :
     *                     label of the message and that can be either "Out" or "In" and
     *                     nothing else
     * @return Returns MessageContext.
     * @throws AxisFault
     */
    public MessageContext getMessageContext(String messageLabel)
            throws AxisFault {
        return oc.getMessageContext(messageLabel);
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }


    /**
     * Executes the MEP. What this does depends on the specific MEP client. The
     * basic idea is to have the MEP client execute and do something with the
     * messages that have been added to it so far. For example, if its an Out-In
     * MEP, then if the Out message has been set, then executing the client asks
     * it to send the message and get the In message, possibly using a different
     * thread.
     *
     * @param block Indicates whether execution should block or return ASAP. What
     *              block means is of course a function of the specific MEP
     *              client. IGNORED BY THIS MEP CLIENT.
     * @throws AxisFault if something goes wrong during the execution of the MEP.
     */
    public void execute(boolean block) throws AxisFault {
        if (log.isDebugEnabled()) {
            log.debug("Entry: OutInAxisOperationClient::execute, " + block);
        }
        if (completed) {
            throw new AxisFault(Messages.getMessage("mepiscomplted"));
        }
        ConfigurationContext cc = sc.getConfigurationContext();

        // copy interesting info from options to message context.
        MessageContext mc = oc
                .getMessageContext(WSDLConstants.MESSAGE_LABEL_OUT_VALUE);
        if (mc == null) {
            throw new AxisFault(Messages.getMessage("outmsgctxnull"));
        }
        prepareMessageContext(cc, mc);

        if (options.getTransportIn() == null && mc.getTransportIn() == null) {
            mc.setTransportIn(ClientUtils.inferInTransport(cc
                    .getAxisConfiguration(), options, mc));
        } else if (mc.getTransportIn() == null) {
            mc.setTransportIn(options.getTransportIn());
        }

        /**
         * If a module has set the USE_ASYNC_OPERATIONS option then we override the behaviour
         * for sync calls, and effectively USE_CUSTOM_LISTENER too. However we leave real
         * async calls alone.
         */
        boolean useAsync = false;
        if (!options.isUseSeparateListener()) {
            Boolean useAsyncOption =
                    (Boolean) mc.getProperty(Constants.Configuration.USE_ASYNC_OPERATIONS);
            if (useAsyncOption != null) {
                useAsync = useAsyncOption.booleanValue();
            }
        }

        if (useAsync || options.isUseSeparateListener()) {
            if (log.isDebugEnabled()) {
                log.debug("useAsync=" + useAsync + ", seperateListener=" +
                        options.isUseSeparateListener());
            }
            /**
             * We are following the async path. If the user hasn't set a callback object then we must
             * block until the whole MEP is complete, as they have no other way to get their reply message.
             */
            CallbackReceiver callbackReceiver = null;
            if (axisOp.getMessageReceiver() != null &&
                    axisOp.getMessageReceiver() instanceof CallbackReceiver) {
                callbackReceiver = (CallbackReceiver) axisOp.getMessageReceiver();
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Creating new callback receiver");
                }
                callbackReceiver = new CallbackReceiver();
                axisOp.setMessageReceiver(callbackReceiver);
            }

            SyncCallBack internalCallback = null;
            if (callback != null) {
                callbackReceiver.addCallback(mc.getMessageID(), callback);
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Creating internal callback");
                }
                internalCallback = new SyncCallBack();
                callbackReceiver.addCallback(mc.getMessageID(), internalCallback);
            }

            /**
             * If USE_CUSTOM_LISTENER is set to 'true' the replyTo value will not be replaced and Axis2 will not
             * start its internal listner. Some other enntity (e.g. a module) should take care of obtaining the 
             * response message.
             */
            Boolean useCustomListener =
                    (Boolean) options.getProperty(Constants.Configuration.USE_CUSTOM_LISTENER);
            if (useAsync) {
                useCustomListener = Boolean.TRUE;
            }
            if (useCustomListener == null || !useCustomListener.booleanValue()) {

                EndpointReference replyToFromTransport =
                        mc.getConfigurationContext().getListenerManager().
                                getEPRforService(sc.getAxisService().getName(),
                                                 axisOp.getName().getLocalPart(), mc
                                        .getTransportIn().getName());

                if (mc.getReplyTo() == null) {
                    mc.setReplyTo(replyToFromTransport);
                } else {
                    mc.getReplyTo().setAddress(replyToFromTransport.getAddress());
                }
            }

            //if we don't do this , this guy will wait till it gets HTTP 202 in the HTTP case
            mc.setProperty(MessageContext.TRANSPORT_NON_BLOCKING, Boolean.TRUE);
            AxisEngine engine = new AxisEngine(cc);
            mc.getConfigurationContext().registerOperationContext(mc.getMessageID(), oc);
            engine.send(mc);

            if (internalCallback != null) {
                long timeout = options.getTimeOutInMilliSeconds();
                long waitTime = timeout;
                long startTime = System.currentTimeMillis();

                synchronized (internalCallback) {
                    while (! internalCallback.isComplete() && waitTime >= 0) {
                        try {
                            internalCallback.wait(timeout);
                        } catch (InterruptedException e) {
                            // We were interrupted for some reason, keep waiting
                            // or throw new AxisFault( "Callback was interrupted by someone?" );
                        }
                        // The wait finished, compute remaining time
                        // - wait can end prematurely, see Object.wait( int timeout )
                        waitTime = timeout - (System.currentTimeMillis() - startTime);
                    }
                }
                // process the result of the invocation
                if (internalCallback.envelope != null) {
                    // The call ended normally, so there is nothing to do
                } else {
                    if (internalCallback.error instanceof AxisFault) {
                        throw (AxisFault) internalCallback.error;
                    } else if (internalCallback.error != null) {
                        throw new AxisFault(internalCallback.error);
                    } else if (! internalCallback.isComplete()) {
                        throw new AxisFault(Messages.getMessage("responseTimeOut"));
                    } else {
                        throw new AxisFault(Messages.getMessage("callBackCompletedWithError"));
                    }
                }
            }
        } else {
            if (block) {
                // Send the SOAP Message and receive a response
                send(mc);
                completed = true;
            } else {
                sc.getConfigurationContext().getThreadPool().execute(
                        new NonBlockingInvocationWorker(callback, mc));
            }
        }
    }


    /**
     * @param msgctx
     * @return Returns MessageContext.
     * @throws AxisFault Sends the message using a two way transport and waits for a response
     */
    protected MessageContext send(MessageContext msgctx) throws AxisFault {

        AxisEngine engine = new AxisEngine(msgctx.getConfigurationContext());

        // create the responseMessageContext

        MessageContext responseMessageContext = ContextFactory.createMessageContext(
                msgctx.getConfigurationContext());

        // This is a hack - Needs to change
        responseMessageContext.setOptions(options);


        responseMessageContext.setServerSide(false);
        responseMessageContext.setMessageID(msgctx.getMessageID());
        addMessageContext(responseMessageContext);
        responseMessageContext.setServiceContext(msgctx.getServiceContext());
        responseMessageContext.setAxisMessage(
                axisOp.getMessage(WSDLConstants.MESSAGE_LABEL_IN_VALUE));

        //sending the message
        engine.send(msgctx);
        responseMessageContext.setDoingREST(msgctx.isDoingREST());

        responseMessageContext.setProperty(MessageContext.TRANSPORT_IN, msgctx
                .getProperty(MessageContext.TRANSPORT_IN));
        responseMessageContext.setTransportIn(msgctx.getTransportIn());
        responseMessageContext.setTransportOut(msgctx.getTransportOut());

        // Options object reused above so soapAction needs to be removed so
        // that soapAction+wsa:Action on response don't conflict
        responseMessageContext.setSoapAction(null);

        if (responseMessageContext.getEnvelope() == null) {
            // If request is REST we assume the responseMessageContext is REST, so
            // set the variable
            /*
             * old code here was using the outbound message context to set the inbound SOAP namespace,
             * as such and passing it to TransportUtils.createSOAPMessage
             * 
             * msgctx.getEnvelope().getNamespace().getNamespaceURI()
             * 
             * However, the SOAP1.2 spec, appendix A indicates that if a SOAP1.2 message is sent to a SOAP1.1
             * endpoint, we will get a SOAP1.1 (fault) message response.  We need another way to set
             * the inbound SOAP version.  Best way to do this is to trust the content type and let
             * createSOAPMessage take care of figuring out what the SOAP namespace is.
             */
            SOAPEnvelope resenvelope = TransportUtils.createSOAPMessage(responseMessageContext);
            if (resenvelope != null) {
                responseMessageContext.setEnvelope(resenvelope);
            } else {
                throw new AxisFault(Messages
                        .getMessage("blockingInvocationExpectsResponse"));
            }
        }
        SOAPEnvelope resenvelope = responseMessageContext.getEnvelope();
        if (resenvelope != null) {
            if (resenvelope.getBody().hasFault()) {
                SOAPFault soapFault = resenvelope.getBody().getFault();
                //we need to call engine.receiveFault
                engine = new AxisEngine(msgctx.getConfigurationContext());
                engine.receiveFault(responseMessageContext);
                if (options.isExceptionToBeThrownOnSOAPFault()) {
                    // does the SOAPFault has a detail element for Excpetion
                    AxisFault af = Utils.getInboundFaultFromMessageContext(responseMessageContext);
                    throw af;
                }
            } else {
                engine = new AxisEngine(msgctx.getConfigurationContext());
                engine.receive(responseMessageContext);
                if (responseMessageContext.getReplyTo() != null) {
                    sc.setTargetEPR(responseMessageContext.getReplyTo());
                }
            }
        }
        return responseMessageContext;
    }

    /**
     * This class is the workhorse for a non-blocking invocation that uses a two
     * way transport.
     */
    private class NonBlockingInvocationWorker implements Runnable {
        private Callback callback;

        private MessageContext msgctx;

        public NonBlockingInvocationWorker(Callback callback,
                                           MessageContext msgctx) {
            this.callback = callback;
            this.msgctx = msgctx;
        }

        public void run() {
            try {
                // send the request and wait for response
                MessageContext response = send(msgctx);
                // call the callback
                if (response != null) {
                    SOAPEnvelope resenvelope = response.getEnvelope();
                    SOAPBody body = resenvelope.getBody();
                    if (body.hasFault()) {
                        // If a fault was found, create an AxisFault with a MessageContext so that
                        // other programming models can deserialize the fault to an alternative form.
                        AxisFault fault = new AxisFault(body.getFault(), response);
                        callback.onError(fault);
                    } else {
                        AsyncResult asyncResult = new AsyncResult(response);
                        callback.onComplete(asyncResult);
                    }
                }

            } catch (Exception e) {
                callback.onError(e);
            } finally {
                callback.setComplete(true);
            }
        }
    }

    /**
     * This class acts as a callback that allows users to wait on the result.
     */
    private class SyncCallBack extends Callback {

        private SOAPEnvelope envelope;

        private Exception error;

        public void onComplete(AsyncResult result) {
            if (log.isDebugEnabled()) {
                log.debug("Entry: OutInAxisOperationClient$SyncCallBack::onComplete");
            }
            // Transport input stream gets closed after calling setComplete
            // method. Have to build the whole envelope including the
            // attachments at this stage. Data might get lost if the input
            // stream gets closed before building the whole envelope.
            this.envelope = result.getResponseEnvelope();
            this.envelope.buildWithAttachments();
            if (log.isDebugEnabled()) {
                log.debug("Exit: OutInAxisOperationClient$SyncCallBack::onComplete");
            }
        }

        public void setComplete(boolean complete) {
            if (log.isDebugEnabled()) {
                log.debug("Entry: OutInAxisOperationClient$SyncCallBack::setComplete, " + complete);
            }
            super.setComplete(complete);
            synchronized (this) {
                notify();
            }
            if (log.isDebugEnabled()) {
                log.debug("Exit: OutInAxisOperationClient$SyncCallBack::setComplete, " + complete);
            }
        }

        public void onError(Exception e) {
            if (log.isDebugEnabled()) {
                log.debug("Entry: OutInAxisOperationClient$SyncCallBack::onError, " + e);
            }
            error = e;
            if (log.isDebugEnabled()) {
                log.debug("Exit: OutInAxisOperationClient$SyncCallBack::onError");
            }
        }
    }

}
