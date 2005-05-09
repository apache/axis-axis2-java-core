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

import java.util.ArrayList;

import org.apache.axis.context.EngineContext;
import org.apache.axis.description.HandlerMetadata;
import org.apache.axis.description.PhasesInclude;
import org.apache.axis.engine.AxisFault;
import org.apache.axis.engine.EngineConfiguration;
import org.apache.axis.engine.Handler;
import org.apache.axis.engine.SimplePhase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
    private final EngineConfiguration registry;    // = new  ServerMetaData();

    private PhasesInclude phaseInclude;

    private ArrayList inPhases;
    private ArrayList outPhases;
    private ArrayList faultInPhases;
    private ArrayList faultOutPhases;

    private int flowType = -1;

    /**
     * Constructor PhaseHolder
     *
     * @param registry
     */
    public PhaseHolder(EngineConfiguration registry, PhasesInclude phaseInclude) {
        this.registry = registry;
        this.phaseInclude = phaseInclude;
        fillFlowPhases();
    }

    public PhaseHolder(EngineConfiguration registry) {
        this.registry = registry;
        this.phaseInclude = null;
        fillFlowPhases();
    }


    public void setFlowType(int flowType) {
        switch (flowType) {
            case PhaseMetadata.IN_FLOW:
                {
                    phaseholder = inPhases;
                    break;
                }
            case PhaseMetadata.OUT_FLOW:
                {
                    phaseholder = outPhases;
                    break;
                }
            case PhaseMetadata.FAULT_IN_FLOW:
                {
                    phaseholder = faultInPhases;
                    break;
                }
            case PhaseMetadata.FAULT_OUT_FLOW:
                {
                    phaseholder = faultOutPhases;
                    break;
                }
        }
        this.flowType = flowType;
    }

    private void fillFlowPhases() {
        inPhases = new ArrayList();
        outPhases = new ArrayList();
        faultInPhases = new ArrayList();
        faultOutPhases = new ArrayList();

        ArrayList tempPhases = registry.getInPhases();
        for (int i = 0; i < tempPhases.size(); i++) {
            String name = (String) tempPhases.get(i);
            PhaseMetadata pm = new PhaseMetadata(name);
            inPhases.add(pm);
        }
        inPhases.add(0, new PhaseMetadata(PhaseMetadata.PRE_DISPATCH));
        tempPhases = registry.getOutPhases();
        for (int i = 0; i < tempPhases.size(); i++) {
            String name = (String) tempPhases.get(i);
            PhaseMetadata pm = new PhaseMetadata(name);
            outPhases.add(pm);
        }
        outPhases.add(new PhaseMetadata(PhaseMetadata.PRE_DISPATCH));

        tempPhases = registry.getInFaultPhases();
        faultInPhases.add(0, new PhaseMetadata(PhaseMetadata.PRE_DISPATCH));
        for (int i = 0; i < tempPhases.size(); i++) {
            String name = (String) tempPhases.get(i);
            PhaseMetadata pm = new PhaseMetadata(name);
            faultInPhases.add(pm);
        }
        tempPhases = registry.getOutFaultPhases();
        for (int i = 0; i < tempPhases.size(); i++) {
            String name = (String) tempPhases.get(i);
            PhaseMetadata pm = new PhaseMetadata(name);
            faultOutPhases.add(pm);
        }
        faultOutPhases.add(new PhaseMetadata(PhaseMetadata.PRE_DISPATCH));
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
            throw new PhaseException("Invalid Phase ," + phaseName
                    + "for the handler "
                    + handler.getName()
                    + " dose not exit in server.xml or refering to phase in diffrent flow");
        }
        /*
        else {
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
        }*/
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

    public ArrayList getOrderHandler() throws PhaseException {
        ArrayList handlerList = new ArrayList();
        //OrderThePhases();
        HandlerMetadata[] handlers;
        for (int i = 0; i < phaseholder.size(); i++) {
            PhaseMetadata phase =
                    (PhaseMetadata) phaseholder.get(i);
            handlers = phase.getOrderedHandlers();
            for (int j = 0; j < handlers.length; j++) {
                handlerList.add(handlers[j]);
            }

        }
        return handlerList;
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
            //OrderThePhases();

            HandlerMetadata[] handlers;
            switch (chainType) {
                case PhaseMetadata.IN_FLOW:
                    {
                        ArrayList inChain = new ArrayList();
                        for (int i = 0; i < phaseholder.size(); i++) {
                            PhaseMetadata phase =
                                    (PhaseMetadata) phaseholder.get(i);
                            SimplePhase axisPhase = new SimplePhase(phase.getName());
                            handlers = phase.getOrderedHandlers();
                            for (int j = 0; j < handlers.length; j++) {
                                axisPhase.addHandler(handlers[j].getHandler());
                            }
                            inChain.add(axisPhase);
                        }
                        phaseInclude.setPhases(inChain, EngineConfiguration.INFLOW);
                        // service.setPhases(inChain, EngineConfiguration.INFLOW);
                        break;
                    }
                case PhaseMetadata.OUT_FLOW:
                    {
                        ArrayList outChain = new ArrayList();
                        for (int i = 0; i < phaseholder.size(); i++) {
                            PhaseMetadata phase =
                                    (PhaseMetadata) phaseholder.get(i);
                            SimplePhase axisPhase = new SimplePhase(phase.getName());
                            handlers = phase.getOrderedHandlers();
                            for (int j = 0; j < handlers.length; j++) {
                                axisPhase.addHandler(handlers[j].getHandler());
                            }
                            outChain.add(axisPhase);
                        }
                        //service.setPhases(outChain, EngineConfiguration.OUTFLOW);
                        phaseInclude.setPhases(outChain, EngineConfiguration.OUTFLOW);
                        break;
                    }
                case PhaseMetadata.FAULT_IN_FLOW:
                    {
                        ArrayList faultInChain = new ArrayList();
                        for (int i = 0; i < phaseholder.size(); i++) {
                            PhaseMetadata phase =
                                    (PhaseMetadata) phaseholder.get(i);
                            SimplePhase axisPhase = new SimplePhase(phase.getName());
                            handlers = phase.getOrderedHandlers();
                            for (int j = 0; j < handlers.length; j++) {
                                axisPhase.addHandler(handlers[j].getHandler());
                            }
                            faultInChain.add(axisPhase);
                        }
                        phaseInclude.setPhases(faultInChain, EngineConfiguration.FAULT_IN_FLOW);
                        // service.setPhases(faultInChain, EngineConfiguration.FAULT_IN_FLOW);
                        break;
                    }
                case PhaseMetadata.FAULT_OUT_FLOW:
                    {
                        ArrayList faultOutChain = new ArrayList();
                        for (int i = 0; i < phaseholder.size(); i++) {
                            PhaseMetadata phase =
                                    (PhaseMetadata) phaseholder.get(i);
                            SimplePhase axisPhase = new SimplePhase(phase.getName());
                            handlers = phase.getOrderedHandlers();
                            for (int j = 0; j < handlers.length; j++) {
                                axisPhase.addHandler(handlers[j].getHandler());
                            }
                            faultOutChain.add(axisPhase);
                        }
                        phaseInclude.setPhases(faultOutChain, EngineConfiguration.FAULT_OUT_FLOW);
                        //   service.setPhases(faultOutChain, EngineConfiguration.FAULT_IN_FLOW);
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
            //OrderThePhases();

            HandlerMetadata[] handlers;
            Class handlerClass = null;
            Handler handler;
            switch (chainType) {
                case PhaseMetadata.IN_FLOW:
                    {
                        ArrayList inChain = new ArrayList();
                        for (int i = 0; i < phaseholder.size(); i++) {
                            PhaseMetadata phase =
                                    (PhaseMetadata) phaseholder.get(i);
                            SimplePhase axisPhase = new SimplePhase(phase.getName());
                            handlers = phase.getOrderedHandlers();
                            for (int j = 0; j < handlers.length; j++) {
                                try {
                                    handlerClass = Class.forName(handlers[j].getClassName(), true,
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
                        trnsport.setPhases(inChain, EngineConfiguration.INFLOW);
                        break;
                    }
                case PhaseMetadata.OUT_FLOW:
                    {
                        ArrayList outChain = new ArrayList();
                        for (int i = 0; i < phaseholder.size(); i++) {
                            PhaseMetadata phase =
                                    (PhaseMetadata) phaseholder.get(i);
                            SimplePhase axisPhase = new SimplePhase(phase.getName());
                            handlers = phase.getOrderedHandlers();
                            for (int j = 0; j < handlers.length; j++) {
                                try {
                                    handlerClass = Class.forName(handlers[j].getClassName(), true,
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
                        trnsport.setPhases(outChain, EngineConfiguration.OUTFLOW);
                        break;
                    }
                case PhaseMetadata.FAULT_IN_FLOW:
                    {
                        ArrayList faultChain = new ArrayList();
                        for (int i = 0; i < phaseholder.size(); i++) {
                            PhaseMetadata phase =
                                    (PhaseMetadata) phaseholder.get(i);
                            SimplePhase axisPhase = new SimplePhase(phase.getName());
                            handlers = phase.getOrderedHandlers();
                            for (int j = 0; j < handlers.length; j++) {
                                try {
                                    handlerClass = Class.forName(handlers[j].getClassName(), true,
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
                        trnsport.setPhases(faultChain, EngineConfiguration.FAULT_IN_FLOW);
                        break;
                    }
                case PhaseMetadata.FAULT_OUT_FLOW:
                    {
                        ArrayList faultChain = new ArrayList();
                        for (int i = 0; i < phaseholder.size(); i++) {
                            PhaseMetadata phase =
                                    (PhaseMetadata) phaseholder.get(i);
                            SimplePhase axisPhase = new SimplePhase(phase.getName());
                            handlers = phase.getOrderedHandlers();
                            for (int j = 0; j < handlers.length; j++) {
                                try {
                                    handlerClass = Class.forName(handlers[j].getClassName(), true,
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
                        trnsport.setPhases(faultChain, EngineConfiguration.FAULT_OUT_FLOW);
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
     * @param engineContext
     * @param chainType
     * @throws PhaseException
     */
    public void buildGlobalChain(EngineContext engineContext, int chainType)
            throws PhaseException {
        try {
            //OrderThePhases();
            HandlerMetadata[] handlers;
            switch (chainType) {
                case PhaseMetadata.IN_FLOW:
                    {
                        ArrayList inChain = new ArrayList();
                        for (int i = 0; i < phaseholder.size(); i++) {
                            PhaseMetadata phase =
                                    (PhaseMetadata) phaseholder.get(i);
                            SimplePhase axisPhase = new SimplePhase(phase.getName());
                            handlers = phase.getOrderedHandlers();
                            for (int j = 0; j < handlers.length; j++) {
                                axisPhase.addHandler(handlers[j].getHandler());
                            }
                            inChain.add(axisPhase);
                        }
                        engineContext.setPhases(inChain, EngineConfiguration.INFLOW);
                        break;
                    }
                case PhaseMetadata.OUT_FLOW:
                    {
                        ArrayList outChain = new ArrayList();
                        for (int i = 0; i < phaseholder.size(); i++) {
                            PhaseMetadata phase =
                                    (PhaseMetadata) phaseholder.get(i);
                            SimplePhase axisPhase = new SimplePhase(phase.getName());
                            handlers = phase.getOrderedHandlers();
                            for (int j = 0; j < handlers.length; j++) {
                                axisPhase.addHandler(handlers[j].getHandler());
                            }
                            outChain.add(axisPhase);
                        }
                        engineContext.setPhases(outChain, EngineConfiguration.OUTFLOW);
                        break;
                    }
                case PhaseMetadata.FAULT_IN_FLOW:
                    {
                        ArrayList faultChain = new ArrayList();
                        for (int i = 0; i < phaseholder.size(); i++) {
                            PhaseMetadata phase =
                                    (PhaseMetadata) phaseholder.get(i);
                            SimplePhase axisPhase = new SimplePhase(phase.getName());
                            handlers = phase.getOrderedHandlers();
                            for (int j = 0; j < handlers.length; j++) {
                                axisPhase.addHandler(handlers[j].getHandler());
                            }
                            faultChain.add(axisPhase);
                        }
                        engineContext.setPhases(faultChain, EngineConfiguration.FAULT_IN_FLOW);
                        break;
                    }
                case PhaseMetadata.FAULT_OUT_FLOW:
                    {
                        ArrayList faultChain = new ArrayList();
                        for (int i = 0; i < phaseholder.size(); i++) {
                            PhaseMetadata phase =
                                    (PhaseMetadata) phaseholder.get(i);
                            SimplePhase axisPhase = new SimplePhase(phase.getName());
                            handlers = phase.getOrderedHandlers();
                            for (int j = 0; j < handlers.length; j++) {
                                axisPhase.addHandler(handlers[j].getHandler());
                            }
                            faultChain.add(axisPhase);
                        }
                        engineContext.setPhases(faultChain, EngineConfiguration.FAULT_OUT_FLOW);
                        break;
                    }
            }
        } catch (AxisFault e) {
            throw new PhaseException(e);
        }
    }
}
