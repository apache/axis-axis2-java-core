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

package test.interop.mtom;

import junit.framework.TestCase;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Call;
import org.apache.axis2.client.Options;
import org.apache.axis2.om.OMElement;
import org.apache.axis2.soap.SOAP12Constants;
import test.interop.util.BodyElements;

import java.util.Iterator;

/**
 * white mesa cr interop test
 */
public class MTOMEchoTestMultipleTest extends TestCase {
    private EndpointReference targetEPR = new EndpointReference("http://www.whitemesa.net/mtom-test-cr-inter");
    private int repeat = 8;

    public MTOMEchoTestMultipleTest() {
        super(MTOMEchoTestMultipleTest.class.getName());
    }

    public MTOMEchoTestMultipleTest(String testName) {
        super(testName);
    }

    public void runTest(boolean optimized, int repeat) throws Exception {
        Call call = new Call("target/test-resources/integrationRepo");
        Options options = new Options();
        call.setClientOptions(options);
        options.setTo(targetEPR);
        options.setProperty(Constants.Configuration.ENABLE_MTOM,
                Constants.VALUE_TRUE);
        options.setListenerTransportProtocol(Constants.TRANSPORT_HTTP);
        options.setUseSeparateListener(false);
        options.setSoapVersionURI(SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI);

        OMElement resultElem = call.invokeBlocking("EchoTestMultiple", BodyElements.bodyMultiple(optimized, repeat));
        responseAssertion(resultElem);
    }

    public void testNonOptimized() throws Exception {
        runTest(false, repeat);
    }

    public void testOptimized() throws Exception {
        runTest(true, repeat);
    }

    private void responseAssertion(OMElement response) {
        int child = 0;
        TestCase.assertNotNull(response);

        Iterator iterator = response.getChildren();
        while (iterator.hasNext()) {
            OMElement ele = (OMElement) iterator.next();
            if (ele != null) {
                child++;
            }
        }
        TestCase.assertEquals(repeat, child);


    }
}
