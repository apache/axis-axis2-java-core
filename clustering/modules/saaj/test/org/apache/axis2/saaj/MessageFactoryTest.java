/*                                                                             
 * Copyright 2004,2005 The Apache Software Foundation.                         
 *                                                                             
 * Licensed under the Apache License, Version 2.0 (the "License");             
 * you may not use this file except in compliance with the License.            
 * You may obtain a copy of the License at                                     
 *                                                                             
 *      http://www.apache.org/licenses/LICENSE-2.0                             
 *                                                                             
 * Unless required by applicable law or agreed to in writing, software         
 * distributed under the License is distributed on an "AS IS" BASIS,           
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.    
 * See the License for the specific language governing permissions and         
 * limitations under the License.                                              
 */
package org.apache.axis2.saaj;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPMessage;

import org.custommonkey.xmlunit.XMLTestCase;

/**
 * 
 */
public class MessageFactoryTest extends XMLTestCase {
    private MessageFactory mf = null;

    protected void setUp() throws Exception {
        mf = MessageFactory.newInstance();
    }

    public void testCreateMessage() {
        try {
            ByteArrayOutputStream baos1 = new ByteArrayOutputStream();
            ByteArrayOutputStream baos2 = new ByteArrayOutputStream();

            SOAPMessage msg1 = mf.createMessage();
            msg1.writeTo(baos1);

            MimeHeaders headers = new MimeHeaders();
            headers.addHeader("Content-Type", "text/xml");

            // Create SOAPMessage from MessageFactory object using InputStream
            SOAPMessage msg2 = mf.createMessage(headers,
                                                new ByteArrayInputStream(baos1.toString().getBytes()));
            if (msg2 == null) {
                fail();
            }
            msg2.writeTo(baos2);

            if (!(baos1.toString().equals(baos2.toString()))) {
                fail();
            }
        } catch (Exception e) {
            fail();
        }
    }

    public void testCreateMessage2() {
        try {
            ByteArrayOutputStream baos1 = new ByteArrayOutputStream();
            ByteArrayOutputStream baos2 = new ByteArrayOutputStream();

            final String XML_STRING =
                    "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                    "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\"\n" +
                    "                   xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\"\n" +
                    "                   xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n" +
                    " <soapenv:Header>\n" +
                    "  <shw:Hello xmlns:shw=\"http://www.jcommerce.net/soap/ns/SOAPHelloWorld\">\n" +
                    "    <shw:Myname>Tony</shw:Myname>\n" +
                    "  </shw:Hello>\n" +
                    " </soapenv:Header>\n" +
                    " <soapenv:Body>\n" +
                    "<shw:Address shw:t='test' xmlns:shw=\"http://www.jcommerce.net/soap/ns/SOAPHelloWorld\">\n" +
                    "<shw:City>GENT</shw:City>\n" +
                    "</shw:Address>\n" +
                    "</soapenv:Body>\n" +
                    "</soapenv:Envelope>";

            MimeHeaders headers = new MimeHeaders();
            headers.addHeader("Content-Type", "text/xml");

            SOAPMessage msg1 =
                    mf.createMessage(headers, new ByteArrayInputStream(XML_STRING.getBytes()));
            msg1.writeTo(baos1);

            // Create SOAPMessage from MessageFactory object using InputStream
            SOAPMessage msg2 = mf.createMessage(headers,
                                                new ByteArrayInputStream(baos1.toString().getBytes()));
            if (msg2 == null) {
                fail();
            }
            msg2.writeTo(baos2);

            this.assertXMLEqual(baos1.toString(), baos2.toString());
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    public void _testMessageFactory3() {
        MimeHeaders headers = new MimeHeaders();
        headers.addHeader("Content-Type",
                          "multipart/related; boundary=MIMEBoundaryurn:uuid:F02ECC18873CFB73E211412748909307; type=\"application/xop+xml\"; start=\"<0.urn:uuid:F02ECC18873CFB73E211412748909308@apache.org>\"; start-info=\"text/xml\"; charset=UTF-16");

        try {
            FileInputStream fis = new FileInputStream("test-resources" + File.separator + "message.bin");
            SOAPMessage msg1 = mf.createMessage(headers, fis);
        } catch (Exception e) {
            e.printStackTrace();
            fail("Unexpected Exception : " + e);
        }
    }
    
    public void testNewInstane(){
    	try {
    		// Create a Dynamic MessageFactory object
    		System.out.println("Create Dynamic MessageFactory object");
    		MessageFactory mf = MessageFactory.newInstance(
    				SOAPConstants.DYNAMIC_SOAP_PROTOCOL);
    		if(mf == null) {
    			System.out.println("MessageFactory.newInstance(" +
    			"DYNAMIC_SOAP_PROTOCOL) returned null");
    		} else if(!(mf instanceof MessageFactory)) {
    			System.out.println("MessageFactory.newInstance(" +
    			"DYNAMIC_SOAP_PROTOCOL) did not return MessageFactory object");
    		} 

    		MessageFactory mf2 = MessageFactory.newInstance();
    		ByteArrayOutputStream baos1 = new ByteArrayOutputStream();
    		ByteArrayOutputStream baos2 = new ByteArrayOutputStream();

    		System.out.println("Create SOAPMessage msg1 using createMessage()");
    		SOAPMessage msg1 = mf2.createMessage();
    		msg1.writeTo(baos1);

    		MimeHeaders headers = new MimeHeaders();
    		headers.addHeader("Content-Type", "text/xml");

    		// Create SOAPMessage from MessageFactory object using InputStream
    		System.out.println("Create SOAPMessage msg2 using SOAPMessage msg1" +
    		" as the InputStream");
    		System.out.println("Create SOAPMessage msg2 using createMessage(" +
    		"MimeHeaders, InputStream)");
    		SOAPMessage msg2 = mf.createMessage(headers, 
    				new ByteArrayInputStream(baos1.toByteArray()));
    		if(msg2 == null) {
    			System.out.println("Could not create SOAPMessage (msg = null)");
    		} else if(!(msg2 instanceof SOAPMessage)) {
    			fail("Could not create SOAPMessage (msg != SOAPMessage)");
    		}
    		msg2.writeTo(baos2);
    		System.out.println("Compare msg1 and msg2 (should be equal)");
    		if(!(baos1.toString().equals(baos2.toString()))) {
    			System.out.println("msg1 = " + baos1.toString());
    			System.out.println("msg2 = " + baos2.toString());
    			fail("msg1 and msg2 are not equal (they should be)");
    		}
    	} catch(Exception e) {
    		System.out.println("Exception: " + e);
    	}
    }
    
}
