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
import org.apache.axis2.deployment.util.PhasesInfo;
import org.apache.axis2.description.ModuleConfiguration;
import org.apache.axis2.description.ParameterInclude;
import org.apache.axis2.description.TransportInDescription;
import org.apache.axis2.description.TransportOutDescription;
import org.apache.axis2.engine.AbstractDispatcher;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.engine.AxisConfigurationImpl;
import org.apache.axis2.engine.AxisObserver;
import org.apache.axis2.engine.MessageReceiver;
import org.apache.axis2.engine.Phase;
import org.apache.axis2.i18n.Messages;
import org.apache.axis2.om.OMAttribute;
import org.apache.axis2.om.OMElement;
import org.apache.axis2.phaseresolver.PhaseMetadata;
import org.apache.axis2.storage.AxisStorage;
import org.apache.axis2.transport.TransportListener;
import org.apache.axis2.transport.TransportSender;
import org.apache.axis2.util.HostConfiguration;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;

public class AxisConfigBuilder extends DescriptionBuilder {

    private AxisConfiguration axisConfiguration;
    private DeploymentEngine engine;

    public AxisConfigBuilder(InputStream serviceInputSteram, DeploymentEngine engine,
                             AxisConfiguration axisConfiguration) {
        super(serviceInputSteram, axisConfiguration);
        this.axisConfiguration = axisConfiguration;
        this.engine = engine;
    }

    public void populateConfig() throws DeploymentException {
        try {
            OMElement config_element = buildOM();

            //processing Paramters
            //Processing service level paramters
            Iterator itr = config_element.getChildrenWithName(
                    new QName(PARAMETERST));
            processParameters(itr, axisConfiguration, axisConfiguration);

            //process MessageReciver

            Iterator msgRecives = config_element.getChildrenWithName(new QName(MESSAGERECEIVER));
            while (msgRecives.hasNext()) {
                OMElement msgRev = (OMElement) msgRecives.next();
                MessageReceiver msgrecivere = loadMessageReceiver(
                        Thread.currentThread().getContextClassLoader(), msgRev);
                OMAttribute mepAtt = msgRev.getAttribute(new QName(MEP));
                ((AxisConfigurationImpl) axisConfiguration).addMessageReceiver(
                        mepAtt.getAttributeValue(), msgrecivere);
            }

            //processing Dispatching Order
            OMElement dispatch_order = config_element.getFirstChildWithName(
                    new QName(DISPATCH_ORDER));
            if (dispatch_order != null) {
                processDispatchingOrder(dispatch_order);
                log.info("found the custom dispatching order and continue with that order");
            } else {
                ((AxisConfigurationImpl) axisConfiguration).setDefaultDispatchers();
                log.info("no custom dispatching order found, continuing with the default dispaching order");
            }

            //Process Module refs
            Iterator moduleitr = config_element.getChildrenWithName(
                    new QName(DeploymentConstants.MODULEST));
            processModuleRefs(moduleitr);

            // Proccessing Transport Sennders
            Iterator trs_senders = config_element.getChildrenWithName(new QName(TRANSPORTSENDER));
            processTransportSenders(trs_senders);

            // Proccessing Transport Recivers
            Iterator trs_Reivers = config_element.getChildrenWithName(new QName(TRANSPORTRECEIVER));
            processTransportReceivers(trs_Reivers);

            // Process Observers
            Iterator obs_ittr = config_element.getChildrenWithName(new QName(LISTENERST));
            processObservers(obs_ittr);

            //processing Phase orders
            Iterator phaserders = config_element.getChildrenWithName(new QName(PHASE_ORDER));
            processPhaseOrders(phaserders);

            //processing Axis Storages
            OMElement storages = config_element.getFirstChildWithName(new QName(AXIS_STORAGE));
            processAxisStorage(storages);

            Iterator moduleConfigs = config_element.getChildrenWithName(new QName(MODULECONFIG));
            processModuleConfig(moduleConfigs, axisConfiguration, axisConfiguration);

            // setting host configuration
            OMElement hostElement = config_element.getFirstChildWithName(new QName(HOST_CONFIG));
            if (hostElement != null) {
                processHostCongiguration(hostElement, axisConfiguration);
            }
        } catch (XMLStreamException e) {
            throw new DeploymentException(e);
        }
    }


