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

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.jaxws.ClientConfigurationFactory;
import org.apache.axis2.metadata.registry.MetadataFactoryRegistry;
import org.apache.axis2.testutils.AbstractConfigurationContextRule;

public class ClientConfigurationContextBinder extends AbstractConfigurationContextRule {
    private ClientConfigurationFactory savedClientConfigurationFactory;

    public ClientConfigurationContextBinder(String repositoryPath) {
        super(repositoryPath);
    }

    @Override
    protected void before() throws Throwable {
        super.before();
        savedClientConfigurationFactory = (ClientConfigurationFactory)MetadataFactoryRegistry.getFactory(ClientConfigurationFactory.class);
        MetadataFactoryRegistry.setFactory(ClientConfigurationFactory.class, new ClientConfigurationFactory() {
            @Override
            public synchronized ConfigurationContext getClientConfigurationContext() {
                return ClientConfigurationContextBinder.this.getConfigurationContext();
            }
        });
    }

    @Override
    protected void after() {
        MetadataFactoryRegistry.setFactory(ClientConfigurationFactory.class, savedClientConfigurationFactory);
        super.after();
    }
}
