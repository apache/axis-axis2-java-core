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
* Time: 2:36:54 PM
*/
package org.apache.axis.om.soap;

import org.apache.axis.AbstractTestCase;
import org.apache.axis.om.OMTestUtils;
import org.apache.axis.om.impl.OMNamespaceImpl;


public class SOAPBodyTest extends AbstractTestCase{

    SOAPBody soapBody;


    public SOAPBodyTest(String testName) {
        super(testName);
    }

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        SOAPEnvelope soapEnvelope = OMTestUtils.getOMBuilder(getTestResourceFile("soap/soapmessage1.xml")).
                                        getSOAPMessage().getEnvelope();
        soapBody = soapEnvelope.getBody();
    }

    /*
     * Class under test for SOAPFault addFault()
     */
    public void testAddFault() {
        //TODO Implement addFault().
    }

    public void testHasFault() {
        //TODO Implement hasFault().
    }

    public void testGetFault() {
        //TODO Implement getFault().
    }

    public void testAddBodyElement() {

        assertNotNull(soapBody);

        String newElementName = "MyBodyElement";
        SOAPBodyElement soapBodyElement = soapBody.addBodyElement(newElementName, new OMNamespaceImpl("http://opensource.lk", "lsf"));

        assertTrue("Body Element added has different parent than it should have", soapBodyElement.getParent() == soapBody);
        assertTrue("Body Element added has different localname than it was given", soapBodyElement.getLocalName().equalsIgnoreCase(newElementName));
    }

    /*
     * Class under test for void addFault(SOAPFault)
     */
    public void testAddFaultSOAPFault() {
        //TODO Implement addFault().
    }

    public void testAddDocument() {
        //TODO Implement addDocument().
    }

}
