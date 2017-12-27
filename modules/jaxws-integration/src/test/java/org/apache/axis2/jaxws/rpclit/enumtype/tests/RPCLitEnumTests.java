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

package org.apache.axis2.jaxws.rpclit.enumtype.tests;

import org.apache.axis2.jaxws.TestLogger;
import org.apache.axis2.jaxws.rpclit.enumtype.sei.PortType;
import org.apache.axis2.jaxws.rpclit.enumtype.sei.Service;
import org.apache.axis2.testutils.Axis2Server;
import org.junit.ClassRule;
import org.junit.Test;
import org.test.rpclit.schema.ElementString;

import static org.junit.Assert.fail;

import javax.xml.ws.BindingProvider;
import javax.xml.ws.Holder;

public class RPCLitEnumTests {
    @ClassRule
    public static final Axis2Server server = new Axis2Server("target/repo");

    @Test
    public void testEnumSimpleType(){
        TestLogger.logger.debug("------------------------------");
        try{
            Service service = new Service();
            PortType portType = service.getPort();

            BindingProvider p = (BindingProvider) portType;
            p.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
                    server.getEndpoint("RPCLitEnumService.PortTypeImplPort"));

            Holder<ElementString> pString = new Holder<ElementString>(ElementString.A);
            portType.echoString(pString);
            ElementString es = pString.value;
            TestLogger.logger.debug("Response =" + es);
            
            // Try a second time
            pString = new Holder<ElementString>(ElementString.A);
            portType.echoString(pString);
            es = pString.value;
            TestLogger.logger.debug("Response =" + es);
            System.out.print("---------------------------------");
        }catch(Exception e){
            e.printStackTrace();
            fail();
        }
    }
}
