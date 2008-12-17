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

package org.apache.axis2.saaj;

import junit.framework.TestCase;
import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;

import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import java.io.IOException;
import java.net.URL;

/**
 * 
 */
public class SOAPConnectionTest extends TestCase {
    public void testClose() {
        try {
            SOAPConnection sCon = SOAPConnectionFactory.newInstance().createConnection();
            sCon.close();
        } catch (SOAPException e) {
            fail("Unexpected Exception " + e);
        }
    }

    public void testCloseTwice() {
        SOAPConnectionFactory soapConnectionFactory = null;
        try {
            soapConnectionFactory = SOAPConnectionFactory.newInstance();
        } catch (SOAPException e) {
            fail("Unexpected Exception " + e);
        }

        SOAPConnection sCon = null;
        try {
            sCon = soapConnectionFactory.createConnection();
            sCon.close();
        } catch (SOAPException e) {
            fail("Unexpected Exception " + e);
        }

        try {
            sCon.close();
            fail("Expected Exception did not occur");
        } catch (SOAPException e) {
            assertTrue(true);
        }
    }

    public void testCallOnCloseConnection() {
        SOAPConnectionFactory soapConnectionFactory = null;
        try {
            soapConnectionFactory = SOAPConnectionFactory.newInstance();
        } catch (SOAPException e) {
            fail("Unexpected Exception " + e);
        }

        SOAPConnection sCon = null;
        try {
            sCon = soapConnectionFactory.createConnection();
            sCon.close();
        } catch (SOAPException e) {
            fail("Unexpected Exception " + e);
        }

        try {
            sCon.call(null, new Object());
            fail("Expected Exception did not occur");
        } catch (SOAPException e) {
            assertTrue(true);
        }
    }


    public void testGet() {
    	if(isNetworkedResourceAvailable("http://java.sun.com/index.html")){
            try {
                SOAPConnectionFactory sf = new SOAPConnectionFactoryImpl();
                SOAPConnection con = sf.createConnection();
                //Create a valid non webservice endpoint for invoking HTTP-GET
                URL urlEndpoint = new URL("http", "java.sun.com", 80, "/index.html");
                //invoking HTTP-GET with a valid non webservice endpoint should throw a SOAPException
                SOAPMessage reply = con.get(urlEndpoint);
            } catch (Exception e) {
                assertTrue(e instanceof SOAPException);
            }
    	}else{
    		//If resource is not available online, do a mock test
    		assertTrue(true);
    	}
    }
    
    
    private boolean isNetworkedResourceAvailable(String url) {
        HttpClient client = new HttpClient();
        GetMethod method = new GetMethod(url);
        client.getHttpConnectionManager().getParams().setConnectionTimeout(1000);
        method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,
                                        new DefaultHttpMethodRetryHandler(1, false));

        try {
            int statusCode = client.executeMethod(method);
            if (statusCode != HttpStatus.SC_OK) {
                return false;
            }

        } catch (HttpException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            method.releaseConnection();
        }
        return true;
    }     
}
