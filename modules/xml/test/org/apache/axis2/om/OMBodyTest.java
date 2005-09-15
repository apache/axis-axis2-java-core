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
package org.apache.axis2.om;

import org.apache.axis2.soap.SOAPBody;
import org.apache.axis2.soap.impl.llom.SOAPProcessingException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class OMBodyTest extends OMTestCase implements OMConstants {
    SOAPBody soapBody;
    private Log log = LogFactory.getLog(getClass());

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
        log.info("Adding SOAP fault to body ....");
        try {
            soapBody.addChild(
                    soapFactory.createSOAPFault(soapBody,
                            new Exception("Testing soap fault")));
        } catch (SOAPProcessingException e) {
            log.info(e.getMessage());
            fail(e.getMessage());
        }
        log.info("\t checking for SOAP Fault ...");
        assertTrue("SOAP body has no SOAP fault", soapBody.hasFault());
        log.info("\t checking for not-nullity ...");
        assertTrue("SOAP body has no SOAP fault", soapBody.getFault() != null);

        //SimpleOMSerializer simpleOMSerializer = new SimpleOMSerializer();
        //simpleOMSerializer.serializeWithCache(soapBody, System.out);
    }

}
