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

package org.apache.axis2.dispatchers;

import junit.framework.TestCase;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.soap.SOAPBody;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.engine.AxisConfiguration;

public class SOAPMessageBodyBasedServiceDispatcherTest extends TestCase {

    /**
     * Body-namespace dispatch is off unless asked for: the Dispatch phase runs after
     * the Security phase, so a service chosen from message content is chosen after
     * the handlers that would have authenticated the request for it have run against
     * no service. See {@link org.apache.axis2.dispatchers.ContentBasedDispatchPolicy}.
     */
    public void testFindServiceDeniedByDefault() throws AxisFault {
        MessageContext messageContext = messageContextNaming("Service2");

        new SOAPMessageBodyBasedServiceDispatcher().invoke(messageContext);

        assertNull("a service must not be selected from the body by default",
                messageContext.getAxisService());
    }

    public void testFindService() throws AxisFault {
        MessageContext messageContext;
        AxisService as1 = new AxisService("Service1");
        AxisService as2 = new AxisService("Service2");
        ConfigurationContext cc = ConfigurationContextFactory.createEmptyConfigurationContext();
        AxisConfiguration ac = cc.getAxisConfiguration();
        ac.addService(as1);
        ac.addService(as2);
        messageContext = cc.createMessageContext();

        SOAPEnvelope se = OMAbstractFactory.getSOAP11Factory().createSOAPEnvelope();
        SOAPBody sb = OMAbstractFactory.getSOAP11Factory().createSOAPBody(se);
        sb.addChild(OMAbstractFactory.getSOAP11Factory().createOMElement("operation2",
                                                                         "http://127.0.0.1:8080/axis2/services/Service2",
                                                                         "pfx"));
        messageContext.setEnvelope(se);

        ac.addParameter(ContentBasedDispatchPolicy.ALLOW_CONTENT_BASED_DISPATCH, "true");

        SOAPMessageBodyBasedServiceDispatcher ruisd = new SOAPMessageBodyBasedServiceDispatcher();
        ruisd.invoke(messageContext);

        assertEquals(as2, messageContext.getAxisService());
    }

    /** A message whose body first element namespace addresses the named service. */
    private MessageContext messageContextNaming(String serviceName) throws AxisFault {
        ConfigurationContext cc = ConfigurationContextFactory.createEmptyConfigurationContext();
        AxisConfiguration ac = cc.getAxisConfiguration();
        ac.addService(new AxisService(serviceName));
        MessageContext messageContext = cc.createMessageContext();

        SOAPEnvelope se = OMAbstractFactory.getSOAP11Factory().createSOAPEnvelope();
        SOAPBody sb = OMAbstractFactory.getSOAP11Factory().createSOAPBody(se);
        sb.addChild(OMAbstractFactory.getSOAP11Factory().createOMElement("operation2",
                "http://127.0.0.1:8080/axis2/services/" + serviceName, "pfx"));
        messageContext.setEnvelope(se);
        return messageContext;
    }

}
