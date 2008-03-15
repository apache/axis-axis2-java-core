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
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.engine.AxisConfiguration;

public class RequestURIBasedServiceDispatcherTest extends TestCase {

    public void testFindService() throws AxisFault {
        MessageContext messageContext;
        AxisService as1 = new AxisService("Service1");
        AxisService as2 = new AxisService("Service2");

        ConfigurationContext cc = ConfigurationContextFactory.createEmptyConfigurationContext();
        AxisConfiguration ac = cc.getAxisConfiguration();
        ac.addService(as1);
        ac.addService(as2);
        messageContext = cc.createMessageContext();

        messageContext
                .setTo(new EndpointReference("http://127.0.0.1:8080/axis2/services/Service2"));

        RequestURIBasedServiceDispatcher ruisd = new RequestURIBasedServiceDispatcher();
        ruisd.invoke(messageContext);

        assertEquals(as2, messageContext.getAxisService());
    }

}
