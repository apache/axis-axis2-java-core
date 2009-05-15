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

package org.apache.axis2.clustering.tribes;

import org.apache.axis2.clustering.ClusterManager;
import org.apache.axis2.clustering.configuration.DefaultConfigurationManager;
import org.apache.axis2.clustering.configuration.DefaultConfigurationManagerListener;
import org.apache.axis2.clustering.context.DefaultContextManager;
import org.apache.axis2.clustering.context.DefaultContextManagerListener;
import org.apache.axis2.context.ConfigurationContext;

public class ConfigurationManagerTest extends
                                      org.apache.axis2.clustering.configuration.ConfigurationManagerTestCase {

    protected ClusterManager getClusterManager(ConfigurationContext configCtx) {
        TribesClusterManager tribesClusterManager = new TribesClusterManager();
        tribesClusterManager.setConfigurationContext(configCtx);
        DefaultConfigurationManager configurationManager = new DefaultConfigurationManager();
        configurationManager.
                setConfigurationManagerListener(new DefaultConfigurationManagerListener());
        tribesClusterManager.setConfigurationManager(configurationManager);
        DefaultContextManager contextManager = new DefaultContextManager();
        contextManager.setContextManagerListener(new DefaultContextManagerListener());
        tribesClusterManager.setContextManager(contextManager);
        return tribesClusterManager;
    }

}
