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

package org.apache.axis2.transport.http;

import org.apache.axis2.Constants;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.OperationContext;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.kernel.http.HTTPConstants;
import org.apache.axis2.transport.http.mock.server.AbstractHTTPServerTest;
import org.apache.axis2.transport.http.mock.server.BasicHttpServer;

import jakarta.ws.rs.core.HttpHeaders;

import static com.google.common.truth.Truth.assertAbout;
import static org.apache.axiom.truth.xml.XMLTruth.xml;

import java.io.IOException;
import java.net.URL;

/**
 * The Class HTTPSenderTest.
 */
public abstract class HTTPSenderTest extends AbstractHTTPServerTest {

    private HTTPSender httpSender;

    protected abstract HTTPSender getHTTPSender();

    /**
     * Send via http.
     * 
     * @param httpMethod
     *            the http method
     * @param soapAction
     *            the soap action
     * @param address
     *            the address
     * @param rest
     *            the rest
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    protected MessageContext sendViaHTTP(String httpMethod, String soapAction, String address, boolean rest)
            throws IOException {
        httpSender = getHTTPSender();
        ServiceContext serviceContext = new ServiceContext();
        MessageContext msgContext = new MessageContext();
        msgContext.setServiceContext(serviceContext);
        ConfigurationContext configContext = ConfigurationContextFactory
                .createEmptyConfigurationContext();
        OperationContext opContext = new OperationContext();
        opContext.setParent(serviceContext);

        msgContext.setConfigurationContext(configContext);
        msgContext.setEnvelope(getEnvelope());
        msgContext.setDoingREST(rest);
        msgContext.setProperty(Constants.Configuration.HTTP_METHOD, httpMethod);
        msgContext.setOperationContext(opContext);
        URL url = new URL(address);
        httpSender.send(msgContext, url, soapAction);
        return msgContext;
    }

    /**
     * Test send via get.
     * 
     * @throws Exception
     *             the exception
     */
    public void testSendViaGet() throws Exception {
        int port = getBasicHttpServer().getPort();
        sendViaHTTP(Constants.Configuration.HTTP_METHOD_GET, "urn:getService", "http://localhost:"
                + port + "/getService", true);
        assertEquals("Not the expected HTTP Method", Constants.Configuration.HTTP_METHOD_GET,
                getHTTPMethod());
        assertEquals("Not the expected content", "/getService?part=sample%20data",
                getStringContent());
        assertNull("Not the expected HTTP Header value", getHeaders().get("SOAPAction"));
        assertEquals("Not the expected HTTP Header value",
                "application/x-www-form-urlencoded;action=\"urn:getService\";",
                getHeaders().get(HttpHeaders.CONTENT_TYPE));
        assertEquals("Not the expected HTTP Header value", "localhost:" + port,
                getHeaders().get(HttpHeaders.HOST));
        assertEquals("Not the expected HTTP Header value", "Axis2",
                getHeaders().get(HttpHeaders.USER_AGENT));
    }

    /**
     * Test send via post.
     * 
     * @throws Exception
     *             the exception
     */
    public void testSendViaPost() throws Exception {
        // test with REST payload
        int port = getBasicHttpServer().getPort();
        sendViaHTTP(Constants.Configuration.HTTP_METHOD_POST, "urn:postService",
                "http://localhost:" + port + "/postService", true);
        assertEquals("Not the expected HTTP Method", Constants.Configuration.HTTP_METHOD_POST,
                getHTTPMethod());
        assertEquals("Not the expected content", getEnvelope().getFirstElement().getFirstElement()
                .toString(), getStringContent());
        assertNull("Not the expected HTTP Header value", getHeaders().get("SOAPAction"));
        assertEquals("Not the expected HTTP Header value", "application/xml",
                getHeaders().get(HttpHeaders.CONTENT_TYPE));
        assertEquals("Not the expected HTTP Header value", "localhost:" + port,
                getHeaders().get(HttpHeaders.HOST));
        assertEquals("Not the expected HTTP Header value", "Axis2",
                getHeaders().get(HttpHeaders.USER_AGENT));

        // test with SOAP payload.
        sendViaHTTP(Constants.Configuration.HTTP_METHOD_POST, "urn:postService",
                "http://localhost:" + port + "/postService", false);
        assertEquals("Not the expected HTTP Method", Constants.Configuration.HTTP_METHOD_POST,
                getHTTPMethod());
        assertAbout(xml()).that(getStringContent()).hasSameContentAs(getEnvelope().toString());
        assertEquals("Not the expected HTTP Header value", "urn:postService",
                getHeaders().get("SOAPAction").replace("\"", ""));
        assertEquals("Not the expected HTTP Header value", "text/xml",
                getHeaders().get(HttpHeaders.CONTENT_TYPE));
        assertEquals("Not the expected HTTP Header value", "localhost:" + port,
                getHeaders().get(HttpHeaders.HOST));
        assertEquals("Not the expected HTTP Header value", "Axis2",
                getHeaders().get(HttpHeaders.USER_AGENT));
    }




