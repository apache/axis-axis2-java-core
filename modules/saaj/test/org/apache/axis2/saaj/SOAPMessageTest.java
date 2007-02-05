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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
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
import javax.xml.soap.SOAPFactory;
import javax.xml.soap.SOAPFault;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.transform.stream.StreamSource;

import junit.framework.TestCase;

import org.apache.axis2.saaj.util.SAAJDataSource;

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


			InputStream in1 = new FileInputStream(new File(System.getProperty("basedir",".") + "/target/test-resources" + File.separator + "attach.xml"));
			ap1 = msg.createAttachmentPart(in1, "text/xml");
			msg.addAttachmentPart(ap1);

			InputStream in2 = new FileInputStream(new File(System.getProperty("basedir",".") + "/target/test-resources" + File.separator + "axis2.xml"));
			ap2 = msg.createAttachmentPart(in2, "text/xml");
			msg.addAttachmentPart(ap2);

			InputStream in3 = new FileInputStream(new File(System.getProperty("basedir",".") + "/target/test-resources" + File.separator + "axis2.xml"));
			ap3 = msg.createAttachmentPart(in3, "text/plain");
			msg.addAttachmentPart(ap3);

			//get all attachments
			iterator = msg.getAttachments();

			int cnt = 0;
			while(iterator.hasNext()){
				cnt++;
				iterator.next();
			}
			assertEquals(cnt,3);
			//remove just the text/xml attachments
			MimeHeaders mhs = new MimeHeaders();
			mhs.addHeader("Content-Type", "text/xml");
			msg.removeAttachments(mhs);

			//get all attachments
			iterator = msg.getAttachments();
			cnt = 0;
			iterator = msg.getAttachments();
			while(iterator.hasNext()){
				cnt++;
				iterator.next();
			}
			System.out.println("number of attachments: " + cnt);
			assertEquals(cnt,1);
			iterator = msg.getAttachments();
			AttachmentPart ap = (AttachmentPart) iterator.next();
			String ctype = ap.getContentType();
			System.out.println(ctype);
			assertTrue(ctype.equals("text/plain"));

		} catch(Exception e) {
			fail("Exception: " + e);
		}
	}


	public void testGetContent(){
		try {
			MessageFactory fac = MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL);
			//MessageFactory fac = MessageFactory.newInstance();
			SOAPMessage msg = fac.createMessage();
			SOAPPart soapPart = msg.getSOAPPart();
			SOAPEnvelope envelope = soapPart.getEnvelope();
			SOAPBody body = envelope.getBody();

			AttachmentPart ap;

			InputStream inputStream = new FileInputStream(new File(System.getProperty("basedir",".") + "/test-resources" + File.separator + "attach.xml"));
			ap = msg.createAttachmentPart(inputStream, "text/xml");
			DataHandler dh = new DataHandler(new SAAJDataSource(inputStream,1000,"text/xml",true));

			StringBuffer sb1 = copyToBuffer(dh.getInputStream());
			assertNotNull(ap);

			//Verify attachment part is not empty and contents are correct
			try {
				Object o = ap.getContent();
				InputStream is = null;
				assertNotNull(o);
				if(o instanceof StreamSource) {
					StreamSource ss = (StreamSource)o;
					is = ss.getInputStream();
				} else {
					fail("got object: " + o + 
					", expected object: javax.xml.transform.stream.StreamSource");
				}

				//if(is != null) {
				//	StringBuffer sb2 = copyToBuffer(is);
				//	String s1 = sb1.toString();
				//	String s2 = sb2.toString();
				//	assertTrue(s1.equals(s2));
				//}
			}
			catch (Exception e) {
				fail("attachment has no content - unexpected");
			}
		} catch(Exception e) {
			fail("Exception: " + e);
		}
	}


	private StringBuffer copyToBuffer(InputStream inputStream)
	{
		if (inputStream == null){
			return null;
		}
		StringWriter stringWriter = new StringWriter();
		BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
		try {
			String s;
			while((s = br.readLine()) != null)
				stringWriter.write(s);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return stringWriter.getBuffer();
	}

	
	/*
	 * Do not add this test unless below mentioned resources are accessible
	 */

	public void testGetAttachmentsByHREF(){
		String NS_PREFIX="mypre";
		String NS_URI="http://myuri.org/";

		try {
			//MessageFactory fac = MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL);
			SOAPMessage msg = MessageFactory.newInstance().createMessage();


			// Message creation takes care of creating the SOAPPart - a
			// required part of the message as per the SOAP 1.1 spec.
			SOAPPart sp = msg.getSOAPPart();
			SOAPEnvelope envelope = sp.getEnvelope();
			SOAPHeader hdr = envelope.getHeader();
			SOAPBody bdy = envelope.getBody();
			SOAPBodyElement sbe1 = bdy.addBodyElement(
					envelope.createName("Body1", NS_PREFIX, NS_URI));
			sbe1.addChildElement(envelope.createName(
					"TheGifAttachment", NS_PREFIX, NS_URI));
			sbe1.setAttribute("href", "cid:THEGIF");
			SOAPBodyElement sbe2 = bdy.addBodyElement(
					envelope.createName("Body2", NS_PREFIX, NS_URI));
			sbe2.addChildElement(envelope.createName(
					"TheXmlAttachment", NS_PREFIX, NS_URI));
			sbe2.setAttribute("href", "cid:THEXML");

			URL url1 = new URL("http://localhost:8080/SOAPMessage/attach.xml");
			URL url2 = new URL("http://localhost:8080/SOAPMessage/attach.gif");
			URL url3 = new URL("http://localhost:8080/SOAPMessage/attach.txt");
			URL url4 = new URL("http://localhost:8080/SOAPMessage/attach.html");
			URL url5 = new URL("http://localhost:8080/SOAPMessage/attach.jpeg");

			System.out.println("Add various mime type attachments to SOAP message");
			AttachmentPart ap1 = msg.createAttachmentPart(new DataHandler(url1));
			AttachmentPart ap2 = msg.createAttachmentPart(new DataHandler(url2));
			AttachmentPart ap3 = msg.createAttachmentPart(new DataHandler(url3));
			AttachmentPart ap4 = msg.createAttachmentPart(new DataHandler(url4));
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

			msg.addAttachmentPart(ap1);
			msg.addAttachmentPart(ap2);
			msg.addAttachmentPart(ap3);
			msg.addAttachmentPart(ap4);
			msg.addAttachmentPart(ap5);
			msg.saveChanges();

			//Retrieve attachment with href=cid:THEGIF
			AttachmentPart myap = msg.getAttachment(sbe1);
			if(myap == null) {
				fail("Returned null (unexpected)");
			} else if(!myap.getContentType().equals("image/gif")) {
				fail("Wrong attachment was returned: Got Content-Type of "
						+myap.getContentType()+", Expected Content-Type of image/gif");
			} 
			//Retrieve attachment with href=cid:THEXML
			myap = msg.getAttachment(sbe2);
			if(myap == null) {
				fail("Returned null (unexpected)");
			} else if(!myap.getContentType().equals("text/xml")) {
				fail("Wrong attachment was returned: Got Content-Type of "
						+myap.getContentType()+", Expected Content-Type of text/xml");
			}
			//Retrieve attachment with href=cid:boo-hoo (expect null)
			QName myqname = new QName("boo-hoo");
			SOAPElement myse = SOAPFactoryImpl.newInstance().createElement(myqname);
			myse.addTextNode("<theBooHooAttachment href=\"cid:boo-hoo\"/>");
			myap = msg.getAttachment(myse);
			assertNull(myap);

		} catch(Exception e) {
			fail("Error :"+e);
		}
	}


	/*
	 * Do not add this test unless below mentioned resources are accessible
	 */
	public void _testGetAttachmentByHREF2(){
		String NS_PREFIX="mypre";
		String NS_URI="http://myuri.org/";

		try {
			System.out.println("Create SOAP message from message factory");
			MessageFactory fac = MessageFactory.newInstance();
			SOAPMessage msg = fac.createMessage();

			// Message creation takes care of creating the SOAPPart - a
			// required part of the message as per the SOAP 1.1 spec.
			SOAPPart sp = msg.getSOAPPart();
			SOAPEnvelope envelope = sp.getEnvelope();
			SOAPHeader hdr = envelope.getHeader();

			SOAPBody bdy = envelope.getBody();
			SOAPBodyElement sbe1 = bdy.addBodyElement(
					envelope.createName("Body1", NS_PREFIX, NS_URI));
			sbe1.addChildElement(envelope.createName(
					"TheGifAttachment", NS_PREFIX, NS_URI));

			SOAPBodyElement sbe2 = bdy.addBodyElement(
					envelope.createName("Body2", NS_PREFIX, NS_URI));

			sbe2.addChildElement(envelope.createName(
					"TheXmlAttachment", NS_PREFIX, NS_URI));

			URL url1 = new URL("http://localhost:8080/SOAPMessage/attach.xml");
			URL url2 = new URL("http://localhost:8080/SOAPMessage/attach.gif");
			URL url3 = new URL("http://localhost:8080/SOAPMessage/attach.txt");
			URL url4 = new URL("http://localhost:8080/SOAPMessage/attach.html");
			URL url5 = new URL("http://localhost:8080/SOAPMessage/attach.jpeg");

			// Set href on body elements using Content-Location headers and relative URI's
			sbe1.setAttribute("href", url2.toString());
			sbe2.setAttribute("href", url1.toString());

			AttachmentPart ap1 = msg.createAttachmentPart(new DataHandler(url1));
			AttachmentPart ap2 = msg.createAttachmentPart(new DataHandler(url2));
			AttachmentPart ap3 = msg.createAttachmentPart(new DataHandler(url3));
			AttachmentPart ap4 = msg.createAttachmentPart(new DataHandler(url4));
			AttachmentPart ap5 = msg.createAttachmentPart(new DataHandler(url5));

			ap1.setContentType("text/xml");
			ap1.setContentId("<THEXML>");
			ap1.setContentLocation(url1.toString());
			ap2.setContentType("image/gif");
			ap2.setContentId("<THEGIF>");
			ap2.setContentLocation(url2.toString());
			ap3.setContentType("text/plain");
			ap3.setContentId("<THEPLAIN>");
			ap3.setContentLocation(url3.toString());
			ap4.setContentType("text/html");
			ap4.setContentId("<THEHTML>");
			ap4.setContentLocation(url4.toString());
			ap5.setContentType("image/jpeg");
			ap5.setContentId("<THEJPEG>");
			ap5.setContentLocation(url5.toString());

			// Add the attachments to the message.
			msg.addAttachmentPart(ap1);
			msg.addAttachmentPart(ap2);
			msg.addAttachmentPart(ap3);
			msg.addAttachmentPart(ap4);
			msg.addAttachmentPart(ap5);
			msg.saveChanges();

			//Retrieve attachment with href=THEGIF
			AttachmentPart myap = msg.getAttachment(sbe1);
			if(myap == null) {
				fail("Returned null (unexpected)");
			} else if(!myap.getContentType().equals("image/gif")) {
				fail("Wrong attachment was returned: Got Content-Type of "
						+myap.getContentType()+", Expected Content-Type of image/gif");
			}
			
			//Retrieve attachment with href=THEXML
			myap = msg.getAttachment(sbe2);
			if(myap == null) {
				fail("Returned null (unexpected)");
			} else if(!myap.getContentType().equals("text/xml")) {
				fail("Wrong attachment was returned: Got Content-Type of "
						+myap.getContentType()+", Expected Content-Type of text/xml");
			}
			
			//Retrieve attachment with href=boo-hoo (expect null)
			QName myqname = new QName("boo-hoo");
			SOAPElement myse = SOAPFactory.newInstance().createElement(myqname);
			myse.addTextNode("<theBooHooAttachment href=\"boo-hoo\"/>");
			myap = msg.getAttachment(myse);
			assertNull(myap);
		} catch(Exception e) {
			fail("Exception: " + e);
		}
	}

}

