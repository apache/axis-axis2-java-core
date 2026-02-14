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

import static com.google.common.truth.Truth.assertAbout;
import static org.apache.axiom.truth.xml.XMLTruth.xml;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import jakarta.servlet.http.HttpServletResponse;
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
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.NamedValue;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.description.TransportOutDescription;
import org.apache.axis2.engine.Handler.InvocationResponse;
import org.apache.axis2.kernel.http.HTTPConstants;
import org.apache.axis2.kernel.OutTransportInfo;
import org.apache.axis2.kernel.TransportSender;
import org.apache.axis2.transport.http.mock.MockAxisHttpResponse;
import org.apache.axis2.transport.http.mock.MockHttpServletResponse;
import org.apache.axis2.transport.http.mock.MockHTTPResponse;
import org.apache.hc.core5.http.ProtocolVersion;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpVersion;
import org.apache.hc.core5.http.Method;
import org.apache.hc.core5.http.message.RequestLine;

public abstract class HTTPTransportSenderTest extends TestCase  {
    
    protected abstract TransportSender getTransportSender();

    public void testInvokeWithServletBasedOutTransportInfo() throws Exception {
        MockHTTPResponse httpResponse = new MockHttpServletResponse();
        ServletBasedOutTransportInfo info = new ServletBasedOutTransportInfo(
                (HttpServletResponse) httpResponse);
        SOAPEnvelope envelope = getEnvelope();
        httpResponse = configAndRun(httpResponse, info, null, getTransportSender());

        final Header[] headers = httpResponse.getHeaders();
	final Map<String,String> headerMap = new HashMap<>();
	if (headers != null) {
            for (final Header header: headers) {
                headerMap.put(header.getName(), header.getValue());
            }
	}

        assertEquals("Not the expected Header value", "application/xml", headerMap.get("Content-Type"));
        assertEquals("Not the expected Header value", "custom-value", headerMap.get("Custom-header"));
        assertAbout(xml())
                .that(new String(httpResponse.getByteArrayOutputStream().toByteArray()))
                .hasSameContentAs(envelope.toString());
    }
    
    public void testInvokeWithAxisHttpResponseImpl() throws Exception {
        RequestLine line = new RequestLine(Method.POST.name(), "", HttpVersion.HTTP_1_1);
        MockHTTPResponse httpResponse = new MockAxisHttpResponse(line);
        SOAPEnvelope envelope = getEnvelope();
        httpResponse = (MockAxisHttpResponse) configAndRun(httpResponse,
                (OutTransportInfo) httpResponse, null, getTransportSender());

        final Header[] headers = httpResponse.getHeaders();
	final Map<String,String> headerMap = new HashMap<>();
        for (final Header header: headers) {
            headerMap.put(header.getName(), header.getValue());
        }

        assertEquals("Not the expected Header value", "application/xml", headerMap.get("Content-Type"));
        assertEquals("Not the expected Header value", "custom-value", headerMap.get("Custom-header"));
        assertAbout(xml())
                .that(new String(httpResponse.getByteArrayOutputStream().toByteArray()))
                .hasSameContentAs(envelope.toString());
    }

    public void testInit() throws AxisFault {
        ConfigurationContext confContext = ConfigurationContextFactory
                .createEmptyConfigurationContext();
        TransportOutDescription transportOut = new TransportOutDescription("http");
        TransportSender sender = getTransportSender();
        sender.init(confContext, transportOut);

    }

    public static MockHTTPResponse configAndRun(MockHTTPResponse outResponse,
            OutTransportInfo outTransportInfo, String epr, TransportSender sender) throws Exception {
        MockHTTPResponse response = outResponse;
        ConfigurationContext confContext = ConfigurationContextFactory
                .createEmptyConfigurationContext();
        TransportOutDescription transportOut = new TransportOutDescription("http");
        Parameter param = new Parameter(HTTPConstants.OMIT_SOAP_12_ACTION, false);
        SOAPEnvelope envelope = getEnvelope();
        MessageContext msgContext = new MessageContext();
        
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
        List<NamedValue> headerList = new ArrayList<NamedValue>();
        NamedValue header1 = new NamedValue("Content-Type", "application/xml");
        NamedValue header2 = new NamedValue("Custom-header", "custom-value");
        headerList.add(header1);
        headerList.add(header2);
        msgContext.setProperty(HTTPConstants.HTTP_HEADERS, headerList);
        sender.init(confContext, transportOut);
        InvocationResponse inResponse = sender.invoke(msgContext);
        assertEquals("Not the expected InvocationResponse", InvocationResponse.CONTINUE, inResponse);
        return response;

    }
    
    /**
     * AXIS2-3879: Fault with a user-specified custom status code should use that code,
     * not the default 500.
     */
    public void testFaultWithCustomStatusCode() throws Exception {
        MockHttpServletResponse httpResponse = new MockHttpServletResponse();
        ServletBasedOutTransportInfo info = new ServletBasedOutTransportInfo(httpResponse);
        configAndRunFault(httpResponse, info, "503", getTransportSender());
        assertEquals("Custom status code should be respected", 503, httpResponse.getStatus());
    }

    /**
     * AXIS2-3879: Fault without a custom status code should default to 500.
     */
    public void testFaultDefaultsTo500() throws Exception {
        MockHttpServletResponse httpResponse = new MockHttpServletResponse();
        ServletBasedOutTransportInfo info = new ServletBasedOutTransportInfo(httpResponse);
        configAndRunFault(httpResponse, info, null, getTransportSender());
        assertEquals("Default fault status should be 500", 500, httpResponse.getStatus());
    }

    /**
     * AXIS2-4146: User-set status code 400 should not be overwritten to 500.
     */
    public void testFaultWithStatus400NotOverwritten() throws Exception {
        MockHttpServletResponse httpResponse = new MockHttpServletResponse();
        ServletBasedOutTransportInfo info = new ServletBasedOutTransportInfo(httpResponse);
        configAndRunFault(httpResponse, info, "400", getTransportSender());
        assertEquals("Status 400 should not be overwritten to 500", 400, httpResponse.getStatus());
    }

    private static void configAndRunFault(MockHttpServletResponse outResponse,
            OutTransportInfo outTransportInfo, String customStatus,
            TransportSender sender) throws Exception {
        ConfigurationContext confContext = ConfigurationContextFactory
                .createEmptyConfigurationContext();
        TransportOutDescription transportOut = new TransportOutDescription("http");
        Parameter param = new Parameter(HTTPConstants.OMIT_SOAP_12_ACTION, false);
        SOAPEnvelope envelope = getFaultEnvelope();
        MessageContext msgContext = new MessageContext();

        transportOut.addParameter(param);
        msgContext.setEnvelope(envelope);
        msgContext.setProperty(MessageContext.TRANSPORT_OUT,
                outResponse.getByteArrayOutputStream());
        msgContext.setProperty(Constants.OUT_TRANSPORT_INFO, outTransportInfo);
        msgContext.setTransportOut(transportOut);
        msgContext.setConfigurationContext(confContext);
        if (customStatus != null) {
            msgContext.setProperty(Constants.HTTP_RESPONSE_STATE, customStatus);
        }
        sender.init(confContext, transportOut);
        sender.invoke(msgContext);
    }

    static SOAPEnvelope getFaultEnvelope() {
        SOAPFactory soapFac = OMAbstractFactory.getSOAP11Factory();
        SOAPEnvelope envelope = soapFac.getDefaultFaultEnvelope();
        envelope.getBody().getFault().getReason().setText("test fault");
        return envelope;
    }

    static SOAPEnvelope getEnvelope() throws IOException {
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
