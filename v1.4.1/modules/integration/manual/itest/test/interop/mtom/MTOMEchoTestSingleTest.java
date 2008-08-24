/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package test.interop.mtom;

import junit.framework.TestCase;
import org.apache.axis2.Constants;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.SOAP12Constants;
import test.interop.util.BodyElements;

/**
 * white mesa interop test
 */
public class MTOMEchoTestSingleTest extends TestCase {
    private EndpointReference targetEPR = new EndpointReference("http://www.whitemesa.net/mtom-test-cr-inter");

    public MTOMEchoTestSingleTest() {
        super(MTOMEchoTestSingleTest.class.getName());
    }

    public MTOMEchoTestSingleTest(String testName) {
        super(testName);
    }

    public void runTest(boolean optimized) throws Exception {
        Options options = new Options();
        options.setTo(targetEPR);
        options.setProperty(HTTPConstants.CHUNKED, Constants.VALUE_FALSE);
        options.setProperty(Constants.Configuration.ENABLE_MTOM,
                Constants.VALUE_TRUE);
        options.setTransportInProtocol(Constants.TRANSPORT_HTTP);
        options.setSoapVersionURI(SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI);
        ConfigurationContext configContext =
                ConfigurationContextFactory.createConfigurationContextFromFileSystem("target/test-resources/integrationRepo",null);
        ServiceClient sender = new ServiceClient(configContext, null);
        sender.setOptions(options);
        options.setTo(targetEPR);
        OMElement resultElem = sender.sendReceive(BodyElements.bodySingle(optimized));
        responseAssertion(resultElem, optimized);
    }

    public void testNonOptimized() throws Exception {
        runTest(false);
    }

    public void testOptimized() throws Exception {
        runTest(true);
    }

    private void responseAssertion(OMElement response, boolean optimized) {
        TestCase.assertNotNull(response);

        String responseText = response.getText();
        TestCase.assertEquals(BodyElements.bodySingle(optimized).getText(), responseText);
    }
}
