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

package org.apache.axis2.jaxws.sample;

import org.apache.axis2.jaxws.TestLogger;
import org.apache.axis2.jaxws.sample.wsgen.client.WSGenInterface;
import org.apache.axis2.jaxws.sample.wsgen.client.WSGenService;
import org.apache.axis2.testutils.Axis2Server;
import org.junit.ClassRule;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import jakarta.xml.ws.BindingProvider;

public class WSGenTests {
    @ClassRule
    public static final Axis2Server server = new Axis2Server("target/repo");
    
    @Test
    public void testWSGen() {
        try{
            TestLogger.logger.debug("----------------------------------");
            
            WSGenService service = new WSGenService();
            WSGenInterface proxy = service.getWSGenPort();
            
            BindingProvider p = (BindingProvider)proxy;
            p.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, 
                    server.getEndpoint("WSGenService.WSGenPort"));  
            String outString = "this is a wonderful test";
            String s = proxy.echoString(outString);

            TestLogger.logger.debug("String s = " + s);
            assertEquals(outString, s);
            
            // Repeat to ensure validity
            s = proxy.echoString(outString);

            TestLogger.logger.debug("String s = " + s);
            assertEquals(outString, s);
            TestLogger.logger.debug("----------------------------------");
        } catch(Exception e) {
            e.printStackTrace();
            fail("We should not get an exception, but we did: " + e);
        }
    }
}
