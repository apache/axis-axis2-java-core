/*
 * Copyright 2004,2005 The Apache Software Foundation.
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

package org.apache.axis2.cluster.configuration;

import org.apache.axis2.engine.AxisConfiguration;

public interface ConfigurationManagerListener {
    void serviceGroupLoaded(ConfigurationEvent event);
    void serviceGroupUnloaded(ConfigurationEvent event);
    void policyApplied(ConfigurationEvent event);
    void configurationReloaded (ConfigurationEvent event);
    void prepareCalled (ConfigurationEvent event);
    void rollbackCalled (ConfigurationEvent event);
    void commitCalled (ConfigurationEvent event);
    void handleException(Throwable throwable);
    void setAxisConfiguration (AxisConfiguration axisConfiguration);
}
