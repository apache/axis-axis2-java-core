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
package org.apache.axis2.jaxws.core.controller;

import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants.Configuration;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.OperationClient;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.AbstractContext;
import org.apache.axis2.context.OperationContext;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.engine.MessageReceiver;
import org.apache.axis2.jaxws.BindingProvider;
import org.apache.axis2.jaxws.ExceptionFactory;
import org.apache.axis2.jaxws.client.ClientUtils;
import org.apache.axis2.jaxws.client.async.AsyncResponse;
import org.apache.axis2.jaxws.client.async.CallbackFuture;
import org.apache.axis2.jaxws.client.async.PollingFuture;
import org.apache.axis2.jaxws.core.InvocationContext;
import org.apache.axis2.jaxws.core.MEPContext;
import org.apache.axis2.jaxws.core.MessageContext;
import org.apache.axis2.jaxws.description.OperationDescription;
import org.apache.axis2.jaxws.i18n.Messages;
import org.apache.axis2.jaxws.message.Message;
import org.apache.axis2.jaxws.message.factory.MessageFactory;
import org.apache.axis2.jaxws.message.util.MessageUtils;
import org.apache.axis2.jaxws.registry.FactoryRegistry;
import org.apache.axis2.jaxws.util.Constants;
import org.apache.axis2.jaxws.utility.ClassUtils;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.transport.http.HttpTransportProperties;
import org.apache.axis2.util.CallbackReceiver;
import org.apache.axis2.util.ThreadContextMigratorUtil;
import org.apache.axis2.wsdl.WSDLConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.namespace.QName;
import javax.xml.ws.AsyncHandler;
import javax.xml.ws.Response;
import javax.xml.ws.WebServiceException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.Future;

/**
 * The <tt>AxisInvocationController</tt> is an implementation of the {@link
 * org.apache.axis2.jaxws.core.controller.InvocationController} interface.  This implemenation uses
 * the Axis2 engine to drive the request to the target service.
 * <p/>
 * For more information on how to invoke this class, please see the InvocationController interface
 * comments.
 */
public class AxisInvocationController extends InvocationController {

    private static Log log = LogFactory.getLog(AxisInvocationController.class);

    /*
    * (non-Javadoc)
    * @see org.apache.axis2.jaxws.core.controller.InvocationController#invoke(org.apache.axis2.jaxws.core.InvocationContext)
    */
    public MessageContext doInvoke(MessageContext request) {
        // We need the qname of the operation being invoked to know which 
        // AxisOperation the OperationClient should be based on.
        // Note that the OperationDesc is only set through use of the Proxy. Dispatch
        // clients do not use operations, so the operationDesc will be null.  In this
        // case an anonymous AxisService with anoymouns AxisOperations for the supported
        // MEPs will be created; and it is that anonymous operation name which needs to
        // be specified
        QName operationName = getOperationNameToUse(request, ServiceClient.ANON_OUT_IN_OP);

        // TODO: Will the ServiceClient stick around on the InvocationContext
        // or will we need some other mechanism of creating this?
        // Try to create an OperationClient from the passed in ServiceClient
        InvocationContext ic = request.getInvocationContext();
        ServiceClient svcClient = ic.getServiceClient();
        OperationClient opClient = createOperationClient(svcClient, operationName);

        initOperationClient(opClient, request);

        org.apache.axis2.context.MessageContext axisRequestMsgCtx = request.getAxisMessageContext();
        org.apache.axis2.context.MessageContext axisResponseMsgCtx = null;

        MessageContext response = null;

        AxisFault faultexception =
                null;  // don't let the keyword "fault" confuse you.  This is an exception class.
        try {
            execute(opClient, true, axisRequestMsgCtx);
        } catch (AxisFault af) {
            // save the fault in case it didn't come from the endpoint, and thus
            // there would be no message on the MessageContext
            faultexception = af;
            if (log.isDebugEnabled()) {
                log.debug(axisRequestMsgCtx.getLogIDString() + " AxisFault received from client: " +
                        af.getMessage());
            }
        }

        try {
            // Collect the response MessageContext and envelope
            axisResponseMsgCtx = opClient.getMessageContext(WSDLConstants.MESSAGE_LABEL_IN_VALUE);
            response = new MessageContext(axisResponseMsgCtx);
            response.setMEPContext(request.getMEPContext());

            // If the Message object is still null, then it's possible that a
            // local AxisFault was thrown and we need to save it for later throwing
            // We do not want to create a message and go through the whole handler or
            // XMLFault processing because it's unnecessary.
            // 
            // Same is true if we get a valid non-fault server response but some jaxws
            // client processing (a handler, perhaps) throws an exception.
            // 
            // If the response message itself is a fault message, let it pass through.
            if ((faultexception != null) && ((response.getMessage() == null)
                    || (!response.getMessage().isFault()))) {
                MessageFactory factory =
                        (MessageFactory)FactoryRegistry.getFactory(MessageFactory.class);
                Message message = factory.create(request.getMessage().getProtocol());
                response.setLocalException(faultexception);
                response.setMessage(message);
            }

            // This assumes that we are on the ultimate execution thread
            ThreadContextMigratorUtil.performMigrationToThread(
                    Constants.THREAD_CONTEXT_MIGRATOR_LIST_ID, axisResponseMsgCtx);
        } catch (Exception e) {
            throw ExceptionFactory.makeWebServiceException(e);
        }

        return response;
    }

