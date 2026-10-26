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
import org.apache.axis2.description.AxisBinding;
import org.apache.axis2.description.AxisEndpoint;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.InOnlyAxisOperation;
import org.apache.axis2.description.WSDL2Constants;
import org.apache.axis2.kernel.http.HTTPConstants;

import javax.xml.namespace.QName;
import java.util.HashMap;
import java.util.Map;

public class HTTPLocationBasedDispatcherTest extends TestCase {

    private AxisService service;
    private AxisOperation safe;
    private AxisOperation delete;
    private AxisEndpoint endpoint;
    private ConfigurationContext cc;

    protected void setUp() throws Exception {
        service = new AxisService("Svc");
        safe = new InOnlyAxisOperation(new QName("safe"));
        delete = new InOnlyAxisOperation(new QName("delete"));
        service.addOperation(safe);
        service.addOperation(delete);

        Map<String, AxisOperation> httpLocationTable = new HashMap<String, AxisOperation>();
        httpLocationTable.put("GET/safe", safe);
        httpLocationTable.put("GET/delete", delete);
        AxisBinding binding = new AxisBinding();
        binding.setProperty(WSDL2Constants.HTTP_LOCATION_TABLE, httpLocationTable);
        endpoint = new AxisEndpoint();
        endpoint.setBinding(binding);

        cc = ConfigurationContextFactory.createEmptyConfigurationContext();
        cc.getAxisConfiguration().addService(service);
    }

    private AxisOperation findOperation(String to) throws AxisFault {
        MessageContext messageContext = cc.createMessageContext();
        messageContext.setTo(new EndpointReference(to));
        messageContext.setAxisService(service);
        messageContext.setProperty(WSDL2Constants.ENDPOINT_LOCAL_NAME, endpoint);
        messageContext.setProperty(HTTPConstants.HTTP_METHOD, "GET");
        return new HTTPLocationBasedDispatcher().findOperation(service, messageContext);
    }

    public void testLocationFromPath() throws AxisFault {
        assertEquals(safe, findOperation("/axis2/services/Svc/safe"));
        assertEquals(delete, findOperation("/axis2/services/Svc/delete"));
        assertEquals(safe, findOperation("/axis2/services/Svc/safe?x=1"));
    }

    /**
     * The location starts after the service name in the request path; a query
     * value that repeats the service name must not select a different location.
     */
    public void testLocationNotTakenFromQueryString() throws AxisFault {
        assertEquals(safe, findOperation("/axis2/services/Svc/safe?x=/Svc/delete"));
        assertNull(findOperation("/axis2/services/Svc?x=/Svc/delete"));
    }
}
