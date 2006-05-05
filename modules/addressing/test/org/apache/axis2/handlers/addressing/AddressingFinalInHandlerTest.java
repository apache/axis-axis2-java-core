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

import java.util.ArrayList;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.addressing.AddressingConstants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class AddressingFinalInHandlerTest extends AddressingInHandlerTestBase {

    private Log log = LogFactory.getLog(getClass());
   
    /**
     * @param testName
     */
    public AddressingFinalInHandlerTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        super.setUp();
        inHandler = new AddressingFinalInHandler();
        addressingNamespace = AddressingConstants.Final.WSA_NAMESPACE;
        versionDirectory = "final";
        fromAddress = "http://www.w3.org/2005/08/addressing/anonymous";
        secondRelationshipType = "http://some.custom.relationship";
    }

    public void testExtractAddressingInformationFromHeaders() {
        try {
            Options options = extractAddressingInformationFromHeaders();
            
            assertNotNull(options);
            assertNotNull(options.getTo());
   
            Map allReferenceParameters = options.getTo().getAllReferenceParameters();
            assertNotNull(allReferenceParameters);
            QName qName = new QName("http://ws.apache.org/namespaces/axis2", "ParamOne", "axis2");
            assertNotNull(allReferenceParameters.get(qName));

            assertEPRHasCorrectMetadata(options.getFrom());
            assertEPRHasCorrectMetadata(options.getFaultTo());
            assertEPRHasCorrectMetadata(options.getReplyTo());
            
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
            fail(" An Exception has occured " + e.getMessage());
        }
    }
    
    private void assertEPRHasCorrectMetadata(EndpointReference epr){
    	ArrayList metadata = epr.getMetaData();
    	if(metadata != null){
    		OMElement md = (OMElement)metadata.get(0);
    		assertEquals(md.getQName(),new QName("http://ws.apache.org/namespaces/axis2","MetaExt"));
    		assertEquals(md.getText(),"123456789");
    		assertEquals(md.getAttributeValue(new QName("http://ws.apache.org/namespaces/axis2","AttrExt")),"123456789");
    	}else{
    		fail("No Metadata found in EPR");
    	}
    }
}
