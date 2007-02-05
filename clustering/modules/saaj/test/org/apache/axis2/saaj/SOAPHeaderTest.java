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

import java.util.ArrayList;
import java.util.Iterator;

import javax.xml.namespace.QName;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFactory;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPHeaderElement;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;

import junit.framework.TestCase;

public class SOAPHeaderTest extends TestCase {
    private MessageFactory mf = null;
    private SOAPMessage msg = null;
    private SOAPPart sp = null;
    private SOAPEnvelope envelope = null;
    private SOAPHeader hdr = null;
    private SOAPHeaderElement she1 = null;
    private SOAPHeaderElement she2 = null;

    public SOAPHeaderTest(String name) {
        super(name);
    }

    public void _testAddHeaderElements() throws Exception {
        javax.xml.soap.SOAPMessage soapMessage =
                javax.xml.soap.MessageFactory.newInstance().createMessage();
        javax.xml.soap.SOAPEnvelope soapEnv =
                soapMessage.getSOAPPart().getEnvelope();
        javax.xml.soap.SOAPHeader header = soapEnv.getHeader();
        assertTrue(header.addChildElement("ebxmlms1") instanceof SOAPHeaderElement);
        assertTrue(header.addChildElement("ebxmlms2", "ch2", "http;//test.apache.org") instanceof SOAPHeaderElement);
        assertTrue(header.addHeaderElement(soapEnv.createName("ebxmlms3", "ch3", "http://test2.apache.org")) != null);
        assertTrue(header.addHeaderElement(soapEnv.createName("ebxmlms4")) != null);
        assertTrue(header.addHeaderElement(new PrefixedQName("http://test3.apache.org", "ebxmlms5", "ch5")) != null);

        SOAPHeaderElement firstChild = (SOAPHeaderElement) header.getFirstChild();
        assertEquals("ebxmlms1", firstChild.getLocalName());
        assertEquals("", firstChild.getPrefix());
        assertEquals("", firstChild.getNamespaceURI());

        SOAPHeaderElement secondChild = (SOAPHeaderElement) firstChild.getNextSibling();
        assertEquals("ebxmlms2", secondChild.getLocalName());
        assertEquals("ch2", secondChild.getPrefix());
        assertEquals("http;//test.apache.org", secondChild.getNamespaceURI());

        SOAPHeaderElement thirdChild = (SOAPHeaderElement) secondChild.getNextSibling();
        assertEquals("ebxmlms3", thirdChild.getLocalName());
        assertEquals("ch3", thirdChild.getPrefix());
        assertEquals("http://test2.apache.org", thirdChild.getNamespaceURI());

        SOAPHeaderElement lastChild = (SOAPHeaderElement) header.getLastChild();
        assertEquals("ebxmlms5", lastChild.getLocalName());
        assertEquals("ch5", lastChild.getPrefix());
        assertEquals("http://test3.apache.org", lastChild.getNamespaceURI());

        SOAPHeaderElement fourthChild = (SOAPHeaderElement) lastChild.getPreviousSibling();
        assertEquals("ebxmlms4", fourthChild.getLocalName());
        assertEquals("", fourthChild.getPrefix());
        assertEquals("", fourthChild.getNamespaceURI());

        Iterator it = header.getChildElements();
        int numOfHeaderElements = 0;
        while (it.hasNext()) {
            Object o = it.next();
            assertTrue(o instanceof SOAPHeaderElement);
            SOAPHeaderElement el = (SOAPHeaderElement) o;
            String lName = el.getLocalName();
            assertTrue(lName.equals("ebxmlms" + ++numOfHeaderElements));
        }
        assertEquals(5, numOfHeaderElements);
    }

