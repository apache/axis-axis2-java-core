package org.apache.axis.context;

import java.util.ArrayList;

import javax.xml.namespace.QName;

import org.apache.axis.deployment.DeploymentEngine;
import org.apache.axis.deployment.DeploymentException;
import org.apache.axis.description.AxisService;
import org.apache.axis.engine.AxisFault;
import org.apache.axis.engine.EngineConfiguration;
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




    private void initModules(EngineContext context) throws DeploymentException {
        try{
            ArrayList modules = (ArrayList)context.getEngineConfig().getGlobal().getModules();
            for (int i = 0; i < modules.size(); i++) {
                QName name = (QName) modules.get(i);
                Module module = context.getEngineConfig().getModule(name).getModule();
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
            return  serviceContext = reolve.buildchains();
        } catch (PhaseException e) {
            throw new PhaseException(e.getMessage()) ;
        } catch (AxisFault axisFault) {
            throw new PhaseException(axisFault.getMessage()) ;
        }
    }
}