    /*
    *  (non-Javadoc)
    * @see org.apache.axis2.jaxws.core.controller.InvocationController#invokeOneWay(org.apache.axis2.jaxws.core.InvocationContext)
    */
    public void doInvokeOneWay(MessageContext request) throws WebServiceException {
        // We need the qname of the operation being invoked to know which 
        // AxisOperation the OperationClient should be based on.
        // Note that the OperationDesc is only set through use of the Proxy. Dispatch
        // clients do not use operations, so the operationDesc will be null.  In this
        // case an anonymous AxisService with anoymouns AxisOperations for the supported
        // MEPs will be created; and it is that anonymous operation name which needs to
        // be specified
        QName operationName = getOperationNameToUse(request, ServiceClient.ANON_OUT_ONLY_OP);

        InvocationContext ic = request.getInvocationContext();
        ServiceClient svcClient = ic.getServiceClient();
        OperationClient opClient = createOperationClient(svcClient, operationName);

        initOperationClient(opClient, request);

        org.apache.axis2.context.MessageContext axisRequestMsgCtx = request.getAxisMessageContext();

        try {
            execute(opClient, true, axisRequestMsgCtx);
        } catch (AxisFault af) {
            // JAXWS 6.4.2 says to throw it...
            // Whatever exception we get here will not be from the server since a one-way
            // invocation has no response.  This will always be a SENDER fault
            if (log.isDebugEnabled()) {
                log.debug(axisRequestMsgCtx.getLogIDString() + " AxisFault received from client: " +
                        af.getMessage());
            }
            throw ExceptionFactory.makeWebServiceException(ClassUtils.getRootCause(af));
        }

        return;
    }

