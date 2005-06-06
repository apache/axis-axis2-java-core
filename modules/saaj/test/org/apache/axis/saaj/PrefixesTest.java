/*
 * Created on Apr 26, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.apache.axis.saaj;

import junit.framework.TestCase;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPElement;

import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;

/**
 * @author Ashutosh Shahi
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class PrefixesTest extends TestCase {
	
    public PrefixesTest(String name) {
        super(name);
    }
    
    public void testAddingPrefixesForChildElements() throws Exception {
    	MessageFactory factory = MessageFactory.newInstance();
    	SOAPMessage msg = factory.createMessage();
    	SOAPPart sp = msg.getSOAPPart();
    	SOAPEnvelope se = sp.getEnvelope();
    	SOAPBody sb = se.getBody();
        SOAPElement el1 = sb.addBodyElement(se.createName
                ("element1", "prefix1", "http://www.sun.com"));
        SOAPElement el2 = el1.addChildElement(se.createName
                ("element2", "prefix2", "http://www.apache.org"));
        
        org.apache.axis.soap.SOAPEnvelope omEnv = ((SOAPEnvelopeImpl)se).getOMEnvelope();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        msg.writeTo(baos);
        
        String xml = new String(baos.toByteArray());
        System.out.println(xml);
        assertTrue(xml.indexOf("prefix1") != -1);
        assertTrue(xml.indexOf("prefix2") != -1);
        assertTrue(xml.indexOf("http://www.sun.com") != -1);
        assertTrue(xml.indexOf("http://www.apache.org") != -1);
    }
    
   /* public void testAttribute() throws Exception {
        String soappacket = "<SOAP-ENV:Envelope xmlns:SOAP-ENV =\"http://schemas.xmlsoap.org/soap/envelope/\"" + 
                            "xmlns:xsi =\"http://www.w3.org/1999/XMLSchema-instance\"" +
                            "xmlns:xsd =\"http://www.w3.org/1999/XMLSchema\">" + 
                            "<SOAP-ENV:Body>" +
                            "<helloworld name=\"tester\" />" + 
                            "</SOAP-ENV:Body>" +
                            "</SOAP-ENV:Envelope>";
        SOAPMessage msg = MessageFactory.newInstance().createMessage(new MimeHeaders(), new ByteArrayInputStream(soappacket.getBytes()));
        SOAPBody body = msg.getSOAPPart().getEnvelope().getBody();
        msg.writeTo(System.out);

        SOAPElement ele = (SOAPElement) body.getChildElements().next();
        java.util.Iterator attit = ele.getAllAttributes();

        System.out.println(attit.next().getClass());
        
        javax.xml.soap.Name n = (javax.xml.soap.Name) attit.next();
        //assertEquals("Test fail prefix problem",n.getQualifiedName(),"name");
    }*/

}
