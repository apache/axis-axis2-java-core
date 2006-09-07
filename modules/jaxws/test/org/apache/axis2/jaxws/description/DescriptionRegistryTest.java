package org.apache.axis2.jaxws.description;
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
import java.io.File;
import java.net.URL;

import javax.xml.namespace.QName;

import org.apache.axis2.jaxws.description.DescriptionKey;
import org.apache.axis2.jaxws.proxy.doclitwrapped.sei.ProxyDocLitWrappedService;

import junit.framework.TestCase;

public class DescriptionRegistryTest extends TestCase {
	private String wsdlLocation = "test/org/apache/axis2/jaxws/proxy/doclitwrapped/META-INF/ProxyDocLitWrapped.wsdl";
	private QName serviceName = new QName("http://org.apache.axis2.proxy.doclitwrapped", "ProxyDocLitWrappedService");
	private Class serviceClass = ProxyDocLitWrappedService.class;
	public DescriptionRegistryTest() {
		super();
		
	}

	public DescriptionRegistryTest(String arg0) {
		super(arg0);
		
	}
	
	public void testDescriptionRegistry(){
		try{
			File wsdl= new File(wsdlLocation); 
			URL wsdlUrl = wsdl.toURL(); 
			DescriptionKey key = new DescriptionKey(serviceName, wsdlUrl, serviceClass);
			
			DescriptionRegistry registry = DescriptionRegistry.getRegistry();
			//Get ServiceDescription and cache it in Registry
			ServiceDescription description1 = registry.getServiceDescription(key);
			//Now get ServiceDescription from cache. So lets start over.
			registry = null;
			registry = DescriptionRegistry.getRegistry();
			ServiceDescription description2 = registry.getServiceDescription(key);
			
			if(description1 == description2){
				System.out.println("Got ServiceDescription from Registry Cache");
			}
			else{
				throw new Exception("Could not get ServiceDescription from Registry Cache");
			}
		}catch(Exception e){
			fail("Test :"+ getName() + " failed with following exception" + e.getMessage());
		}
	}
	
	private DescriptionRegistry getRegistry(){
		return DescriptionRegistry.getRegistry();
	}
	
	
}
