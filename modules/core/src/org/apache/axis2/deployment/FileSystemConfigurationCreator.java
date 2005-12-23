package org.apache.axis2.deployment;

import org.apache.axis2.AxisFault;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.engine.AxisConfigurationCreator;

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
*
*
*/

public class FileSystemConfigurationCreator implements AxisConfigurationCreator {

    /**
     * To check whether need to create a service side or client side
     */
    private boolean isServer;
    private String repoLocation;

    /**
     * Load an AxisConfiguration from the repository directory specified
     * 
     * @param repoLocation
     * @param isServer
     */
    public FileSystemConfigurationCreator(String repoLocation, boolean isServer) {
        if (repoLocation == null) {
            repoLocation = AxisConfiguration.getAxis2HomeDirectory();
        }
        this.repoLocation = repoLocation;
        this.isServer = isServer;
    }

    /**
     * First create a Deployment engine, use that to create an AxisConfiguration
     *
     * @return Axis Configuration
     * @throws AxisFault
     */
    public AxisConfiguration getAxisConfiguration() throws AxisFault {
        if (isServer) {
            DeploymentEngine deploymentEngine = new DeploymentEngine(repoLocation);
            return deploymentEngine.load();
        } else {
            return new DeploymentEngine().loadClient(repoLocation);
        }
    }
}
