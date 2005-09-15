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

import javax.xml.soap.SOAPElement;
import java.util.Iterator;

/**
 * @author Ashutosh Shahi ashutosh.shahi@gmail.com
 */
public class HeadersTest extends TestCase {

    private final String actor = "ACTOR#1";
    private final String localName = "Local1";
    private final String namespace = "http://ws.apache.org";
    private final String prefix = "P1";

    public HeadersTest(String name) {
        super(name);
    }

    public void testAddingHeaderElements() throws Exception {
        javax.xml.soap.SOAPMessage soapMessage = javax.xml.soap.MessageFactory.newInstance()
                .createMessage();
        javax.xml.soap.SOAPEnvelope soapEnv = soapMessage.getSOAPPart()
                .getEnvelope();
        javax.xml.soap.SOAPHeader header = soapEnv.getHeader();
        header.addChildElement("ebxmlms");
        
        /*ByteArrayOutputStream baos = new ByteArrayOutputStream();
        soapMessage.writeTo(baos);
        String xml = new String(baos.toByteArray());
        assertTrue(xml.indexOf("ebxmlms") != -1);*/
        
        Iterator it = header.getChildElements();
        boolean b = false;
        while (it.hasNext()) {
            SOAPElement el = (SOAPElement) it.next();
            String lName = el.getNodeName();
            if (lName.equalsIgnoreCase("ebxmlms")) {
                b = true;
                break;
            }
        }
        assertTrue(b);
    }

}
