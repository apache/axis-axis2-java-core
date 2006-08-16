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
package org.apache.axis2.jaxws.server.dispatcher;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import javax.xml.ws.RequestWrapper;

import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.jaxws.ExceptionFactory;
import org.apache.axis2.jaxws.core.MessageContext;
import org.apache.axis2.jaxws.core.util.MessageContextUtils;
import org.apache.axis2.jaxws.description.DescriptionFactory;
import org.apache.axis2.jaxws.description.EndpointDescription;
import org.apache.axis2.jaxws.description.EndpointInterfaceDescription;
import org.apache.axis2.jaxws.description.OperationDescription;
import org.apache.axis2.jaxws.description.ServiceDescription;
import org.apache.axis2.jaxws.message.Block;
import org.apache.axis2.jaxws.message.Message;
import org.apache.axis2.jaxws.message.factory.BlockFactory;
import org.apache.axis2.jaxws.message.factory.JAXBBlockFactory;
import org.apache.axis2.jaxws.message.factory.MessageFactory;
import org.apache.axis2.jaxws.registry.FactoryRegistry;
import org.apache.axis2.jaxws.wrapper.JAXBWrapperTool;
import org.apache.axis2.jaxws.wrapper.impl.JAXBWrapperToolImpl;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * The JavaBeanDispatcher is used to manage creating an instance of a 
 * JAX-WS service implementation bean and dispatching the inbound 
 * request to that instance.
 */
public class JavaBeanDispatcher extends JavaDispatcher {

    private static final Log log = LogFactory.getLog(JavaBeanDispatcher.class);
    
    private Object serviceInstance;
    
    public JavaBeanDispatcher(Class implClass) {
        super(implClass);
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

        //FIXME: This block should be in the EndpointController.  We'll hold off on that
        //though until this is up and running.
        ServiceDescription serviceDesc = getServiceDescription(mc, serviceImplClass);
        mc.setServiceDescription(serviceDesc);
        mc.setOperationName(mc.getAxisMessageContext().getAxisOperation().getName());
        OperationDescription opDesc = getOperationDescription(mc);
        mc.setOperationDescription(opDesc);
        
        Method target = resolveJavaMethodForOperation(mc);
        Object[] params = getParameterData(target, mc);

        //At this point, we have the method that is going to be invoked and
        //the parameter data to invoke it with, so create an instance and 
        //do the invoke.
        serviceInstance = createServiceInstance();
        Object response = target.invoke(serviceInstance, params);
        
        Block responseBlock = createResponseWrapper(response, opDesc);

        //Create the Message for the response
        MessageFactory factory = (MessageFactory) FactoryRegistry.getFactory(
                MessageFactory.class);
        Message message = factory.create(mc.getMessage().getProtocol());
        message.setBodyBlock(0, responseBlock);

        MessageContext responseMsgCtx = MessageContextUtils.createMessageMessageContext(mc);
        responseMsgCtx.setMessage(message);
        
        return responseMsgCtx;
    }
    
    /*
     * Find the Java method that corresponds to the WSDL operation that was 
     * targeted by the Axis2 Dispatchers.
     */
    private Method resolveJavaMethodForOperation(MessageContext mc) {
        QName opName = mc.getOperationName();
        if (opName == null)
            throw ExceptionFactory.makeWebServiceException("Operation name was not set");
        
        String localPart = opName.getLocalPart();
        Method[] methods = serviceImplClass.getMethods();
        for (int i = 0; i < methods.length; ++i) {
            if (localPart.equals(methods[i].getName()))
                return methods[i];
        }
        
        if (log.isDebugEnabled()) {
            log.debug("No Java method found for the operation");
        }
        
        throw ExceptionFactory.makeWebServiceException("No Java method was found for the operation");
    }
    