    private void processDispatchingOrder(OMElement dispatch_order) throws DeploymentException {
        Iterator dispatchers = dispatch_order.getChildrenWithName(new QName(DISPATCHER));
        boolean foundDiaptcher = false;
        Phase dispatchPhae = new Phase(PhaseMetadata.PHASE_DISPATCH);
        int count = 0;
        while (dispatchers.hasNext()) {
            foundDiaptcher = true;
            OMElement dispchter = (OMElement) dispatchers.next();
            String clssName = dispchter.getAttribute(new QName(CLASSNAME)).getAttributeValue();
            AbstractDispatcher disptachClas;
            Class classInstance;
            try {
                classInstance = Class.forName(
                        clssName, true, Thread.currentThread().getContextClassLoader());
                disptachClas = (AbstractDispatcher) classInstance.newInstance();
                disptachClas.initDispatcher();
                disptachClas.getHandlerDesc().setParent(axisConfiguration);
                dispatchPhae.addHandler(disptachClas, count);
                count ++;
            } catch (ClassNotFoundException e) {
                throw new DeploymentException(e);
            } catch (IllegalAccessException e) {
                throw new DeploymentException(e);
            } catch (InstantiationException e) {
                throw new DeploymentException(e);
            }
        }

        if (!foundDiaptcher) {
            throw new DeploymentException(Messages.getMessage(DeploymentErrorMsgs.NO_DISPATCHER_FOUND));
        } else {
            ((AxisConfigurationImpl) axisConfiguration).setDispatchPhase(dispatchPhae);
        }

    }

    private void processAxisStorage(OMElement storageElement) throws DeploymentException {
        AxisStorage axisStorage;
        if (storageElement != null) {
            OMAttribute className = storageElement.getAttribute(new QName(CLASSNAME));
            if (className == null) {
                throw new DeploymentException(Messages.getMessage(
                        DeploymentErrorMsgs.INVALID_STORGE_CLASS));
            } else {
                String classNameStr = className.getAttributeValue();
                Class stoarge;
                if (classNameStr != null && !"".equals(classNameStr)) {
                    try {
                        stoarge = Class.forName(classNameStr, true,
                                Thread.currentThread().getContextClassLoader());
                        axisStorage = (AxisStorage) stoarge.newInstance();
                        axisConfiguration.setAxisStorage(axisStorage);

                        // adding storage paramters
                        Iterator paramters = storageElement.getChildrenWithName(
                                new QName(PARAMETERST));
                        processParameters(paramters, axisStorage, axisConfiguration);


                    } catch (ClassNotFoundException e) {
                        throw new DeploymentException
                                (Messages.getMessage(DeploymentErrorMsgs.CLASS_NOT_FOUND,
                                        e.getMessage()));
                    } catch (InstantiationException e) {
                        throw new DeploymentException
                                (Messages.getMessage(DeploymentErrorMsgs.INSTANTITAIONEXP,
                                        e.getMessage()));
                    } catch (IllegalAccessException e) {
                        throw new DeploymentException
                                (Messages.getMessage(DeploymentErrorMsgs.ILEGAL_ACESS,
                                        e.getMessage()));
                    }
                } else {
                    throw new DeploymentException(Messages.getMessage(
                            DeploymentErrorMsgs.INVALID_STORGE_CLASS));
                }

            }

        } else {
            try {
                //Default Storeg :  org.apache.axis2.storage.impl.AxisMemoryStorage
                Class stoarge = Class.forName("org.apache.axis2.storage.impl.AxisMemoryStorage", true,
                        Thread.currentThread().getContextClassLoader());
                axisStorage = (AxisStorage) stoarge.newInstance();
                axisConfiguration.setAxisStorage(axisStorage);
            } catch (ClassNotFoundException e) {
                throw new DeploymentException
                        (Messages.getMessage(DeploymentErrorMsgs.CLASS_NOT_FOUND,
                                e.getMessage()));
            } catch (InstantiationException e) {
                throw new DeploymentException
                        (Messages.getMessage(DeploymentErrorMsgs.INSTANTITAIONEXP,
                                e.getMessage()));
            } catch (IllegalAccessException e) {
                throw new DeploymentException
                        (Messages.getMessage(DeploymentErrorMsgs.ILEGAL_ACESS,
                                e.getMessage()));
            }
        }

    }

    /**
     * To process all the phase orders which are defined in axis2.xml
     *
     * @param phaserders
     */
    private void processPhaseOrders(Iterator phaserders) {
        PhasesInfo info = engine.getPhasesinfo();
        while (phaserders.hasNext()) {
            OMElement phaseOrders = (OMElement) phaserders.next();
            String flowType = phaseOrders.getAttribute(new QName(TYPE)).getAttributeValue();
            if (INFLOWST.equals(flowType)) {
                info.setINPhases(getPhaseList(phaseOrders));
            } else if (IN_FAILTFLOW.equals(flowType)) {
                info.setIN_FaultPhases(getPhaseList(phaseOrders));
            } else if (OUTFLOWST.equals(flowType)) {
                info.setOUTPhases(getPhaseList(phaseOrders));
            } else if (OUT_FAILTFLOW.equals(flowType)) {
                info.setOUT_FaultPhases(getPhaseList(phaseOrders));
            }

        }
    }


