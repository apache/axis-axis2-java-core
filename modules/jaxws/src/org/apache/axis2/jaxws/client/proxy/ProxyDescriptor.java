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
import java.util.ArrayList;
import java.util.Arrays;

import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.Style;
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
	private PortData port = null;

    private ServiceDescription serviceDescription= null;
	private OperationDescription operationDescription= null;
	private EndpointDescription endpointDescription = null;

	//TODO Need to put validation to check if seiMethod is null;
	public ProxyDescriptor(Class seiClazz, ServiceDescription serviceDescription){
		this.seiClazz = seiClazz;
        this.serviceDescription = serviceDescription;
        // TODO: (JLB) This probably needs to be more robust; can there be > 1 endpoints; if so, how choose which one?
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
		String className = operationDescription.getResponseWrapperClassName();
		return Class.forName(className, true, Thread.currentThread().getContextClassLoader());
	}

	public String getResponseWrapperLocalName(){
		return operationDescription.getResponseWrapperLocalName();
	}
    public String getWebResultName(boolean isAsync){
        return operationDescription.getWebResultName();
	}

	public ArrayList<String> getParamNames(){
        return new ArrayList<String>(Arrays.asList(operationDescription.getWebParamNames()));
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
}
