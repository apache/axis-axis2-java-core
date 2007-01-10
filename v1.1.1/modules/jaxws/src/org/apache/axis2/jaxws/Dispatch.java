/*
 * Copyright 2004,2005 The Apache Software Foundation.
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
package org.apache.axis2.jaxws;

import java.util.concurrent.Future;

import javax.xml.bind.JAXBContext;
import javax.xml.namespace.QName;
import javax.xml.ws.AsyncHandler;
import javax.xml.ws.Response;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.Service.Mode;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.SOAPBody;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axis2.jaxws.core.InvocationContext;
import org.apache.axis2.jaxws.core.InvocationContextFactory;
import org.apache.axis2.jaxws.core.MessageContext;
import org.apache.axis2.jaxws.core.controller.AxisInvocationController;
import org.apache.axis2.jaxws.core.controller.InvocationController;
import org.apache.axis2.jaxws.i18n.Messages;
import org.apache.axis2.jaxws.param.JAXBParameter;
import org.apache.axis2.jaxws.param.Parameter;
import org.apache.axis2.jaxws.param.ParameterFactory;
import org.apache.axis2.jaxws.param.ParameterUtils;
import org.apache.axis2.jaxws.spi.ServiceDelegate;
import org.apache.axis2.jaxws.util.Constants;
import org.apache.axis2.jaxws.util.WSDLWrapper;

/**
 * Dispatch is an implementation of the {@link javax.xml.ws.Dispatch} interface.
 * 
 * The Dispatch is a client that can be used to invoke remote services that
 * requires the programmer to operate at the raw XML level.  The XML payloads
 * can be in two different formats, or Modes ({@link javax.xml.ws.Service.Mode}).
 * 
 * @param <T>
 */
public class Dispatch<T> extends BindingProvider implements javax.xml.ws.Dispatch {
    
    //FIXME: Remove the AxisController completely and replace with InvocationController
    private AxisController axisController = null;
    
    private InvocationController ic;
    private ServiceDelegate serviceDelegate;
    private Mode mode;
    private JAXBContext jaxbContext;
   
    public Dispatch() {
        super();
    }
    
    public Dispatch(AxisController axisController){
        super();
        
        //FIXME: Remove this when we remove the AxisController
        this.axisController = axisController;
        
        ic = new AxisInvocationController();
        setRequestContext();
    }
    
    /**
     * Sets the back pointer to the ServiceDelegate instance that created
     * this Dispatch.
     * 
     * @param svcDlg
     */
    public void setServiceDelegate(ServiceDelegate svcDlg) {
        serviceDelegate = svcDlg;
    }
    
    /**
     * Returns the back pointer to the Service Delegate instance that created
     * this Dispatch.
     * 
     * @return
     */
    public ServiceDelegate getServiceDescription() {
        return serviceDelegate;
    }
    
    public Mode getMode() {
        return mode;
    }
    
    public void setMode(Mode m) {
        mode = m;
    }
    
    public JAXBContext getJAXBContext() {
        return jaxbContext;
    }
    
    public void setJAXBContext(JAXBContext jbc) {
        jaxbContext = jbc;
    }
    
