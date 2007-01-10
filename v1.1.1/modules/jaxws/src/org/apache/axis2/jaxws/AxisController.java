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

import java.net.URL;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import javax.xml.namespace.QName;
import javax.xml.ws.AsyncHandler;
import javax.xml.ws.Response;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.Service.Mode;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.SOAPBody;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPHeader;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.addressing.AddressingConstants;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.context.ServiceGroupContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.jaxws.handler.PortData;
import org.apache.axis2.jaxws.i18n.Messages;
import org.apache.axis2.jaxws.impl.AsyncListenerWrapper;
import org.apache.axis2.jaxws.impl.AsyncListener;
import org.apache.axis2.jaxws.param.JAXBParameter;
import org.apache.axis2.jaxws.param.Parameter;
import org.apache.axis2.jaxws.param.ParameterFactory;
import org.apache.axis2.jaxws.param.ParameterUtils;
import org.apache.axis2.jaxws.util.Constants;
import org.apache.axis2.jaxws.util.WSDLWrapper;


public class AxisController {
    private AxisService axisService = null;
//  TODO: This configContext will come from websphere deployment code later
    private ConfigurationContext configContext = null; 
    private ServiceClient serviceClient = null;
    private ServiceContext serviceContext = null;
    private JAXWSClientContext clientContext = null;
    private ServiceGroupContext groupContext = null;
    private EndpointReference myEPR;
    
    public AxisService getAxisService() {
        return axisService;
    }
    public void setAxisService(AxisService axisService) {
        this.axisService = axisService;
    }
    public ConfigurationContext getConfigContext() {
        return configContext;
    }
    public void setConfigContext(ConfigurationContext configContext) {
        this.configContext = configContext;
    }
    public ServiceClient getServiceClient() {
        return serviceClient;
    }
    public void setServiceClient(ServiceClient serviceClient) {
        this.serviceClient = serviceClient;
    }
    public ServiceContext getServiceContext() {
        return serviceContext;
    }
    public void setServiceContext(ServiceContext serviceContext) {
        this.serviceContext = serviceContext;
    }
    public JAXWSClientContext getClientContext() {
        return clientContext;
    }
    public void setClientContext(JAXWSClientContext clientContext) {
        this.clientContext = clientContext;
    }
    public ServiceGroupContext getGroupContext() {
        return groupContext;
    }
    public void setGroupContext(ServiceGroupContext groupContext) {
        this.groupContext = groupContext;
    }
    public PortData getPortInfo(){
        return clientContext.getPort();
    }
    public QName getServiceName(){
        return getPortInfo().getServiceName();
    }
    public QName getPortName(){
        return getPortInfo().getPortName();
    }
    public String getEndpointAddress(){
        return getPortInfo().getEndpointAddress();
    }
    public String getBindingId(){
        return getPortInfo().getBindingID();
    }
    public WSDLWrapper getWSDLContext(){
        return clientContext.getWsdlContext();
    }
    public ExecutorService getExecutor() {
        return clientContext.getExecutor();
    }
    public Mode getServiceMode() {
        return (Mode) clientContext.getServiceMode();
    }
    public URL getWSDLLocation(){
        return clientContext.getWSDLLocation(); 
    }

    public Object invoke(Parameter param, Map requestContext) throws WebServiceException {
        setupProperties(requestContext);
        
        try{
            //TODO: This is not the correct way to setup the JAXBContext
            if (clientContext.getJAXBContext() != null) {
                JAXBParameter p = (JAXBParameter) param;
                p.setJAXBContext(clientContext.getJAXBContext());
            }
            
            serviceClient.getOptions().setTo(new org.apache.axis2.addressing.EndpointReference(getEndpointURL(requestContext)));
            String soapAction = getSOAPAction(requestContext);
            if (soapAction != null) {
                serviceClient.getOptions().setAction(soapAction);    
            }
            else {
                //TODO: This should be an addressing exception on the client side
                serviceClient.getOptions().setAction("none");
            }
            
            //Create the Parameter wrapper for the response based on what the input 
            //type was.  If it was a JAXBParameter, then set the JAXBContext on it as well
            Parameter response = ParameterFactory.createParameter(param.getValue().getClass());
            if (param instanceof JAXBParameter) {
                JAXBParameter p = (JAXBParameter) response;
                p.setJAXBContext(clientContext.getJAXBContext());
            }
            OMElement axisResponse = null;
            
            //TODO: Team needs to decide if we are going to use ServiceClient api or go to AxisEngine api directly. ServiceClient requires that we send a OMElement
            //and it creates a SOAPEnvelop by reading the headers that dispatch sets in ServiceClient. This is not a good way for message modeas we will be 
            //manuplating client message first to read all the headers and then read the body. we add the headers in ServiceClient then create OMElement from body
            //and send then OMElement in SendReceive operation, which then is converted again to an envelope and header headers are added to it by ServiceClient 
            //before sending it to axis enging. 
            axisResponse = serviceClient.sendReceive(ServiceClient.ANON_OUT_IN_OP, toOM(param));

            //TODO: If ServiceClient can return the actual sopaEnvelope from MessageContext we can use the message mode and param this way.
            //response.fromEnvelope(mode, axisResponse);
            //return response.getValue();
            return buildResponse(axisResponse, response).getValue();
        }catch(AxisFault e){
        	// TODO Add Fault Processing
        	throw ExceptionFactory.makeWebServiceException(Messages.getMessage("faultProcessingNotSupported", e.getMessage()), e);
            
        }       
    }
    
