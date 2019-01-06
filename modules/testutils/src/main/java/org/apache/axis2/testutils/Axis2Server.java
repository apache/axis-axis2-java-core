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
package org.apache.axis2.testutils;

import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.transport.http.SimpleHTTPServer;
import org.junit.rules.ExternalResource;

public class Axis2Server extends ExternalResource {
    private final String repositoryPath;
    private int port = -1;
    private ConfigurationContext configurationContext;
    private SimpleHTTPServer server;

    public Axis2Server(String repositoryPath) {
        this.repositoryPath = repositoryPath;
    }

    public int getPort() {
        if (port == -1) {
            throw new IllegalStateException();
        }
        return port;
    }

    public ConfigurationContext getConfigurationContext() {
        if (configurationContext == null) {
            throw new IllegalStateException();
        }
        return configurationContext;
    }

    public String getEndpoint(String serviceName) throws AxisFault {
        return getConfigurationContext().getAxisConfiguration().getService(serviceName).getEPRs()[0];
    }

    public EndpointReference getEndpointReference(String serviceName) throws AxisFault {
        return new EndpointReference(getEndpoint(serviceName));
    }

    @Override
    protected void before() throws Throwable {
        port = PortAllocator.allocatePort();
        configurationContext =
                ConfigurationContextFactory.createConfigurationContextFromFileSystem(repositoryPath);
        server = new SimpleHTTPServer(configurationContext, port);
        server.start();
    }

    @Override
    protected void after() {
        port = -1;
        configurationContext = null;
        server.stop();
        server = null;
    }
}