    public void testHeaders() {
        try {
            // Create message factory and SOAP factory
            MessageFactory messageFactory = MessageFactory.newInstance();
            SOAPFactory soapFactory = SOAPFactory.newInstance();

            // Create a message
            SOAPMessage message = messageFactory.createMessage();

            // Get the SOAP header from the message and
            //  add headers to it
            SOAPHeader header = message.getSOAPHeader();

            String nameSpace = "ns";
            String nameSpaceURI = "http://gizmos.com/NSURI";

            Name order =
                    soapFactory.createName("orderDesk", nameSpace, nameSpaceURI);
            SOAPHeaderElement orderHeader = header.addHeaderElement(order);
            orderHeader.setActor("http://gizmos.com/orders");

            Name shipping =
                    soapFactory.createName("shippingDesk", nameSpace, nameSpaceURI);
            SOAPHeaderElement shippingHeader =
                    header.addHeaderElement(shipping);
            shippingHeader.setActor("http://gizmos.com/shipping");

            Name confirmation =
                    soapFactory.createName("confirmationDesk", nameSpace,
                                           nameSpaceURI);
            SOAPHeaderElement confirmationHeader =
                    header.addHeaderElement(confirmation);
            confirmationHeader.setActor("http://gizmos.com/confirmations");

            Name billing =
                    soapFactory.createName("billingDesk", nameSpace, nameSpaceURI);
            SOAPHeaderElement billingHeader = header.addHeaderElement(billing);
            billingHeader.setActor("http://gizmos.com/billing");

            // Add header with mustUnderstand attribute
            Name tName =
                    soapFactory.createName("Transaction", "t",
                                           "http://gizmos.com/orders");

            SOAPHeaderElement transaction = header.addHeaderElement(tName);
            transaction.setMustUnderstand(true);
            transaction.addTextNode("5");

            // Get the SOAP body from the message but leave
            // it empty
            SOAPBody body = message.getSOAPBody();

            message.saveChanges();

            // Display the message that would be sent
            System.out.println("\n----- Request Message ----\n");
            message.writeTo(System.out);

            // Look at the headers
            Iterator allHeaders = header.examineAllHeaderElements();

            while (allHeaders.hasNext()) {
                SOAPHeaderElement headerElement =
                        (SOAPHeaderElement) allHeaders.next();
                Name headerName = headerElement.getElementName();
                System.out.println("\nHeader name is " +
                                   headerName.getQualifiedName());
                System.out.println("Actor is " + headerElement.getActor());
                System.out.println("mustUnderstand is " +
                                   headerElement.getMustUnderstand());
            }
        } catch (Exception e) {
            fail("Enexpected Exception " + e);
        }
    }

    protected void setUp() throws Exception {
        msg = MessageFactory.newInstance().createMessage();
        sp = msg.getSOAPPart();
        envelope = sp.getEnvelope();
        hdr = envelope.getHeader();
    }

    public void testExamineHeader() {
        SOAPHeaderElement she = null;

        try {
            she1 = hdr.addHeaderElement(envelope.createName("foo1", "f1", "foo1-URI"));
            she1.setActor("actor-URI");
            Iterator iterator = hdr.examineAllHeaderElements();
            int cnt = 0;
            while (iterator.hasNext()) {
                cnt++;
                she = (SOAPHeaderElement) iterator.next();
                if (!she.equals(she1)) {
                    fail("SOAP Header Elements do not match");
                }
            }

            if (cnt != 1) {
                fail("SOAPHeaderElement count mismatch: expected 1, received " + cnt);
            }

            iterator = hdr.examineAllHeaderElements();
            if (!iterator.hasNext()) {
                fail("no elements in iterator - unexpected");
            }

        } catch (Exception e) {
            fail("Unexpected Exception: " + e);
        }
    }
    
