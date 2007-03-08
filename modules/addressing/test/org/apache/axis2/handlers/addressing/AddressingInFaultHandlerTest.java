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

package org.apache.axis2.handlers.addressing;

import junit.framework.TestCase;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.impl.builder.StAXSOAPModelBuilder;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.handlers.util.TestUtil;

public class AddressingInFaultHandlerTest extends TestCase {

    TestUtil testUtil = new TestUtil();

    /** @param testName  */
    public AddressingInFaultHandlerTest(String testName) {
        super(testName);
    }

    public void testInvalidAddressingHeaderWsaToSOAP11() throws Exception {
        AxisFault af = getFaultForTest("InvalidAddressingHeader", true);
        assertNotNull(af);
        assertEquals("The server failed to process the WS-Addressing header: " + "wsa:To" +
                " [Reason]: A header representing a Message Addressing Property is not valid and the message cannot be processed",
                     af.getMessage());
    }

    public void testMissingActionSOAP11() throws Exception {
        AxisFault af = getFaultForTest("MessageAddressingHeaderRequired", true);
        assertNotNull(af);
    }

    public void testInvalidAddressingHeaderWsaToSOAP12() throws Exception {
        AxisFault af = getFaultForTest("InvalidAddressingHeader", false);
        assertNotNull(af);
        assertEquals("The server failed to process the WS-Addressing header: " + "wsa:To" +
                " [Reason]: A header representing a Message Addressing Property is not valid and the message cannot be processed",
                     af.getMessage());
    }

    public void testMissingActionSOAP12() throws Exception {
        AxisFault af = getFaultForTest("MessageAddressingHeaderRequired", false);
        assertNotNull(af);
    }

    private AxisFault getFaultForTest(String testName, boolean isSOAP11) throws Exception {
        String testfile =
                "fault-messages/" + (isSOAP11 ? "soap11" : "soap12") + "/" + testName + ".xml";
        StAXSOAPModelBuilder omBuilder = testUtil.getOMBuilder(testfile);
        SOAPEnvelope envelope = ((SOAPEnvelope) omBuilder.getDocumentElement());
        MessageContext msgContext = new MessageContext();
        msgContext.setEnvelope(envelope);
        AddressingFinalInHandler afih = new AddressingFinalInHandler();
        afih.invoke(msgContext);
        AddressingInFaultHandler aifh = new AddressingInFaultHandler();
        aifh.invoke(msgContext);

        return (AxisFault) msgContext.getProperty(Constants.INBOUND_FAULT_OVERRIDE);
    }

}