    /*
    *  (non-Javadoc)
    * @see org.apache.axis2.jaxws.core.controller.InvocationController#invokeAsync(org.apache.axis2.jaxws.core.InvocationContext, javax.xml.ws.AsyncHandler)
    */
    public Future<?> doInvokeAsync(MessageContext request, AsyncHandler callback) {
        // We need the qname of the operation being invoked to know which 
        // AxisOperation the OperationClient should be based on.
        // Note that the OperationDesc is only set through use of the Proxy. Dispatch
        // clients do not use operations, so the operationDesc will be null.  In this
        // case an anonymous AxisService with anoymouns AxisOperations for the supported
        // MEPs will be created; and it is that anonymous operation name which needs to
        // be specified
        QName operationName = getOperationNameToUse(request, ServiceClient.ANON_OUT_IN_OP);

        // TODO: Will the ServiceClient stick around on the InvocationContext
        // or will we need some other mechanism of creating this?
        // Try to create an OperationClient from the passed in ServiceClient
        InvocationContext ic = request.getInvocationContext();
        ServiceClient svcClient = ic.getServiceClient();
        OperationClient opClient = createOperationClient(svcClient, operationName);

        initOperationClient(opClient, request);

        // Setup the client so that it knows whether the underlying call to
        // Axis2 knows whether or not to start a listening port for an
        // asynchronous response.
        Boolean useAsyncMep = (Boolean)request.getProperties().get(Constants.USE_ASYNC_MEP);
        if ((useAsyncMep != null && useAsyncMep.booleanValue())
                || opClient.getOptions().isUseSeparateListener()) {
            configureAsyncListener(opClient, request.getAxisMessageContext());
        } else {
            if (log.isDebugEnabled()) {
                log.debug(
                        "Asynchronous message exchange not enabled.  The invocation will be synchronous.");
            }
        }


        CallbackFuture cbf = null;
        if (callback != null) {
            cbf = new CallbackFuture(ic, callback);
        } else {
            throw ExceptionFactory.makeWebServiceException(Messages.getMessage("ICErr4"));
        }

        opClient.setCallback(cbf);

        org.apache.axis2.context.MessageContext axisRequestMsgCtx = request.getAxisMessageContext();
        try {
            execute(opClient, false, axisRequestMsgCtx);
        } catch (AxisFault af) {
            if (log.isDebugEnabled()) {
                log.debug(axisRequestMsgCtx.getLogIDString() + " AxisFault received from client: " +
                        af.getMessage());
            }
            /*
             * Save the exception on the callback.  The client will learn about the error when they try to
             * retrieve the async results via the Response.get().  "Errors that occur during the invocation
             * are reported via an exception when the client attempts to retrieve the results of the operation."
             * -- JAXWS 4.3.3
             */

            /*
            * TODO:  This is the appropriate thing to do here since the thrown exception may occur before
            * we switch threads to the async thread.  But... what happens if we've already switched over
            * to the async thread?  So far, it appears that the exception gets set on the FutureTask
            * Concurrent object, and we never hit this scope.  This means that later, when the client
            * calls future.get(), no exception will be thrown despite what the spec says.  The client can,
            * however, retrieve errors via it's AsyncHandler.
            */
            cbf.onError(af);
        }

        return cbf.getFutureTask();
    }

    /*
    *  (non-Javadoc)
    * @see org.apache.axis2.jaxws.core.controller.InvocationController#invokeAsync(org.apache.axis2.jaxws.core.InvocationContext)
    */
    public Response doInvokeAsync(MessageContext request) {
        // We need the qname of the operation being invoked to know which 
        // AxisOperation the OperationClient should be based on.
        // Note that the OperationDesc is only set through use of the Proxy. Dispatch
        // clients do not use operations, so the operationDesc will be null.  In this
        // case an anonymous AxisService with anoymouns AxisOperations for the supported
        // MEPs will be created; and it is that anonymous operation name which needs to
        // be specified
        QName operationName = getOperationNameToUse(request, ServiceClient.ANON_OUT_IN_OP);

        // TODO: Will the ServiceClient stick around on the InvocationContext
        // or will we need some other mechanism of creating this?
        // Try to create an OperationClient from the passed in ServiceClient
        InvocationContext ic = request.getInvocationContext();
        ServiceClient svcClient = ic.getServiceClient();
        OperationClient opClient = createOperationClient(svcClient, operationName);

        initOperationClient(opClient, request);

        // Setup the client so that it knows whether the underlying call to
        // Axis2 knows whether or not to start a listening port for an
        // asynchronous response.
        Boolean useAsyncMep = (Boolean)request.getProperties().get(Constants.USE_ASYNC_MEP);
        if ((useAsyncMep != null && useAsyncMep.booleanValue())
                || opClient.getOptions().isUseSeparateListener()) {
            configureAsyncListener(opClient, request.getAxisMessageContext());
        } else {
            if (log.isDebugEnabled()) {
                log.debug(
                        "Asynchronous message exchange not enabled.  The invocation will be synchronous.");
            }
        }

        AsyncResponse resp = ic.getAsyncResponseListener();
        PollingFuture pf = new PollingFuture(ic);
        opClient.setCallback(pf);

        org.apache.axis2.context.MessageContext axisRequestMsgCtx = request.getAxisMessageContext();
        try {
            execute(opClient, false, axisRequestMsgCtx);
        } catch (AxisFault af) {
            if (log.isDebugEnabled()) {
                log.debug(axisRequestMsgCtx.getLogIDString() + " AxisFault received from client: " +
                        af.getMessage());
            }
            /*
             * Save the exception on the callback.  The client will learn about the error when they try to
             * retrieve the async results via the Response.get().  "Errors that occur during the invocation
             * are reported via an exception when the client attempts to retrieve the results of the operation."
             * -- JAXWS 4.3.3
             */
            pf.onError(af);
        }

        return resp;
    }