    public void testAddNotUnderstoodHeaderElement() throws Exception {
        javax.xml.soap.SOAPMessage soapMessage =
                javax.xml.soap.MessageFactory.newInstance(
                		SOAPConstants.SOAP_1_2_PROTOCOL).createMessage();
        
        javax.xml.soap.SOAPEnvelope soapEnv =
                soapMessage.getSOAPPart().getEnvelope();
        javax.xml.soap.SOAPHeader header = soapEnv.getHeader();
        
	    SOAPElement soapElement = header.addNotUnderstoodHeaderElement(
	    		new QName("http://foo.org", "foo", "f"));
	    
        assertNotNull(soapElement);
	        Name name = soapElement.getElementName();
	        System.out.println("URI = " + name.getURI());
	        System.out.println("QualifiedName = " + name.getQualifiedName());
	        System.out.println("Prefix = " + name.getPrefix());
	        System.out.println("LocalName = " + name.getLocalName());
	        String uri = name.getURI();
	        String localName = name.getLocalName();
	        System.out.println("Validate the URI which must be " 
		    + SOAPConstants.URI_NS_SOAP_1_2_ENVELOPE);
	        
	        assertEquals(SOAPConstants.URI_NS_SOAP_1_2_ENVELOPE, uri);
	        System.out.println(
	        		"Validate the LocalName which must be NotUnderstood");
	        assertEquals("NotUnderstood", localName);
    }

    
    public void testAddUpgradeHeaderElement() throws Exception {
    	javax.xml.soap.SOAPMessage soapMessage =
    		javax.xml.soap.MessageFactory.newInstance(
    				SOAPConstants.SOAP_1_2_PROTOCOL).createMessage();

    	javax.xml.soap.SOAPEnvelope soapEnv =
    		soapMessage.getSOAPPart().getEnvelope();
    	javax.xml.soap.SOAPHeader header = soapEnv.getHeader();

    	// create a list of supported URIs.
    	ArrayList supported = new ArrayList();
    	supported.add(SOAPConstants.URI_NS_SOAP_1_2_ENVELOPE);
    	supported.add(SOAPConstants.URI_NS_SOAP_ENVELOPE);

    	System.out.println("Creating Upgrade SOAPHeaderElement");
    	SOAPElement soapElement = header.addUpgradeHeaderElement(supported.iterator());

    	System.out.println("Validating SOAPHeaderElement object creation");
    	assertNotNull(soapElement);
    	System.out.println("SOAPHeaderElement was created");

    	System.out.println("Validating Upgrade SOAPHeaderElement Name");
    	System.out.println("Get the ElementName");
    	Name name = soapElement.getElementName();
    	System.out.println("URI = " + name.getURI());
    	System.out.println("QualifiedName = " + name.getQualifiedName());
    	System.out.println("Prefix = " + name.getPrefix());
    	System.out.println("LocalName = " + name.getLocalName());
    	String uri = name.getURI();
    	String localName = name.getLocalName();
    	System.out.println("Validate the URI which must be "
    			+ SOAPConstants.URI_NS_SOAP_1_2_ENVELOPE);

    	assertTrue(uri.equals(SOAPConstants.URI_NS_SOAP_1_2_ENVELOPE));

    	System.out.println("Validate the LocalName which must be Upgrade");
    	assertTrue(localName.equals("Upgrade"));
    }

    public void testExamineHeaderElements() throws Exception {
    	javax.xml.soap.SOAPMessage soapMessage =
    		javax.xml.soap.MessageFactory.newInstance(
    				SOAPConstants.SOAP_1_2_PROTOCOL).createMessage();

    	javax.xml.soap.SOAPEnvelope soapEnv =
    		soapMessage.getSOAPPart().getEnvelope();
    	javax.xml.soap.SOAPHeader header = soapEnv.getHeader();

    	System.out.println("Creating SOAPHeaderElement 1");
    	SOAPHeaderElement soapHeaderElement = header.addHeaderElement(envelope.createName("foo1", "f1", "foo1-URI"));

    	Iterator iterator = null;
    	System.out.println("Set the role associated with SOAPHeaderElement");
    	soapHeaderElement.setRole("role-URI");

    	System.out.println("Examing SOAPHeaderElements with role of role1-URI");
    	iterator = header.examineHeaderElements("role1-URI");

    	int count=0;
    	while (iterator.hasNext()) {
    		count++;
    		iterator.next();
    	}

    	assertEquals(0, count);
    	System.out.println("SOAPHeaderElement count mismatch: expected 0, received " + count);

    }
    
