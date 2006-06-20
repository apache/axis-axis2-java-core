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
import org.apache.axis2.jaxws.util.Constants;


/**
 * This class serves as a factory for ConfigurationContexts suitable in the client environment.  
 */
public class ClientConfigurationFactory {
    
    private static ClientConfigurationFactory instance = new ClientConfigurationFactory();
    
    private ConfigurationContext configContext = null;
    
    protected ClientConfigurationFactory(){
    }

    /**
     * Returns the singleton ClientConfigurationFactory object.
     */
    public static ClientConfigurationFactory newInstance() {
        return instance;
    }

    /**
     * Loads up a ConfigurationContext object using the WAS-specific configuration builder.
     * @return a ConfigurationContext object that is suitable for the client environment
     */
    public synchronized ConfigurationContext getClientConfigurationContext() {
        if (configContext == null) {
            //TODO: Add logging 
            String repoPath = System.getProperty(Constants.AXIS2_REPO_PATH);
            String axisConfigPath = System.getProperty(Constants.AXIS2_CONFIG_PATH);
            try {
                configContext = ConfigurationContextFactory
                        .createConfigurationContextFromFileSystem(repoPath, axisConfigPath);
            } catch (AxisFault e) {
                e.printStackTrace();
            }
        }
        
        return configContext;
    }
}
