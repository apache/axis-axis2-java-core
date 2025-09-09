/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


package org.apache.axis2.receivers;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.soap.SOAP11Constants;
import org.apache.axiom.soap.SOAP12Constants;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.classloader.ThreadContextDescriptor;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.description.InOnlyAxisOperation;
import org.apache.axis2.description.WSDL2Constants;
import org.apache.axis2.engine.AxisEngine;
import org.apache.axis2.engine.DependencyManager;
import org.apache.axis2.engine.MessageReceiver;
import org.apache.axis2.i18n.Messages;
import org.apache.axis2.util.JavaUtils;
import org.apache.axis2.util.MessageContextBuilder;
import org.apache.axis2.util.Utils;
import org.apache.axis2.wsdl.WSDLUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.security.PrivilegedAction;

public abstract class AbstractMessageReceiver implements MessageReceiver {
    protected static final Log log = LogFactory.getLog(AbstractMessageReceiver.class);

    public static final String SCOPE = "scope";
    protected String serviceTCCL = null;
    public static final String SAVED_TCCL = "_SAVED_TCCL_";
    public static final String SAVED_MC = "_SAVED_MC_";
    public static final String DO_ASYNC = "messageReceiver.invokeOnSeparateThread";
    


    /**
     * Do the actual work of the MessageReceiver.  Must be overridden by concrete subclasses.
     *
     * @param messageCtx active MessageContext
     * @throws AxisFault if a problem occurred
     */
    protected abstract void invokeBusinessLogic(MessageContext messageCtx) throws AxisFault;

    /**
     *
     * @param messageCtx active MessageContext
     * @throws AxisFault if a problem occurred
     */
    public void receive(final MessageContext messageCtx) throws AxisFault {
        // Checking whether the replyTo address, if it is non Anonymous then we need to send the ACK and
        // send the reply to on replyTo address
        EndpointReference replyTo = messageCtx.getReplyTo();
        if (replyTo != null && !replyTo.hasAnonymousAddress()) {
            // We have a valid reply to address, so processing the request through AsyncMessageReceiverWorker and send the ACK
            processAsAsync(messageCtx);
            return;
        }
        // Checking for long running services
        if (messageCtx.isPropertyTrue(DO_ASYNC)
                || ((messageCtx.getParameter(DO_ASYNC) != null) &&
                JavaUtils.isTrueExplicitly(messageCtx.getParameter(DO_ASYNC).getValue()))) {
            String mep = messageCtx.getAxisOperation()
                    .getMessageExchangePattern();
            // Checking whether the replyTo address is valid, so that we can send the Application response
            // In order to invoke the service in the ASYNC mode, the request
			// should contain ReplyTo header if the MEP of the service is not
			// InOnly type
            if ((!WSDLUtil.isOutputPresentForMEP(mep))
                    || (replyTo != null && !replyTo.hasAnonymousAddress())) {
                processAsAsync(messageCtx);
                return;
            }

        }

        ThreadContextDescriptor tc = ThreadContextDescriptor.setThreadContext(messageCtx);
        try {
            invokeBusinessLogic(messageCtx);
        } catch (AxisFault fault) {
            // signal the transport to rollback the tx, if any
            messageCtx.setProperty(Constants.SET_ROLLBACK_ONLY, true);
            // If we're in-only, eat this.  Otherwise, toss it upwards!
            if ((messageCtx.getAxisOperation() instanceof InOnlyAxisOperation) &&
                    !WSDL2Constants.MEP_URI_ROBUST_IN_ONLY.equals(messageCtx.getAxisOperation().getMessageExchangePattern())) {
                log.error(fault);
            } else {
                fault.setFaultType(Constants.APPLICATION_FAULT);
                throw fault;
            }
        } finally {
            //We can call the serviceContext destroy method for request scope services
            if (Constants.SCOPE_REQUEST.equals(messageCtx.getAxisService().getScope())) {
                DependencyManager.destroyServiceObject(messageCtx.getServiceContext());
            }
            restoreThreadContext(tc);
        }
    }

