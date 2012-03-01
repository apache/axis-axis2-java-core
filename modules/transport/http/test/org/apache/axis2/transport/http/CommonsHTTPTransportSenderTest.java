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
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.NamedValue;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.description.TransportOutDescription;
import org.apache.axis2.engine.Handler.InvocationResponse;
import org.apache.axis2.transport.OutTransportInfo;
import org.apache.axis2.transport.TransportSender;
import org.apache.axis2.transport.http.mock.MockAxisHttpResponse;
import org.apache.axis2.transport.http.mock.MockHttpServletResponse;
import org.apache.axis2.transport.http.mock.MockHTTPResponse;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.http.ProtocolVersion;
import org.apache.http.RequestLine;
import org.apache.http.message.BasicRequestLine;

public abstract class CommonsHTTPTransportSenderTest extends TestCase  {
    
    protected abstract TransportSender getTransportSender();

    public void testInvokeWithServletBasedOutTransportInfo() throws Exception {
        MockHTTPResponse httpResponse = new MockHttpServletResponse();
        ServletBasedOutTransportInfo info = new ServletBasedOutTransportInfo(
                (HttpServletResponse) httpResponse);
        SOAPEnvelope envelope = getEnvelope();
        httpResponse = configAndRun(httpResponse, info, null, getTransportSender());

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
                (OutTransportInfo) httpResponse, null, getTransportSender());

        assertEquals("Not the expected Header value", "application/xml", httpResponse.getHeaders()
                .get("Content-Type"));
        assertEquals("Not the expected Header value", "custom-value", httpResponse.getHeaders()
                .get("Custom-header"));
        assertEquals("Not the expected body content", envelope.toString().replace("utf", "UTF"),
                new String(httpResponse.getByteArrayOutputStream().toByteArray()));
    }

    public void testCleanup() throws AxisFault {
        TransportSender sender = getTransportSender();
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
    
    static SOAPEnvelope getEnvelope() throws IOException, MessagingException {
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
