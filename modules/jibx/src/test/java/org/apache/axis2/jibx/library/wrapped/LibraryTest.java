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
package org.apache.axis2.jibx.library.wrapped;

import org.apache.axis2.Constants;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.jibx.UtilServer;
import org.apache.axis2.jibx.beans.Book;
import org.apache.axis2.jibx.library.wrapped.client.LibraryStub;
import org.apache.axis2.jibx.library.wrapped.service.LibraryImpl;
import org.apache.axis2.jibx.wrappers.AddBookRequest;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class LibraryTest {
    @BeforeClass
    public static void startServer() throws Exception {
        UtilServer.start(System.getProperty("basedir", ".") + "/target/repo/library-wrapped");
        AxisConfiguration axisConfiguration = UtilServer.getConfigurationContext().getAxisConfiguration();
        AxisService service = axisConfiguration.getService("library");
        service.getParameter(Constants.SERVICE_CLASS).setValue(LibraryImpl.class.getName());
        service.setScope(Constants.SCOPE_APPLICATION);
        service.engageModule(axisConfiguration.getModule("checker"));
    }
    
    @AfterClass
    public static void stopServer() throws Exception {
        UtilServer.stop();
    }
    
    @Test
    public void test() throws Exception {
        LibraryStub stub = new LibraryStub(UtilServer.getConfigurationContext(), "http://127.0.0.1:5555/axis2/services/library");
        stub._getServiceClient().engageModule("checker");
        
        stub.addBook(new AddBookRequest(new Book("Paperback", "0618918248", "The God Delusion", new String[] { "Richard Dawkins" })));
    }
}