    /**
     * Test send via put.
     * 
     * @throws Exception
     *             the exception
     */
    public void testSendViaPut() throws Exception {
        // test with REST payload
        int port = getBasicHttpServer().getPort();
        sendViaHTTP(Constants.Configuration.HTTP_METHOD_PUT, "urn:putService", "http://localhost:"
                + port + "/putService", true);
        assertEquals("Not the expected HTTP Method", Constants.Configuration.HTTP_METHOD_PUT,
                getHTTPMethod());
        assertEquals("Not the expected content", getEnvelope().getFirstElement().getFirstElement()
                .toString(), getStringContent());
        assertNull("Not the expected HTTP Header value", getHeaders().get("SOAPAction"));
        assertEquals("Not the expected HTTP Header value", "application/xml",
                getHeaders().get(HttpHeaders.CONTENT_TYPE));
        assertEquals("Not the expected HTTP Header value", "localhost:" + port,
                getHeaders().get(HttpHeaders.HOST));
        assertEquals("Not the expected HTTP Header value", "Axis2",
                getHeaders().get(HttpHeaders.USER_AGENT));

        // test with SOAP payload.
        sendViaHTTP(Constants.Configuration.HTTP_METHOD_PUT, "urn:putService", "http://localhost:"
                + port + "/putService", false);
        assertEquals("Not the expected HTTP Method", Constants.Configuration.HTTP_METHOD_PUT,
                getHTTPMethod());
        assertAbout(xml()).that(getStringContent()).hasSameContentAs(getEnvelope().toString());
        assertEquals("Not the expected HTTP Header value", "urn:putService",
                getHeaders().get("SOAPAction").replace("\"", ""));
        assertEquals("Not the expected HTTP Header value", "text/xml",
                getHeaders().get(HttpHeaders.CONTENT_TYPE));
        assertEquals("Not the expected HTTP Header value", "localhost:" + port,
                getHeaders().get(HttpHeaders.HOST));
        assertEquals("Not the expected HTTP Header value", "Axis2",
                getHeaders().get(HttpHeaders.USER_AGENT));
    }

    /**
     * Test send via delete.
     * 
     * @throws Exception
     *             the exception
     */
    public void testSendViaDelete() throws Exception {
        // test with REST payload
        int port = getBasicHttpServer().getPort();
        sendViaHTTP(Constants.Configuration.HTTP_METHOD_DELETE, "urn:deleteService",
                "http://localhost:" + port + "/deleteService", true);
        assertEquals("Not the expected HTTP Method", Constants.Configuration.HTTP_METHOD_DELETE,
                getHTTPMethod());
        assertEquals("Not the expected content", "/deleteService?part=sample%20data",
                getStringContent());
        assertEquals("Not the expected HTTP Header value",
                "application/x-www-form-urlencoded;action=\"urn:deleteService\";", getHeaders()
                        .get(HttpHeaders.CONTENT_TYPE));
        assertEquals("Not the expected HTTP Header value", "localhost:" + port,
                getHeaders().get(HttpHeaders.HOST));
        assertEquals("Not the expected HTTP Header value", "Axis2",
                getHeaders().get(HttpHeaders.USER_AGENT));

    }

    /**
     * Test send via head.
     * 
     * @throws Exception
     *             the exception
     */
    // This is test is bullshit; if we send a HEAD request, we shouldn't expect the method to be POST
    public void _testSendViaHead() throws Exception {

        int port = getBasicHttpServer().getPort();
        sendViaHTTP(Constants.Configuration.HTTP_METHOD_HEAD, "urn:deleteService",
                "http://localhost:" + port + "/deleteService", true);
        assertEquals("Not the expected HTTP Method", Constants.Configuration.HTTP_METHOD_POST,
                getHTTPMethod());
        assertEquals("Not the expected content", getEnvelope().getFirstElement().getFirstElement()
                .toString(), getStringContent());
        assertEquals("Not the expected HTTP Header value", "application/xml",
                getHeaders().get(HttpHeaders.CONTENT_TYPE));
        assertEquals("Not the expected HTTP Header value", "localhost:" + port,
                getHeaders().get(HttpHeaders.HOST));
        assertEquals("Not the expected HTTP Header value", "Axis2",
                getHeaders().get(HttpHeaders.USER_AGENT));

    }

