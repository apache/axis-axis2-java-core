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

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import org.apache.axis2.jaxws.spi.ServiceDelegate;

import junit.framework.TestCase;

/**
 * Directly test the Description classes built with a WSDL file.
 */
public class WSDLDescriptionTests extends TestCase {
    
    private static boolean setupDone = false;
    private static Service service;
    private static ServiceDelegate serviceDelegate;
    private static ServiceDescription serviceDescription;


    
    protected void setUp() {
        if (!setupDone) {
            setupDone = true;
            String namespaceURI= "http://ws.apache.org/axis2/tests";
            String localPart = "EchoService";
            service = Service.create(getWSDLURL(), new QName(namespaceURI, localPart));
            serviceDelegate = getServiceDelegate(service);
            serviceDescription = serviceDelegate.getServiceDescription();
        }
    }
    
    /* 
     * ========================================================================
     * ServiceDescription Tests
     * ========================================================================
     */
    public void testValidServiceGetEndpoint() {
        QName validPortQname = new QName("http://ws.apache.org/axis2/tests", "EchoPort");
        EndpointDescription endpointDescription = serviceDescription.getEndpointDescription(validPortQname);
        assertNotNull("EndpointDescription should be found", endpointDescription);
    }

    public void testInvalidLocalpartServiceGetEndpoint() {
        QName validPortQname = new QName("http://ws.apache.org/axis2/tests", "InvalidEchoPort");
        EndpointDescription endpointDescription = serviceDescription.getEndpointDescription(validPortQname);
        assertNull("EndpointDescription should not be found", endpointDescription);
    }

    public void testInvalidNamespaceServiceGetEndpoint() {
        QName validPortQname = new QName("http://ws.apache.org/axis2/tests/INVALID", "EchoPort");
        EndpointDescription endpointDescription = serviceDescription.getEndpointDescription(validPortQname);
        assertNull("EndpointDescription should not be found", endpointDescription);
    }
    
    /*
     * ========================================================================
     * Test utility methods
     * ========================================================================
     */

    private URL getWSDLURL() {
        URL wsdlURL = null;
        // Get the URL to the WSDL file.  Note that 'basedir' is setup by Maven
        String basedir = System.getProperty("basedir");
        String urlString = "file://localhost/" + basedir + "/test-resources/wsdl/WSDLTests.wsdl";
        try {
            wsdlURL = new URL(urlString);
        } catch (Exception e) {
            fail("Caught exception creating WSDL URL :" + urlString + "; exception: " + e.toString());
        }
        return wsdlURL;
    }

    private ServiceDelegate getServiceDelegate(Service service) {
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

}
