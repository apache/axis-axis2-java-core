/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 *      
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.axis2.jaxws.server.endpoint.injection.factory;

import javax.xml.ws.WebServiceContext;

import org.apache.axis2.jaxws.i18n.Messages;
import org.apache.axis2.jaxws.server.endpoint.injection.ResourceInjector;
import org.apache.axis2.jaxws.server.endpoint.injection.impl.ResourceInjectionException;
import org.apache.axis2.jaxws.server.endpoint.injection.impl.WebServiceContextInjectorImpl;

public class ResourceInjectionFactory {

	/**
	 * 
	 */
	public ResourceInjectionFactory() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	public static ResourceInjector createResourceInjector(Class resourceType) throws ResourceInjectionException{
		if(resourceType == WebServiceContext.class || resourceType.isAssignableFrom(WebServiceContext.class)){
			return new WebServiceContextInjectorImpl();
		}
		throw new ResourceInjectionException(Messages.getMessage("ResourceInjectionFactoryErr1"));
	}
}
