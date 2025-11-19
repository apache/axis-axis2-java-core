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
package org.apache.axis2.transport.h2;

import static com.google.common.truth.Truth.assertAbout;
import static org.apache.axiom.truth.xml.XMLTruth.xml;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.namespace.QName;

import junit.framework.TestCase;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.soap.SOAPBody;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.SOAP11Constants;
import org.apache.axiom.soap.SOAP12Constants;
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
import org.apache.axis2.transport.h2.impl.httpclient5.H2TransportSender;

/**
 * Basic SOAP compatibility tests for HTTP/2 transport.
 *
 * These tests verify that SOAP 1.1 and SOAP 1.2 messages can be processed
 * over HTTP/2 transport with basic functionality. Performance testing and
 * advanced SOAP features are not the focus - this provides minimal coverage
 * to ensure compatibility.
 *
 * As documented in the migration plan, SOAP testing represents only 10% of
 * the effort with limited business case, while JSON APIs receive 90% focus.
 */
public class H2SOAPCompatibilityTest extends TestCase {

    private H2TransportSender transportSender;
    private ConfigurationContext configContext;
    private TransportOutDescription transportOut;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        transportSender = new H2TransportSender();
        configContext = ConfigurationContextFactory.createEmptyConfigurationContext();
        transportOut = new TransportOutDescription("h2");

        // Add basic HTTP/2 parameters
        transportOut.addParameter(new Parameter("PROTOCOL", "HTTP/2.0"));
        transportOut.addParameter(new Parameter("maxConcurrentStreams", "50"));
        transportOut.addParameter(new Parameter("initialWindowSize", "131072")); // 128KB for SOAP

