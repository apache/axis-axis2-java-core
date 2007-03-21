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

import org.apache.axis2.cluster.ClusteringFault;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.neethi.Policy;

public interface ConfigurationManager {

    /*
      * Configuration management methods
      */
    void loadServiceGroup(String serviceGroupName) throws ClusteringFault;

    void unloadServiceGroup(String serviceGroupName) throws ClusteringFault;

    void applyPolicy(String serviceGroupName, Policy policy) throws ClusteringFault;

    void reloadConfiguration() throws ClusteringFault;

    /*
    * Transaction management methods
    */
    void prepare() throws ClusteringFault;

    void rollback() throws ClusteringFault;

    void commit() throws ClusteringFault;

    /**
     * To notify other nodes that an Exception occurred, during the processing
     * of a {@link ConfigurationEvent}
     *
     * @param throwable The throwable which has to be propogated to other nodes
     */
    void exceptionOccurred(Throwable throwable) throws ClusteringFault;

    /**
     * For registering a configuration event listener.
     */
    void addConfigurationManagerListener(ConfigurationManagerListener listener);

    void setConfigurationContext(ConfigurationContext configurationContext);
}