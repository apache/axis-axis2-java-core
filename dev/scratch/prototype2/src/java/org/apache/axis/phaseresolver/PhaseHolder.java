package org.apache.axis.phaseresolver;

import java.util.ArrayList;
import java.util.Vector;

import org.apache.axis.deployment.DeploymentConstants;
import org.apache.axis.description.AxisGlobal;
import org.apache.axis.description.AxisService;
import org.apache.axis.description.AxisTransport;
import org.apache.axis.description.HandlerMetaData;
import org.apache.axis.engine.AxisFault;
import org.apache.axis.engine.EngineRegistry;
import org.apache.axis.engine.Handler;
import org.apache.axis.engine.Phase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
 */


/**
 * This class hold all the phases found in the service.xml and server.xml
 */
public class PhaseHolder implements DeploymentConstants {

    private Log log = LogFactory.getLog(getClass());
    private Vector phaseholder = new Vector();

    /**
     * Referance to ServerMetaData inorder to get information about phases.
     */
    private EngineRegistry registry;// = new  ServerMetaData();
    private AxisService service;


    public PhaseHolder(EngineRegistry registry,AxisService serviceIN) {
        this.registry = registry;
        this.service = serviceIN;
    }

    private boolean isPhaseExist(String phaseName) {
        for (int i = 0; i < phaseholder.size(); i++) {
            PhaseMetaData phase = (PhaseMetaData) phaseholder.elementAt(i);
            if (phase.getName().equals(phaseName)) {
                return true;
            }

        }
        return false;
    }

    private boolean isPhaseExistinER(String phaseName){
        ArrayList pahselist = registry.getPhases();
        for (int i = 0; i < pahselist.size(); i++) {
            String pahse = (String) pahselist.get(i);
            if(pahse.equals(phaseName)){
                return true;
            }
        }
        return false;
    }

    public void addHandler(HandlerMetaData handler) throws PhaseException {
        String phaseName = handler.getRules().getPhaseName();

        if (isPhaseExist(phaseName)) {
            getPhase(phaseName).addHandler(handler);
        } else {
            if (isPhaseExistinER(phaseName)) {
                PhaseMetaData newpPhase = new PhaseMetaData(phaseName);
                addPhase(newpPhase);
                newpPhase.addHandler(handler);
            } else {
                throw new PhaseException("Invalid Phase ," + phaseName + "for the handler " + handler.getName()    + " dose not exit in server.xml");
            }

        }
    }

    private void addPhase(PhaseMetaData phase) {
        phaseholder.add(phase);
    }

    private PhaseMetaData getPhase(String phaseName) {
        for (int i = 0; i < phaseholder.size(); i++) {
            PhaseMetaData phase = (PhaseMetaData) phaseholder.elementAt(i);
            if (phase.getName().equals(phaseName)) {
                return phase;
            }

        }
        return null;
    }

    private  void OrderdPhases() {
        //todo complet this using phaseordeer
        PhaseMetaData[] phase = new PhaseMetaData[phaseholder.size()];
        for (int i = 0; i < phaseholder.size(); i++) {
            PhaseMetaData tempphase = (PhaseMetaData) phaseholder.elementAt(i);
            phase[i] = tempphase;
        }
        phase = getOrderPhases(phase);
        // remove all items inorder to rearrange them
        phaseholder.removeAllElements();

        for (int i = 0; i < phase.length; i++) {
            PhaseMetaData phaseMetaData = phase[i];
            phaseholder.add(phaseMetaData);

        }
    }


    private PhaseMetaData[] getOrderPhases(PhaseMetaData[] phasesmetadats) {
        PhaseMetaData[] temppahse = new PhaseMetaData[phasesmetadats.length];
        int count = 0;
        ArrayList pahselist = registry.getPhases();
        for (int i = 0; i < pahselist.size(); i++) {
            String phasemetadata = (String) pahselist.get(i);
            for (int j = 0; j < phasesmetadats.length; j++) {
                PhaseMetaData tempmetadata = phasesmetadats[j];
                if (tempmetadata.getName().equals(phasemetadata)) {
                    temppahse[count] = tempmetadata;
                    count++;
                }
            }


        }
        return temppahse;
    }


