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

package org.apache.axis2.saaj;

import junit.framework.Assert;

import javax.xml.namespace.QName;
import jakarta.xml.soap.Detail;
import jakarta.xml.soap.DetailEntry;
import jakarta.xml.soap.MessageFactory;
import jakarta.xml.soap.SOAPBody;
import jakarta.xml.soap.SOAPConstants;
import jakarta.xml.soap.SOAPEnvelope;
import jakarta.xml.soap.SOAPFault;
import jakarta.xml.soap.SOAPHeader;
import jakarta.xml.soap.SOAPHeaderElement;
import jakarta.xml.soap.SOAPMessage;
import jakarta.xml.soap.SOAPPart;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * 
 */
@RunWith(SAAJTestRunner.class)
public class SAAJDetailTest extends Assert {
    private SOAPMessage msg = null;
    private SOAPPart sp = null;
    private SOAPBody body = null;
    private SOAPEnvelope envelope = null;
    private SOAPHeader header = null;
    private SOAPHeaderElement headerEle = null;

    @Before
    public void setUp() throws Exception {
        msg = MessageFactory.newInstance().createMessage();
        sp = msg.getSOAPPart();
        envelope = sp.getEnvelope();
        body = envelope.getBody();
        header = envelope.getHeader();
        headerEle = header.addHeaderElement(envelope.createName("foo", "f", "foo-URI"));
        headerEle.setActor("actor-URI");
    }


    /*
    * for soap version 1.1
    */
    @Validated @Test
    public void testAddDetailEntry() throws Exception {
        //Add a SOAPFault object to the SOAPBody
        SOAPFault sf = body.addFault();
        //Add a Detail object to the SOAPFault object
        Detail d = sf.addDetail();
        QName name = new QName("http://www.wombat.org/trader",
                               "GetLastTradePrice", "WOMBAT");
        //Add a DetailEntry object to the Detail object
        DetailEntry de = d.addDetailEntry(name);
        assertNotNull(de);
        assertTrue(de instanceof DetailEntry);
    }

    /*
     * for soap version 1.2
     */
    @Validated @Test
    public void testAddDetailEntry2() throws Exception {
        msg = MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL).createMessage();
        sp = msg.getSOAPPart();
        envelope = sp.getEnvelope();
        body = envelope.getBody();

        //Add a SOAPFault object to the SOAPBody
        SOAPFault sf = body.addFault();
        //Add a Detail object to the SOAPFault object
        Detail d = sf.addDetail();
        QName name = new QName("http://www.wombat.org/trader",
                               "GetLastTradePrice", "WOMBAT");
        //Add a DetailEntry object to the Detail object
        DetailEntry de = d.addDetailEntry(name);
        //Successfully created DetailEntry object
        assertNotNull(de);
        assertTrue(de instanceof DetailEntry);
    }
}
