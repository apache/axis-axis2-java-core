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

import java.lang.reflect.Field;
import java.net.URL;

import javax.jws.WebParam.Mode;
import javax.xml.ws.Service;

import org.apache.axis2.jaxws.description.builder.DescriptionBuilderComposite;
import org.apache.axis2.jaxws.description.builder.MethodDescriptionComposite;
import org.apache.axis2.jaxws.description.builder.ParameterDescriptionComposite;
import org.apache.axis2.jaxws.description.builder.WebMethodAnnot;
import org.apache.axis2.jaxws.description.builder.WebParamAnnot;
import org.apache.axis2.jaxws.description.builder.WebServiceAnnot;
import org.apache.axis2.jaxws.spi.ServiceDelegate;

/**
 * 
 */
public class DescriptionTestUtils {
    
    /*
     * ========================================================================
     * Test utility methods
     * ========================================================================
     */

    static public URL getWSDLURL() {
        URL wsdlURL = null;
        // Get the URL to the WSDL file.  Note that 'basedir' is setup by Maven
        String basedir = System.getProperty("basedir");
        String urlString = "file://localhost/" + basedir + "/test-resources/wsdl/WSDLTests.wsdl";
        try {
            wsdlURL = new URL(urlString);
        } catch (Exception e) {
            System.out.println("Caught exception creating WSDL URL :" + urlString + "; exception: " + e.toString());
        }
        return wsdlURL;
    }

    static public ServiceDelegate getServiceDelegate(Service service) {
        // Need to get to the private Service._delegate field in order to get to the ServiceDescription to test
        ServiceDelegate returnServiceDelegate = null;
        try {
            Field serviceDelgateField = service.getClass().getDeclaredField("_delegate");
            serviceDelgateField.setAccessible(true);
            returnServiceDelegate = (ServiceDelegate) serviceDelgateField.get(service);
        } catch (SecurityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return returnServiceDelegate;
    }

    static public DescriptionBuilderComposite buildDBCNoEndpointInterface() {
    	
    	//Create a WebServiceAnnot
        String WSName = "EchoServiceAnnotated";
        String WSTargetNamespace = "http://description.jaxws.axis2.apache.org/";
        String WSServiceName = "EchoServiceName";
        //String WSWsdlLocation = "http://EchoService/wsdl";
        String WSWsdlLocation = "";
        String WSEndpointInterface = "";
        String WSPortName = "EchoServiceAnnotatedPort";
        
    	WebServiceAnnot webServiceAnnot = 
			WebServiceAnnot.createWebServiceAnnotImpl(		
					WSName,
					WSTargetNamespace,
					WSServiceName,
					WSWsdlLocation,
					WSEndpointInterface,
					WSPortName);
    	
    	//Create a WebMethodAnnot
    	String operationName = "echoStringMethod";
        String action = "urn:EchoStringMethod";
        boolean exclude = false;
         
    	WebMethodAnnot webMethodAnnot = WebMethodAnnot.createWebMethodAnnotImpl();
    	webMethodAnnot.setOperationName(operationName);
    	webMethodAnnot.setAction(action);
    	webMethodAnnot.setExclude(exclude);
    	
    	//Create the WebParamAnnot
    	String WPName = "arg0";
        String WPPartName = "sku";
        String WPTargetNamespace = "http://description.jaxws.axis2.apache.org/";
    	Mode WPMode = Mode.IN;
        boolean WPHeader = true;
        
    	WebParamAnnot webParamAnnot = WebParamAnnot.createWebParamAnnotImpl();
    	webParamAnnot.setName(WPName);
    	webParamAnnot.setPartName(WPPartName);
    	webParamAnnot.setMode(WPMode);
    	webParamAnnot.setTargetNamespace(WPTargetNamespace);
    	webParamAnnot.setHeader(WPHeader);
    
    	//Build up the the DBC and all necessary composites 	
    	ParameterDescriptionComposite pdc = new ParameterDescriptionComposite(); 	
    	pdc.setParameterType("java.lang.String");
    	pdc.setWebParamAnnot(webParamAnnot);	

    	MethodDescriptionComposite mdc = new MethodDescriptionComposite();
       	mdc.setWebMethodAnnot(webMethodAnnot);
    	mdc.setMethodName(operationName);
    	mdc.addParameterDescriptionComposite(pdc,0);
       	
    	DescriptionBuilderComposite dbc = new DescriptionBuilderComposite(); 
    	dbc.setClassName("org.apache.axis2.samples.EchoServiceAnnotated");
    	dbc.setWebServiceAnnot(webServiceAnnot);
    	dbc.addMethodDescriptionComposite(mdc); 
    	
    	return dbc;
    }
    
}
