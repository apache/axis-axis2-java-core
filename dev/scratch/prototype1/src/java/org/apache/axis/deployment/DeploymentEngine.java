package org.apache.axis.deployment;

import org.apache.axis.deployment.MetaData.*;
import org.apache.axis.deployment.MetaData.phaserule.PhaseException;
import org.apache.axis.deployment.fileloader.utill.HDFileItem;
import org.apache.axis.deployment.fileloader.utill.UnZipJAR;
import org.apache.axis.deployment.fileloader.utill.WSInfo;
import org.apache.axis.deployment.scheduler.DeploymentIterator;
import org.apache.axis.deployment.scheduler.Scheduler;
import org.apache.axis.deployment.scheduler.SchedulerTask;
import org.apache.axis.deployment.schemaparser.SchemaParser;
import org.apache.axis.engine.*;
import org.apache.axis.engine.exec.Constants;
import org.apache.axis.engine.exec.ExecutionChain;
import org.apache.axis.engine.exec.Phase;
import org.apache.axis.engine.registry.*;
import org.apache.axis.providers.SimpleJavaProvider;
import org.apache.axis.providers.SyncProvider;

import javax.xml.namespace.QName;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
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
 *
 */
public class DeploymentEngine implements DeployCons {

    private final Scheduler scheduler = new Scheduler();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy HH:mm:ss.SSS");
    private final int hourOfDay, minute, second;
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

    private Vector servicelist = new Vector();
    /**
     * this constaructor for the testing
     */

    private String folderName;

    /**
     * This the constructor which is used by Engine inorder to start
     * Deploymenat module,
     * @param RepositaryName is the path to which Repositary Listner should
     * listent.
     */
    public DeploymentEngine(String RepositaryName){
        Date date = new Date();
        this.hourOfDay = date.getHours();
        this.minute = date.getMinutes();
        this.second = date.getSeconds();
        this.folderName = RepositaryName;
    }

    /**
     * This method will fill the engine registry and return it to Engine
     * @return
     * @throws AxisFault
     * @throws PhaseException
     */
    public EngineRegistry start() throws AxisFault, PhaseException {
        engineRegistry = createEngineRegistry();
        startSearch(this);
        return engineRegistry;
    }


    public DeploymentEngine() {
        Date date = new Date();
        this.hourOfDay = date.getHours();
        this.minute = date.getMinutes();
        this.second = date.getSeconds();
    }

    public void addService(ServiceMetaData serviceMetaData) {
        servicelist.add(serviceMetaData);
        try {
            addnewService(serviceMetaData);
             System.out.println("Numbetr of service" + engineRegistry.getServiceCount());
        } catch (AxisFault axisFault) {
            axisFault.printStackTrace();  //To change body of catch statement use Options | File Templates.
        } catch (PhaseException e) {
            e.printStackTrace();  //To change body of catch statement use Options | File Templates.
        }
    }

    /**
     * this method use to start the Deployment engine
     * inorder to perform Hot deployment and so on..
     *
     */
    private void startSearch(DeploymentEngine engine) {
        scheduler.schedule(new SchedulerTask(engine, folderName), new DeploymentIterator(hourOfDay, minute, second));
    }

    private EngineRegistry createEngineRegistry() throws AxisFault {
        EngineRegistry newEngineRegisty ;
        //todo the below line dse not need ask from srinath why
        // do we need to have this line
        QName transportName = new QName("Transport");

        /**
         * adding Global
         */
        ConcreateFlow globalinflow = new ConcreateFlow();
        ConcreateFlow globaloutflow = new ConcreateFlow();
        ConcreateFlow globalfaultflow = new ConcreateFlow();

        Global global = new SimpleGlobal();
        global.setInFlow(globalinflow);
        global.setOutFlow(globaloutflow);
        global.setFaultFlow(globalfaultflow);
        newEngineRegisty = new SimpleEngineRegistry(global);
        /**
         * adding transport
         */
        ConcreateFlow transportinflow = new ConcreateFlow();
        ConcreateFlow transportoutflow = new ConcreateFlow();
        ConcreateFlow transportfaultflow = new ConcreateFlow();

        Transport transport = new SimpleTransport(transportName);
        transport.setInFlow(transportinflow);
        transport.setOutFlow(transportoutflow);
        transport.setFaultFlow(transportfaultflow);
        newEngineRegisty.addTransport(transport);

        /**
         * adding services
         */
        /*   for (int i = 0; i < servicelist.size(); i++) {
        ServiceMetaData serviceMetaData = (ServiceMetaData) servicelist.elementAt(i);
        addnewService(serviceMetaData);
        } */
        return newEngineRegisty;
    }


