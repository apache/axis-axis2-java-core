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


package org.apache.axis2.deployment;

import org.apache.axis2.AxisFault;
import org.apache.axis2.deployment.listener.RepositoryListener;
import org.apache.axis2.deployment.listener.RepositoryListenerImpl;
import org.apache.axis2.deployment.repository.util.ArchiveFileData;
import org.apache.axis2.deployment.repository.util.ArchiveReader;
import org.apache.axis2.deployment.repository.util.WSInfo;
import org.apache.axis2.deployment.scheduler.DeploymentIterator;
import org.apache.axis2.deployment.scheduler.Scheduler;
import org.apache.axis2.deployment.scheduler.SchedulerTask;
import org.apache.axis2.deployment.util.PhasesInfo;
import org.apache.axis2.deployment.util.Utils;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.AxisServiceGroup;
import org.apache.axis2.description.Flow;
import org.apache.axis2.description.ModuleDescription;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.engine.Phase;
import org.apache.axis2.i18n.Messages;
import org.apache.axis2.modules.Module;
import org.apache.axis2.phaseresolver.PhaseMetadata;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class DeploymentEngine implements DeploymentConstants {

    private Log log = LogFactory.getLog(getClass());
    private boolean hotUpdate = true;    // to do hot update or not
    private boolean hotDeployment = true;    // to do hot deployment or not
    public String axis2repository = null;

    /**
     * Stores all the web Services to deploy.
     */
    private List wsToDeploy = new ArrayList();

    /**
     * Stores all the web Services to undeploy.
     */
    private List wsToUnDeploy = new ArrayList();
    private PhasesInfo phasesinfo = new PhasesInfo();    // to store phases list in axis2.xml

    /**
     * Stores the module specified in the server.xml at the document parsing time.
     */
    private ArrayList modulelist = new ArrayList();

    /**
     * to keep a ref to engine register
     * this ref will pass to engine when it call start()
     * method
     */
    private AxisConfiguration axisConfig;
    private ArchiveFileData currentArchiveFile;
    private String engineConfigName;


    /**
     * Default constructor is needed to deploy module and service programatically.
     */
    public DeploymentEngine() {
    }

    /**
     * This constructor is used by Engine to start the deployment module.
     *
     * @param repositoryName is the path to which Repository Listener should
     *                       listen to.
     */
    public DeploymentEngine(String repositoryName) throws DeploymentException {
        this(repositoryName, AXIS2_CONFIGURATION_XML);
    }

    public DeploymentEngine(String repositoryName, String xmlFile)
            throws DeploymentException {
        if ((repositoryName == null) || "".equals(repositoryName.trim())) {
            throw new DeploymentException(
                    Messages.getMessage(DeploymentErrorMsgs.REPOSITORY_CANNOT_BE_NULL));
        }
        axis2repository = repositoryName;
        prepareRepository(repositoryName, xmlFile);
        this.engineConfigName = repositoryName + '/' + xmlFile;
    }

    /**
     * Adds module references to the list while parsing the axis2.xml file.
     * time none of module availble (they load after parsing the document)
     *
     * @param moduleName <code>QName</code>
     */
    public void addModule(QName moduleName) {
        modulelist.add(moduleName);
    }

    private void addNewModule(ModuleDescription modulemetadata) throws AxisFault {

        Flow inflow = modulemetadata.getInFlow();
        ClassLoader moduleClassLoader = modulemetadata.getModuleClassLoader();

        if (inflow != null) {
            Utils.addFlowHandlers(inflow, moduleClassLoader);
        }

        Flow outFlow = modulemetadata.getOutFlow();

        if (outFlow != null) {
            Utils.addFlowHandlers(outFlow, moduleClassLoader);
        }

        Flow faultInFlow = modulemetadata.getFaultInFlow();

        if (faultInFlow != null) {
            Utils.addFlowHandlers(faultInFlow, moduleClassLoader);
        }

        Flow faultOutFlow = modulemetadata.getFaultOutFlow();

        if (faultOutFlow != null) {
            Utils.addFlowHandlers(faultOutFlow, moduleClassLoader);
        }

        axisConfig.addModule(modulemetadata);
        log.debug(Messages.getMessage(DeploymentErrorMsgs.ADDING_NEW_MODULE));
    }

    private void addServiceGroup(AxisServiceGroup serviceGroup, ArrayList serviceList)
            throws AxisFault {
        serviceGroup.setParent(axisConfig);

        // engaging globally engage module to this service group
        Iterator itr_global_modules = axisConfig.getEngagedModules().iterator();

        while (itr_global_modules.hasNext()) {
            QName qName = (QName) itr_global_modules.next();

            serviceGroup.engageModule(axisConfig.getModule(qName));
        }

        // module from services.xml at serviceGroup level
        ArrayList groupModules = serviceGroup.getModuleRefs();

        for (int i = 0; i < groupModules.size(); i++) {
            QName moduleName = (QName) groupModules.get(i);
            ModuleDescription module = axisConfig.getModule(moduleName);

            if (module != null) {
                serviceGroup.engageModule(axisConfig.getModule(moduleName));
            } else {
                throw new DeploymentException(
                        Messages.getMessage(
                                DeploymentErrorMsgs.BAD_MODULE_FROM_SERVICE,
                                serviceGroup.getServiceGroupName(), moduleName.getLocalPart()));
            }
        }

        Iterator services = serviceList.iterator();

        while (services.hasNext()) {
            AxisService axisService = (AxisService) services.next();
            axisService.setUseDefaultChains(false);

            axisService.setFileName(currentArchiveFile.getFile().getAbsolutePath());
            serviceGroup.addService(axisService);

            // modules from <service>
            ArrayList list = axisService.getModules();

            for (int i = 0; i < list.size(); i++) {
                ModuleDescription module = axisConfig.getModule((QName) list.get(i));

                if (module == null) {
                    throw new DeploymentException(
                            Messages.getMessage(
                                    DeploymentErrorMsgs.BAD_MODULE_FROM_SERVICE, axisService.getName(),
                                    ((QName) list.get(i)).getLocalPart()));
                }

                axisService.engageModule(module, axisConfig);
            }

            HashMap operations = axisService.getOperations();
            Collection opCol = operations.values();

            for (Iterator iterator = opCol.iterator(); iterator.hasNext();) {
                AxisOperation opDesc = (AxisOperation) iterator.next();
                ArrayList modules = opDesc.getModuleRefs();

                for (int i = 0; i < modules.size(); i++) {
                    QName moduleName = (QName) modules.get(i);
                    ModuleDescription module = axisConfig.getModule(moduleName);

                    if (module != null) {
                        opDesc.engageModule(module, axisConfig);
                    } else {
                        throw new DeploymentException(
                                Messages.getMessage(
                                        DeploymentErrorMsgs.BAD_MODULE_FROM_OPERATION,
                                        opDesc.getName().getLocalPart(), moduleName.getLocalPart()));
                    }
                }
            }
        }

        axisConfig.addServiceGroup(serviceGroup);
    }

    /**
     * @param file
     */
    public void addWSToDeploy(ArchiveFileData file) {
        wsToDeploy.add(file);
    }

    /**
     * @param file
     */
    public void addWSToUndeploy(WSInfo file) {
        wsToUnDeploy.add(file);
    }

    /**
     * Builds ModuleDescription for a given module archive file.
     *
     * @param modulearchive
     * @return Returns ModuleDescription.
     * @throws DeploymentException
     */
    public ModuleDescription buildModule(File modulearchive, AxisConfiguration config)
            throws DeploymentException {
        ModuleDescription axismodule;

        try {
            this.setPhasesinfo(config.getPhasesInfo());
            currentArchiveFile = new ArchiveFileData(modulearchive, TYPE_MODULE);
            axismodule = new ModuleDescription();
            axismodule.setModuleClassLoader(currentArchiveFile.getClassLoader());

            ArchiveReader archiveReader = new ArchiveReader();
            ClassLoader moduleClassLoader = config.getModuleClassLoader();

            currentArchiveFile.setClassLoader(false, moduleClassLoader);
            archiveReader.readModuleArchive(currentArchiveFile.getAbsolutePath(), this, axismodule,
                    false, axisConfig);

            Flow inflow = axismodule.getInFlow();

            if (inflow != null) {
                Utils.addFlowHandlers(inflow, moduleClassLoader);
            }

            Flow outFlow = axismodule.getOutFlow();

            if (outFlow != null) {
                Utils.addFlowHandlers(outFlow, moduleClassLoader);
            }

            Flow faultInFlow = axismodule.getFaultInFlow();

            if (faultInFlow != null) {
                Utils.addFlowHandlers(faultInFlow, moduleClassLoader);
            }

            Flow faultOutFlow = axismodule.getFaultOutFlow();

            if (faultOutFlow != null) {
                Utils.addFlowHandlers(faultOutFlow, moduleClassLoader);
            }

            //initilization Module
            Module module = axismodule.getModule();

            if (module != null) {
                module.init(axisConfig);
            }

        } catch (AxisFault axisFault) {
            throw new DeploymentException(axisFault);
        }

        currentArchiveFile = null;

        return axismodule;
    }

    /**
     * Fills an axisservice object using services.xml. First creates
     * an axisservice object using WSDL and then fills it using the given services.xml.
     * Loads all the required class and builds the chains, finally adds the
     * servicecontext to EngineContext and axisservice into EngineConfiguration.
     *
     * @param axisService
     * @param serviceInputStream
     * @param classLoader
     * @return Returns AxisService.
     * @throws DeploymentException
     */
    public AxisService buildService(AxisService axisService, InputStream serviceInputStream,
                                    ClassLoader classLoader, AxisConfiguration axisConfig)
            throws DeploymentException {
        try {
            currentArchiveFile = new ArchiveFileData(TYPE_SERVICE, "");
            currentArchiveFile.setClassLoader(classLoader);

            ServiceBuilder builder = new ServiceBuilder(serviceInputStream, axisConfig,
                    axisService);

            builder.populateService(builder.buildOM());
        } catch (AxisFault axisFault) {
            throw new DeploymentException(axisFault);
        } catch (XMLStreamException e) {
            throw new DeploymentException(e);
        }

        return axisService;
    }

    private void checkClientHome(String repositoryName) throws DeploymentException {
        prepareRepository(repositoryName, AXIS2_CONFIGURATION_XML);
        this.engineConfigName = repositoryName + '/' + AXIS2_CONFIGURATION_XML;
    }

    private AxisConfiguration createEngineConfig() {
        return new AxisConfiguration();
    }

    public void doDeploy() {
        if (wsToDeploy.size() > 0) {
            for (int i = 0; i < wsToDeploy.size(); i++) {
                currentArchiveFile = (ArchiveFileData) wsToDeploy.get(i);

                boolean explodedDir = currentArchiveFile.getFile().isDirectory();
                int type = currentArchiveFile.getType();

                try {
                    ArchiveReader archiveReader;
                    StringWriter errorWriter = new StringWriter();

                    switch (type) {
                        case TYPE_SERVICE :
                            currentArchiveFile.setClassLoader(explodedDir,
                                    axisConfig.getServiceClassLoader());
                            archiveReader = new ArchiveReader();

                            String serviceStatus = "";

                            try {
                                HashMap wsdlservice = archiveReader.processWSDLs(currentArchiveFile,
                                        this);

                                AxisServiceGroup sericeGroup = new AxisServiceGroup(axisConfig);

                                sericeGroup.setServiceGroupClassLoader(
                                        currentArchiveFile.getClassLoader());

                                ArrayList serviceList = archiveReader.processServiceGroup(
                                        currentArchiveFile.getAbsolutePath(), this,
                                        sericeGroup, explodedDir, wsdlservice,
                                        axisConfig);

                                addServiceGroup(sericeGroup, serviceList);
                                log.debug(Messages.getMessage(DeploymentErrorMsgs.DEPLOYING_WS,
                                        currentArchiveFile.getName()));
                            } catch (DeploymentException de) {
                                log.info(Messages.getMessage(DeploymentErrorMsgs.INVALID_SERVICE,
                                        currentArchiveFile.getName(),
                                        de.getMessage()));

                                PrintWriter error_ptintWriter = new PrintWriter(errorWriter);

                                de.printStackTrace(error_ptintWriter);
                                serviceStatus = "Error:\n" + errorWriter.toString();
                            } catch (AxisFault axisFault) {
                                log.info(Messages.getMessage(DeploymentErrorMsgs.INVALID_SERVICE,
                                        currentArchiveFile.getName(),
                                        axisFault.getMessage()));

                                PrintWriter error_ptintWriter = new PrintWriter(errorWriter);

                                axisFault.printStackTrace(error_ptintWriter);
                                serviceStatus = "Error:\n" + errorWriter.toString();
                            } catch (Exception e) {
                                log.info(Messages.getMessage(DeploymentErrorMsgs.INVALID_SERVICE,
                                        currentArchiveFile.getName(),
                                        e.getMessage()));

                                PrintWriter error_ptintWriter = new PrintWriter(errorWriter);

                                e.printStackTrace(error_ptintWriter);
                                serviceStatus = "Error:\n" + errorWriter.toString();
                            } finally {
                                if (serviceStatus.startsWith("Error:")) {
                                    axisConfig.getFaultyServices().put(
                                            getAxisServiceName(currentArchiveFile.getName()),
                                            serviceStatus);
                                }

                                currentArchiveFile = null;
                            }

                            break;

                        case TYPE_MODULE :
                            currentArchiveFile.setClassLoader(explodedDir,
                                    axisConfig.getModuleClassLoader());
                            archiveReader = new ArchiveReader();

                            String moduleStatus = "";

                            try {
                                ModuleDescription metaData = new ModuleDescription();

                                metaData.setModuleClassLoader(currentArchiveFile.getClassLoader());
                                metaData.setParent(axisConfig);
                                archiveReader.readModuleArchive(currentArchiveFile.getAbsolutePath(),
                                        this, metaData, explodedDir,
                                        axisConfig);
                                addNewModule(metaData);
                                log.info(Messages.getMessage(DeploymentErrorMsgs.DEPLOYING_MODULE,
                                        metaData.getName().getLocalPart()));
                            } catch (DeploymentException e) {
                                log.info(Messages.getMessage(DeploymentErrorMsgs.INVALID_MODULE,
                                        currentArchiveFile.getName(),
                                        e.getMessage()));

                                PrintWriter error_ptintWriter = new PrintWriter(errorWriter);

                                e.printStackTrace(error_ptintWriter);
                                moduleStatus = "Error:\n" + errorWriter.toString();
                            } catch (AxisFault axisFault) {
                                log.info(Messages.getMessage(DeploymentErrorMsgs.INVALID_MODULE,
                                        currentArchiveFile.getName(),
                                        axisFault.getMessage()));

                                PrintWriter error_ptintWriter = new PrintWriter(errorWriter);

                                axisFault.printStackTrace(error_ptintWriter);
                                moduleStatus = "Error:\n" + errorWriter.toString();
                            } finally {
                                if (moduleStatus.startsWith("Error:")) {
                                    axisConfig.getFaultyModules().put(
                                            getAxisServiceName(currentArchiveFile.getName()), moduleStatus);
                                }

                                currentArchiveFile = null;
                            }

                            break;
                    }
                } catch (AxisFault axisFault) {
                    log.info(Messages.getMessage(DeploymentErrorMsgs.ERROR_SETTING_CLIENT_HOME,
                            axisFault.getMessage()));
                }
            }
        }

        wsToDeploy.clear();
    }

    /**
     * Checks if the modules, referred by server.xml, exist or that they are deployed.
     */
    private void engageModules() throws AxisFault {
        for (Iterator iterator = modulelist.iterator(); iterator.hasNext();) {
            QName name = (QName) iterator.next();

            axisConfig.engageModule(name);
        }
    }

    public AxisConfiguration load() throws DeploymentException {
        if (engineConfigName == null) {
            throw new DeploymentException(
                    Messages.getMessage(DeploymentErrorMsgs.PATH_TO_CONFIG_CANNOT_BE_NULL));
        }

        File tempfile = new File(engineConfigName);
        RepositoryListenerImpl repoListener;

        try {
            InputStream in = new FileInputStream(tempfile);

            axisConfig = createEngineConfig();

            AxisConfigBuilder builder = new AxisConfigBuilder(in, this, axisConfig);

            builder.populateConfig();
            axisConfig.setPhasesinfo(phasesinfo);

            // setting the CLs
            setClassLoaders(axis2repository);
        } catch (FileNotFoundException e) {
            throw new DeploymentException(e);
        }

        setDeploymentFeatures();
        repoListener = new RepositoryListenerImpl(axis2repository, this);

        try {
            axisConfig.setRepository(axis2repository);
            validateSystemPredefinedPhases();
            engageModules();
        } catch (AxisFault axisFault) {
            log.info(Messages.getMessage(DeploymentErrorMsgs.MODULE_VALIDATION_FAILED,
                    axisFault.getMessage()));

            throw new DeploymentException(axisFault);
        }

        repoListener.checkServices();

        if (hotDeployment) {
            startSearch(repoListener);
        }

        return axisConfig;
    }

    public AxisConfiguration loadClient(String clientHome) throws DeploymentException {
        InputStream in;

        axis2repository = clientHome;

        boolean isRepositoryExist = false;
        RepositoryListenerImpl repoListener = null;

        if (!((clientHome == null) || "".equals(clientHome.trim()))) {
            checkClientHome(clientHome);
            isRepositoryExist = true;

            try {
                File tempfile = new File(engineConfigName);

                in = new FileInputStream(tempfile);
            } catch (FileNotFoundException e) {
                throw new DeploymentException(e);
            }
        } else {
            ClassLoader cl = Thread.currentThread().getContextClassLoader();

            in = cl.getResourceAsStream(AXIS2_CONFIGURATION_RESOURCE);

            if (in == null) {
                throw new DeploymentException(
                        Messages.getMessage(DeploymentErrorMsgs.CONFIG_NOT_FOUND));
            }
        }

        axisConfig = createEngineConfig();

        AxisConfigBuilder builder = new AxisConfigBuilder(in, this, axisConfig);

        axisConfig.setPhasesinfo(phasesinfo);
        builder.populateConfig();

        if (isRepositoryExist) {
            hotDeployment = false;
            hotUpdate = false;

            // setting CLs
            setClassLoaders(axis2repository);
            repoListener = new RepositoryListenerImpl(axis2repository, this);
        }

        try {
            axisConfig.setRepository(axis2repository);
            validateSystemPredefinedPhases();
            engageModules();
        } catch (AxisFault axisFault) {
            log.info(Messages.getMessage(DeploymentErrorMsgs.MODULE_VALIDATION_FAILED,
                    axisFault.getMessage()));

            throw new DeploymentException(axisFault);
        }

        if (repoListener != null) {
            repoListener.checkServices();
        }

        return axisConfig;
    }

    /**
     * Starts the Deployment engine to perform Hot deployment and so on..
     */
    private void startSearch(RepositoryListener listener) {
        Scheduler scheduler = new Scheduler();

        scheduler.schedule(new SchedulerTask(listener), new DeploymentIterator());
    }

    public void unDeploy() {
        String serviceName = "";

        try {
            if (wsToUnDeploy.size() > 0) {
                for (int i = 0; i < wsToUnDeploy.size(); i++) {
                    WSInfo wsInfo = (WSInfo) wsToUnDeploy.get(i);

                    if (wsInfo.getType() == TYPE_SERVICE) {
                        serviceName = getAxisServiceName(wsInfo.getFileName());

                        // todo fix me deepal
                        log.info(Messages.getMessage(DeploymentErrorMsgs.SERVICE_REMOVED,
                                wsInfo.getFileName()));
                    }

                    axisConfig.getFaultyServices().remove(serviceName);
                }
            }
        } catch (Exception e) {
            log.info(e);
        }

        wsToUnDeploy.clear();
    }

    /**
     * Checks whether some one has changed the system pre-defined phases
     * for all the flows. If they have been changed,throws a DeploymentException.
     *
     * @throws DeploymentException
     */
    private void validateSystemPredefinedPhases() throws DeploymentException {
        ArrayList inPhases = phasesinfo.getINPhases();

        try {
            String phase1 = ((Phase) inPhases.get(0)).getPhaseName();
            String phases = ((Phase) inPhases.get(1)).getPhaseName();
            String phase3 = ((Phase) inPhases.get(2)).getPhaseName();

            if (!(phase1.equals(PhaseMetadata.PHASE_TRANSPORTIN)
                    && phases.equals(PhaseMetadata.PHASE_PRE_DISPATCH)
                    && phase3.equals(PhaseMetadata.PHASE_DISPATCH))) {
                throw new DeploymentException(
                        Messages.getMessage(DeploymentErrorMsgs.INVALID_PHASE));
            }
        } catch (Exception e) {
            throw new DeploymentException(Messages.getMessage(DeploymentErrorMsgs.INVALID_PHASE));
        }

        axisConfig.setInPhasesUptoAndIncludingPostDispatch(phasesinfo.getGlobalInflow());
        axisConfig.setInFaultPhases(phasesinfo.getIN_FaultPhases());
        axisConfig.setGlobalOutPhase(phasesinfo.getGlobalOutPhaseList());
    }

    /**
     * Gets AxisConfiguration.
     *
     * @return AxisConfiguration <code>AxisConfiguration</code>
     */
    public AxisConfiguration getAxisConfig() {
        return axisConfig;
    }

    /**
     * Retrieves service name from the archive file name.
     * If the archive file name is service1.aar , then axis service name would be service1
     *
     * @param fileName
     * @return Returns String.
     */
    private String getAxisServiceName(String fileName) {
        char seperator = '.';
        String value;
        int index = fileName.indexOf(seperator);

        if (index > 0) {
            value = fileName.substring(0, index);

            return value;
        }

        return fileName;
    }

    public ArchiveFileData getCurrentFileItem() {
        return currentArchiveFile;
    }

    public ModuleDescription getModule(QName moduleName) throws AxisFault {
        return axisConfig.getModule(moduleName);
    }

    public PhasesInfo getPhasesinfo() {
        return phasesinfo;
    }

    public boolean isHotUpdate() {
        return hotUpdate;
    }

    /**
     * To set the all the classLoader hierarchy this method can be used , the top most parenet is
     * CCL then SCL(system Class Loader)
     * CCL
     * :
     * SCL
     * :  :
     * MCCL  SCCL
     * :      :
     * MCL    SCL
     * <p/>
     * <p/>
     * MCCL :  module common class loader
     * SCCL : Service commin class loader
     * MCL : module class loader
     * SCL  : Service class loader
     *
     * @param axis2repo : The repository folder of Axis2
     * @throws DeploymentException
     */
    private void setClassLoaders(String axis2repo) throws DeploymentException {
        ClassLoader sysClassLoader =
                Utils.getClassLoader(Thread.currentThread().getContextClassLoader(), axis2repo);

        axisConfig.setSystemClassLoader(sysClassLoader);

        File services = new File(axis2repo, DIRECTORY_SERVICES);

        if (services.exists()) {
            axisConfig.setServiceClassLoader(
                    Utils.getClassLoader(axisConfig.getSystemClassLoader(), services));
        } else {
            axisConfig.setServiceClassLoader(axisConfig.getSystemClassLoader());
        }

        File modules = new File(axis2repo, DIRECTORY_MODULES);

        if (modules.exists()) {
            axisConfig.setModuleClassLoader(Utils.getClassLoader(axisConfig.getSystemClassLoader(),
                    modules));
        } else {
            axisConfig.setModuleClassLoader(axisConfig.getSystemClassLoader());
        }
    }

    /**
     * Sets hotDeployment and hot update.
     */
    private void setDeploymentFeatures() {
        String value;
        Parameter parahotdeployment = axisConfig.getParameter(TAG_HOT_DEPLOYMENT);
        Parameter parahotupdate = axisConfig.getParameter(TAG_HOT_UPDATE);

        if (parahotdeployment != null) {
            value = (String) parahotdeployment.getValue();

            if ("false".equalsIgnoreCase(value)) {
                hotDeployment = false;
            }
        }

        if (parahotupdate != null) {
            value = (String) parahotupdate.getValue();

            if ("false".equalsIgnoreCase(value)) {
                hotUpdate = false;
            }
        }
    }

    public void setPhasesinfo(PhasesInfo phasesinfo) {
        this.phasesinfo = phasesinfo;
    }

    /**
     * Creates directories for modules/services, copies configuration xml from class loader if necessary
     *
     * @param repositoryName
     * @param xmlFile
     * @throws DeploymentException
     */
    private void prepareRepository(String repositoryName, String xmlFile) throws DeploymentException {
        File repository = new File(repositoryName);

        if (!repository.exists()) {
            repository.mkdirs();

            File services = new File(repository, DIRECTORY_SERVICES);
            File modules = new File(repository, DIRECTORY_MODULES);

            modules.mkdirs();
            services.mkdirs();
        }

        File serverConf = new File(repository, xmlFile);

        if (!serverConf.exists()) {
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            InputStream in = cl.getResourceAsStream(AXIS2_CONFIGURATION_RESOURCE);
            FileOutputStream out = null;

            if (in != null) {
                try {
                    serverConf.createNewFile();
                    out = new FileOutputStream(serverConf);

                    int BUFSIZE = 512;    // since only a test file going to load , the size has selected
                    byte[] buf = new byte[BUFSIZE];
                    int read;

                    while ((read = in.read(buf)) > 0) {
                        out.write(buf, 0, read);
                    }

                    in.close();
                    out.close();
                } catch (IOException e) {
                    throw new DeploymentException(e);
                } finally {
                    if (out != null) {
                        try {
                            out.close();
                        } catch (IOException e) {
                            // ignore
                        }
                    }

                    try {
                        in.close();
                    } catch (IOException e) {
                        // ignore
                    }
                }
            } else {
                throw new DeploymentException(
                        Messages.getMessage(DeploymentErrorMsgs.CONFIG_NOT_FOUND));
            }
        }
    }
}
