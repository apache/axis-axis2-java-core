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

import java.util.Iterator;

import org.apache.axis.impl.llom.OMNamespaceImpl;
import org.apache.axis.impl.llom.serialize.SimpleOMSerializer;

public class OMBodyTest extends OMTestCase implements OMConstants{

    SOAPBody soapBody;

    public OMBodyTest(String testName) {
        super(testName);
    }
    
    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        soapBody = soapEnvelope.getBody();
    }

    /*
     * Class under test for SOAPFault addFault()
     */
    public void testAddFault() {
        System.out.println("Adding SOAP fault to body ....");

        soapBody.addChild(ombuilderFactory.createSOAPFault(soapBody,new Exception("Testing soap fault")));

        System.out.println("\t checking for SOAP Fault ...");
        assertTrue("SOAP body has no SOAP fault", soapBody.hasFault());

        System.out.println("\t checking for not-nullity ...");
        assertTrue("SOAP body has no SOAP fault", soapBody.getFault() != null);

        //SimpleOMSerializer simpleOMSerializer = new SimpleOMSerializer();
        //simpleOMSerializer.serialize(soapBody, System.out);
    }
              
}
