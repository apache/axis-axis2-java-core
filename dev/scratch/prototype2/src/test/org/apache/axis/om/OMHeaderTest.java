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
* Time: 2:39:57 PM
*/
package org.apache.axis.om;

import java.util.Iterator;

import org.apache.axis.impl.llom.OMNamespaceImpl;


public class OMHeaderTest extends OMTestCase {

    OMHeader soapHeader;

    public OMHeaderTest(String testName) {
        super(testName);
    }

    public static void main(String[] args) {
    }

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        soapHeader = soapEnvelope.getHeader();
    }

    public void testAddHeaderElement() {
        String newElementName = "MyHeaderElement";
        OMHeaderBlock soapHeaderElement = soapHeader.addHeaderBlock(newElementName, new OMNamespaceImpl("http://opensource.lk", "lsf"));
        assertTrue("Header Element added has different parent than it should have", soapHeaderElement.getParent() == soapHeader);
        assertTrue("Header Element added has different localname than it was given", soapHeaderElement.getLocalName().equalsIgnoreCase(newElementName));
    }

    public void testExamineHeaderElements() {
    }

    public void testExtractHeaderElements() {
        //TODO Implement extractHeaderBlocks().
    }

    public void testExamineMustUnderstandHeaderElements() {
        //TODO Implement examineMustUnderstandHeaderBlocks().
    }

    public void testExamineAllHeaderElements() {
        Iterator iterator = soapHeader.examineAllHeaderBlocks();
        int headerElementCount = 0;
        while (iterator.hasNext()) {
            iterator.next();
            headerElementCount++;
        }

        assertTrue("Number of header elements in the header differs from expected value of 3", headerElementCount == 3);
    }

    public void testExtractAllHeaderElements() {
        //TODO Implement extractAllHeaderBlocks().
    }

}
