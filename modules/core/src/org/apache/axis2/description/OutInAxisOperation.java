package org.apache.axis2.description;

import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.AddressingConstants;
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
import org.apache.axis2.i18n.Messages;
import org.apache.axis2.om.OMElement;
import org.apache.axis2.soap.SOAPBody;
import org.apache.axis2.soap.SOAPEnvelope;
import org.apache.axis2.soap.SOAPFault;
import org.apache.axis2.soap.SOAPHeader;
import org.apache.axis2.transport.TransportUtils;
import org.apache.axis2.util.CallbackReceiver;
import org.apache.axis2.util.UUIDGenerator;
import org.apache.wsdl.WSDLConstants;

import javax.xml.namespace.QName;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

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
     * MessageID then just copy that into MessageContext , and with that there can be mutiple
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

        mc.setOptions(options);
        // if the transport to use for sending is not specified, try to find it
        // from the URL
        TransportOutDescription transportOut = options.getTranportOut();
        if (transportOut == null) {
            EndpointReference toEPR = (options.getTo() != null) ? options
                    .getTo() : mc.getTo();
            transportOut = ClientUtils.inferOutTransport(cc
                    .getAxisConfiguration(), toEPR);
        }
        mc.setTransportOut(transportOut);
        if (mc.getTransportIn() == null) {
            TransportInDescription transportIn = options.getTransportIn();
            if (transportIn == null) {
                mc.setTransportIn(ClientUtils.inferInTransport(cc
                        .getAxisConfiguration(), options, mc
                        .getServiceContext()));
            } else {
                mc.setTransportIn(transportIn);
            }
        }

        if (mc.getSoapAction() == null || "".equals(mc.getSoapAction())) {
            Parameter soapaction = axisOp.getParameter(AxisOperation.SOAP_ACTION);
            if (soapaction != null) {
                mc.setSoapAction((String) soapaction.getValue());
            }
        }
        addReferenceParameters(mc.getEnvelope());
        if (options.isUseSeparateListener()) {
            CallbackReceiver callbackReceiver = (CallbackReceiver) axisOp
                    .getMessageReceiver();
            callbackReceiver.addCallback(mc.getMessageID(), callback);
            EndpointReference replyToFromTransport = ListenerManager
                    .replyToEPR(cc, sc.getAxisService().getName() + "/"
                            + axisOp.getName().getLocalPart(), options
                            .getTransportIn().getName()
                            .getLocalPart());

            if (mc.getReplyTo() == null) {
                mc.setReplyTo(replyToFromTransport);
            } else {
                mc.getReplyTo().setAddress(replyToFromTransport.getAddress());
            }
            AxisEngine engine = new AxisEngine(cc);
            engine.send(mc);
        } else {
            if (block) {
                // Send the SOAP Message and receive a response
                MessageContext response = send(mc);
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

    private void addReferenceParameters(SOAPEnvelope env) {
        if (options.isManageSession()) {
            EndpointReference tepr = sc.getTargetEPR();
            if (tepr != null) {
                Map map = tepr.getAllReferenceParameters();
                Iterator valuse = map.values().iterator();
                SOAPHeader sh = env.getHeader();
                while (valuse.hasNext()) {
                    Object refparaelement = valuse.next();
                    if (refparaelement instanceof OMElement) {
                        sh.addChild((OMElement) refparaelement);
                    }
                }
            }
        }
    }

    /**
     * Sends the message using a two way transport and waits for a response
     *
     * @param msgctx
     * @return Returns MessageContext.
     * @throws AxisFault
     */
    private MessageContext send(MessageContext msgctx) throws AxisFault {

        AxisEngine engine = new AxisEngine(msgctx.getConfigurationContext());

        engine.send(msgctx);

        // create the responseMessageContext
        MessageContext responseMessageContext = new MessageContext();
        responseMessageContext.setTransportIn(msgctx.getTransportIn());
        responseMessageContext.setTransportOut(msgctx.getTransportOut());

        // This is a hack - Needs to change
        responseMessageContext.setOptions(options);


        responseMessageContext.setProperty(MessageContext.TRANSPORT_IN, msgctx
                .getProperty(MessageContext.TRANSPORT_IN));
        responseMessageContext.setServerSide(false);
        responseMessageContext.setDoingREST(msgctx.isDoingREST());
        addMessageContext(responseMessageContext);

        // If request is REST we assume the responseMessageContext is REST, so
        // set the variable


        SOAPEnvelope resenvelope = TransportUtils.createSOAPMessage(
                responseMessageContext, msgctx.getEnvelope().getNamespace()
                .getName());
        try {
            // Adding request reference parameters into ServiceContext , so then in the next
            // requesy automatically send them back
            sc.setTargetEPR(getReplyToEPR(resenvelope.getHeader()
                    .getFirstChildWithName(new QName("ReplyTo"))));
        } catch (Exception e) {
            //NPE may occure there for need to catch this
        }

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

    private EndpointReference getReplyToEPR(OMElement headerElement) {
        EndpointReference epr = new EndpointReference(null);
        if(headerElement == null)
            return null;
        
        Iterator childElements = headerElement.getChildElements();
        while (childElements.hasNext()) {
            OMElement eprChildElement = (OMElement) childElements.next();
            if (AddressingConstants.EPR_ADDRESS.equals(eprChildElement.getLocalName())) {
            } else if (AddressingConstants.EPR_REFERENCE_PARAMETERS.equals(eprChildElement.getLocalName())) {

                Iterator referenceParameters = eprChildElement.getChildElements();
                while (referenceParameters.hasNext()) {
                    OMElement element = (OMElement) referenceParameters.next();
                    epr.addReferenceParameter(element);
                }
            } else if (AddressingConstants.Final.WSA_METADATA.equals(eprChildElement.getLocalName())) {
                epr.setMetaData(eprChildElement);
            }
        }
        return epr;
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
            throw new AxisFault("MEP is not yet complete: cannot reset");
        }
        oc = null;
        completed = false;
    }

    public void complete(MessageContext msgCtxt) throws AxisFault {
        ListenerManager.stop(msgCtxt.getConfigurationContext(),
                msgCtxt.getTransportIn().getName().getLocalPart());
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
                MessageContext response = send(msgctx);

                // call the callback
                SOAPEnvelope resenvelope = response.getEnvelope();
                resenvelope.build();
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
