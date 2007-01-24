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

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.Iterator;

import javax.activation.DataHandler;
import javax.xml.namespace.QName;
import javax.xml.soap.AttachmentPart;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPBodyElement;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPFault;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;

import junit.framework.TestCase;

/**
 * 
 */
public class SOAPMessageTest extends TestCase {
    private SOAPMessage msg;

    protected void setUp() throws Exception {
        msg = MessageFactory.newInstance().createMessage();
    }

    public void testSaveRequired() {
        try {
            assertTrue("Save Required is False",msg.saveRequired());
        } catch (Exception e) {
            fail("Unexpected Exception : " + e);
        }
    }

    public void testSaveRequired2() {
        try {
            msg.saveChanges();
            assertFalse("Save Required is True",msg.saveRequired());
        } catch (Exception e) {
            fail("Unexpected Exception : " + e);
        }
    }
    
    public void _testGetAttachmentByHref() {
    	String NS_PREFIX="mypre";
    	String NS_URI="http://myuri.org/";

    	try {
    		System.out.println("Create SOAP message from message factory");

    		// Message creation takes care of creating the SOAPPart - a
    		// required part of the message as per the SOAP 1.1 spec.
    		System.out.println("Get SOAP Part");
    		SOAPPart sp = msg.getSOAPPart();

    		// Retrieve the envelope from the soap part to start building
    		// the soap message.
    		System.out.println("Get SOAP Envelope");
    		SOAPEnvelope envelope = sp.getEnvelope();

    		// Create a soap header from the envelope.
    		System.out.println("Create SOAP Header");
    		SOAPHeader hdr = envelope.getHeader();

    		// Create a soap body from the envelope.
    		System.out.println("Create SOAP Body");
    		SOAPBody bdy = envelope.getBody();

    		// Add a soap body element
    		System.out.println("Add SOAP BodyElement Body1");
    		SOAPBodyElement sbe1 = bdy.addBodyElement(
    				envelope.createName("Body1", NS_PREFIX, NS_URI));

    		// Add a child element
    		System.out.println("Add ChildElement TheGifAttachment");
    		sbe1.addChildElement(envelope.createName(
    				"TheGifAttachment", NS_PREFIX, NS_URI));
    		sbe1.setAttribute("href", "cid:THEGIF");

    		// Add another soap body element
    		System.out.println("Add SOAP BodyElement Body2");
    		SOAPBodyElement sbe2 = bdy.addBodyElement(
    				envelope.createName("Body2", NS_PREFIX, NS_URI));

    		// Add a child element
    		System.out.println("Add ChildElement TheXmlAttachment");
    		sbe2.addChildElement(envelope.createName(
    				"TheXmlAttachment", NS_PREFIX, NS_URI));
    		sbe2.setAttribute("href", "cid:THEXML");

    		System.out.println("Add various mime type attachments to SOAP message");
    		URL url1 = new URL("http://my.uri.org");
    		URL url2 = new URL("http://my.uri.org");
    		URL url3 = new URL("http://my.uri.org");
    		URL url4 = new URL("http://my.uri.org");
    		URL url5 = new URL("http://my.uri.org");

    		System.out.println("Create SOAP Attachment (XML document)");
    		System.out.println("URL1=" + url1);
    		AttachmentPart ap1 = msg.createAttachmentPart(new DataHandler(url1));

    		System.out.println("Create SOAP Attachment (GIF image)");
    		System.out.println("URL2=" + url2);
    		AttachmentPart ap2 = msg.createAttachmentPart(new DataHandler(url2));

    		System.out.println("Create SOAP Attachment (Plain text)");
    		System.out.println("URL3=" + url3);
    		AttachmentPart ap3 = msg.createAttachmentPart(new DataHandler(url3));

    		System.out.println("Create SOAP Attachment (HTML document)");
    		System.out.println("URL4=" + url4);
    		AttachmentPart ap4 = msg.createAttachmentPart(new DataHandler(url4));

    		System.out.println("Create SOAP Attachment (JPEG image)");
    		System.out.println("URL5=" + url5);
    		AttachmentPart ap5 = msg.createAttachmentPart(new DataHandler(url5));

    		ap1.setContentType("text/xml");
    		ap1.setContentId("<THEXML>");
    		ap2.setContentType("image/gif");
    		ap2.setContentId("<THEGIF>");
    		ap3.setContentType("text/plain");
    		ap3.setContentId("<THEPLAIN>");
    		ap4.setContentType("text/html");
    		ap4.setContentId("<THEHTML>");
    		ap5.setContentType("image/jpeg");
    		ap5.setContentId("<THEJPEG>");

    		// Add the attachments to the message.
    		System.out.println(
    		"Add SOAP Attachment (XML document) to SOAP message");
    		msg.addAttachmentPart(ap1);
    		System.out.println(
    				"Add SOAP Attachment (GIF image) to SOAP message");
    		msg.addAttachmentPart(ap2);
    		System.out.println(
    				"Add SOAP Attachment (Plain text) to SOAP message");
    		msg.addAttachmentPart(ap3);
    		System.out.println(
    				"Add SOAP Attachment (HTML document) to SOAP message");
    		msg.addAttachmentPart(ap4);
    		System.out.println(
    				"Add SOAP Attachment (JPEG image) to SOAP message");
    		msg.addAttachmentPart(ap5);
    		msg.saveChanges();
    		System.out.println("Done creating SOAP message");

    		System.out.println("Retrieve attachment with href=cid:THEGIF");
    		AttachmentPart myap = msg.getAttachment(sbe1);
    		if(myap == null) {
    			System.out.println("Returned null (unexpected)");
    		} else if(!myap.getContentType().equals("image/gif")) {
    			System.out.println("Wrong attachment was returned: Got Content-Type of "
    					+myap.getContentType()+", Expected Content-Type of image/gif");
    		} else
    			System.out.println("Correct attachment was returned");

    		System.out.println("Retrieve attachment with href=cid:THEXML");
    		myap = msg.getAttachment(sbe2);
    		if(myap == null) {
    			System.out.println("Returned null (unexpected)");
    		} else if(!myap.getContentType().equals("text/xml")) {
    			System.out.println("Wrong attachment was returned: Got Content-Type of "
    					+myap.getContentType()+", Expected Content-Type of text/xml");
    		} else
    			System.out.println("Correct attachment was returned");

    		System.out.println("Retrieve attachment with href=cid:boo-hoo (expect null)");
    		QName myqname = new QName("boo-hoo");
    		SOAPElement myse = SOAPFactoryImpl.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL).createElement(myqname);
    		myse.addTextNode("<theBooHooAttachment href=\"cid:boo-hoo\"/>");
    		myap = msg.getAttachment(myse);
    		if(myap == null)
    			System.out.println("Returned null (expected)");
    		else {
    			System.out.println("Returned non null (unexpected)");
    		}

    	} catch (Exception e) {
    		fail("Unexpected Exception : " + e);
    	}
    }
    
    
    
    //TODO : sumedha complete
    public void testRemoveAttachements(){
    	Iterator iterator = null;
        AttachmentPart ap1 = null;
        AttachmentPart ap2 = null;
        AttachmentPart ap3 = null;
    	
    	try 
    	{
    		MessageFactory fac = MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL);
    		//MessageFactory fac = MessageFactory.newInstance();
    		SOAPMessage msg = fac.createMessage();
    		SOAPPart soapPart = msg.getSOAPPart();
    		SOAPEnvelope envelope = soapPart.getEnvelope();
    		SOAPBody body = envelope.getBody();
    		SOAPFault sf = body.addFault();

    		
    		InputStream in1 = new FileInputStream(new File("test-resources" + File.separator + "attach.xml"));
    		ap1 = msg.createAttachmentPart(in1, "text/xml");
            msg.addAttachmentPart(ap1);

    		InputStream in2 = new FileInputStream(new File("test-resources" + File.separator + "axis2.xml"));
    		ap2 = msg.createAttachmentPart(in2, "text/xml");
            msg.addAttachmentPart(ap2);

    		InputStream in3 = new FileInputStream(new File("test-resources" + File.separator + "axis2.xml"));
    		ap3 = msg.createAttachmentPart(in3, "text/plain");
            msg.addAttachmentPart(ap3);
    		
    		
    		System.out.println("get all attachments");
    		iterator = msg.getAttachments();

    		int cnt = 0;
    		while(iterator.hasNext()){
    			cnt++;
    			iterator.next();
    		}

    		System.out.println("number of attachments: " + cnt);

    		if (cnt != 3) {
    			System.out.println("only 3 attachments was added, count not correct");
    		}else{
    			System.out.println("3 attachments exist as expected");
    		}

    		System.out.println("remove just the text/xml attachments which are 2");
    		MimeHeaders mhs = new MimeHeaders();
    		mhs.addHeader("Content-Type", "text/xml");
    		msg.removeAttachments(mhs);

    		System.out.println("get all attachments");
    		iterator = msg.getAttachments();

    		cnt = 0;
    		iterator = msg.getAttachments();

    		while(iterator.hasNext()){
    			cnt++;
    			iterator.next();
    		}
    		System.out.println("number of attachments: " + cnt);

    		if (cnt > 1) {
    			System.out.println("the 2 text/xml attachments were not removed (unexpected)");
    		} else if(cnt == 1) {
    			iterator = msg.getAttachments();
    			AttachmentPart ap = (AttachmentPart) iterator.next();
    			String ctype = ap.getContentType();
    			System.out.println("Content-Type of remaining attachment is: "+ctype);
    			if(ctype.equals("text/xml")) {
    				System.out.println("one of the text/xml attachments was not removed");
    			}
    		} else {
    			System.out.println("all attachments were removed (unexpected)");
    		}

    	} catch(Exception e) {
    		System.out.println("Exception: " + e);
    	}
    }
}

