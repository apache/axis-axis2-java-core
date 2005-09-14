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
import org.apache.axis2.deployment.listener.RepositoryListenerImpl;
import org.apache.axis2.deployment.repository.util.ArchiveFileData;
import org.apache.axis2.deployment.repository.util.ArchiveReader;
import org.apache.axis2.deployment.repository.util.WSInfo;
import org.apache.axis2.deployment.scheduler.DeploymentIterator;
import org.apache.axis2.deployment.scheduler.Scheduler;
import org.apache.axis2.deployment.scheduler.SchedulerTask;
import org.apache.axis2.deployment.util.PhasesInfo;
import org.apache.axis2.description.*;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.engine.AxisConfigurationImpl;
import org.apache.axis2.engine.Handler;
import org.apache.axis2.i18n.Messages;
import org.apache.axis2.modules.Module;
import org.apache.axis2.phaseresolver.PhaseException;
import org.apache.axis2.phaseresolver.PhaseMetadata;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.namespace.QName;
import java.io.*;
import java.util.*;


public class DeploymentEngine implements DeploymentConstants {

    private Log log = LogFactory.getLog(getClass());
    private  Scheduler scheduler;

    public String axis2repository = null;


    private boolean hotDeployment = true;   //to do hot deployment or not
    private boolean hotUpdate = true;  // to do hot update or not
    private boolean extractServiceArchive = false;// need to exatract service archive file


    /**
     * This will store all the web Services to deploy
     */
    private List wsToDeploy = new ArrayList();
    /**
     * this will store all the web Services to undeploy
     */
    private List wsToUnDeploy = new ArrayList();

    /**
     * to keep a ref to engine register
     * this ref will pass to engine when it call start()
     * method
     */
    private AxisConfiguration axisConfig;

    /**
     * this constaructor for the testing
     */

    private String folderName;

    private String engineConfigName;

    /**
     * This to keep a referance to serverMetaData object
     */
    // private static ServerMetaData axisGlobal = new ServerMetaData();

    private ArchiveFileData currentArchiveFile;

    //tobuild chains
//    private ConfigurationContextFactory factory;

    /**
     * To store the module specified in the server.xml at the document parsing time
     */
    private ArrayList modulelist = new ArrayList();

    private PhasesInfo phasesinfo = new PhasesInfo(); //to store phases list in axis2.xml

    /**
     * Resource that contains the configuration
     */
    protected static final String AXIS2_CONFIGURATION_RESOURCE = "org/apache/axis2/deployment/axis2.xml";
    protected static final String SERVER_XML_FILE = "axis2.xml";

    /**
     * Default constructor is need to deploye module and service programatically
     */
    public DeploymentEngine() {
    }

    /**
     * This the constructor which is used by Engine inorder to start
     * Deploymenat module,
     *
     * @param repositoryName is the path to which Repositary Listner should
     *                       listent.
     */

    public DeploymentEngine(String repositoryName) throws DeploymentException {
        this(repositoryName, SERVER_XML_FILE);
    }

    public DeploymentEngine(String repositoryName, String serverXMLFile)
            throws DeploymentException {
        if (repositoryName == null || repositoryName.trim().equals("")) {
            throw new DeploymentException(Messages.getMessage(DeploymentErrorMsgs.REPO_CAN_NOT_BE_NULL));
        }
        this.folderName = repositoryName;
        axis2repository = repositoryName;
        File repository = new File(repositoryName);
        if (!repository.exists()) {
            repository.mkdirs();
            File services = new File(repository, "services");
            File modules = new File(repository, "modules");
            modules.mkdirs();
            services.mkdirs();
        }
        File serverConf = new File(repository, serverXMLFile);
        if (!serverConf.exists()) {
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            InputStream in = cl.getResourceAsStream(AXIS2_CONFIGURATION_RESOURCE);
            FileOutputStream out = null;
            if (in != null) {
                try {
                    serverConf.createNewFile();
                    out = new FileOutputStream(serverConf);
                    int BUFSIZE = 512; // since only a test file going to load , the size has selected
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
                    if( out!=null) {
                        try {
                            out.close();
                        } catch (IOException e) {
                            //ignore
                        }
                    }
                    try {
                        in.close();
                    } catch (IOException e) {
                        //ignore
                    }
                }


            } else {
                throw new DeploymentException(Messages.getMessage(DeploymentErrorMsgs.CONFIG_NOT_FOUND));
            }
        }
//        factory = new ConfigurationContextFactory();
        this.engineConfigName = repositoryName + '/' + serverXMLFile;
    }

