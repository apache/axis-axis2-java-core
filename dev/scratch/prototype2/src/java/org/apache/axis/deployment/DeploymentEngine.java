package org.apache.axis.deployment;

import org.apache.axis.deployment.repository.utill.HDFileItem;
import org.apache.axis.deployment.repository.utill.UnZipJAR;
import org.apache.axis.deployment.repository.utill.WSInfo;
import org.apache.axis.deployment.scheduler.DeploymentIterator;
import org.apache.axis.deployment.scheduler.Scheduler;
import org.apache.axis.deployment.scheduler.SchedulerTask;
import org.apache.axis.description.AxisGlobal;
import org.apache.axis.description.AxisModule;
import org.apache.axis.description.Flow;
import org.apache.axis.description.HandlerMetaData;
import org.apache.axis.engine.AxisFault;
import org.apache.axis.engine.EngineRegistry;
import org.apache.axis.engine.Handler;
import org.apache.axis.engine.Provider;
import org.apache.axis.impl.description.AxisService;
import org.apache.axis.impl.engine.EngineRegistryImpl;
import org.apache.axis.phaseresolver.PhaseException;
import org.apache.axis.phaseresolver.PhaseResolver;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Iterator;
import java.util.Vector;
import java.util.ArrayList;
import java.util.List;


/**
 * Copyright 2001-2004 The Apache Software Foundation.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public class DeploymentEngine implements DeploymentConstants {
    private Log log = LogFactory.getLog(getClass());
    private final Scheduler scheduler = new Scheduler();
    /**
     * This will store all the web Services to deploye
     */
    private List wsToDeploy = new ArrayList();
    /**
     * this will store all the web Services to undeploye
     */
    private List wsToUnDeploy = new ArrayList();

    /**
     * to keep a ref to engine register
     * this ref will pass to engine when it call start()
     * method
     */
    private EngineRegistry engineRegistry = null;

    /**
     * this constaructor for the testing
     */

    private String folderName;

    private String serverconfigName;

    /**
     * This to keep a referance to serverMetaData object
     */
    // private static ServerMetaData server = new ServerMetaData();
    private AxisGlobal server;


    private HDFileItem currentFileItem = null;

    /**
     * This the constructor which is used by Engine inorder to start
     * Deploymenat module,
     *
     * @param RepositaryName is the path to which Repositary Listner should
     *                       listent.
     */

    public DeploymentEngine(String RepositaryName) throws DeploymentException {
        this(RepositaryName,"server.xml");
        
    }
    public DeploymentEngine(String RepositaryName, String serverXMLFile) throws DeploymentException {
        this.folderName = RepositaryName;
        File repository = new File(RepositaryName);
        if(!repository.exists()){
            repository.mkdirs();
            File servcies = new File(repository,"services");
            File modules = new File(repository,"modules");
            modules.mkdirs();
            servcies.mkdirs();
        }
        File serverConf = new File(repository,serverXMLFile);
        if(!serverConf.exists()){
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            InputStream in = cl.getResourceAsStream("org/apache/axis/deployment/server.xml");
            if(in != null){
                try {
                    serverConf.createNewFile();
                    FileOutputStream out = new FileOutputStream(serverConf);
                    int BUFSIZE = 512; // since only a test file going to load , the size has selected
                    byte[] buf = new byte[BUFSIZE];
                    int read;
                    while((read = in.read(buf)) > 0){
                        out.write(buf,0,read);
                    }
                    in.close();
                    out.close();
                } catch (IOException e) {
                    throw new DeploymentException(e.getMessage());
                }


            } else{
                throw new DeploymentException("can not found org/apache/axis/deployment/server.xml");

            }
        }
            this.serverconfigName = RepositaryName + "/" + serverXMLFile;
    }

