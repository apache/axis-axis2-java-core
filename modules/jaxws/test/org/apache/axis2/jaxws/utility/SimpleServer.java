/*
 * Copyright 2004,2005 The Apache Software Foundation.
 * Copyright 2006 International Business Machines Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.axis2.jaxws.utility;

import org.apache.axis2.AxisFault;
import org.apache.axis2.jaxws.TestLogger;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.transport.http.SimpleHTTPServer;
import org.apache.log4j.BasicConfigurator;



public class SimpleServer {

    private static SimpleHTTPServer server;
    private String repositoryDir;
    private int port = 8080;
    
    public void init() {
        repositoryDir = System.getProperty("basedir",".")+"/"+System.getProperty("build.repository");
//        repositoryDir = "target/test-classes"; 
        TestLogger.logger.debug(">> repositoryDir = " + repositoryDir);
        
        String axis2xml = System.getProperty("axis2.config");
        TestLogger.logger.debug(">> axis2.xml     = " + axis2xml);
        
        try {
            ConfigurationContext config = ConfigurationContextFactory.createConfigurationContextFromFileSystem(
                    repositoryDir, axis2xml);
            server = new SimpleHTTPServer(config, port);
        } catch (AxisFault e) {
            e.printStackTrace();
        }
    }
    
    public void start() {
        TestLogger.logger.debug("------------ starting server ---------------");
        init();
        if (server != null) {
            try {
                server.start();
            } catch (AxisFault e) {
                e.printStackTrace();
            }
        }
        TestLogger.logger.debug("------------------ done --------------------");
    }
    
    public void stop() {
        TestLogger.logger.debug("------------ stopping server ---------------");
        if (server != null) {
            server.stop();
        }
        TestLogger.logger.debug("------------------ done --------------------");
    }
    
    public static void main(String[] args) throws Exception {
        // To change the settings, edit the log4j.property file
        // in the test-resources directory.
        BasicConfigurator.configure();
        SimpleServer server = new SimpleServer();
        server.start();
    }
}
