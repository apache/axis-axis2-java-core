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

package org.apache.axis2.handlers.addressing;

import junit.framework.TestCase;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPHeader;
import org.apache.axiom.soap.impl.builder.StAXSOAPModelBuilder;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.AddressingConstants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.handlers.util.TestUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.namespace.QName;
import java.util.Map;

public class AddressingInHandlerTest extends TestCase {
	private static final Log log = LogFactory.getLog(AddressingInHandlerTest.class);
    AddressingInHandler inHandler;
    TestUtil testUtil = new TestUtil();
    private static final String testFileName = "soapmessage.xml";
    private static final String wsaFinalTestFile = "soapWithWSAFinalInfo.xml";
    private static final String invalidCardinalityTestFile = "invalidCardinalityReplyToMessage.xml";

    private String action = "http://ws.apache.org/tests/action";
    private String messageID = "uuid:920C5190-0B8F-11D9-8CED-F22EDEEBF7E5";
    private String fromAddress = "http://schemas.xmlsoap.org/ws/2004/03/addressing/role/anonymous";
    private String faultAddress = "http://example.com/fabrikam/fault";

    /**
     * @param testName
     */
    public AddressingInHandlerTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        super.setUp();
        inHandler = new AddressingFinalInHandler();
    }


    public void testExtractAddressingInformationFromHeaders() {
        try {
            StAXSOAPModelBuilder omBuilder = testUtil.getOMBuilder(
                    testFileName);

            SOAPHeader header = ((SOAPEnvelope) omBuilder.getDocumentElement()).getHeader();
            Options options =
                    inHandler.extractAddressingInformation(header,
                            new MessageContext(),
                            header.getHeaderBlocksWithNSURI(
                                    AddressingConstants.Submission.WSA_NAMESPACE),
                            AddressingConstants.Submission.WSA_NAMESPACE);

            if (options == null) {
                fail(
                        "Addressing Information Headers have not been retrieved properly");
            }
            assertEquals("action header is not correct",
                    options.getAction(),
                    action);
            assertEquals("action header is not correct",
                    options.getMessageId().trim(),
                    messageID.trim());

            assertFromEPR(options.getFrom());

        } catch (Exception e) {
            e.printStackTrace();
            log.info(e.getMessage());
            fail(" An Exception has occured " + e.getMessage());
        }
    }
    
    public void testExtractAddressingInformationFromHeadersInvalidCardinality() {
        try {
            StAXSOAPModelBuilder omBuilder = testUtil.getOMBuilder(invalidCardinalityTestFile);

            SOAPHeader header = ((SOAPEnvelope) omBuilder.getDocumentElement()).getHeader();
            MessageContext mc = new MessageContext();
            try{
            	inHandler.extractAddressingInformation(header,
                    		mc,
                            header.getHeaderBlocksWithNSURI(
                                    AddressingConstants.Final.WSA_NAMESPACE),
                            AddressingConstants.Final.WSA_NAMESPACE);

            
            	fail("An AxisFault should have been thrown due to 2 wsa:ReplyTo headers.");
            }catch(AxisFault af){
            	assertNull("No ReplyTo should be set on the MessageContext", mc.getReplyTo());
            	assertFaultEPR(mc.getFaultTo());
            	assertEquals("WSAAction property is not correct",
            			mc.getWSAAction(),
                        action);
                assertEquals("MessageID property is not correct",
                		mc.getMessageID().trim(),
                        messageID.trim());
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.info(e.getMessage());
            fail(" An Exception has occured " + e.getMessage());
        }
    }

    private void assertFromEPR(EndpointReference fromEPR) {
        assertEquals("Address in From EPR is not valid",
                fromEPR.getAddress().trim(),
                fromAddress.trim());
    }
    
    private void assertFaultEPR(EndpointReference faultEPR) {
        assertEquals("Address in FaultTo EPR is not valid",
        		faultEPR.getAddress().trim(),
                faultAddress.trim());
    }

    public void testWSAFinalInformation() {
        try {
            StAXSOAPModelBuilder omBuilder = testUtil.getOMBuilder(
                    wsaFinalTestFile);
            inHandler.addressingNamespace = AddressingConstants.Final.WSA_NAMESPACE;
            SOAPHeader header = ((SOAPEnvelope) omBuilder.getDocumentElement()).getHeader();
            Options options =
                    inHandler.extractAddressingInformation(header,
                            new MessageContext(),
                            header.getHeaderBlocksWithNSURI(
                                    AddressingConstants.Final.WSA_NAMESPACE),
                            AddressingConstants.Final.WSA_NAMESPACE);
            assertNotNull(options);
            assertNotNull(options.getTo());

            Map allReferenceParameters = options.getTo().getAllReferenceParameters();
            assertNotNull(allReferenceParameters);
            QName qName = new QName("http://ws.apache.org/namespaces/axis2", "ParamOne", "axis2");
            assertNotNull(allReferenceParameters.get(qName));

        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
            fail(" An Exception has occured " + e.getMessage());
        }
    }


}