    public ArchiveFileData getCurrentFileItem() {
        return currentArchiveFile;
    }


    /**
     * tio get ER
     *
     * @return AxisConfiguration <code>AxisConfiguration</code>
     */
    public AxisConfiguration getAxisConfig() {
        return axisConfig;
    }

    /**
     * To set hotDeployment and hot update
     */
    private void setDeploymentFeatures() {
        String value;
        Parameter parahotdeployment = axisConfig.getParameter(HOTDEPLOYMENT);
        Parameter parahotupdate = axisConfig.getParameter(HOTUPDATE);
        Parameter paraextractServiceArchive = axisConfig.getParameter(EXTRACTSERVICEARCHIVE);
        if (parahotdeployment != null) {
            value = (String) parahotdeployment.getValue();
            if ("false".equalsIgnoreCase(value))
                hotDeployment = false;
        }
        if (parahotupdate != null) {
            value = (String) parahotupdate.getValue();
            if ("false".equalsIgnoreCase(value))
                hotUpdate = false;
        }
        if(paraextractServiceArchive != null){
            value = (String) paraextractServiceArchive.getValue();
            if ("true".equalsIgnoreCase(value))
                extractServiceArchive = true;
        }
    }

    public AxisConfiguration load() throws DeploymentException {
        if (engineConfigName == null) {
            throw new DeploymentException(Messages.getMessage(DeploymentErrorMsgs.PATH_TO_CONFIG_CAN_NOT_B_NULL));
        }
        File tempfile = new File(engineConfigName);
        try {
            InputStream in = new FileInputStream(tempfile);
            axisConfig = createEngineConfig();
            AxisConfigBuilder builder =new AxisConfigBuilder(in,this,axisConfig);
            builder.populateConfig();
//            DeploymentParser parser = new DeploymentParser(in, this);
            // parser.processGlobalConfig(((AxisConfigurationImpl) axisConfig), AXIS2CONFIG);
        } catch (FileNotFoundException e) {
            throw new DeploymentException(e);
        }
        setDeploymentFeatures();
        if (hotDeployment) {
            startSearch(this);
        } else {
            new RepositoryListenerImpl(folderName, this,extractServiceArchive);
        }
        try {
            ((AxisConfigurationImpl) axisConfig).setRepository(axis2repository);
            validateSystemPredefinedPhases();
            ((AxisConfigurationImpl) axisConfig).setPhasesinfo(phasesinfo);
            engageModules();
        } catch (AxisFault axisFault) {
            log.info(Messages.getMessage(DeploymentErrorMsgs.MODULE_VAL_FAILED, axisFault.getMessage()));
            throw new DeploymentException(axisFault);
        }
        return axisConfig;
    }


