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

import java.util.Iterator;

public class SOAPHeaderBlockTest extends SOAPHeaderTestCase {
    protected SOAPHeaderBlock soap11HeaderBlock;
    protected SOAPHeaderBlock soap12HeaderBlock;
    protected SOAPHeaderBlock soap11HeaderBlock1WithParser;
    protected SOAPHeaderBlock soap12HeaderBlock1WithParser;
    protected SOAPHeaderBlock soap11HeaderBlock2WithParser;
    protected SOAPHeaderBlock soap12HeaderBlock2WithParser;
    protected SOAPHeaderBlock soap11HeaderBlock3WithParser;
    protected SOAPHeaderBlock soap12HeaderBlock3WithParser;

    public SOAPHeaderBlockTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        super.setUp();
        soap11HeaderBlock =
                soap11Factory.createSOAPHeaderBlock("testHeaderBlock",
                        namespace,
                        soap11Header);
        soap12HeaderBlock =
                soap12Factory.createSOAPHeaderBlock("testHeaderBlock",
                        namespace,
                        soap12Header);
        Iterator iterator = soap11HeaderWithParser.examineAllHeaderBlocks();
        iterator.next();
        soap11HeaderBlock1WithParser = (SOAPHeaderBlock) iterator.next();
        iterator.next();
        soap11HeaderBlock2WithParser = (SOAPHeaderBlock) iterator.next();
        iterator.next();
        soap11HeaderBlock3WithParser = (SOAPHeaderBlock) iterator.next();

