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

package org.apache.axis2.jaxws.type_substitution.tests;

import org.apache.axis2.jaxws.TestLogger;
import org.apache.axis2.testutils.Axis2Server;
import org.junit.ClassRule;
import org.junit.Test;

import javax.xml.namespace.QName;
import jakarta.xml.soap.MessageFactory;
import jakarta.xml.soap.SOAPBody;
import jakarta.xml.soap.SOAPElement;
import jakarta.xml.soap.SOAPMessage;
import jakarta.xml.ws.Dispatch;
import jakarta.xml.ws.Service;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.dom.DOMSource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.util.Iterator;

public class TypeSubstitutionTests {
    @ClassRule
    public static final Axis2Server server = new Axis2Server("target/repo");

    private String NS = "http://apple.org";
    private QName XSI_TYPE = new QName("http://www.w3.org/2001/XMLSchema-instance", "type");

    private QName serviceName = new QName(NS, "AppleFinderService");
    private QName portName = new QName(NS, "AppleFinderPort");
    
    private String reqMsgStart = 
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
        "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\"><soap:Body>";
    
    private String reqMsgEnd = "</soap:Body></soap:Envelope>";
   
    private String GET_APPLES = "<ns2:getApples xmlns:ns2=\"" + NS + "\"/>";

    @Test
    public void testTypeSubstitution() throws Exception {
        Dispatch<SOAPMessage> dispatch = createDispatch();
             
        String msg = reqMsgStart + GET_APPLES + reqMsgEnd;
        MessageFactory factory = MessageFactory.newInstance();
        SOAPMessage request = factory.createMessage(null, 
                                                    new ByteArrayInputStream(msg.getBytes()));
            
        SOAPMessage response = dispatch.invoke(request);

        SOAPBody body = response.getSOAPBody();
	// AXIS2-6051, SOAPBody.toString no longer works
        // TestLogger.logger.debug(">> Response [" + body + "]");
	/*
	org.w3c.dom.Document document = body.extractContentAsDocument();
        Source source = new DOMSource(document);
        StringWriter out = new StringWriter(); 
        Result result = new StreamResult(out); 
        TransformerFactory tFactory = TransformerFactory.newInstance(); 
        Transformer transformer = tFactory.newTransformer(); 
        transformer.transform(source, result); 
        String bodyStr = out.toString();

        TestLogger.logger.debug(">> Response [" + bodyStr + "]");
	*/

        QName expectedXsiType1 = new QName(NS, "fuji");
        QName expectedXsiType2 = new QName(NS, "freyburg");

        Iterator iter;
        SOAPElement element;
        QName xsiType;

        iter = body.getChildElements(new QName(NS, "getApplesResponse"));
        assertTrue(iter.hasNext());
        
        element = (SOAPElement)iter.next();
	// {http://apple.org}getApplesResponse
	QName appleResponse = element.getElementQName();
        TestLogger.logger.debug("appleResponse: " + appleResponse);
	iter = null;
        iter = element.getChildElements(new QName(NS, "return"));

        // check value1
        assertTrue(iter.hasNext());
        SOAPElement returnElement = (SOAPElement)iter.next();
        // {http://apple.org}return	
        QName returnName = returnElement.getElementQName();
        TestLogger.logger.debug("returnName: " + returnName);
	// {http://apple.org}fuji
        xsiType = getXsiTypeAttribute(returnElement);
        if (xsiType != null) {
            TestLogger.logger.debug("found xsiType: " + xsiType + " , on getElementQName() : " + returnElement.getElementQName() + " , needs to match: " + expectedXsiType1);
        }
        assertEquals("xsi:type 1", expectedXsiType1, xsiType);
        TestLogger.logger.debug("xsi:type 1 passed");
        
        // check value2
        assertTrue(iter.hasNext());
        element = (SOAPElement)iter.next();
        xsiType = getXsiTypeAttribute(element);
        assertEquals("xsi:type 2", expectedXsiType2, xsiType);
        TestLogger.logger.debug("xsi:type 2 passed");
    }
    
    private QName getXsiTypeAttribute(SOAPElement element) throws Exception {
        String value = element.getAttributeValue(XSI_TYPE);
        QName xsiType = null;
        if (value != null) {
            TestLogger.logger.debug("getXsiTypeAttribute() found value: " + value);
            int pos = value.indexOf(":");
            if (pos != -1) {
                String prefix = value.substring(0, pos);
                String localName = value.substring(pos+1);
                String namespace = element.getNamespaceURI(prefix);
                xsiType = new QName(namespace, localName, prefix);
            } else {
	        // AXIS2-6051, with jakarta this is now the default
		// and the namespace is required
                xsiType = new QName(NS, value);
            }
        }
        return xsiType;
    }

    private Dispatch<SOAPMessage> createDispatch() throws Exception {
        Service svc = Service.create(serviceName);
        svc.addPort(portName, null, server.getEndpoint("AppleFinderService.AppleFinderPort"));
        Dispatch<SOAPMessage> dispatch = 
            svc.createDispatch(portName, SOAPMessage.class, Service.Mode.MESSAGE);
        return dispatch;
    }

}
