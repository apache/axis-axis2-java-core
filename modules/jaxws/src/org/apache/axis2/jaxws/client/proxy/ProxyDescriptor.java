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

import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;

/**
 * ProxyDescriptor is instantiated from ProxyHandler using the Method argument. 
 * ProxyDescriptor will provide all the annotation details like RequestWrapper class
 * ResponseWrapper Class, WebParam name etc...
 *
 */
public class ProxyDescriptor {
	private Method proxyMethod = null;
	private RequestWrapper requestWrapper= null;
	private ResponseWrapper responseWrapper= null;
	private WebParam[] webParam = null;
	private WebResult webResult = null;
	
	public ProxyDescriptor(Method method){
		this.proxyMethod = method;
	}
	
	//TODO remove this once OperationDescription is implemented
	public RequestWrapper getRequestWrapper() {
		if(requestWrapper == null){
			requestWrapper = proxyMethod.getAnnotation(RequestWrapper.class);
		}
		return requestWrapper;
	}
	
	//TODO remove this once OperationDescription is implemented
	public ResponseWrapper getResponseWrapper() {
		if(responseWrapper == null){
			responseWrapper = proxyMethod.getAnnotation(ResponseWrapper.class);
		}
		return responseWrapper;
	}
	
	//TODO remove this once OperationDescription is implemented
	public WebParam[] getWebParam() {
		if(webParam == null){
			Annotation[][] paramAnnotation = proxyMethod.getParameterAnnotations();
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
			webResult = proxyMethod.getAnnotation(WebResult.class);
		}
		return webResult;
	}
	
	//TODO: refactor this once PropertyDescriptor is implemented.
	public Class getRequestWrapperClass() throws ClassNotFoundException{
		return Class.forName(getRequestWrapper().className(), true, ClassLoader.getSystemClassLoader());
	}
	
	public String getRequestWrapperClassName(){
		return getRequestWrapper().className();
	}
	public String getRequestWrapperLocalName(){
		return getRequestWrapper().localName();
	}
	//TODO remove this once OperationDescription is implemented
	public Class getResponseWrapperClass() throws ClassNotFoundException{
		return Class.forName(getResponseWrapper().className(), true, ClassLoader.getSystemClassLoader());
	}
	public String getResponseWrapperClassName(){
		return getResponseWrapper().className();
	}
	public String getResponseWrapperLocalName(){
		return getResponseWrapper().localName();
	}
	//TODO remove this once OperationDescription is implemented
	public String getWebResultName(){
		return getWebResult().name();
	}
	
	public ArrayList<String> getParamNames(){
		//TODO what if the param itself is a holder class;
		WebParam[] params = getWebParam();
		ArrayList<String> names = new ArrayList<String>();
		for(WebParam webParam:params){
			names.add(webParam.name());
		}
		return names;
	}
}
