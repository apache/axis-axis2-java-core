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
import java.util.Iterator;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

import org.apache.axis.context.EngineContext;
import org.apache.axis.context.ServiceContext;
import org.apache.axis.deployment.listener.RepositoryListenerImpl;
import org.apache.axis.deployment.repository.utill.HDFileItem;
import org.apache.axis.deployment.repository.utill.UnZipJAR;
import org.apache.axis.deployment.repository.utill.WSInfo;
import org.apache.axis.deployment.scheduler.DeploymentIterator;
import org.apache.axis.deployment.scheduler.Scheduler;
import org.apache.axis.deployment.scheduler.SchedulerTask;
import org.apache.axis.description.AxisGlobal;
import org.apache.axis.description.AxisModule;
import org.apache.axis.description.AxisService;
import org.apache.axis.description.Flow;
import org.apache.axis.description.HandlerMetadata;
import org.apache.axis.description.Parameter;
import org.apache.axis.engine.AxisFault;
import org.apache.axis.engine.EngineConfiguration;
import org.apache.axis.engine.EngineConfigurationImpl;
import org.apache.axis.engine.Handler;
import org.apache.axis.engine.MessageReceiver;
import org.apache.axis.phaseresolver.PhaseException;
import org.apache.axis.phaseresolver.PhaseResolver;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class DeploymentEngine implements DeploymentConstants {

    private Log log = LogFactory.getLog(getClass());
    private static Scheduler scheduler;


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
    private EngineConfiguration engineconfig;

    /**
     * this constaructor for the testing
     */

    private String folderName;

    private String serverConfigName;

    /**
     * This to keep a referance to serverMetaData object
     */
    // private static ServerMetaData server = new ServerMetaData();
    private AxisGlobal server;

    private EngineContext engineContext;


    private HDFileItem currentFileItem;

    /**
     * This the constructor which is used by Engine inorder to start
     * Deploymenat module,
     *
     * @param RepositaryName is the path to which Repositary Listner should
     *                       listent.
     */

    public DeploymentEngine(String RepositaryName) throws DeploymentException {
        this(RepositaryName, "server.xml");

    }

    /**
     * this constructor is used to deploy a web service programatically, by using classLoader
     * and InputStream
     *
     * @param engineconfig
     */
    public DeploymentEngine(EngineConfiguration engineconfig) {
        this.engineconfig = engineconfig;
    }

    public DeploymentEngine(String RepositaryName, String serverXMLFile) throws DeploymentException {
        this.folderName = RepositaryName;
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
            InputStream in = cl.getResourceAsStream("org/apache/axis/deployment/server.xml");
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
                    throw new DeploymentException(e.getMessage());
                }


            } else {
                throw new DeploymentException("can not found org/apache/axis/deployment/server.xml");

            }
        }
        this.serverConfigName = RepositaryName + '/' + serverXMLFile;
    }

