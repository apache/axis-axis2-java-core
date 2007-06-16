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

    private ConfigurationContext configContext;

    /**
     * Will create a configuration context from the avialable data and then it
     * will start the listener manager
     * @throws AxisFault if something went wrong
     */
    public void start()throws AxisFault {
        configContext = ConfigurationContextFactory.createConfigurationContextFromFileSystem(null);
        ListenerManager listenerManager = new ListenerManager();
        listenerManager.startSystem(configContext);
    }

    /**
     * Will make Java class into a web service
     * @param serviceClassName : Actual class you want to make as a web service
     * @throws AxisFault : If something went wrong
     */
    public void deployService(String serviceClassName) throws AxisFault{
        if(configContext==null){
            start();
        }
        AxisConfiguration axisConfig = configContext.getAxisConfiguration();
        AxisService service = AxisService.createService(serviceClassName,axisConfig);
        axisConfig.addService(service);
    }

    public void stop() throws AxisFault{
        if(configContext!=null){
            configContext.terminate();
        }
    }


    public ConfigurationContext getConfigContext() {
        return configContext;
    }

    public void setConfigContext(ConfigurationContext configContext) {
        this.configContext = configContext;
    }
}