    /*
    * (non-Javadoc)
    * @see org.apache.axis2.jaxws.core.controller.InvocationController#prepareRequest(org.apache.axis2.jaxws.core.MessageContext)
    */
    protected void prepareRequest(MessageContext requestMsgCtx) {
        try {
            if (requestMsgCtx == null) {
                //throw an exception
            }

            org.apache.axis2.context.MessageContext axisRequestMsgCtx =
                    requestMsgCtx.getAxisMessageContext();

            // The MessageContext will contain a Message object with the
            // contents that need to be sent.  We need to get those contents
            // in a form that Axis2 can consume them, an AXIOM SOAPEnvelope.
            MessageUtils.putMessageOnMessageContext(
                    requestMsgCtx.getMessage(),  // JAX-WS Message
                    axisRequestMsgCtx // Axis 2 MessageContext
            );

            // For now, just take all of the properties that were in the 
            // JAX-WS MessageContext, and set them on the Axis2 MessageContext.
            axisRequestMsgCtx.setProperty(AbstractContext.COPY_PROPERTIES,
                                          Boolean.TRUE);
            Map props = axisRequestMsgCtx.getOptions().getProperties();
            props.putAll(requestMsgCtx.getProperties());

            axisRequestMsgCtx.getOptions().setProperties(props);
            if (log.isDebugEnabled()) {
                log.debug("Properties: " + axisRequestMsgCtx.getProperties().toString());
            }
        } catch (WebServiceException e) {
            throw ExceptionFactory
                    .makeWebServiceException(Messages.getMessage("prepareRequestFail"));
        } catch (AxisFault e) {
            throw ExceptionFactory
                    .makeWebServiceException(Messages.getMessage("prepareRequestFail"), e);
        }
    }

    /*
    * (non-Javadoc)
    * @see org.apache.axis2.jaxws.core.controller.InvocationController#prepareResponse(org.apache.axis2.jaxws.core.MessageContext)
    */
    protected void prepareResponse(MessageContext responseMsgCtx) {

    }
    
    private void initOperationClient(OperationClient opClient, MessageContext requestMsgCtx) {
        org.apache.axis2.context.MessageContext axisRequest = requestMsgCtx.getAxisMessageContext();
        setupProperties(requestMsgCtx, axisRequest.getOptions());

        if (opClient != null) {
            Options options = opClient.getOptions();
            
            // Get the target endpoint address and setup the TO endpoint 
            // reference.  This tells us where the request is going.
            if (options.getTo() == null) {
                String targetUrl = (String)requestMsgCtx.getProperties().get(
                        BindingProvider.ENDPOINT_ADDRESS_PROPERTY);
                EndpointReference toEPR = new EndpointReference(targetUrl);
                options.setTo(toEPR);
            }

            // Get the SOAP Action (if needed)
            String soapAction = ClientUtils.findSOAPAction(requestMsgCtx);
            options.setAction(soapAction);
            // get the timeout from the request message context options as it may have been
            // set by the user; if it was not set by the user we will just be setting the
            // timeout on the operation client to the default so it will not have a negative
            // effect; this logic is reliant on the fact the JAX-WS MessageContext is delegating
            // to the Axis2 Options object and not storing its own property bag
            long timeout = axisRequest.getOptions().getTimeOutInMilliSeconds();
            options.setTimeOutInMilliSeconds(timeout);

            // Use the OperationClient to send the request and put the contents
            // of the response in the response MessageContext.
            try {
                // Setting the ServiceContext will create the association between 
                // the OperationClient it's MessageContexts and the 
                // AxisService/AxisOperation that they are tied to.
                OperationContext opContext = opClient.getOperationContext();
                ServiceContext svcContext = opContext.getServiceContext();
                axisRequest.setServiceContext(svcContext);

                // Set the Axis2 request MessageContext
                opClient.addMessageContext(axisRequest);
            }
            catch (Exception e) {
                //TODO: Do something
            }
        }
    }