    public void invokeOneWay(Parameter param, Map requestContext) throws WebServiceException{
        setupProperties(requestContext);
        
        try{
            //TODO: This is not the correct way to setup the JAXBContext
            if (clientContext.getJAXBContext() != null) {
                JAXBParameter p = (JAXBParameter) param;
                p.setJAXBContext(clientContext.getJAXBContext());
            }
            
            serviceClient.getOptions().setTo(new org.apache.axis2.addressing.EndpointReference(getEndpointURL(requestContext)));
            String soapAction = getSOAPAction(requestContext);
            if (soapAction != null) {
                serviceClient.getOptions().setAction(soapAction);    
            }
            else {
                serviceClient.getOptions().setAction("none");
            }
            
            serviceClient.fireAndForget(ServiceClient.ANON_OUT_ONLY_OP, toOM(param));
        } catch(AxisFault e) {
        	// TODO Add Fault Processing
        	throw ExceptionFactory.makeWebServiceException(Messages.getMessage("faultProcessingNotSupported", e.getMessage()), e);

        }
    }
    
    public Future<?> invokeAsync(Parameter param, AsyncHandler asynchandler, Map requestContext) throws WebServiceException{
        setupProperties(requestContext);
        
        try{
            //TODO: This is not the correct way to setup the JAXBContext
            if (clientContext.getJAXBContext() != null) {
                JAXBParameter p = (JAXBParameter) param;
                p.setJAXBContext(clientContext.getJAXBContext());
            }
            
            serviceClient.getOptions().setTo(new EndpointReference(getEndpointURL(requestContext)));
            serviceClient.getOptions().setReplyTo(getMyEPR());
            
            //TODO: This is a hack.  Need a better way to determine the default wsa:Action if
            //a SOAPAction header does not exist.
            String soapAction = getSOAPAction(requestContext);
            if (soapAction != null) {
                serviceClient.getOptions().setAction(soapAction);    
            }
            else {
                serviceClient.getOptions().setAction("none");
            }
            
            AxisCallback callback = new AxisCallback();
            Boolean useAsyncMep = (Boolean) requestContext.get(Constants.USE_ASYNC_MEP);

            if((useAsyncMep != null && useAsyncMep.booleanValue()) 
                    || serviceClient.getOptions().isUseSeparateListener()) {
                serviceClient.getOptions().setUseSeparateListener(true);
                serviceClient.getOptions().setTransportInProtocol("http");
            }

            serviceClient.sendReceiveNonBlocking(ServiceClient.ANON_OUT_IN_OP, 
                    toOM(param), callback);
            
            //Create the Parameter wrapper for the response based on what the input 
            //type was.  If it was a JAXBParameter, then set the JAXBContext on it as well
            Parameter responseParam = ParameterFactory.createParameter(param.getValue().getClass());
            if (param instanceof JAXBParameter) {
                JAXBParameter p = (JAXBParameter) responseParam;
                p.setJAXBContext(clientContext.getJAXBContext());
            }
            
            AsyncListener asyncProcessor = new AsyncListener(callback);
            asyncProcessor.setMode((Mode) clientContext.getServiceMode());
            //asyncProcessor.setParameter(responseParam);
            
            AsyncListenerWrapper<?> response = new AsyncListenerWrapper<Object>(asyncProcessor);
            if(asynchandler !=null){
                response.setAsyncHandler(asynchandler);
            }
            
            try {
                getExecutor().submit(response).get();
            } catch (InterruptedException e) {
                e.printStackTrace();
                throw ExceptionFactory.makeWebServiceException(e);
            } catch (ExecutionException e) {
                e.printStackTrace();
                throw ExceptionFactory.makeWebServiceException(e);
            }
            
            //TODO: Need to figure out who/when the Listener should be shutdown
            //Do we do it after this request?  Or, can we ask the listener to check
            //itself to see if any other responses are outstanding.
            return response;
        }catch(AxisFault e){
        	// TODO Add Fault Processing
        	throw ExceptionFactory.makeWebServiceException(Messages.getMessage("faultProcessingNotSupported", e.getMessage()), e);
        }
    }
    
