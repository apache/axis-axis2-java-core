package org.apache.axis.context;

import org.apache.axis.deployment.DeploymentEngine;
import org.apache.axis.deployment.DeploymentException;
import org.apache.axis.engine.EngineConfiguration;
import org.apache.axis.engine.AxisFault;
import org.apache.axis.phaseresolver.PhaseResolver;
import org.apache.axis.phaseresolver.PhaseException;
import org.apache.axis.description.AxisService;

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
        } catch (AxisFault axisFault) {
            throw new DeploymentException(axisFault.getMessage()) ;
        } catch (PhaseException e) {
            throw new DeploymentException(e.getMessage()) ;
        }
        return engineContext;
    }

    public ServiceContext refresh(AxisService service,EngineContext context) throws PhaseException {
        try {
            ServiceContext serviceContext = new ServiceContext(service);
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
