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

package org.apache.axis2.jibx;

import static org.junit.Assert.assertEquals;

import org.apache.axis2.jibx.customer.EchoCustomerServiceStub;
import org.apache.axis2.testutils.Axis2Server;
import org.junit.ClassRule;

/**
 * Full code generation and runtime test for JiBX data binding extension. This is based on the
 * XMLBeans test code.
 */
public class Test {
    @ClassRule
    public static Axis2Server server = new Axis2Server("target/repo/echo");

    @org.junit.Test
    public void testBuildAndRun() throws Exception {
//         finish by testing a roundtrip call to the echo server
        Person person = new Person(42, "John", "Smith");
        Customer customer = new Customer("Redmond", person, "+14258858080",
                                         "WA", "14619 NE 80th Pl.", new Integer(98052));
        EchoCustomerServiceStub stub = new EchoCustomerServiceStub(server.getConfigurationContext(),
                server.getEndpoint("Echo") + "/echo");
        Customer result = stub.echo(customer);
        assertEquals("Result object does not match request object",
                     customer, result);
    }
}