    /*
     * Takes the contents of the message and uses that to prepare the parameters
     * for the method that will be invoked.
     */
    private Object[] getParameterData(Method method, MessageContext mc) {
        Class[] params = method.getParameterTypes();
        
        // If there are no params, we don't need to do anything.
        if (params.length == 0)
            return null;

        try {
            OperationDescription opDesc = mc.getOperationDescription();
            
            JAXBContext jbc = createJAXBContext(opDesc);
            BlockFactory factory = (BlockFactory) FactoryRegistry.getFactory(JAXBBlockFactory.class);
            
            Message msg = mc.getMessage();
            Block wrapper = msg.getBodyBlock(0, jbc, factory);
            
            JAXBWrapperTool wrapperTool = new JAXBWrapperToolImpl();
            
            WebParam[] webParams = opDesc.getWebParam();
            ArrayList<String> elements = new ArrayList<String>();
            for (int i = 0; i < webParams.length; ++i) {
                elements.add(webParams[i].name());
            }
            
            Object param = wrapper.getBusinessObject(true);
            Object[] contents = wrapperTool.unWrap(param, elements);
            return contents;
        } catch (Exception e) {
            throw ExceptionFactory.makeWebServiceException(e);
        }
    }
    
    /*
     * Gets the ServiceDescription associated with the request that is currently
     * being processed. 
     */
    //FIXME: This method should be in the EndpointController
    private ServiceDescription getServiceDescription(MessageContext mc, Class implClass) {
        AxisService axisSvc = mc.getAxisMessageContext().getAxisService();
        
        //Check to see if we've already created a ServiceDescription for this
        //service before trying to create a new one. 
        if (axisSvc.getParameter("JAXWS_SERVICE_DESCRIPTION") != null) {
            Parameter param = axisSvc.getParameter("JAXWS_SERVICE_DESCRIPTION");
            ServiceDescription sd = (ServiceDescription) param.getValue();
            return sd;
        }
        else {
            ServiceDescription sd = DescriptionFactory.
                createServiceDescriptionFromServiceImpl(implClass, axisSvc);
            return sd;
        }
    }
    
    /*
     * Gets the OperationDescription associated with the request that is currently
     * being processed. 
     */
    //FIXME: This method should be in the EndpointController
    private OperationDescription getOperationDescription(MessageContext mc) {
        ServiceDescription sd = mc.getServiceDescription();
        EndpointDescription[] eds = sd.getEndpointDescriptions();
        EndpointDescription ed = eds[0];
        EndpointInterfaceDescription eid = ed.getEndpointInterfaceDescription();
        
        OperationDescription[] ops = eid.getOperation(mc.getOperationName());
        OperationDescription op = ops[0];
        
        if (log.isDebugEnabled()) {
            log.debug("wsdl operation: " + op.getName());
            log.debug("   java method: " + op.getJavaMethodName());
        }
        
        return op;        
    }
    
    private JAXBContext createJAXBContext(OperationDescription opDesc) {
        // This will only support Doc/Lit Wrapped params for now.
        try {
            RequestWrapper wrapper = opDesc.getRequestWrapper();
            if (wrapper != null) {
                String wrapperClass = wrapper.className();
                String wrapperPkg = wrapperClass.substring(0, wrapperClass.lastIndexOf("."));
                JAXBContext jbc = JAXBContext.newInstance(wrapperPkg);
                return jbc;
            }
            else {
                throw ExceptionFactory.makeWebServiceException("");
            }
        } catch (JAXBException e) {
            throw ExceptionFactory.makeWebServiceException(e);
        }
    }
    
    private Block createResponseWrapper(Object response, OperationDescription opDesc) {
        try {
            //We'll need a JAXBContext to marshall the response object(s).
            JAXBContext jbc = createJAXBContext(opDesc);
            BlockFactory bfactory = (BlockFactory) FactoryRegistry.getFactory(
                    JAXBBlockFactory.class);
            
            String responseWrapper = opDesc.getResponseWrapper().className();
            Class responseWrapperClass = Class.forName(responseWrapper);
            JAXBWrapperTool wrapperTool = new JAXBWrapperToolImpl();

            WebResult webResult = opDesc.getWebResult();
            ArrayList<String> responseParams = new ArrayList<String>();
            responseParams.add(webResult.name());
 
            ArrayList<String> elements = new ArrayList<String>();
            elements.add(webResult.name());
            
            Map<String, Object> responseParamValues = new HashMap<String, Object>();
            responseParamValues.put(webResult.name(), response);
            
            Object wrapper = wrapperTool.wrap(responseWrapperClass, 
                    responseWrapper, responseParams, responseParamValues);
            
            Block block = bfactory.createFrom(wrapper ,jbc, null);
            return block;
        } catch (Exception e) {
            throw ExceptionFactory.makeWebServiceException(e);
        }
    }
    
}
