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

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.mail.MessagingException;
import javax.xml.namespace.QName;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMOutputFormat;
import org.apache.axiom.soap.SOAPBody;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;

import junit.framework.TestCase;

/**
 * The Class MultipartFormDataFormatterTest.
 */
public class MultipartFormDataFormatterTest extends TestCase {

    private MultipartFormDataFormatter formatter;

    private MessageContext messageContext;

    protected void setUp() throws Exception {
        super.setUp();
        formatter = new MultipartFormDataFormatter();
        messageContext = new MessageContext();
        SOAPEnvelope enp = getEnvelope();
        messageContext.setEnvelope(enp);
    }

    private SOAPEnvelope getEnvelope() throws IOException, MessagingException {
        SOAPFactory soapFac = OMAbstractFactory.getSOAP11Factory();
        OMFactory omFac = OMAbstractFactory.getOMFactory();
        SOAPEnvelope enp = soapFac.createSOAPEnvelope();
        SOAPBody sopaBody = soapFac.createSOAPBody();

        OMElement content = omFac.createOMElement(new QName("message"));
        OMElement data1 = omFac.createOMElement(new QName("part1"));
        data1.setText("sample data part 1");

        OMElement data2 = omFac.createOMElement(new QName("part2"));
        data2.setText("sample data part 2");

        content.addChild(data1);
        content.addChild(data2);
        sopaBody.addChild(content);
        enp.addChild(sopaBody);
        return enp;
    }

    public void testGetBytes() throws AxisFault {

        OMOutputFormat omOutput = new OMOutputFormat();
        String boundary = omOutput.getMimeBoundary();
        byte[] bytes = formatter.getBytes(messageContext, omOutput);
        String message = new String(bytes);
        
        assertNotNull("bytes can not be null", bytes);
        assertTrue("Can not find the content", message.contains(boundary));
        assertTrue("Can not find the content",
                message.contains("Content-Disposition: form-data; name=\"part1\""));
        assertTrue("Can not find the content",
                message.contains("Content-Disposition: form-data; name=\"part2\""));
        assertTrue("Can not find the content",
                message.contains("Content-Type: text/plain; charset=US-ASCII"));
        //assertTrue("Can not find the content", message.contains("Content-Transfer-Encoding: 8bit"));
        assertTrue("Can not find the content", message.contains("sample data part 1"));
        assertTrue("Can not find the content", message.contains("sample data part 2"));

    }

    public void testWriteTo() throws AxisFault {

        OMOutputFormat omOutput = new OMOutputFormat();
        String boundary = omOutput.getMimeBoundary();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        formatter.writeTo(messageContext, omOutput, out, false);
        String message = new String(out.toByteArray());

        assertTrue("Can not find the content", message.contains(boundary));
        assertTrue("Can not find the content",
                message.contains("Content-Disposition: form-data; name=\"part1\""));
        assertTrue("Can not find the content",
                message.contains("Content-Disposition: form-data; name=\"part2\""));
        assertTrue("Can not find the content",
                message.contains("Content-Type: text/plain; charset=US-ASCII"));
        //assertTrue("Can not find the content", message.contains("Content-Transfer-Encoding: 8bit"));
        assertTrue("Can not find the content", message.contains("sample data part 1"));
        assertTrue("Can not find the content", message.contains("sample data part 2"));
    }

    public void testGetContentType() {
        OMOutputFormat omOutput = new OMOutputFormat();
        String boundary = omOutput.getMimeBoundary();
        String type = formatter.getContentType(messageContext, omOutput, null);

        assertTrue("Can not find the content", type.startsWith("multipart/form-data;"));
        assertTrue("Can not find the content", type.endsWith((boundary)));

    }

}
