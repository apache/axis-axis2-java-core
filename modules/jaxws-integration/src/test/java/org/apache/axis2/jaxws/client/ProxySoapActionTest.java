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

package org.apache.axis2.jaxws.client;

import javax.xml.ws.BindingProvider;

import org.apache.axis2.jaxws.TestLogger;
import org.apache.axis2.jaxws.client.soapaction.BookStore;
import org.apache.axis2.jaxws.client.soapaction.BookStoreService;
import org.apache.axis2.testutils.Axis2Server;
import org.junit.ClassRule;
import org.junit.Test;

/**
 * A suite of SOAPAction related tests for the dynamic proxy client 
 */
public class ProxySoapActionTest {
    @ClassRule
    public static final Axis2Server server = new Axis2Server("target/repo");
    
    @Test
    public void testSendRequestWithSoapAction() throws Exception {
        TestLogger.logger.debug("----------------------------------");

        BookStoreService service = new BookStoreService();
        BookStore bs = service.getBookStorePort();
        ((BindingProvider)bs).getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
                server.getEndpoint("BookStoreService"));
        
        float price = bs.getPriceWithAction("test item");
        TestLogger.logger.debug("return value [" + price + "]");
        //assertTrue("The return value was invalid", price > 0);
    }

}