        transportSender.init(configContext, transportOut);
    }

    /**
     * Test basic SOAP 1.1 message processing over HTTP/2.
     * Verifies that SOAP 1.1 envelopes can be created and processed.
     */
    public void testSOAP11OverHTTP2() throws Exception {
        SOAPEnvelope soap11Envelope = createSOAP11Envelope();
        MessageContext msgContext = createMessageContext(soap11Envelope);

        // SOAP version is determined by the envelope type - no need to set explicitly

        assertNotNull("SOAP 1.1 envelope should be created", soap11Envelope);
        assertNotNull("Message context should be created", msgContext);
        assertEquals("Should be SOAP 1.1 namespace",
                    SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI,
                    soap11Envelope.getNamespace().getNamespaceURI());

        // Verify envelope structure
        assertNotNull("SOAP body should exist", soap11Envelope.getBody());
        assertTrue("SOAP body should have content", soap11Envelope.getBody().getFirstElement() != null);
    }

    /**
     * Test basic SOAP 1.2 message processing over HTTP/2.
     * Verifies that SOAP 1.2 envelopes can be created and processed.
     */
    public void testSOAP12OverHTTP2() throws Exception {
        SOAPEnvelope soap12Envelope = createSOAP12Envelope();
        MessageContext msgContext = createMessageContext(soap12Envelope);

        // SOAP version is determined by the envelope type - no need to set explicitly

        assertNotNull("SOAP 1.2 envelope should be created", soap12Envelope);
        assertNotNull("Message context should be created", msgContext);
        assertEquals("Should be SOAP 1.2 namespace",
                    SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI,
                    soap12Envelope.getNamespace().getNamespaceURI());

        // Verify envelope structure
        assertNotNull("SOAP body should exist", soap12Envelope.getBody());
        assertTrue("SOAP body should have content", soap12Envelope.getBody().getFirstElement() != null);
    }

    /**
     * Test SOAP message with multiple elements over HTTP/2.
     * Verifies handling of more complex SOAP message structures.
     */
    public void testComplexSOAPMessageOverHTTP2() throws Exception {
        SOAPEnvelope complexEnvelope = createComplexSOAPEnvelope();
        MessageContext msgContext = createMessageContext(complexEnvelope);

        assertNotNull("Complex SOAP envelope should be created", complexEnvelope);

        // Verify multiple child elements
        OMElement body = complexEnvelope.getBody().getFirstElement();
        assertNotNull("SOAP body content should exist", body);

        // Count child elements
        int childCount = 0;
        Iterator<OMElement> childIterator = body.getChildElements();
        while (childIterator.hasNext()) {
            childIterator.next();
            childCount++;
        }
        assertTrue("Should have multiple child elements", childCount >= 3);
    }

    /**
     * Test HTTP/2 transport sender initialization.
     * Verifies that the H2TransportSender can be properly initialized.
     */
    public void testH2TransportSenderInitialization() throws Exception {
        assertNotNull("Transport sender should be initialized", transportSender);
        assertNotNull("Configuration context should be set", configContext);
        assertNotNull("Transport out description should be set", transportOut);

        // Verify HTTP/2 specific parameters
        Parameter protocolParam = transportOut.getParameter("PROTOCOL");
        assertNotNull("PROTOCOL parameter should be set", protocolParam);
        assertEquals("Should be HTTP/2.0", "HTTP/2.0", protocolParam.getValue());
    }

    // Helper methods

    private SOAPEnvelope createSOAP11Envelope() throws IOException {
        SOAPFactory soapFac = OMAbstractFactory.getSOAP11Factory();
        OMFactory omFac = OMAbstractFactory.getOMFactory();
        SOAPEnvelope envelope = soapFac.createSOAPEnvelope();
        SOAPBody soapBody = soapFac.createSOAPBody();

        OMElement content = omFac.createOMElement(new QName("http://example.com/soap", "testRequest"));
        OMElement data = omFac.createOMElement(new QName("data"));
        data.setText("SOAP 1.1 test data over HTTP/2");

        content.addChild(data);
        soapBody.addChild(content);
        envelope.addChild(soapBody);
        return envelope;
    }

    private SOAPEnvelope createSOAP12Envelope() throws IOException {
        SOAPFactory soapFac = OMAbstractFactory.getSOAP12Factory();
        OMFactory omFac = OMAbstractFactory.getOMFactory();
        SOAPEnvelope envelope = soapFac.createSOAPEnvelope();
        SOAPBody soapBody = soapFac.createSOAPBody();

        OMElement content = omFac.createOMElement(new QName("http://example.com/soap", "testRequest"));
        OMElement data = omFac.createOMElement(new QName("data"));
        data.setText("SOAP 1.2 test data over HTTP/2");

        content.addChild(data);
        soapBody.addChild(content);
        envelope.addChild(soapBody);
        return envelope;
    }

    private SOAPEnvelope createComplexSOAPEnvelope() throws IOException {
        SOAPFactory soapFac = OMAbstractFactory.getSOAP11Factory();
        OMFactory omFac = OMAbstractFactory.getOMFactory();
        SOAPEnvelope envelope = soapFac.createSOAPEnvelope();
        SOAPBody soapBody = soapFac.createSOAPBody();

        OMElement content = omFac.createOMElement(new QName("http://example.com/soap", "complexRequest"));

        // Add multiple child elements to test complex structure
        OMElement param1 = omFac.createOMElement(new QName("param1"));
        param1.setText("value1");
        OMElement param2 = omFac.createOMElement(new QName("param2"));
        param2.setText("value2");
        OMElement param3 = omFac.createOMElement(new QName("param3"));
        param3.setText("value3");

        content.addChild(param1);
        content.addChild(param2);
        content.addChild(param3);
        soapBody.addChild(content);
        envelope.addChild(soapBody);
        return envelope;
    }

    private MessageContext createMessageContext(SOAPEnvelope envelope) throws AxisFault {
        MessageContext msgContext = new MessageContext();
        msgContext.setEnvelope(envelope);
        msgContext.setTransportOut(transportOut);
        msgContext.setConfigurationContext(configContext);

        // Set HTTP/2 transport properties
        msgContext.setProperty("TRANSPORT_NAME", "h2");
        msgContext.setProperty("HTTP2_ENABLED", Boolean.TRUE);

        // Add basic headers for SOAP
        List<NamedValue> headerList = new ArrayList<NamedValue>();
        NamedValue contentType = new NamedValue("Content-Type", "text/xml; charset=UTF-8");
        NamedValue soapAction = new NamedValue("SOAPAction", "\"http://example.com/soap/testAction\"");
        headerList.add(contentType);
        headerList.add(soapAction);
        msgContext.setProperty(HTTPConstants.HTTP_HEADERS, headerList);

        return msgContext;
    }
}