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
package org.apache.axis2.jaxws.server.dispatcher;

import org.apache.axis2.jaxws.ExceptionFactory;
import org.apache.axis2.jaxws.context.utils.ContextUtils;
import org.apache.axis2.jaxws.core.MessageContext;
import org.apache.axis2.jaxws.core.util.MessageContextUtils;
import org.apache.axis2.jaxws.description.EndpointDescription;
import org.apache.axis2.jaxws.description.EndpointInterfaceDescription;
import org.apache.axis2.jaxws.description.OperationDescription;
import org.apache.axis2.jaxws.i18n.Messages;
import org.apache.axis2.jaxws.marshaller.MethodMarshaller;
import org.apache.axis2.jaxws.marshaller.factory.MethodMarshallerFactory;
import org.apache.axis2.jaxws.message.Message;
import org.apache.axis2.jaxws.message.Protocol;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.ws.soap.SOAPBinding;
import java.lang.reflect.Method;

/**
 * The JavaBeanDispatcher is used to manage creating an instance of a JAX-WS service implementation
 * bean and dispatching the inbound request to that instance.
 */
public class JavaBeanDispatcher extends JavaDispatcher {

    private static final Log log = LogFactory.getLog(JavaBeanDispatcher.class);

    private EndpointDescription endpointDesc = null;

    public JavaBeanDispatcher(Class implClass, Object serviceInstance) {
        super(implClass, serviceInstance);
    }

