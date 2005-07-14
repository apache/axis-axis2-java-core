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

import org.apache.axis2.om.OMAbstractFactory;
import org.apache.axis2.soap.impl.llom.soap12.SOAP12Constants;

public class SOAPFaultTest extends SOAPFaultTestCase {

    public SOAPFaultTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        super.setUp();
    }

    //SOAP 1.1 Fault Test (Programaticaly created)-----------------------------------------------------------------------------------
    public void testSOAP11SetCode() {
        soap11Fault.setCode(soap11Factory.createSOAPFaultCode(soap11Fault));
        assertNotNull(
                "SOAP 1.1 Fault Test:- After calling setCode method, Fault has no code",
                soap11Fault.getCode());
        try {
            soap11Fault.setCode(soap12Factory.createSOAPFaultCode(soap12Fault));
            fail("SOAP12FaultCode should not not be set in to a SOAP11Fault");
        } catch (Exception e) {
        }
    }

    public void testSOAP11GetCode() {
        assertTrue(
                "SOAP 1.1 Fault Test:- After creating a SOAP11Fault, it has a code",
                soap11Fault.getCode() == null);
        soap11Fault.setCode(soap11Factory.createSOAPFaultCode(soap11Fault));
        assertFalse(
                "SOAP 1.1 Fault Test:- After calling setCode method, Fault has no code",
                soap11Fault.getCode() == null);
    }

    public void testSOAP11SetReason() {
        soap11Fault.setReason(soap11Factory.createSOAPFaultReason(soap11Fault));
        assertFalse(
                "SOAP 1.1 Fault Test:- After calling setReason method, Fault has no reason",
                soap11Fault.getReason() == null);
        try {
            soap11Fault.setReason(
                    soap12Factory.createSOAPFaultReason(soap12Fault));
            fail("SOAP12FaultReason should not be set in to a SOAP11Fault");

        } catch (Exception e) {
            assertTrue(true);
        }
    }

    public void testSOAP11GetReason() {
        assertTrue(
                "SOAP 1.1 Fault Test:- After creating a SOAP11Fault, it has a reason",
                soap11Fault.getReason() == null);
        soap11Fault.setReason(soap11Factory.createSOAPFaultReason(soap11Fault));
        assertFalse(
                "SOAP 1.1 Fault Test:- After calling setReason method, Fault has no reason",
                soap11Fault.getReason() == null);
    }

    public void testSOAP11SetNode() {
        soap11Fault.setNode(soap11Factory.createSOAPFaultNode(soap11Fault));
        assertFalse(
                "SOAP 1.1 Fault Test:- After calling setNode method, Fault has no node",
                soap11Fault.getNode() == null);
        try {
            soap11Fault.setNode(soap12Factory.createSOAPFaultNode(soap12Fault));
            fail("SOAP12FaultNode should not be set in to a SOAP11Fault");

        } catch (Exception e) {
            assertTrue(true);

        }
    }

    public void testSOAP11GetNode() {
        assertTrue(
                "SOAP 1.1 Fault Test:- After creating a SOAP11Fault, it has a node",
                soap11Fault.getNode() == null);
        soap11Fault.setNode(soap11Factory.createSOAPFaultNode(soap11Fault));
        assertFalse(
                "SOAP 1.1 Fault Test:- After calling setNode method, Fault has no node",
                soap11Fault.getNode() == null);
    }

    public void testSOAP11SetRole() {
        soap11Fault.setRole(soap11Factory.createSOAPFaultRole(soap11Fault));
        assertFalse(
                "SOAP 1.1 Fault Test:- After calling setRole method, Fault has no role",
                soap11Fault.getRole() == null);
        try {
            soap11Fault.setRole(soap12Factory.createSOAPFaultRole(soap12Fault));
            fail("SOAP12FaultRole should not be set in to a SOAP11Fault");
        } catch (Exception e) {
            assertTrue(true);
        }
    }

    public void testSOAP11GetRole() {
        assertTrue(
                "SOAP 1.1 Fault Test:- After creating a SOAP11Fault, it has a role",
                soap11Fault.getRole() == null);
        soap11Fault.setRole(soap11Factory.createSOAPFaultRole(soap11Fault));
        assertFalse(
                "SOAP 1.1 Fault Test:- After calling setRole method, Fault has no role",
                soap11Fault.getRole() == null);
    }

    public void testSOAP11SetDetail() {
        soap11Fault.setDetail(soap11Factory.createSOAPFaultDetail(soap11Fault));
        assertFalse(
                "SOAP 1.1 Fault Test:- After calling setDetail method, Fault has no detail",
                soap11Fault.getDetail() == null);
        try {
            soap11Fault.setDetail(
                    soap12Factory.createSOAPFaultDetail(soap12Fault));
            fail("SOAP12FaultDetail should not be set in to a SOAP11Fault");
        } catch (Exception e) {
            assertTrue(true);
        }
    }

    public void testSOAP11GetDetail() {
        assertTrue(
                "SOAP 1.1 Fault Test:- After creating a SOAP11Fault, it has a detail",
                soap11Fault.getDetail() == null);
        soap11Fault.setDetail(soap11Factory.createSOAPFaultDetail(soap11Fault));
        assertFalse(
                "SOAP 1.1 Fault Test:- After calling setDetail method, Fault has no detail",
                soap11Fault.getDetail() == null);
    }

    //SOAP 1.2 Fault Test ((Programaticaly created)--------------------------------------------------------------------------------
    public void testSOAP12SetCode() {
        soap12Fault.setCode(soap12Factory.createSOAPFaultCode(soap12Fault));
        assertFalse(
                "SOAP 1.2 Fault Test:- After calling setCode method, Fault has no code",
                soap12Fault.getCode() == null);
        assertTrue("SOAP 1.2 Fault Test:- Code local name mismatch",
                soap12Fault.getCode().getLocalName().equals(
                        SOAP12Constants.SOAP_FAULT_CODE_LOCAL_NAME));
        try {
            soap12Fault.setCode(soap11Factory.createSOAPFaultCode(soap11Fault));
            fail("SOAP11FaultCode should not be set in to a SOAP12Fault");
        } catch (Exception e) {
            assertTrue(true);
        }
    }

    public void testSOAP12GetCode() {
        assertTrue(
                "SOAP 1.2 Fault Test:- After creating a SOAP12Fault, it has a code",
                soap12Fault.getCode() == null);
        soap12Fault.setCode(soap12Factory.createSOAPFaultCode(soap12Fault));
        assertFalse(
                "SOAP 1.2 Fault Test:- After calling setCode method, Fault has no code",
                soap12Fault.getCode() == null);
        assertTrue("SOAP 1.2 Fault Test:- Fault code local name mismatch",
                soap12Fault.getCode().getLocalName().equals(
                        SOAP12Constants.SOAP_FAULT_CODE_LOCAL_NAME));
    }

    public void testSOAP12SetReason() {
        soap12Fault.setReason(soap12Factory.createSOAPFaultReason(soap12Fault));
        assertFalse(
                "SOAP 1.2 Fault Test:- After calling setReason method, Fault has no reason",
                soap12Fault.getReason() == null);
        assertTrue("SOAP 1.2 Fault Test:- Fault reason local name mismatch",
                soap12Fault.getReason().getLocalName().equals(
                        SOAP12Constants.SOAP_FAULT_REASON_LOCAL_NAME));
        try {
            soap12Fault.setReason(
                    soap11Factory.createSOAPFaultReason(soap11Fault));
            fail("SOAP11FaultReason should not be set in to a SOAP12Fault");

        } catch (Exception e) {
            assertTrue(true);
        }
    }

    public void testSOAP12GetReason() {
        assertTrue(
                "SOAP 1.2 Fault Test:- After creating a SOAP12Fault, it has a reason",
                soap12Fault.getReason() == null);
        soap12Fault.setReason(soap12Factory.createSOAPFaultReason(soap12Fault));
        assertFalse(
                "SOAP 1.2 Fault Test:- After calling setReason method, Fault has no reason",
                soap12Fault.getReason() == null);
        assertTrue("SOAP 1.2 Fault Test:- Fault reason local name mismatch",
                soap12Fault.getReason().getLocalName().equals(
                        SOAP12Constants.SOAP_FAULT_REASON_LOCAL_NAME));
    }

    public void testSOAP12SetNode() {
        soap12Fault.setNode(soap12Factory.createSOAPFaultNode(soap12Fault));
        assertFalse(
                "SOAP 1.2 Fault Test:- After calling setNode method, Fault has no node",
                soap12Fault.getNode() == null);
        assertTrue("SOAP 1.2 Fault Test:- Fault node local name mismatch",
                soap12Fault.getNode().getLocalName().equals(
                        SOAP12Constants.SOAP_FAULT_NODE_LOCAL_NAME));
        try {
            soap12Fault.setNode(soap11Factory.createSOAPFaultNode(soap11Fault));
            fail("SOAP11FaultNode should nott be set in to a SOAP12Fault");

        } catch (Exception e) {
            assertTrue(true);
        }
    }

    public void testSOAP12GetNode() {
        assertTrue(
                "SOAP 1.2 Fault Test:- After creating a SOAP12Fault, it has a node",
                soap12Fault.getNode() == null);
        soap12Fault.setNode(soap12Factory.createSOAPFaultNode(soap12Fault));
        assertFalse(
                "SOAP 1.2 Fault Test:- After calling setNode method, Fault has no node",
                soap12Fault.getNode() == null);
        assertTrue("SOAP 1.2 Fault Test:- Fault node local name mismatch",
                soap12Fault.getNode().getLocalName().equals(
                        SOAP12Constants.SOAP_FAULT_NODE_LOCAL_NAME));
    }

    public void testSOAP12SetRole() {
        soap12Fault.setRole(soap12Factory.createSOAPFaultRole(soap12Fault));
        assertFalse(
                "SOAP 1.2 :- After calling setRole method, Fault has no role",
                soap12Fault.getRole() == null);
        assertTrue("SOAP 1.2 Fault Test:- Fault role local name mismatch",
                soap12Fault.getRole().getLocalName().equals(
                        SOAP12Constants.SOAP_FAULT_ROLE_LOCAL_NAME));
        try {
            soap12Fault.setRole(soap11Factory.createSOAPFaultRole(soap11Fault));
            fail("SOAP11FaultRole should not be set in to a SOAP12Fault");
        } catch (Exception e) {
            assertTrue(true);
        }
    }

    public void testSOAP12GetRole() {
        assertTrue(
                "SOAP 1.2 Fault Test:- After creating a SOAP11Fault, it has a role",
                soap12Fault.getRole() == null);
        soap12Fault.setRole(soap12Factory.createSOAPFaultRole(soap12Fault));
        assertFalse(
                "SOAP 1.2 Fault Test:- After calling setRole method, Fault has no role",
                soap12Fault.getRole() == null);
        assertTrue("SOAP 1.2 Fault Test:- Fault role local name mismatch",
                soap12Fault.getRole().getLocalName().equals(
                        SOAP12Constants.SOAP_FAULT_ROLE_LOCAL_NAME));
    }

    public void testSOAP12SetDetail() {
        soap12Fault.setDetail(soap12Factory.createSOAPFaultDetail(soap12Fault));
        assertFalse(
                "SOAP 1.2 Fault Test:- After calling setDetaile method, Fault has no detail",
                soap12Fault.getDetail() == null);
        assertTrue("SOAP 1.2 Fault Test:- Fault detail local name mismatch",
                soap12Fault.getDetail().getLocalName().equals(
                        SOAP12Constants.SOAP_FAULT_DETAIL_LOCAL_NAME));
        try {
            soap12Fault.setDetail(
                    soap11Factory.createSOAPFaultDetail(soap11Fault));
            fail("SOAP11FaultDetail should not be set in to a SOAP12Fault");

        } catch (Exception e) {
            assertTrue(true);
        }
    }

    public void testSOAP12GetDetail() {
        assertTrue(
                "SOAP 1.2 Fault Test:- After creating a SOAP12Fault, it has a detail",
                soap12Fault.getDetail() == null);
        soap12Fault.setDetail(soap12Factory.createSOAPFaultDetail(soap12Fault));
        assertFalse(
                "SOAP 1.2 Fault Test:- After calling setDetail method, Fault has no detail",
                soap12Fault.getDetail() == null);
        assertTrue("SOAP 1.2 Fault Test:- Fault detail local name mismatch",
                soap12Fault.getDetail().getLocalName().equals(
                        SOAP12Constants.SOAP_FAULT_DETAIL_LOCAL_NAME));
    }

    //SOAP 1.1 Fault Test (With parser)
    public void testSOAP11GetCodeWithParser() {
        assertFalse(
                "SOAP 1.1 Fault Test with parser: - getCode method returns null",
                soap11FaultWithParser.getCode() == null);
    }

    public void testSOAP11GetRoleWithParser() {
        assertFalse(
                "SOAP 1.1 Fault Test with parser: - getRole method returns null",
                soap11FaultWithParser.getRole() == null);
    }

    public void testSOAP11GetDetailWithParser() {
        assertFalse(
                "SOAP 1.1 Fault Test with parser: - getDetail method returns null",
                soap11FaultWithParser.getDetail() == null);
    }

    //SOAP 1.2 Fault Test (With parser)
    public void testSOAP12GetCodeWithParser() {
        assertFalse(
                "SOAP 1.2 Fault Test with parser: - getCode method returns null",
                soap12FaultWithParser.getCode() == null);
        assertTrue(
                "SOAP 1.2 Fault Test with parser: - Fault code local name mismatch",
                soap12FaultWithParser.getCode().getLocalName().equals(
                        SOAP12Constants.SOAP_FAULT_CODE_LOCAL_NAME));
    }

    public void testSOAP12GetReasonWithParser() {
        assertFalse(
                "SOAP 1.2 Fault Test with parser: - getReason method returns null",
                soap12FaultWithParser.getReason() == null);
        assertTrue(
                "SOAP 1.2 Fault Test with parser: - Fault reason local name mismatch",
                soap12FaultWithParser.getReason().getLocalName().equals(
                        SOAP12Constants.SOAP_FAULT_REASON_LOCAL_NAME));
    }

    public void testSOAP12GetNodeWithParser() {
        assertFalse(
                "SOAP 1.2 Fault Test with parser: - getNode method returns null",
                soap12FaultWithParser.getNode() == null);
        assertTrue(
                "SOAP 1.2 Fault Test with parser: - Fault node local name mismatch",
                soap12FaultWithParser.getNode().getLocalName().equals(
                        SOAP12Constants.SOAP_FAULT_NODE_LOCAL_NAME));
    }

    public void testSOAP12GetRoleWithParser() {
        assertFalse(
                "SOAP 1.2 Fault Test with parser: - getRole method returns null",
                soap12FaultWithParser.getRole() == null);
        assertTrue(
                "SOAP 1.2 Fault Test with parser: - Fault role local name mismatch",
                soap12FaultWithParser.getRole().getLocalName().equals(
                        SOAP12Constants.SOAP_FAULT_ROLE_LOCAL_NAME));
    }

    public void testSOAP12GetDetailWithParser() {
        assertFalse(
                "SOAP 1.2 Fault Test with parser: - getDetail method returns null",
                soap12FaultWithParser.getDetail() == null);
        assertTrue(
                "SOAP 1.2 Fault Test with parser: - Fault detail local name mismatch",
                soap12FaultWithParser.getDetail().getLocalName().equals(
                        SOAP12Constants.SOAP_FAULT_DETAIL_LOCAL_NAME));
    }

    public void testMoreChildrenAddition() {
        org.apache.axis2.om.impl.OMOutputImpl output = null;
        try {
            output = new org.apache.axis2.om.impl.OMOutputImpl(System.out, false);
            SOAPFactory soapFactory = OMAbstractFactory.getSOAP12Factory();
            SOAPEnvelope envelope = soapFactory.getDefaultFaultEnvelope();

            assertNotNull("Default FaultEnvelope must have a SOAPFault in it",
                    envelope.getBody().getFault());
            assertNotNull(
                    "Default FaultEnvelope must have a SOAPFaultCode in it",
                    envelope.getBody().getFault().getCode());
            assertNotNull(
                    "Default FaultEnvelope must have a SOAPFaultCodeValue in it",
                    envelope.getBody().getFault().getCode().getValue());
            assertNotNull(
                    "Default FaultEnvelope must have a SOAPFaultReason in it",
                    envelope.getBody().getFault().getReason());
            assertNotNull(
                    "Default FaultEnvelope must have a SOAPFaultText in it",
                    envelope.getBody().getFault().getReason().getSOAPText());

            SOAPEnvelope soapEnvelope = soapFactory.getDefaultFaultEnvelope();
            String errorCodeString = "Some Error occurred !!";
            soapEnvelope.getBody().getFault().getCode().getValue().setText(
                    errorCodeString);

            SOAPFaultCode code = soapEnvelope.getBody().getFault().getCode();
            envelope.getBody().getFault().setCode(code);

            assertTrue("Parent Value of Code has not been set to new fault",
                    code.getParent() == envelope.getBody().getFault());
            assertTrue("Parent Value of Code is still pointing to old fault",
                    code.getParent() != soapEnvelope.getBody().getFault());
            assertNull("Old fault must not have a fault code",
                    soapEnvelope.getBody().getFault().getCode());
            assertEquals("The SOAP Code value must be " + errorCodeString,
                    errorCodeString,
                    envelope.getBody().getFault().getCode().getValue().getText());

        } catch (Exception e) {
            fail(e.getMessage());
        }


    }
}