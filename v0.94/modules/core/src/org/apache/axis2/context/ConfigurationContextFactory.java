package org.apache.axis2.context;

import org.apache.axis2.AxisFault;
import org.apache.axis2.deployment.DeploymentException;
import org.apache.axis2.deployment.FileSystemConfigurator;
import org.apache.axis2.description.ModuleDescription;
import org.apache.axis2.description.TransportInDescription;
import org.apache.axis2.description.TransportOutDescription;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.engine.AxisConfigurator;
import org.apache.axis2.modules.Module;
import org.apache.axis2.phaseresolver.PhaseException;
import org.apache.axis2.phaseresolver.PhaseResolver;
import org.apache.axis2.transport.TransportListener;
import org.apache.axis2.transport.TransportSender;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

public class ConfigurationContextFactory {
    
    private Log log = LogFactory.getLog(getClass());

    /**
     * Creates a AxisConfiguration depending on the user requirment. 
     * First creates an AxisConfigurator object with appropriate parameters.
     * Depending on the implementation getAxisConfiguration(), gets
     * the AxisConfiguration and uses it to create the ConfigurationContext.
     *
     * @param axisConfigurator
     * @return Returns ConfigurationContext.
     * @throws AxisFault
     */
    public ConfigurationContext createConfigurationContext(
            AxisConfigurator axisConfigurator) throws AxisFault {
        AxisConfiguration axisConfig = axisConfigurator.getAxisConfiguration();
        ConfigurationContext configContext = new ConfigurationContext(axisConfig);
        init(configContext);
        return configContext;
    }

    /**
     * Builds the configuration. 
     *
     * @param path
     * @return Returns the built ConfigurationContext.
     * @throws DeploymentException
     */
    public ConfigurationContext createConfigurationContextFromFileSystem(String path) throws AxisFault {
        return createConfigurationContext(new FileSystemConfigurator(path, true));
    }

    /**
     * Initializes modules and creates Transports.
     */
    
    private void init(ConfigurationContext configContext) throws AxisFault {
        try {
            PhaseResolver phaseResolver = new PhaseResolver(configContext.getAxisConfiguration());

            phaseResolver.buildTranspotsChains();
            initModules(configContext);
            initTransports(configContext);
        } catch (PhaseException e) {
            throw new AxisFault(e);
        } catch (DeploymentException e) {
            throw new AxisFault(e);
        }
    }

    /**
     * Initializes the modules. If the module needs to perform some recovery process
     * it can do so in init and this is different from module.engage().
     *
     * @param context
     * @throws DeploymentException
     */
    private void initModules(ConfigurationContext context) throws DeploymentException {
        try {
            HashMap modules = context.getAxisConfiguration().getModules();
            Collection col = modules.values();

            for (Iterator iterator = col.iterator(); iterator.hasNext();) {
                ModuleDescription axismodule = (ModuleDescription) iterator.next();
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
     * Initializes TransportSenders and TransportListeners with appropriate configuration information
     *
     * @param configContext
     */
    public void initTransports(ConfigurationContext configContext) {
        AxisConfiguration axisConf = configContext.getAxisConfiguration();

        // Initialize Transport Ins
        HashMap transportIns = axisConf.getTransportsIn();
        Iterator values = transportIns.values().iterator();

        while (values.hasNext()) {
            TransportInDescription transportIn = (TransportInDescription) values.next();
            TransportListener listener = transportIn.getReceiver();

            if (listener != null) {
                try {
                    listener.init(configContext, transportIn);
                } catch (AxisFault axisFault) {
                    log.info("Transport-IN initialization error : "
                            + transportIn.getName().getLocalPart());
                }
            }
        }

        // Initialize Transport Outs
        HashMap transportOuts = axisConf.getTransportsOut();

        values = transportOuts.values().iterator();

        while (values.hasNext()) {
            TransportOutDescription transportOut = (TransportOutDescription) values.next();
            TransportSender sender = transportOut.getSender();

            if (sender != null) {
                try {
                    sender.init(configContext, transportOut);
                } catch (AxisFault axisFault) {
                    log.info("Transport-OUT initialization error : "
                            + transportOut.getName().getLocalPart());
                }
            }
        }
    }

    /**
     * Gets the default configuration context by using the file system based AxisConfiguration.
     *
     * @return Returns ConfigurationContext.
     */
    public ConfigurationContext createEmptyConfigurationContext() {
        return new ConfigurationContext(new AxisConfiguration());
    }
}
