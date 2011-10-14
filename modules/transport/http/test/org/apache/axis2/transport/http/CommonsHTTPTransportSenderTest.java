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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletResponse;
import javax.xml.namespace.QName;

import junit.framework.TestCase;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.soap.SOAPBody;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.description.TransportOutDescription;
import org.apache.axis2.engine.Handler.InvocationResponse;
import org.apache.axis2.transport.OutTransportInfo;
import org.apache.axis2.transport.TransportSender;
import org.apache.axis2.transport.http.mock.MockAxisHttpResponse;
import org.apache.axis2.transport.http.mock.MockHttpServletResponse;
import org.apache.axis2.transport.http.mock.MockHTTPResponse;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.http.ProtocolVersion;
import org.apache.http.RequestLine;
import org.apache.http.message.BasicRequestLine;
import org.mortbay.http.SocketListener;
import org.mortbay.jetty.Server;

public class CommonsHTTPTransportSenderTest extends TestCase  {

    /**
     * Tests that HTTP connections are properly released when the server returns a 404 error. This
     * is a regression test for AXIS2-5093.
     * 
     * @throws Exception
     */
    
    public void testConnectionReleaseWith404() throws Exception {
        // Create a Jetty server instance without any contexts. It will always return HTTP 404.
        Server server = new Server();
        SocketListener listener = new SocketListener();
        server.addListener(listener);
        server.start();
        try {
            ConfigurationContext configurationContext =
                    ConfigurationContextFactory.createConfigurationContextFromURIs(
                            CommonsHTTPTransportSenderTest.class.getResource("axis2.xml"), null);
            ServiceClient serviceClient = new ServiceClient(configurationContext, null);
            Options options = serviceClient.getOptions();
            options.setTo(new EndpointReference("http://localhost:" + listener.getPort() + "/nonexisting"));
            OMElement request = OMAbstractFactory.getOMFactory().createOMElement(new QName("urn:test", "test"));
            // If connections are not properly released then we will end up with a
            // ConnectionPoolTimeoutException here.
            for (int i=0; i<200; i++) {
                try {
                    serviceClient.sendReceive(request);
                } catch (AxisFault ex) {
                    // Check that this is a 404 error
                    assertNull(ex.getCause());
                    assertTrue(ex.getMessage().contains("404"));
                }
                serviceClient.cleanupTransport();
            }
        } finally {
            server.stop();
        }
    }    

    public void testInvokeWithServletBasedOutTransportInfo() throws Exception {
        MockHTTPResponse httpResponse = new MockHttpServletResponse();
        ServletBasedOutTransportInfo info = new ServletBasedOutTransportInfo(
                (HttpServletResponse) httpResponse);
        SOAPEnvelope envelope = getEnvelope();
        httpResponse = configAndRun(httpResponse, info, null);

        assertEquals("Not the expected Header value", "application/xml", httpResponse.getHeaders()
                .get("Content-Type"));
        assertEquals("Not the expected Header value", "custom-value", httpResponse.getHeaders()
                .get("Custom-header"));
        assertEquals("Not the expected body content", envelope.toString().replace("utf", "UTF"),
                new String(httpResponse.getByteArrayOutputStream().toByteArray()));
    }
    
    public void testInvokeWithAxisHttpResponseImpl() throws Exception {
        RequestLine line = new BasicRequestLine("", "", new ProtocolVersion("http", 1, 0));
        MockHTTPResponse httpResponse = new MockAxisHttpResponse(line);
        SOAPEnvelope envelope = getEnvelope();
        httpResponse = (MockAxisHttpResponse) configAndRun(httpResponse,
                (OutTransportInfo) httpResponse, null);

        assertEquals("Not the expected Header value", "application/xml", httpResponse.getHeaders()
                .get("Content-Type"));
        assertEquals("Not the expected Header value", "custom-value", httpResponse.getHeaders()
                .get("Custom-header"));
        assertEquals("Not the expected body content", envelope.toString().replace("utf", "UTF"),
                new String(httpResponse.getByteArrayOutputStream().toByteArray()));
    }
    
