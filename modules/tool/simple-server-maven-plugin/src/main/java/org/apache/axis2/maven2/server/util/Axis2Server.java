/*
 * Copyright 2004, 2009 The Apache Software Foundation.
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

package org.apache.axis2.maven2.server.util;

import org.apache.axis2.AxisFault;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.description.TransportInDescription;
import org.apache.axis2.transport.SimpleAxis2Server;
import org.apache.maven.plugin.logging.Log;

import static org.apache.axis2.maven2.server.util.Constants.DEFAULT_REPO_LOCATION;
import static org.apache.axis2.maven2.server.util.Constants.DEFAULT_PORT_PARAM;


/**
 * The Class Axis2Server.
 * 
 * @since 1.7.0
 */
public class Axis2Server extends SimpleAxis2Server {

    private static Axis2Server server;
    private static Log log;

    /**
     * Create new instance of Axis2Server.
     *
     * @param repoPath the repo path
     * @param confPath the conf path
     * @param port the port
     * @param log 
     * @return the axis2 server
     */
    public static Axis2Server newInstance(String repoPath, String confPath, String port, Log mavenLog) {
        try {
            log = mavenLog;
            if (repoPath == null) {
                repoPath = DEFAULT_REPO_LOCATION;
            }
            server = new Axis2Server(repoPath, confPath);
            if (confPath == null) {
                /**
                 * By default Axis2 HTTP transport listen on 6060 ,if user does
                 * not specify any port use 8080 as HTTP port.
                 */
                Parameter parameter = new Parameter();
                parameter.setName(DEFAULT_PORT_PARAM);
                parameter.setValue(port);
                ((TransportInDescription) server.getConfigurationContext().getAxisConfiguration()
                        .getTransportIn("http")).addParameter(parameter);
            }
        } catch (Exception e) {
            log.error(e);
        }
        return server;
    }

    /**
     * Instantiates a new axis2 server.
     *
     * @param repoPath the repo path
     * @param confPath the conf path
     * @throws Exception the exception
     */
    private Axis2Server(String repoPath, String confPath) throws Exception {       
        super(repoPath, confPath);
        server = null;
    }

    /**
     * Start server.
     */
    public void startServer() {
        try {
            server.start();
        } catch (AxisFault e) {
            log.error(e);
        }
    }

    /**
     * Stop server.
     */
    public void stopServer() {
        try {
            server.stop();
        } catch (AxisFault e) {
            log.error(e);            
        }
    }

}
