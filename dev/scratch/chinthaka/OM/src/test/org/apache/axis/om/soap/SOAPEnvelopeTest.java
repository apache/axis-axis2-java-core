/**
* Copyright 2001-2004 The Apache Software Foundation.
* <p/>
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
* <p/>
* http://www.apache.org/licenses/LICENSE-2.0
* <p/>
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
* <p/>
* Author: Eran Chinthaka - Lanka Software Foundation
* Date: Nov 2, 2004
* Time: 2:39:39 PM
*/
package org.apache.axis.om.soap;

import org.apache.axis.om.impl.OMXMLPullParserWrapper;
import org.apache.axis.om.soap.SOAPBody;
import org.apache.axis.om.soap.SOAPHeader;
import org.apache.axis.om.OMTestCase;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.FileReader;
import java.util.Iterator;


public class SOAPEnvelopeTest extends OMTestCase {


    public static void main(String[] args) {
    }

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
    }


    public void testGetHeader() {
        SOAPHeader header = soapEnvelope.getHeader();
        assertTrue("Header information retrieved not correct", ( header != null && header.getLocalName().equalsIgnoreCase("Header")) );
    }

    public void testGetBody() {
        SOAPBody body = soapEnvelope.getBody();
        assertTrue("Header information retrieved not correct", ( body != null && body.getLocalName().equalsIgnoreCase("Body")) );

    }



}
