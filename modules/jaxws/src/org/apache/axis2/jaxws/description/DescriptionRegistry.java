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

package org.apache.axis2.jaxws.description;

import java.util.Hashtable;
import java.util.Map;
/*
 * Description Registry is a singleton class that will be used to cache ServiceDescription.
 */
public class DescriptionRegistry {

	/**
	 * This class will be used to cache the ServiceDescription, Description factory will reuse ServiceDescription Instances for same 
	 * ServiceQName, WSDL URL & ServiceClass.
	 */
	private static DescriptionRegistry registry = new DescriptionRegistry();
	private Map<DescriptionKey, ServiceDescription> cache = new Hashtable<DescriptionKey, ServiceDescription>();
	private DescriptionRegistry() {
		super();
		
	}
	
	public ServiceDescription getServiceDescription(DescriptionKey key){
		ServiceDescription sd = cache.get(key);
		if(sd == null){
			sd = DescriptionFactory.createServiceDescription(key.getWsdlUrl(), key.getServiceName(), key.getServiceClass());
			cache.put(key, sd);
		}
		return sd;
	}
	
	public static DescriptionRegistry getRegistry(){
		return registry;	
	}
}
