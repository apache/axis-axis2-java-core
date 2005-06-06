/*
 * Created on Apr 15, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.apache.axis.saaj;

import junit.framework.TestCase;
import java.io.ByteArrayOutputStream;
import java.util.Iterator;

import javax.xml.soap.Name;
import javax.xml.soap.SOAPElement;

/**
 * @author Ashutosh Shahi ashutosh.shahi@gmail.com
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class HeadersTest extends TestCase {
	
    private final String actor = "ACTOR#1";
    private final String localName = "Local1";
    private final String namespace = "http://ws.apache.org";
    private final String prefix = "P1";
	
    public HeadersTest(String name) {
        super(name);
    }
    
    public void testAddingHeaderElements() throws Exception {
        javax.xml.soap.SOAPMessage soapMessage = javax.xml.soap.MessageFactory.newInstance().createMessage();
        javax.xml.soap.SOAPEnvelope soapEnv = soapMessage.getSOAPPart().getEnvelope();
        javax.xml.soap.SOAPHeader header = soapEnv.getHeader();
        header.addChildElement("ebxmlms");
        
        /*ByteArrayOutputStream baos = new ByteArrayOutputStream();
        soapMessage.writeTo(baos);
        String xml = new String(baos.toByteArray());
        assertTrue(xml.indexOf("ebxmlms") != -1);*/
        
        Iterator it = header.getChildElements();
        boolean b = false;
        while(it.hasNext()){
            SOAPElement el = (SOAPElement) it.next();
            String lName = el.getNodeName();
            if(lName.equalsIgnoreCase("ebxmlms")){
            	b = true;
            	break;
            }
        }
        assertTrue(b);
    }

}
