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
package org.apache.axis2.jaxws.client.proxy;

import java.util.concurrent.ExecutionException;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.SOAPBody;
import javax.xml.ws.WebServiceException;

import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axis2.jaxws.AxisCallback;
import org.apache.axis2.jaxws.ExceptionFactory;
import org.apache.axis2.jaxws.core.MessageContext;
import org.apache.axis2.jaxws.impl.AsyncListener;


/**
 * ProxyAsyncListener will be used to create response object when client does
 * response.get();
 * The Class will return the data type associated with Response<T> Generic Class.
 * Example Response<Float> will return a Float object to client on Response.get() call.
 */
public class ProxyAsyncListener extends AsyncListener {

	BaseProxyHandler handler = null;
	public ProxyAsyncListener() {
		super();
	}

	public BaseProxyHandler getHandler() {
		return handler;
	}

	public void setHandler(BaseProxyHandler handler) {
		this.handler = handler;
	}

	/**
	 * @param cb
	 */
	public ProxyAsyncListener(AxisCallback cb) {
		super(cb);
		
	}
	
	 public Object getResponseValueObject(MessageContext mc){
		 
		 try{
			 //I will delegate the request to create respose to proxyHandler since it has all the logic written to create response for Sync and oneWay.
			  return handler.createResponse(null, mc);
		 }catch(Exception e){
			throw ExceptionFactory.makeWebServiceException(e);
		 }
	 }

	 
}
