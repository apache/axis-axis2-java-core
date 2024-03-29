
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

package org.apache.axis2.jaxws.sample.doclitbare.sei;

import javax.xml.namespace.QName;
import jakarta.xml.ws.Service;
import jakarta.xml.ws.WebEndpoint;
import jakarta.xml.ws.WebServiceClient;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * This class was generated by the JAXWS SI.
 * JAX-WS RI 2.0_01-b15-fcs
 * Generated source version: 2.0
 * 
 */
@WebServiceClient(name = "BareDocLitService", targetNamespace = "http://org.test.sample.doclitbare", wsdlLocation = "proxy_doclit.wsdl")
public class BareDocLitService
    extends Service
{

    private final static URL BAREDOCLITSERVICE_WSDL_LOCATION;

    private static String wsdlLocation="/src/test/java/org/apache/axis2/jaxws/sample/doclitbare/META-INF/doclitbare.wsdl";
    static {
        URL url = null;
        try {
        	try{
	        	String baseDir = new File(System.getProperty("basedir",".")).getCanonicalPath();
	        	wsdlLocation = new File(baseDir + wsdlLocation).getAbsolutePath();
        	}catch(Exception e){
        		e.printStackTrace();
        	}
        	File file = new File(wsdlLocation);
        	url = file.toURI().toURL();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        BAREDOCLITSERVICE_WSDL_LOCATION = url;
    }

    public BareDocLitService(URL wsdlLocation, QName serviceName) {
        super(wsdlLocation, serviceName);
    }

    public BareDocLitService() {
        super(BAREDOCLITSERVICE_WSDL_LOCATION, new QName("http://doclitbare.sample.test.org", "BareDocLitService"));
    }

    /**
     * 
     * @return
     *     returns DocLitBarePortType
     */
    @WebEndpoint(name = "BareDocLitPort")
    public DocLitBarePortType getBareDocLitPort() {
        return (DocLitBarePortType)super.getPort(new QName("http://doclitbare.sample.test.org", "BareDocLitPort"), DocLitBarePortType.class);
    }

}