    /**
     * cahinType
     *  1 : inFlowExcChain
     *  2 : OutFlowExeChain
     *  3 : FaultFlowExcechain
     * @param chainType
     * @throws org.apache.axis.phaseresolver.PhaseException
     */
    public  void getOrderdHandlers(int chainType) throws PhaseException {
        try {
            OrderdPhases();
            Vector tempHander = new Vector();
            HandlerMetaData[] handlers;

            switch (chainType) {
                case 1 : {
                    ArrayList inChain =  new ArrayList();//       service.getExecutableInChain();
                    for (int i = 0; i < phaseholder.size(); i++) {
                        PhaseMetaData phase = (PhaseMetaData) phaseholder.elementAt(i);
                        Phase axisPhase = new Phase(phase.getName());
                        handlers = phase.getOrderedHandlers();
                        for (int j = 0; j < handlers.length; j++) {
                            axisPhase.addHandler(handlers[j].getHandler());
                        }
                        inChain.add(axisPhase);
                    }
                    service.setPhases(inChain,EngineRegistry.INFLOW);
                    break;
                }
                case 2 : {
                    ArrayList outChain =new ArrayList();// service.getExecutableOutChain();
                    for (int i = 0; i < phaseholder.size(); i++) {
                        PhaseMetaData phase = (PhaseMetaData) phaseholder.elementAt(i);
                        Phase axisPhase = new Phase(phase.getName());
                        handlers = phase.getOrderedHandlers();
                        for (int j = 0; j < handlers.length; j++) {
                            axisPhase.addHandler(handlers[j].getHandler());
                        }
                        outChain.add(axisPhase);
                    }
                    service.setPhases(outChain,EngineRegistry.OUTFLOW);
                    break;
                }
                case 3 : {
                    ArrayList faultChain = new ArrayList();//service.getExecutableFaultChain();
                    for (int i = 0; i < phaseholder.size(); i++) {
                        PhaseMetaData phase = (PhaseMetaData) phaseholder.elementAt(i);
                        Phase axisPhase = new Phase(phase.getName());
                        handlers = phase.getOrderedHandlers();
                        for (int j = 0; j < handlers.length; j++) {
                            axisPhase.addHandler(handlers[j].getHandler());
                        }
                        faultChain.add(axisPhase);
                    }
                    service.setPhases(faultChain,EngineRegistry.FAULTFLOW);
                    break;
                }
            }
        } catch (AxisFault e) {
            throw new PhaseException(e);
        } 
    }

