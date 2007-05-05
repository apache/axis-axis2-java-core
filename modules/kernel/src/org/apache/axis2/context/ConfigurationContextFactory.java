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

package org.apache.axis2.context;

import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.deployment.*;
import org.apache.axis2.deployment.util.Utils;
import org.apache.axis2.description.AxisModule;
import org.apache.axis2.description.AxisServiceGroup;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.description.TransportOutDescription;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.engine.AxisConfigurator;
import org.apache.axis2.engine.DependencyManager;
import org.apache.axis2.i18n.Messages;
import org.apache.axis2.modules.Module;
import org.apache.axis2.transport.TransportSender;
import org.apache.axis2.util.SessionUtils;
import org.apache.axis2.util.Loader;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.InputStream;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

public class ConfigurationContextFactory {

    protected static final Log log = LogFactory.getLog(ConfigurationContextFactory.class);

    /**
     * Creates a AxisConfiguration depending on the user requirement.
     * First creates an AxisConfigurator object with appropriate parameters.
     * Depending on the implementation getAxisConfiguration(), gets
     * the AxisConfiguration and uses it to create the ConfigurationContext.
     *
     * @param axisConfigurator
     * @return Returns ConfigurationContext.
     * @throws AxisFault
     */
    public static ConfigurationContext createConfigurationContext(
            AxisConfigurator axisConfigurator) throws AxisFault {
        AxisConfiguration axisConfig = axisConfigurator.getAxisConfiguration();
        ConfigurationContext configContext = new ConfigurationContext(axisConfig);
        
        if (axisConfig.getClusterManager() != null) {
        	configContext.initCluster();
        }
        
        if (axisConfigurator instanceof DeploymentEngine) {
            ((DeploymentEngine) axisConfigurator).setConfigContext(configContext);
        }
        //To override context path
        setContextPaths(axisConfig, configContext);
        init(configContext);
        axisConfigurator.engageGlobalModules();
        axisConfigurator.loadServices();
        addModuleService(configContext);
        initApplicationScopeServices(configContext);
        axisConfig.setStart(true);
        return configContext;
    }

    private static void initApplicationScopeServices(ConfigurationContext configCtx)
            throws AxisFault {
        Iterator serviceGroups = configCtx.getAxisConfiguration().getServiceGroups();
        while (serviceGroups.hasNext()) {
            AxisServiceGroup axisServiceGroup = (AxisServiceGroup) serviceGroups.next();
            String maxScope = SessionUtils.calculateMaxScopeForServiceGroup(axisServiceGroup);
            if (Constants.SCOPE_APPLICATION.equals(maxScope)) {
                ServiceGroupContext serviceGroupContext =
                        configCtx.createServiceGroupContext(axisServiceGroup);
                configCtx.addServiceGroupContextintoApplicatoionScopeTable(serviceGroupContext);
                DependencyManager.initService(serviceGroupContext);
            }
        }
    }

    private static void addModuleService(ConfigurationContext configCtx) throws AxisFault {
        AxisConfiguration axisConfig = configCtx.getAxisConfiguration();
        HashMap modules = axisConfig.getModules();
        if (modules != null && modules.size() > 0) {
            Iterator mpduleItr = modules.values().iterator();
            while (mpduleItr.hasNext()) {
                AxisModule axisModule = (AxisModule) mpduleItr.next();
                Utils.deployModuleServices(axisModule, configCtx);
            }
        }
    }

    private static void setContextPaths(AxisConfiguration axisConfig,
                                        ConfigurationContext configContext) {
        // Checking for context path
        Parameter servicePath = axisConfig.getParameter(Constants.PARAM_SERVICE_PATH);
        if (servicePath != null) {
            String spath = ((String) servicePath.getValue()).trim();
            if (spath.length() > 0) {
                configContext.setServicePath(spath);
            }
        } else {
            configContext.setServicePath(Constants.DEFAULT_SERVICES_PATH);
        }

       Parameter contextPath = axisConfig.getParameter(Constants.PARAM_CONTEXT_ROOT);
        if (contextPath != null) {
            String cpath = ((String) contextPath.getValue()).trim();
            if (cpath.length() > 0) {
                configContext.setContextRoot(cpath);
            }
        } else {
            configContext.setContextRoot("axis2");
        }
    }

