package org.apache.axis.deployment;

import org.apache.axis.context.MessageContext;
import org.apache.axis.deployment.metadata.ServerMetaData;
import org.apache.axis.deployment.metadata.phaserule.PhaseException;
import org.apache.axis.deployment.repository.utill.HDFileItem;
import org.apache.axis.deployment.repository.utill.UnZipJAR;
import org.apache.axis.deployment.repository.utill.WSInfo;
import org.apache.axis.deployment.scheduler.DeploymentIterator;
import org.apache.axis.deployment.scheduler.Scheduler;
import org.apache.axis.deployment.scheduler.SchedulerTask;
import org.apache.axis.description.*;
import org.apache.axis.engine.AxisFault;
import org.apache.axis.engine.EngineRegistry;
import org.apache.axis.engine.Handler;
import org.apache.axis.impl.description.SimpleAxisServiceImpl;
import org.apache.axis.impl.engine.EngineRegistryImpl;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Vector;


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
 *
 * @author Deepal Jayasinghe
 *         Oct 13, 2004
 *         12:33:17 PM
 */
public class DeploymentEngine implements DeploymentConstants {
    private Log log = LogFactory.getLog(getClass());
    private final Scheduler scheduler = new Scheduler();
    /**
     * This will store all the web Services to deploye
     */
    private Vector wsToDeploy = new Vector();
    /**
     * this will store all the web Services to undeploye
     */
    private Vector wsToUnDeploy = new Vector();

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

    /**
     * This to keep a referance to serverMetaData object
     */
    private static ServerMetaData server = new ServerMetaData();


    private HDFileItem currentFileItem = null;

    /**
     * This the constructor which is used by Engine inorder to start
     * Deploymenat module,
     *
     * @param RepositaryName is the path to which Repositary Listner should
     *                       listent.
     */
    public DeploymentEngine(String RepositaryName) {
        this.folderName = RepositaryName;
    }


    /**
     * This method should use inorder to get the referance to servermetadata
     * object which keep all the detail about all the phases
     *
     * @return
     */
    public static ServerMetaData getServerMetaData() {
        return server;
    }


    /**
     * This method will fill the engine registry and return it to Engine
     *
     * @return
     * @throws AxisFault
     * @throws PhaseException
     */
    public EngineRegistry start() throws AxisFault, PhaseException, DeploymentException, XMLStreamException {
        String fileName = "src/test-resources/deployment/server.xml";
        File tempfile = new File(fileName);
        try {
            InputStream in = new FileInputStream(tempfile);
            DeploymentParser parser = new DeploymentParser(in, this);
            parser.procesServerXML(server);
        } catch (FileNotFoundException e) {
            throw new AxisFault(e.getMessage());
        }
        engineRegistry = createEngineRegistry();
        startSearch(this);
        valideServerModule() ;
        return engineRegistry;
    }

    /**
     * This methode used to check the modules referd by server.xml
     * are exist , or they have deployed
     */
    private void valideServerModule() throws AxisFault{
        int moduleCount = server.getModuleCount();
        QName moduleName;
        for (int i = 0; i < moduleCount ; i++) {
            moduleName = server.getModule(i);
            if(getModule(moduleName) == null ){
                throw new AxisFault(server.getName() + " Refer to invalid module " + moduleName + " has not bean deployed yet !");
            }
        }

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

        AxisGlobal global = new AxisGlobal();
        newEngineRegisty = new EngineRegistryImpl(global);

        return newEngineRegisty;
    }


    private void addnewService(AxisService serviceMetaData) throws AxisFault {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

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

        try {
            Handler handler = (Handler) handlerClass.newInstance();
            MessageContext msgContext = null;
            try {
                handler.invoke(msgContext);
            } catch (AxisFault axisFault) {
                throw new AxisFault(axisFault.getMessage());
            }
        } catch (InstantiationException e) {
            throw new AxisFault(e.getMessage());
        } catch (IllegalAccessException e) {
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

    public void doDeploye() {
        //todo complete this
        if (wsToDeploy.size() > 0) {
            for (int i = 0; i < wsToDeploy.size(); i++) {
                currentFileItem = (HDFileItem) wsToDeploy.elementAt(i);
                int type = currentFileItem.getType();
                UnZipJAR unZipJAR = new UnZipJAR();
                switch (type) {
                    case SERVICE:
                        {
                            //todo implemnt this in right manner
                            AxisService service = new SimpleAxisServiceImpl(null);
                            unZipJAR.unzipService(currentFileItem.getAbsolutePath(), this, service);
                            try {
                                if (service != null) {
                                    addnewService(service);
                                    log.info("Deployement WS Name  " + currentFileItem.getName());
                                    currentFileItem = null;
                                    break;
                                }
                            }catch (AxisFault axisFault) {
                                axisFault.printStackTrace();  //To change body of catch statement use Options | File Templates.
                            }
                        }
                    case MODULE:
                        {
                            AxisModule metaData = new AxisModule();
                            unZipJAR.unzipModule(currentFileItem.getAbsolutePath(), this,metaData);
                            try {
                                if (metaData != null) {
                                    addNewModule(metaData);
                                    log.info("Moduel WS Name  " + currentFileItem.getName() + " modulename :" + metaData.getName());
                                    currentFileItem = null;
                                    break;
                                }
                            } catch (AxisFault axisFault) {
                                axisFault.printStackTrace();  //To change body of catch statement use Options | File Templates.
                            }
                        }
                }
            }
        }
        wsToDeploy.removeAllElements();
    }

    public void doUnDeploye() {
        //todo complete this
        if (wsToUnDeploy.size() > 0) {
            for (int i = 0; i < wsToUnDeploy.size(); i++) {
                WSInfo wsInfo = (WSInfo) wsToUnDeploy.elementAt(i);
                log.info("UnDeployement WS Name  " + wsInfo.getFilename());
            }

        }
        wsToUnDeploy.removeAllElements();
    }


}
