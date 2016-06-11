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
package org.apache.axis2;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.engine.AxisConfiguration;
import org.junit.rules.ExternalResource;

public class Axis2Repo extends ExternalResource {
    private final String location;
    private ConfigurationContext configurationContext;

    public Axis2Repo(String location) {
        this.location = location;
    }

    public ConfigurationContext getConfigurationContext() {
        return configurationContext;
    }

    public AxisConfiguration getAxisConfiguration() {
        return configurationContext.getAxisConfiguration();
    }

    @Override
    protected void before() throws Throwable {
        configurationContext = ConfigurationContextFactory
                .createConfigurationContextFromFileSystem(location, location + "/axis2.xml");
    }

    @Override
    protected void after() {
        try {
            configurationContext.terminate();
        } catch (AxisFault ex) {
            throw new RuntimeException(ex);
        }
    }
}