    /**
     * Use the provided ServiceClient instance to create an OperationClient identified by the
     * operation QName provided.
     *
     * @param sc
     * @param operation
     * @return
     */
    private OperationClient createOperationClient(ServiceClient sc, QName operation) {
        if (sc == null) {
            throw ExceptionFactory
                    .makeWebServiceException(Messages.getMessage("ICCreateOpClientErr1"));
        }
        if (operation == null) {
            throw ExceptionFactory
                    .makeWebServiceException(Messages.getMessage("ICCreateOpClientErr2"));
        }

        if (log.isDebugEnabled()) {
            log.debug("Creating OperationClient for operation: " + operation);
        }

        try {
            OperationClient client = sc.createClient(operation);
            return client;
        } catch (AxisFault e) {
            //TODO: NLS and ExceptionFactory
            throw ExceptionFactory.makeWebServiceException(e);
        }
    }

    private void configureAsyncListener(OperationClient client,
                                        org.apache.axis2.context.MessageContext mc) {
        if (log.isDebugEnabled()) {
            log.debug(
                    "Enabling asynchronous message exchange.  An asynchronous listener will be establish.");
        }

        client.getOptions().setUseSeparateListener(true);

        //FIXME: This has to be here so the ThreadContextMigrator can pick it up.
        //This should go away once AXIS2-978 is fixed.
        mc.getOptions().setUseSeparateListener(true);

        // Setup the response callback receiver to receive the async response
        // This logic is based on org.apache.axis2.client.ServiceClient.sendReceiveNonBlocking(...)
        AxisOperation op = client.getOperationContext().getAxisOperation();
        MessageReceiver messageReceiver = op.getMessageReceiver();
        if (messageReceiver == null || !(messageReceiver instanceof CallbackReceiver))
            op.setMessageReceiver(new CallbackReceiver());
    }

    /*
    * TODO: This is a first pass at filtering the properties that are set on the
    * RequestContext.  Right now it's called during the invoke, but needs to be
    * moved over to when the property is set.  This should not be in the path
    * of performance.
    */
    private void setupProperties(MessageContext mc, Options ops) {
        Map<String, Object> properties = mc.getProperties();
        for (Iterator<String> it = properties.keySet().iterator(); it.hasNext();) {
            String key = it.next();
            Object value = properties.get(key);
            ops.setProperty(key, value);
        }

        // Enable MTOM
        Message msg = mc.getMessage();
        if (msg.isMTOMEnabled()) {
            ops.setProperty(Configuration.ENABLE_MTOM, "true");
        }

        // Enable session management
        if (mc.isMaintainSession()) {
            ops.setManageSession(true);
        }

        // Check to see if BASIC_AUTH is enabled.  If so, make sure
        // the properties are setup correctly.
        if (properties.containsKey(BindingProvider.USERNAME_PROPERTY) &&
                properties.containsKey(BindingProvider.PASSWORD_PROPERTY)) {

            String userId = (String)properties.get(BindingProvider.USERNAME_PROPERTY);
            if (userId == null || userId == "") {
                throw ExceptionFactory
                        .makeWebServiceException(Messages.getMessage("checkUserName"));
            }

            String password = (String)properties.get(BindingProvider.PASSWORD_PROPERTY);
            if (password == null || password == "") {
                throw ExceptionFactory
                        .makeWebServiceException(Messages.getMessage("checkPassword"));
            }

            URL url = null;
            try {
                url = new URL((String)mc.getProperties()
                        .get(BindingProvider.ENDPOINT_ADDRESS_PROPERTY));
            }
            catch (MalformedURLException e) {
                throw ExceptionFactory.makeWebServiceException(e);
            }

            HttpTransportProperties.Authenticator basicAuthentication =
                    new HttpTransportProperties.Authenticator();
            basicAuthentication.setUsername(userId);
            basicAuthentication.setPassword(password);
            basicAuthentication.setHost(url.getHost());
            basicAuthentication.setPort(url.getPort());

            ops.setProperty(HTTPConstants.AUTHENTICATE, basicAuthentication);
        } else if ((!properties.containsKey(BindingProvider.USERNAME_PROPERTY) &&
                properties.containsKey(BindingProvider.PASSWORD_PROPERTY)) ||
                (properties.containsKey(BindingProvider.USERNAME_PROPERTY) &&
                        !properties.containsKey(BindingProvider.PASSWORD_PROPERTY))) {
            throw ExceptionFactory
                    .makeWebServiceException(Messages.getMessage("checkUsernameAndPassword"));
        }
    }

