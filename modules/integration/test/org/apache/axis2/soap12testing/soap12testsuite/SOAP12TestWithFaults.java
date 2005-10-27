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

package org.apache.axis2.soap12testing.soap12testsuite;

import junit.framework.TestCase;
import test.soap12testing.client.MessageComparator;
import test.soap12testing.client.SOAP12TestClient;
import test.soap12testing.server.SimpleServer;

public class SOAP12TestWithFaults extends TestCase {
    private SimpleServer server;
    private MessageComparator comparator;
    private SOAP12TestClient client;

    public SOAP12TestWithFaults() {
        server = new SimpleServer(8007);
        server.start();
        comparator = new MessageComparator();
        client = new SOAP12TestClient();
    }

    protected void setUp() {

    }

    public void testWithFaults() {
        //Test No. 6 to 9 : - There are intermediaries

        //Test No. 12 - 14 : - Reply message has fault
        //assertTrue("SOAP 1.2 Test : - Test No. 12  Failed", comparator.compare("12", client.getRelpy(8007, "SOAP12TestServiceC", "12")));
        //assertTrue("SOAP 1.2 Test : - Test No. 13  Failed", comparator.compare("13", client.getRelpy(8007, "SOAP12TestServiceC", "13")));
        //assertTrue("SOAP 1.2 Test : - Test No. 14  Failed", comparator.compare("14", client.getRelpy(8007, "SOAP12TestServiceC", "14")));

        //Reply message has fault
        //assertTrue("SOAP 1.2 Test : - Test No. 16  Failed", comparator.compare("16", client.getRelpy(8007,"SOAP12TestServiceC", "16")));

        //Request sends to B and reply has a fault
        //assertTrue("SOAP 1.2 Test : - Test No. 17  Failed", comparator.compare("17_B", client.getRelpy(8007,"SOAP12TestServiceB", "17_A")));

        //There are intermediaries
        //assertTrue("SOAP 1.2 Test : - Test No. 18  Failed", comparator.compare("18", client.getRelpy("SOAP12TestServiceC", "18")));

        //Test No. 20 missing

        //Request sends to B and reply has a fault
        //assertTrue("SOAP 1.2 Test : - Test No. 21  Failed", comparator.compare("21_B", client.getRelpy(8007,"SOAP12TestServiceB", "21_A")));

        //mustUnderstand value is "wrong" and reply has a fault
        //assertTrue("SOAP 1.2 Test : - Test No. 23  Failed", comparator.compare("23", client.getRelpy("SOAP12TestServiceC", "23")));

        //soap envelope namespace uri incorrect  and reply has a fault
        //assertTrue("SOAP 1.2 Test : - Test No. 24  Failed", comparator.compare("24", client.getRelpy("SOAP12TestServiceC", "24")));

        //Has DTD  and reply has a fault
        //assertTrue("SOAP 1.2 Test : - Test No. 25  Failed", comparator.compare("25", client.getRelpy("SOAP12TestServiceC", "25")));

        //Has style sheet and reply has a fault
        //assertTrue("SOAP 1.2 Test : - Test No. 26  Failed", comparator.compare("26", client.getRelpy("SOAP12TestServiceC", "26")));

        //Test 30 is not here, because Axis 2 is supported both SOAP 1.1 and SOAP 1.2

        //In Test No. 32, headerblock value should be inserted to body
        //assertTrue("SOAP 1.2 Test : - Test No. 32  Failed", comparator.compare("32", client.getRelpy("SOAP12TestServiceC", "32")));

        //Test No. 33, body element has an error
        //assertTrue("SOAP 1.2 Test : - Test No. 33  Failed", comparator.compare("33", client.getRelpy("SOAP12TestServiceC", "33")));

        //Test No. 35 - 36 : - Reply message has fault
        //assertTrue("SOAP 1.2 Test : - Test No. 35  Failed", comparator.compare("35", client.getRelpy(8007,"SOAP12TestServiceC", "35")));
        //assertTrue("SOAP 1.2 Test : - Test No. 36  Failed", comparator.compare("36", client.getRelpy(8007,"SOAP12TestServiceC", "36")));

        //Test No. 38 has intermediaries

        //Test No. 39, mustUnderstand value is "9"
        //assertTrue("SOAP 1.2 Test : - Test No. 39  Failed", comparator.compare("39", client.getRelpy("SOAP12TestServiceC", "39")));

        //Test No. 62 has intermediaries

        //Test No. 63, headerblock name is validateCountryCode

        //Test No. 64 has <!NOTATION ...>

        //Test No. 65 has <!ELEMENT ..> ..<

        //Test No. 67 has <?xml version='1.0' standalone='yes'?>
        //assertTrue("SOAP 1.2 Test : - Test No. 67  Failed", comparator.compare("67", client.getRelpy("SOAP12TestServiceC", "67")));

        //Test No. 69 sends message without body

        //Test No. 70 sends message with an element after the body element

        //Test No 71 sends message with non namespace qualified attribute in envelope element

        //Test no. 75 sends a message with headerblock echoResolvedRef containing a relative reference
        //assertTrue("SOAP 1.2 Test : - Test No. 78  Failed", comparator.compare("75", client.getRelpy("SOAP12TestServiceC", "78")));

        //Test No. 79 has intermediaries
    }
}
