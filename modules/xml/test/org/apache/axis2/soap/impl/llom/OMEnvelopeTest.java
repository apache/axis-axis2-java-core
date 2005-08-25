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
package org.apache.axis2.soap.impl.llom;

import org.apache.axis2.om.OMAbstractFactory;
import org.apache.axis2.om.OMTestCase;
import org.apache.axis2.om.OMTestUtils;
import org.apache.axis2.soap.SOAPBody;
import org.apache.axis2.soap.SOAPEnvelope;
import org.apache.axis2.soap.SOAPHeader;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class OMEnvelopeTest extends OMTestCase {
    private Log log = LogFactory.getLog(getClass());
    public OMEnvelopeTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        super.setUp();
    }

    public void testGetHeader1() {
        SOAPHeader header = soapEnvelope.getHeader();
        assertTrue("Header information retrieved not correct",
                (header != null &&
                header.getLocalName().equalsIgnoreCase("Header")));
    }

    public void testGetBody1() {
        SOAPBody body = soapEnvelope.getBody();
        assertTrue("Header information retrieved not correct",
                (body != null && body.getLocalName().equalsIgnoreCase("Body")));
    }

    private SOAPEnvelope getSecondEnvelope() throws Exception {
        return (SOAPEnvelope) OMTestUtils.getOMBuilder(
                getTestResourceFile("soap/sample1.xml"))
                .getDocumentElement();
    }

    public void testGetHeader2() throws Exception {
        SOAPHeader header = getSecondEnvelope().getHeader();
        assertTrue("Header information retrieved not correct",
                (header != null &&
                header.getLocalName().equalsIgnoreCase("Header")));
    }

    public void testGetBody2() throws Exception {
        SOAPBody body = getSecondEnvelope().getBody();
        assertTrue("Header information retrieved not correct",
                (body != null && body.getLocalName().equalsIgnoreCase("Body")));
    }

    public void testDefaultEnveleope() {
        SOAPEnvelope env = null;
        try {
            env = OMAbstractFactory.getSOAP11Factory().getDefaultEnvelope();
        } catch (SOAPProcessingException e) {
            log.info(e.getMessage());
            fail(e.getMessage());
        }
        assertNotNull(env);
        assertNotNull("Body should not be null", env.getBody());
    }
}
