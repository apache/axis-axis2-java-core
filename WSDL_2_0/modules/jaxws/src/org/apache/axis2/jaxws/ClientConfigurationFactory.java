/*
 * Copyright 2004,2005 The Apache Software Foundation.
 * Copyright 2006 International Business Machines Corp.
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
package org.apache.axis2.jaxws;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.deployment.DeploymentException;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.jaxws.util.Constants;


/**
 * This class serves as a factory for ConfigurationContexts suitable in the client environment.  
 */
public class ClientConfigurationFactory {
    
    private static ClientConfigurationFactory instance = new ClientConfigurationFactory();
    
    protected ClientConfigurationFactory(){
    }

    /**
     * Returns a ClientConfigurationFactory object.
     */
    public static ClientConfigurationFactory newInstance() {
        return instance;
    }

    /**
     * Loads up a ConfigurationContext object using the configuration builder.
     * @return a ConfigurationContext object that is suitable for the client environment
     */
    public synchronized ConfigurationContext getClientConfigurationContext() {
        ConfigurationContext configContext = null;
        //TODO: Add logging 
        String repoPath = System.getProperty(Constants.AXIS2_REPO_PATH);
        String axisConfigPath = System.getProperty(Constants.AXIS2_CONFIG_PATH);
        try {
            configContext = ConfigurationContextFactory
                    .createConfigurationContextFromFileSystem(repoPath, axisConfigPath);
        } catch (AxisFault e) {
            // TODO: Add RAS logging and processing
            e.printStackTrace();
        }
        return configContext;
    }
    
    /**
     * Perform any final client-specific configuration on a newly created AxisService.
     * 
     * @param service A newly created AxisService on which to perform any final client-related configuration.
     * @throws DeploymentException
     * @throws Exception
     */
    public synchronized void completeAxis2Configuration(AxisService service) throws DeploymentException, Exception {
    }
}