    public AxisConfiguration loadClient(String clientHome) throws DeploymentException {
        InputStream in;
        axis2repository = clientHome;
        boolean isRepositoryExist = false;
        if (!(clientHome == null || clientHome.trim().equals(""))) {
            checkClientHome(clientHome);
            isRepositoryExist = true;
            try {
//                engineConfigName = axis2repository + "/axis2.xml";
                File tempfile = new File(engineConfigName);
                in = new FileInputStream(tempfile);
            } catch (FileNotFoundException e) {
                throw new DeploymentException(e);
            }
        } else {
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            in =
                    cl.getResourceAsStream(AXIS2_CONFIGURATION_RESOURCE);
        }
        axisConfig = createEngineConfig();
        AxisConfigBuilder builder =new AxisConfigBuilder(in,this,axisConfig);
        builder.populateConfig();
//            DeploymentParser parser = new DeploymentParser(in, this);
//            parser.processGlobalConfig(((AxisConfigurationImpl) axisConfig),
//                    AXIS2CONFIG);
        if (isRepositoryExist) {
            hotDeployment = false;
            hotUpdate = false;
            new RepositoryListenerImpl(folderName, this,extractServiceArchive);
        }
        try {
            ((AxisConfigurationImpl) axisConfig).setRepository(axis2repository);
            ((AxisConfigurationImpl) axisConfig).setPhasesinfo(phasesinfo);
            engageModules();
        } catch (AxisFault axisFault) {
            log.info(Messages.getMessage(DeploymentErrorMsgs.MODULE_VAL_FAILED, axisFault.getMessage()));
            throw new DeploymentException(axisFault);
        }
        return axisConfig;
    }


    private void checkClientHome(String clientHome) throws DeploymentException {
        String clientXML = SERVER_XML_FILE;
        this.folderName = clientHome;
        File repository = new File(clientHome);
        if (!repository.exists()) {
            repository.mkdirs();
            File services = new File(repository, "services");
            File modules = new File(repository, "modules");
            modules.mkdirs();
            services.mkdirs();
        }
        File serverConf = new File(repository, clientXML);
        if (!serverConf.exists()) {
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            InputStream in = cl.getResourceAsStream(AXIS2_CONFIGURATION_RESOURCE);
            if (in != null) {
                try {
                    serverConf.createNewFile();
                    FileOutputStream out = new FileOutputStream(serverConf);
                    int BUFSIZE = 512; // since only a test file going to load , the size has selected
                    byte[] buf = new byte[BUFSIZE];
                    int read;
                    while ((read = in.read(buf)) > 0) {
                        out.write(buf, 0, read);
                    }
                    in.close();
                    out.close();
                } catch (IOException e) {
                    throw new DeploymentException(e);
                }


            } else {
                throw new DeploymentException(Messages.getMessage(DeploymentErrorMsgs.CONFIG_NOT_FOUND));
            }
        }
//        factory = new ConfigurationContextFactory();
        this.engineConfigName = clientHome + '/' + clientXML;
    }

    /**
     * This methode used to check the modules referd by server.xml
     * are exist , or they have deployed
     */
    private void engageModules() throws AxisFault {
        // ArrayList modules = DeploymentData.getInstance().getModules();
        // PhaseResolver resolver = new PhaseResolver(axisConfig);
        for (Iterator iterator = modulelist.iterator(); iterator.hasNext();) {
            QName name = (QName) iterator.next();
            axisConfig.engageModule(name);
        }
    }

    /**
     * This method is to check wether some one has change the system pre defined phases for all the
     * flows if some one has done so will throw a DeploymentException
     *
     * @throws DeploymentException
     */
    private void validateSystemPredefinedPhases() throws DeploymentException {
        ArrayList inPhases = phasesinfo.getINPhases();
        //TODO condition checking should be otherway since null value can occur
        if (!(inPhases.get(0).equals(PhaseMetadata.PHASE_TRANSPORTIN) &&
                inPhases.get(1).equals(PhaseMetadata.PHASE_PRE_DISPATCH) &&
                inPhases.get(2).equals(PhaseMetadata.PHASE_DISPATCH) &&
                inPhases.get(3).equals(PhaseMetadata.PHASE_POST_DISPATCH))) {
            throw new DeploymentException(Messages.getMessage(DeploymentErrorMsgs.IN_VALID_PHASE));
        }
        //  ArrayList outPhaes = tempdata.getOutphases();
        //TODO do the validation code here
        //ArrayList systemDefaultPhases =((AxisConfigurationImpl)axisConfig).getInPhasesUptoAndIncludingPostDispatch();
    }

    public ModuleDescription getModule(QName moduleName) throws AxisFault {
        return axisConfig.getModule(moduleName);
    }

