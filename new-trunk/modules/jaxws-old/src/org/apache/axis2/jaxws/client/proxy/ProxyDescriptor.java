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
package org.apache.axis2.jaxws.client.proxy;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Map;

import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.Style;
import javax.xml.ws.AsyncHandler;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.Response;
import javax.xml.ws.ResponseWrapper;

import org.apache.axis2.jaxws.ExceptionFactory;
import org.apache.axis2.jaxws.description.EndpointDescription;
import org.apache.axis2.jaxws.description.OperationDescription;
import org.apache.axis2.jaxws.description.ServiceDescription;
import org.apache.axis2.jaxws.handler.PortData;
import org.apache.axis2.jaxws.i18n.Messages;

/**
 * ProxyDescriptor is instantiated from ProxyHandler using the Method argument. 
 * ProxyDescriptor will provide all the annotation details like RequestWrapper class
 * ResponseWrapper Class, WebParam name etc...
 *
 */
public class ProxyDescriptor {
	
	private Class seiClazz = null;
	private Method seiMethod = null;
	private PortData port = null;
    private ServiceDescription serviceDescription= null;
	private OperationDescription operationDescription= null;
	private EndpointDescription endpointDescription = null;
   
    
	public ServiceDescription getServiceDescription() {
     return serviceDescription;   
    }
    public OperationDescription getOperationDescription() {
        return operationDescription;
    }
    public EndpointDescription getEndpointDescription() {
        return endpointDescription;
    }

	//TODO Need to put validation to check if seiMethod is null;
	public ProxyDescriptor(Class seiClazz, ServiceDescription serviceDescription){
		this.seiClazz = seiClazz;
        this.serviceDescription = serviceDescription;
        // FIXME: This probably needs to be more robust; can there be > 1 endpoints; if so, how choose which one?
        this.endpointDescription = serviceDescription.getEndpointDescription(seiClazz)[0];
	}
	
	public Class getRequestWrapperClass(boolean isAsync) throws ClassNotFoundException{
		String className = operationDescription.getRequestWrapperClassName();
		return Class.forName(className, true, Thread.currentThread().getContextClassLoader());
	}
	
	public String getRequestWrapperClassName(){
		return operationDescription.getRequestWrapperClassName();
	}
	
	public String getRequestWrapperLocalName(){
		return operationDescription.getRequestWrapperLocalName();
	}

	public Class getResponseWrapperClass(boolean isAsync) throws ClassNotFoundException{
		//TODO: Move this logic to OperationDescription. This is a hack right now.
		if(isAsync){
			return getReturnType(isAsync);
		}
		
		String className = operationDescription.getResponseWrapperClassName();
		return Class.forName(className, true, Thread.currentThread().getContextClassLoader());
	}

	public String getResponseWrapperLocalName(){
		return operationDescription.getResponseWrapperLocalName();
	}
    public String getWebResultName(boolean isAsync){
        return operationDescription.getWebResultName();
	}
    // TODO: Move to OperationDescription?
	public ArrayList<String> getParamNames(){ 
        return new ArrayList<String>(Arrays.asList(operationDescription.getWebParamNames()));
	}
	public ArrayList<String> getParamtns(){ 
        return new ArrayList<String>(Arrays.asList(operationDescription.getWebParamTNS()));
	}
	public String getParamtns(String name){ 
        return operationDescription.getWebParamTNS(name);
	}
	public PortData getPort() {
		return port;
	}
	public void setPort(PortData port) {
		this.port = port;
	}
	public Method getSeiMethod() {
		return seiMethod;
	}
	public void setSeiMethod(Method seiMethod) {
		this.seiMethod = seiMethod;
        operationDescription = endpointDescription.getEndpointInterfaceDescription().getOperation(seiMethod);
	}
	
	/*
	 * This method looks at @SOAPBindingAnnotation on clazz to look for Parameter Style
	 */
	public boolean isClazzDocLitBare(){
		SOAPBinding.ParameterStyle style = endpointDescription.getEndpointInterfaceDescription().getSoapBindingParameterStyle();
		return style == SOAPBinding.ParameterStyle.BARE;
	}
	
	public boolean isClazzDocLitWrapped(){
		SOAPBinding.ParameterStyle style = endpointDescription.getEndpointInterfaceDescription().getSoapBindingParameterStyle();
		return style == SOAPBinding.ParameterStyle.WRAPPED;
	}
	
	//TODO read soap binding on method too, make sure if Binding style is different from binding style in Clazz throw Exception.
	public Style getBindingStyle(){
        return endpointDescription.getEndpointInterfaceDescription().getSoapBindingStyle(); 
	}
	public Class getSeiClazz() {
		return seiClazz;
	}
	public void setSeiClazz(Class seiClazz) {
		this.seiClazz = seiClazz;
	}
    public boolean isOneWay(){
        return operationDescription.isOneWay();
    }

    public String filterAsync(String method){
		if(method.endsWith("Async")){
			int index =method.lastIndexOf("Async");
			return method.substring(0,index);
		}
		else{
			return method;
		}
	}
	
	/**
	 * In this method I am trying get the return type of the method.
	 * if SEI method is Async pooling implmentation then return type is actual type in Generic Response, example Response<ClassName>.
	 * if SEI method is Async Callback implementation then return type is actual type of method parameter type AsyncHandler, example AsyncHandler<ClassName>
	 * I use java reflection to get the return type.
	 * @param isAsync
	 * @return
	 */
	public Class getReturnType(boolean isAsync){
		Class returnType = seiMethod.getReturnType();
		if(isAsync){
			//pooling implementation
			if(Response.class.isAssignableFrom(returnType)){
				Type type = seiMethod.getGenericReturnType();
				ParameterizedType pType = (ParameterizedType) type;
				return (Class)pType.getActualTypeArguments()[0];	
			}
			//Callback Implementation
			else{
				Type[] type = seiMethod.getGenericParameterTypes();
				Class parameters[]= seiMethod.getParameterTypes();
				int i=0;
				for(Class param:parameters){
					if(AsyncHandler.class.isAssignableFrom(param)){
						ParameterizedType pType = (ParameterizedType)type[i];
						return (Class)pType.getActualTypeArguments()[0];
					}
					i++;
				}
			}
			
		}
		
		return returnType;	
	}
}
