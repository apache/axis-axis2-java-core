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
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.client.Stub;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.junit.rules.ExternalResource;

public class ClientHelper extends ExternalResource {
    private final AbstractAxis2Server server;
    private final String repositoryPath;
    private ConfigurationContext configurationContext;

    public ClientHelper(AbstractAxis2Server server, String repositoryPath) {
        this.server = server;
        this.repositoryPath = repositoryPath;
    }

    public ClientHelper(AbstractAxis2Server server) {
        this(server, server.getRepositoryPath());
    }

    @Override
    protected final void before() throws Throwable {
        configurationContext =
                ConfigurationContextFactory.createConfigurationContextFromFileSystem(repositoryPath);
    }

    @Override
    protected final void after() {
        configurationContext = null;
    }

    public final ServiceClient createServiceClient(String serviceName) throws Exception {
        ServiceClient serviceClient = new ServiceClient(configurationContext, null);
        serviceClient.getOptions().setTo(server.getEndpointReference(serviceName));
        configureServiceClient(serviceClient);
        return serviceClient;
    }

    public final <T extends Stub> T createStub(Class<T> type, String serviceName) throws Exception {
        T stub = type
                .getConstructor(ConfigurationContext.class, String.class)
                .newInstance(configurationContext, server.getEndpoint(serviceName));
        configureServiceClient(stub._getServiceClient());
        return stub;
    }

    protected void configureServiceClient(ServiceClient serviceClient) throws Exception {
    }
}