    /**
     * To get a ConfigurationContext for  given data , and underline implementation
     * is Axis2 default impl which is file system based deployment model to create
     * an AxisConfiguration.
     * <p/>
     * Here either or both parameter can be null. So that boil down to following
     * scenarios and it should note that parameter value should be full path ,
     * you are not allowed to give one relative to other. And these two can be located
     * in completely different locations.
     * <ul>
     * <li>If none of them are null , then AxisConfiguration will be based on the
     * value of axis2xml , and the repository will be the value specified by the
     * path parameter and there will not be any assumptions.</li>
     * <li>If axis2xml is null , then the repository will be the value specfied by
     * path parameter and AxisConfiguration will be created using default_axis2.xml</li>
     * <li>If path parameter is null , then AxisConfiguration will be created using
     * that axis2.xml. And after creating AxisConfiguration system will try to
     * find user has specified repository parameter in axis2.xml
     * (&lt;parameter name="repository"&gt;location of the repo&lt;/parameter&gt;) , if it
     * find that then repository will be the value specified by that parameter.</li>
     * <li>If both are null , then it is simple , AixsConfiguration will be created
     * using default_axis2.xml and thats it.</li>
     * </ul>
     * <p/>
     * Note : rather than passing any parameters you can give them as System
     * properties. Simple you can add following system properties before
     * you call this.
     * <ul>
     * <li>axis2.repo : same as path parameter</li>
     * <li>axis2.xml  : same as axis2xml</li>
     * </ul>
     *
     * @param path     : location of the repository
     * @param axis2xml : location of the axis2.xml (configuration) , you can not give
     *                 axis2xml relative to repository.
     * @return Returns the built ConfigurationContext.
     * @throws AxisFault in case of problems
     */
    public static ConfigurationContext createConfigurationContextFromFileSystem(
            String path,
            String axis2xml) throws AxisFault {
        return createConfigurationContext(new FileSystemConfigurator(path, axis2xml));
    }

    public static ConfigurationContext createConfigurationContextFromFileSystem(String path)
            throws AxisFault {
        return createConfigurationContextFromFileSystem(path, null);
    }

    public static ConfigurationContext createConfigurationContextFromURIs(
            URL axis2xml, URL repositoy) throws AxisFault {
        return createConfigurationContext(new URLBasedAxisConfigurator(axis2xml, repositoy));
    }

    /**
     * Initializes modules and creates Transports.
     *
     * @param configContext ConfigurationContext
     */

    private static void init(ConfigurationContext configContext) {
        initModules(configContext);
        initTransportSenders(configContext);
    }

    /**
     * Initializes the modules. If the module needs to perform some recovery process
     * it can do so in init and this is different from module.engage().
     *
     * @param context
     */
    private static void initModules(ConfigurationContext context) {
        try {
            HashMap modules = context.getAxisConfiguration().getModules();
            Collection col = modules.values();

            for (Iterator iterator = col.iterator(); iterator.hasNext();) {
                AxisModule axismodule = (AxisModule) iterator.next();
                Module module = axismodule.getModule();

                if (module != null) {
                    module.init(context, axismodule);
                }
            }
        } catch (AxisFault e) {
            log.info(e.getMessage());
        }
    }

    /**
     * Initializes TransportSenders and TransportListeners with appropriate configuration information
     *
     * @param configContext
     */
    private static void initTransportSenders(ConfigurationContext configContext) {
        AxisConfiguration axisConf = configContext.getAxisConfiguration();

        // Initialize Transport Outs
        HashMap transportOuts = axisConf.getTransportsOut();

        Iterator values = transportOuts.values().iterator();

        while (values.hasNext()) {
            TransportOutDescription transportOut = (TransportOutDescription) values.next();
            TransportSender sender = transportOut.getSender();

            if (sender != null) {
                try {
                    sender.init(configContext, transportOut);
                } catch (AxisFault axisFault) {
                    log.info(Messages.getMessage("transportiniterror", transportOut.getName()));
                }
            }
        }
    }

    /**
     * creates an empty configuration context.
     *
     * @return Returns ConfigurationContext.
     */
    public static ConfigurationContext createEmptyConfigurationContext() throws AxisFault {
        AxisConfiguration axisConfiguration = new AxisConfiguration();
        ConfigurationContext configContext = new ConfigurationContext(axisConfiguration);
        if (axisConfiguration.getClusterManager() != null) {
        	configContext.initCluster();
        }
        
        setContextPaths(axisConfiguration, configContext);
        return configContext;
    }

    /**
     * Gets the default configuration context by using Axis2.xml in the classpath
     *
     * @return Returns ConfigurationContext.
     */
    public static ConfigurationContext createDefaultConfigurationContext() throws Exception {
        InputStream in = Loader.getResourceAsStream(DeploymentConstants.AXIS2_CONFIGURATION_RESOURCE);

        AxisConfiguration axisConfig = new AxisConfiguration();
        AxisConfigBuilder builder = new AxisConfigBuilder(in, axisConfig, null);
        builder.populateConfig();
        axisConfig.validateSystemPredefinedPhases();
        ConfigurationContext configContext = new ConfigurationContext(axisConfig);
        
        if (axisConfig.getClusterManager() != null) {
        	configContext.initCluster();
        }
        
        setContextPaths(axisConfig, configContext);
        return configContext;
    }
}
