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
package org.apache.axis.om;

import org.apache.axis.impl.llom.OMNamespaceImpl;

import java.util.Iterator;

public class OMBodyTest extends OMTestCase {

    SOAPBody soapBody;

    public OMBodyTest(String testName) {
        super(testName);
    }

    public static void main(String[] args) {
    }

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        soapBody = soapEnvelope.getBody();
    }

    /*
     * Class under test for OMFault addFault()
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
         String newElementName = "MyBodyElement";
        soapBody.addChild(ombuilderFactory.createOMElement(newElementName, new OMNamespaceImpl("http://opensource.lk", "lsf")));

        Iterator children = soapBody.getChildren();
        // TODO test this
    }

    /*
     * Class under test for void addFault(OMFault)
     */
    public void testAddFaultSOAPFault() {
        //TODO Implement addFault().
    }

    public void testAddDocument() {
        //TODO Implement addDocument().
    }

}
