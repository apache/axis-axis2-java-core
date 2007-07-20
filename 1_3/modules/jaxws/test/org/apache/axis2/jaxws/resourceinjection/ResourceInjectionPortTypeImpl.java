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
package org.apache.axis2.jaxws.resourceinjection;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.jws.WebService;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;

import org.apache.axis2.jaxws.resourceinjection.sei.ResourceInjectionPortType;
import org.apache.axis2.jaxws.TestLogger;

@WebService(endpointInterface="org.apache.axis2.jaxws.resourceinjection.sei.ResourceInjectionPortType")
public class ResourceInjectionPortTypeImpl implements ResourceInjectionPortType {

	@Resource
	public WebServiceContext ctx = null;
	
	public ResourceInjectionPortTypeImpl() {
		super();
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see org.apache.axis2.jaxws.resourceinjection.sei.ResourceInjectionPortType#echo(java.lang.String)
	 */
	public String echo(String arg) {
		String response = "Response of Activities on Server;";
		if(ctx != null){
			response = response + "WebServiceContext injected;";
			MessageContext msgContext = ctx.getMessageContext();
			if(msgContext !=null){
				response = response + "MessageContext was also found;";
			}
			else{
				response = response + "MessageContext not found;";
			}
		}
		else{
			response = response + "WebServiceContext not found;";
		}
	
		return response;
	}
	
	@PostConstruct
	public void initialize(){
		//Called after resource injection and before a method is called.
        TestLogger.logger.debug("Calling PostConstruct to Initialize");
	}
	
	@PreDestroy
	public void distructor(){
		//Called before the scope of request or session or application ends.

        TestLogger.logger.debug("Calling PreDestroy ");
		
	}

}
