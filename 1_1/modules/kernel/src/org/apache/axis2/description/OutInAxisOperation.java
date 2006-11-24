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

import org.apache.axiom.om.OMElement;
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
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.OperationContext;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.engine.AxisEngine;
import org.apache.axis2.i18n.Messages;
import org.apache.axis2.transport.TransportUtils;
import org.apache.axis2.util.CallbackReceiver;
import org.apache.axis2.util.UUIDGenerator;
import org.apache.axis2.util.TargetResolver;
import org.apache.axis2.wsdl.WSDLConstants;

import javax.xml.namespace.QName;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class OutInAxisOperation extends InOutAxisOperation {
    public OutInAxisOperation() {
        super();
        setMessageExchangePattern(WSDL20_2004Constants.MEP_URI_OUT_IN);
    }

    public OutInAxisOperation(QName name) {
        super(name);
        setMessageExchangePattern(WSDL20_2004Constants.MEP_URI_OUT_IN);
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
class OutInAxisOperationClient implements OperationClient {

    private AxisOperation axisOp;

    protected ServiceContext sc;

    protected Options options;

    protected OperationContext oc;

    protected Callback callback;

    /*
     * indicates whether the MEP execution has completed (and hence ready for
     * resetting)
     */
    boolean completed;

    OutInAxisOperationClient(OutInAxisOperation axisOp, ServiceContext sc,
                             Options options) {
        this.axisOp = axisOp;
        this.sc = sc;
        this.options = options;
        this.completed = false;
        this.oc = new OperationContext(axisOp);
        this.oc.setParent(this.sc);
    }

    /**
     * Sets the options that should be used for this particular client. This
     * resets the entire set of options to use the new options - so you'd lose
     * any option cascading that may have been set up.
     *
     * @param options the options
     */
    public void setOptions(Options options) {
        this.options = options;
    }

    /**
     * Returns the options used by this client. If you want to set a single
     * option, then the right way is to call getOptions() and set specific
     * options.
     *
     * @return Returns the options, which will never be null.
     */
    public Options getOptions() {
        return options;
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
     * Create a message ID for the given message context if needed. If user gives an option with
     * MessageID then just copy that into MessageContext , and with that there can be multiple
     * message with same MessageID unless user call setOption for each invocation.
     * <p/>
     * If user want to give message ID then the better way is to set the message ID in the option and
     * call setOption for each invocation then the right thing will happen.
     * <p/>
     * If user does not give a message ID then the new one will be created and set that into Message
     * Context.
     *
     * @param mc the message context whose id is to be set
     */
    private void setMessageID(MessageContext mc) {
        // now its the time to put the parameters set by the user in to the
        // correct places and to the
        // if there is no message id still, set a new one.
        String messageId = options.getMessageId();
        if (messageId == null || "".equals(messageId)) {
            messageId = UUIDGenerator.getUUID();
        }
        mc.setMessageID(messageId);
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
        //setting AxisMessage
        mc.setAxisMessage(axisOp.getMessage(WSDLConstants.MESSAGE_LABEL_OUT_VALUE));
        if (mc.getSoapAction() == null || "".equals(mc.getSoapAction())) {
            mc.setSoapAction(options.getAction());
        }
        mc.setOptions(options);

        // do Target Resolution
        TargetResolver targetResolver = cc.getAxisConfiguration().getTargetResolverChain();
        if(targetResolver != null){
            targetResolver.resolveTarget(mc);
        }

        // if the transport to use for sending is not specified, try to find it
        // from the URL
        TransportOutDescription transportOut = options.getTransportOut();
        if (transportOut == null) {
            EndpointReference toEPR = (options.getTo() != null) ? options
                    .getTo() : mc.getTo();
            transportOut = ClientUtils.inferOutTransport(cc
                    .getAxisConfiguration(), toEPR, mc);
        }
        mc.setTransportOut(transportOut);


        if (options.getTransportIn() == null && mc.getTransportIn() == null) {
            mc.setTransportIn(ClientUtils.inferInTransport(cc
                    .getAxisConfiguration(), options, mc));
        } else if (mc.getTransportIn() == null) {
            mc.setTransportIn(options.getTransportIn());
        }

        addReferenceParameters(mc);
        if (options.isUseSeparateListener()) {
            CallbackReceiver callbackReceiver = (CallbackReceiver) axisOp
                    .getMessageReceiver();
            callbackReceiver.addCallback(mc.getMessageID(), callback);
            
            /**
             * If USE_CUSTOM_LISTENER is set to 'true' the replyTo value will not be replaced and Axis2 will not
             * start its internal listner. Some other enntity (e.g. a module) should take care of obtaining the 
             * response message.
             */
            Boolean useCustomListener = (Boolean) options.getProperty(Constants.Configuration.USE_CUSTOM_LISTENER);
            if (useCustomListener==null || !useCustomListener.booleanValue()) {

                EndpointReference replyToFromTransport = mc.getConfigurationContext().getListenerManager().
                getEPRforService(sc.getAxisService().getName(), axisOp.getName().getLocalPart(), mc
                        .getTransportIn().getName()
                        .getLocalPart());
                
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
        } else {
            if (block) {
                // Send the SOAP Message and receive a response
                MessageContext response = send(mc);
                // check for a fault and return the result
                if (response != null) {
                    SOAPEnvelope resEnvelope = response.getEnvelope();
                    if (resEnvelope.getBody().hasFault()) {
                        SOAPFault soapFault = resEnvelope.getBody().getFault();

                        //we need to call engine.receiveFault
                        AxisEngine engine = new AxisEngine(mc.getConfigurationContext());
                        engine.receiveFault(response);
                        if (options.isExceptionToBeThrownOnSOAPFault()) {
                            // does the SOAPFault has a detail element for Excpetion

                            throw new AxisFault(soapFault.getCode(), soapFault.getReason(),
                                    soapFault.getNode(), soapFault.getRole(), soapFault.getDetail());

                        }
                    }
                }
                completed = true;
            } else {
                sc.getConfigurationContext().getThreadPool().execute(
                        new NonBlockingInvocationWorker(callback, mc));
            }
        }
    }

    private void addReferenceParameters(MessageContext msgctx) {
        EndpointReference to = msgctx.getTo();
        if (options.isManageSession()) {
            EndpointReference tepr = sc.getTargetEPR();
            if (tepr != null) {
                Map map = tepr.getAllReferenceParameters();
                if (map != null) {
                    Iterator valuse = map.values().iterator();
                    while (valuse.hasNext()) {
                        Object refparaelement = valuse.next();
                        if (refparaelement instanceof OMElement) {
                            to.addReferenceParameter((OMElement) refparaelement);
                        }
                    }
                }
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
        MessageContext responseMessageContext = new MessageContext();

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
        responseMessageContext.setSoapAction("");

        if (responseMessageContext.getEnvelope() == null) {
            // If request is REST we assume the responseMessageContext is REST, so
            // set the variable

            SOAPEnvelope resenvelope = TransportUtils.createSOAPMessage(
                    responseMessageContext, msgctx.getEnvelope().getNamespace()
                    .getNamespaceURI());
            if (resenvelope != null) {
                responseMessageContext.setEnvelope(resenvelope);
                engine = new AxisEngine(msgctx.getConfigurationContext());
                engine.receive(responseMessageContext);
                if (responseMessageContext.getReplyTo() != null) {
                    sc.setTargetEPR(responseMessageContext.getReplyTo());
                }
            } else {
                throw new AxisFault(Messages
                        .getMessage("blockingInvocationExpectsResponse"));
            }
        }
        return responseMessageContext;
    }

    /**
     * Resets the MEP client to a clean status after the MEP has completed. This
     * is how you can reuse a MEP client. NOTE: this does not reset the options;
     * only the internal state so the client can be used again.
     *
     * @throws AxisFault if reset is called before the MEP client has completed an
     *                   interaction.
     */
    public void reset() throws AxisFault {
        if (!completed) {
            throw new AxisFault(Messages.getMessage("cannotreset"));
        }
        oc = null;
        completed = false;
    }

    public void complete(MessageContext msgCtxt) throws AxisFault {
        TransportOutDescription trsout = msgCtxt.getTransportOut();
        if (trsout != null) {
            trsout.getSender().cleanup(msgCtxt);
        }
    }

    public OperationContext getOperationContext() {
        return oc;
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
                        Exception ex = body.getFault().getException();

                        if (ex != null) {
                            callback.onError(ex);
                        } else {
                            callback.onError(new Exception(body.getFault()
                                    .getReason().getText()));
                        }
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
}
