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

import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import java.io.ByteArrayOutputStream;

public class PrefixesTest extends TestCase {

    public PrefixesTest(String name) {
        super(name);
    }

    public void testAddingPrefixesForChildElements() throws Exception {
        MessageFactory factory = MessageFactory.newInstance();
        SOAPMessage msg = factory.createMessage();
        SOAPPart sp = msg.getSOAPPart();
        SOAPEnvelope se = sp.getEnvelope();
        SOAPBody sb = se.getBody();
        SOAPElement el1 = sb.addBodyElement(
                se.createName
                ("element1", "prefix1", "http://www.sun.com"));
        SOAPElement el2 = el1.addChildElement(
                se.createName
                ("element2", "prefix2", "http://www.apache.org"));

        org.apache.axis2.soap.SOAPEnvelope omEnv = ((SOAPEnvelopeImpl) se).getOMEnvelope();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        msg.writeTo(baos);

        String xml = new String(baos.toByteArray());
        assertTrue(xml.indexOf("prefix1") != -1);
        assertTrue(xml.indexOf("prefix2") != -1);
        assertTrue(xml.indexOf("http://www.sun.com") != -1);
        assertTrue(xml.indexOf("http://www.apache.org") != -1);
    }

    /* public void testAttribute() throws Exception {
         String soappacket = "<SOAP-ENV:Envelope xmlns:SOAP-ENV =\"http://schemas.xmlsoap.org/soap/envelope/\"" +
                             "xmlns:xsi =\"http://www.w3.org/1999/XMLSchema-instance\"" +
                             "xmlns:xsd =\"http://www.w3.org/1999/XMLSchema\">" +
                             "<SOAP-ENV:Body>" +
                             "<helloworld name=\"tester\" />" +
                             "</SOAP-ENV:Body>" +
                             "</SOAP-ENV:Envelope>";
         SOAPMessage msg = MessageFactory.newInstance().createMessage(new MimeHeaders(), new ByteArrayInputStream(soappacket.getBytes()));
         SOAPBody body = msg.getSOAPPart().getEnvelope().getBody();
         msg.writeTo(System.out);

         SOAPElement ele = (SOAPElement) body.getChildElements().next();
         java.util.Iterator attit = ele.getAllAttributes();

         System.out.println(attit.next().getClass());

         javax.xml.soap.Name n = (javax.xml.soap.Name) attit.next();
         //assertEquals("Test fail prefix problem",n.getQualifiedName(),"name");
     }*/

}
