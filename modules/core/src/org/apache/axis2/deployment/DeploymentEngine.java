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
import org.apache.axis2.Constants;
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
import org.apache.axis2.description.*;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.engine.Phase;
import org.apache.axis2.i18n.Messages;
import org.apache.axis2.modules.Module;
import org.apache.axis2.phaseresolver.PhaseMetadata;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class DeploymentEngine implements DeploymentConstants {

    private Log log = LogFactory.getLog(getClass());
    private boolean hotUpdate = true;    // to do hot update or not
    private boolean hotDeployment = true;    // to do hot deployment or not
    public String axis2repository = null;
    private boolean useDefault = false;

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
    private String axis2_xml_file_name;


    /**
     * Default constructor is needed to deploy module and service programatically.
     */
    public DeploymentEngine() {
    }

    public DeploymentEngine(String repositoryName, String xmlFile)
            throws DeploymentException {
        if ((repositoryName == null || "".equals(repositoryName.trim())) &&
                (xmlFile == null || "".equals(xmlFile.trim()))) {
            String axis2_home = System.getProperty(Constants.AXIS2_HOME);
            if (axis2_home != null && !"".equals("")) {
                useDefault = false;
                axis2repository = axis2_home;
            } else {
                useDefault = true;
                axis2repository = null;
                log.debug("neither repository location nor axis2.xml are given ," +
                        " so system will continue using default configuration (using default_axis2.xml)");
            }

        } else if (!(repositoryName == null || "".equals(repositoryName.trim()))) {
            axis2repository = repositoryName.trim();
            File axisRepo = new File(axis2repository);
            if (!axisRepo.exists()) {
                throw new DeploymentException("System can not find the given repository location: "
                        + axis2repository);
            }
            if (xmlFile == null || "".equals(xmlFile.trim())) {
                axis2_xml_file_name = null;
                useDefault = true;
            } else {
                axis2_xml_file_name = xmlFile;
            }
            prepareRepository(repositoryName);
        } else if (!(xmlFile == null || "".equals(xmlFile.trim()))) {
            axis2_xml_file_name = xmlFile;
            axis2repository = null;
        }
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

    private void addNewModule(AxisModule modulemetadata) throws AxisFault {

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
        // module from services.xml at serviceGroup level
        ArrayList groupModules = serviceGroup.getModuleRefs();

        for (int i = 0; i < groupModules.size(); i++) {
            QName moduleName = (QName) groupModules.get(i);
            AxisModule module = axisConfig.getModule(moduleName);

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
            ArrayList contolops = new ArrayList();
            AxisService axisService = (AxisService) services.next();
            axisService.setUseDefaultChains(false);

            axisService.setFileName(currentArchiveFile.getFile().getAbsolutePath());
            serviceGroup.addService(axisService);

            // modules from <service>
            ArrayList list = axisService.getModules();

            for (int i = 0; i < list.size(); i++) {
                AxisModule module = axisConfig.getModule((QName) list.get(i));

                if (module == null) {
                    throw new DeploymentException(
                            Messages.getMessage(
                                    DeploymentErrorMsgs.BAD_MODULE_FROM_SERVICE, axisService.getName(),
                                    ((QName) list.get(i)).getLocalPart()));
                }

                axisService.engageModule(module, axisConfig);
            }

            for (Iterator iterator = axisService.getOperations(); iterator.hasNext();) {
                AxisOperation opDesc = (AxisOperation) iterator.next();
                ArrayList modules = opDesc.getModuleRefs();

                for (int i = 0; i < modules.size(); i++) {
                    QName moduleName = (QName) modules.get(i);
                    AxisModule module = axisConfig.getModule(moduleName);

                    if (module != null) {
                        ArrayList controlops = opDesc.engageModule(module, axisConfig);
                        for (int j = 0; j < controlops.size(); j++) {
                            AxisOperation axisOperation = (AxisOperation) controlops.get(j);
                            contolops.add(axisOperation);
                        }
                    } else {
                        throw new DeploymentException(
                                Messages.getMessage(
                                        DeploymentErrorMsgs.BAD_MODULE_FROM_OPERATION,
                                        opDesc.getName().getLocalPart(), moduleName.getLocalPart()));
                    }
                }
            }
            for (int i = 0; i < contolops.size(); i++) {
                AxisOperation axisOperation = (AxisOperation) contolops.get(i);
                axisService.addOperation(axisOperation);
            }
            contolops.clear();
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
    public AxisModule buildModule(File modulearchive, AxisConfiguration config)
            throws DeploymentException {
        AxisModule axismodule;

        try {
            this.setPhasesinfo(config.getPhasesInfo());
            currentArchiveFile = new ArchiveFileData(modulearchive, TYPE_MODULE);
            axismodule = new AxisModule();
            ArchiveReader archiveReader = new ArchiveReader();

            currentArchiveFile.setClassLoader(false, config.getModuleClassLoader());
            axismodule.setModuleClassLoader(currentArchiveFile.getClassLoader());
            archiveReader.readModuleArchive(currentArchiveFile.getAbsolutePath(), this, axismodule,
                    false, axisConfig);
            ClassLoader moduleClassLoader = axismodule.getModuleClassLoader();
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
                //TODO : Need to fix this , I just comment this to remove compile errors
//                module.init(axisConfig, null);
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
                                AxisModule metaData = new AxisModule();
                                metaData.setModuleClassLoader(currentArchiveFile.getClassLoader());
                                metaData.setParent(axisConfig);
                                archiveReader.readModuleArchive(currentArchiveFile.getAbsolutePath(),
                                        this, metaData, explodedDir,
                                        axisConfig);
                                metaData.setFileName(currentArchiveFile.getAbsolutePath());
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
        axisConfig = new AxisConfiguration();
        if (useDefault && axis2repository == null) {
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            InputStream in = cl.getResourceAsStream(AXIS2_CONFIGURATION_RESOURCE);
            AxisConfigBuilder builder = new AxisConfigBuilder(in, this, axisConfig);
            builder.populateConfig();
            axisConfig.setPhasesinfo(phasesinfo);
            //To load modules from the class path
            new RepositoryListenerImpl(this);
            org.apache.axis2.util.Utils.calculateDefaultModuleVersion(axisConfig.getModules(), axisConfig);
            return axisConfig;
        } else if (axis2repository != null) {
            InputStream in;
            if (useDefault) {
                ClassLoader cl = Thread.currentThread().getContextClassLoader();
                in = cl.getResourceAsStream(AXIS2_CONFIGURATION_RESOURCE);
            } else {
                try {
                    in = new FileInputStream(axis2_xml_file_name);
                } catch (FileNotFoundException e) {
                    throw new DeploymentException(e);
                }
            }
            AxisConfigBuilder builder = new AxisConfigBuilder(in, this, axisConfig);

            builder.populateConfig();
            axisConfig.setPhasesinfo(phasesinfo);

            // setting the CLs
            setClassLoaders(axis2repository);
            setDeploymentFeatures();
            RepositoryListener repoListener = new RepositoryListenerImpl(axis2repository, this);
            org.apache.axis2.util.Utils.calculateDefaultModuleVersion(axisConfig.getModules(), axisConfig);
            try {
                axisConfig.setRepository(axis2repository);
                validateSystemPredefinedPhases();
                if (!useDefault) {
                    engageModules();
                }
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
        } else {
            InputStream in;
            try {
                in = new FileInputStream(axis2_xml_file_name);
            } catch (FileNotFoundException e) {
                throw new DeploymentException(e);
            }
            AxisConfigBuilder builder = new AxisConfigBuilder(in, this, axisConfig);
            builder.populateConfig();
            axisConfig.setPhasesinfo(phasesinfo);
            Parameter axis2repoPara = axisConfig.getParameter(AXIS2_REPO);
            if (axis2repoPara != null) {
                axis2repository = (String) axis2repoPara.getValue();
                setClassLoaders(axis2repository);
                setDeploymentFeatures();
                RepositoryListener repoListener = new RepositoryListenerImpl(axis2repository, this);
                org.apache.axis2.util.Utils.calculateDefaultModuleVersion(axisConfig.getModules(), axisConfig);
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
            } else {
                log.info("no repository location found in axis2.xml");
                new RepositoryListenerImpl(this);
                org.apache.axis2.util.Utils.calculateDefaultModuleVersion(axisConfig.getModules(), axisConfig);
                validateSystemPredefinedPhases();
                return axisConfig;
            }
        }
    }

    /**
     * Starts the Deployment engine to perform Hot deployment and so on..
     */
    private void startSearch(RepositoryListener listener) {
        Scheduler scheduler = new Scheduler();

        scheduler.schedule(new SchedulerTask(listener), new DeploymentIterator());
    }

    public void unDeploy() {
        String fileName;
        try {
            if (wsToUnDeploy.size() > 0) {
                for (int i = 0; i < wsToUnDeploy.size(); i++) {
                    WSInfo wsInfo = (WSInfo) wsToUnDeploy.get(i);
                    if (wsInfo.getType() == TYPE_SERVICE) {
                        fileName = getAxisServiceName(wsInfo.getFileName());
                        axisConfig.removeServiceGroup(fileName);
                        log.info(Messages.getMessage(DeploymentErrorMsgs.SERVICE_REMOVED,
                                wsInfo.getFileName()));
                    }
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
        axisConfig.setOutFaultPhases(phasesinfo.getOUT_FaultPhases());
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
     * If the archive file name is service1.aar , then axis2 service name would be service1
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

    public AxisModule getModule(QName moduleName) throws AxisFault {
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
     */

    private void prepareRepository(String repositoryName) {
        File repository = new File(repositoryName);

        File services = new File(repository, DIRECTORY_SERVICES);
        if (!services.exists()) {
            services.mkdirs();
            log.info("no services directory found , new one created");
        }
        File modules = new File(repository, DIRECTORY_MODULES);
        if (!modules.exists()) {
            modules.mkdirs();
            log.info("no modules directory found , new one created");
        }

        if (axis2_xml_file_name == null) {
            File confdir = new File(repository, DIRECTORY_CONF);
            if (!confdir.exists()) {
                log.info("conf directory not found , and no axis2.xml file is given ! " +
                        "System will continue using default_axis2.xml");
                useDefault = true;
            } else {
                File axis2xml = new File(confdir, "axis2.xml");
                if (!axis2xml.exists()) {
                    useDefault = true;
                    log.info("axis2.xml file not found in conf directory , " +
                            "system will continue using default_axis2.xml");
                } else {
                    useDefault = false;
                    axis2_xml_file_name = axis2xml.getAbsolutePath();
                }
            }
        }
    }
}
