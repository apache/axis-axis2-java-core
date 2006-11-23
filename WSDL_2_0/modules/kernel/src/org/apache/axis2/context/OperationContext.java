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


package org.apache.axis2.context;

import org.apache.axis2.AxisFault;
import org.apache.axis2.description.AxisOperation;

import java.util.HashMap;
import java.util.Map;

/**
 * An OperationContext represents a running "instance" of an operation, which is
 * represented by an AxisOperation object. This concept is needed to allow
 * messages to be grouped into operations as in WSDL 2.0-speak operations are
 * essentially arbitrary message exchange patterns. So as messages are being
 * exchanged the OperationContext remembers the state of where in the message
 * exchange pattern it is in.
 * <p/>
 * OperationContextFactory factory. The base implementation of OperationContext
 * supports MEPs which have one input message and/or one output message. That
 * is, it supports the all the MEPs that are in the WSDL 2.0 specification. In
 * order to support another MEP one must extend this class and register its
 * creation in the OperationContexFactory.
 */
public class OperationContext extends AbstractContext {
    private boolean isComplete;

    //The key value of the operationContextMap;
    private String key;

    // the AxisOperation of which this is a running instance. The MEP of this
    // AxisOperation must be one of the 8 predefined ones in WSDL 2.0.
    private transient AxisOperation axisOperation;
    private HashMap messageContexts;

    // this is the global MessageID -> OperationContext map which is stored in
    // the EngineContext. We're caching it here for faster access.
    private Map operationContextMap;

    public OperationContext(AxisOperation axisOperation) {
        super(null);
        this.messageContexts = new HashMap();
        this.axisOperation = axisOperation;
    }

    /**
     * Constructs a new OperationContext.
     *
     * @param axisOperation  the AxisOperation whose running instances' state this
     *                       OperationContext represents.
     * @param serviceContext the parent ServiceContext representing any state related to
     *                       the set of all operations of the service.
     */
    public OperationContext(AxisOperation axisOperation,
                            ServiceContext serviceContext) {
        super(serviceContext);
        this.messageContexts = new HashMap();
        this.axisOperation = axisOperation;
        this.operationContextMap =
                getServiceContext().getConfigurationContext()
                        .getOperationContextMap();
    }

    /**
     * When a new message is added to the <code>MEPContext</code> the logic
     * should be included remove the MEPContext from the table in the
     * <code>EngineContext</code>. Example: IN_IN_OUT At the second IN
     * message the MEPContext should be removed from the AxisOperation.
     *
     * @param msgContext
     */
    public void addMessageContext(MessageContext msgContext) throws AxisFault {
        if (axisOperation != null) {
            axisOperation.addMessageContext(msgContext, this);
            touch();
        }
    }


    /**
     * Removes the pointers to this <code>OperationContext</code> in the
     * <code>ConfigurationContext</code>'s OperationContextMap so that this
     * <code>OperationContext</code> will eventually get garbage collected
     * along with the <code>MessageContext</code>'s it contains. Note that if
     * the caller wants to make sure its safe to clean up this OperationContext
     * he should call isComplete() first. However, in cases like IN_OPTIONAL_OUT
     * and OUT_OPTIONAL_IN, it is possibe this will get called without the MEP
     * being complete due to the optional nature of the MEP.
     */
    public void cleanup() {
        if (key != null) {
            operationContextMap.remove(key);
        }
    }

    /**
     * @return Returns the axisOperation.
     */
    public AxisOperation getAxisOperation() {
        return axisOperation;
    }

    /**
     * Returns the EngineContext in which the parent ServiceContext lives.
     *
     * @return Returns parent ServiceContext's parent EngineContext.
     */
    public ConfigurationContext getConfigurationContext() {
        return ((ServiceContext) parent).getConfigurationContext();
    }

    /**
     * @param messageLabel
     * @return Returns MessageContext.
     * @throws AxisFault
     */
    public MessageContext getMessageContext(String messageLabel)
            throws AxisFault {
        return (MessageContext) messageContexts.get(messageLabel);
    }

    public HashMap getMessageContexts() {
        return messageContexts;
    }

    /**
     * Returns the ServiceContext in which this OperationContext lives.
     *
     * @return Returns parent ServiceContext.
     */
    public ServiceContext getServiceContext() {
        return (ServiceContext) parent;
    }

    /**
     * Checks to see if the MEP is complete. i.e. whether all the messages that
     * are associated with the MEP has arrived and MEP is complete.
     */
    public boolean isComplete() {
        return isComplete;
    }

    public void setComplete(boolean complete) {
        isComplete = complete;
    }

    public void setParent(AbstractContext context) {
        super.setParent(context);
        this.operationContextMap =
                getServiceContext().getConfigurationContext()
                        .getOperationContextMap();
    }

    public void setKey(String key) {
        this.key = key;
    }
}
