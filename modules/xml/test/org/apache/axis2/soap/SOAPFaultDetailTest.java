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

package org.apache.ws.commons.soap;

import org.apache.ws.commons.om.OMElement;
import org.apache.ws.commons.om.OMNamespace;
import org.apache.ws.commons.om.OMText;

import java.util.Iterator;

public class SOAPFaultDetailTest extends SOAPFaultTestCase {
    protected SOAPFaultDetail soap11FaultDetail;
    protected SOAPFaultDetail soap12FaultDetail;
    protected SOAPFaultDetail soap11FaultDetailWithParser;
    protected SOAPFaultDetail soap12FaultDetailWithParser;
    protected OMNamespace omNamespace;

    public SOAPFaultDetailTest(String testName) {
        super(testName);
        omNamespace =
                omFactory.createOMNamespace("http://www.test.org", "test");
    }

    protected void setUp() throws Exception {
        super.setUp();
        soap11FaultDetail = soap11Factory.createSOAPFaultDetail(soap11Fault);
        soap12FaultDetail = soap12Factory.createSOAPFaultDetail(soap12Fault);
        soap11FaultDetailWithParser = soap11FaultWithParser.getDetail();
        soap12FaultDetailWithParser = soap12FaultWithParser.getDetail();
    }

    //SOAP 1.1 Fault Detail Test (Programaticaly Created)
    public void testSOAP11AddDetailEntry() {
        soap11FaultDetail.addDetailEntry(
                omFactory.createOMElement("DetailEntry1", omNamespace));
        soap11FaultDetail.addDetailEntry(
                omFactory.createOMElement("DetailEntry2", omNamespace));
        Iterator iterator = soap11FaultDetail.getAllDetailEntries();
        OMElement detailEntry1 = (OMElement) iterator.next();
        assertFalse(
                "SOAP 1.1 Fault Detail Test : - After calling addDetailEntry method twice, getAllDetailEntries method returns empty iterator",
                detailEntry1 == null);
        assertTrue(
                "SOAP 1.1 Fault Detail Test : - detailEntry1 local name mismatch",
                detailEntry1.getLocalName().equals("DetailEntry1"));
        assertTrue(
                "SOAP 1.1 Fault Detail Test : - detailEntry1 namespace uri mismatch",
                detailEntry1.getNamespace().getName().equals(
                        "http://www.test.org"));
        OMElement detailEntry2 = (OMElement) iterator.next();
        assertFalse(
                "SOAP 1.1 Fault Detail Test : - After calling addDetailEntry method twice, getAllDetailEntries method returns an iterator with only one object",
                detailEntry2 == null);
        assertTrue(
                "SOAP 1.1 Fault Detail Test : - detailEntry2 local name mismatch",
                detailEntry2.getLocalName().equals("DetailEntry2"));
        assertTrue(
                "SOAP 1.1 Fault Detail Test : - detailEntry2 namespace uri mismatch",
                detailEntry2.getNamespace().getName().equals(
                        "http://www.test.org"));
        assertTrue(
                "SOAP 1.1 Fault Detail Test : - After calling addDetailEntry method twice, getAllDetailEntries method returns an iterator with three objects",
                iterator.next() == null);
    }

    public void testSOAP11GetAllDetailEntries() {
        Iterator iterator = soap11FaultDetail.getAllDetailEntries();
        assertTrue(
                "SOAP 1.1 Fault Detail Test : - After creating SOAP11FaultDetail element, it has DetailEntries",
                iterator.next() == null);
        soap11FaultDetail.addDetailEntry(
                omFactory.createOMElement("DetailEntry", omNamespace));
        iterator = soap11FaultDetail.getAllDetailEntries();
        OMElement detailEntry = (OMElement) iterator.next();
        assertFalse(
                "SOAP 1.1 Fault Detail Test : - After calling addDetailEntry method, getAllDetailEntries method returns empty iterator",
                detailEntry == null);
        assertTrue(
                "SOAP 1.1 Fault Detail Test : - detailEntry local name mismatch",
                detailEntry.getLocalName().equals("DetailEntry"));
        assertTrue(
                "SOAP 1.1 Fault Detail Test : - detailEntry namespace uri mismatch",
                detailEntry.getNamespace().getName().equals(
                        "http://www.test.org"));
        assertTrue(
                "SOAP 1.1 Fault Detail Test : - After calling addDetailEntry method once, getAllDetailEntries method returns an iterator with two objects",
                iterator.next() == null);
    }

    //SOAP 1.2 Fault Detail Test (Programaticaly Created)
    public void testSOAP12AddDetailEntry() {
        soap12FaultDetail.addDetailEntry(
                omFactory.createOMElement("DetailEntry1", omNamespace));
        soap12FaultDetail.addDetailEntry(
                omFactory.createOMElement("DetailEntry2", omNamespace));
        Iterator iterator = soap12FaultDetail.getAllDetailEntries();
        OMElement detailEntry1 = (OMElement) iterator.next();
        assertFalse(
                "SOAP 1.2 Fault Detail Test : - After calling addDetailEntry method twice, getAllDetailEntries method returns empty iterator",
                detailEntry1 == null);
        assertTrue(
                "SOAP 1.2 Fault Detail Test : - detailEntry1 local name mismatch",
                detailEntry1.getLocalName().equals("DetailEntry1"));
        assertTrue(
                "SOAP 1.2 Fault Detail Test : - detailEntry1 namespace uri mismatch",
                detailEntry1.getNamespace().getName().equals(
                        "http://www.test.org"));
        OMElement detailEntry2 = (OMElement) iterator.next();
        assertFalse(
                "SOAP 1.2 Fault Detail Test : - After calling addDetailEntry method twice, getAllDetailEntries method returns an iterator with only one object",
                detailEntry2 == null);
        assertTrue(
                "SOAP 1.2 Fault Detail Test : - detailEntry2 local name mismatch",
                detailEntry2.getLocalName().equals("DetailEntry2"));
        assertTrue(
                "SOAP 1.2 Fault Detail Test : - detailEntry2 namespace uri mismatch",
                detailEntry2.getNamespace().getName().equals(
                        "http://www.test.org"));
        assertTrue(
                "SOAP 1.2 Fault Detail Test : - After calling addDetailEntry method twice, getAllDetailEntries method returns an iterator with three objects",
                iterator.next() == null);
    }