    /*
     * examineHeaderElementsTest4
     */
    public void testExamineHeaderElements2() throws Exception {
    	javax.xml.soap.SOAPMessage soapMessage =
    		javax.xml.soap.MessageFactory.newInstance().createMessage();

    	javax.xml.soap.SOAPEnvelope soapEnv =
    		soapMessage.getSOAPPart().getEnvelope();
    	javax.xml.soap.SOAPHeader header = soapEnv.getHeader();
    	SOAPHeaderElement soapHeaderElement = null;

    	try{
    		// Add some soap header elements
    		System.out.println("Add SOAP HeaderElement Header1");
    		SOAPElement se = header.addHeaderElement(
    				envelope.createName("Header1", "prefix", "http://myuri"))
    				.addTextNode("This is Header1");
    		soapHeaderElement = (SOAPHeaderElement) se;
    		soapHeaderElement.setMustUnderstand(true);

    		System.out.println("Add SOAP HeaderElement Header2");
    		se = header.addHeaderElement(
    				envelope.createName("Header2", "prefix", "http://myuri"))
    				.addTextNode("This is Header2");
    		soapHeaderElement = (SOAPHeaderElement) se;
    		soapHeaderElement.setMustUnderstand(false);

    		System.out.println("Add SOAP HeaderElement Header3");
    		se = header.addHeaderElement(
    				envelope.createName("Header3", "prefix", "http://myuri"))
    				.addTextNode("This is Header3");
    		soapHeaderElement = (SOAPHeaderElement) se;
    		soapHeaderElement.setMustUnderstand(true);

    		System.out.println("Add SOAP HeaderElement Header4");
    		se = header.addHeaderElement(
    				envelope.createName("Header4", "prefix", "http://myuri"))
    				.addTextNode("This is Header4");
    		soapHeaderElement = (SOAPHeaderElement) se;
    		soapHeaderElement.setMustUnderstand(false);

    		System.out.println("Examing all SOAPHeaderElements");
    		Iterator iterator = header.examineAllHeaderElements();

    		System.out.println("Validating Iterator count .... should be 4");
    		int cnt=0;
    		while (iterator.hasNext()) {
    			cnt++;
    			soapHeaderElement = (SOAPHeaderElement)iterator.next();
    		}
    		if (cnt != 4) {
    			fail("SOAPHeaderElement count mismatch: expected 4, received " + cnt);
    		}

    		System.out.println("Examing SOAPHeaderElements passing actor next uri");
    		iterator = header.examineHeaderElements(SOAPConstants.URI_SOAP_ACTOR_NEXT);

    		System.out.println("Validating Iterator count .... should now be 0");
    		cnt=0;
    		while (iterator.hasNext()) {
    			cnt++;
    			soapHeaderElement = (SOAPHeaderElement)iterator.next();
    		}
    		if (cnt != 0) {
    			fail("SOAPHeaderElement count mismatch: expected 0, received " + cnt);
    		}

    	} catch (Exception e) {
    		fail("Unexpected Exception: " + e);
    	}
    }
    
    public void testQNamesOnHeader(){
    	SOAPHeaderElement transaction = null;
    	try {
    		System.out.println("SOAP1.1 and SOAP1.2 requires all HeaderElements to be"
    				+ " namespace qualified");
    		System.out.println("Try adding HeaderElement with unqualified QName "
    				+ "not belonging to any namespace (expect SOAPException)");
    		System.out.println("No URI and no PREFIX in QName");
    		transaction = 
    			hdr.addHeaderElement(envelope.createName("Transaction"));
    		System.out.println("Did not throw expected SOAPException");
    	} catch (SOAPException e) {
    		System.out.println("Did throw expected SOAPException");
    	} catch (Exception e) {
    		System.out.println("Unexpected Exception: " + e.getMessage());
    	}
    }
}