        iterator = soap12HeaderWithParser.examineAllHeaderBlocks();
        iterator.next();
        soap12HeaderBlock1WithParser = (SOAPHeaderBlock) iterator.next();
        iterator.next();
        soap12HeaderBlock2WithParser = (SOAPHeaderBlock) iterator.next();
        iterator.next();
        soap12HeaderBlock3WithParser = (SOAPHeaderBlock) iterator.next();
    }

    //SOAP 1.1 SOAPHeaderBlock Test (Programaticaly Created)
    public void testSOAP11SetRole() {
        soap11HeaderBlock.setRole(
                "http://schemas.xmlsoap.org/soap/envelope/actor/next");
        assertTrue(
                "SOAP 1.1 HeaderBlock Test : - After calling setRole method, getRole method returns incorrect role value",
                soap11HeaderBlock.getRole().equals(
                        "http://schemas.xmlsoap.org/soap/envelope/actor/next"));
        try {
            soap11HeaderBlock.setRole("Any Value");
        } catch (Exception e) {
            fail(
                    "SOAP 1.1 HeaderBlock Test : - role value can not be set to any value");
        }
    }

    public void testSOAP11GetRole() {
        assertTrue(
                "SOAP 1.1 HeaderBlock Test : - After creating SOAPHeaderBlock, it has a role",
                soap11HeaderBlock.getRole() == null);
        soap11HeaderBlock.setRole(
                "http://schemas.xmlsoap.org/soap/envelope/actor/next");
        assertTrue(
                "SOAP 1.1 HeaderBlock Test : - After calling setRole method, getRole method returns incorrect role value",
                soap11HeaderBlock.getRole().equals(
                        "http://schemas.xmlsoap.org/soap/envelope/actor/next"));
    }

    public void testSOAP11SetMustUnderstand() {
        soap11HeaderBlock.setMustUnderstand(true);
        assertTrue(
                "SOAP 1.1 HeaderBlock Test : - After setting MustUnderstand true calling setMustUnderstand method , getMustUnderstand method returns false",
                soap11HeaderBlock.getMustUnderstand());
        soap11HeaderBlock.setMustUnderstand(false);
        assertFalse(
                "SOAP 1.1 HeaderBlock Test : - After setting MustUnderstand false calling setMustUnderstand method , getMustUnderstand method returns true",
                soap11HeaderBlock.getMustUnderstand());
        soap11HeaderBlock.setMustUnderstand("1");
        assertTrue(
                "SOAP 1.1 HeaderBlock Test : - After setting MustUnderstand \"1\" calling setMustUnderstand method , getMustUnderstand method returns false",
                soap11HeaderBlock.getMustUnderstand());
        soap11HeaderBlock.setMustUnderstand("0");
        assertFalse(
                "SOAP 1.1 HeaderBlock Test : - After setting MustUnderstand \"0\" calling setMustUnderstand method , getMustUnderstand method returns true",
                soap11HeaderBlock.getMustUnderstand());
        try {
            soap11HeaderBlock.setMustUnderstand("true");
        } catch (Exception e) {
            fail(
                    "SOAP 1.1 HeaderBlock Test : - MustUnderstatnd value can not be set to any value rather than 1 or 0");
        }
    }

    public void testSOAP11GetMustUnderstand() {
        assertFalse(
                "SOAP 1.1 HeaderBlock Test : - After creating SOAPHeaderBlock, default MustUnderstand value true",
                soap11HeaderBlock.getMustUnderstand());
        soap11HeaderBlock.setMustUnderstand(true);
        assertTrue(
                "SOAP 1.1 HeaderBlock Test : - After setting MustUnderstand true calling setMustUnderstand method , getMustUnderstand method returns false",
                soap11HeaderBlock.getMustUnderstand());
    }

    //SOAP 1.2 SOAPHeaderBlock Test (Programaticaly Created)
    public void testSOAP12SetRole() {
        soap12HeaderBlock.setRole(
                "http://www.w3.org/2003/05/soap-envelope/role/ultimateReceiver");
        assertTrue(
                "SOAP 1.2 HeaderBlock Test : - After calling setRole method, getRole method returns incorrect role value",
                soap12HeaderBlock.getRole().equals(
                        "http://www.w3.org/2003/05/soap-envelope/role/ultimateReceiver"));
        try {
            soap12HeaderBlock.setRole("Any Value");
        } catch (Exception e) {
            fail(
                    "SOAP 1.2 HeaderBlock Test : - role value can not be set to any value");
        }
    }

    public void testSOAP12GetRole() {
        assertTrue(
                "SOAP 1.2 HeaderBlock Test : - After creating SOAPHeaderBlock, it has a role",
                soap12HeaderBlock.getRole() == null);
        soap12HeaderBlock.setRole(
                "http://www.w3.org/2003/05/soap-envelope/role/ultimateReceiver");
        assertTrue(
                "SOAP 1.2 HeaderBlock Test : - After calling setRole method, getRole method returns incorrect role value",
                soap12HeaderBlock.getRole().equals(
                        "http://www.w3.org/2003/05/soap-envelope/role/ultimateReceiver"));
    }

    public void testSOAP12SetMustUnderstand() {
        soap12HeaderBlock.setMustUnderstand(true);
        assertTrue(
                "SOAP 1.2 HeaderBlock Test : - After setting MustUnderstand true calling setMustUnderstand method , getMustUnderstand method returns false",
                soap12HeaderBlock.getMustUnderstand());
        soap12HeaderBlock.setMustUnderstand(false);
        assertFalse(
                "SOAP 1.2 HeaderBlock Test : - After setting MustUnderstand false calling setMustUnderstand method , getMustUnderstand method returns true",
                soap12HeaderBlock.getMustUnderstand());
        soap12HeaderBlock.setMustUnderstand("true");
        assertTrue(
                "SOAP 1.2 HeaderBlock Test : - After setting MustUnderstand \"true\" calling setMustUnderstand method , getMustUnderstand method returns false",
                soap12HeaderBlock.getMustUnderstand());
        soap12HeaderBlock.setMustUnderstand("false");
        assertFalse(
                "SOAP 1.2 HeaderBlock Test : - After setting MustUnderstand \"false\" calling setMustUnderstand method , getMustUnderstand method returns true",
                soap12HeaderBlock.getMustUnderstand());
        soap12HeaderBlock.setMustUnderstand("1");
        assertTrue(
                "SOAP 1.2 HeaderBlock Test : - After setting MustUnderstand \"1\" calling setMustUnderstand method , getMustUnderstand method returns false",
                soap12HeaderBlock.getMustUnderstand());
        soap12HeaderBlock.setMustUnderstand("0");
        assertFalse(
                "SOAP 1.2 HeaderBlock Test : - After setting MustUnderstand \"0\" calling setMustUnderstand method , getMustUnderstand method returns true",
                soap12HeaderBlock.getMustUnderstand());
        try {
            soap12HeaderBlock.setMustUnderstand("otherValue");
            fail(
                    "SOAP 1.2 HeaderBlock Test : - MustUnderstatnd value can not be set to any value rather than 1 , 0 , true , false");

        } catch (Exception e) {
            assertTrue(true);
        }
    }

    public void testSOAP12GetMustUnderstand() {
        assertFalse(
                "SOAP 1.2 HeaderBlock Test : - After creating SOAPHeaderBlock, default MustUnderstand value true",
                soap12HeaderBlock.getMustUnderstand());
        soap12HeaderBlock.setMustUnderstand(true);
        assertTrue(
                "SOAP 1.2 HeaderBlock Test : - After setting MustUnderstand true calling setMustUnderstand method , getMustUnderstand method returns false",
                soap12HeaderBlock.getMustUnderstand());
    }

    //SOAP 1.1 SOAPHeaderBlock Test (With Parser)
    public void testSOAP11GetRoleWithParser() {
        assertTrue(
                "SOAP 1.1 HeaderBlock Test Wiht Parser : - getRole method returns incorrect role value",
                soap11HeaderBlock1WithParser.getRole().equals(
                        "http://schemas.xmlsoap.org/soap/actor/next"));
    }

    public void testSOAP11GetMustUnderstandWithParser() {
        assertTrue(
                "SOAP 1.1 HeaderBlock Test Wiht Parser : - getMustUnderstatnd method returns incorrect value",
                soap11HeaderBlock2WithParser.getMustUnderstand());
        assertFalse(
                "SOAP 1.1 HeaderBlock Test Wiht Parser : - getMustUnderstatnd method returns incorrect value",
                soap11HeaderBlock3WithParser.getMustUnderstand());

    }

    //SOAP 1.2 SOAPHeaderBlock Test (With Parser)
    public void testSOAP12GetRoleWithParser() {
        assertTrue(
                "SOAP 1.2 HeaderBlock Test Wiht Parser : - getRole method returns incorrect role value",
                soap12HeaderBlock1WithParser.getRole().equals(
                        "http://www.w3.org/2003/05/soap-envelope/role/ultimateReceiver"));
    }

    public void testSOAP12GetMustUnderstandWithParser() {
        assertTrue(
                "SOAP 1.2 HeaderBlock Test Wiht Parser : - getMustUnderstatnd method returns incorrect value",
                soap12HeaderBlock1WithParser.getMustUnderstand());
        assertFalse(
                "SOAP 1.2 HeaderBlock Test Wiht Parser : - getMustUnderstatnd method returns incorrect value",
                soap12HeaderBlock2WithParser.getMustUnderstand());
        try {
            soap12HeaderBlock3WithParser.getMustUnderstand();
        } catch (Exception e) {
            fail(
                    "SOAP 1.2 HeaderBlock Test Wiht Parser : - getMustUnderstatnd method should returns exception when mustunderstand value is incorrect");
        }
    }
}
