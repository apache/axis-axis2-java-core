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


package org.apache.axis2.jaxws.description.builder;

import junit.framework.TestCase;

import org.apache.axis2.jaxws.description.builder.DescriptionBuilderComposite;
import org.apache.axis2.jaxws.description.builder.WebServiceAnnot;

/**
 * Directly test the Description classes built via annotations without a WSDL file.
 * These tests focus on combinations of the following:
 * - A generic service (no annotations)
 * - A generated service (annotations)
 * - An SEI
 */
public class DescriptionBuilderTests extends TestCase {
    
    /* 
     * ========================================================================
     * ServiceDescription Tests
     * ========================================================================
     */
    public void testCreateWebServiceInputImpl() {
        String name = "EchoServiceAnnotated";
        String targetNamespace = "echoTargetNamespace";
        String serviceName = "EchoServiceName";
        String wsdlLocation = "http://EchoService/wsdl";
        String endpointInterface = "EchoServiceEndpointInterface";
        String portName = "EchoServiceAnnotatedPort";
        
    	WebServiceAnnot webServiceAnnotImpl1 = 
    				WebServiceAnnot.createWebServiceAnnotImpl();

    	WebServiceAnnot webServiceAnnotImpl2 = 
			WebServiceAnnot.createWebServiceAnnotImpl(
	    													name,
	    													targetNamespace,
	    													serviceName,
	    													wsdlLocation,
	    													endpointInterface,
	    													portName);
    	
    	DescriptionBuilderComposite descriptionBuilderComposite = 
    									new DescriptionBuilderComposite();
    	
    	descriptionBuilderComposite.setWebServiceAnnot(webServiceAnnotImpl2);
    	
    	
    	WebServiceAnnot webServiceAnnotImpl3 = 
    		descriptionBuilderComposite.getWebServiceAnnot();
        
    	assertNotNull("WebService name not set", webServiceAnnotImpl3.name());
    	assertNotNull("WebService targetNamespace not set", webServiceAnnotImpl3.name());
    	assertNotNull("WebService serviceName not set", webServiceAnnotImpl3.name());
    	assertNotNull("WebService wsdlLocation not set", webServiceAnnotImpl3.name());
    	assertNotNull("WebService endpointInterface not set", webServiceAnnotImpl3.name());
	
    	System.out.println("WebService name:" +webServiceAnnotImpl3.name());
    }
 
}
