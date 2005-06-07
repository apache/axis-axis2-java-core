package org.apache.axis.context;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import javax.xml.namespace.QName;

import org.apache.axis.deployment.DeploymentEngine;
import org.apache.axis.deployment.DeploymentException;
import org.apache.axis.description.ModuleDescription;
import org.apache.axis.description.ServiceDescription;
import org.apache.axis.engine.AxisConfiguration;
import org.apache.axis.engine.AxisConfigurationImpl;
import org.apache.axis.engine.AxisFault;
import org.apache.axis.modules.Module;
import org.apache.axis.phaseresolver.PhaseException;
import org.apache.axis.phaseresolver.PhaseResolver;

/**
 * Created by IntelliJ IDEA.
 * Author : Deepal Jayasinghe
 * Date: Apr 19, 2005
 * Time: 10:44:38 AM
 */
public class ConfigurationContextFactory {

    public ConfigurationContext buildEngineContext(String RepositaryName) throws DeploymentException {
        ConfigurationContext configurationContext = null;
        try {
            DeploymentEngine deploymentEngine = new DeploymentEngine(RepositaryName);
            AxisConfiguration configuration = deploymentEngine.load();
            PhaseResolver phaseResolver = new PhaseResolver(configuration);
            configurationContext = new ConfigurationContext(configuration) ;
            phaseResolver.buildTranspotsChains();
            initModules(configurationContext);
        } catch (AxisFault axisFault) {
            throw new DeploymentException(axisFault.getMessage());
        }
        return configurationContext;
    }

    public ConfigurationContext buildClientEngineContext(String axis2home) throws DeploymentException {
        ConfigurationContext engineContext = null;
        try {
            AxisConfiguration configuration = new DeploymentEngine().loadClient(axis2home);
            PhaseResolver phaseResolver = new PhaseResolver(configuration);
            engineContext = new ConfigurationContext(configuration) ;
            phaseResolver.buildTranspotsChains();
            initModules(engineContext);
        } catch (AxisFault axisFault) {
            throw new DeploymentException(axisFault.getMessage());
        }
        return engineContext;
    }

    /**
     * Is used to initilize the modules , if the module needs to so some recovery process
     * it can do inside init and this is differnt form module.engage()
     *
     * @param context
     * @throws DeploymentException
     */

   private void initModules(ConfigurationContext context) throws DeploymentException {
        try {
            HashMap modules = ((AxisConfigurationImpl) context.getAxisConfiguration()).getModules();
            Collection col = modules.values();
            for (Iterator iterator = col.iterator(); iterator.hasNext();) {
                ModuleDescription axismodule = (ModuleDescription) iterator.next();
                Module module = axismodule.getModule();
                if (module != null) {
                    module.init(context.getAxisConfiguration());
                }
            }
        } catch (AxisFault e) {
            throw new DeploymentException(e.getMessage());
        }
    }

    public static void createChains(ServiceDescription service, AxisConfiguration configurationContextVal , ArrayList modules) throws PhaseException {
        try {
            PhaseResolver reolve = new PhaseResolver(configurationContextVal, service);
            reolve.buildchains();
            for (int i = 0; i < modules.size(); i++) {
                QName qName = (QName) modules.get(i);
                ModuleDescription moduledecs = configurationContextVal.getModule(qName);
                reolve.engageModuleToService(service,moduledecs);
            }
        } catch (PhaseException e) {
            throw new PhaseException(e.getMessage());
        } catch (AxisFault axisFault) {
            throw new PhaseException(axisFault.getMessage());
        }
    }
}
