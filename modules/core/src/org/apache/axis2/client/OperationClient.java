package org.apache.axis2.client;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.async.Callback;
import org.apache.axis2.context.MessageContext;

/**
 * An operation client is the way an advanced user interacts with Axis2. Actual
 * operation clients understand a specific MEP and hence their behavior is
 * defined by their MEP. To interact with an operation client, you first get one
 * from a specific AxisOperation. Then you set the messages into it one by one
 * (whatever is available). Then, when you call execute() the client will
 * execute what it can at that point. If executing the operation client results
 * in a new message being created, then if a message receiver is registered with
 * the client then the message will be delivered to that client.
 */
public interface OperationClient {
    /**
     * Sets the options that should be used for this particular client. This
     * resets the entire set of options to use the new options - so you'd lose
     * any option cascading that may have been set up.
     * 
     * @param options
     *            the options
     */
    public void setOptions(Options options);

    /**
     * Return the options used by this client. If you want to set a single
     * option, then the right way is to do getOptions() and set specific
     * options.
     * 
     * @return the options, which will never be null.
     */
    public Options getOptions();

    /**
     * Add a message context to the client for processing. This method must not
     * process the message - it only records it in the operation client.
     * Processing only occurs when execute() is called.
     * 
     * @param mc
     *            the message context
     * @throws AxisFault
     *             if this is called inappropriately.
     */
    public void addMessageContext(MessageContext mc) throws AxisFault;

    /**
     * Return a message from the client - will return null if the requested
     * message is not available.
     * 
     * @param messageLabel
     *            the message label of the desired message context
     * @return the desired message context or null if its not available.
     * @throws AxisFault
     *             if the message label is invalid
     */
    public MessageContext getMessageContext(String messageLabel)
            throws AxisFault;

    /**
     * Set the callback to be executed when a message comes into the MEP and the
     * operation client is executed. This is the way the operation client
     * provides notification that a message has been received by it. Exactly
     * when its executed and under what conditions is a function of the specific
     * operation client.
     * 
     * @param callback
     *            the callback to be used when the client decides its time to
     *            use it
     */
    public void setCallback(Callback callback);

    /**
     * Execute the MEP. What this does depends on the specific operation client.
     * The basic idea is to have the operation client execute and do something
     * with the messages that have been added to it so far. For example, if its
     * an Out-In MEP, then if the Out message has been set, then executing the
     * client asks it to send the message and get the In message, possibly using
     * a different thread.
     * 
     * @param block
     *            Indicates whether execution should block or return ASAP. What
     *            block means is of course a function of the specific operation
     *            client.
     * @throws AxisFault
     *             if something goes wrong during the execution of the operation
     *             client.
     */
    public void execute(boolean block) throws AxisFault;

    /**
     * Reset the operation client to a clean status after the MEP has completed.
     * This is how you can reuse an operation client. NOTE: this does not reset
     * the options; only the internal state so the client can be used again.
     * 
     * @throws AxisFault
     *             if reset is called before the MEP client has completed an
     *             interaction.
     */
    public void reset() throws AxisFault;
}
