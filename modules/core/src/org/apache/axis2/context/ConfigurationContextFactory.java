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
     * Build the configuration for the Server
     * @param repositoryName
     * @return
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

            Parameter parameter = configuration.getParameter("seralizeLocation");
            String serializeLocation = ".";
            if (parameter !=null) {
                serializeLocation = ((String)parameter.getValue()).trim();
            }
            File objFile = new File(serializeLocation,"Axis2.obj");
            if(objFile.exists()){
                try {
                    FileInputStream filein = new FileInputStream(objFile);
                    ObjectInputStream in = new ObjectInputStream(filein);
                    Object obj = in.readObject();
                    if(obj instanceof ConfigurationContext){
                        configurationContext = (ConfigurationContext)obj;
                        configurationContext.init(configuration);
                    }
                } catch (IOException e) {
                    log.info(e.getMessage());
                } catch (ClassNotFoundException e) {
                    log.info(e.getMessage());
                }
            }
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
     * Built the Configuration for the Client
     * @param axis2home the value can be null and it is resolved to the default
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

            File objFile = new File("./Axis2.obj");
            if(objFile.exists()){
                try {
                    FileInputStream filein = new FileInputStream(objFile);
                    ObjectInputStream in = new ObjectInputStream(filein);
                    Object obj = in.readObject();
                    if(obj instanceof ConfigurationContext){
                        engineContext = (ConfigurationContext)obj;
                        engineContext.init(configuration);
                    }
                } catch (IOException e) {
                    log.info(e.getMessage());
                } catch (ClassNotFoundException e) {
                    log.info(e.getMessage());
                }
            }
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
    public static void createChains(AxisService service,
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
            throw e;
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