    /**
     * this method use to start the Deployment engine
     * inorder to perform Hot deployment and so on..
     */
    private void startSearch(DeploymentEngine engine) {
        scheduler = new Scheduler();
        scheduler.schedule(new SchedulerTask(engine, folderName,extractServiceArchive),
                new DeploymentIterator());
    }

    private AxisConfiguration createEngineConfig() {
        return new AxisConfigurationImpl();
    }


    private void addnewService(ServiceDescription serviceMetaData) throws AxisFault {
        try {
            loadServiceProperties(serviceMetaData);
            axisConfig.addService(serviceMetaData);
            serviceMetaData.setFileName(currentArchiveFile.getFile().getAbsolutePath());
            ArrayList list = currentArchiveFile.getModules();
            for (int i = 0; i < list.size(); i++) {
                ModuleDescription module = axisConfig.getModule((QName) list.get(i));
                if (module != null) {
                    serviceMetaData.engageModule(module, axisConfig);
                } else {
                    throw new DeploymentException(Messages.getMessage(
                            DeploymentErrorMsgs.IN_VALID_MODUELE_REF, serviceMetaData.getName().
                            getLocalPart(), ((QName) list.get(i)).getLocalPart()));
                }
            }

            HashMap opeartions = serviceMetaData.getOperations();
            Collection opCol = opeartions.values();
            for (Iterator iterator = opCol.iterator(); iterator.hasNext();) {
                OperationDescription opDesc = (OperationDescription) iterator.next();
                ArrayList modules = opDesc.getModuleRefs();
                for (int i = 0; i < modules.size(); i++) {
                    QName moduleName = (QName) modules.get(i);
                    ModuleDescription module = axisConfig.getModule(moduleName);
                    if (module != null) {
                        opDesc.engageModule(module);
                    } else {
                        throw new DeploymentException(Messages.getMessage(
                                DeploymentErrorMsgs.IN_VALID_MODUELE_REF_BY_OP, opDesc.getName()
                                .getLocalPart(), moduleName.getLocalPart()));
                    }
                }

            }
            ///factory.createChains(serviceMetaData, axisConfig, );
        } catch (PhaseException e) {
            throw new AxisFault(e);
        }
//        System.out.println("Adding service = " + serviceMetaData.getName().getLocalPart());
    }

    /**
     * This method is used to fill the axis service , it dose loading service class and also the provider class
     * and it will also load the service handlers
     *
     * @param axisService
     * @throws org.apache.axis2.AxisFault
     */
    private void loadServiceProperties(ServiceDescription axisService) throws AxisFault {
        Flow inflow = axisService.getInFlow();
        if (inflow != null) {
            addFlowHandlers(inflow);
        }

        Flow outFlow = axisService.getOutFlow();
        if (outFlow != null) {
            addFlowHandlers(outFlow);
        }

        Flow faultInFlow = axisService.getFaultInFlow();
        if (faultInFlow != null) {
            addFlowHandlers(faultInFlow);
        }

        Flow faultOutFlow = axisService.getFaultOutFlow();
        if (faultOutFlow != null) {
            addFlowHandlers(faultOutFlow);
        }
        // axisService.setClassLoader(currentArchiveFile.getClassLoader());
    }


    private void loadModuleClass(ModuleDescription module) throws AxisFault {
        Class moduleClass ;
        try {
            String readInClass = currentArchiveFile.getModuleClass();
            if (readInClass != null && !"".equals(readInClass)) {
                moduleClass =
                        Class.forName(readInClass,
                                true,
                                currentArchiveFile.getClassLoader());
                module.setModule((Module) moduleClass.newInstance());
            }
        } catch (Exception e) {
            throw new AxisFault(e.getMessage(), e);
        }

    }


