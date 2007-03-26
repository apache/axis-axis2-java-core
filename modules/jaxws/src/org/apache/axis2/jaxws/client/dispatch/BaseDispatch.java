/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 *      
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.axis2.jaxws.client.dispatch;

import java.util.concurrent.Executor;
import java.util.concurrent.Future;

import javax.xml.transform.dom.DOMSource;
import javax.xml.ws.AsyncHandler;
import javax.xml.ws.Binding;
import javax.xml.ws.ProtocolException;
import javax.xml.ws.Response;
import javax.xml.ws.Service.Mode;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.http.HTTPBinding;
import javax.xml.ws.soap.SOAPBinding;

import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.jaxws.BindingProvider;
import org.apache.axis2.jaxws.ExceptionFactory;
import org.apache.axis2.jaxws.client.async.AsyncResponse;
import org.apache.axis2.jaxws.core.InvocationContext;
import org.apache.axis2.jaxws.core.InvocationContextFactory;
import org.apache.axis2.jaxws.core.MessageContext;
import org.apache.axis2.jaxws.core.controller.AxisInvocationController;
import org.apache.axis2.jaxws.core.controller.InvocationController;
import org.apache.axis2.jaxws.description.EndpointDescription;
import org.apache.axis2.jaxws.marshaller.impl.alt.MethodMarshallerUtils;
import org.apache.axis2.jaxws.message.Message;
import org.apache.axis2.jaxws.message.XMLFault;
import org.apache.axis2.jaxws.spi.Constants;
import org.apache.axis2.jaxws.spi.ServiceDelegate;
import org.apache.axis2.jaxws.spi.migrator.ApplicationContextMigratorUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public abstract class BaseDispatch<T> extends BindingProvider 
    implements javax.xml.ws.Dispatch {

    private Log log = LogFactory.getLog(BaseDispatch.class);
    
    protected InvocationController ic;
    protected ServiceClient serviceClient;
    protected Mode mode;
    
    protected BaseDispatch(ServiceDelegate svcDelgate, EndpointDescription epDesc) {
        super(svcDelgate, epDesc);
        
        ic = new AxisInvocationController();
    }
    
    /**
     * Take the input object and turn it into an OMElement so that it can
     * be sent.
     * 
     * @param value
     * @return
     */
    protected abstract Message createMessageFromValue(Object value);
    
    /**
     * Given a message, return the business object based on the requestor's
     * required format (PAYLOAD vs. MESSAGE) and datatype.
     * 
     * @param message
     * @return
     */
    protected abstract Object getValueFromMessage(Message message);
    
    /**
     * Creates an instance of the AsyncListener that is to be used for waiting
     * for async responses.
     * 
     * @return a configured AsyncListener instance
     */
    protected abstract AsyncResponse createAsyncResponseListener();
    
    public Object invoke(Object obj) throws WebServiceException {
        
        // Catch all exceptions and rethrow an appropriate WebService Exception
        try {
            if (log.isDebugEnabled()) { 
                log.debug("Entered synchronous invocation: BaseDispatch.invoke()");
            }
            
            // Create the InvocationContext instance for this request/response flow.
            InvocationContext invocationContext = InvocationContextFactory.createInvocationContext(null);
            invocationContext.setServiceClient(serviceClient);
            
            // Create the MessageContext to hold the actual request message and its
            // associated properties
            MessageContext requestMsgCtx = new MessageContext();
            requestMsgCtx.setServiceDescription(getEndpointDescription().getServiceDescription());
            invocationContext.setRequestMessageContext(requestMsgCtx);
            
            Message requestMsg = null;
            if (isValidInvocationParam(obj)) {
                requestMsg = createMessageFromValue(obj);
            }
            else {
                throw ExceptionFactory.makeWebServiceException("dispatchInvalidParam");
            }
            
            setupMessageProperties(requestMsg);
            requestMsgCtx.setMessage(requestMsg);            
            
            // Migrate the properties from the client request context bag to
            // the request MessageContext.
            ApplicationContextMigratorUtil.performMigrationToMessageContext(
                    Constants.APPLICATION_CONTEXT_MIGRATOR_LIST_ID, 
                    getRequestContext(), requestMsgCtx);
            
            // Send the request using the InvocationController
            ic.invoke(invocationContext);
            
            MessageContext responseMsgCtx = invocationContext.getResponseMessageContext();
            responseMsgCtx.setServiceDescription(requestMsgCtx.getServiceDescription());
            
            // Migrate the properties from the response MessageContext back
            // to the client response context bag.
            ApplicationContextMigratorUtil.performMigrationFromMessageContext(
                    Constants.APPLICATION_CONTEXT_MIGRATOR_LIST_ID, 
                    getResponseContext(), responseMsgCtx);
            
            if (hasFaultResponse(responseMsgCtx)) {
                WebServiceException wse = BaseDispatch.getFaultResponse(responseMsgCtx);
                throw wse;
            }

            Message responseMsg = responseMsgCtx.getMessage();
            Object returnObj = getValueFromMessage(responseMsg);
            
            //Check to see if we need to maintain session state
            if (requestMsgCtx.isMaintainSession()) {
                //TODO: Need to figure out a cleaner way to make this call. 
                setupSessionContext(invocationContext.getServiceClient().getServiceContext().getProperties());
            }
            
            if (log.isDebugEnabled()) {
                log.debug("Synchronous invocation completed: BaseDispatch.invoke()");
            }
            
            return returnObj;
        } catch (WebServiceException e) {
            throw e; 
        } catch (Exception e) {
            // All exceptions are caught and rethrown as a WebServiceException
            throw ExceptionFactory.makeWebServiceException(e);
        }
    }
    
    public void invokeOneWay(Object obj) throws WebServiceException{
        
        // All exceptions are caught and rethrown as a WebServiceException
        try {
            if (log.isDebugEnabled()) { 
                log.debug("Entered one-way invocation: BaseDispatch.invokeOneWay()");
            }
            
            // Create the InvocationContext instance for this request/response flow.
            InvocationContext invocationContext = InvocationContextFactory.createInvocationContext(null);
            invocationContext.setServiceClient(serviceClient);
            
            // Create the MessageContext to hold the actual request message and its
            // associated properties
            MessageContext requestMsgCtx = new MessageContext();
            requestMsgCtx.setServiceDescription(getEndpointDescription().getServiceDescription());
            invocationContext.setRequestMessageContext(requestMsgCtx);
            
            Message requestMsg = null;
            if (isValidInvocationParam(obj)) {
                requestMsg = createMessageFromValue(obj);
            }
            else {
                throw ExceptionFactory.makeWebServiceException("dispatchInvalidParam");
            }
            
            setupMessageProperties(requestMsg);
            requestMsgCtx.setMessage(requestMsg);
            
            // Migrate the properties from the client request context bag to
            // the request MessageContext.
            ApplicationContextMigratorUtil.performMigrationToMessageContext(
                    Constants.APPLICATION_CONTEXT_MIGRATOR_LIST_ID, 
                    getRequestContext(), requestMsgCtx);
            
            // Send the request using the InvocationController
            ic.invokeOneWay(invocationContext);
            
            //Check to see if we need to maintain session state
            if (requestMsgCtx.isMaintainSession()) {
                //TODO: Need to figure out a cleaner way to make this call. 
                setupSessionContext(invocationContext.getServiceClient().getServiceContext().getProperties());
            }
            
            if (log.isDebugEnabled()) {
                log.debug("One-way invocation completed: BaseDispatch.invokeOneWay()");
            }
            
            return;
        } catch (WebServiceException e) {
            throw e; 
        } catch (Exception e) {
            // All exceptions are caught and rethrown as a WebServiceException
            throw ExceptionFactory.makeWebServiceException(e);
        }
    }
   
    public Future<?> invokeAsync(Object obj, AsyncHandler asynchandler) throws WebServiceException {
        
        // All exceptions are caught and rethrown as a WebServiceException
        try {
            if (log.isDebugEnabled()) { 
                log.debug("Entered asynchronous (callback) invocation: BaseDispatch.invokeAsync()");
            }
            
            // Create the InvocationContext instance for this request/response flow.
            InvocationContext invocationContext = InvocationContextFactory.createInvocationContext(null);
            invocationContext.setServiceClient(serviceClient);
            
            // Create the MessageContext to hold the actual request message and its
            // associated properties
            MessageContext requestMsgCtx = new MessageContext();
            requestMsgCtx.setServiceDescription(getEndpointDescription().getServiceDescription());
            invocationContext.setRequestMessageContext(requestMsgCtx);
            
            Message requestMsg = null;
            if (isValidInvocationParam(obj)) {
                requestMsg = createMessageFromValue(obj);
            }
            else {
                throw ExceptionFactory.makeWebServiceException("dispatchInvalidParam");
            }
            
            setupMessageProperties(requestMsg);
            requestMsgCtx.setMessage(requestMsg);
            
            // Migrate the properties from the client request context bag to
            // the request MessageContext.
            ApplicationContextMigratorUtil.performMigrationToMessageContext(
                    Constants.APPLICATION_CONTEXT_MIGRATOR_LIST_ID, 
                    getRequestContext(), requestMsgCtx);
            
            // Setup the Executor that will be used to drive async responses back to 
            // the client.
            // FIXME: We shouldn't be getting this from the ServiceDelegate, rather each 
            // Dispatch object should have it's own.
            Executor e = serviceDelegate.getExecutor();
            invocationContext.setExecutor(e);
            
            // Create the AsyncListener that is to be used by the InvocationController.
            AsyncResponse listener = createAsyncResponseListener();
            invocationContext.setAsyncResponseListener(listener);
            
            // Send the request using the InvocationController
            Future<?> asyncResponse = ic.invokeAsync(invocationContext, asynchandler);
            
            //Check to see if we need to maintain session state
            if (requestMsgCtx.isMaintainSession()) {
                //TODO: Need to figure out a cleaner way to make this call. 
                setupSessionContext(invocationContext.getServiceClient().getServiceContext().getProperties());
            }
            
            if (log.isDebugEnabled()) {
                log.debug("Asynchronous (callback) invocation sent: BaseDispatch.invokeAsync()");
            }
            
            return asyncResponse;
        } catch (WebServiceException e) {
            throw e; 
        } catch (Exception e) {
            // All exceptions are caught and rethrown as a WebServiceException
            throw ExceptionFactory.makeWebServiceException(e);
        }
    }
  
    public Response invokeAsync(Object obj)throws WebServiceException{
        
        // All exceptions are caught and rethrown as a WebServiceException
        try {
            if (log.isDebugEnabled()) { 
                log.debug("Entered asynchronous (polling) invocation: BaseDispatch.invokeAsync()");
            }
            
            // Create the InvocationContext instance for this request/response flow.
            InvocationContext invocationContext = InvocationContextFactory.createInvocationContext(null);
            invocationContext.setServiceClient(serviceClient);
            
            // Create the MessageContext to hold the actual request message and its
            // associated properties
            MessageContext requestMsgCtx = new MessageContext();
            requestMsgCtx.setServiceDescription(getEndpointDescription().getServiceDescription());
            invocationContext.setRequestMessageContext(requestMsgCtx);
            
            Message requestMsg = null;
            if (isValidInvocationParam(obj)) {
                requestMsg = createMessageFromValue(obj);
            }
            else {
                throw ExceptionFactory.makeWebServiceException("dispatchInvalidParam");
            }
            
            setupMessageProperties(requestMsg);
            requestMsgCtx.setMessage(requestMsg);

            // Migrate the properties from the client request context bag to
            // the request MessageContext.
            ApplicationContextMigratorUtil.performMigrationToMessageContext(
                    Constants.APPLICATION_CONTEXT_MIGRATOR_LIST_ID, 
                    getRequestContext(), requestMsgCtx);
            
            // Setup the Executor that will be used to drive async responses back to 
            // the client.
            // FIXME: We shouldn't be getting this from the ServiceDelegate, rather each 
            // Dispatch object should have it's own.
            Executor e = serviceDelegate.getExecutor();
            invocationContext.setExecutor(e);
            
            // Create the AsyncListener that is to be used by the InvocationController.
            AsyncResponse listener = createAsyncResponseListener();
            invocationContext.setAsyncResponseListener(listener);
            
            // Send the request using the InvocationController
            Response asyncResponse = ic.invokeAsync(invocationContext);
            
            //Check to see if we need to maintain session state
            if (requestMsgCtx.isMaintainSession()) {
                //TODO: Need to figure out a cleaner way to make this call. 
                setupSessionContext(invocationContext.getServiceClient().getServiceContext().getProperties());
            }
            
            if (log.isDebugEnabled()) {
                log.debug("Asynchronous (polling) invocation sent: BaseDispatch.invokeAsync()");
            }
            
            return asyncResponse;
        } catch (WebServiceException e) {
            throw e; 
        } catch (Exception e) {
            // All exceptions are caught and rethrown as a WebServiceException
            throw ExceptionFactory.makeWebServiceException(e);
        }
    }
    
    public void setServiceClient(ServiceClient sc) {
        serviceClient = sc;
    }
    
    public Mode getMode() {
        return mode;
    }
    
    public void setMode(Mode m) {
        mode = m;
    }
    
    /**
     * Returns the fault that is contained within the MessageContext for an invocation.  
     * If no fault exists, null will be returned.
     * 
     * @param msgCtx
     * @return
     */
    public static WebServiceException getFaultResponse(MessageContext msgCtx) {
        Message msg = msgCtx.getMessage();        
        if (msg != null && msg.isFault()) {
            //XMLFault fault = msg.getXMLFault();
            // 4.3.2 conformance bullet 1 requires a ProtocolException here
            ProtocolException pe = MethodMarshallerUtils.createSystemException(msg.getXMLFault(), msg);
            return  pe;
        }
        else if (msgCtx.getLocalException() != null) {
            // use the factory, it'll throw the right thing:
            return ExceptionFactory.makeWebServiceException(msgCtx.getLocalException());
        }
        
        return null;
    }
    
    /**
     * Returns a boolean indicating whether or not the MessageContext contained a fault.
     * 
     * @param msgCtx
     * @return
     */
    public boolean hasFaultResponse(MessageContext msgCtx) {
        if (msgCtx.getMessage() != null && msgCtx.getMessage().isFault())
            return true;
        else if (msgCtx.getLocalException() != null)
            return true;
        else 
            return false;
    }

    /*
     * Configure any properties that will be needed on the Message
     */
    private void setupMessageProperties(Message msg) {
        // If the user has enabled MTOM on the SOAPBinding, we need
        // to make sure that gets pushed to the Message object.
        Binding binding = getBinding();
        if (binding != null && binding instanceof SOAPBinding) {
            SOAPBinding soapBinding = (SOAPBinding) binding;
            if (soapBinding.isMTOMEnabled())
                msg.setMTOMEnabled(true);
        }
        
        // Check if the user enabled MTOM using the SOAP binding 
        // properties for MTOM
        String bindingID = endpointDesc.getClientBindingID();
        if((bindingID.equalsIgnoreCase(SOAPBinding.SOAP11HTTP_MTOM_BINDING) ||
        	bindingID.equalsIgnoreCase(SOAPBinding.SOAP12HTTP_MTOM_BINDING)) &&
        	!msg.isMTOMEnabled()){
        	msg.setMTOMEnabled(true);
        }
    }
    
    /*
     * Checks to see if the parameter for the invocation is valid
     * given the scenario that the client is operating in.  There are 
     * some cases when nulls are allowed and others where it is 
     * an error.
     */
    private boolean isValidInvocationParam(Object object){
        String bindingId = endpointDesc.getClientBindingID();
        
        // If no bindingId was found, use the default.
        if (bindingId == null) {
            bindingId = SOAPBinding.SOAP11HTTP_BINDING;
        }
        
        // If it's not an HTTP_BINDING, then we can allow for null params,  
        // but only in PAYLOAD mode per JAX-WS Section 4.3.2.
        if (!bindingId.equals(HTTPBinding.HTTP_BINDING)) { 
            if (mode.equals(Mode.MESSAGE) && object == null) {
                throw ExceptionFactory.makeWebServiceException("dispatchNullParamMessageMode");
            }
        }
        else {
            // In all cases (PAYLOAD and MESSAGE) we must throw a WebServiceException
            // if the parameter is null.
            if (object == null) {
                throw ExceptionFactory.makeWebServiceException("dispatchNullParamHttpBinding");
            }
        }
        
        if (object instanceof DOMSource) {
            DOMSource ds = (DOMSource) object;
            if (ds.getNode() == null && ds.getSystemId() == null) {
                throw ExceptionFactory.makeWebServiceException("dispatchBadDOMSource");
            }
        }
        
        // If we've gotten this far, then all is good.
        return true;
    }
}
