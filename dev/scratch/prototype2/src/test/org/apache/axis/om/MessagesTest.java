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
* Time: 2:39:39 PM
*/
package org.apache.axis.om;



public class MessagesTest extends OMTestCase {
    OMEnvelope soapEnvelope;

    public MessagesTest(String testName) {
        super(testName);
    }

    public void testMessageWithLotOfWhiteSpaces() throws OMException, Exception {
        soapEnvelope = OMTestUtils.getOMBuilder("src/test-resources/soap/whitespacedMessage.xml").getOMEnvelope();
        OMTestUtils.walkThrough(soapEnvelope);
    }
    
    public void testMinimalMessage() throws OMException, Exception {
        soapEnvelope = OMTestUtils.getOMBuilder("src/test-resources/soap/minimalMessage.xml").getOMEnvelope();
        OMTestUtils.walkThrough(soapEnvelope);
    }
    
    public void testReallyBigMessage() throws OMException, Exception {
        soapEnvelope = OMTestUtils.getOMBuilder("src/test-resources/soap/reallyReallyBigMessage.xml").getOMEnvelope();
        OMTestUtils.walkThrough(soapEnvelope);
    }
    
    public void testEmptyBodiedMessage() throws OMException, Exception {
        soapEnvelope = OMTestUtils.getOMBuilder("src/test-resources/soap/emtyBodymessage.xml").getOMEnvelope();
        OMTestUtils.walkThrough(soapEnvelope);
    }

    

}
