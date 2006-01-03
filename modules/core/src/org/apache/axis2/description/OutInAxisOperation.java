package org.apache.axis2.description;

import org.apache.axis2.AxisFault;
import org.apache.axis2.i18n.Messages;
import org.apache.axis2.transport.TransportUtils;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.ListenerManager;
import org.apache.axis2.client.OperationClient;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.async.AsyncResult;
import org.apache.axis2.client.async.Callback;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.OperationContext;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.engine.AxisEngine;
import org.apache.axis2.engine.MessageReceiver;
import org.apache.axis2.soap.SOAPBody;
import org.apache.axis2.soap.SOAPEnvelope;
import org.apache.axis2.soap.SOAPFault;
import org.apache.axis2.util.CallbackReceiver;
import org.apache.wsdl.WSDLConstants;

import javax.xml.namespace.QName;
import java.util.HashMap;

/**
 * Author: Deepal Jayasinghe Date: Oct 3, 2005 Time: 6:01:33 PM
 */
public class OutInAxisOperation extends InOutAxisOperation {
    public OutInAxisOperation() {
        super();
    }

    public OutInAxisOperation(QName name) {
        super(name);
    }

    public void addMessageContext(MessageContext msgContext,
            OperationContext opContext) throws AxisFault {
        HashMap mep = opContext.getMessageContexts();
        MessageContext immsgContext = (MessageContext) mep
                .get(MESSAGE_LABEL_IN_VALUE);
        MessageContext outmsgContext = (MessageContext) mep
                .get(MESSAGE_LABEL_OUT_VALUE);

        if ((immsgContext != null) && (outmsgContext != null)) {
            throw new AxisFault(
                    "Invalid message addition , operation context completed");
        }

        if (outmsgContext == null) {
            mep.put(MESSAGE_LABEL_OUT_VALUE, msgContext);
        } else {
            mep.put(MESSAGE_LABEL_IN_VALUE, msgContext);
            opContext.setComplete(true);
        }
    }

    /**
     * Return a MEP client for an Out-IN operation. This client can be used to
     * interact with a server which is offering an In-Out operation. To use the
     * client, you must call addMessageContext() with a message context and then
     * call execute() to execute the client.
     * 
     * @param sc
     *            The service context for this client to live within. Cannot be
     *            null.
     * @param options
     *            Options to use as defaults for this client. If any options are
     *            set specifically on the client then those override options
     *            here.
     */
    public OperationClient createClient(ServiceContext sc, Options options) {
        return new OutInAxisOperationClient(this, sc, options);
    }
}

/**
 * MEP client for moi.
 */
class OutInAxisOperationClient implements OperationClient {

    private OutInAxisOperation axisOp;

    private ServiceContext sc;

    private Options options;

    private OperationContext oc;

    private Callback callback;

    /*
     * indicates whether the MEP execution has completed (and hence ready for
     * resetting)
     */
    boolean completed;

    OutInAxisOperationClient(OutInAxisOperation axisOp, ServiceContext sc,
            Options options) {
        this.axisOp = axisOp;
        this.sc = sc;
        this.options = new Options(options);
        this.completed = false;
        this.oc = new OperationContext(axisOp);
        this.oc.setParent(this.sc);
    }

    /**
     * Sets the options that should be used for this particular client. This
     * resets the entire set of options to use the new options - so you'd lose
     * any option cascading that may have been set up.
     * 
     * @param options
     *            the options
     */
    public void setOptions(Options options) {
        this.options = options;
    }

    /**
     * Return the options used by this client. If you want to set a single
     * option, then the right way is to do getOptions() and set specific
     * options.
     * 
     * @return the options, which will never be null.
     */
    public Options getOptions() {
        return options;
    }

    /**
     * Adding message context to operation context , so that it will handle the
     * logic correctly if the OperationContext is null then new one will be
     * created , and oc will become null when some one call reset()
     * 
     * @param mc
     * @throws AxisFault
     */
    public void addMessageContext(MessageContext mc) throws AxisFault {
        axisOp.registerOperationContext(mc, oc);
        mc.setServiceContext(sc);
    }

