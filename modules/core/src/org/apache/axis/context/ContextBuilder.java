package org.apache.axis.context;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Collection;
import java.util.Iterator;

import javax.xml.namespace.QName;

import org.apache.axis.deployment.DeploymentEngine;
import org.apache.axis.deployment.DeploymentException;
import org.apache.axis.description.AxisService;
import org.apache.axis.description.AxisModule;
import org.apache.axis.description.AxisOperation;
import org.apache.axis.engine.AxisFault;
import org.apache.axis.engine.EngineConfiguration;
import org.apache.axis.engine.EngineConfigurationImpl;
import org.apache.axis.engine.ExecutionChain;
import org.apache.axis.modules.Module;
import org.apache.axis.phaseresolver.PhaseException;
import org.apache.axis.phaseresolver.PhaseResolver;

/**
 * Created by IntelliJ IDEA.
 * Author : Deepal Jayasinghe
 * Date: Apr 19, 2005
 * Time: 10:44:38 AM
 */
public class ContextBuilder {

    public EngineContext buildEngineContext(String RepositaryName) throws DeploymentException {
        EngineContext engineContext = null;
        try {
            DeploymentEngine deploymentEngine = new DeploymentEngine(RepositaryName);
            EngineConfiguration configuration = deploymentEngine.load();
            PhaseResolver phaseResolver = new PhaseResolver(configuration);
            engineContext = phaseResolver.buildGlobalChains();
            phaseResolver.buildTranspotsChains();
            initModules(engineContext);
        } catch (AxisFault axisFault) {
            throw new DeploymentException(axisFault.getMessage()) ;
        } catch (PhaseException e) {
            throw new DeploymentException(e.getMessage()) ;
        }
        return engineContext;
    }

    public EngineContext buildClientEngineContext(String axis2home) throws DeploymentException {
            EngineContext engineContext = null;
            try {
                DeploymentEngine deploymentEngine = new DeploymentEngine(axis2home);
                EngineConfiguration configuration = deploymentEngine.loadClient();
                PhaseResolver phaseResolver = new PhaseResolver(configuration);
                engineContext = phaseResolver.buildGlobalChains();
                phaseResolver.buildTranspotsChains();
                initModules(engineContext);
            } catch (AxisFault axisFault) {
                throw new DeploymentException(axisFault.getMessage()) ;
            } catch (PhaseException e) {
                throw new DeploymentException(e.getMessage()) ;
            }
            return engineContext;
        }

   /**
    * Is used to initilize the modules , if the module needs to so some recovery process
    * it can do inside init and this is differnt form module.engage()
    * @param context
    * @throws DeploymentException
    */


    private void initModules(EngineContext context) throws DeploymentException {
        try{
            HashMap modules = ((EngineConfigurationImpl)context.getEngineConfig()).getModules();
            Collection col = modules.values();
            for (Iterator iterator = col.iterator(); iterator.hasNext();) {
                AxisModule  axismodule = (AxisModule)iterator.next();
                Module module = axismodule.getModule();
                if(module != null ){
                    module.init(context);
                }
            }
        }catch (AxisFault e){
            throw new DeploymentException(e.getMessage());
        }
    }

    public ServiceContext createServiceContext(AxisService service,EngineContext context) throws PhaseException {
        try {
            ServiceContext serviceContext = new ServiceContext(service,context);
            PhaseResolver reolve = new PhaseResolver(context.getEngineConfig(),serviceContext);
            context.addService(serviceContext);
            serviceContext = reolve.buildchains();
            engageModules(service,context);
            return serviceContext;
        } catch (PhaseException e) {
            throw new PhaseException(e.getMessage()) ;
        } catch (AxisFault axisFault) {
            throw new PhaseException(axisFault.getMessage()) ;
        }
    }

    private void engageModules(AxisService service,EngineContext context) throws AxisFault {
       ArrayList servicemodules = (ArrayList)service.getModules();
       ArrayList opModules ;
       Module module ;
       Collection operations = service.getOperations().values();
       for (Iterator iterator = operations.iterator(); iterator.hasNext();) {
           AxisOperation operation = (AxisOperation) iterator.next();
           opModules = (ArrayList)operation.getModules();
           for (int i = 0; i < servicemodules.size(); i++) {
               QName moduleName = (QName) servicemodules.get(i);
               module = context.getEngineConfig().getModule(moduleName).getModule();
               //todo AxisOperation shoud have a method to get chains
               /*ExecutionChain inchain = new ExecutionChain();
               inchain.addPhases(operation.getPhases(EngineConfiguration.INFLOW));
               module.engage(inchain);*/
           }
           for (int i = 0; i < opModules.size(); i++) {
               QName moduleName = (QName) opModules.get(i);
               module = context.getEngineConfig().getModule(moduleName).getModule();
           }

       }
    }
}
