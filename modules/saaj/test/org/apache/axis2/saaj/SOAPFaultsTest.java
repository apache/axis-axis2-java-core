package org.apache.axis2.saaj;

import java.io.ByteArrayOutputStream;

import javax.xml.soap.Detail;
import javax.xml.soap.DetailEntry;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPFactory;
import javax.xml.soap.SOAPFault;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPHeaderElement;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;

import junit.framework.TestCase;

public class SOAPFaultsTest extends TestCase {
	
	public SOAPFaultsTest(String name){
		super(name);
	}
	
	//Create SOAPFault with additional detail elements
	public void testAdditionDetail() throws Exception{
        String xml ="<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:cwmp=\"http://cwmp.com\">\n" +
        " <soapenv:Header>\n" +
        "  <cwmp:ID soapenv:mustUnderstand=\"1\">HEADERID-7867678</cwmp:ID>\n" +
        " </soapenv:Header>\n" +
        " <soapenv:Body>\n" +
        "  <soapenv:Fault>\n" +
        "   <faultcode>soapenv:Client</faultcode>\n" +
        "   <faultstring>CWMP fault</faultstring>\n" +
        "   <detail>\n" +
        "    <cwmp:Fault>\n" +
        "     <cwmp:FaultCode>This is the fault code</cwmp:FaultCode>\n" +
        "     <cwmp:FaultString>Fault Message</cwmp:FaultString>\n" +
        "    </cwmp:Fault>\n" +
        "   </detail>\n" +
        "  </soapenv:Fault>\n" +
        " </soapenv:Body>\n" +
        "</soapenv:Envelope>";
        
        MessageFactory fac = MessageFactory.newInstance();
        SOAPMessage faultMessage = fac.createMessage();
        
        //Create the response to the message
        faultMessage = fac.createMessage();
        SOAPPart part = faultMessage.getSOAPPart();
        SOAPEnvelope envelope = part.getEnvelope();
        envelope.addNamespaceDeclaration("cwmp", "http://cwmp.com");
        SOAPBody body = envelope.getBody();
        SOAPHeader header = envelope.getHeader();
        Name idName = envelope.createName("ID", "cwmp", "http://cwmp.com");
        SOAPHeaderElement id = header.addHeaderElement(idName);
        id.setMustUnderstand(true);
        id.addTextNode("HEADERID-7867678");
        
        //Create the SOAPFault object
        SOAPFault fault = body.addFault();
        fault.setFaultCode("Client");
        fault.setFaultString("CWMP fault");
        
        //Add Fault Detail information
        Detail faultDetail = fault.addDetail();
        Name cwmpFaultName = envelope.createName("Fault", "cwmp",
        		"http://cwmp.com");
        DetailEntry cwmpFaultDetail =
        	faultDetail.addDetailEntry(cwmpFaultName);
        SOAPElement e = cwmpFaultDetail.addChildElement("FaultCode");
        
        e.addTextNode("This is the fault code");
        SOAPElement e2 = cwmpFaultDetail.addChildElement(envelope.createName("FaultString", "cwmp", "http://cwmp.com"));
        e2.addTextNode("Fault Message");
        faultMessage.saveChanges();
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        faultMessage.writeTo(baos);
        String xml2 = new String(baos.toByteArray());
        faultMessage.writeTo(System.out);
        //assertXMLEqual(xml,xml2);	
	}
	
	public void testQuick() throws Exception {
		MessageFactory msgfactory = MessageFactory.newInstance();
		SOAPFactory factory = SOAPFactory.newInstance();
		SOAPMessage outputmsg = msgfactory.createMessage();
		String valueCode = "faultcode";
		String valueString = "faultString";
		SOAPFault fault = outputmsg.getSOAPPart().getEnvelope().getBody().addFault();
		fault.setFaultCode(valueCode);
		fault.setFaultString(valueString);
		Detail d;
		d = fault.addDetail();
		d.addDetailEntry(factory.createName("Hello"));
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		if (outputmsg != null) {
			if (outputmsg.saveRequired()) {
				outputmsg.saveChanges();
			}
			outputmsg.writeTo(baos);
		}
		String xml = new String(baos.toByteArray());
		assertTrue(xml.indexOf("Hello")!=-1);
	}
	
	public void testSOAPFaultSaveChanges() throws Exception {
		MessageFactory msgFactory =
			MessageFactory.newInstance();
		SOAPMessage msg = msgFactory.createMessage();
		SOAPEnvelope envelope =
			msg.getSOAPPart().getEnvelope();
		SOAPBody body = envelope.getBody();
		SOAPFault fault = body.addFault();
		
		fault.setFaultCode("Client");
		fault.setFaultString(
			"Message does not have necessary info");
		fault.setFaultActor("http://gizmos.com/order");
		
		Detail detail = fault.addDetail();
		
		Name entryName = envelope.createName("order", "PO",
			"http://gizmos.com/orders/");
		DetailEntry entry = detail.addDetailEntry(entryName);
		entry.addTextNode("quantity element does not have a value");
		
		Name entryName2 = envelope.createName("confirmation",
				"PO", "http://gizmos.com/confirm");
		DetailEntry entry2 = detail.addDetailEntry(entryName2);
		entry2.addTextNode("Incomplete address: no zip code");
		
		msg.saveChanges();
		
        // Now retrieve the SOAPFault object and its contents
        //after checking to see that there is one

        if (body.hasFault()) {
            fault = body.getFault();
            String code = fault.getFaultCode();
            String string = fault.getFaultString();
            String actor = fault.getFaultActor();

            System.out.println("SOAP fault contains: ");
            System.out.println("    fault code = " + code);
            System.out.println("    fault string = " + string);
            if (actor != null) {
                System.out.println("    fault actor = " + actor);
            }

            detail = fault.getDetail();
            if (detail != null) {
                java.util.Iterator it = detail.getDetailEntries();
                while (it.hasNext()) {
                    entry = (DetailEntry) it.next();
                    String value = entry.getValue();
                    System.out.println("    Detail entry = " + value);
                }
            }
        }
	}
	
    public static void main(String[] args) throws Exception {
        SOAPFaultsTest detailTest = new SOAPFaultsTest("TestSOAPFaults");
        detailTest.testQuick();
        detailTest.testAdditionDetail();
        detailTest.testSOAPFaultSaveChanges();
    }
}