    /*
    * (non-Javadoc)
    * @see org.apache.axis2.jaxws.server.EndpointDispatcher#invoke(org.apache.axis2.jaxws.core.MessageContext)
    */
    public MessageContext invoke(MessageContext mc) throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("Preparing to invoke service endpoint implementation " +
                    "class: " + serviceImplClass.getName());
        }

        initialize(mc);
        OperationDescription operationDesc =
                getOperationDescription(mc); //mc.getOperationDescription();
        //Set SOAP Operation Related properties in SOAPMessageContext.
        ContextUtils.addWSDLProperties(mc);
        Protocol requestProtocol = mc.getMessage().getProtocol();
        MethodMarshaller methodMarshaller =
                getMethodMarshaller(mc.getMessage().getProtocol(), mc.getOperationDescription());
        Object[] methodInputParams =
                methodMarshaller.demarshalRequest(mc.getMessage(), mc.getOperationDescription());
        Method target = getJavaMethod(mc, serviceImplClass);
        if (log.isDebugEnabled()) {
            // At this point, the OpDesc includes everything we know, including the actual method
            // on the service impl we will delegate to; it was set by getJavaMethod(...) above.
            log.debug("JavaBeanDispatcher about to invoke using OperationDesc: " +
                    operationDesc.toString());
        }

        //At this point, we have the method that is going to be invoked and
        //the parameter data to invoke it with, so we use the instance and 
        //do the invoke.
        //Passing method input params to grab holder values, if any.
        boolean faultThrown = false;
        Throwable fault = null;
        Object response = null;
        try {
            response = invokeService(mc, target, serviceInstance, methodInputParams);
        } catch (Exception e) {
            faultThrown = true;
            fault = e;
            if (log.isDebugEnabled()) {
                log.debug("Exception invoking a method of " +
                        serviceImplClass.toString() + " of instance " +
                        serviceInstance.toString());
                log.debug("Exception type thrown: " + e.getClass().getName());
                log.debug("Method = " + target.toGenericString());
                for (int i = 0; i < methodInputParams.length; i++) {
                    String value = (methodInputParams[i] == null) ? "null" :
                            methodInputParams[i].getClass().toString();
                    log.debug(" Argument[" + i + "] is " + value);
                }
            }
        }

        Message message = null;
        if (operationDesc.isOneWay()) {
            // If the operation is one-way, then we can just return null because
            // we cannot create a MessageContext for one-way responses.
            return null;
        } else if (faultThrown) {
            message = methodMarshaller.marshalFaultResponse(fault, mc.getOperationDescription(),
                                                            requestProtocol); // Send the response using the same protocol as the request
        } else if (target.getReturnType().getName().equals("void")) {
            message = methodMarshaller
                    .marshalResponse(null, methodInputParams, mc.getOperationDescription(),
                                     requestProtocol); // Send the response using the same protocol as the request
        } else {
            message = methodMarshaller
                    .marshalResponse(response, methodInputParams, mc.getOperationDescription(),
                                     requestProtocol); // Send the response using the same protocol as the request
        }

        MessageContext responseMsgCtx = null;
        if (faultThrown) {
            responseMsgCtx = MessageContextUtils.createFaultMessageContext(mc);
            responseMsgCtx.setMessage(message);
        } else {
            responseMsgCtx = MessageContextUtils.createResponseMessageContext(mc);
            responseMsgCtx.setMessage(message);
        }

        //Enable MTOM if necessary
        EndpointInterfaceDescription epInterfaceDesc =
                operationDesc.getEndpointInterfaceDescription();
        EndpointDescription epDesc = epInterfaceDesc.getEndpointDescription();

        String bindingType = epDesc.getBindingType();
        if (bindingType != null) {
            if (bindingType.equals(SOAPBinding.SOAP11HTTP_MTOM_BINDING) ||
                    bindingType.equals(SOAPBinding.SOAP12HTTP_MTOM_BINDING)) {
                message.setMTOMEnabled(true);
            }
        }

        return responseMsgCtx;
    }

    protected Object invokeService(MessageContext ctx,
                                   Method method, 
                                   Object obj,
                                   Object args[]) throws Exception {
        return method.invoke(obj, args);
    }
    
    private void initialize(MessageContext mc) {
        mc.setOperationName(mc.getAxisMessageContext().getAxisOperation().getName());
        endpointDesc = mc.getEndpointDescription();
        mc.setOperationDescription(getOperationDescription(mc));
        String bindingType = endpointDesc.getBindingType();
        if (bindingType != null) {
            if (bindingType.equals(SOAPBinding.SOAP11HTTP_MTOM_BINDING)
                    || bindingType.equals(SOAPBinding.SOAP12HTTP_MTOM_BINDING)) {
                mc.getMessage().setMTOMEnabled(true);
            }
        }
    }

    /*
     * Gets the OperationDescription associated with the request that is currently
     * being processed.
     * 
     *  Note that this is not done in the EndpointController since operations are only relevant
     *  to Endpoint-based implementation (i.e. not to Proxy-based ones)s
     */
    private OperationDescription getOperationDescription(MessageContext mc) {
        EndpointDescription ed = mc.getEndpointDescription();
        EndpointInterfaceDescription eid = ed.getEndpointInterfaceDescription();

        OperationDescription[] ops = eid.getDispatchableOperation(mc.getOperationName());
        // TODO: Implement signature matching.  Currently only matching on the wsdl:OperationName is supported.
        //       That means that overloading of wsdl operations is not supported (although that's not supported in 
        //       WSDL 1.1 anyway).
        if (ops == null || ops.length == 0) {
            // TODO: RAS & NLS
            throw ExceptionFactory.makeWebServiceException(
                    "No operation found.  WSDL Operation name: " + mc.getOperationName());
        }
        if (ops.length > 1) {
            // TODO: RAS & NLS
            throw ExceptionFactory.makeWebServiceException(
                    "More than one operation found. Overloaded WSDL operations are not supported.  WSDL Operation name: " +
                            mc.getOperationName());
        }
        OperationDescription op = ops[0];
        if (log.isDebugEnabled()) {
            log.debug("wsdl operation: " + op.getName());
            log.debug("   java method: " + op.getJavaMethodName());
        }

        return op;
    }

    private MethodMarshaller getMethodMarshaller(Protocol protocol,
                                                 OperationDescription operationDesc) {
        javax.jws.soap.SOAPBinding.Style styleOnSEI =
                endpointDesc.getEndpointInterfaceDescription().getSoapBindingStyle();
        javax.jws.soap.SOAPBinding.Style styleOnMethod = operationDesc.getSoapBindingStyle();
        if (styleOnMethod != null && styleOnSEI != styleOnMethod) {
            throw ExceptionFactory.makeWebServiceException(Messages.getMessage("proxyErr2"));
        }
        return MethodMarshallerFactory.getMarshaller(operationDesc, false);
    }

    private Method getJavaMethod(MessageContext mc, Class serviceImplClass) {

        OperationDescription opDesc = mc.getOperationDescription();
        if (opDesc == null) {
            // TODO: NLS
            throw ExceptionFactory.makeWebServiceException("Operation Description was not set");
        }

        Method returnMethod = opDesc.getMethodFromServiceImpl(serviceImplClass);
        if (returnMethod == null) {
            throw ExceptionFactory
                    .makeWebServiceException(Messages.getMessage("JavaBeanDispatcherErr1"));
        }

        return returnMethod;
    }
    /*
    protected boolean isDocLitBare(EndpointDescription endpointDesc, OperationDescription operationDesc){
		javax.jws.soap.SOAPBinding.ParameterStyle methodParamStyle = operationDesc.getSoapBindingParameterStyle();
		if(methodParamStyle!=null){
			return methodParamStyle == javax.jws.soap.SOAPBinding.ParameterStyle.BARE;
		}
		else{
			javax.jws.soap.SOAPBinding.ParameterStyle SEIParamStyle = endpointDesc.getEndpointInterfaceDescription().getSoapBindingParameterStyle();
			return SEIParamStyle == javax.jws.soap.SOAPBinding.ParameterStyle.BARE;
		}
	}
	
	protected boolean isDocLitWrapped(EndpointDescription endpointDesc, OperationDescription operationDesc){
		javax.jws.soap.SOAPBinding.ParameterStyle methodParamStyle = operationDesc.getSoapBindingParameterStyle();
		if(methodParamStyle!=null){
			return methodParamStyle == javax.jws.soap.SOAPBinding.ParameterStyle.WRAPPED;
		}
		else{
		javax.jws.soap.SOAPBinding.ParameterStyle SEIParamStyle = endpointDesc.getEndpointInterfaceDescription().getSoapBindingParameterStyle();
		return SEIParamStyle == javax.jws.soap.SOAPBinding.ParameterStyle.WRAPPED;
		}
	}
    */

}
