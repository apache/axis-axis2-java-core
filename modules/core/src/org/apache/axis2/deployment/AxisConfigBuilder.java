package org.apache.axis2.deployment;

import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.engine.MessageReceiver;
import org.apache.axis2.engine.AxisConfigurationImpl;
import org.apache.axis2.engine.AxisObserver;
import org.apache.axis2.om.OMElement;
import org.apache.axis2.om.OMAttribute;
import org.apache.axis2.i18n.Messages;
import org.apache.axis2.AxisFault;
import org.apache.axis2.deployment.util.PhasesInfo;
import org.apache.axis2.transport.TransportSender;
import org.apache.axis2.transport.TransportListener;
import org.apache.axis2.description.TransportOutDescription;
import org.apache.axis2.description.TransportInDescription;

import javax.xml.stream.XMLStreamException;
import javax.xml.namespace.QName;
import java.io.InputStream;
import java.util.Iterator;
import java.util.ArrayList;
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
*
*
*/

/**
 * Author: Deepal Jayasinghe
 * Date: Aug 26, 2005
 * Time: 4:44:46 PM
 */
public class AxisConfigBuilder extends DescriptionBuilder{

    private AxisConfiguration axisConfiguration;

    public AxisConfigBuilder(InputStream serviceInputSteram, DeploymentEngine engine,
                             AxisConfiguration axisConfiguration) {
        super(serviceInputSteram, engine);
        this.axisConfiguration = axisConfiguration;
    }

    public void populateConfig() throws DeploymentException {
        try {
            OMElement config_element = buildOM();

            //processing Paramters
            //Processing service level paramters
            Iterator itr = config_element.getChildrenWithName(
                    new QName(PARAMETERST));
            processParameters(itr,axisConfiguration);

            //process MessageReciver

            Iterator msgRecives = config_element.getChildrenWithName(new QName(MESSAGERECEIVER));
            while (msgRecives.hasNext()) {
                OMElement msgRev = (OMElement) msgRecives.next();
                MessageReceiver msgrecivere= loadMessageReceiver(
                        Thread.currentThread().getContextClassLoader(),msgRev);
                OMAttribute mepAtt = msgRev.getAttribute(new QName(MEP));
                ((AxisConfigurationImpl)axisConfiguration).addMessageReceiver(
                        mepAtt.getValue(),msgrecivere);
            }



            //Process Module refs
            Iterator moduleitr = config_element.getChildrenWithName(
                    new QName(DeploymentConstants.MODULEST));
            processModuleRefs(moduleitr);

            // Proccessing Transport Sennders
            Iterator trs_senders = config_element.getChildrenWithName(new QName(TRANSPORTSENDER)) ;
            processTransportSenders(trs_senders);

            // Proccessing Transport Recivers
            Iterator trs_Reivers = config_element.getChildrenWithName(new QName(TRANSPORTRECEIVER)) ;
            processTransportReceivers(trs_Reivers);

            // Process Observers
            Iterator obs_ittr=config_element.getChildrenWithName(new QName(LISTENERST)) ;
            processObservers(obs_ittr);

            //processing Phase orders
            Iterator phaserders =config_element.getChildrenWithName(new QName(PHASE_ORDER)) ;
            processPhaseOrders(phaserders);

        } catch (XMLStreamException e) {
            throw new DeploymentException(e);
        }
    }

    private void processPhaseOrders(Iterator phaserders){
        PhasesInfo info = engine.getPhasesinfo();
        while (phaserders.hasNext()) {
            OMElement phaseOrders = (OMElement) phaserders.next();
            String flowType = phaseOrders.getAttribute(new QName(TYPE)).getValue();
            if(INFLOWST.equals(flowType)){
                info.setINPhases(getPhaseList(phaseOrders));
            }   else if (IN_FAILTFLOW.equals(flowType)){
                info.setIN_FaultPhases(getPhaseList(phaseOrders));
            }   else if (OUTFLOWST.equals(flowType)){
                info.setOUTPhases(getPhaseList(phaseOrders));
            }   else if (OUT_FAILTFLOW.equals(flowType)){
                info.setOUT_FaultPhases(getPhaseList(phaseOrders));
            }

        }
    }


