package org.apache.axis2.deployment;

import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.engine.AxisConfigurator;

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

public class FileSystemConfigurator implements AxisConfigurator {

    /**
     * To check whether need to create a service side or client side
     */
    private String axis2xml;
    private String repoLocation;

    /**
     * Load an AxisConfiguration from the repository directory specified
     *
     * @param repoLocation
     * @param axis2xml
     */
    public FileSystemConfigurator(String repoLocation, String axis2xml) {
        if (repoLocation == null) {
            //checking wether user has set the system property
            repoLocation = System.getProperty(Constants.AXIS2_REPO);
        }
        if (axis2xml == null) {
            axis2xml  = System.getProperty(Constants.AXIS2_CONF);
        }
        this.repoLocation = repoLocation;
        this.axis2xml = axis2xml;
    }

    /**
     * First create a Deployment engine, use that to create an AxisConfiguration
     *
     * @return Axis Configuration
     * @throws AxisFault
     */
    public synchronized AxisConfiguration getAxisConfiguration() throws AxisFault {
        return new DeploymentEngine(repoLocation, axis2xml).load();
    }
}
