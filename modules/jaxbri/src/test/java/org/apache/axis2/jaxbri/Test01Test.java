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
package org.apache.axis2.jaxbri;

import static org.junit.Assert.assertEquals;

import org.apache.axis2.Constants;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.testutils.UtilServer;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.foo.wsns.axis2.test01.Test01;
import com.foo.wsns.axis2.test01.Test01Stub;
import com.foo.xmlns.axis2.test01.Add;

public class Test01Test {
    @BeforeClass
    public static void startServer() throws Exception {
        UtilServer.start(System.getProperty("basedir", ".") + "/target/repo/Test01");
        AxisConfiguration axisConfiguration = UtilServer.getConfigurationContext().getAxisConfiguration();
        AxisService service = axisConfiguration.getService("Test01");
        service.getParameter(Constants.SERVICE_CLASS).setValue(Test01Impl.class.getName());
    }
    
    @AfterClass
    public static void stopServer() throws Exception {
        UtilServer.stop();
    }
    
    @Test
    public void test() throws Exception {
        Test01 stub = new Test01Stub(UtilServer.getConfigurationContext(), "http://127.0.0.1:" + UtilServer.TESTING_PORT + "/axis2/services/Test01");
        Add add = new Add();
        add.setArg1(3);
        add.setArg2(4);
        assertEquals(7, stub.add(add));
    }
}
