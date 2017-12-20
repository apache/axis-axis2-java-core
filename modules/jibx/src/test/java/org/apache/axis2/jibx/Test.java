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

import junit.framework.TestCase;
import org.apache.axis2.jibx.customer.EchoCustomerServiceStub;
import org.apache.axis2.testutils.UtilServer;

/**
 * Full code generation and runtime test for JiBX data binding extension. This is based on the
 * XMLBeans test code.
 */
public class Test extends TestCase {
    private static final String REPOSITORY_DIR =
            System.getProperty("basedir", ".") + "/target/repo/echo";

    private void startServer() throws Exception {
        UtilServer.start(REPOSITORY_DIR);
    }

    private void stopServer() throws Exception {
        UtilServer.stop();
/*        File outputFile = new File(OUTPUT_LOCATION_BASE);
        if (outputFile.exists() && outputFile.isDirectory()){
            deleteDir(outputFile);
        }   */
    }

    public void testBuildAndRun() throws Exception {
        startServer();

//         finish by testing a roundtrip call to the echo server
        Person person = new Person(42, "John", "Smith");
        Customer customer = new Customer("Redmond", person, "+14258858080",
                                         "WA", "14619 NE 80th Pl.", new Integer(98052));
        EchoCustomerServiceStub stub = new EchoCustomerServiceStub(UtilServer.getConfigurationContext(),
                "http://127.0.0.1:" + UtilServer.TESTING_PORT + "/axis2/services/Echo/echo");
        Customer result = stub.echo(customer);
        stopServer();
        assertEquals("Result object does not match request object",
                     customer, result);
    }
}

