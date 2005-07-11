package org.apache.axis2.om;

import org.apache.axis2.soap.SOAPEnvelope;
import org.apache.axis2.soap.impl.llom.builder.StAXSOAPModelBuilder;

import javax.xml.namespace.QName;

/**
 * Copyright 2001-2004 The Apache Software Foundation.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 * <p/>
 */
public class OMElementTest extends OMTestCase implements OMConstants {
    private static final String WSA_URI = "http://schemas.xmlsoap.org/ws/2004/03/addressing";
    private static final String WSA_TO = "To";


    public OMElementTest(String testName) {
        super(testName);
    }

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {

    }

    public void testGetText() {
        try {
            StAXSOAPModelBuilder soapBuilder = getOMBuilder(
                    "soap/OMElementTest.xml");
            SOAPEnvelope soapEnvelope = (SOAPEnvelope) soapBuilder.getDocumentElement();
            OMElement wsaTo = soapEnvelope.getHeader().getFirstChildWithName(
                    new QName(WSA_URI, WSA_TO));

            String expectedString = "http://localhost:8081/axis/services/BankPort";
            assertEquals("getText is not returning the correct value",
                    wsaTo.getText().trim(),
                    expectedString);
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

}
