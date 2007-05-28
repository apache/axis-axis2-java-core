/*
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

package org.apache.axis2.engine;

import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axis2.AxisFault;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.WSDL2Constants;
import org.apache.axis2.integration.LocalTestCase;
import org.apache.axis2.integration.TestingUtils;

public class OneWayRawXMLTest extends LocalTestCase {

	private boolean received;
    protected void setUp() throws Exception {
    	super.setUp();
    	serverConfig.addMessageReceiver(WSDL2Constants.MEP_URI_IN_ONLY, new MessageReceiver(){
			public void receive(MessageContext messageCtx) throws AxisFault {
			    SOAPEnvelope envelope = messageCtx.getEnvelope();
                TestingUtils.compareWithCreatedOMElement(envelope.getBody().getFirstElement());
            	received = true;
			}
    	});
    	deployClassAsService(Echo.SERVICE_NAME, Echo.class);
    }

    public void testOneWay() throws Exception {
        ServiceClient sender = getClient(Echo.SERVICE_NAME, "echoOMElementNoResponse");
        sender.fireAndForget(TestingUtils.createDummyOMElement());
        int index = 0;
        while (!received) {
            Thread.sleep(10);
            index++;
            if (index == 20) {
                throw new AxisFault("error Occured");
            }
        }
    }

}
