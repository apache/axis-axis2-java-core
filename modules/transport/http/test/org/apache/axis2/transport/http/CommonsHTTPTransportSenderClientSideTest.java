package org.apache.axis2.transport.http;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.transport.OutTransportInfo;
import org.apache.axis2.transport.http.impl.httpclient3.HTTPClient3TransportSender;
import org.apache.axis2.transport.http.mock.MockAxisHttpResponse;
import org.apache.axis2.transport.http.mock.MockHTTPResponse;
import org.apache.axis2.transport.http.mock.server.AbstractHTTPServerTest;
import org.apache.axis2.transport.http.mock.server.BasicHttpServer;
import org.apache.http.ProtocolVersion;
import org.apache.http.RequestLine;
import org.apache.http.message.BasicRequestLine;

public class CommonsHTTPTransportSenderClientSideTest extends AbstractHTTPServerTest {

    public void testInvokeWithEPR() throws Exception {
        int port = getBasicHttpServer().getPort();
        RequestLine line = new BasicRequestLine("", "", new ProtocolVersion("http", 1, 0));
        MockHTTPResponse httpResponse = new MockAxisHttpResponse(line);
        getBasicHttpServer().setResponseTemplate(BasicHttpServer.RESPONSE_HTTP_OK_LOOP_BACK);

        // We only interested on HTTP message sent to the server side by this
        // client hence ignore the processing of response at client side.
        try {
            httpResponse = (MockAxisHttpResponse) CommonsHTTPTransportSenderTest.configAndRun(
                    httpResponse, (OutTransportInfo) httpResponse, "http://localhost:" + port,  new HTTPClient3TransportSender());

        } catch (Exception e) {
        }
        assertEquals("Not the expected HTTP Method", "POST", getHTTPMethod());
        assertEquals("Not the expected Header value", "application/xml",
                getHeaders().get("Content-Type"));
        assertEquals("Not the expected Header value", "custom-value",
                getHeaders().get("Custom-header"));
        assertEquals("Not the expected body content", getEnvelope().toString()
                .replace("utf", "UTF"), getStringContent());
    }
     
    /*
     * Tests that HTTP connections are properly released when the server returns
     * a 404 error. This is a regression test for AXIS2-5093.
     */
    public void testConnectionReleaseWith404() throws Exception {
        int port = getBasicHttpServer().getPort();
        getBasicHttpServer().setResponseTemplate(BasicHttpServer.RESPONSE_HTTP_404);
        // If connections are not properly released then we will end up with a
        // ConnectionPoolTimeoutException here.

        ConfigurationContext configurationContext = ConfigurationContextFactory
                .createConfigurationContextFromURIs(
                        CommonsHTTPTransportSenderClientSideTest.class.getResource("axis2.xml"),
                        null);
        ServiceClient serviceClient = new ServiceClient(configurationContext, null);
        Options options = serviceClient.getOptions();
        options.setTo(new EndpointReference("http://localhost:" + port + "//nonexisting"));
        OMElement request = OMAbstractFactory.getOMFactory().createOMElement(
                new QName("urn:test", "test"));
        // If connections are not properly released then we will end up with a
        // ConnectionPoolTimeoutException here.
        for (int i = 0; i < 200; i++) {
            try {
                serviceClient.sendReceive(request);
            } catch (AxisFault ex) {
                // Check that this is a 404 error
                assertNull(ex.getCause());
                assertTrue(ex.getMessage().contains("404"));
            }
            serviceClient.cleanupTransport();
        }

    }

}
