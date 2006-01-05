package org.apache.axis2.saaj;

import junit.framework.TestCase;

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
import java.io.ByteArrayOutputStream;
import java.util.Iterator;

public class SOAPFaultsTest extends TestCase {

    public SOAPFaultsTest(String name) {
        super(name);
    }

    public void testSOAPFaultWithDetails() throws Exception {
        /* We are trying to generate the following SOAPFault

        <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
        xmlns:xsd="http://www.w3.org/2001/XMLSchema"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xmlns:cwmp="http://cwmp.com">
         <soapenv:Header>
          <cwmp:ID soapenv:mustUnderstand="1">HEADERID-7867678</cwmp:ID>
         </soapenv:Header>
         <soapenv:Body>
          <soapenv:Fault>
           <faultcode>Client</faultcode>
           <faultstring>CWMP fault</faultstring>
           <faultactor>http://gizmos.com/order</faultactor>
           <detail>
            <cwmp:Fault>
             <cwmp:FaultCode>This is the fault code</cwmp:FaultCode>
             <cwmp:FaultString>Fault Message</cwmp:FaultString>
             <cwmp:Message>This is a test fault</cwmp:FaultString>
            </cwmp:Fault>
           </detail>
          </soapenv:Fault>
         </soapenv:Body>
        </soapenv:Envelope>

        */

        MessageFactory fac = MessageFactory.newInstance();

        //Create the response to the message
        SOAPMessage soapMessage = fac.createMessage();
        SOAPPart soapPart = soapMessage.getSOAPPart();
        SOAPEnvelope envelope = soapPart.getEnvelope();
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
        fault.setFaultActor("http://gizmos.com/order");

        assertEquals("Client", fault.getFaultCode());
        assertEquals("CWMP fault", fault.getFaultString());
        assertEquals("http://gizmos.com/order", fault.getFaultActor());

        //Add Fault Detail information
        Detail faultDetail = fault.addDetail();
        Name cwmpFaultName = envelope.createName("Fault", "cwmp", "http://cwmp.com");
        DetailEntry faultDetailEntry = faultDetail.addDetailEntry(cwmpFaultName);
        SOAPElement e = faultDetailEntry.addChildElement("FaultCode");

        e.addTextNode("This is the fault code");
        SOAPElement e2 = faultDetailEntry.addChildElement(envelope.createName("FaultString",
                                                                              "cwmp",
                                                                              "http://cwmp.com"));
        e2.addTextNode("Fault Message");

        SOAPElement e3 = faultDetailEntry.addChildElement("Message");
        e3.addTextNode("This is a test fault");

        soapMessage.saveChanges();

        // ------------------- Validate the contents -------------------------------------
        final Detail detail = fault.getDetail();
        final Iterator detailEntryIter = detail.getDetailEntries();
        boolean foundFirst = false;
        boolean foundSecond = false;
        boolean foundThird = false;
        while (detailEntryIter.hasNext()) {
            final DetailEntry detailEntry = (DetailEntry) detailEntryIter.next();
            final Iterator childElementsIter = detailEntry.getChildElements();
            while (childElementsIter.hasNext()) {
                final SOAPElement soapElement = (SOAPElement) childElementsIter.next();
                if (soapElement.getTagName().equals("FaultCode") &&
                    soapElement.getValue().equals("This is the fault code")) {
                    foundFirst = true;
                }
                if (soapElement.getTagName().equals("FaultString") &&
                    soapElement.getValue().equals("Fault Message")) {
                    foundSecond = true;
                }
                if (soapElement.getTagName().equals("Message") &&
                    soapElement.getValue().equals("This is a test fault")) {
                    foundThird = true;
                }
            }
        }
        assertTrue(foundFirst && foundSecond && foundThird);
        // ------------------------------------------------------------------------------

        // Test whether the fault is being serialized properly
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        soapMessage.writeTo(baos);
        String xml = new String(baos.toByteArray());

        System.out.println(xml);
        assertTrue(xml.indexOf("<faultcode>Client</faultcode>") != -1);
        assertTrue(xml.indexOf("<faultstring>CWMP fault</faultstring>") != -1);
        assertTrue(xml.indexOf("<faultactor>http://gizmos.com/order</faultactor>") != -1);
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
        Detail detail = fault.addDetail();
        detail.addDetailEntry(factory.createName("Hello"));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        if (outputmsg.saveRequired()) {
            outputmsg.saveChanges();
        }
        outputmsg.writeTo(baos);
        String xml = new String(baos.toByteArray());
        assertTrue(xml.indexOf("Hello") != -1);
    }

}
