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
package org.apache.axis.phaseresolver;

import org.apache.axis.description.*;
import org.apache.axis.engine.AxisFault;
import org.apache.axis.engine.EngineRegistry;
import org.apache.axis.engine.Handler;
import org.apache.axis.engine.Phase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;

/**
 * This class hold all the phases found in the service.xml and server.xml
 */
public class PhaseHolder {
    /**
     * Field log
     */
    private Log log = LogFactory.getLog(getClass());

    /**
     * Field phaseholder
     */
    private ArrayList phaseholder = new ArrayList();

    /**
     * Referance to ServerMetaData inorder to get information about phases.
     */
    private final EngineRegistry registry;    // = new  ServerMetaData();

    /**
     * Field service
     */
    private final AxisService service;

    /**
     * Constructor PhaseHolder
     *
     * @param registry
     * @param serviceIN
     */
    public PhaseHolder(EngineRegistry registry, AxisService serviceIN) {
        this.registry = registry;
        this.service = serviceIN;
    }

    /**
     * Method isPhaseExist
     *
     * @param phaseName
     * @return
     */
    private boolean isPhaseExist(String phaseName) {
        for (int i = 0; i < phaseholder.size(); i++) {
            PhaseMetadata phase = (PhaseMetadata) phaseholder.get(i);
            if (phase.getName().equals(phaseName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Method isPhaseExistinER
     *
     * @param phaseName
     * @return
     */
    private boolean isPhaseExistinER(String phaseName) {
        ArrayList pahselist = registry.getPhases();
        for (int i = 0; i < pahselist.size(); i++) {
            String pahse = (String) pahselist.get(i);
            if (pahse.equals(phaseName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Method addHandler
     *
     * @param handler
     * @throws PhaseException
     */
    public void addHandler(HandlerMetadata handler) throws PhaseException {
        String phaseName = handler.getRules().getPhaseName();
        if (isPhaseExist(phaseName)) {
            getPhase(phaseName).addHandler(handler);
        } else {
            if (isPhaseExistinER(phaseName)) {
                PhaseMetadata newpPhase = new PhaseMetadata(phaseName);
                addPhase(newpPhase);
                newpPhase.addHandler(handler);
            } else {
                throw new PhaseException("Invalid Phase ," + phaseName
                        + "for the handler "
                        + handler.getName()
                        + " dose not exit in server.xml");
            }
        }
    }

    /**
     * Method addPhase
     *
     * @param phase
     */
    private void addPhase(PhaseMetadata phase) {
        phaseholder.add(phase);
    }

    /**
     * Method getPhase
     *
     * @param phaseName
     * @return
     */
    private PhaseMetadata getPhase(String phaseName) {
        for (int i = 0; i < phaseholder.size(); i++) {
            PhaseMetadata phase = (PhaseMetadata) phaseholder.get(i);
            if (phase.getName().equals(phaseName)) {
                return phase;
            }
        }
        return null;
    }

    /**
     * Method OrderThePhases
     */
    private void OrderThePhases() {
        // todo complete this using phaseorder
        PhaseMetadata[] phase = new PhaseMetadata[phaseholder.size()];
        for (int i = 0; i < phaseholder.size(); i++) {
            PhaseMetadata tempphase = (PhaseMetadata) phaseholder.get(i);
            phase[i] = tempphase;
        }
        phase = getOrderPhases(phase);

        // remove all items inorder to rearrange them
        phaseholder.clear();
        for (int i = 0; i < phase.length; i++) {
            PhaseMetadata phaseMetaData = phase[i];
            phaseholder.add(phaseMetaData);
        }
    }

    /**
     * Method getOrderPhases
     *
     * @param phasesmetadats
     * @return
     */
    private PhaseMetadata[] getOrderPhases(PhaseMetadata[] phasesmetadats) {
        PhaseMetadata[] tempphase = new PhaseMetadata[phasesmetadats.length];
        int count = 0;
        ArrayList pahselist = registry.getPhases();
        for (int i = 0; i < pahselist.size(); i++) {
            String phasemetadata = (String) pahselist.get(i);
            for (int j = 0; j < phasesmetadats.length; j++) {
                PhaseMetadata tempmetadata = phasesmetadats[j];
                if (tempmetadata.getName().equals(phasemetadata)) {
                    tempphase[count] = tempmetadata;
                    count++;
                }
            }
        }
        return tempphase;
    }

    public ArrayList getOrderHandler() throws PhaseException {
        ArrayList handlerList = new ArrayList();
        OrderThePhases();
        HandlerMetadata[] handlers;
        for (int i = 0; i < phaseholder.size(); i++) {
            PhaseMetadata phase =
                    (PhaseMetadata) phaseholder.get(i);
            handlers = phase.getOrderedHandlers();
            for (int j = 0; j < handlers.length; j++) {
                handlerList.add(handlers[j]);
            }

        }
        return  handlerList;
    }


    /**
     * chainType
     * 1 : inFlowExcChain
     * 2 : OutFlowExeChain
     * 3 : FaultFlowExcechain
     *
     * @param chainType
     * @throws org.apache.axis.phaseresolver.PhaseException
     *
     * @throws PhaseException
     */
    public void getOrderedHandlers(int chainType) throws PhaseException {
        try {
            OrderThePhases();

            HandlerMetadata[] handlers;
            switch (chainType) {
                case 1:
                    {
                        ArrayList inChain = new ArrayList();
                        for (int i = 0; i < phaseholder.size(); i++) {
                            PhaseMetadata phase =
                                    (PhaseMetadata) phaseholder.get(i);
                            Phase axisPhase = new Phase(phase.getName());
                            handlers = phase.getOrderedHandlers();
                            for (int j = 0; j < handlers.length; j++) {
                                axisPhase.addHandler(handlers[j].getHandler());
                            }
                            inChain.add(axisPhase);
                        }
                        service.setPhases(inChain, EngineRegistry.INFLOW);
                        break;
                    }
                case 2:
                    {
                        ArrayList outChain = new ArrayList();
                        for (int i = 0; i < phaseholder.size(); i++) {
                            PhaseMetadata phase =
                                    (PhaseMetadata) phaseholder.get(i);
                            Phase axisPhase = new Phase(phase.getName());
                            handlers = phase.getOrderedHandlers();
                            for (int j = 0; j < handlers.length; j++) {
                                axisPhase.addHandler(handlers[j].getHandler());
                            }
                            outChain.add(axisPhase);
                        }
                        service.setPhases(outChain, EngineRegistry.OUTFLOW);
                        break;
                    }
                case 3:
                    {
                        ArrayList faultChain = new ArrayList();  
                        for (int i = 0; i < phaseholder.size(); i++) {
                            PhaseMetadata phase =
                                    (PhaseMetadata) phaseholder.get(i);
                            Phase axisPhase = new Phase(phase.getName());
                            handlers = phase.getOrderedHandlers();
                            for (int j = 0; j < handlers.length; j++) {
                                axisPhase.addHandler(handlers[j].getHandler());
                            }
                            faultChain.add(axisPhase);
                        }
                        service.setPhases(faultChain, EngineRegistry.FAULTFLOW);
                        break;
                    }
            }
        } catch (AxisFault e) {
            throw new PhaseException(e);
        }
    }


    public void buildTransportChain(PhasesInclude trnsport, int chainType)
            throws PhaseException {
        try {
            OrderThePhases();

            HandlerMetadata[] handlers;
            Class handlerClass = null;
            Handler handler;
            switch (chainType) {
                case 1:
                    {
                        ArrayList inChain = new ArrayList();
                        for (int i = 0; i < phaseholder.size(); i++) {
                            PhaseMetadata phase =
                                    (PhaseMetadata) phaseholder.get(i);
                            Phase axisPhase = new Phase(phase.getName());
                            handlers = phase.getOrderedHandlers();
                            for (int j = 0; j < handlers.length; j++) {
                                try {
                                    handlerClass = Class.forName(
                                            handlers[j].getClassName(), true,
                                            Thread.currentThread().getContextClassLoader());
                                    handler =
                                            (Handler) handlerClass.newInstance();
                                    handler.init(handlers[j]);
                                    handlers[j].setHandler(handler);
                                    axisPhase.addHandler(handlers[j].getHandler());
                                } catch (ClassNotFoundException e) {
                                    throw new PhaseException(e);
                                } catch (IllegalAccessException e) {
                                    throw new PhaseException(e);
                                } catch (InstantiationException e) {
                                    throw new PhaseException(e);
                                }
                            }
                            inChain.add(axisPhase);
                        }
                        trnsport.setPhases(inChain, EngineRegistry.INFLOW);
                        break;
                    }
                case 2:
                    {
                        ArrayList outChain = new ArrayList();
                        for (int i = 0; i < phaseholder.size(); i++) {
                            PhaseMetadata phase =
                                    (PhaseMetadata) phaseholder.get(i);
                            Phase axisPhase = new Phase(phase.getName());
                            handlers = phase.getOrderedHandlers();
                            for (int j = 0; j < handlers.length; j++) {
                                try {
                                    handlerClass = Class.forName(
                                            handlers[j].getClassName(), true,
                                            Thread.currentThread().getContextClassLoader());
                                    handler =
                                            (Handler) handlerClass.newInstance();
                                    handler.init(handlers[j]);
                                    handlers[j].setHandler(handler);
                                    axisPhase.addHandler(handlers[j].getHandler());
                                } catch (ClassNotFoundException e) {
                                    throw new PhaseException(e);
                                } catch (IllegalAccessException e) {
                                    throw new PhaseException(e);
                                } catch (InstantiationException e) {
                                    throw new PhaseException(e);
                                }
                            }
                            outChain.add(axisPhase);
                        }
                        trnsport.setPhases(outChain, EngineRegistry.OUTFLOW);
                        break;
                    }
                case 3:
                    {
                        ArrayList faultChain = new ArrayList();
                        for (int i = 0; i < phaseholder.size(); i++) {
                            PhaseMetadata phase =
                                    (PhaseMetadata) phaseholder.get(i);
                            Phase axisPhase = new Phase(phase.getName());
                            handlers = phase.getOrderedHandlers();
                            for (int j = 0; j < handlers.length; j++) {
                                try {
                                    handlerClass = Class.forName(
                                            handlers[j].getClassName(), true,
                                            Thread.currentThread().getContextClassLoader());
                                    handler =
                                            (Handler) handlerClass.newInstance();
                                    handler.init(handlers[j]);
                                    handlers[j].setHandler(handler);
                                    axisPhase.addHandler(handlers[j].getHandler());
                                } catch (ClassNotFoundException e) {
                                    throw new PhaseException(e);
                                } catch (IllegalAccessException e) {
                                    throw new PhaseException(e);
                                } catch (InstantiationException e) {
                                    throw new PhaseException(e);
                                }
                            }
                            faultChain.add(axisPhase);
                        }
                        trnsport.setPhases(faultChain, EngineRegistry.FAULTFLOW);
                        break;
                    }
            }
        } catch (AxisFault e) {
            throw new PhaseException(e);
        }
    }

    /**
     * Method buildTransportChain
     *
     * @param trnsport
     * @param chainType
     * @throws PhaseException
     */
    public void buildTransportChain(AxisTransport trnsport, int chainType)
            throws PhaseException {
        try {
            OrderThePhases();

            HandlerMetadata[] handlers;
            Class handlerClass = null;
            Handler handler;
            switch (chainType) {
                case 1:
                    {
                        ArrayList inChain = new ArrayList();
                        for (int i = 0; i < phaseholder.size(); i++) {
                            PhaseMetadata phase =
                                    (PhaseMetadata) phaseholder.get(i);
                            Phase axisPhase = new Phase(phase.getName());
                            handlers = phase.getOrderedHandlers();
                            for (int j = 0; j < handlers.length; j++) {
                                try {
                                    handlerClass = Class.forName(
                                            handlers[j].getClassName(), true,
                                            Thread.currentThread().getContextClassLoader());
                                    handler =
                                            (Handler) handlerClass.newInstance();
                                    handler.init(handlers[j]);
                                    handlers[j].setHandler(handler);
                                    axisPhase.addHandler(handlers[j].getHandler());
                                } catch (ClassNotFoundException e) {
                                    throw new PhaseException(e);
                                } catch (IllegalAccessException e) {
                                    throw new PhaseException(e);
                                } catch (InstantiationException e) {
                                    throw new PhaseException(e);
                                }
                            }
                            inChain.add(axisPhase);
                        }
                        trnsport.setPhases(inChain, EngineRegistry.INFLOW);
                        break;
                    }
                case 2:
                    {
                        ArrayList outChain = new ArrayList();
                        for (int i = 0; i < phaseholder.size(); i++) {
                            PhaseMetadata phase =
                                    (PhaseMetadata) phaseholder.get(i);
                            Phase axisPhase = new Phase(phase.getName());
                            handlers = phase.getOrderedHandlers();
                            for (int j = 0; j < handlers.length; j++) {
                                try {
                                    handlerClass = Class.forName(
                                            handlers[j].getClassName(), true,
                                            Thread.currentThread().getContextClassLoader());
                                    handler =
                                            (Handler) handlerClass.newInstance();
                                    handler.init(handlers[j]);
                                    handlers[j].setHandler(handler);
                                    axisPhase.addHandler(handlers[j].getHandler());
                                } catch (ClassNotFoundException e) {
                                    throw new PhaseException(e);
                                } catch (IllegalAccessException e) {
                                    throw new PhaseException(e);
                                } catch (InstantiationException e) {
                                    throw new PhaseException(e);
                                }
                            }
                            outChain.add(axisPhase);
                        }
                        trnsport.setPhases(outChain, EngineRegistry.OUTFLOW);
                        break;
                    }
                case 3:
                    {
                        ArrayList faultChain = new ArrayList();
                        for (int i = 0; i < phaseholder.size(); i++) {
                            PhaseMetadata phase =
                                    (PhaseMetadata) phaseholder.get(i);
                            Phase axisPhase = new Phase(phase.getName());
                            handlers = phase.getOrderedHandlers();
                            for (int j = 0; j < handlers.length; j++) {
                                try {
                                    handlerClass = Class.forName(
                                            handlers[j].getClassName(), true,
                                            Thread.currentThread().getContextClassLoader());
                                    handler =
                                            (Handler) handlerClass.newInstance();
                                    handler.init(handlers[j]);
                                    handlers[j].setHandler(handler);
                                    axisPhase.addHandler(handlers[j].getHandler());
                                } catch (ClassNotFoundException e) {
                                    throw new PhaseException(e);
                                } catch (IllegalAccessException e) {
                                    throw new PhaseException(e);
                                } catch (InstantiationException e) {
                                    throw new PhaseException(e);
                                }
                            }
                            faultChain.add(axisPhase);
                        }
                        trnsport.setPhases(faultChain, EngineRegistry.FAULTFLOW);
                        break;
                    }
            }
        } catch (AxisFault e) {
            throw new PhaseException(e);
        }
    }

    /**
     * Method buildGlobalChain
     *
     * @param axisGlobal
     * @param chainType
     * @throws PhaseException
     */
    public void buildGlobalChain(AxisGlobal axisGlobal, int chainType)
            throws PhaseException {
        try {
            OrderThePhases();
            HandlerMetadata[] handlers;
            switch (chainType) {
                case 1:
                    {
                        ArrayList inChain = new ArrayList();
                        for (int i = 0; i < phaseholder.size(); i++) {
                            PhaseMetadata phase =
                                    (PhaseMetadata) phaseholder.get(i);
                            Phase axisPhase = new Phase(phase.getName());
                            handlers = phase.getOrderedHandlers();
                            for (int j = 0; j < handlers.length; j++) {
                                axisPhase.addHandler(handlers[j].getHandler());
                            }
                            inChain.add(axisPhase);
                        }
                        axisGlobal.setPhases(inChain, EngineRegistry.INFLOW);
                        break;
                    }
                case 2:
                    {
                        ArrayList outChain = new ArrayList();
                        for (int i = 0; i < phaseholder.size(); i++) {
                            PhaseMetadata phase =
                                    (PhaseMetadata) phaseholder.get(i);
                            Phase axisPhase = new Phase(phase.getName());
                            handlers = phase.getOrderedHandlers();
                            for (int j = 0; j < handlers.length; j++) {
                                axisPhase.addHandler(handlers[j].getHandler());
                            }
                            outChain.add(axisPhase);
                        }
                        axisGlobal.setPhases(outChain, EngineRegistry.OUTFLOW);
                        break;
                    }
                case 3:
                    {
                        ArrayList faultChain = new ArrayList();
                        for (int i = 0; i < phaseholder.size(); i++) {
                            PhaseMetadata phase =
                                    (PhaseMetadata) phaseholder.get(i);
                            Phase axisPhase = new Phase(phase.getName());
                            handlers = phase.getOrderedHandlers();
                            for (int j = 0; j < handlers.length; j++) {
                                axisPhase.addHandler(handlers[j].getHandler());
                            }
                            faultChain.add(axisPhase);
                        }
                        axisGlobal.setPhases(faultChain, EngineRegistry.FAULTFLOW);
                        break;
                    }
            }
        } catch (AxisFault e) {
            throw new PhaseException(e);
        }
    }
}