    /**
     * Retun the message context for a given message lebel
     * 
     * @param messageLabel :
     *            label of the message and that can be either "Out" or "In" and
     *            nothing else
     * @return
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
     * Execute the MEP. What this does depends on the specific MEP client. The
     * basic idea is to have the MEP client execute and do something with the
     * messages that have been added to it so far. For example, if its an Out-In
     * MEP, then if the Out message has been set, then executing the client asks
     * it to send the message and get the In message, possibly using a different
     * thread.
     * 
     * @param block
     *            Indicates whether execution should block or return ASAP. What
     *            block means is of course a function of the specific MEP
     *            client. IGNORED BY THIS MEP CLIENT.
     * @throws AxisFault
     *             if something goes wrong during the execution of the MEP.
     */
    public void execute(boolean block) throws AxisFault {
        if (completed) {
            throw new AxisFault(
                    "MEP is already completed- need to reset() before re-executing.");
        }
        ConfigurationContext cc = sc.getConfigurationContext();

        // copy interesting info from options to message context.
        MessageContext mc = oc
                .getMessageContext(WSDLConstants.MESSAGE_LABEL_OUT_VALUE);
        if (mc == null) {
            throw new AxisFault(
                    "Out message context is null ,"
                            + " please set the out message context before calling this method");
        }
        mc.setOperationContext(oc);
        mc.setAxisOperation(axisOp);
        mc.setServiceContext(sc);
        mc.setOptions(options);

        // if the transport to use for sending is not specified, try to find it
        // from the URL
        TransportOutDescription senderTransport = options.getSenderTransport();
        if (senderTransport == null) {
            EndpointReference toEPR = (options.getTo() != null) ? options
                    .getTo() : mc.getTo();
            senderTransport = ClientUtils.inferOutTransport(cc
                    .getAxisConfiguration(), toEPR);
        }
        mc.setTransportOut(senderTransport);
        if (mc.getTransportIn() == null) {
            TransportInDescription transportInDescription = options
                    .getTransportInDescription();
            if (transportInDescription == null) {
                mc.setTransportIn(ClientUtils.inferInTransport(cc
                        .getAxisConfiguration(), options, mc
                        .getServiceContext()));
            } else {
                mc.setTransportIn(transportInDescription);
            }
        }

        if (options.isUseSeparateListener())

        {
            CallbackReceiver callbackReceiver = (CallbackReceiver) axisOp
                    .getMessageReceiver();
            callbackReceiver.addCallback(mc.getMessageID(), callback);
            EndpointReference replyToFromTransport = ListenerManager
                    .replyToEPR(cc, sc.getAxisService().getName() + "/"
                            + axisOp.getName().getLocalPart(), options
                            .getTransportInDescription().getName()
                            .getLocalPart());

            if (mc.getReplyTo() == null) {
                mc.setReplyTo(replyToFromTransport);
            } else {
                mc.getReplyTo().setAddress(replyToFromTransport.getAddress());
            }
            AxisEngine engine = new AxisEngine(cc);
            engine.send(mc);
        }

        else

        {
            if (!block) {
                // Send the SOAP Message and receive a response
                MessageContext response = send(mc, options
                        .getTransportInDescription());
                // check for a fault and return the result
                SOAPEnvelope resenvelope = response.getEnvelope();
                if (resenvelope.getBody().hasFault()) {
                    SOAPFault soapFault = resenvelope.getBody().getFault();
                    Exception ex = soapFault.getException();

                    if (options.isExceptionToBeThrownOnSOAPFault()) {

                        // does the SOAPFault has a detail element for Excpetion
                        if (ex != null) {
                            throw new AxisFault(ex);
                        } else {

                            // if detail element not present create a new
                            // Exception from the detail
                            String message = "";

                            message = (message + "Code =" + soapFault.getCode() == null) ? ""
                                    : (soapFault.getCode().getValue() == null) ? ""
                                            : soapFault.getCode().getValue()
                                                    .getText();
                            message = (message + "Reason ="
                                    + soapFault.getReason() == null) ? ""
                                    : (soapFault.getReason().getSOAPText() == null) ? ""
                                            : soapFault.getReason()
                                                    .getSOAPText().getText();

                            throw new AxisFault(message);
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

    /**
     * Sends the message using a two way transport and waits for a response
     * 
     * @param msgctx
     * @param transportIn
     * @return
     * @throws AxisFault
     */
    public MessageContext send(MessageContext msgctx,
            TransportInDescription transportIn) throws AxisFault {
        AxisEngine engine = new AxisEngine(msgctx.getConfigurationContext());

        engine.send(msgctx);

        // create the responseMessageContext
        MessageContext responseMessageContext = new MessageContext(msgctx
                .getConfigurationContext(), msgctx.getSessionContext(), msgctx
                .getTransportIn(), msgctx.getTransportOut());

        responseMessageContext.setProperty(MessageContext.TRANSPORT_IN, msgctx
                .getProperty(MessageContext.TRANSPORT_IN));
        addMessageContext(responseMessageContext);
        responseMessageContext.setServerSide(false);
        responseMessageContext.setServiceContext(msgctx.getServiceContext());
        responseMessageContext.setServiceGroupContext(msgctx
                .getServiceGroupContext());

        // If request is REST we assume the responseMessageContext is REST, so
        // set the variable
        responseMessageContext.setDoingREST(msgctx.isDoingREST());

        SOAPEnvelope resenvelope = TransportUtils.createSOAPMessage(
                responseMessageContext, msgctx.getEnvelope().getNamespace()
                        .getName());

        if (resenvelope != null) {
            responseMessageContext.setEnvelope(resenvelope);
            engine = new AxisEngine(msgctx.getConfigurationContext());
            engine.receive(responseMessageContext);
        } else {
            throw new AxisFault(Messages
                    .getMessage("blockingInvocationExpectsResponse"));
        }

        return responseMessageContext;
    }

    /**
     * Reset the MEP client to a clean status after the MEP has completed. This
     * is how you can reuse a MEP client. NOTE: this does not reset the options;
     * only the internal state so the client can be used again.
     * 
     * @throws AxisFault
     *             if reset is called before the MEP client has completed an
     *             interaction.
     */
    public void reset() throws AxisFault {
        if (!completed) {
            throw new AxisFault("MEP is not yet complete: cannot reset");
        }
        oc = null;
        completed = false;
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

                // send the request and wait for reponse
                MessageContext response = send(msgctx, options
                        .getTransportInDescription());

                // call the callback
                SOAPEnvelope resenvelope = response.getEnvelope();
                SOAPBody body = resenvelope.getBody();

                if (body.hasFault()) {
                    Exception ex = body.getFault().getException();

                    if (ex != null) {
                        callback.onError(ex);
                    } else {

                        // todo this needs to be fixed
                        callback.onError(new Exception(body.getFault()
                                .getReason().getText()));
                    }
                } else {
                    AsyncResult asyncResult = new AsyncResult(response);

                    callback.onComplete(asyncResult);
                }

                callback.setComplete(true);
            } catch (Exception e) {
                callback.onError(e);
            }
        }
    }
}
