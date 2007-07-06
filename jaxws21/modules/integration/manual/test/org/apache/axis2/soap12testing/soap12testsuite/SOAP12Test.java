/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.axis2.soap12testing.soap12testsuite;

import junit.framework.TestCase;
import test.soap12testing.client.MessageComparator;
import test.soap12testing.client.SOAP12TestClient;
import test.soap12testing.server.SimpleServer;

public class SOAP12Test extends TestCase {
    private SimpleServer server;
    private MessageComparator comparator;
    private SOAP12TestClient client;

    public SOAP12Test() {
        server = new SimpleServer();
        server.start();
        comparator = new MessageComparator();
        client = new SOAP12TestClient();
    }

    protected void setUp() {

    }

    public void testWithoutFaults() {

        assertTrue("SOAP 1.2 Test : - Test No. 1  Failed",
                   comparator.compare("1", client.getRelpy(8008, "SOAP12TestServiceC", "1")));
        assertTrue("SOAP 1.2 Test : - Test No. 2  Failed",
                   comparator.compare("2", client.getRelpy(8008, "SOAP12TestServiceC", "2")));
        assertTrue("SOAP 1.2 Test : - Test No. 3  Failed",
                   comparator.compare("3", client.getRelpy(8008, "SOAP12TestServiceC", "3")));
        assertTrue("SOAP 1.2 Test : - Test No. 4  Failed",
                   comparator.compare("4", client.getRelpy(8008, "SOAP12TestServiceC", "4")));
        assertTrue("SOAP 1.2 Test : - Test No. 5  Failed",
                   comparator.compare("5", client.getRelpy(8008, "SOAP12TestServiceC", "5")));

        assertTrue("SOAP 1.2 Test : - Test No. 10  Failed",
                   comparator.compare("10", client.getRelpy(8008, "SOAP12TestServiceC", "10")));
        assertTrue("SOAP 1.2 Test : - Test No. 11  Failed",
                   comparator.compare("11", client.getRelpy(8008, "SOAP12TestServiceC", "11")));

        assertTrue("SOAP 1.2 Test : - Test No. 15  Failed",
                   comparator.compare("15", client.getRelpy(8008, "SOAP12TestServiceC", "15")));

        assertTrue("SOAP 1.2 Test : - Test No. 19  Failed",
                   comparator.compare("19", client.getRelpy(8008, "SOAP12TestServiceC", "19")));

        assertTrue("SOAP 1.2 Test : - Test No. 22  Failed",
                   comparator.compare("22", client.getRelpy(8008, "SOAP12TestServiceC", "22")));

        assertTrue("SOAP 1.2 Test : - Test No. 29  Failed",
                   comparator.compare("29", client.getRelpy(8008, "SOAP12TestServiceC", "29")));

        assertTrue("SOAP 1.2 Test : - Test No. 31  Failed",
                   comparator.compare("31", client.getRelpy(8008, "SOAP12TestServiceC", "31")));

        assertTrue("SOAP 1.2 Test : - Test No. 34  Failed",
                   comparator.compare("34", client.getRelpy(8008, "SOAP12TestServiceC", "34")));

        assertTrue("SOAP 1.2 Test : - Test No. 37  Failed",
                   comparator.compare("37", client.getRelpy(8008, "SOAP12TestServiceC", "37")));

        assertTrue("SOAP 1.2 Test : - Test No. 40  Failed",
                   comparator.compare("40", client.getRelpy(8008, "SOAP12TestServiceC", "40")));

        assertTrue("SOAP 1.2 Test : - Test No. 66  Failed",
                   comparator.compare("66", client.getRelpy(8008, "SOAP12TestServiceC", "66")));

        assertTrue("SOAP 1.2 Test : - Test No. 68  Failed",
                   comparator.compare("68", client.getRelpy(8008, "SOAP12TestServiceC", "68")));

        assertTrue("SOAP 1.2 Test : - Test No. 74  Failed",
                   comparator.compare("74", client.getRelpy(8008, "SOAP12TestServiceC", "74")));

        assertTrue("SOAP 1.2 Test : - Test No. 78  Failed",
                   comparator.compare("78", client.getRelpy(8008, "SOAP12TestServiceC", "78")));
    }
}