    private void addnewService(ServiceMetaData serviceMetaData) throws AxisFault, PhaseException {
        QName serviceName = new QName(serviceMetaData.getName());
        int count = 0;

        ConcreateFlow serviceinflow = new ConcreateFlow();
        ConcreateFlow serviceoutflow = new ConcreateFlow();
        ConcreateFlow servicefaultflow = new ConcreateFlow();

        Service service = new SimpleService(serviceName);
        service.setInFlow(serviceinflow);
        service.setOutFlow(serviceoutflow);
        service.setFaultFlow(servicefaultflow);
        service.setClassLoader(Thread.currentThread().getContextClassLoader());


        ClassLoader serviceClassLoader = service.getClassLoader();
        /**
         * ****************************************************************************
         * ****************************************************************************
         * Adding service inflow detail
         */
        count = serviceMetaData.getInFlow().getHandlercount();
        addFlowHandlers(serviceinflow, count, serviceMetaData.getInFlow(), serviceClassLoader);

        /**
         * ****************************************************************************
         * ****************************************************************************
         * Adding service outflow detail
         */
        count = serviceMetaData.getOutFlow().getHandlercount();
        addFlowHandlers(serviceoutflow, count, serviceMetaData.getOutFlow(), serviceClassLoader);

        /**
         * ****************************************************************************
         * ****************************************************************************
         * Adding service fault detail
         */
        count = serviceMetaData.getFaultFlow().getHandlercount();
        addFlowHandlers(servicefaultflow, count, serviceMetaData.getFaultFlow(), serviceClassLoader);

        /**
         * ****************************************************************************
         * ****************************************************************************
         * Adding service parameters
         */
        count = serviceMetaData.getParametercount();
        for (int j = 0; j < count; j++) {
            ParameterMetaData paraMD = serviceMetaData.getParameter(j);
            Parameter parameter = new ConcreateParameter(paraMD.getName(), paraMD.getElement());
            service.addParameter(parameter);
        }

        /**
         * ****************************************************************************
         * ****************************************************************************
         * setting  service provider
         */
        service.setProvider(new SyncProvider(new SimpleJavaProvider()));

        //todo Module should come inside operation not service (Srinath fix that :) )

        OperationMetaData oprationmd = serviceMetaData.getOperation();
        ModuleMetaData modulemd = oprationmd.getModule();

        /**
         * adding parametrs to module
         */
        Module module = new SimpleModule(new QName(modulemd.getRef()));
        service.addModule(module);
        count = modulemd.getParameterCount();

        for (int j = 0; j < count; j++) {
            ParameterMetaData paraMD = modulemd.getParameter(j);
            Parameter parameter = new ConcreateParameter(paraMD.getName(), paraMD.getElement());
            module.addParameter(parameter);
        }

        ConcreateFlow operationinflow = new ConcreateFlow();
        count = oprationmd.getInFlow().getHandlercount();
        addFlowHandlers(operationinflow, count, oprationmd.getInFlow(), serviceClassLoader);

        ConcreateFlow operationutflow = new ConcreateFlow();
        count = oprationmd.getOutFlow().getHandlercount();
        addFlowHandlers(operationutflow, count, oprationmd.getOutFlow(), serviceClassLoader);

        ConcreateFlow operationfaultflow = new ConcreateFlow();
        count = oprationmd.getFaultFlow().getHandlercount();
        addFlowHandlers(operationfaultflow, count, oprationmd.getFaultFlow(), serviceClassLoader);


        QName opname = new QName(oprationmd.getName());
        Operation operation = new SimpleOperation(opname, service);
        operation.setInFlow(operationinflow);
        operation.setOutFlow(operationutflow);
        operation.setFaultFlow(operationfaultflow);


        service.addOperation(operation);


        ExecutionChain inchain = new ExecutionChain();
        inchain.addPhase(new Phase(Constants.PHASE_TRANSPORT));
        inchain.addPhase(new Phase(Constants.PHASE_GLOBAL));

        /**
         * todo in this implematation all the handers in the servcie
         * i have asume as one phase bt that is not the case
         * I have to modify that getting all the pahse and
         * accooding tp that i have to create phases
         */
        Phase inservicephase = new Phase(Constants.PHASE_SERVICE);
        HandlerMetaData[] handlerMetaDatas = serviceMetaData.getFlowHandlers(INFLOWST);
        for (int i = 0; i < handlerMetaDatas.length; i++) {
            HandlerMetaData handlerMetaData = handlerMetaDatas[i];
            Handler handler = castHanderMetaData(handlerMetaData, serviceClassLoader);
            handler.setName(new QName(handlerMetaData.getName()));
            inservicephase.addHandler(handler);
        }
        inchain.addPhase(inservicephase);


        ExecutionChain outchain = new ExecutionChain();
        outchain.addPhase(new Phase(Constants.PHASE_TRANSPORT));
        outchain.addPhase(new Phase(Constants.PHASE_GLOBAL));

        Phase outservicephase = new Phase(Constants.PHASE_SERVICE);
        handlerMetaDatas = serviceMetaData.getFlowHandlers(OUTFLOWST);
        for (int i = 0; i < handlerMetaDatas.length; i++) {
            HandlerMetaData handlerMetaData = handlerMetaDatas[i];
            Handler handler = castHanderMetaData(handlerMetaData, serviceClassLoader);
            handler.setName(new QName(handlerMetaData.getName()));
            outservicephase.addHandler(handler);
        }
        outchain.addPhase(outservicephase);


        ExecutionChain faultchain = new ExecutionChain();
        faultchain.addPhase(new Phase(Constants.PHASE_TRANSPORT));
        faultchain.addPhase(new Phase(Constants.PHASE_GLOBAL));

        Phase faultservicephase = new Phase(Constants.PHASE_SERVICE);
        handlerMetaDatas = serviceMetaData.getFlowHandlers(FAILTFLOWST);
        for (int i = 0; i < handlerMetaDatas.length; i++) {
            HandlerMetaData handlerMetaData = handlerMetaDatas[i];
            Handler handler = castHanderMetaData(handlerMetaData, serviceClassLoader);
            handler.setName(new QName(handlerMetaData.getName()));
            faultservicephase.addHandler(handler);
        }
        outchain.addPhase(faultservicephase);

        service.setInputExecutionChain(inchain);
        service.setOutExecutionChain(outchain);
        service.setFaultExecutionChain(faultchain);

        engineRegistry.addService(service);
    }