    /**
     * This is to create a separate thread to process business logic invocation. We create a AsyncMessageReceiverWorker
     * which internally calls the message receiver specified for the operation.
     *
     * We send the ACK through the incoming transport and reply through the address specified in replyTo address.
     * @param messageCtx msgContext the current MessageContext
     */
    private void processAsAsync(MessageContext messageCtx) {
        AsyncMessageReceiverWorker worker = new AsyncMessageReceiverWorker(
                messageCtx);
        if (messageCtx.isDoingMTOM() || messageCtx.isDoingSwA()) {
            // If we are doing MTOM or SWA then we need to build with attachment, because we are going to close the incoming connection
            messageCtx.getEnvelope().buildWithAttachments();
        } else {
            // We need to build the envelop since we are going to close the input stream
            messageCtx.getEnvelope().build();
        }

        messageCtx.getConfigurationContext().getThreadPool().execute(
                worker);
    }

    

   
    protected void restoreThreadContext(final ThreadContextDescriptor tc) {
        org.apache.axis2.java.security.AccessController.doPrivileged(
                new PrivilegedAction() {
                    public Object run() {
                        Thread.currentThread().setContextClassLoader(tc.getOldClassLoader());
                        return null;
                    }
                }
        );
        MessageContext.currentMessageContext.set(tc.getOldMessageContext());
    }

    /**
     * Create a new service object.  Override if you want to customize how
     * this happens in your own MessageReceiver.
     *
     * @param msgContext
     * @return Returns Object.
     * @throws AxisFault
     */
    protected Object makeNewServiceObject(MessageContext msgContext) throws AxisFault {
        Object serviceObject = Utils.createServiceObject(msgContext.getAxisService());
        if (serviceObject == null) {
            throw new AxisFault(
                    Messages.getMessage("paramIsNotSpecified", "SERVICE_OBJECT_SUPPLIER"));
        } else {
            return serviceObject;
        }
    }

    public SOAPFactory getSOAPFactory(MessageContext msgContext) throws AxisFault {
        String nsURI = msgContext.getEnvelope().getNamespace().getNamespaceURI();
        if (SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI.equals(nsURI)) {
            return OMAbstractFactory.getSOAP12Factory();
        } else if (SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI.equals(nsURI)) {
            return OMAbstractFactory.getSOAP11Factory();
        } else {
            throw new AxisFault(Messages.getMessage("invalidSOAPversion"));
        }
    }

    /**
     * Retrieve the implementation object.  This will either return a cached
     * object if present in the ServiceContext, or create a new one via
     * makeNewServiceObject() (and then cache that).
     *
     * @param msgContext the active MessageContext
     * @return the appropriate back-end service object.
     * @throws AxisFault if there's a problem
     */
    protected Object getTheImplementationObject(MessageContext msgContext) throws AxisFault {
        ServiceContext serviceContext = msgContext.getServiceContext();
        Object serviceimpl = serviceContext.getProperty(ServiceContext.SERVICE_OBJECT);
        if (serviceimpl != null) {
            // since service impl is there in service context , take that from there
            return serviceimpl;
        } else {
            // create a new service impl class for that service
            serviceimpl = makeNewServiceObject(msgContext);
            //Service initialization
            DependencyManager.initServiceObject(serviceimpl, msgContext.getServiceContext());
            serviceContext.setProperty(ServiceContext.SERVICE_OBJECT, serviceimpl);
            return serviceimpl;
        }
    }

    public class AsyncMessageReceiverWorker implements Runnable {
    	private MessageContext messageCtx;

    	public AsyncMessageReceiverWorker(MessageContext messageCtx){
    		this.messageCtx = messageCtx;
    	}

        public void run() {
            try {
                ThreadContextDescriptor tc = ThreadContextDescriptor.setThreadContext(messageCtx);
                try {
                    invokeBusinessLogic(messageCtx);
                } finally {
                    restoreThreadContext(tc);
                }
            } catch (AxisFault e) {
                // If we're IN-ONLY, swallow this.  Otherwise, send it.
                if (messageCtx.getAxisOperation() instanceof InOnlyAxisOperation) {
                    log.debug(e.getMessage(), e);
                } else {
                    try {
                        MessageContext faultContext =
                                MessageContextBuilder.createFaultMessageContext(messageCtx, e);

                        AxisEngine.sendFault(faultContext);
                    } catch (AxisFault axisFault) {
                        log.error(e.getMessage(), e);
                    }
                    log.error(e.getMessage(), e);
                }
            }
        }
    }
}
