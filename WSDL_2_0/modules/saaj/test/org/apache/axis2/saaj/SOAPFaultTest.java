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

import junit.framework.TestCase;

import javax.xml.soap.Detail;
import javax.xml.soap.DetailEntry;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFactory;
import javax.xml.soap.SOAPFault;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPHeaderElement;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import java.io.ByteArrayOutputStream;
import java.util.Iterator;

public class SOAPFaultTest extends TestCase {

    public SOAPFaultTest(String name) {
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
                if (soapElement.getTagName().equals("cwmp:FaultString") &&
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

    public void testAddDetailsTwice() {
        try {
            MessageFactory fac = MessageFactory.newInstance();

            //Create the response to the message
            SOAPMessage soapMessage = fac.createMessage();
            SOAPPart soapPart = soapMessage.getSOAPPart();
            SOAPEnvelope envelope = soapPart.getEnvelope();
            envelope.addNamespaceDeclaration("cwmp", "http://cwmp.com");
            SOAPBody body = envelope.getBody();

            body.addFault().addDetail();
            try {
                body.getFault().addDetail();
                fail("Expected Exception did not occur");
            } catch (SOAPException e) {
                assertTrue(true);
            }

        } catch (SOAPException e) {
            fail("Unexpected Exception Occurred : " + e);
        }
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

    public void testFaults() {
        try {
            MessageFactory messageFactory = MessageFactory.newInstance();
            SOAPFactory soapFactory = SOAPFactory.newInstance();
            SOAPMessage message = messageFactory.createMessage();
            SOAPBody body = message.getSOAPBody();
            SOAPFault fault = body.addFault();

            Name faultName =
                    soapFactory.createName("Client", "",
                                           SOAPConstants.URI_NS_SOAP_ENVELOPE);
            fault.setFaultCode(faultName);

            fault.setFaultString("Message does not have necessary info");
            fault.setFaultActor("http://gizmos.com/order");

            Detail detail = fault.addDetail();

            Name entryName =
                    soapFactory.createName("order", "PO",
                                           "http://gizmos.com/orders/");
            DetailEntry entry = detail.addDetailEntry(entryName);
            entry.addTextNode("Quantity element does not have a value");

            Name entryName2 =
                    soapFactory.createName("confirmation", "PO",
                                           "http://gizmos.com/confirm");
            DetailEntry entry2 = detail.addDetailEntry(entryName2);
            entry2.addTextNode("Incomplete address: " + "no zip code");

            message.saveChanges();

            System.out.println("Here is what the XML message looks like:");
            message.writeTo(System.out);
            System.out.println();
            System.out.println();

            // Now retrieve the SOAPFault object and
            // its contents, after checking to see that
            // there is one
            if (body.hasFault()) {
                SOAPFault newFault = body.getFault();

                // Get the qualified name of the fault code
                Name code = newFault.getFaultCodeAsName();

                String string = newFault.getFaultString();
                String actor = newFault.getFaultActor();

                System.out.println("SOAP fault contains: ");
                System.out.println("  Fault code = " + code.getQualifiedName());
                System.out.println("  Local name = " + code.getLocalName());
                System.out.println("  Namespace prefix = " + code.getPrefix() +
                                   ", bound to " + code.getURI());
                System.out.println("  Fault string = " + string);

                if (actor != null) {
                    System.out.println("  Fault actor = " + actor);
                }

                Detail newDetail = newFault.getDetail();

                if (newDetail != null) {
                    Iterator entries = newDetail.getDetailEntries();

                    while (entries.hasNext()) {
                        DetailEntry newEntry = (DetailEntry) entries.next();
                        String value = newEntry.getValue();
                        System.out.println("  Detail entry = " + value);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            fail("Unexpected Exception : " + e);
        }
    }

    public void testGetFaultActor() {
        try {
            SOAPMessage msg = MessageFactory.newInstance().createMessage();
            SOAPFault sf = msg.getSOAPBody().addFault();

            sf.setFaultActor("/faultActorURI");
            sf.setFaultActor("/faultActorURI2");
            String result = sf.getFaultActor();

            if (!result.equals("/faultActorURI2")) {
                fail("Fault Actor not properly set");
            }
        } catch (Exception e) {
            fail("Unexpected Exception : " + e);
        }
    }

    public void testGetFaultString() {
        try {
            SOAPMessage msg = MessageFactory.newInstance().createMessage();
            SOAPFault sf = msg.getSOAPBody().addFault();

            sf.setFaultString("1st Fault String");
            sf.setFaultString("2nd Fault String");
            String result = sf.getFaultString();

            if (!result.equals("2nd Fault String")) {
                fail("Fault String not properly set");
            }
        } catch (Exception e) {
            fail("Unexpected Exception : " + e);
        }
    }
}