    private void addFlowHandlers(Flow flow) throws AxisFault {
        int count = flow.getHandlerCount();
        ClassLoader loader1 = currentArchiveFile.getClassLoader();
        for (int j = 0; j < count; j++) {
            HandlerDescription handlermd = flow.getHandler(j);
            Class handlerClass ;
            Handler handler;
            handlerClass = getHandlerClass(handlermd.getClassName(), loader1);
            try {
                handler = (Handler) handlerClass.newInstance();
                handler.init(handlermd);
                handlermd.setHandler(handler);

            } catch (InstantiationException e) {
                throw new AxisFault(e);
            } catch (IllegalAccessException e) {
                throw new AxisFault(e);
            }

        }
    }


    public Class getHandlerClass(String className, ClassLoader loader1) throws AxisFault {
        Class handlerClass ;
        try {
            handlerClass = Class.forName(className, true, loader1);
        } catch (ClassNotFoundException e) {
            throw new AxisFault(e.getMessage());
        }
        return handlerClass;
    }


    private void addNewModule(ModuleDescription modulemetadata) throws AxisFault {
        // currentArchiveFile.setClassLoader();
        Flow inflow = modulemetadata.getInFlow();
        if (inflow != null) {
            addFlowHandlers(inflow);
        }
        Flow outFlow = modulemetadata.getOutFlow();
        if (outFlow != null) {
            addFlowHandlers(outFlow);
        }
        Flow faultInFlow = modulemetadata.getFaultInFlow();
        if (faultInFlow != null) {
            addFlowHandlers(faultInFlow);
        }

        Flow faultOutFlow = modulemetadata.getFaultOutFlow();
        if (faultOutFlow != null) {
            addFlowHandlers(faultOutFlow);
        }
        loadModuleClass(modulemetadata);
        axisConfig.addModule(modulemetadata);
        log.info(Messages.getMessage(DeploymentErrorMsgs.ADDING_NEW_MODULE));
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

    public void doDeploy() {
        if (wsToDeploy.size() > 0) {
            for (int i = 0; i < wsToDeploy.size(); i++) {
                currentArchiveFile = (ArchiveFileData) wsToDeploy.get(i);
                int type = currentArchiveFile.getType();
                try {
                    ArchiveReader archiveReader;
                    StringWriter errorWriter = new StringWriter();
                    switch (type) {
                        case SERVICE:
                            currentArchiveFile.setClassLoader(extractServiceArchive);
                            archiveReader = new ArchiveReader();
                            String serviceStatus = "";
                            try {
                                // ServiceDescription service = archiveReader.createService(currentArchiveFile.getAbsolutePath());
                                ServiceDescription service =
                                        archiveReader.createService(currentArchiveFile);
                                service.setClassLoader(currentArchiveFile.getClassLoader());
                              //  service.setParent(axisConfig);
                                archiveReader.processServiceDescriptor(currentArchiveFile.getAbsolutePath(),
                                        this,
                                        service,extractServiceArchive);
//                                archiveReader.readServiceArchive(currentArchiveFile.getAbsolutePath(),
//                                        this,
//                                        service);
                                addnewService(service);
                                log.info(Messages.getMessage(DeploymentErrorMsgs.DEPLOYING_WS, currentArchiveFile.getName()));
                            } catch (DeploymentException de) {
                                log.info(Messages.getMessage(DeploymentErrorMsgs.IN_VALID_SERVICE,
                                        currentArchiveFile.getName()));
//                            log.info("DeploymentException  " + de);
                                PrintWriter error_ptintWriter = new PrintWriter(errorWriter);
                                de.printStackTrace(error_ptintWriter);
                                serviceStatus = "Error:\n" +
                                        errorWriter.toString();
//                                de.printStackTrace();
                            } catch (AxisFault axisFault) {
//                                axisFault.printStackTrace();
                                log.info(Messages.getMessage(DeploymentErrorMsgs.IN_VALID_SERVICE,
                                        currentArchiveFile.getName()));
//                            log.info("AxisFault  " + axisFault);
                                PrintWriter error_ptintWriter = new PrintWriter(errorWriter);
                                axisFault.printStackTrace(error_ptintWriter);
                                serviceStatus = "Error:\n" +
                                        errorWriter.toString();
                            } catch (Exception e) {
//                                e.printStackTrace();
                                log.info(Messages.getMessage(DeploymentErrorMsgs.IN_VALID_SERVICE,
                                        currentArchiveFile.getName()));
//                            log.info("Exception  " + e);
                                PrintWriter error_ptintWriter = new PrintWriter(errorWriter);
                                e.printStackTrace(error_ptintWriter);
                                serviceStatus = "Error:\n" +
                                        errorWriter.toString();
                            } finally {
                                if (serviceStatus.startsWith("Error:")) {
                                    axisConfig.getFaultyServices().put(getAxisServiceName(currentArchiveFile.getName()),
                                            serviceStatus);
                                }
                                currentArchiveFile = null;
                            }
                            break;
                        case MODULE:
                            currentArchiveFile.setClassLoader(false);
                            archiveReader = new ArchiveReader();
                            String moduleStatus = "";
                            try {
                                ModuleDescription metaData = new ModuleDescription();
                                metaData.setParent(axisConfig);
                                archiveReader.readModuleArchive(currentArchiveFile.getAbsolutePath(),
                                        this,
                                        metaData);
                                addNewModule(metaData);
                                log.info(Messages.getMessage(DeploymentErrorMsgs.DEPLOYING_MODULE,
                                        metaData.getName().getLocalPart()));
//                                    "Moduel WS Name  " +
//                                    currentArchiveFile.getName() +
//                                    " modulename :" +
//                                    metaData.getName());
                            } catch (DeploymentException e) {
//                                e.printStackTrace();
                                log.info(Messages.getMessage(
                                        DeploymentErrorMsgs.INVALID_MODULE, currentArchiveFile.getName(),
                                        e.getMessage()));
//                                    "Invalid module" +
//                                    currentArchiveFile.getName());
//                            log.info("DeploymentException  " + e);
                                PrintWriter error_ptintWriter = new PrintWriter(errorWriter);
                                e.printStackTrace(error_ptintWriter);
                                moduleStatus = "Error:\n" + errorWriter.toString();
                            } catch (AxisFault axisFault) {
//                                axisFault.printStackTrace();
                                log.info(Messages.getMessage(DeploymentErrorMsgs.INVALID_MODULE, currentArchiveFile.getName(),
                                        axisFault.getMessage()));
                                PrintWriter error_ptintWriter = new PrintWriter(errorWriter);
                                axisFault.printStackTrace(error_ptintWriter);
                                moduleStatus = "Error:\n" + errorWriter.toString();
                            } finally {
                                if (moduleStatus.startsWith("Error:")) {
                                    axisConfig.getFaultyModules().put(getAxisServiceName(currentArchiveFile.getName()),
                                            moduleStatus);
                                }
                                currentArchiveFile = null;
                            }
                            break;

                    }
                } catch (AxisFault axisFault) {
                    log.info(Messages.getMessage(DeploymentErrorMsgs.SETTING_CL, axisFault.getMessage()));
//                    continue;
                }

            }
        }
        wsToDeploy.clear();
    }

    public void unDeploy() {
        String serviceName = "";
        try {
            if (wsToUnDeploy.size() > 0) {
                for (int i = 0; i < wsToUnDeploy.size(); i++) {
                    WSInfo wsInfo = (WSInfo) wsToUnDeploy.get(i);
                    if (wsInfo.getType() == SERVICE) {
                        serviceName = getAxisServiceName(wsInfo.getFilename());
                        //todo fix me deepal
                        //   axisConfig.removeService(new QName(serviceName));
                        log.info(Messages.getMessage(DeploymentErrorMsgs.SERVICE_REMOVED,
                                wsInfo.getFilename()));
                    }
                    axisConfig.getFaultyServices().remove(serviceName);
                }

            }
        } catch (Exception e) {
            log.info(e);
        }
        wsToUnDeploy.clear();
    }

    public boolean isHotUpdate() {
        return hotUpdate;
    }

    /**
     * This method is used to retrive service name form the arechive file name
     * if the archive file name is service1.aar , then axis service name would be service1
     *
     * @param fileName
     * @return String
     */
    private String getAxisServiceName(String fileName) {
        char seperator = '.';
        String value ;
        int index = fileName.indexOf(seperator);
        if (index > 0) {
            value = fileName.substring(0, index);
            return value;
        }
        return fileName;
    }

    /**
     * while parsing the axis2.xml the module refferences have to be store some where , since at that
     * time none of module availble (they load after parsing the document)
     *
     * @param moduleName <code>QName</code>
     */
    public void addModule(QName moduleName) {
        modulelist.add(moduleName);
    }

    public PhasesInfo getPhasesinfo() {
        return phasesinfo;
    }

    public void setPhasesinfo(PhasesInfo phasesinfo) {
        this.phasesinfo = phasesinfo;
    }

    /* public ServiceDescription deployService(ClassLoader classLoder, InputStream serviceStream, String servieName) throws DeploymentException {
    ServiceDescription service = null;
    try {
    currentArchiveFileile = new ArchiveFileData(SERVICE, servieName);
    currentArchiveFileile.setClassLoader(classLoder);
    service = new ServiceDescription();
    DeploymentParser schme = new DeploymentParser(serviceStream, this);
    schme.parseServiceXML(service);
    service = loadServiceProperties(service);
    } catch (XMLStreamException e) {
    throw  new DeploymentException(e.getMessage());
    } catch (PhaseException e) {
    throw  new DeploymentException(e.getMessage());
    } catch (AxisFault axisFault) {
    throw  new DeploymentException(axisFault.getMessage());
    }
    return service;
    }
*/

    /**
     * This method is used to fill a axisservice object using service.xml , first it should create
     * an axisservice object using WSDL and then fill that using given servic.xml and load all the requed
     * class and build the chains , finally add the  servicecontext to EngineContext and axisservice into
     * EngineConfiguration
     *
     * @param axisService
     * @param serviceInputStream
     * @param classLoader
     * @return
     * @throws DeploymentException
     */
    public ServiceDescription buildService(ServiceDescription axisService,
                                           InputStream serviceInputStream,
                                           ClassLoader classLoader) throws DeploymentException {
        try {
            currentArchiveFile = new ArchiveFileData(SERVICE, "");
            currentArchiveFile.setClassLoader(classLoader);

            ServiceBuilder builder = new ServiceBuilder(serviceInputStream,this,axisService);
            builder.populateService();

//            DeploymentParser schme = new DeploymentParser(serviceInputStream,
//                    this);
//            schme.parseServiceXML(axisService);
            loadServiceProperties(axisService);
//            axisConfig.addService(axisService);
        }  catch (AxisFault axisFault) {
            throw new DeploymentException(axisFault);
        }
        return axisService;
    }

    /**
     * This method can be used to build ModuleDescription for a given module archiev file
     *
     * @param modulearchive
     * @return
     * @throws DeploymentException
     */

    public ModuleDescription buildModule(File modulearchive) throws DeploymentException {
        ModuleDescription axismodule;
        try {
            currentArchiveFile = new ArchiveFileData(modulearchive, MODULE);
            axismodule = new ModuleDescription();
            ArchiveReader archiveReader = new ArchiveReader();
            archiveReader.readModuleArchive(currentArchiveFile.getAbsolutePath(), this, axismodule);
            currentArchiveFile.setClassLoader(false);
            Flow inflow = axismodule.getInFlow();
            if (inflow != null) {
                addFlowHandlers(inflow);
            }
            Flow outFlow = axismodule.getOutFlow();
            if (outFlow != null) {
                addFlowHandlers(outFlow);
            }
            Flow faultInFlow = axismodule.getFaultInFlow();
            if (faultInFlow != null) {
                addFlowHandlers(faultInFlow);
            }
            Flow faultOutFlow = axismodule.getFaultOutFlow();
            if (faultOutFlow != null) {
                addFlowHandlers(faultOutFlow);
            }
            loadModuleClass(axismodule);
        } catch (AxisFault axisFault) {
            throw new DeploymentException(axisFault);
        }
        return axismodule;
    }

}
