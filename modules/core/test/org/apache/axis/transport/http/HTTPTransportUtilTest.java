package org.apache.axis.transport.http;

import junit.framework.TestCase;
import org.apache.axis.om.OMAbstractFactory;
import org.apache.axis.om.OMElement;
import org.apache.axis.om.OMText;
import org.apache.axis.soap.SOAPEnvelope;
import org.apache.axis.soap.SOAPFactory;

import javax.xml.namespace.QName;

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
 *
 * author : Eran Chinthaka (chinthaka@apache.org)
 */

public class HTTPTransportUtilTest extends TestCase{
    private HTTPTransportUtils httpTransportUtils;
    private SOAPFactory factory;

    protected void setUp() throws Exception {
        super.setUp();
        httpTransportUtils = new HTTPTransportUtils();
        factory = OMAbstractFactory.getSOAP11Factory();
    }

    public void testOptimizedEnvelope(){
        SOAPEnvelope soapEnvelope = factory.getDefaultEnvelope();

        OMElement element = factory.createOMElement(new QName("MyFirstBodyElement"), soapEnvelope.getBody());
        OMElement element11 = factory.createOMElement(new QName("MyFirstBodyElement"), element);
        OMText optimizedText = factory.createText("Hi", "text/plain", true);
        element11.addChild(optimizedText);
        assertTrue("optmization check has not performed correctly in SOAPEnvelope", httpTransportUtils.checkEnvelopeForOptimise(soapEnvelope));
    }

    public void testNonOptimizedEnvelope(){
        SOAPEnvelope soapEnvelope = factory.getDefaultEnvelope();

        OMElement element = factory.createOMElement(new QName("MyFirstBodyElement"), soapEnvelope.getBody());
        OMElement element11 = factory.createOMElement(new QName("MyFirstBodyElement"), element);
        OMText optimizedText = factory.createText("Hi", "text/plain", false);
        element11.addChild(optimizedText);
        assertFalse("optmization check has not performed correctly in SOAPEnvelope", httpTransportUtils.checkEnvelopeForOptimise(soapEnvelope));
    }



}
