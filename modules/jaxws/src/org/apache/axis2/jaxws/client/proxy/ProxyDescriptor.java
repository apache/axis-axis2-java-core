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

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;

import javax.jws.Oneway;
import javax.jws.SOAPBinding;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.SOAPBinding.Style;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;

import org.apache.axis2.jaxws.description.EndpointDescription;
import org.apache.axis2.jaxws.description.OperationDescription;
import org.apache.axis2.jaxws.description.ServiceDescription;
import org.apache.axis2.jaxws.handler.PortData;

/**
 * ProxyDescriptor is instantiated from ProxyHandler using the Method argument. 
 * ProxyDescriptor will provide all the annotation details like RequestWrapper class
 * ResponseWrapper Class, WebParam name etc...
 *
 */
public class ProxyDescriptor {
	
	private Class seiClazz = null;
	private Method seiMethod = null;
	private SOAPBinding soapBinding = null;
	private RequestWrapper requestWrapper= null;
	private ResponseWrapper responseWrapper= null;
	private WebParam[] webParam = null;
	private WebResult webResult = null;
	private PortData port = null;
	//TODO replace annotation work once serviceDescription is ready
	private ServiceDescription serviceDescription= null;
	//TODO replace annotation work once operationDescription is ready
	private OperationDescription operationDescription= null;
	//TODO replace annotation work once endpointDescription is ready
	private EndpointDescription endpointDescription = null;

	//TODO Need to put validation to check if seiMethod is null;
	public ProxyDescriptor(Class seiClazz){
		this.seiClazz = seiClazz;
	}
	
	//TODO remove this once OperationDescription is implemented
	public RequestWrapper getRequestWrapper() {
		if(requestWrapper == null){
			requestWrapper = seiMethod.getAnnotation(RequestWrapper.class);
		}
		return requestWrapper;
	}
	
	//TODO remove this once OperationDescription is implemented
	public ResponseWrapper getResponseWrapper() {
		if(responseWrapper == null){
			responseWrapper = seiMethod.getAnnotation(ResponseWrapper.class);
		}
		return responseWrapper;
	}
	
	//TODO remove this once OperationDescription is implemented
	public WebParam[] getWebParam() {
		if(webParam == null){
			Annotation[][] paramAnnotation = seiMethod.getParameterAnnotations();
			ArrayList<WebParam> webParamList = new ArrayList<WebParam>();
			for(Annotation[] pa:paramAnnotation){
				for(Annotation webParam:pa){
					if(webParam.annotationType()==WebParam.class){
						webParamList.add((WebParam)webParam);
					}
				}
			}
			webParam = new WebParam[webParamList.size()];
			webParamList.toArray(webParam);
			
		}
		return webParam;
	}
	
	//TODO remove this once OperationDescription is implemented
	public WebResult getWebResult(){
		if(webResult == null){
			webResult = seiMethod.getAnnotation(WebResult.class);
		}
		return webResult;
	}
	
	//TODO: refactor this once PropertyDescriptor is implemented.
	public Class getRequestWrapperClass(boolean isAsync) throws ClassNotFoundException{
		RequestWrapper requestWrapper = getRequestWrapper();
		String className = null;
		if(requestWrapper == null){
			Class clazz = seiMethod.getDeclaringClass();
			String packageName =clazz.getPackage().getName();
			String capitalized = toClass(seiMethod.getName());
			className = packageName+"."+capitalized;
		}
		if(requestWrapper!=null){
			className = requestWrapper.className();
		}
		return Class.forName(className, true, ClassLoader.getSystemClassLoader());
	}
	
	public String getRequestWrapperClassName(){
		if(getRequestWrapper()== null){
			Class clazz = seiMethod.getDeclaringClass();
			String packageName =clazz.getPackage().getName();
			String className = toClass(seiMethod.getName());
			return packageName+"."+className;
		}
		return getRequestWrapper().className();
	}
	
	public String getRequestWrapperLocalName(){
		if(getRequestWrapper() == null){
			return seiMethod.getName();
		}
		return getRequestWrapper().localName();
	}
	//TODO remove this once OperationDescription is implemented
	public Class getResponseWrapperClass(boolean isAsync) throws ClassNotFoundException{
		ResponseWrapper responseWrapper = getResponseWrapper();
		String className = null;
		if( responseWrapper==null && isAsync){
			//As per jaxws spec section 2.3.4.4
			className = toClass(seiMethod.getName()) + "Response";
		}
		if(responseWrapper == null){
			return seiMethod.getReturnType();
		}
		if(responseWrapper !=null){
			className = responseWrapper.className();
		}
		return Class.forName(className, true, ClassLoader.getSystemClassLoader());
	}

	public String getResponseWrapperLocalName(){
		if(getResponseWrapper()==null){
			return null;
		}
		return getResponseWrapper().localName();
	}
	//TODO remove this once OperationDescription is implemented
	public String getWebResultName(boolean isAsync){
		WebResult webResult = getWebResult();
		if(webResult == null &&!isAsync){
			if(!isOneWay() && !seiMethod.getReturnType().getName().equals("void")){
				return "return";
			}
		}
		if(webResult == null){
			//I will return null here and when creating result in ProxyHandler I will check for null and return the wrapperObject if no webResultName found.
			return null;
		}
		return getWebResult().name();
	}
	
	public ArrayList<String> getParamNames(){
		//TODO what if the param itself is a holder class;
		WebParam[] params = getWebParam();
		ArrayList<String> names = new ArrayList<String>();
		for(WebParam webParam:params){
			//skip asyncHandler, method param name will be asyncHandler as per jaxws specification.
			if(webParam.name().equals("asyncHandler")){
				continue;
			}
			names.add(webParam.name());
		}
		return names;
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
	}
	public SOAPBinding getSoapBindingOnClazz(){
		if(soapBinding == null){
			soapBinding = (SOAPBinding)seiClazz.getAnnotation(SOAPBinding.class);
		}
		return soapBinding;
	}
	public SOAPBinding getSoapBindingOnMethod(){
		//TODO who has presendence if there is SOAPBinding on Class and method.
		return null;
	}
	//TODO read soap binding on method too, make sure if Binding style is different from binding style in Clazz throw Exception.
	public Style getBindingStyle(){
		if(getSoapBindingOnClazz()== null){
			return SOAPBinding.Style.DOCUMENT;
		}
		return getSoapBindingOnClazz().style(); 
	}

	public Class getSeiClazz() {
		return seiClazz;
	}

	public void setSeiClazz(Class seiClazz) {
		this.seiClazz = seiClazz;
	}
	/*
	 * Convert getString to GetString. Converts method to clazz;
	 */
	private String toClass(String method){
		if(method == null){
			//Throw exception but I should have check this even before this method is Invoked.
		}
		StringBuffer methodName = new StringBuffer(method);
		return methodName.replace(0,1, methodName.substring(0,1).toUpperCase()).toString();
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
	
	public boolean isOneWay(){
		return seiMethod.isAnnotationPresent(Oneway.class);
	}
}
