package org.apache.axis2.description;

import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.ListenerManager;
import org.apache.axis2.client.OperationClient;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.async.Callback;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.OperationContext;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.engine.AxisEngine;
import org.apache.axis2.util.UUIDGenerator;
import org.apache.wsdl.WSDLConstants;

import javax.xml.namespace.QName;
import java.util.ArrayList;

public class OutOnlyAxisOperation extends AxisOperation {
    private AxisMessage inFaultMessage;

    // just to keep the inflow , there wont be any usage
    private ArrayList inPhases;

    private AxisMessage outFaultMessage;

    private AxisMessage outMessage;

    public OutOnlyAxisOperation() {
        super();
        createMessage();
    }

    public OutOnlyAxisOperation(QName name) {
        super(name);
        createMessage();
    }

    public void addMessage(AxisMessage message, String label) {
        if (WSDLConstants.MESSAGE_LABEL_OUT_VALUE.equals(label)) {
            outMessage = message;
        } else {
            throw new UnsupportedOperationException("Not yet implemented");
        }
    }

    public void addMessageContext(MessageContext msgContext,
                                  OperationContext opContext) throws AxisFault {
        if (!opContext.isComplete()) {
            opContext.getMessageContexts().put(MESSAGE_LABEL_OUT_VALUE,
                    msgContext);
            opContext.setComplete(true);
        } else {
            throw new AxisFault(
                    "Invalid message addition , operation context completed");
        }
    }

    private void createMessage() {
        outMessage = new AxisMessage();
        outMessage.setDirection(WSDLConstants.WSDL_MESSAGE_DIRECTION_OUT);
        inFaultMessage = new AxisMessage();
        outFaultMessage = new AxisMessage();
        inPhases = new ArrayList();
    }

    public AxisMessage getMessage(String label) {
        if (WSDLConstants.MESSAGE_LABEL_OUT_VALUE.equals(label)) {
            return outMessage;
        } else {
            throw new UnsupportedOperationException("Not yet implemented");
        }
    }

    public ArrayList getPhasesInFaultFlow() {
        return inFaultMessage.getMessageFlow();
    }

    public ArrayList getPhasesOutFaultFlow() {
        return outFaultMessage.getMessageFlow();
    }

    public ArrayList getPhasesOutFlow() {
        return outMessage.getMessageFlow();
    }

    public ArrayList getRemainingPhasesInFlow() {
        return inPhases;
    }

    public void setPhasesInFaultFlow(ArrayList list) {
        inFaultMessage.setMessageFlow(list);
    }

    public void setPhasesOutFaultFlow(ArrayList list) {
        outFaultMessage.setMessageFlow(list);
    }

    public void setPhasesOutFlow(ArrayList list) {
        outMessage.setMessageFlow(list);
    }

    public void setRemainingPhasesInFlow(ArrayList list) {
        inPhases = list;
    }

    /**
     * Returns a MEP client for an Out-only operation. This client can be used to
     * interact with a server which is offering an In-only operation. To use the
     * client, you must call addMessageContext() with a message context and then
     * call execute() to execute the client. Note that the execute method's
     * block parameter is ignored by this client and also the setMessageReceiver
     * method cannot be used.
     *
     * @param sc      The service context for this client to live within. Cannot be
     *                null.
     * @param options Options to use as defaults for this client. If any options are
     *                set specifically on the client then those override options
     *                here.
     */
    public OperationClient createClient(ServiceContext sc, Options options) {
        return new OutOnlyAxisOperationClient(this, sc, options);
    }
}

/**
 * MEP client for moi.
 */
class OutOnlyAxisOperationClient implements OperationClient {
    OutOnlyAxisOperation axisOp;

    ServiceContext sc;

    Options options;

    MessageContext mc;

    OperationContext oc;

    /*
    * indicates whether the MEP execution has completed (and hence ready for
    * resetting)
    */
    boolean completed;

    OutOnlyAxisOperationClient(OutOnlyAxisOperation axisOp, ServiceContext sc,
                               Options options) {
        this.axisOp = axisOp;
        this.sc = sc;
        this.options = new Options(options);
        this.completed = false;
        oc = new OperationContext(axisOp, sc);
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
     * option, then the right way is to do getOptions() and set specific
     * options.
     *
     * @return Returns the options, which will never be null.
     */
    public Options getOptions() {
        return options;
    }

    /**
     * Adds a message context to the client for processing. This method must not
     * process the message - it only records it in the MEP client. Processing
     * only occurs when execute() is called.
     *
     * @param mc the message context
     * @throws AxisFault if this is called inappropriately.
     */
    public void addMessageContext(MessageContext mc) throws AxisFault {
        if (this.mc != null) {
            throw new AxisFault(
                    "Can't add message context again until client has been executed");
        }
        this.mc = mc;
        if (mc.getMessageID() == null) {
            setMessageID(mc);
        }
        axisOp.registerOperationContext(mc, oc);
        this.completed = false;
    }

    /**
     * Returns a message from the client - will return null if the requested
     * message is not available.
     *
     * @param messageLabel the message label of the desired message context
     * @return Returns the desired message context or null if its not available.
     * @throws AxisFault if the message label is invalid
     */
    public MessageContext getMessageContext(String messageLabel)
            throws AxisFault {
        if (messageLabel.equals(WSDLConstants.MESSAGE_LABEL_OUT_VALUE)) {
            return mc;
        }
        throw new AxisFault("Unknown message label: '" + messageLabel + "'");
    }

    /**
     * Sets the message receiver to be executed when a message comes into the MEP
     * and the MEP is executed. This is the way the MEP client provides
     * notification that a message has been received by it. Exactly when its
     * executed and under what conditions is a function of the specific MEP
     * client.
     */
    public void setCallback(Callback callback) {
        throw new UnsupportedOperationException(
                "This feature is not supported by this MEP");
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

        // set options on the message context
        mc.setOptions(options);
        // setting messge ID if it null
        // if the transport to use for sending is not specified, try to find it
        // from the URL
        TransportOutDescription senderTransport = options.getTranportOut();
        if (senderTransport == null) {
            EndpointReference toEPR = (options.getTo() != null) ? options
                    .getTo() : mc.getTo();
            senderTransport = ClientUtils.inferOutTransport(cc
                    .getAxisConfiguration(), toEPR);
        }
        mc.setTransportOut(senderTransport);

        if (mc.getSoapAction() == null) {
            Parameter soapaction = axisOp.getParameter(AxisOperation.SOAP_ACTION);
            if (soapaction != null) {
                mc.setSoapAction((String) soapaction.getValue());
            }
        }

        // create the operation context for myself
        OperationContext oc = new OperationContext(axisOp, sc);
        oc.addMessageContext(mc);
        // ship it out
        AxisEngine engine = new AxisEngine(cc);
        engine.send(mc);

        // all done
        completed = true;
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
        mc = null;
        completed = false;
    }

    public void complete(MessageContext msgCtxt) throws AxisFault {
        ListenerManager.stop(msgCtxt.getConfigurationContext(),
                msgCtxt.getTransportIn().getName().getLocalPart());
    }
}
