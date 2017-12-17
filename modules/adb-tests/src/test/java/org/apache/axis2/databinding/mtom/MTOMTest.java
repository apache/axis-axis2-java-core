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
package org.apache.axis2.databinding.mtom;

import javax.activation.DataHandler;
import javax.xml.ws.Endpoint;

import org.apache.axiom.testutils.PortAllocator;
import org.apache.axiom.testutils.activation.RandomDataSource;
import org.apache.axiom.testutils.io.IOTestUtils;
import org.apache.axis2.Constants;
import org.apache.axis2.databinding.mtom.client.MTOMServiceStub;
import org.apache.axis2.databinding.mtom.client.MTOMServiceStub.GetContent;
import org.apache.axis2.databinding.mtom.service.MTOMServiceImpl;
import org.junit.Test;

public class MTOMTest {
    @Test
    public void test() throws Exception {
        int port = PortAllocator.allocatePort();
        String address = "http://localhost:" + port + "/mtom";
        Endpoint endpoint = Endpoint.publish(address, new MTOMServiceImpl());
        try {
            MTOMServiceStub stub = new MTOMServiceStub(address);
            // JAX-WS only produces an MTOM response if the request uses MTOM
            stub._getServiceClient().getOptions().setProperty(Constants.Configuration.ENABLE_MTOM, Constants.VALUE_TRUE);
            DataHandler content = stub.getContent(new GetContent()).getContent();
            IOTestUtils.compareStreams(
                    new RandomDataSource(654321L, 1000000).getInputStream(), "expected",
                    content.getInputStream(), "actual");
        } finally {
            endpoint.stop();
        }
    }
}
