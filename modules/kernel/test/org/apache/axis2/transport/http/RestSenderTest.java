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


package org.apache.axis2.transport.http;

import junit.framework.TestCase;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.AxisFault;


public class RestSenderTest extends TestCase {

    String testURL = "http://locahost:8080/paramOne/{FirstName}?test=1&lastName={LastName}";
    private MessageContext messageContext;


    protected void setUp() throws Exception {
        messageContext = new MessageContext();

        SOAPFactory soapFactory = OMAbstractFactory.getSOAP12Factory();
        SOAPEnvelope defaultEnvelope = soapFactory.getDefaultEnvelope();

        messageContext.setEnvelope(defaultEnvelope);

        OMElement bodyFirstElement = soapFactory.createOMElement("TestOperation", null);
        defaultEnvelope.getBody().addChild(bodyFirstElement);

        soapFactory.createOMElement("FirstName", null, bodyFirstElement).setText("Foo");
        soapFactory.createOMElement("LastName", null, bodyFirstElement).setText("Bar");

    }

    public void testAppendParametersToURL() throws AxisFault {
        RESTSender restSender = new RESTSender();
        String modifiedURL = null;

        int separator = testURL.indexOf('{');

        if (separator > 0) {
            String path = testURL.substring(0, separator - 1);
            String query = testURL.substring(separator - 1);

            modifiedURL = path + restSender.applyURITemplating(messageContext, query, true);

        }

        System.out.println("original = " + testURL);
        System.out.println("modifiedURL = " + modifiedURL);

        String expectedURL = "http://locahost:8080/paramOne/Foo?test=1&lastName=Bar";
        assertEquals(modifiedURL, expectedURL);

    }
}
