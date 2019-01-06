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

import javax.net.ssl.SSLContext;

import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.engine.AxisConfiguration;
import org.junit.rules.ExternalResource;

public abstract class AbstractAxis2Server extends ExternalResource {
    private final String repositoryPath;
    private final AxisServiceFactory[] serviceFactories;
    private ConfigurationContext configurationContext;

    public AbstractAxis2Server(String repositoryPath, AxisServiceFactory... serviceFactories) {
        this.repositoryPath = repositoryPath;
        this.serviceFactories = serviceFactories;
    }

    final String getRepositoryPath() {
        return repositoryPath;
    }

    public final ConfigurationContext getConfigurationContext() {
        if (configurationContext == null) {
            throw new IllegalStateException();
        }
        return configurationContext;
    }

    @Override
    protected void before() throws Throwable {
        configurationContext =
                ConfigurationContextFactory.createConfigurationContextFromFileSystem(repositoryPath);
        AxisConfiguration axisConfiguration = configurationContext.getAxisConfiguration();
        for (AxisServiceFactory serviceFactory : serviceFactories) {
            axisConfiguration.addService(serviceFactory.createService(axisConfiguration));
        }
        startServer(configurationContext);
    }

    @Override
    protected void after() {
        stopServer();
        configurationContext = null;
    }

    protected abstract void startServer(ConfigurationContext configurationContext) throws Throwable;
    protected abstract void stopServer();

    public abstract boolean isSecure();
    public abstract SSLContext getClientSSLContext() throws Exception;
    public abstract int getPort();
    public abstract String getEndpoint(String serviceName) throws AxisFault;
    public abstract EndpointReference getEndpointReference(String serviceName) throws AxisFault;
}
