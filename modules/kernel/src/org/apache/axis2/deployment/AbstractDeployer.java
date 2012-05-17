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
package org.apache.axis2.deployment;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.deployment.repository.util.DeploymentFileData;
import org.apache.axis2.description.AxisService;


import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * AbstractDeployer class which can be extended by all Axis2 deployers
 */
public abstract class AbstractDeployer implements Deployer{

    /**
     * The Map<String absoluteFilePath, DeploymentFileData data> of all artifacts deployed by this
     * deployer. 
     */
    protected Map<String, DeploymentFileData> deploymentFileDataMap
            = new ConcurrentHashMap<String, DeploymentFileData>();
    
    /**
     * Keep ServiceBuilderExtension associated with this Deployer.
     */
    private List<ServiceBuilderExtension> serviceBuilderExtensions = new CopyOnWriteArrayList<ServiceBuilderExtension>();

    public void deploy(DeploymentFileData deploymentFileData) throws DeploymentException {
        deploymentFileDataMap.put(deploymentFileData.getAbsolutePath(), deploymentFileData);
    }

    public void undeploy(String fileName) throws DeploymentException {
        deploymentFileDataMap.remove(fileName);
    }

    public void cleanup() throws DeploymentException {
        // Deployers which require cleaning up should override this method
    }

    public List<ServiceBuilderExtension> getServiceBuilderExtensions() {
        return serviceBuilderExtensions;
    }

    public void addServiceBuilderExtensions(ServiceBuilderExtension serviceBuilderExtension) {
        serviceBuilderExtensions.add(serviceBuilderExtension);
    }

    /**
     * This method executes ServiceBuilderExtensions associated with this
     * Deployer instance and return a list AxisService instances. It is required
     * to explicitly call this method within the deploy() method in order to use
     * ServiceBuilderExtension.
     * 
     * @param deploymentFileData
     * @param configurationContext
     * @return
     * @throws DeploymentException
     */
    protected Map<String, AxisService> executeServiceBuilderExtensions(
            DeploymentFileData deploymentFileData, ConfigurationContext configurationContext)
            throws DeploymentException {
        if (getServiceBuilderExtensions().size() > 0) {
            for (ServiceBuilderExtension ext : getServiceBuilderExtensions()) {
                // a Service should be build by only one ServiceBuilderExtension
                Map<String, AxisService> serviceMap = ext.buildAxisServices(deploymentFileData);
                if (serviceMap != null && serviceMap.size() > 0) {
                    return serviceMap;
                }
            }
        }
        return new HashMap<String, AxisService>();
    }
    
}