    public void buildTransportChain(AxisTransport trnsport , int chainType) throws PhaseException {
        try{
            OrderdPhases();
            Vector tempHander = new Vector();
            HandlerMetaData[] handlers;
            Class handlerClass = null;
            Handler handler;
            switch (chainType) {
                case 1 : {
                    ArrayList inChain =  new ArrayList();//       service.getExecutableInChain();
                    for (int i = 0; i < phaseholder.size(); i++) {
                        PhaseMetaData phase = (PhaseMetaData) phaseholder.elementAt(i);
                        Phase axisPhase = new Phase(phase.getName());
                        handlers = phase.getOrderedHandlers();
                        for (int j = 0; j < handlers.length; j++) {
                            try{
                                handlerClass = Class.forName(handlers[j].getClassName(), true, Thread.currentThread().getContextClassLoader());//getHandlerClass(handlermd.getClassName(), loader1);
                                handler = (Handler) handlerClass.newInstance();
                                handler.init(handlers[j]);
                                handlers[j].setHandler(handler);
                                axisPhase.addHandler(handlers[j].getHandler());
                            }catch (ClassNotFoundException e){
                                throw new PhaseException(e);
                            } catch (IllegalAccessException e) {
                                throw new PhaseException(e);
                            } catch (InstantiationException e) {
                                throw new PhaseException(e);
                            }
                        }
                        inChain.add(axisPhase);
                    }
                    trnsport.setPhases(inChain,EngineRegistry.INFLOW);
                    break;
                }
                case 2 : {
                    ArrayList outChain =new ArrayList();// service.getExecutableOutChain();
                    for (int i = 0; i < phaseholder.size(); i++) {
                        PhaseMetaData phase = (PhaseMetaData) phaseholder.elementAt(i);
                        Phase axisPhase = new Phase(phase.getName());
                        handlers = phase.getOrderedHandlers();
                        for (int j = 0; j < handlers.length; j++) {
                            try{
                                handlerClass = Class.forName(handlers[j].getClassName(), true, Thread.currentThread().getContextClassLoader());//getHandlerClass(handlermd.getClassName(), loader1);
                                handler = (Handler) handlerClass.newInstance();
                                handler.init(handlers[j]);
                                handlers[j].setHandler(handler);
                                axisPhase.addHandler(handlers[j].getHandler());
                            }catch (ClassNotFoundException e){
                                throw new PhaseException(e);
                            } catch (IllegalAccessException e) {
                                throw new PhaseException(e);
                            } catch (InstantiationException e) {
                                throw new PhaseException(e);
                            }
                        }
                        outChain.add(axisPhase);
                    }
                    trnsport.setPhases(outChain,EngineRegistry.OUTFLOW);
                    break;
                }
                case 3 : {
                    ArrayList faultChain = new ArrayList();//service.getExecutableFaultChain();
                    for (int i = 0; i < phaseholder.size(); i++) {
                        PhaseMetaData phase = (PhaseMetaData) phaseholder.elementAt(i);
                        Phase axisPhase = new Phase(phase.getName());
                        handlers = phase.getOrderedHandlers();
                        for (int j = 0; j < handlers.length; j++) {
                            try{
                                handlerClass = Class.forName(handlers[j].getClassName(), true, Thread.currentThread().getContextClassLoader());//getHandlerClass(handlermd.getClassName(), loader1);
                                handler = (Handler) handlerClass.newInstance();
                                handler.init(handlers[j]);
                                handlers[j].setHandler(handler);
                                axisPhase.addHandler(handlers[j].getHandler());
                            }catch (ClassNotFoundException e){
                                throw new PhaseException(e);
                            } catch (IllegalAccessException e) {
                                throw new PhaseException(e);
                            } catch (InstantiationException e) {
                                throw new PhaseException(e);
                            }
                        }
                        faultChain.add(axisPhase);
                    }
                    trnsport.setPhases(faultChain,EngineRegistry.FAULTFLOW);
                    break;
                }
            }
        }   catch (AxisFault e) {
            throw new PhaseException(e);
        }
    }

    public  void buildGoblalChain(AxisGlobal axisGlobal, int chainType) throws PhaseException {
        try {
            OrderdPhases();
            Vector tempHander = new Vector();
            HandlerMetaData[] handlers;

            switch (chainType) {
                case 1 : {
                    ArrayList inChain =  new ArrayList();//       service.getExecutableInChain();
                    for (int i = 0; i < phaseholder.size(); i++) {
                        PhaseMetaData phase = (PhaseMetaData) phaseholder.elementAt(i);
                        Phase axisPhase = new Phase(phase.getName());
                        handlers = phase.getOrderedHandlers();
                        for (int j = 0; j < handlers.length; j++) {
                            axisPhase.addHandler(handlers[j].getHandler());
                        }
                        inChain.add(axisPhase);
                    }
                    axisGlobal.setPhases(inChain,EngineRegistry.INFLOW);
                    break;
                }
                case 2 : {
                    ArrayList outChain =new ArrayList();// service.getExecutableOutChain();
                    for (int i = 0; i < phaseholder.size(); i++) {
                        PhaseMetaData phase = (PhaseMetaData) phaseholder.elementAt(i);
                        Phase axisPhase = new Phase(phase.getName());
                        handlers = phase.getOrderedHandlers();
                        for (int j = 0; j < handlers.length; j++) {
                            axisPhase.addHandler(handlers[j].getHandler());
                        }
                        outChain.add(axisPhase);
                    }
                    axisGlobal.setPhases(outChain,EngineRegistry.OUTFLOW);
                    break;
                }
                case 3 : {
                    ArrayList faultChain = new ArrayList();//service.getExecutableFaultChain();
                    for (int i = 0; i < phaseholder.size(); i++) {
                        PhaseMetaData phase = (PhaseMetaData) phaseholder.elementAt(i);
                        Phase axisPhase = new Phase(phase.getName());
                        handlers = phase.getOrderedHandlers();
                        for (int j = 0; j < handlers.length; j++) {
                            axisPhase.addHandler(handlers[j].getHandler());
                        }
                        faultChain.add(axisPhase);
                    }
                    axisGlobal.setPhases(faultChain,EngineRegistry.FAULTFLOW);
                    break;
                }
            }
        } catch (AxisFault e) {
            throw new PhaseException(e);
        }
    }


}
