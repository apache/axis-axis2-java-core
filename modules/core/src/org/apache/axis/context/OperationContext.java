package org.apache.axis.context;

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

import org.apache.axis.description.OperationDescription;
import org.apache.axis.engine.AxisError;
import org.apache.axis.engine.AxisFault;
import org.apache.wsdl.WSDLConstants;

import java.util.Map;

/**
 * An OperationContext represents a running "instance" of an operation, which is
 * represented by an OperationDescription object. This concept is needed to allow
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
    // the in and out messages that may be present
    private MessageContext inMessageContext;

    private MessageContext outMessageContext;

    // the OperationDescription of which this is a running instance. The MEP of this
    // OperationDescription must be one of the 8 predefined ones in WSDL 2.0.
    private OperationDescription axisOperation;

    private int operationMEP;

    private boolean isComplete = false;

    // this is the global MessageID -> OperationContext map which is stored in
    // the EngineContext. We're caching it here for faster acccess.
    private Map operationContextMap;

    /**
     * Construct a new OperationContext.
     *
     * @param axisOperation  the OperationDescription whose running instances' state this
     *                       OperationContext represents.
     * @param serviceContext the parent ServiceContext representing any state related to
     *                       the set of all operations of the service.
     */
    public OperationContext(OperationDescription axisOperation,
                            ServiceContext serviceContext) {
        super(serviceContext);
        this.axisOperation = axisOperation;
        this.operationMEP = axisOperation.getAxisSpecifMEPConstant();
        this.operationContextMap = getServiceContext().getEngineContext()
                .getOperationContextMap();
    }

    /**
     * @return Returns the axisOperation.
     */
    public OperationDescription getAxisOperation() {
        return axisOperation;
    }

    /**
     * Return the ServiceContext in which this OperationContext lives.
     *
     * @return parent ServiceContext
     */
    public ServiceContext getServiceContext() {
        return (ServiceContext) parent;
    }

    /**
     * Return the EngineContext in which the parent ServiceContext lives.
     *
     * @return parent ServiceContext's parent EngineContext
     */
    public ConfigurationContext getEngineContext() {
        return (ConfigurationContext) parent.parent;
    }

    /**
     * When a new message is added to the <code>MEPContext</code> the logic
     * should be included remove the MEPContext from the table in the
     * <code>EngineContext</code>. Example: IN_IN_OUT At the second IN
     * message the MEPContext should be removed from the OperationDescription
     *
     * @param msgContext
     */
    public void addMessageContext(MessageContext msgContext) throws AxisFault {
        // this needs to store the msgContext in either inMessageContext or
        // outMessageContext depending on the MEP of the OperationDescription
        // and on the current state of the operation.
        if (WSDLConstants.MEP_CONSTANT_IN_OUT == operationMEP
                || WSDLConstants.MEP_CONSTANT_IN_OPTIONAL_OUT == operationMEP
                || WSDLConstants.MEP_CONSTANT_ROBUST_IN_ONLY == operationMEP) {
            if (inMessageContext == null) {
                inMessageContext = msgContext;
            } else {
                outMessageContext = msgContext;
                isComplete = true;
            }
        } else if (WSDLConstants.MEP_CONSTANT_IN_ONLY == operationMEP) {
            inMessageContext = msgContext;
            isComplete = true;
        } else if (WSDLConstants.MEP_CONSTANT_OUT_ONLY == operationMEP) {
            outMessageContext = msgContext;
            isComplete = true;
        } else if (WSDLConstants.MEP_CONSTANT_OUT_IN == operationMEP
                || WSDLConstants.MEP_CONSTANT_OUT_OPTIONAL_IN == operationMEP
                || WSDLConstants.MEP_CONSTANT_ROBUST_IN_ONLY == operationMEP) {
            if (outMessageContext == null) {
                outMessageContext = msgContext;
            } else {
                inMessageContext = msgContext;
                isComplete = true;
            }
        } else {
            // NOT REACHED: the factory created this context incorrectly
            throw new AxisError("Invalid behavior of OperationContextFactory");
        }
    }

    /**
     * @param messageLabel
     * @return
     * @throws AxisFault
     */
    public MessageContext getMessageContext(byte messageLabel) throws AxisFault {
        if (messageLabel == WSDLConstants.MESSAGE_LABEL_IN) {
            return inMessageContext;
        } else if (messageLabel == WSDLConstants.MESSAGE_LABEL_OUT) {
            return outMessageContext;
        } else {
            throw new AxisFault("Unrecognized message label: '" + messageLabel
                    + "'");
        }
    }

   /**
     * Checks to see if the MEP is complete. i.e. whether all the messages that
     * are associated with the MEP has arrived and MEP is complete.
     *
     * @return
     */
    public boolean isComplete() {
        return isComplete;
    }

    /**
     * Removes the pointers to this <code>OperationContext</code> in the
     * <code>EngineContext</code>'s OperationContextMap so that this
     * <code>OperationContext</code> will eventually get garbage collected
     * along with the <code>MessageContext</code>'s it contains. Note that if
     * the caller wants to make sure its safe to clean up this OperationContext
     * he should call isComplete() first. However, in cases like IN_OPTIONAL_OUT
     * and OUT_OPTIONAL_IN, it is possibe this will get called without the MEP
     * being complete due to the optional nature of the MEP.
     */
    public void cleanup() {
        if (null != this.inMessageContext) {
            operationContextMap.remove(inMessageContext.getMessageID());
        }
        if (null != this.outMessageContext) {
            operationContextMap.remove(outMessageContext.getMessageID());
        }
    }


//    public MessageContext createMessageContext(AxisM){
//    
//    }

}