    private ArrayList getPhaseList(OMElement phaseOrders) {
        ArrayList phaselist = new ArrayList();
        Iterator phases = phaseOrders.getChildrenWithName(new QName(PHASE));
        while (phases.hasNext()) {
            OMElement phase = (OMElement) phases.next();
            phaselist.add(phase.getAttribute(new QName(ATTNAME)).getAttributeValue());
        }
        return phaselist;
    }


    private void processTransportSenders(Iterator trs_senders) throws DeploymentException {
        while (trs_senders.hasNext()) {
            TransportOutDescription transportout;
            OMElement transport = (OMElement) trs_senders.next();

            // getting trsnport Name
            OMAttribute trsName = transport.getAttribute(
                    new QName(ATTNAME));
            if (trsName != null) {
                String name = trsName.getAttributeValue();
                transportout = new TransportOutDescription(new QName(name));

                //tranport impl class
                OMAttribute trsClas = transport.getAttribute(
                        new QName(CLASSNAME));
                if (trsClas == null) {
                    throw new DeploymentException(Messages.getMessage(
                            DeploymentErrorMsgs.TRANSPORT_SENDER_ERROR, name));
                }
                String clasName = trsClas.getAttributeValue();
                Class sender;
                try {
                    sender = Class.forName(clasName, true,
                            Thread.currentThread()
                                    .getContextClassLoader());
                    TransportSender transportSender = (TransportSender) sender.newInstance();
                    transportout.setSender(transportSender);

                    //process Parameters
                    //processing Paramters
                    //Processing service level paramters
                    Iterator itr = transport.getChildrenWithName(
                            new QName(PARAMETERST));
                    processParameters(itr, transportout, axisConfiguration);

                    //process INFLOW
                    OMElement inFlow = transport.getFirstChildWithName(
                            new QName(INFLOWST));
                    if (inFlow != null) {
                        throw new DeploymentException(
                                Messages.getMessage(
                                        DeploymentErrorMsgs.INFLOW_NOT_ALLOWED_IN_TRS_OUT, name));
                    }

                    OMElement outFlow = transport.getFirstChildWithName(
                            new QName(OUTFLOWST));
                    if (outFlow != null) {
                        transportout.setOutFlow(processFlow(outFlow, axisConfiguration));
                    }

                    OMElement inFaultFlow = transport.getFirstChildWithName(
                            new QName(IN_FAILTFLOW));
                    if (inFaultFlow != null) {
                        throw new DeploymentException(
                                Messages.getMessage(
                                        DeploymentErrorMsgs.INFLOW_NOT_ALLOWED_IN_TRS_OUT, name));
                    }

                    OMElement outFaultFlow = transport.getFirstChildWithName(
                            new QName(OUT_FAILTFLOW));
                    if (outFaultFlow != null) {
                        transportout.setFaultFlow(processFlow(outFaultFlow, axisConfiguration));
                    }

                    //adding to axis config
                    axisConfiguration.addTransportOut(transportout);

                } catch (ClassNotFoundException e) {
                    throw new DeploymentException(e);
                } catch (IllegalAccessException e) {
                    throw new DeploymentException(e);
                } catch (InstantiationException e) {
                    throw new DeploymentException(e);
                } catch (AxisFault axisFault) {
                    throw new DeploymentException(axisFault);
                }

            }
        }
    }


    private void processTransportReceivers(Iterator trs_senders) throws DeploymentException {
        while (trs_senders.hasNext()) {
            TransportInDescription transportIN;
            OMElement transport = (OMElement) trs_senders.next();

            // getting trsnport Name
            OMAttribute trsName = transport.getAttribute(
                    new QName(ATTNAME));
            if (trsName != null) {
                String name = trsName.getAttributeValue();
                transportIN = new TransportInDescription(new QName(name));

                //tranport impl class
                OMAttribute trsClas = transport.getAttribute(new QName(CLASSNAME));
                if (trsClas != null) {
                    try {
                        String clasName = trsClas.getAttributeValue();
                        Class receiverClass = Class.forName(clasName, true,
                                Thread.currentThread()
                                        .getContextClassLoader());
                        TransportListener receiver = (TransportListener) receiverClass.newInstance();
                        transportIN.setReceiver(receiver);
                    } catch (ClassNotFoundException e) {
                        throw new DeploymentException(e);
                    } catch (IllegalAccessException e) {
                        throw new DeploymentException(e);
                    } catch (InstantiationException e) {
                        throw new DeploymentException(e);
                    }
                }
                try {

                    //process Parameters
                    //processing Paramters
                    //Processing service level paramters
                    Iterator itr = transport.getChildrenWithName(
                            new QName(PARAMETERST));
                    processParameters(itr, transportIN, axisConfiguration);

                    //process INFLOW
                    OMElement inFlow = transport.getFirstChildWithName(
                            new QName(INFLOWST));
                    if (inFlow != null) {
                        throw new DeploymentException(
                                Messages.getMessage(
                                        DeploymentErrorMsgs.INFLOW_NOT_ALLOWED_IN_TRS_OUT, name));
                    }

                    OMElement outFlow = transport.getFirstChildWithName(
                            new QName(OUTFLOWST));
                    if (outFlow != null) {
                        transportIN.setInFlow(processFlow(outFlow, axisConfiguration));
                    }

                    OMElement inFaultFlow = transport.getFirstChildWithName(
                            new QName(IN_FAILTFLOW));
                    if (inFaultFlow != null) {
                        transportIN.setFaultFlow(processFlow(inFaultFlow, axisConfiguration));
                    }

                    OMElement outFaultFlow = transport.getFirstChildWithName(
                            new QName(OUT_FAILTFLOW));
                    if (outFaultFlow != null) {
                        throw new DeploymentException(Messages.getMessage(
                                DeploymentErrorMsgs.OUTFLOW_NOT_ALLOWED_IN_TRS_IN, name));
                    }

                    //adding to axis config
                    axisConfiguration.addTransportIn(transportIN);

                } catch (AxisFault axisFault) {
                    throw new DeploymentException(axisFault);
                }

            }
        }
    }


