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

package org.apache.axis.engine;

import org.apache.axis.AbstractTestCase;
import org.apache.axis.context.ConfigurationContext;
import org.apache.axis.context.MessageContext;
import org.apache.axis.description.GlobalDescription;
import org.apache.axis.description.ServiceDescription;
import org.apache.axis.om.OMAbstractFactory;
import org.apache.axis.soap.SOAPFactory;

public class MessageContextTest extends AbstractTestCase {
    public MessageContextTest(String testName) {
        super(testName);
    }

    public void testMesssageContext() throws AxisFault {
        AxisConfiguration er = new AxisSystemImpl(new GlobalDescription());
        ServiceDescription servicesDesc = new ServiceDescription();
        er.addService(servicesDesc);

        ConfigurationContext engineContext = new ConfigurationContext(er);

        MessageContext msgctx = new MessageContext(null, null, null, engineContext);

        SOAPFactory omFac = OMAbstractFactory.getSOAP11Factory();

        msgctx.setEnvelope(omFac.getDefaultEnvelope());
        assertNotNull(msgctx.getEnvelope());

        msgctx.setFaultTo(null);
        assertNull(msgctx.getFaultTo());

        msgctx.setFrom(null);
        assertNull(msgctx.getFrom());

        msgctx.setRelatesTo(null);
        assertNull(msgctx.getRelatesTo());

        msgctx.setReplyTo(null);
        assertNull(msgctx.getReplyTo());

        msgctx.setMessageID(null);
        assertNull(msgctx.getMessageID());
    }
}
