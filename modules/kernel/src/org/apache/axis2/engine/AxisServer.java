package org.apache.axis2.engine;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.description.AxisService;

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
/**
 * This class provide a very convenient way of creating server and deploying services.
 * Once someone call start method it will fire up configuration context and start up the listeners.
 *  One can provide repository location and axis.xml as system properties.
 */
public class AxisServer {

    protected ConfigurationContext configContext;
    protected ListenerManager listenerManager;

    public AxisServer() throws Exception {
    }

    protected ConfigurationContext createDefaultConfigurationContext() throws AxisFault {
        return ConfigurationContextFactory.createConfigurationContextFromFileSystem(null);
    }

    /**
     * Will create a configuration context from the avialable data and then it
     * will start the listener manager
     * @throws AxisFault if something went wrong
     */
    public void start()throws AxisFault {
        listenerManager = new ListenerManager();
        listenerManager.startSystem(getConfigurationContext());
    }

    /**
     * Will make Java class into a web service
     * @param serviceClassName : Actual class you want to make as a web service
     * @throws AxisFault : If something went wrong
     */
    public void deployService(String serviceClassName) throws AxisFault{
        AxisConfiguration axisConfig = getConfigurationContext().getAxisConfiguration();
        AxisService service = AxisService.createService(serviceClassName,axisConfig);
        axisConfig.addService(service);
    }

    public void stop() throws AxisFault{
        if(configContext!=null){
            configContext.terminate();
        }
    }

    /**
     * Creates a default configuration context if one is not set already via setConfigurationContext
     * 
     * @return
     * @throws AxisFault
     */
    public ConfigurationContext getConfigurationContext() throws AxisFault {
        if(configContext == null){
            configContext = createDefaultConfigurationContext();
        }
        return configContext;
    }

    /**
     * Set the configuration context. Please call this before you call deployService or start method
     * 
     * @param configContext
     */
    public void setConfigurationContext(ConfigurationContext configContext) {
        this.configContext = configContext;
    }
}