    private ArrayList getPhaseList(OMElement phaseOrders){
        ArrayList phaselist = new ArrayList();
        Iterator phases =  phaseOrders.getChildrenWithName(new QName(PHASE));
        while (phases.hasNext()) {
            OMElement phase = (OMElement) phases.next();
            phaselist.add(phase.getAttribute(new QName(ATTNAME)).getValue());
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
            if(trsName !=null){
                String name = trsName.getValue();
                transportout = new TransportOutDescription(new QName(name));

                //tranport impl class
                OMAttribute trsClas = transport.getAttribute(
                        new QName(CLASSNAME));
                if(trsClas == null){
                    throw new DeploymentException("TransportSEnder Implementation class is required " +
                            "for the transport" + name);
                }
                String clasName = trsClas.getValue();
                Class sender;
                try {
                    sender =Class.forName(clasName,true,
                            Thread.currentThread()
                                    .getContextClassLoader());
                    TransportSender transportSender = (TransportSender) sender.newInstance();
                    transportout.setSender(transportSender);


                    //process Parameters
                    //processing Paramters
                    //Processing service level paramters
                    Iterator itr = transport.getChildrenWithName(
                            new QName(PARAMETERST));
                    processParameters(itr,transportout);

                    //process INFLOW
                    OMElement inFlow = transport.getFirstChildWithName(
                            new QName(INFLOWST));
                    if(inFlow !=null){
                        throw new DeploymentException(
                                Messages.getMessage(
                                        DeploymentErrorMsgs.INFLOW_NOT_ALLOWED_IN_TRS_OUT, name));
                    }

                    OMElement outFlow = transport.getFirstChildWithName(
                            new QName(OUTFLOWST));
                    if(outFlow !=null){
                        transportout.setOutFlow(processFlow(outFlow));
                    }

                    OMElement inFaultFlow = transport.getFirstChildWithName(
                            new QName(IN_FAILTFLOW));
                    if(inFaultFlow !=null){
                        throw new DeploymentException(
                                Messages.getMessage(
                                        DeploymentErrorMsgs.INFLOW_NOT_ALLOWED_IN_TRS_OUT, name));
                    }

                    OMElement outFaultFlow = transport.getFirstChildWithName(
                            new QName(OUT_FAILTFLOW));
                    if(outFaultFlow !=null){
                        transportout.setFaultFlow(processFlow(outFaultFlow));
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
            if(trsName !=null){
                String name = trsName.getValue();
                transportIN = new TransportInDescription(new QName(name));

                //tranport impl class
                OMAttribute trsClas = transport.getAttribute(new QName(CLASSNAME));
                if(trsClas !=null) {
                    try {
                        String clasName = trsClas.getValue();
                        Class receiverClass =Class.forName(clasName,true,
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
                    processParameters(itr,transportIN);

                    //process INFLOW
                    OMElement inFlow = transport.getFirstChildWithName(
                            new QName(INFLOWST));
                    if(inFlow !=null){
                        throw new DeploymentException(
                                Messages.getMessage(
                                        DeploymentErrorMsgs.INFLOW_NOT_ALLOWED_IN_TRS_OUT, name));
                    }

                    OMElement outFlow = transport.getFirstChildWithName(
                            new QName(OUTFLOWST));
                    if(outFlow !=null){
                        transportIN.setInFlow( processFlow(outFlow));
                    }

                    OMElement inFaultFlow = transport.getFirstChildWithName(
                            new QName(IN_FAILTFLOW));
                    if(inFaultFlow !=null){
                        transportIN.setFaultFlow(processFlow(inFaultFlow));
                    }

                    OMElement outFaultFlow = transport.getFirstChildWithName(
                            new QName(OUT_FAILTFLOW));
                    if(outFaultFlow !=null){
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

    /**
     * To process AxisObservers
     * @param oservers
     */
    private void processObservers(Iterator oservers) throws DeploymentException {
        while (oservers.hasNext()) {
            OMElement observerelement = (OMElement) oservers.next();
            AxisObserver observer;
            OMAttribute trsClas = observerelement.getAttribute(
                    new QName(CLASSNAME));
            String clasName;
            if (trsClas !=null) {
                clasName = trsClas.getValue();
            } else {
                throw new DeploymentException("Observer Implementation Class is requird");
            }
            try {
                Class observerclass = Class.forName(clasName, true, Thread.currentThread().
                        getContextClassLoader());
                observer = (AxisObserver) observerclass.newInstance();
                //processing Paramters
                //Processing service level paramters
                Iterator itr = observerelement.getChildrenWithName(
                        new QName(PARAMETERST));
                processParameters(itr,observer);

                // initilization
                observer.init();
                ((AxisConfigurationImpl)axisConfiguration).addObservers(observer);

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
     * @param moduleRefs  <code>java.util.Iterator</code>
     */
    protected void processModuleRefs(Iterator moduleRefs) {
        while (moduleRefs.hasNext()) {
            OMElement moduleref = (OMElement) moduleRefs.next();
            OMAttribute moduleRefAttribute = moduleref.getAttribute(
                    new QName(REF));
            String refName = moduleRefAttribute.getValue();
            engine.addModule(new QName(refName));
        }

    }

}
