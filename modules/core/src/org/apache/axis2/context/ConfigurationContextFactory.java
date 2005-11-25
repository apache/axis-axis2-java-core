package org.apache.axis2.context;

import org.apache.axis2.AxisFault;
import org.apache.axis2.deployment.DeploymentEngine;
import org.apache.axis2.deployment.DeploymentException;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.ModuleDescription;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.description.TransportInDescription;
import org.apache.axis2.description.TransportOutDescription;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.engine.AxisConfigurationImpl;
import org.apache.axis2.modules.Module;
import org.apache.axis2.phaseresolver.PhaseException;
import org.apache.axis2.phaseresolver.PhaseResolver;
import org.apache.axis2.transport.TransportListener;
import org.apache.axis2.transport.TransportSender;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.namespace.QName;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

public class ConfigurationContextFactory {

    private Log log = LogFactory.getLog(getClass());

    /**
     * Builds the configuration for the Server.
     * @param repositoryName
     * @return Returns the built ConfigurationContext.
     * @throws DeploymentException
     */
    public ConfigurationContext buildConfigurationContext(
            String repositoryName)
            throws DeploymentException {
        ConfigurationContext configurationContext = null;
        try {
            DeploymentEngine deploymentEngine =
                    new DeploymentEngine(repositoryName);
            AxisConfiguration configuration = deploymentEngine.load();
            PhaseResolver phaseResolver = new PhaseResolver(configuration);

            if(configurationContext == null){
                configurationContext = new ConfigurationContext(configuration);
            }
            phaseResolver.buildTranspotsChains();
            initModules(configurationContext);
            initTransports(configurationContext);
        } catch (AxisFault axisFault) {
            throw new DeploymentException(axisFault);
        }
        return configurationContext;
    }

    /**
     * Builds the configuration for the client.
     * @param axis2home the value can be null and resolves to the default axis2.xml file
     * @return Returns ConfigurationContext.
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

            if(engineContext == null){
                engineContext = new ConfigurationContext(configuration);
            }

            phaseResolver.buildTranspotsChains();
            initModules(engineContext);
            initTransports(engineContext);
        } catch (AxisFault axisFault) {
            throw new DeploymentException(axisFault);
        }
        return engineContext;
    }

    /**
     * Initializes the modules. If the module needs to perform some recovery process
     * it can do so in init and this is different from module.engage().
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
     * Resolves the phases and establishes the order of handlers.
     * @param service
     * @param configurationContextVal
     * @param modules
     * @throws PhaseException
     */
    public static void createChains(AxisService service,
                                    AxisConfiguration configurationContextVal,
                                    ArrayList modules)
            throws PhaseException {
        try {
            PhaseResolver resolve =
                    new PhaseResolver(configurationContextVal, service);
            resolve.buildchains();
            for (int i = 0; i < modules.size(); i++) {
                QName qName = (QName) modules.get(i);
                ModuleDescription moduledesc =
                        configurationContextVal.getModule(qName);
                resolve.engageModuleToService(service, moduledesc);
            }
        } catch (PhaseException e) {
            throw e;
        } catch (AxisFault axisFault) {
            throw new PhaseException(axisFault);
        }
    }

    /**
     * Initializes TransportSenders and TransportListeners with appropriate configuration information 
     * 
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
