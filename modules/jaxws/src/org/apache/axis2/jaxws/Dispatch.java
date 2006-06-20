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

import javax.xml.ws.AsyncHandler;
import javax.xml.ws.Response;
import javax.xml.ws.WebServiceException;


import javax.xml.namespace.QName;

import org.apache.axis2.jaxws.param.Parameter;
import org.apache.axis2.jaxws.param.ParameterFactory;
import org.apache.axis2.jaxws.util.Constants;
import org.apache.axis2.jaxws.util.WSDLWrapper;

public class Dispatch<T> extends BindingProvider implements javax.xml.ws.Dispatch {
	
    AxisController axisController = null;
   
    public Dispatch() {
        super();
    }
    
    public Dispatch(AxisController axisController){
    	super();
    	this.axisController = axisController;
    	setRequestContext();
    }
  
    public Object invoke(Object obj) throws WebServiceException {
    	try{
    		if(obj == null){
    			throw new WebServiceException("Dispatch Cannot Invoke SEI with null object");
    		}
    		Parameter param = ParameterFactory.createParameter(obj);
    		return axisController.invoke(param,requestContext);
    	}catch(Exception e){
    		throw new WebServiceException(e);
    	}
   }
    
   public void invokeOneWay(Object obj) throws WebServiceException{
       if(obj == null){
			throw new WebServiceException("Dispatch Cannot Invoke SEI with null object");
		}
    	try{
    		Parameter param = ParameterFactory.createParameter(obj);
            axisController.invokeOneWay(param, requestContext);
    	}catch(Exception e){
    		throw new WebServiceException(e);
    	}
    }
   
    public Future<?> invokeAsync(Object obj, AsyncHandler asynchandler) throws WebServiceException {
       if(obj == null){
           throw new WebServiceException("Dispatch Cannot Invoke SEI with null object");
       }
       try{
           Parameter param = ParameterFactory.createParameter(obj);
           return axisController.invokeAsync(param, asynchandler, requestContext);
       } catch(Exception e) {
           throw new WebServiceException(e);
       }
    }
  
    public Response invokeAsync(Object obj)throws WebServiceException{
    	if(obj == null){
			throw new WebServiceException("Dispatch Cannot Invoke SEI with null object");
		}
    	try{
    		Parameter param = ParameterFactory.createParameter(obj);
    		return axisController.invokeAsync(param, requestContext);
    	}catch(Exception e){
    		throw new WebServiceException(e);
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
        
        getRequestContext().put(Constants.QOS_WSADDRESSING_ENABLE, Boolean.FALSE);
        getRequestContext().put(Constants.QOS_WSRM_ENABLE, Boolean.FALSE);
    }
}