    /**
     * Test send nohttp method.
     * 
     * @throws Exception
     *             the exception
     */
    public void testSendNOHTTPMethod() throws Exception {

        int port = getBasicHttpServer().getPort();
        sendViaHTTP(null, "urn:noService", "http://localhost:" + port + "/noService", true);
        assertEquals("Not the expected HTTP Method", Constants.Configuration.HTTP_METHOD_POST,
                getHTTPMethod());
        assertEquals("Not the expected content", getEnvelope().getFirstElement().getFirstElement()
                .toString(), getStringContent());
        assertNull("Not the expected HTTP Header value", getHeaders().get("SOAPAction"));
        assertEquals("Not the expected HTTP Header value", "application/xml",
                getHeaders().get(HttpHeaders.CONTENT_TYPE));
        assertEquals("Not the expected HTTP Header value", "localhost:" + port,
                getHeaders().get(HttpHeaders.HOST));
        assertEquals("Not the expected HTTP Header value", "Axis2",
                getHeaders().get(HttpHeaders.USER_AGENT));

        sendViaHTTP(null, "urn:noService", "http://localhost:" + port + "/noService", false);
        assertAbout(xml()).that(getStringContent()).hasSameContentAs(getEnvelope().toString());
        assertEquals("Not the expected HTTP Header value", "urn:noService",
                getHeaders().get("SOAPAction").replace("\"", ""));
        assertEquals("Not the expected HTTP Header value", "text/xml",
                getHeaders().get(HttpHeaders.CONTENT_TYPE));
        assertEquals("Not the expected HTTP Header value", "localhost:" + port,
                getHeaders().get(HttpHeaders.HOST));
        assertEquals("Not the expected HTTP Header value", "Axis2",
                getHeaders().get(HttpHeaders.USER_AGENT));
    }
    public void testHandleResponseHTTPStatusCode200() throws Exception {
        httpSender = getHTTPSender();
        int port = getBasicHttpServer().getPort();
        getBasicHttpServer().setResponseTemplate(BasicHttpServer.RESPONSE_HTTP_200);
        sendViaHTTP(Constants.Configuration.HTTP_METHOD_POST, "urn:postService",
                "http://localhost:" + port + "/postService", true);
    }
    
    public void testHandleResponseHTTPStatusCode201() throws Exception {
        httpSender = getHTTPSender();
        int port = getBasicHttpServer().getPort();
        getBasicHttpServer().setResponseTemplate(BasicHttpServer.RESPONSE_HTTP_201);
        sendViaHTTP(Constants.Configuration.HTTP_METHOD_POST, "urn:postService",
                "http://localhost:" + port + "/postService", true);
    }
    
    public void testHandleResponseHTTPStatusCode202() throws Exception {
        httpSender = getHTTPSender();
        int port = getBasicHttpServer().getPort();
        getBasicHttpServer().setResponseTemplate(BasicHttpServer.RESPONSE_HTTP_202);
        sendViaHTTP(Constants.Configuration.HTTP_METHOD_POST, "urn:postService",
                "http://localhost:" + port + "/postService", true);
    }
    
    public void testHandleResponseHTTPStatusCode400() throws Exception {
        httpSender = getHTTPSender();
        int port = getBasicHttpServer().getPort();
        getBasicHttpServer().setResponseTemplate(BasicHttpServer.RESPONSE_HTTP_400);
        sendViaHTTP(Constants.Configuration.HTTP_METHOD_POST, "urn:postService",
                "http://localhost:" + port + "/postService", true);
    }
    
    public void testHandleResponseHTTPStatusCode500() throws Exception {
        httpSender = getHTTPSender();
        int port = getBasicHttpServer().getPort();
        getBasicHttpServer().setResponseTemplate(BasicHttpServer.RESPONSE_HTTP_500);
        sendViaHTTP(Constants.Configuration.HTTP_METHOD_POST, "urn:postService",
                "http://localhost:" + port + "/postService", true);
    }

    public void testCookiesAreObtainedAfterRequest() throws Exception {
        httpSender = getHTTPSender();
        int port = getBasicHttpServer().getPort();
        getBasicHttpServer().setResponseTemplate(BasicHttpServer.RESPONSE_HTTP_COOKIE);
        final MessageContext mc = sendViaHTTP(Constants.Configuration.HTTP_METHOD_POST, "urn:postService",
                "http://localhost:" + port + "/postService", true);

        assertEquals("Cookie was not set", "JSESSIONID=abcde12345",
                mc.getProperty(HTTPConstants.COOKIE_STRING));
    }
    

}
