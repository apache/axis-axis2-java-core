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

import junit.framework.Assert;

import org.eclipse.jetty.ee9.nested.AbstractHandler;
import org.eclipse.jetty.ee9.nested.ContextHandler;
import org.eclipse.jetty.ee9.nested.Handler;
import org.eclipse.jetty.ee9.nested.HandlerCollection;
import org.eclipse.jetty.ee9.nested.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.server.Server;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.xml.soap.MessageFactory;
import jakarta.xml.soap.SOAPBody;
import jakarta.xml.soap.SOAPConnection;
import jakarta.xml.soap.SOAPConnectionFactory;
import jakarta.xml.soap.SOAPConstants;
import jakarta.xml.soap.SOAPElement;
import jakarta.xml.soap.SOAPException;
import jakarta.xml.soap.SOAPMessage;
import java.io.IOException;
import java.net.URL;

/**
 * 
 */
@RunWith(SAAJTestRunner.class)
public class SOAPConnectionTest extends Assert {
    @Validated @Test
    public void testClose() {
        try {
            SOAPConnection sCon = SOAPConnectionFactory.newInstance().createConnection();
            sCon.close();
        } catch (SOAPException e) {
            fail("Unexpected Exception " + e);
        }
    }

    @Validated @Test
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

    @Validated @Test
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


    /* FIXME: AXIS2-6051, why is the error below happening with Jetty 12?
     *
     * java.lang.ClassNotFoundException: org.apache.axis2.jaxws.framework.JAXWSServiceBuilderExtension
     * Just adding axis2-jaxws is a circular reference.
     *
    @Validated @Test
    public void testGet() throws Exception {
        Server server = new Server(0);
        Handler handler = new AbstractHandler() {
            @Override
            public void handle(String target, Request baseRequest, HttpServletRequest request,
                    HttpServletResponse response) throws IOException, ServletException {
                try {
                    SOAPMessage message = MessageFactory.newInstance().createMessage();
                    SOAPBody body = message.getSOAPBody();
                    body.addChildElement("root");
                    response.setContentType(SOAPConstants.SOAP_1_1_CONTENT_TYPE);
                    message.writeTo(response.getOutputStream());
                    baseRequest.setHandled(true);
                } catch (SOAPException ex) {
                    throw new RuntimeException("Failed to generate SOAP message", ex);
                }
            }
        };

        ContextHandler context = new ContextHandler(server);
        HandlerCollection ee9HandlerCollection = new HandlerCollection();
        context.setHandler(ee9HandlerCollection);
        server.start();
        try {
            SOAPConnectionFactory sf = new SOAPConnectionFactoryImpl();
            SOAPConnection con = sf.createConnection();
            SOAPMessage reply = con.get(new URL(server.getURI().toURL(), "/test"));
            SOAPElement bodyElement = (SOAPElement)reply.getSOAPBody().getChildElements().next();
            assertEquals("root", bodyElement.getLocalName());
        } finally {
            server.stop();
        }
    }
    */
}