    public Response invokeAsync(Parameter param, Map requestContext)throws WebServiceException{
    	AsyncListenerWrapper<Object>  response= (AsyncListenerWrapper<Object>)invokeAsync(param, null, requestContext);
        return response;
    }
    
    private String getEndpointURL(Map requestContext){
        return (String) requestContext.get(BindingProvider.ENDPOINT_ADDRESS_PROPERTY);
    }
    
    private String getSOAPAction(Map requestContext){
        Boolean useSoapAction = (Boolean)requestContext.get(BindingProvider.SOAPACTION_USE_PROPERTY);
        if(useSoapAction!=null && useSoapAction.booleanValue()){
            return (String)requestContext.get(BindingProvider.SOAPACTION_URI_PROPERTY);
        }
        return null;
    }
    
    private OMElement toOM(Parameter param){
        /*TODO: This is a a hack.... I am getting the Header of the message and setting serviceClent header, then 
        * extract body of message as OM and ServiceCleint will create the envelope. 
        * I am doing this because ServiceClient wants to form the envelope and send it to AxisEngine.
        * I would like to return param.toEnvelope() but ServiceClient will try to build envelope on top of envelope.
        * Let just go directly to AxisEngine forget about ServiceClient... can I?
        */
        SOAPEnvelope env = ParameterUtils.toEnvelope((Mode) clientContext.getServiceMode(), 
                serviceClient.getOptions().getSoapVersionURI(),
                param);
        SOAPBody body= env.getBody();
        SOAPHeader soapHeader = env.getHeader();
        addHeadersToServiceClient(soapHeader);
        return body.getFirstElement();
    }
    
    private void addHeadersToServiceClient(SOAPHeader soapHeader){
        if(soapHeader!=null){
            for(Iterator headers = soapHeader.getChildElements(); headers.hasNext();){
                OMElement header = (OMElement)headers.next();
                serviceClient.addHeader(header);
            }
        }
    }
    
    private Parameter buildResponse(OMElement element, Parameter xmlResponse){
        //Create empty SoapResponse first 
        Parameter soapResponse = xmlResponse;
        
        /* get xmlResponse param from ServiceClient OM response, By the way ServiceClient always retuns an 
         * OMElement xml string not Soap Env or Body.
         * It does something like msgCtx.getEnvelope().getBody.getChild() --> i.e OMElement under the body.
         * So we now have to go thru the pain of recreating the envelope. This is a performance issue...
        */
        xmlResponse.fromOM(element);
        /*I will convert param toEnvelope since ServiceClient always send xml string.
         * toEnvelope() in Parameter is coded just to handle this.
         */
        SOAPEnvelope env =xmlResponse.toEnvelope(null,serviceClient.getOptions().getSoapVersionURI());
        
        //TODO:(NLG) Need to figure out why we have to cast to (Mode) here. 
        soapResponse.fromEnvelope((Mode) clientContext.getServiceMode(), env);
        
        return soapResponse;
    }

    /*
     * Returns the EPR that should be used for in-bound async responses
     */
    private EndpointReference getMyEPR() {
        if (myEPR != null) {        
            return myEPR;
        }
        else {
            try {
                //TODO:(NLG) This should not be hard coded to HTTP and should allow
                //for other transports to be used.
                myEPR = serviceClient.getMyEPR("http");
            } catch (AxisFault e) {
                e.printStackTrace();
            }
            return myEPR;
        }
    }
    
    
    /*
     * TODO: This is a first pass at filtering the properties that are set on the 
     * RequestContext.  Right now it's called during the invoke, but needs to be 
     * moved over to when the property is set.  This should not be in the path
     * of performance.
     */
    private void setupProperties(Map<String, Object> requestCtx) {
        for (Iterator<String> it = requestCtx.keySet().iterator(); it.hasNext(); ) {
            String key = it.next();
            Object value = requestCtx.get(key);
            
            serviceClient.getOptions().setProperty(key, value);
        }
    }
}
