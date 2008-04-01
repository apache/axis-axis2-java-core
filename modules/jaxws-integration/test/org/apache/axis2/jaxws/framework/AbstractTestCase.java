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

package org.apache.axis2.jaxws.framework;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestCase;
import org.apache.axis2.jaxws.TestLogger;
import org.apache.log4j.BasicConfigurator;

public class AbstractTestCase extends TestCase {
    public AbstractTestCase() {
        super();
    }

    static {
        BasicConfigurator.configure();
    }
    
    /*
     * users may pass in their own repositoryDir path and path to custom configuration file.
     * Passing 'null' for either param will use the default
     */
    protected static Test getTestSetup(Test test, final String repositoryDir, final String axis2xml) {
        return new TestSetup(test) {
            public void setUp() throws Exception {
                TestLogger.logger.debug("Starting the server for: " +this.getClass().getName());
                StartServer startServer = new StartServer("server1");
                startServer.testStartServer(repositoryDir, axis2xml);
            }

            public void tearDown() throws Exception {
                TestLogger.logger.debug("Stopping the server for: " +this.getClass().getName());
                StopServer stopServer = new StopServer("server1");
                stopServer.testStopServer();
            }
        };
    }

    protected static Test getTestSetup(Test test) {
        return new TestSetup(test) {
            public void setUp() throws Exception {
                TestLogger.logger.debug("Starting the server for: " +this.getClass().getName());
                StartServer startServer = new StartServer("server1");
                startServer.testStartServer();
            }

            public void tearDown() throws Exception {
                TestLogger.logger.debug("Stopping the server for: " +this.getClass().getName());
                StopServer stopServer = new StopServer("server1");
                stopServer.testStopServer();
            }
        };
    }
}
