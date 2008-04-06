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

package org.apache.axis2.clustering.configuration;

import org.apache.axis2.context.ConfigurationContext;

import java.util.ArrayList;

public class TestConfigurationManagerListener implements ConfigurationManagerListener {

    private ArrayList commandList;
    private ConfigurationContext configurationContext;

    public TestConfigurationManagerListener() {
        commandList = new ArrayList();
    }

    public void serviceGroupsLoaded(ConfigurationClusteringCommand command) {
        //TODO: Method implementation

    }

    public void serviceGroupsUnloaded(ConfigurationClusteringCommand command) {
        //TODO: Method implementation

    }

    public void policyApplied(ConfigurationClusteringCommand command) {
        //TODO: Method implementation

    }

    public void configurationReloaded(ConfigurationClusteringCommand command) {
        //TODO: Method implementation

    }

    public void prepareCalled() {
        //TODO: Method implementation

    }

    public void rollbackCalled() {
        //TODO: Method implementation

    }

    public void commitCalled() {
        //TODO: Method implementation

    }

    public void handleException(Throwable throwable) {
        // TODO Auto-generated method stub
    }

    public void setConfigurationContext(ConfigurationContext configurationContext) {
        //TODO: Method implementation

    }


}