//    public DeploymentEngine(String RepositaryName , String configFileName) {
//        this.folderName = RepositaryName;
//        this.serverconfigName = configFileName;
//    }

    public HDFileItem getCurrentFileItem() {
        return currentFileItem;
    }


    /**
     * tio get ER
     * @return
     */
    public EngineRegistry getEngineRegistry() {
        return engineRegistry;
    }

    /**
     * This method will fill the engine registry and return it to Engine
     *
     * @return
     * @throws AxisFault
     * @throws PhaseException
     */
    public EngineRegistry start() throws AxisFault, PhaseException, DeploymentException, XMLStreamException {
        //String fileName;
        if(serverconfigName == null) {
            throw new DeploymentException("path to Server.xml can not be NUll");
        }
        File tempfile = new File(serverconfigName);
        try {
            InputStream in = new FileInputStream(tempfile);
            engineRegistry = createEngineRegistry();
            DeploymentParser parser = new DeploymentParser(in, this);
            parser.procesServerXML(server);
        } catch (FileNotFoundException e) {
            throw new AxisFault("Exception at deployment",e);
        }

        startSearch(this);
        valideServerModule() ;
        return engineRegistry;
    }

    /**
     * This methode used to check the modules referd by server.xml
     * are exist , or they have deployed
     */
    private void valideServerModule() throws AxisFault, PhaseException{
        Iterator itr= server.getModules().iterator();
        while (itr.hasNext()) {
            QName qName = (QName) itr.next();
            if(getModule(qName) == null ){
                throw new AxisFault(server + " Refer to invalid module " + qName + " has not bean deployed yet !");
            }
        }
        PhaseResolver phaseResolver = new PhaseResolver(engineRegistry);
        phaseResolver.buildGlobalChains(server);
        phaseResolver.buildTranspotsChains();

    }

    public AxisModule getModule(QName moduleName) throws AxisFault {
        AxisModule metaData = engineRegistry.getModule(moduleName);
        return metaData;
    }

    /**
     * this method use to start the Deployment engine
     * inorder to perform Hot deployment and so on..
     */
    private void startSearch(DeploymentEngine engine) {
        scheduler.schedule(new SchedulerTask(engine, folderName), new DeploymentIterator());
    }

    private EngineRegistry createEngineRegistry()  {
        EngineRegistry newEngineRegisty;

        server = new AxisGlobal();
        newEngineRegisty = new EngineRegistryImpl(server);

        return newEngineRegisty;
    }


    private void addnewService(AxisService serviceMetaData) throws AxisFault, PhaseException {

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        loadServiceClass(serviceMetaData,classLoader);

        Flow inflow = serviceMetaData.getInFlow();
        if(inflow != null ){
            addFlowHandlers(inflow,classLoader);
        }

        Flow outFlow = serviceMetaData.getOutFlow();
        if(outFlow != null){
            addFlowHandlers(outFlow,classLoader);
        }

        Flow faultFlow = serviceMetaData.getFaultFlow();
        if(faultFlow != null) {
            addFlowHandlers(faultFlow,classLoader);
        }
        PhaseResolver reolve = new PhaseResolver(engineRegistry,serviceMetaData);
        reolve.buildchains();
        engineRegistry.addService(serviceMetaData);
    }

    private void loadServiceClass(AxisService service, ClassLoader parent) throws AxisFault{
        File file = currentFileItem.getFile();
        Class serviceclass = null;
        URLClassLoader loader1 = null;
        if (file != null) {
            URL[] urlsToLoadFrom = new URL[0];
            try {
                if (!file.exists()) {
                    throw new RuntimeException("file not found !!!!!!!!!!!!!!!");
                }
                urlsToLoadFrom = new URL[]{file.toURL()};
                loader1 = new URLClassLoader(urlsToLoadFrom, parent);
                service.setClassLoader(loader1);
                
                String readInClass = currentFileItem.getClassName();
                
                if(readInClass != null && !"".equals(readInClass)){
                    serviceclass = Class.forName(currentFileItem.getClassName(), true, loader1);
                }
				service.setServiceClass(serviceclass);
				
                String readInProviderName = currentFileItem.getProvideName();
                if(readInProviderName != null && ! "".equals(readInProviderName)){
                    Class provider =Class.forName(currentFileItem.getProvideName(), true, loader1);
                    service.setProvider((Provider)provider.newInstance());
                }
            } catch (MalformedURLException e) {
                throw new AxisFault(e.getMessage(),e);
            } catch (Exception e) {
                throw new AxisFault(e.getMessage(),e);
            }

        }

    }


    private void addFlowHandlers(Flow flow, ClassLoader parent) throws AxisFault {
        int count = flow.getHandlerCount();
        File file = currentFileItem.getFile();
        URLClassLoader loader1 = null;
        if (file != null) {
            URL[] urlsToLoadFrom = new URL[0];
            try {
                if (!file.exists()) {
                    throw new RuntimeException("file not found !!!!!!!!!!!!!!!");
                }
                urlsToLoadFrom = new URL[]{file.toURL()};
            } catch (MalformedURLException e) {
                throw new AxisFault(e.getMessage());
            }
            loader1 = new URLClassLoader(urlsToLoadFrom, parent);
        }

        for (int j = 0; j < count; j++) {
            //todo handle exception in properway
            HandlerMetaData handlermd = flow.getHandler(j);
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


    public Class getHandlerClass(String className, URLClassLoader loader1) throws AxisFault {
        Class handlerClass = null;

        try {
            handlerClass = Class.forName(className, true, loader1);
        } catch (ClassNotFoundException e) {
            throw new AxisFault(e.getMessage());
        }
        return handlerClass;
    }


    private void addNewModule(AxisModule moduelmetada) throws AxisFault {

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        Flow inflow = moduelmetada.getInFlow();
        addFlowHandlers(inflow,classLoader);

        Flow outFlow = moduelmetada.getOutFlow();
        addFlowHandlers(outFlow,classLoader);

        Flow faultFlow = moduelmetada.getFaultFlow();
        addFlowHandlers(faultFlow,classLoader);

        engineRegistry.addMdoule(moduelmetada);
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
                switch (type) {
                    case SERVICE:
                        {
                            try {
//
                                AxisService service = new AxisService();
                                unZipJAR.unzipService(currentFileItem.getAbsolutePath(), this, service);
                                addnewService(service);
                                log.info("Deployement WS Name  " + currentFileItem.getName());
                            }catch (DeploymentException de){
                                throw new RuntimeException(de.getMessage());
                            }catch (AxisFault axisFault) {
                                throw new RuntimeException(axisFault.getMessage());
                            } catch (Exception e){
                                   throw new RuntimeException(e.getMessage());
                            } finally{
                                currentFileItem = null;
                            }
                            break;
                        }
                    case MODULE:
                        {
                            try {
                                AxisModule metaData = new AxisModule();
                                unZipJAR.unzipModule(currentFileItem.getAbsolutePath(), this,metaData);
                                addNewModule(metaData);
                                log.info("Moduel WS Name  " + currentFileItem.getName() + " modulename :" + metaData.getName());
                            } catch (DeploymentException e) {
                                   throw new RuntimeException(e.getMessage());
                            } catch (AxisFault axisFault) {
                                   throw new RuntimeException(axisFault.getMessage());
                            }finally{
                                currentFileItem = null;
                            }
                            break;
                        }

                }
            }
        }
        wsToDeploy.clear();
    }

    public void doUnDeploye() {
        //todo complete this
        if (wsToUnDeploy.size() > 0) {
            for (int i = 0; i < wsToUnDeploy.size(); i++) {
                WSInfo wsInfo = (WSInfo) wsToUnDeploy.get(i);
                log.info("UnDeployement WS Name  " + wsInfo.getFilename());
            }

        }
        wsToUnDeploy.clear();
    }


}
