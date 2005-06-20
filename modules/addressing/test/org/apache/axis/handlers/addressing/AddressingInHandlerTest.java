package org.apache.axis.handlers.addressing;

import junit.framework.TestCase;

import org.apache.axis.addressing.EndpointReference;
import org.apache.axis.addressing.MessageInformationHeadersCollection;
import org.apache.axis.addressing.AddressingConstants;
import org.apache.axis.soap.SOAPEnvelope;
import org.apache.axis.soap.SOAPHeader;
import org.apache.axis.soap.impl.llom.builder.StAXSOAPModelBuilder;
import org.apache.axis.handlers.util.TestUtil;

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
public class AddressingInHandlerTest extends TestCase {
    /**
     * Eran Chinthaka (chinthaka@apache.org)
     */

    AddressingInHandler inHandler;
    TestUtil testUtil = new TestUtil();
    private static final String testFileName = "soapmessage.xml";

    private String action = "http://ws.apache.org/tests/action";
    private String messageID = "uuid:920C5190-0B8F-11D9-8CED-F22EDEEBF7E5";
    private String fromAddress = "http://schemas.xmlsoap.org/ws/2004/03/addressing/role/anonymous";

    /**
     * @param testName
     */
    public AddressingInHandlerTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        super.setUp();
        inHandler = new AddressingInHandler();
    }


    public void testExtractAddressingInformationFromHeaders() {
        try {
            StAXSOAPModelBuilder omBuilder = testUtil.getOMBuilder(testFileName);
            
            SOAPHeader header = ((SOAPEnvelope) omBuilder.getDocumentElement()).getHeader();
            MessageInformationHeadersCollection messageInformationHeadersCollection =
                    inHandler.extractCommonAddressingParameters(header, null,header.getHeaderBolcksWithNSURI(AddressingConstants.Submission.WSA_NAMESPACE), AddressingConstants.Submission.WSA_NAMESPACE);

            if(messageInformationHeadersCollection == null){
                fail("Addressing Information Headers have not been retrieved properly");
            }
            assertEquals("action header is not correct", messageInformationHeadersCollection.getAction(), action);
            assertEquals("action header is not correct", messageInformationHeadersCollection.getMessageId(), messageID);

            assertFromEPR(messageInformationHeadersCollection.getFrom());

        } catch (Exception e) {
            e.printStackTrace();
            fail(" An Exception has occured " + e.getMessage());
        }
    }

    private void assertFromEPR(EndpointReference fromEPR){
        assertEquals("Address in EPR is not valid", fromEPR.getAddress(), fromAddress);
    }




}
