package org.apache.axis2.saaj;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;

import javax.xml.soap.Detail;
import javax.xml.soap.DetailEntry;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.Name;
import javax.xml.soap.Node;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFault;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPHeaderElement;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.soap.Text;

import junit.framework.TestCase;
import junit.framework.AssertionFailedError;

public class EnvelopeTest extends TestCase {
	
    public EnvelopeTest(String name) {
        super(name);
    }

    String xmlString =
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
        "  <shw:Address xmlns:shw=\"http://www.jcommerce.net/soap/ns/SOAPHelloWorld\">\n" +
        "    <shw:City>GENT</shw:City>\n" +
        "  </shw:Address>\n" +
        " </soapenv:Body>\n" +
        "</soapenv:Envelope>";
    
   public void testEnvelope() throws Exception{
    	MessageFactory mf = MessageFactory.newInstance();
    	SOAPMessage smsg = 
    		mf.createMessage(new MimeHeaders(), new ByteArrayInputStream(xmlString.getBytes()));
    	SOAPPart sp = smsg.getSOAPPart();
    	SOAPEnvelope se = (SOAPEnvelope)sp.getEnvelope();
    	//smsg.writeTo(System.out);
    	assertTrue(se != null);
    }
    
    public void testEnvelope2() throws Exception{
    	MessageFactory mf = MessageFactory.newInstance();
    	SOAPMessage smsg =
    		mf.createMessage(new MimeHeaders(), new ByteArrayInputStream(xmlString.getBytes()));
    	SOAPPart sp = smsg.getSOAPPart();
    	SOAPEnvelope se = (SOAPEnvelope)sp.getEnvelope();
    	ByteArrayOutputStream baos = new ByteArrayOutputStream();
    	smsg.writeTo(baos);
    	SOAPBody body = smsg.getSOAPPart().getEnvelope().getBody();
    	assertTrue(body != null);
    }
  
    
    public void testEnvelopeWithLeadingComment() throws Exception {
    	String soapMessageWithLeadingComment =
    		"<?xml version='1.0' encoding='UTF-8'?>" + 
    		"<!-- Comment -->" +
			"<env:Envelope xmlns:env='http://schemas.xmlsoap.org/soap/envelope/'>" +
			"<env:Body><echo><arg0>Hello</arg0></echo></env:Body>" +
			"</env:Envelope>";
    	
    	SOAPConnectionFactory scFactory = SOAPConnectionFactory.newInstance();
    	SOAPConnection con = scFactory.createConnection();
    	
    	MessageFactory factory = MessageFactory.newInstance();
    	SOAPMessage message =
    		factory.createMessage(new MimeHeaders(),
    				new ByteArrayInputStream(soapMessageWithLeadingComment.getBytes()));
    	SOAPPart part = message.getSOAPPart();
    	SOAPEnvelope envelope = (SOAPEnvelope) part.getEnvelope();
    	message.writeTo(System.out);
    	assertTrue(envelope != null);
    }
    
    
    private SOAPEnvelope getSOAPEnvelope() throws Exception {
        SOAPConnectionFactory scFactory = SOAPConnectionFactory.newInstance();
        SOAPConnection con = scFactory.createConnection();

        MessageFactory factory = MessageFactory.newInstance();
        SOAPMessage message = factory.createMessage();
        return message.getSOAPPart().getEnvelope();
    }
    
    public void testFaults() throws Exception {
        SOAPEnvelope envelope = getSOAPEnvelope();
        SOAPBody body = envelope.getBody();
        SOAPFault sf = body.addFault();
        //sf.setFaultCode("myFault");
        //String fc = sf.getFaultCode();
        sf.setFaultString("myFault");
        String fc = sf.getFaultString(); //Chk the same for FaultCode as well
        							// currently not done in SAAJ
        assertTrue(fc.equals("myFault"));
    }
    
    private int getIteratorCount(java.util.Iterator i) {
        int count = 0;
        while (i.hasNext()) {
            count++;
            i.next();
        }
        return count;
    }
    