    public void testSOAP12GetAllDetailEntries() {
        Iterator iterator = soap12FaultDetail.getAllDetailEntries();
        assertTrue(
                "SOAP 1.2 Fault Detail Test : - After creating SOAP11FaultDetail element, it has DetailEntries",
                iterator.next() == null);
        soap12FaultDetail.addDetailEntry(
                omFactory.createOMElement("DetailEntry", omNamespace));
        iterator = soap12FaultDetail.getAllDetailEntries();
        OMElement detailEntry = (OMElement) iterator.next();
        assertFalse(
                "SOAP 1.2 Fault Detail Test : - After calling addDetailEntry method, getAllDetailEntries method returns empty iterator",
                detailEntry == null);
        assertTrue(
                "SOAP 1.2 Fault Detail Test : - detailEntry local name mismatch",
                detailEntry.getLocalName().equals("DetailEntry"));
        assertTrue(
                "SOAP 1.2 Fault Detail Test : - detailEntry namespace uri mismatch",
                detailEntry.getNamespace().getName().equals(
                        "http://www.test.org"));
        assertTrue(
                "SOAP 1.2 Fault Detail Test : - After calling addDetailEntry method once, getAllDetailEntries method returns an iterator with two objects",
                iterator.next() == null);
    }

    //SOAP 1.1 Fault Detail Test (With Parser)
    public void testSOAP11GetAllDetailEntriesWithParser() {
        Iterator iterator = soap11FaultDetailWithParser.getAllDetailEntries();
        OMText textEntry = (OMText) iterator.next();
        assertFalse(
                "SOAP 1.1 Fault Detail Test With Parser : - getAllDetailEntries method returns empty iterator",
                textEntry == null);
        assertTrue(
                "SOAP 1.1 Fault Detail Test With Parser : - text value mismatch",
                textEntry.getText().trim().equals("Details of error"));
        OMElement detailEntry1 = (OMElement) iterator.next();
        assertFalse(
                "SOAP 1.1 Fault Detail Test With Parser : - getAllDetailEntries method returns an itrator without detail entries",
                detailEntry1 == null);
        assertTrue(
                "SOAP 1.1 Fault Detail Test With Parser : - detailEntry1 localname mismatch",
                detailEntry1.getLocalName().equals("MaxTime"));
        iterator.next();
        OMElement detailEntry2 = (OMElement) iterator.next();
        assertFalse(
                "SOAP 1.1 Fault Detail Test With Parser : - getAllDetailEntries method returns an itrator with only one detail entries",
                detailEntry2 == null);
        assertTrue(
                "SOAP 1.1 Fault Detail Test With Parser : - detailEntry2 localname mismatch",
                detailEntry2.getLocalName().equals("AveTime"));
        iterator.next();
        assertTrue(
                "SOAP 1.1 Fault Detail Test With Parser : - getAllDetailEntries method returns an itrator with more than two detail entries",
                iterator.next() == null);
    }

    //SOAP 1.2 Fault Detail Test (With Parser)
    public void testSOAP12GetAllDetailEntriesWithParser() {
        Iterator iterator = soap12FaultDetailWithParser.getAllDetailEntries();
        OMText textEntry = (OMText) iterator.next();
        assertFalse(
                "SOAP 1.2 Fault Detail Test With Parser : - getAllDetailEntries method returns empty iterator",
                textEntry == null);
        assertTrue(
                "SOAP 1.2 Fault Detail Test With Parser : - text value mismatch",
                textEntry.getText().trim().equals("Details of error"));
        OMElement detailEntry1 = (OMElement) iterator.next();
        assertFalse(
                "SOAP 1.2 Fault Detail Test With Parser : - getAllDetailEntries method returns an itrator without detail entries",
                detailEntry1 == null);
        assertTrue(
                "SOAP 1.2 Fault Detail Test With Parser : - detailEntry1 localname mismatch",
                detailEntry1.getLocalName().equals("MaxTime"));
        iterator.next();
        OMElement detailEntry2 = (OMElement) iterator.next();
        assertFalse(
                "SOAP 1.2 Fault Detail Test With Parser : - getAllDetailEntries method returns an itrator with only one detail entries",
                detailEntry2 == null);
        assertTrue(
                "SOAP 1.2 Fault Detail Test With Parser : - detailEntry2 localname mismatch",
                detailEntry2.getLocalName().equals("AveTime"));
        iterator.next();
        assertTrue(
                "SOAP 1.2 Fault Detail Test With Parser : - getAllDetailEntries method returns an itrator with more than two detail entries",
                iterator.next() == null);
    }
}
