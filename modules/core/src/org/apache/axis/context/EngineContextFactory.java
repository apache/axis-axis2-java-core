package org.apache.axis.context;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import javax.xml.namespace.QName;

import org.apache.axis.deployment.DeploymentEngine;
import org.apache.axis.deployment.DeploymentException;
import org.apache.axis.description.ModuleDescription;
import org.apache.axis.description.OperationDescription;
import org.apache.axis.description.ServiceDescription;
import org.apache.axis.engine.AxisFault;
import org.apache.axis.engine.AxisConfiguration;
import org.apache.axis.engine.AxisSystemImpl;
import org.apache.axis.modules.Module;
import org.apache.axis.phaseresolver.PhaseException;
import org.apache.axis.phaseresolver.PhaseResolver;

/**
 * Created by IntelliJ IDEA.
 * Author : Deepal Jayasinghe
 * Date: Apr 19, 2005
 * Time: 10:44:38 AM
 */
public class EngineContextFactory {

    public ConfigurationContext buildEngineContext(String RepositaryName) throws DeploymentException {
        ConfigurationContext engineContext = null;
        try {
            DeploymentEngine deploymentEngine = new DeploymentEngine(RepositaryName);
            AxisConfiguration configuration = deploymentEngine.load();
            PhaseResolver phaseResolver = new PhaseResolver(configuration);
            engineContext = phaseResolver.buildGlobalChains();
            phaseResolver.buildTranspotsChains();
            initModules(engineContext);
        } catch (AxisFault axisFault) {
            throw new DeploymentException(axisFault.getMessage());
        } catch (PhaseException e) {
            throw new DeploymentException(e.getMessage());
        }
        return engineContext;
    }

    public ConfigurationContext buildClientEngineContext(String axis2home) throws DeploymentException {
        ConfigurationContext engineContext = null;
        try {
            DeploymentEngine deploymentEngine = new DeploymentEngine(axis2home);
            AxisConfiguration configuration = deploymentEngine.loadClient();
            PhaseResolver phaseResolver = new PhaseResolver(configuration);
            engineContext = phaseResolver.buildGlobalChains();
            phaseResolver.buildTranspotsChains();
            initModules(engineContext);
        } catch (AxisFault axisFault) {
            throw new DeploymentException(axisFault.getMessage());
        } catch (PhaseException e) {
            throw new DeploymentException(e.getMessage());
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
            HashMap modules = ((AxisSystemImpl) context.getEngineConfig()).getModules();
            Collection col = modules.values();
            for (Iterator iterator = col.iterator(); iterator.hasNext();) {
                ModuleDescription axismodule = (ModuleDescription) iterator.next();
                Module module = axismodule.getModule();
                if (module != null) {
                    module.init(context.getEngineConfig());
                }
            }
        } catch (AxisFault e) {
            throw new DeploymentException(e.getMessage());
        }
    }

    public void createChains(ServiceDescription service, AxisConfiguration system) throws PhaseException {
        try {
            PhaseResolver reolve = new PhaseResolver(system, service);
            reolve.buildchains();
            engageModules(service, system);
        } catch (PhaseException e) {
            throw new PhaseException(e.getMessage());
        } catch (AxisFault axisFault) {
            throw new PhaseException(axisFault.getMessage());
        }
    }

    private void engageModules(ServiceDescription service, AxisConfiguration context) throws AxisFault {
        ArrayList servicemodules = (ArrayList) service.getModules();
        ArrayList opModules;
        Module module;
        Collection operations = service.getOperations().values();
        for (Iterator iterator = operations.iterator(); iterator.hasNext();) {
            OperationDescription operation = (OperationDescription) iterator.next();
            opModules = (ArrayList) operation.getModules();
            for (int i = 0; i < servicemodules.size(); i++) {
                QName moduleName = (QName) servicemodules.get(i);
                module = context.getModule(moduleName).getModule();
                //todo OperationDescription shoud have a method to get chains
                /*ExecutionChain inchain = new ExecutionChain();
                inchain.addPhases(operation.getPhases(EngineConfiguration.INFLOW));
                module.engage(inchain);*/
            }
            for (int i = 0; i < opModules.size(); i++) {
                QName moduleName = (QName) opModules.get(i);
                module = context.getModule(moduleName).getModule();
            }

        }
    }
}