    public void testFaults2() throws Exception {
    
    	SOAPEnvelope envelope = getSOAPEnvelope();
    	SOAPBody body = envelope.getBody();
    	SOAPFault sf = body.addFault();
    	
    	assertTrue(body.getFault() != null);
    	
    	Detail d1 = sf.addDetail();
    	Name name = envelope.createName("GetLastTradePrice", "WOMBAT",
    	"http://www.wombat.org/trader");
    	d1.addDetailEntry(name);
    	
    	Detail d2 = sf.getDetail();
    	assertTrue(d2 != null);
    	Iterator i = d2.getDetailEntries();
    	assertTrue(getIteratorCount(i) == 1);
    	i = d2.getDetailEntries();
    	//message.writeTo(System.out);
    	while(i.hasNext()) {
    		DetailEntry de = (DetailEntry)i.next();
    		assertEquals(de.getElementName(),name);
    	}
    }
    
//    public void testHeaderElements() throws Exception {
    	SOAPEnvelope envelope = getSOAPEnvelope();
    	SOAPBody body = envelope.getBody();
    	SOAPHeader hdr = envelope.getHeader();
//
    	SOAPHeaderElement she1 = hdr.addHeaderElement(envelope.createName("foo1", "f1", "foo1-URI"));
//    	she1.setActor("actor-URI");
    	java.util.Iterator iterator = hdr.extractHeaderElements("actor-URI");
//    	int cnt = 0;
//    	while (iterator.hasNext()) {
//    		cnt++;
//    		SOAPHeaderElement she = (SOAPHeaderElement) iterator.next();
//    		assertTrue(she.equals(she1));
//    	}
//    	assertTrue(cnt == 1);
//    	iterator = hdr.extractHeaderElements("actor-URI");
//    	assertTrue(!iterator.hasNext());
//    }
    
    public void testText1() throws Exception {
    	SOAPEnvelope envelope = getSOAPEnvelope();
    	SOAPBody body = envelope.getBody();
     	Iterator iStart = body.getChildElements();
    	int countStart = getIteratorCount(iStart);
    	SOAPElement se = body.addTextNode("<txt>This is text</txt>");
    	assertTrue(se != null);
    	assertTrue(body.getValue().equals("<txt>This is text</txt>"));
    	Iterator i = body.getChildElements();
    	int count = getIteratorCount(i);
    	assertTrue(count == countStart + 1);
    }
    
    public void testText2() throws Exception {
    	SOAPEnvelope envelope = getSOAPEnvelope();
    	SOAPBody body = envelope.getBody();
    	SOAPElement se = body.addTextNode("This is text");
    	Iterator iterator = se.getChildElements();
    	Node n = null;
    	while (iterator.hasNext()) {
    		n = (Node)iterator.next();
    		if (n instanceof Text){
    			break;
    		}
    	}
    	assertTrue(n instanceof Text);
    	Text t = (Text)n;
    	assertTrue(!t.isComment());
    }
    
    public void testText3() throws Exception {
    	SOAPEnvelope envelope = getSOAPEnvelope();
    	SOAPBody body = envelope.getBody();
    	SOAPElement se = body.addTextNode("<!-- This is a comment -->");
    	Iterator iterator = se.getChildElements();
    	Node n = null;
    	while (iterator.hasNext()) {
    		n = (Node)iterator.next();
    		if (n instanceof Text)
    			break;
    	}
    	assertTrue(n instanceof Text);
    	Text t = (Text)n;
    	assertTrue(t.isComment());
    }
    
    public void testAttributes() throws Exception {
    	SOAPEnvelope envelope = getSOAPEnvelope();
    	SOAPBody body = envelope.getBody();
    	
    	Name name1 = envelope.createName("MyAttr1");
    	String value1 = "MyValue1";
    	Name name2 = envelope.createName("MyAttr2");
    	String value2 = "MyValue2";
    	Name name3 = envelope.createName("MyAttr3");
    	String value3 = "MyValue3";
    	body.addAttribute(name1, value1);
    	body.addAttribute(name2, value2);
    	body.addAttribute(name3, value3);
    	java.util.Iterator iterator = body.getAllAttributes();
    	assertTrue(getIteratorCount(iterator) == 3);
    	iterator = body.getAllAttributes();
    	boolean foundName1 = false;
    	boolean foundName2 = false;
    	boolean foundName3 = false;
    	while (iterator.hasNext()) {
    		Name name = (Name) iterator.next();
    		if (name.equals(name1))
    			foundName1 = true;
    		else if (name.equals(name2))
    			foundName2 = true;
    		else if (name.equals(name3))
    			foundName3 = true;
    	}
    }
    
    public static void main(String[] args) throws Exception {
    	EnvelopeTest tester = new EnvelopeTest("EnvelopeTest");
    	tester.testEnvelope();
    	tester.testEnvelope2();
    	tester.testFaults();
    	tester.testFaults2();
//    	tester.testHeaderElements();
    	tester.testText1();
    	tester.testText2();
    	tester.testText3();
    	tester.testAttributes();
    	tester.testEnvelopeWithLeadingComment();
    }
}
