package org.apache.axis2.transport.http;

import junit.framework.TestCase;
import org.apache.axis2.context.MessageContext;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.SOAPEnvelope;

import java.util.regex.Pattern;
import java.util.regex.Matcher;
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

public class RestSenderTest extends TestCase {

    String testURL = "http://locahost:8080/paramOne/{FirstName}";
    String queryPart = "?test=1&lastName={LastName}";


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

    public void testAppendParametersToURL() {
        RESTSender restSender = new RESTSender();
        String modifiedURL = restSender.appendParametersToURL(messageContext, testURL, queryPart);

        System.out.println("original = " + testURL + queryPart);
        System.out.println("modifiedURL = " + modifiedURL);

        String expectedURL = "http://locahost:8080/paramOne/Foo?test=1&lastName=Bar";

        assertEquals(modifiedURL, expectedURL);

    }
}
