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

package org.apache.axis.deployment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.wsdl.factory.WSDLFactory;

import org.apache.axis.context.ConfigurationContextFactory;
import org.apache.axis.deployment.listener.RepositoryListenerImpl;
import org.apache.axis.deployment.repository.util.ArchiveFileData;
import org.apache.axis.deployment.repository.util.ArchiveReader;
import org.apache.axis.deployment.repository.util.WSInfo;
import org.apache.axis.deployment.scheduler.DeploymentIterator;
import org.apache.axis.deployment.scheduler.Scheduler;
import org.apache.axis.deployment.scheduler.SchedulerTask;
import org.apache.axis.deployment.util.DeploymentData;
import org.apache.axis.description.Flow;
import org.apache.axis.description.HandlerDescription;
import org.apache.axis.description.ModuleDescription;
import org.apache.axis.description.OperationDescription;
import org.apache.axis.description.Parameter;
import org.apache.axis.description.ServiceDescription;
import org.apache.axis.engine.AxisConfiguration;
import org.apache.axis.engine.AxisConfigurationImpl;
import org.apache.axis.engine.AxisFault;
import org.apache.axis.engine.Handler;
import org.apache.axis.modules.Module;
import org.apache.axis.phaseresolver.PhaseException;
import org.apache.axis.phaseresolver.PhaseMetadata;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class DeploymentEngine implements DeploymentConstants {

    private Log log = LogFactory.getLog(getClass());
    private static Scheduler scheduler;

    public static String axis2repository = null;


    private boolean hotDeployment = true;   //to do hot deployment or not
    private boolean hotUpdate = true;  // to do hot update or not


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
    private ConfigurationContextFactory factory;

    /**
     * Default constructor is need to deploye module and service programatically
     */
    public DeploymentEngine() {
    }

    /**
     * This the constructor which is used by Engine inorder to start
     * Deploymenat module,
     *
     * @param RepositaryName is the path to which Repositary Listner should
     *                       listent.
     */

    public DeploymentEngine(String RepositaryName) throws DeploymentException {
        this(RepositaryName, "axis2.xml");
    }

    public DeploymentEngine(String RepositaryName, String serverXMLFile) throws DeploymentException {
        if(RepositaryName == null || RepositaryName.trim().equals("")){
            throw new DeploymentException("Axis2 repositary can not be null");
        }
        this.folderName = RepositaryName;
        axis2repository = RepositaryName;
        File repository = new File(RepositaryName);
        if (!repository.exists()) {
            repository.mkdirs();
            File servcies = new File(repository, "services");
            File modules = new File(repository, "modules");
            modules.mkdirs();
            servcies.mkdirs();
        }
        File serverConf = new File(repository, serverXMLFile);
        if (!serverConf.exists()) {
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            InputStream in = cl.getResourceAsStream("org/apache/axis/deployment/axis2.xml");
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
                throw new DeploymentException("can not found org/apache/axis/deployment/axis2.xml");

            }
        }
        factory = new ConfigurationContextFactory();
        this.engineConfigName = RepositaryName + '/' + serverXMLFile;
    }

    public ArchiveFileData getCurrentFileItem() {
        return currentArchiveFile;
    }


    /**
     * tio get ER
     *
     * @return
     */
    public AxisConfiguration getAxisConfig() {
        return axisConfig;
    }

    /**
     * To set hotDeployment and hot update
     */
    private void setDeploymentFeatures() {
        String value;
        Parameter parahotdeployment = ((AxisConfigurationImpl) axisConfig).getParameter(HOTDEPLOYMENT);
        Parameter parahotupdate = ((AxisConfigurationImpl) axisConfig).getParameter(HOTUPDATE);
        if (parahotdeployment != null) {
            value = (String) parahotdeployment.getValue();
            if ("false".equals(value))
                hotDeployment = false;
        }
        if (parahotupdate != null) {
            value = (String) parahotupdate.getValue();
            if ("false".equals(value))
                hotUpdate = false;

        }
    }

    public AxisConfiguration load() throws DeploymentException {
        if (engineConfigName == null) {
            throw new DeploymentException("path to axis2.xml can not be NUll");
        }
        File tempfile = new File(engineConfigName);
        try {
            InputStream in = new FileInputStream(tempfile);
            axisConfig = createEngineConfig();
            DeploymentParser parser = new DeploymentParser(in, this);
            parser.processGlobalConfig(((AxisConfigurationImpl) axisConfig), AXIS2CONFIG);
        } catch (FileNotFoundException e) {
            throw new DeploymentException("Exception at deployment", e);
        } catch (XMLStreamException e) {
            throw new DeploymentException(e);
        }
        setDeploymentFeatures();
        if (hotDeployment) {
            startSearch(this);
        } else {
            new RepositoryListenerImpl(folderName, this);
        }
        try {
            engagdeModules();
            validateSystemPredefinedPhases();
        } catch (AxisFault axisFault) {
            log.info("Module validation failed" + axisFault.getMessage());
            throw new DeploymentException(axisFault);
        }
        return axisConfig;
    }


    public AxisConfiguration loadClient(String clientHome) throws DeploymentException {
        InputStream in = null;
        axis2repository = clientHome;
        boolean isRepositoryExist = false;
        if (clientHome != null) {
            checkClientHome(clientHome);
            isRepositoryExist = true;
            try {
                File tempfile = new File(engineConfigName);
                in = new FileInputStream(tempfile);
            } catch (FileNotFoundException e) {
                throw new DeploymentException("Exception at deployment", e);
            }
        } else {
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            in = cl.getResourceAsStream("org/apache/axis/deployment/axis2.xml");
        }
        try {
            axisConfig = createEngineConfig();
            DeploymentParser parser = new DeploymentParser(in, this);
            parser.processGlobalConfig(((AxisConfigurationImpl) axisConfig), AXIS2CONFIG);
        } catch (XMLStreamException e) {
            throw new DeploymentException(e);
        }
        if (isRepositoryExist) {
            hotDeployment = false;
            hotUpdate = false;
            new RepositoryListenerImpl(folderName, this);
            try {
                engagdeModules();
            } catch (AxisFault axisFault) {
                log.info("Module validation failed" + axisFault.getMessage());
                throw new DeploymentException(axisFault);
            }
        }
        return axisConfig;
    }


    private void checkClientHome(String clientHome) throws DeploymentException {
        String clientXML = "axis2.xml";
        this.folderName = clientHome;
        File repository = new File(clientHome);
        if (!repository.exists()) {
            repository.mkdirs();
            File servcies = new File(repository, "services");
            File modules = new File(repository, "modules");
            modules.mkdirs();
            servcies.mkdirs();
        }
        File serverConf = new File(repository, clientXML);
        if (!serverConf.exists()) {
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            InputStream in = cl.getResourceAsStream("org/apache/axis/deployment/axis2.xml");
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
                throw new DeploymentException("can not found org/apache/axis/deployment/axis2.xml");

            }
        }
        factory = new ConfigurationContextFactory();
        this.engineConfigName = clientHome + '/' + clientXML;
    }

    /**
     * This methode used to check the modules referd by server.xml
     * are exist , or they have deployed
     */
    private void engagdeModules() throws AxisFault {
        ArrayList modules = DeploymentData.getInstance().getModules();
        // PhaseResolver resolver = new PhaseResolver(axisConfig);
        for (Iterator iterator = modules.iterator(); iterator.hasNext();) {
            QName name = (QName) iterator.next();
            ((AxisConfigurationImpl) axisConfig).engageModule(name);
        }
    }

    /**
     * This method is to check wether some one has change the system pre defined phases for all the
     * flows if some one has done so will throw a DeploymentException
     *
     * @throws DeploymentException
     */
    private void validateSystemPredefinedPhases() throws DeploymentException {
        DeploymentData tempdata = DeploymentData.getInstance();
        ArrayList inPhases = tempdata.getINPhases();
        //TODO condition checking should be otherway since null value can occur
        if (!(((String) inPhases.get(0)).equals(PhaseMetadata.PHASE_TRANSPORTIN) &&
                ((String) inPhases.get(1)).equals(PhaseMetadata.PHASE_PRE_DISPATCH) &&
                ((String) inPhases.get(2)).equals(PhaseMetadata.PHASE_DISPATCH) &&
                ((String) inPhases.get(3)).equals(PhaseMetadata.PHASE_POST_DISPATCH))) {
            throw new DeploymentException("Invalid System predefined inphases , phase order dose not" +
                    " support\n recheck axis2.xml");
        }
        //  ArrayList outPhaes = tempdata.getOUTPhases();
        //TODO do the validation code here
        //ArrayList systemDefaultPhases =((AxisConfigurationImpl)axisConfig).getInPhasesUptoAndIncludingPostDispatch();
    }

    public ModuleDescription getModule(QName moduleName) throws AxisFault {
        ModuleDescription axisModule = axisConfig.getModule(moduleName);
        return axisModule;
    }

    /**
     * this method use to start the Deployment engine
     * inorder to perform Hot deployment and so on..
     */
    private void startSearch(DeploymentEngine engine) {
        scheduler = new Scheduler();
        scheduler.schedule(new SchedulerTask(engine, folderName), new DeploymentIterator());
    }

    private AxisConfiguration createEngineConfig() {
        AxisConfiguration newEngineConfig = new AxisConfigurationImpl();
        return newEngineConfig;
    }


    private void addnewService(ServiceDescription serviceMetaData) throws AxisFault {
        try {
            loadServiceProperties(serviceMetaData);
            axisConfig.addService(serviceMetaData);

            ArrayList list = currentArchiveFile.getModules();
            for(int i = 0;i<list.size();i++){
                ModuleDescription module = axisConfig.getModule((QName)list.get(i));
                if (module != null) {
                    serviceMetaData.engageModule(module);
                } else {
                    throw new DeploymentException("Service  "  +  serviceMetaData.getName().getLocalPart() +
                            "  Refer to invalide module  " + ((QName)list.get(i)).getLocalPart());
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
                    if(module != null) {
                        opDesc.engageModule(module);
                    } else {
                        throw new DeploymentException("Operation "  +  opDesc.getName().getLocalPart() +
                                "  Refer to invalide module  " + moduleName.getLocalPart());
                    }
                }

            }
            ///factory.createChains(serviceMetaData, axisConfig, );
            System.out.println("service Description : " + serviceMetaData.getServiceDescription());
            System.out.println("adding new service : " + serviceMetaData.getName().getLocalPart());
        } catch (PhaseException e) {
            throw new AxisFault(e);
        }

    }

    /**
     * This method is used to fill the axis service , it dose loading service class and also the provider class
     * and it will also load the service handlers
     *
     * @param axisService
     * @throws AxisFault
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
        axisService.setClassLoader(currentArchiveFile.getClassLoader());
    }


    private void loadModuleClass(ModuleDescription module) throws AxisFault {
        Class moduleClass = null;
        try {
            String readInClass = currentArchiveFile.getModuleClass();
            if (readInClass != null && !"".equals(readInClass)) {
                moduleClass = Class.forName(readInClass, true, currentArchiveFile.getClassLoader());
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
            Class handlerClass = null;
            Handler handler;
            handlerClass = getHandlerClass(handlermd.getClassName(), loader1);
            try {
                handler = (Handler) handlerClass.newInstance();
                handler.init(handlermd);
                handlermd.setHandler(handler);

            } catch (InstantiationException e) {
                throw new AxisFault(e.getMessage());
            } catch (IllegalAccessException e) {
                throw new AxisFault(e.getMessage());
            }

        }
    }


    public Class getHandlerClass(String className, ClassLoader loader1) throws AxisFault {
        Class handlerClass = null;

        try {
            handlerClass = Class.forName(className, true, loader1);
        } catch (ClassNotFoundException e) {
            throw new AxisFault(e.getMessage());
        }
        return handlerClass;
    }


    private void addNewModule(ModuleDescription moduelmetada) throws AxisFault {
        // currentArchiveFile.setClassLoader();
        Flow inflow = moduelmetada.getInFlow();
        if (inflow != null) {
            addFlowHandlers(inflow);
        }
        Flow outFlow = moduelmetada.getOutFlow();
        if (outFlow != null) {
            addFlowHandlers(outFlow);
        }
        Flow faultInFlow = moduelmetada.getFaultInFlow();
        if (faultInFlow != null) {
            addFlowHandlers(faultInFlow);
        }

        Flow faultOutFlow = moduelmetada.getFaultOutFlow();
        if (faultOutFlow != null) {
            addFlowHandlers(faultOutFlow);
        }
        loadModuleClass(moduelmetada);
        axisConfig.addMdoule(moduelmetada);
        System.out.println("adding new module");
    }


    /**
     * @param file
     */
    public void addtowsToDeploy(ArchiveFileData file) {
        wsToDeploy.add(file);
    }

    /**
     * @param file
     */
    public void addtowstoUnDeploy(WSInfo file) {
        wsToUnDeploy.add(file);
    }

    public void doDeploy() {
        if (wsToDeploy.size() > 0) {
            for (int i = 0; i < wsToDeploy.size(); i++) {
                currentArchiveFile = (ArchiveFileData) wsToDeploy.get(i);
                int type = currentArchiveFile.getType();
                try {
                    currentArchiveFile.setClassLoader();
                } catch (AxisFault axisFault) {
                    log.info("Setting Class Loader  " +axisFault);
                    continue;
                }
                ArchiveReader archiveReader = new ArchiveReader();
                String serviceStatus = "";
                switch (type) {
                    case SERVICE:
                        try {
                           // ServiceDescription service = archiveReader.createService(currentArchiveFile.getAbsolutePath());
                            ServiceDescription service = archiveReader.createService(currentArchiveFile);
                            archiveReader.readServiceArchive(currentArchiveFile.getAbsolutePath(), this, service);
                            addnewService(service);
                            log.info("Deployement WS Name  " + currentArchiveFile.getName());
                        } catch (DeploymentException de) {
                            log.info("Invalid service" + currentArchiveFile.getName());
                            log.info("DeploymentException  " + de);
                            serviceStatus = "Error:\n" + de.getMessage();
                        } catch (AxisFault axisFault) {
                            log.info("Invalid service" + currentArchiveFile.getName());
                            log.info("AxisFault  " + axisFault);
                            serviceStatus = "Error:\n" + axisFault.getMessage();
                        } catch (Exception e) {
                            log.info("Invalid service" + currentArchiveFile.getName());
                            log.info("Exception  " + e);
                            serviceStatus = "Error:\n" + e.getMessage();
                        } finally {
                            if (serviceStatus.startsWith("Error:")) {
                                axisConfig.getFaulytServices().put(getAxisServiceName(currentArchiveFile.getName()), serviceStatus);
                            }
                            currentArchiveFile = null;
                        }
                        break;
                    case MODULE:
                        String moduleStatus = "";
                        try {
                            ModuleDescription metaData = new ModuleDescription();
                            archiveReader.readModuleArchive(currentArchiveFile.getAbsolutePath(), this, metaData);
                            addNewModule(metaData);
                            log.info("Moduel WS Name  " + currentArchiveFile.getName() + " modulename :" + metaData.getName());
                        } catch (DeploymentException e) {
                            log.info("Invalid module" + currentArchiveFile.getName());
                            log.info("DeploymentException  " + e);
                            moduleStatus = "Error:\n" + e.getMessage();
                        } catch (AxisFault axisFault) {
                            log.info("Invalid module" + currentArchiveFile.getName());
                            log.info("AxisFault  " + axisFault);
                            moduleStatus = "Error:\n" + axisFault.getMessage();
                        } finally {
                            if (moduleStatus.startsWith("Error:")) {
                                axisConfig.getFaulytModules().put(getAxisServiceName(currentArchiveFile.getName()), moduleStatus);
                            }
                            currentArchiveFile = null;
                        }
                        break;

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
                        axisConfig.removeService(new QName(serviceName));
                        log.info("UnDeployement WS Name  " + wsInfo.getFilename());
                    }
                    axisConfig.getFaulytServices().remove(serviceName);
                }

            }
        } catch (AxisFault e) {
            log.info("AxisFault " + e);
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
     * @return
     */
    private String getAxisServiceName(String fileName) {
        char seperator = '.';
        String value = null;
        int index = fileName.indexOf(seperator);
        if (index > 0) {
            value = fileName.substring(0, index);
            return value;
        }
        return fileName;
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
    public ServiceDescription buildService(ServiceDescription axisService, InputStream serviceInputStream, ClassLoader classLoader) throws DeploymentException {
        try {
            currentArchiveFile = new ArchiveFileData(SERVICE, "");
            currentArchiveFile.setClassLoader(classLoader);
            DeploymentParser schme = new DeploymentParser(serviceInputStream, this);
            schme.parseServiceXML(axisService);
            loadServiceProperties(axisService);
            axisConfig.addService(axisService);
        } catch (XMLStreamException e) {
            throw new DeploymentException(e);
        } catch (AxisFault axisFault) {
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
        ModuleDescription axismodule = null;
        try {
            currentArchiveFile = new ArchiveFileData(modulearchive, MODULE);
            axismodule = new ModuleDescription();
            ArchiveReader archiveReader = new ArchiveReader();
            archiveReader.readModuleArchive(currentArchiveFile.getAbsolutePath(), this, axismodule);
            currentArchiveFile.setClassLoader();
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
