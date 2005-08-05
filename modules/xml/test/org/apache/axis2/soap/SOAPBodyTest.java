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

package org.apache.axis2.soap;

import org.apache.axis2.soap.impl.llom.SOAPConstants;
import org.apache.axis2.om.OMException;

public class SOAPBodyTest extends SOAPBodyTestCase {

    public SOAPBodyTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        super.setUp();
    }

    //SOAP 1.1 Body Test (Programaticaly created)----------------------------------------------------------------------------------
    public void testSOAP11AddFault1() {
        soap11Body.addFault(new Exception("This an exception for testing"));
        assertTrue(
                "SOAP 1.1 Body Test:- After calling addFault method, SOAP body has no fault",
                soap11Body.hasFault());

    }

    public void testSOAP11addFault2() {
        soap11Body.addFault(soap11Factory.createSOAPFault(soap11Body));
        assertTrue(
                "SOAP 1.1 Body Test:- After calling addFault method, SOAP body has no fault",
                soap11Body.hasFault());


    }

    public void testSOAP11HasFault() {
        assertFalse(
                "SOAP 1.1 Body Test:- After creating a soap body it has a fault",
                soap11Body.hasFault());
        soap11Body.addFault(new Exception("This an exception for testing"));
        assertTrue(
                "SOAP 1.1 Body Test:- After calling addFault method, hasFault method returns false",
                soap11Body.hasFault());
    }

    public void testSOAP11GetFault() {
        assertTrue(
                "SOAP 1.1 Body Test:- After creating a soap body it has a fault",
                soap11Body.getFault() == null);
        soap11Body.addFault(new Exception("This an exception for testing"));
        assertFalse(
                "SOAP 1.1 Body Test:- After calling addFault method, getFault method returns null",
                soap11Body.getFault() == null);
    }

    //SOAP 1.2 Body Test (Programaticaly Created)----------------------------------------------------------------------------------
    public void testSOAP12AddFault1() {
        soap12Body.addFault(new Exception("This an exception for testing"));
        assertTrue(
                "SOAP 1.2 Body Test:- After calling addFault method, SOAP body has no fault",
                soap12Body.hasFault());

    }

    public void testSOAP12AddFault2() {
        soap12Body.addFault(soap12Factory.createSOAPFault(soap12Body));
        assertTrue(
                "SOAP 1.2 Body Test:- After calling addFault method, SOAP body has no fault",
                soap12Body.hasFault());
    }

    public void testSOAP12HasFault() {
        assertFalse(
                "SOAP 1.2 Body Test:- After creating a soap body it has a fault",
                soap12Body.hasFault());
        soap12Body.addFault(new Exception("This an exception for testing"));
        assertTrue(
                "SOAP 1.2 Body Test:- After calling addFault method, hasFault method returns false",
                soap12Body.hasFault());
    }

    public void testSOAP12GetFault() {
        assertTrue(
                "SOAP 1.2 Body Test:- After creating a soap body it has a fault",
                soap12Body.getFault() == null);
        soap12Body.addFault(new Exception("This an exception for testing"));
        assertFalse(
                "SOAP 1.2 Body Test:- After calling addFault method, getFault method returns null",
                soap12Body.getFault() == null);
    }

    //SOAP 1.1 Body Test (With Parser)-------------------------------------------------------------------------------------------
    public void testSOAP11HasFaultWithParser() {
        assertTrue(
                "SOAP 1.1 Body Test With parser :- hasFault method returns false",
                soap11BodyWithParser.hasFault());
    }

    public void testSOAP11GetFaultWithParser() {
        assertFalse(
                "SOAP 1.1 Body Test With parser :- getFault method returns null",
                soap11BodyWithParser.getFault() == null);
        assertTrue(
                "SOAP 1.1 Body Test With parser : - SOAP fault name mismatch",
                soap11BodyWithParser.getFault().getLocalName().equals(
                        SOAPConstants.SOAPFAULT_LOCAL_NAME));
    }

    //SOAP 1.2 Body Test (With Parser)-------------------------------------------------------------------------------------------------
    public void testSOAP12HasFaultWithParser() {
        assertTrue(
                "SOAP 1.2 Body Test With parser :- hasFault method returns false",
                soap12BodyWithParser.hasFault());
    }

    public void testSOAP12GetFaultWithParser() {
        assertFalse(
                "SOAP 1.2 Body Test With parser :- getFault method returns null",
                soap12BodyWithParser.getFault() == null);
        assertTrue(
                "SOAP 1.2 Body Test With parser : - SOAP fault name mismatch",
                soap12BodyWithParser.getFault().getLocalName().equals(
                        SOAPConstants.SOAPFAULT_LOCAL_NAME));
    }

    public void testSOAPBodyDetachment(){
        try {
            soap11Body.detach();
            fail("Detachment of SOAP Body is not allowed !!");
        } catch (OMException e) {
            assertTrue(true);
        }

        try {
            soap12Body.detach();
            fail("Detachment of SOAP Body is not allowed !!");
        } catch (OMException e) {
            assertTrue(true);
        }
    }
}
