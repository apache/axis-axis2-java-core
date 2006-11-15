/*
 * Copyright 2006 The Apache Software Foundation.
 * Copyright 2006 International Business Machines Corp.
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
package org.apache.axis2.jaxws.client;

import java.util.concurrent.Executor;
import java.util.concurrent.Future;

import javax.xml.ws.AsyncHandler;
import javax.xml.ws.Response;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.Service.Mode;
import javax.xml.ws.soap.SOAPBinding;

import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.jaxws.AxisController;
import org.apache.axis2.jaxws.BindingProvider;
import org.apache.axis2.jaxws.ExceptionFactory;
import org.apache.axis2.jaxws.core.InvocationContext;
import org.apache.axis2.jaxws.core.InvocationContextFactory;
import org.apache.axis2.jaxws.core.MessageContext;
import org.apache.axis2.jaxws.core.controller.AxisInvocationController;
import org.apache.axis2.jaxws.core.controller.InvocationController;
import org.apache.axis2.jaxws.handler.PortData;
import org.apache.axis2.jaxws.impl.AsyncListener;
import org.apache.axis2.jaxws.message.Message;
import org.apache.axis2.jaxws.spi.ServiceDelegate;
import org.apache.axis2.jaxws.util.Constants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public abstract class BaseDispatch<T> extends BindingProvider 
    implements javax.xml.ws.Dispatch {

    private Log log = LogFactory.getLog(BaseDispatch.class);
    
    //FIXME: Remove the AxisController completely and replace with InvocationController
    protected AxisController axisController = null;
    
    protected InvocationController ic;
    protected ServiceDelegate serviceDelegate;
    protected ServiceClient serviceClient;
    protected Mode mode;
    protected PortData port;
    
    protected BaseDispatch(PortData p) {
        super();
        
        port = p;
        ic = new AxisInvocationController();
        setRequestContext();
    }
    
    protected BaseDispatch(AxisController ac) {
        super();
        
        //FIXME: Remove this when we remove the AxisController
        axisController = ac;
        
        ic = new AxisInvocationController();
        setRequestContext();
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
    protected abstract AsyncListener createAsyncListener();
    
    public Object invoke(Object obj) throws WebServiceException {
        if (log.isDebugEnabled()) { 
            log.debug("Entered synchronous invocation: BaseDispatch.invoke()");
        }
        
        //Check for valid invocation parameter
        obj = validateInvocationParam(obj);
        
        // Create the InvocationContext instance for this request/response flow.
        InvocationContext invocationContext = InvocationContextFactory.createInvocationContext(null);
        invocationContext.setServiceClient(serviceClient);
        
        // Create the MessageContext to hold the actual request message and its
        // associated properties
        MessageContext requestMsgCtx = new MessageContext();
        invocationContext.setRequestMessageContext(requestMsgCtx);
        
        Message requestMsg = createMessageFromValue(obj);
        setupMessageProperties(requestMsg);
        requestMsgCtx.setMessage(requestMsg);
        
        // Copy the properties from the request context into the MessageContext
        requestMsgCtx.getProperties().putAll(requestContext);
        
        // Send the request using the InvocationController
        ic.invoke(invocationContext);
        
        MessageContext responseMsgCtx = invocationContext.getResponseMessageContext();
        
        //FIXME: This is temporary until more of the Message model is available
        Message responseMsg = responseMsgCtx.getMessage();
        Object returnObj = getValueFromMessage(responseMsg);
        
        if (log.isDebugEnabled()) {
            log.debug("Synchronous invocation completed: BaseDispatch.invoke()");
        }
        
        return returnObj;
    }
    
    public void invokeOneWay(Object obj) throws WebServiceException{
        if (log.isDebugEnabled()) { 
            log.debug("Entered one-way invocation: BaseDispatch.invokeOneWay()");
        }
        
        //Check for valid invocation parameter
        obj = validateInvocationParam(obj);
       
        // Create the InvocationContext instance for this request/response flow.
        InvocationContext invocationContext = InvocationContextFactory.createInvocationContext(null);
        invocationContext.setServiceClient(serviceClient);
       
        // Create the MessageContext to hold the actual request message and its
        // associated properties
        MessageContext requestMsgCtx = new MessageContext();
        invocationContext.setRequestMessageContext(requestMsgCtx);
       
        Message requestMsg = createMessageFromValue(obj);
        setupMessageProperties(requestMsg);
        requestMsgCtx.setMessage(requestMsg);
       
        // Copy the properties from the request context into the MessageContext
        requestMsgCtx.getProperties().putAll(requestContext);
       
        // Send the request using the InvocationController
        ic.invokeOneWay(invocationContext);
       
        if (log.isDebugEnabled()) {
            log.debug("One-way invocation completed: BaseDispatch.invokeOneWay()");
        }
       
        return;
    }
   
    public Future<?> invokeAsync(Object obj, AsyncHandler asynchandler) throws WebServiceException {
        if (log.isDebugEnabled()) { 
            log.debug("Entered asynchronous (callback) invocation: BaseDispatch.invokeAsync()");
        }
        
        //Check for valid invocation parameter
        obj = validateInvocationParam(obj);
        
        // Create the InvocationContext instance for this request/response flow.
        InvocationContext invocationContext = InvocationContextFactory.createInvocationContext(null);
        invocationContext.setServiceClient(serviceClient);
        
        // Create the MessageContext to hold the actual request message and its
        // associated properties
        MessageContext requestMsgCtx = new MessageContext();
        invocationContext.setRequestMessageContext(requestMsgCtx);
        
        Message requestMsg = createMessageFromValue(obj);
        setupMessageProperties(requestMsg);
        requestMsgCtx.setMessage(requestMsg);
        
        // Copy the properties from the request context into the MessageContext
        requestMsgCtx.getProperties().putAll(requestContext);

        // Setup the Executor that will be used to drive async responses back to 
        // the client.
        // FIXME: We shouldn't be getting this from the ServiceDelegate, rather each 
        // Dispatch object should have it's own.
        Executor e = serviceDelegate.getExecutor();
        invocationContext.setExecutor(e);
        
        // Create the AsyncListener that is to be used by the InvocationController.
        AsyncListener listener = createAsyncListener();
        invocationContext.setAsyncListener(listener);
        
        // Send the request using the InvocationController
        Future<?> asyncResponse = ic.invokeAsync(invocationContext, asynchandler);
        
        if (log.isDebugEnabled()) {
            log.debug("Asynchronous (callback) invocation sent: BaseDispatch.invokeAsync()");
        }
        
        return asyncResponse;
    }
  
    public Response invokeAsync(Object obj)throws WebServiceException{
        throw new UnsupportedOperationException("Async (polling) invocations are not yet supported.");
    }
    
    //FIXME: This needs to be moved up to the BindingProvider and should actually
    //be called "initRequestContext()" or something like that.
    protected void setRequestContext(){
        String endPointAddress = port.getEndpointAddress();
        //WSDLWrapper wsdl =  axisController.getWSDLContext();
        //QName serviceName = axisController.getServiceName();
        //QName portName = axisController.getPortName();
        
        
        if(endPointAddress != null && !"".equals(endPointAddress)){
            getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, endPointAddress);
        }
        //else if(wsdl != null){
        //    String soapAddress = wsdl.getSOAPAddress(serviceName, portName);
        //    getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, soapAddress);
        //}
        
        //if(wsdl != null){
        //    String soapAction = wsdl.getSOAPAction(serviceName, portName);
        //    getRequestContext().put(BindingProvider.SOAPACTION_URI_PROPERTY, soapAction);
        //}
    }
    
    public ServiceDelegate getServiceDelegate() {
        return serviceDelegate;
    }
    
    public void setServiceDelegate(ServiceDelegate sd) {
        serviceDelegate = sd;
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

    public PortData getPort() {
        return port;
    }
    
    /*
     * Configure any properties that will be needed on the Message
     */
    private void setupMessageProperties(Message msg) {
        // If the user has enabled MTOM on the SOAPBinding, we need
        // to make sure that gets pushed to the Message object.
        if (binding != null && binding instanceof SOAPBinding) {
            SOAPBinding soapBinding = (SOAPBinding) binding;
            if (soapBinding.isMTOMEnabled())
                msg.setMTOMEnabled(true);
        }
    }
    
    /**
     * Validate invocation parameter value.
     * @param object
     * @return object
     */
    private Object validateInvocationParam(Object object){
    	Object obj = object;
    	String bindingId = port.getBindingID();
    	
    	try {
    		if(bindingId.equalsIgnoreCase(SOAPBinding.SOAP11HTTP_BINDING) ||
    		   bindingId.equalsIgnoreCase(SOAPBinding.SOAP11HTTP_MTOM_BINDING)){
    			if(mode.toString().equalsIgnoreCase(Mode.PAYLOAD.toString()) && object == null){
    				//TODO Per JAXWS 2.0 Specification in Section 4.3.2, may need to send a soap
    				//     message with empty body. For now, implementation will through a
    				//     NullPointerException wrapped in WebServiceException
    				throw new NullPointerException("Error: Dispatch invocation value is NULL");
    			}
    		}
    		else if(object == null){
    			throw new NullPointerException("Error: Dispatch invocation value is NULL");
    		}
    	}
    	catch(NullPointerException npe){
    		throw ExceptionFactory.makeWebServiceException(npe);
    	}
    	catch(Exception e){
    		throw ExceptionFactory.makeWebServiceException(e);
    	}
    	
    	return obj;
    }
}