    /**
     * this method used to add handlers to given Flow
     * @param flow
     * @param count
     */
    private void addFlowHandlers(Flow flow, int count, FlowMetaData flowmetadata, ClassLoader serviceClassLoader) throws AxisFault {
        for (int j = 0; j < count; j++) {
            //todo handle exception in properway
            HandlerMetaData handlermd = flowmetadata.getHandler(j);
            Class handlerClass = null;
            Handler handler;
            try {
                handlerClass = Class.forName(handlermd.getClassName(), true, serviceClassLoader);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();  //To change body of catch statement use Options | File Templates.
                throw new AxisFault();
            }
            try {
                handler = (Handler)handlerClass.newInstance();
            } catch (InstantiationException e) {
                e.printStackTrace();  //To change body of catch statement use Options | File Templates.
                throw new AxisFault();
            } catch (IllegalAccessException e) {
                e.printStackTrace();  //To change body of catch statement use Options | File Templates.
                throw new AxisFault();
            }
            handler.setName(new QName(handlermd.getName()));
            int paracount = handlermd.getParacount();
            for (int k = 0; k < paracount; k++) {
                ParameterMetaData paraMD = handlermd.getParameter(k);
                //todo check with srinath whether this is correct
                Parameter parameter = new ConcreateParameter(paraMD.getName(), paraMD.getElement());
                handler.addParameter(parameter);
            }
            flow.addHandler(handler);
        }
    }

    /**
     * this method used to cast Hander Metta data into hander
     * @param handlerMetaData
     * @param serviceClassLoader
     * @return
     * @throws AxisFault
     */
    private Handler castHanderMetaData(HandlerMetaData handlerMetaData, ClassLoader serviceClassLoader) throws AxisFault {
        Class handlerClass = null;
        Handler handler;
        try {
            handlerClass = Class.forName(handlerMetaData.getClassName(), true, serviceClassLoader);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();  //To change body of catch statement use Options | File Templates.
            throw new AxisFault();
        }
        try {
            handler = (Handler) handlerClass.newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();  //To change body of catch statement use Options | File Templates.
            throw new AxisFault();
        } catch (IllegalAccessException e) {
            e.printStackTrace();  //To change body of catch statement use Options | File Templates.
            throw new AxisFault();
        }
        return handler;
    }


    /**
     *
     * @param file
     */
    public void addtowsToDeploy(HDFileItem file) {
        wsToDeploy.add(file);
    }

    /**
     *
     * @param file
     */
    public void addtowstoUnDeploy(WSInfo file) {
        wsToUnDeploy.add(file);
    }

    public void doDeploye() {
        //todo complete this
        if (wsToDeploy.size() > 0) {
            for (int i = 0; i < wsToDeploy.size(); i++) {
                HDFileItem fileItem = (HDFileItem) wsToDeploy.elementAt(i);
                UnZipJAR unZipJAR = new UnZipJAR();
                ServiceMetaData service = new ServiceMetaData();
                service = unZipJAR.unzip(fileItem.getAbsolutePath(), this);
                addService(service);
                System.out.println("\nDeployement WS Name  " + fileItem.getName());
            }
        }
        wsToDeploy.removeAllElements();
    }

    public void doUnDeploye() {
        //todo complete this
        if (wsToUnDeploy.size() > 0) {
            for (int i = 0; i < wsToUnDeploy.size(); i++) {
                WSInfo wsInfo = (WSInfo) wsToUnDeploy.elementAt(i);
                System.out.println("UnDeployement WS Name  " + wsInfo.getFilename());
            }

        }
        wsToUnDeploy.removeAllElements();
    }


}