    public Object invoke(Object obj) throws WebServiceException {
        /*
        
        // Create the InvocationContext instance for this request/response flow.
        InvocationContext invocationContext = InvocationContextFactory.createInvocationContext(null);
        invocationContext.setServiceClient(axisController.getServiceClient());
        
        // Create the MessageContext to hold the actual request message and its
        // associated properties
        MessageContext requestMsgCtx = new MessageContext();
        invocationContext.setRequestMessageContext(requestMsgCtx);
        
        // FIXME: This is where the Message Model will be integrated instead of 
        // the ParameterFactory/Parameter APIs.
        Parameter param = ParameterFactory.createParameter(obj);
        if (param instanceof JAXBParameter) {
            JAXBParameter p = (JAXBParameter) param;
            p.setJAXBContext(jaxbContext);
        }
        
        OMElement reqEnvelope = toOM(param, axisController.getServiceClient().getOptions().getSoapVersionURI());
        requestMsgCtx.setMessageAsOM(reqEnvelope);
        
        // Copy the properties from the request context into the MessageContext
        requestMsgCtx.getProperties().putAll(requestContext);
        
        // Send the request using the InvocationController
        ic.invoke(invocationContext);
        
        MessageContext responseMsgCtx = invocationContext.getResponseMessageContext();
        
        //FIXME: This is temporary until more of the Message model is available
        OMElement rspEnvelope = responseMsgCtx.getMessageAsOM();
        Parameter rspParam;
        // Create a new Parameter class for the output based on in the 
        // input Parameter type.
        try {
            rspParam = param.getClass().newInstance();
            if (rspParam instanceof JAXBParameter) {
                JAXBParameter p = (JAXBParameter) rspParam;
                p.setJAXBContext(jaxbContext);
            }
        } catch (Exception e) {
            throw ExceptionFactory.makeWebServiceException(e);
        }
        
        rspParam = fromOM(rspEnvelope, rspParam, axisController.getServiceClient().getOptions().getSoapVersionURI());
        
        return rspParam.getValue();
        
        */
        
        return null;
    }
    
   public void invokeOneWay(Object obj) throws WebServiceException{
       if(obj == null){
           throw ExceptionFactory.makeWebServiceException(Messages.getMessage("dispatchInvokeErr1"));
        }
        try{
            Parameter param = ParameterFactory.createParameter(obj);
            axisController.invokeOneWay(param, requestContext);
        }catch(Exception e){
            throw ExceptionFactory.makeWebServiceException(e);
        }
    }
   
    public Future<?> invokeAsync(Object obj, AsyncHandler asynchandler) throws WebServiceException {
       if(obj == null){
    	   throw ExceptionFactory.makeWebServiceException(Messages.getMessage("dispatchInvokeErr2"));
       }
       try{
           Parameter param = ParameterFactory.createParameter(obj);
           return axisController.invokeAsync(param, asynchandler, requestContext);
       } catch(Exception e) {
           throw ExceptionFactory.makeWebServiceException(e);
       }
    }
  
    public Response invokeAsync(Object obj)throws WebServiceException{
        if(obj == null){
        	throw ExceptionFactory.makeWebServiceException(Messages.getMessage("dispatchInvokeErr2"));
        }
        try{
            Parameter param = ParameterFactory.createParameter(obj);
            return axisController.invokeAsync(param, requestContext);
        }catch(Exception e){
            throw ExceptionFactory.makeWebServiceException(e);
        }
    }    

    protected void setRequestContext(){
        String endPointAddress = axisController.getEndpointAddress();
        WSDLWrapper wsdl =  axisController.getWSDLContext();
        QName serviceName = axisController.getServiceName();
        QName portName = axisController.getPortName();
        if(endPointAddress != null && !"".equals(endPointAddress)){
            getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, endPointAddress);
        }else if(wsdl != null){
            String soapAddress = wsdl.getSOAPAddress(serviceName, portName);
            getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, soapAddress);
        }
        
        if(wsdl != null){
            String soapAction = wsdl.getSOAPAction(serviceName, portName);
            getRequestContext().put(BindingProvider.SOAPACTION_URI_PROPERTY, soapAction);
        }
    }
    
    /* 
     * FIXME: This is temporary until more of the Message Model is available.
     */
    private OMElement toOM(Parameter param, String soapVersion){
        SOAPEnvelope env = ParameterUtils.toEnvelope(mode, soapVersion, param);
        System.out.println(">> Generated envelope [" + env.toString() + "]");
        
        SOAPBody body = env.getBody();
        //SOAPHeader soapHeader = env.getHeader();
        //addHeadersToServiceClient(soapHeader);
        return body.getFirstElement();
    }
    
    /*
     * FIXME: This is temporary until more of the Message Model is available. 
     */
    private Parameter fromOM(OMElement element, Parameter response, String soapVersion){
        response.fromOM(element);

        // Convert param toEnvelope since ServiceClient always send xml string.
        // toEnvelope() in Parameter is coded just to handle this.
        SOAPEnvelope env = response.toEnvelope(null, soapVersion);
        
        response.fromEnvelope(mode, env);
        return response;
    }
}