//    public DeploymentEngine(String RepositaryName , String configFileName) {
//        this.folderName = RepositaryName;
//        this.serverConfigName = configFileName;
//    }

    public HDFileItem getCurrentFileItem() {
        return currentFileItem;
    }


    /**
     * tio get ER
     *
     * @return
     */
    public EngineConfiguration getEngineconfig() {
        return engineconfig;
    }

    /**
     * This method will fill the engine registry and return it to Engine
     *
     * @return
     * @throws AxisFault
     */
    public EngineContext start() throws AxisFault, DeploymentException, XMLStreamException {
        //String fileName;
        if (serverConfigName == null) {
            throw new DeploymentException("path to Server.xml can not be NUll");
        }
        File tempfile = new File(serverConfigName);
        try {
            InputStream in = new FileInputStream(tempfile);
            engineconfig = createEngineRegistry();
            DeploymentParser parser = new DeploymentParser(in, this);
            parser.procesServerXML(server);
        } catch (FileNotFoundException e) {
            throw new AxisFault("Exception at deployment", e);
        }
        setDeploymentFeatures();
        if (hotDeployment) {
            startSearch(this);
        } else {
            new RepositoryListenerImpl(folderName, this);
        }
        try {
            valideServerModule();
        } catch (PhaseException e) {
            log.info("Module validation failed" + e.getMessage());
        }
        return new EngineContext(engineconfig);
    }

    /**
     * To set hotDeployment and hot update
     */
    private void setDeploymentFeatures() {
        String value;
        Parameter parahotdeployment = server.getParameter(HOTDEPLOYMENT);
        Parameter parahotupdate = server.getParameter(HOTUPDATE);
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

    /**
     * This methode used to check the modules referd by server.xml
     * are exist , or they have deployed
     */
    private void valideServerModule() throws AxisFault, PhaseException {
        Iterator itr = server.getModules().iterator();
        while (itr.hasNext()) {
            QName qName = (QName) itr.next();
            if (getModule(qName) == null) {
                throw new AxisFault(server + " Refer to invalid module " + qName + " has not bean deployed yet !");
            }
        }
        PhaseResolver phaseResolver = new PhaseResolver(engineconfig);
        phaseResolver.buildGlobalChains(engineContext);
        phaseResolver.buildTranspotsChains();

    }

    public AxisModule getModule(QName moduleName) throws AxisFault {
        AxisModule metaData = engineconfig.getModule(moduleName);
        return metaData;
    }

    /**
     * this method use to start the Deployment engine
     * inorder to perform Hot deployment and so on..
     */
    private void startSearch(DeploymentEngine engine) {
        scheduler = new Scheduler();
        scheduler.schedule(new SchedulerTask(engine, folderName), new DeploymentIterator());
    }

    private EngineConfiguration createEngineRegistry() throws AxisFault {
        EngineConfiguration newEngineRegisty;

        server = new AxisGlobal();
        newEngineRegisty = new EngineConfigurationImpl(server);
        engineContext = new EngineContext(newEngineRegisty);

        return newEngineRegisty;
    }


    private void addnewService(AxisService serviceMetaData) throws AxisFault, PhaseException {
        currentFileItem.setClassLoader();
        ServiceContext serviceContext = getRunnableService(serviceMetaData);
        engineContext.addService(serviceContext);
        engineconfig.addService(serviceMetaData);
        /*Parameter para = serviceMetaData.getParameter("OUTSERVICE");
        if (para != null) {
        String value = (String) para.getValue();
        if ("true".equals(value)) {
        Class temp = serviceMetaData.getServiceClass();
        try {
        Thread servie = (Thread) temp.newInstance();
        servie.start();
        } catch (InstantiationException e) {
        throw new AxisFault(e.getMessage());
        } catch (IllegalAccessException e) {
        throw new AxisFault(e.getMessage());
        }

        }
        }*/
    }

    /**
     * This method is used to fill the axis service , it dose loading service class and also the provider class
     * and it will also load the service handlers
     *
     * @param serviceMetaData
     * @return
     * @throws AxisFault
     * @throws PhaseException
     */
    private ServiceContext getRunnableService(AxisService serviceMetaData) throws AxisFault, PhaseException {
        loadMessageReceiver(serviceMetaData);
        Flow inflow = serviceMetaData.getInFlow();
        if (inflow != null) {
            addFlowHandlers(inflow);
        }

        Flow outFlow = serviceMetaData.getOutFlow();
        if (outFlow != null) {
            addFlowHandlers(outFlow);
        }

        Flow faultFlow = serviceMetaData.getFaultInFlow();
        if (faultFlow != null) {
            addFlowHandlers(faultFlow);
        }
        ServiceContext serviceContext = new ServiceContext(serviceMetaData);
        PhaseResolver reolve = new PhaseResolver(engineconfig, serviceMetaData,serviceContext);
        reolve.buildchains();
        serviceMetaData.setClassLoader(currentFileItem.getClassLoader());
        return serviceContext;
    }


    private void loadMessageReceiver(AxisService service) throws AxisFault {
        Class messageReceiver = null;
        ClassLoader loader1 = currentFileItem.getClassLoader();
        try {
            service.setClassLoader(loader1);
            String readInClass = currentFileItem.getMessgeReceiver();
            if (readInClass != null && !"".equals(readInClass)) {
                messageReceiver = Class.forName(readInClass, true, loader1);
                service.setMessageReceiver((MessageReceiver)messageReceiver.newInstance());
            }
        } catch (Exception e) {
            throw new AxisFault(e.getMessage(), e);
        }

    }


    private void addFlowHandlers(Flow flow) throws AxisFault {
        int count = flow.getHandlerCount();
        ClassLoader loader1 = currentFileItem.getClassLoader();
        for (int j = 0; j < count; j++) {
            //todo handle exception in properway
            HandlerMetadata handlermd = flow.getHandler(j);
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


    private void addNewModule(AxisModule moduelmetada) throws AxisFault {
        currentFileItem.setClassLoader();

        Flow inflow = moduelmetada.getInFlow();
        addFlowHandlers(inflow);

        Flow outFlow = moduelmetada.getOutFlow();
        addFlowHandlers(outFlow);

        Flow faultFlow = moduelmetada.getFaultInFlow();
        addFlowHandlers(faultFlow);

        engineconfig.addMdoule(moduelmetada);
    }


    /**
     * @param file
     */
    public void addtowsToDeploy(HDFileItem file) {
        wsToDeploy.add(file);
    }

    /**
     * @param file
     */
    public void addtowstoUnDeploy(WSInfo file) {
        wsToUnDeploy.add(file);
    }

    public void doDeploy() {
        //todo complete this
        if (wsToDeploy.size() > 0) {
            for (int i = 0; i < wsToDeploy.size(); i++) {
                currentFileItem = (HDFileItem) wsToDeploy.get(i);
                int type = currentFileItem.getType();
                UnZipJAR unZipJAR = new UnZipJAR();
                String serviceStatus = "";
                switch (type) {
                    case SERVICE:
                        try {

                            AxisService service = new AxisService();
                            unZipJAR.unzipService(currentFileItem.getAbsolutePath(), this, service);
                            addnewService(service);
                            log.info("Deployement WS Name  " + currentFileItem.getName());
                        } catch (DeploymentException de) {
                            log.info("Invalid service" + currentFileItem.getName());
                            log.info("DeploymentException  " + de);
                            serviceStatus = "Error:\n" + de.getMessage();
                        } catch (AxisFault axisFault) {
                            log.info("Invalid service" + currentFileItem.getName());
                            log.info("AxisFault  " + axisFault);
                            serviceStatus = "Error:\n" + axisFault.getMessage();
                        } catch (Exception e) {
                            log.info("Invalid service" + currentFileItem.getName());
                            log.info("Exception  " + e);
                            serviceStatus = "Error:\n" + e.getMessage();
                        } finally {
                            if (serviceStatus.startsWith("Error:")) {
                                engineconfig.getFaulytServices().put(getAxisServiceName(currentFileItem.getName()), serviceStatus);
                            }
                            currentFileItem = null;
                        }
                        break;
                    case MODULE:
                        try {
                            AxisModule metaData = new AxisModule();
                            unZipJAR.unzipModule(currentFileItem.getAbsolutePath(), this, metaData);
                            addNewModule(metaData);
                            log.info("Moduel WS Name  " + currentFileItem.getName() + " modulename :" + metaData.getName());
                        } catch (DeploymentException e) {
                            log.info("Invalid module" + currentFileItem.getName());
                            log.info("DeploymentException  " + e);
                        } catch (AxisFault axisFault) {
                            log.info("Invalid module" + currentFileItem.getName());
                            log.info("AxisFault  " + axisFault);
                        } finally {
                            currentFileItem = null;
                        }
                        break;

                }
            }
        }
        wsToDeploy.clear();
    }

    public void unDeploy() {
        //todo complete this
        String serviceName = "";
        try {
            if (wsToUnDeploy.size() > 0) {
                for (int i = 0; i < wsToUnDeploy.size(); i++) {
                    WSInfo wsInfo = (WSInfo) wsToUnDeploy.get(i);
                    if (wsInfo.getType() == SERVICE) {
                        serviceName = getAxisServiceName(wsInfo.getFilename());
                        engineconfig.removeService(new QName(serviceName));
                        log.info("UnDeployement WS Name  " + wsInfo.getFilename());
                    }
                    engineconfig.getFaulytServices().remove(serviceName);
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

    /* public AxisService deployService(ClassLoader classLoder, InputStream serviceStream, String servieName) throws DeploymentException {
    AxisService service = null;
    try {
    currentFileItem = new HDFileItem(SERVICE, servieName);
    currentFileItem.setClassLoader(classLoder);
    service = new AxisService();
    DeploymentParser schme = new DeploymentParser(serviceStream, this);
    schme.parseServiceXML(service);
    service = getRunnableService(service);
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
     * @param axisService
     * @param serviceInputStream
     * @param classLoader
     * @return
     * @throws DeploymentException
     */
    public ServiceContext buildService(AxisService axisService , InputStream serviceInputStream, ClassLoader classLoader) throws DeploymentException {
        ServiceContext serviceContext;
        try {
            DeploymentParser schme = new DeploymentParser(serviceInputStream, this);
            schme.parseServiceXML(axisService);
            axisService.setClassLoader(classLoader);
            serviceContext = getRunnableService(axisService);
            engineContext.addService(serviceContext);
            engineconfig.addService(axisService);

        } catch (XMLStreamException e) {
            throw new DeploymentException("XMLStreamException" + e.getMessage());
        } catch (DeploymentException e) {
            throw new DeploymentException(e.getMessage()) ;
        } catch (PhaseException e) {
            throw new DeploymentException(e.getMessage());
        } catch (AxisFault axisFault) {
            throw new DeploymentException(axisFault.getMessage());
        }
        return  serviceContext;
    }

}
