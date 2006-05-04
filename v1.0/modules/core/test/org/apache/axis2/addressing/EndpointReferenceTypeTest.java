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

package org.apache.axis2.addressing;

import junit.framework.TestCase;
import junit.textui.TestRunner;
import org.apache.axiom.om.OMElement;

import javax.xml.namespace.QName;
import java.util.Map;


public class EndpointReferenceTypeTest extends TestCase {

    EndpointReference endpointReference;
    private String address = "htttp://wwww.openource.lk/~chinthaka";

    public static void main(String[] args) {
        TestRunner.run(EndpointReferenceTypeTest.class);
    }

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        endpointReference = new EndpointReference(address);
    }

    public void testGetAndSetAddress() {
        assertEquals("Address not set properly in the constructor",
                address,
                endpointReference.getAddress());

        String newAddress = "http://www.axis2.com";
        endpointReference.setAddress(newAddress);
        assertEquals("Address not set properly in the setter method",
                newAddress,
                endpointReference.getAddress());
    }

    public void testGetAndSetReferenceParameters() {
        for (int i = 0; i < 10; i++) {
            endpointReference.addReferenceParameter(
                    new QName("http://www.opensouce.lk/" + i, "" + i),
                    "value " + i * 50);
        }

        Map retrievedReferenceParameters = endpointReference.getAllReferenceParameters();
        for (int i = 0; i < 10; i++) {
            OMElement referenceParameter = (OMElement) retrievedReferenceParameters.get(
                    new QName("http://www.opensouce.lk/" + i, "" + i));
            assertEquals(
                    "Input value differs from what is taken out from AnyContentType",
                    referenceParameter.getText(),
                    "value " + i * 50);
        }
    }
}