    private static QName getOperationNameToUse(MessageContext requestMsgCtx, QName defaultOpName) {
        // We need the qname of the operation being invoked to know which 
        // AxisOperation the OperationClient should be based on.
        // Note that the OperationDesc is only set through use of the Proxy. Dispatch
        // clients do not use operations, so the operationDesc will be null.  In this
        // case an anonymous AxisService with anonymous AxisOperations for the supported
        // MEPs will be created; and it is that anonymous operation name which needs to
        // be specified
        QName operationName = null;
        OperationDescription opDesc = requestMsgCtx.getOperationDescription();
        if (opDesc != null && opDesc.getAxisOperation() != null)
            operationName = opDesc.getName();
        else
            operationName = defaultOpName;
        return operationName;
    }

    /**
     * Executes the OperationClient
     *
     * @param opClient   - Fully configured OperationClient
     * @param block      - Indicates if blocking or non-blocking execute
     * @param msgContext - Axis2 MessageContext
     * @throws AxisFault - All exceptions are returned as AxisFaults
     */
    private void execute(OperationClient opClient,
                         boolean block,
                         org.apache.axis2.context.MessageContext msgContext) throws AxisFault {
        try {
            // Pre-Execute logging and setup
            preExecute(opClient, block, msgContext);

            // Invoke the OperationClient
            opClient.execute(block);
        } catch (Throwable e) {
            // Catch all Throwable (including runtime exceptions and Errors) and
            // throw as AxisFault.
            // Since e could be a Throwable (or Error) instead of an Exception, we'll have to wrap it:
            throw AxisFault.makeFault(ExceptionFactory.makeWebServiceException(e));
        } finally {
            // Post-Execute logging and setup
            postExecute(opClient, block, msgContext);
        }
    }

    /**
     * Called by execute(OperationClient) to perform pre-execute tasks.
     *
     * @param opClient
     * @param block      - Indicates if blocking or non-blocking execute
     * @param msgContext - Axis2 MessageContext
     */
    private void preExecute(OperationClient opClient,
                            boolean block,
                            org.apache.axis2.context.MessageContext msgContext) throws AxisFault {
        // This assumes that we are on the ultimate execution thread

        ThreadContextMigratorUtil
                .performMigrationToContext(Constants.THREAD_CONTEXT_MIGRATOR_LIST_ID, msgContext);

        if (log.isDebugEnabled()) {
            log.debug("Start OperationClient.execute(" + block + ")");
        }
    }

    /**
     * Called by execute(OperationClient) to perform post-execute tasks.  Should be a mirror of
     * preExecute
     *
     * @param opClient
     * @param block      - Indicates if blocking or non-blocking execute
     * @param msgContext - Axis2 MessageContext
     */
    private void postExecute(OperationClient opClient,
                             boolean block,
                             org.apache.axis2.context.MessageContext msgContext) {
        if (log.isDebugEnabled()) {
            log.debug("End OperationClient.execute(" + block + ")");
        }

        /* TODO Currently this check causes SOAPMessageProviderTests to fail.
        if (log.isDebugEnabled()) {
            // Check for exploded OMSourcedElement
            OMElement bodyElement = null;
            if (msgContext.getEnvelope() != null &&
                msgContext.getEnvelope().getBody() != null) {
                bodyElement = msgContext.getEnvelope().getBody().getFirstElement();     
            }
            
            boolean expanded = false;
            if (bodyElement != null && bodyElement instanceof OMSourcedElementImpl) {
                expanded = ((OMSourcedElementImpl)bodyElement).isExpanded();
            }
            // An exploded xml block may indicate a performance problem.  
            // In general an xml block should remain unexploded unless there is an
            // outbound handler that touches the block.
            if (expanded) {
                log.debug("Developer Debug: Found an expanded xml block:" + bodyElement.getNamespace());
            }
        }
        */
        // Cleanup context
        ThreadContextMigratorUtil
                .performContextCleanup(Constants.THREAD_CONTEXT_MIGRATOR_LIST_ID, msgContext);
    }
}