    private void processHostCongiguration(OMElement element, AxisConfiguration config) {
        OMElement ipele = element.getFirstChildWithName(new QName("ip"));
        String ip = null;
        int port = -1;
        if (ipele != null) {
            ip = ipele.getText().trim();
        }
        OMElement portele = element.getFirstChildWithName(new QName("port"));
        if (portele != null) {
            port = Integer.parseInt(portele.getText().trim());
        }
        HostConfiguration hostconfig = new HostConfiguration(ip, port);
        config.setHostConfiguration(hostconfig);
    }

    /**
     * To process AxisObservers
     *
     * @param oservers
     */
    private void processObservers(Iterator oservers) throws DeploymentException {
        while (oservers.hasNext()) {
            OMElement observerelement = (OMElement) oservers.next();
            AxisObserver observer;
            OMAttribute trsClas = observerelement.getAttribute(
                    new QName(CLASSNAME));
            String clasName;
            if (trsClas != null) {
                clasName = trsClas.getAttributeValue();
            } else {
                throw new DeploymentException(Messages.getMessage(
                        DeploymentErrorMsgs.OBSERVER_ERROR));
            }
            try {
                Class observerclass = Class.forName(clasName, true, Thread.currentThread().
                        getContextClassLoader());
                observer = (AxisObserver) observerclass.newInstance();
                //processing Paramters
                //Processing service level paramters
                Iterator itr = observerelement.getChildrenWithName(
                        new QName(PARAMETERST));
                processParameters(itr, observer, axisConfiguration);

                // initilization
                observer.init();
                ((AxisConfigurationImpl) axisConfiguration).addObservers(observer);

            } catch (ClassNotFoundException e) {
                throw new DeploymentException(e);
            } catch (IllegalAccessException e) {
                throw new DeploymentException(e);
            } catch (InstantiationException e) {
                throw new DeploymentException(e);
            }
        }

    }

    /**
     * To get the list og modules that is requird to be engage globally
     *
     * @param moduleRefs <code>java.util.Iterator</code>
     */
    protected void processModuleRefs(Iterator moduleRefs) {
        while (moduleRefs.hasNext()) {
            OMElement moduleref = (OMElement) moduleRefs.next();
            OMAttribute moduleRefAttribute = moduleref.getAttribute(
                    new QName(REF));
            String refName = moduleRefAttribute.getAttributeValue();
            engine.addModule(new QName(refName));
        }
    }

    protected void processModuleConfig(Iterator moduleConfigs,
                                       ParameterInclude parent, AxisConfiguration config)
            throws DeploymentException {
        while (moduleConfigs.hasNext()) {
            OMElement moduleConfig = (OMElement) moduleConfigs.next();
            OMAttribute moduleName_att = moduleConfig.getAttribute(
                    new QName(ATTNAME));
            if (moduleName_att == null) {
                throw new DeploymentException(Messages.getMessage(DeploymentErrorMsgs.INVALID_MODULE_CONFIG));
            } else {
                String module = moduleName_att.getAttributeValue();
                ModuleConfiguration moduleConfiguration =
                        new ModuleConfiguration(new QName(module), parent);
                Iterator paramters = moduleConfig.getChildrenWithName(new QName(PARAMETERST));
                processParameters(paramters, moduleConfiguration, parent);
                ((AxisConfigurationImpl) config).addModuleConfig(moduleConfiguration);
            }
        }
    }

}