    public void testInvokeWithEPR() throws Exception {
        RequestLine line = new BasicRequestLine("", "", new ProtocolVersion("http", 1, 0));
        MockHTTPResponse httpResponse = new MockAxisHttpResponse(line);
        /*
         * TODO - This method used to test client side support of
         * CommonsHTTPTransportSender. At the moment this will return Connection
         * refused exception because there is no server side support given. It
         * is required to complete this test by adding a HTTP server and verify
         * data in the server side.
         */
        try {
            httpResponse = (MockAxisHttpResponse) configAndRun(httpResponse,
                    (OutTransportInfo) httpResponse, "http://localhost:8080");
            fail("Should raise org.apache.axis2.AxisFault: Connection refused");
        } catch (AxisFault e) {
        }

        // assertEquals("Not the expected Header value", "application/xml",
        // httpResponse.getHeaders().get("Content-Type"));
        // assertEquals("Not the expected Header value", "custom-value",
        // httpResponse.getHeaders().get("Custom-header"));
        // assertEquals("Not the expected body content",
        // envelope.toString().replace("utf", "UTF"), new String(httpResponse
        // .getByteArrayOutputStream().toByteArray()));

    }
    
    public void testCleanup() throws AxisFault {
        TransportSender sender = new CommonsHTTPTransportSender();
        MessageContext msgContext = new MessageContext();
        HttpMethod httpMethod = new GetMethod();
        msgContext.setProperty(HTTPConstants.HTTP_METHOD, httpMethod);
        assertNotNull("HttpMethod can not be null",
                msgContext.getProperty(HTTPConstants.HTTP_METHOD));
        sender.cleanup(msgContext);
        assertNull("HttpMethod should be null", msgContext.getProperty(HTTPConstants.HTTP_METHOD));

    }
    
    public void testInit() throws AxisFault {
        ConfigurationContext confContext = ConfigurationContextFactory
                .createEmptyConfigurationContext();
        TransportOutDescription transportOut = new TransportOutDescription("http");
        TransportSender sender = new CommonsHTTPTransportSender();
        sender.init(confContext, transportOut);

    }
    
    private MockHTTPResponse configAndRun(MockHTTPResponse outResponse,
            OutTransportInfo outTransportInfo, String epr) throws Exception {
        MockHTTPResponse response = outResponse;
        ConfigurationContext confContext = ConfigurationContextFactory
                .createEmptyConfigurationContext();
        TransportOutDescription transportOut = new TransportOutDescription("http");
        Parameter param = new Parameter(HTTPConstants.OMIT_SOAP_12_ACTION, false);
        SOAPEnvelope envelope = getEnvelope();
        MessageContext msgContext = new MessageContext();

        TransportSender sender = new CommonsHTTPTransportSender();
        transportOut.addParameter(param);
        // create dummy SOAPEnvelope
        msgContext.setEnvelope(envelope);
        msgContext.setProperty(MessageContext.TRANSPORT_OUT,
                ((MockHTTPResponse) response).getByteArrayOutputStream());
        msgContext.setProperty(Constants.OUT_TRANSPORT_INFO, outTransportInfo);
        msgContext.setTransportOut(transportOut);
        msgContext.setConfigurationContext(confContext);
        if (epr != null) {
            msgContext.setProperty(Constants.Configuration.TRANSPORT_URL, epr);
        }
        // set two Headers for testing
        List<Header> headerList = new ArrayList<Header>();
        Header header1 = new Header("Content-Type", "application/xml");
        Header header2 = new Header("Custom-header", "custom-value");
        headerList.add(header1);
        headerList.add(header2);
        msgContext.setProperty(HTTPConstants.HTTP_HEADERS, headerList);
        sender.init(confContext, transportOut);
        InvocationResponse inResponse = sender.invoke(msgContext);
        assertEquals("Not the expected InvocationResponse", InvocationResponse.CONTINUE, inResponse);
        return response;

    }
    
    private SOAPEnvelope getEnvelope() throws IOException, MessagingException {
        SOAPFactory soapFac = OMAbstractFactory.getSOAP11Factory();
        OMFactory omFac = OMAbstractFactory.getOMFactory();
        SOAPEnvelope enp = soapFac.createSOAPEnvelope();
        SOAPBody sopaBody = soapFac.createSOAPBody();

        OMElement content = omFac.createOMElement(new QName("message"));
        OMElement data1 = omFac.createOMElement(new QName("part"));
        data1.setText("sample data");

        content.addChild(data1);
        sopaBody.addChild(content);
        enp.addChild(sopaBody);
        return enp;
    }
}
