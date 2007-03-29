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

import java.net.URL;

import javax.wsdl.Definition;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;

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
        return getWSDLURL("WSDLTests.wsdl");
        
    }
    static public URL getWSDLURL(String wsdlFileName) {
        URL wsdlURL = null;
        // Get the URL to the WSDL file.  Note that 'basedir' is setup by Maven
        String basedir = System.getProperty("basedir");
        String urlString = "file://localhost/" + basedir + "/test-resources/wsdl/" + wsdlFileName;
        try {
            wsdlURL = new URL(urlString);
        } catch (Exception e) {
            System.out.println("Caught exception creating WSDL URL :" + urlString + "; exception: " + e.toString());
        }
        return wsdlURL;
    }
    
    static Definition createWSDLDefinition(URL wsdlURL) {
        Definition wsdlDefinition = null;
        try {
            WSDLFactory factory = WSDLFactory.newInstance();
            WSDLReader reader = factory.newWSDLReader();
            wsdlDefinition = reader.readWSDL(wsdlURL.toString());
        }
        catch (Exception e) {
            System.out.println("*** ERROR ***: Caught exception trying to create WSDL Definition: " + e);
            e.printStackTrace();
        }

        return wsdlDefinition;
    }
}
