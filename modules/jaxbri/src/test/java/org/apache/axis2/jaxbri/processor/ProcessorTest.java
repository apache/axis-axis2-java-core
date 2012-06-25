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
package org.apache.axis2.jaxbri.processor;

import static org.junit.Assert.assertEquals;

import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMXMLBuilderFactory;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.jaxbri.processor.client.Processor;
import org.apache.axis2.jaxbri.processor.client.ProcessorStub;
import org.apache.axis2.jaxbri.processor.data.ReplyMessage;
import org.apache.axis2.jaxbri.processor.data.RequestMessage;
import org.apache.axis2.testutils.UtilServer;
import org.custommonkey.xmlunit.XMLAssert;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;

/**
 * Regression test for AXIS2-5147.
 */
public class ProcessorTest {
    private static final String ENDPOINT = "http://127.0.0.1:" + UtilServer.TESTING_PORT + "/axis2/services/Processor";
    
    @BeforeClass
    public static void startServer() throws Exception {
        UtilServer.start(System.getProperty("basedir", ".") + "/target/repo/processor");
        AxisConfiguration axisConfiguration = UtilServer.getConfigurationContext().getAxisConfiguration();
        AxisService service = axisConfiguration.getService("Processor");
        service.getParameter(Constants.SERVICE_CLASS).setValue(ProcessorImpl.class.getName());
    }
    
    @AfterClass
    public static void stopServer() throws Exception {
        UtilServer.stop();
    }
    
    @Test
    public void testStub() throws Exception {
        Processor stub = new ProcessorStub(UtilServer.getConfigurationContext(), ENDPOINT);
        RequestMessage request = new RequestMessage();
        request.setRequestID("A3TN39840");
        request.setRequestData("DATA");
        ReplyMessage reply = stub.runTransaction(request);
        assertEquals("A3TN39840.1", reply.getReplyID());
        assertEquals("PROCESSED", reply.getReplyData());
    }
    
    @Test
    public void testServiceClient() throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document response = db.newDocument();
        InputStream in = ProcessorTest.class.getResourceAsStream("request.xml");
        try {
            OMElement request = OMXMLBuilderFactory.createOMBuilder(in).getDocumentElement();
            ServiceClient client = new ServiceClient(UtilServer.getConfigurationContext(), null);
            Options options = client.getOptions();
            options.setTo(new EndpointReference(ENDPOINT));
            try {
                OMElement omResponse = client.sendReceive(request);
                TransformerFactory.newInstance().newTransformer().transform(omResponse.getSAXSource(false), new DOMResult(response));
            } finally {
                client.cleanupTransport();
                client.cleanup();
            }
        } finally {
            in.close();
        }
        in = ProcessorTest.class.getResourceAsStream("response.xml");
        Document expectedResponse;
        try {
            expectedResponse = db.parse(in);
        } finally {
            in.close();
        }
        XMLAssert.assertXMLEqual(expectedResponse, response);
    }
}
