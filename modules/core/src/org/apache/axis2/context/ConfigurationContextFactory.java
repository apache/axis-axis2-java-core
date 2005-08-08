package org.apache.axis2.context;

import org.apache.axis2.deployment.DeploymentEngine;
import org.apache.axis2.deployment.DeploymentException;
import org.apache.axis2.description.ModuleDescription;
import org.apache.axis2.description.ServiceDescription;
import org.apache.axis2.description.TransportInDescription;
import org.apache.axis2.description.TransportOutDescription;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.engine.AxisConfigurationImpl;
import org.apache.axis2.modules.Module;
import org.apache.axis2.phaseresolver.PhaseException;
import org.apache.axis2.phaseresolver.PhaseResolver;
import org.apache.axis2.transport.TransportListener;
import org.apache.axis2.transport.TransportSender;
import org.apache.axis2.AxisFault;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by IntelliJ IDEA.
 * Author : Deepal Jayasinghe
 * Date: Apr 19, 2005
 * Time: 10:44:38 AM
 */
public class ConfigurationContextFactory {

    /**
     * Build the configuration for the Server
     * @param RepositaryName
     * @return
     * @throws DeploymentException
     */
    public ConfigurationContext buildConfigurationContext(
            String RepositaryName)
            throws DeploymentException {
        ConfigurationContext configurationContext = null;
        try {
            DeploymentEngine deploymentEngine =
                    new DeploymentEngine(RepositaryName);
            AxisConfiguration configuration = deploymentEngine.load();
            PhaseResolver phaseResolver = new PhaseResolver(configuration);
            configurationContext = new ConfigurationContext(configuration);
            phaseResolver.buildTranspotsChains();
            initModules(configurationContext);
            initTransports(configurationContext);
        } catch (AxisFault axisFault) {
            throw new DeploymentException(axisFault);
        }
        return configurationContext;
    }
    /**
     * Built the Configuration for the Client
     * @param axis2home, the value can be null and it is resolved to the default
     * axis2.xml file
     * @return
     * @throws DeploymentException
     */

    public ConfigurationContext buildClientConfigurationContext(
            String axis2home)
            throws DeploymentException {
        ConfigurationContext engineContext = null;
        try {
            AxisConfiguration configuration =
                    new DeploymentEngine().loadClient(axis2home);
            PhaseResolver phaseResolver = new PhaseResolver(configuration);
            engineContext = new ConfigurationContext(configuration);
            phaseResolver.buildTranspotsChains();
            initModules(engineContext);
            initTransports(engineContext);
        } catch (AxisFault axisFault) {
            throw new DeploymentException(axisFault);
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

    private void initModules(ConfigurationContext context)
            throws DeploymentException {
        try {
            HashMap modules =
                    ((AxisConfigurationImpl) context.getAxisConfiguration())
                    .getModules();
            Collection col = modules.values();
            for (Iterator iterator = col.iterator(); iterator.hasNext();) {
                ModuleDescription axismodule =
                        (ModuleDescription) iterator.next();
                Module module = axismodule.getModule();
                if (module != null) {
                    module.init(context.getAxisConfiguration());
                }
            }
        } catch (AxisFault e) {
            throw new DeploymentException(e);
        }
    }

    /**
     * Here the Phases are resolved and the Order of the Handlers are established
     * @param service
     * @param configurationContextVal
     * @param modules
     * @throws PhaseException
     */
    public static void createChains(ServiceDescription service,
                                    AxisConfiguration configurationContextVal,
                                    ArrayList modules)
            throws PhaseException {
        try {
            PhaseResolver reolve =
                    new PhaseResolver(configurationContextVal, service);
            reolve.buildchains();
            for (int i = 0; i < modules.size(); i++) {
                QName qName = (QName) modules.get(i);
                ModuleDescription moduledecs =
                        configurationContextVal.getModule(qName);
                reolve.engageModuleToService(service, moduledecs);
            }
        } catch (PhaseException e) {
            throw new PhaseException(e.getMessage());
        } catch (AxisFault axisFault) {
            throw new PhaseException(axisFault);
        }
    }
    
    /**
     * This method initilize the transports, passing the information taken from the
     * deployment to the real instance, for and example here the <code>TransportSender</code>
     * get a referance to the <code>TransportOutDescription</code>.
     * @param configContext
     * @throws AxisFault
     */
    public void initTransports(ConfigurationContext configContext)
            throws AxisFault {
        AxisConfiguration axisConf = configContext.getAxisConfiguration();
        
        //Initzialize Transport Ins
        HashMap transportIns = axisConf.getTransportsIn();
        Iterator values = transportIns.values().iterator();
        while (values.hasNext()) {
            TransportInDescription transportIn =
                    (TransportInDescription) values.next();
            TransportListener listener = transportIn.getReceiver();
            if (listener != null) {
                listener.init(configContext, transportIn);
            }
        }
        //Initzialize Transport Outs
        HashMap transportOuts = axisConf.getTransportsOut();
        values = transportOuts.values().iterator();
        while (values.hasNext()) {
            TransportOutDescription transportOut =
                    (TransportOutDescription) values.next();
            TransportSender sender = transportOut.getSender();
            if (sender != null) {
                sender.init(configContext, transportOut);
            }
        }

    }

}
