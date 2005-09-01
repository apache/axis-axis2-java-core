package org.apache.axis2.deployment;

import org.apache.axis2.deployment.util.PhasesInfo;
import org.apache.axis2.description.*;
import org.apache.axis2.engine.MessageReceiver;
import org.apache.axis2.i18n.Messages;
import org.apache.axis2.om.OMAttribute;
import org.apache.axis2.om.OMElement;
import org.apache.axis2.om.impl.OMOutputImpl;
import org.apache.axis2.Constants;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
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
 * Date: Aug 24, 2005
 * Time: 11:21:02 PM
 */


/**
 * This class is to convert OM->ServiceDescrption , where first create OM from service.xml and
 * then populate service description by using OM
 */


public class ServiceBuilder extends DescriptionBuilder{

    private ServiceDescription service;

    public ServiceBuilder(InputStream serviceInputSteram,
                          DeploymentEngine engine,ServiceDescription service) {
        super(serviceInputSteram, engine);
        this.service = service;
    }

    /**
     * top most method that used to populate service from corresponding OM
     */
    public void populateService() throws DeploymentException {
        try {
            OMElement service_element = buildOM();

            //setting service Name
            OMAttribute serviceNameatt = service_element.getAttribute(
                    new QName(ATTNAME));
            if(serviceNameatt !=null){
                String serviceName = serviceNameatt.getValue();
                if (serviceName != null && !"".equals(serviceName)) {
                    service.setName(new QName(serviceName));
                } else {
                    service.setName(new QName(getShortFileName(engine.getCurrentFileItem()
                            .getServiceName())));
                }
            } else {
                service.setName(new QName(getShortFileName(engine.getCurrentFileItem()
                        .getServiceName())));
            }
            //Processing service level paramters
            Iterator itr = service_element.getChildrenWithName(
                    new QName(PARAMETERST));
            processParameters(itr,service,service.getParent());

            //process service description
            OMElement descriptionElement = service_element.getFirstChildWithName(
                    new QName(DESCRIPTION));
            if (descriptionElement !=null) {
                OMElement descriptionValue = descriptionElement.getFirstElement();
                if(descriptionValue !=null){
                    StringWriter writer = new StringWriter();
                    descriptionValue.build();
                    descriptionValue.serializeWithCache(new
                            OMOutputImpl(XMLOutputFactory.newInstance().createXMLStreamWriter(writer)));
                    writer.flush();
                    service.setServiceDescription(writer.toString());
                } else {
                    service.setServiceDescription(descriptionElement.getText());
                }
            }

            //processing servicewide modules which required to engage gloabbly
            Iterator moduleRefs = service_element.getChildrenWithName(
                    new QName(MODULEST));
            processModuleRefs(moduleRefs);

            //process INFLOW
            OMElement inFlow = service_element.getFirstChildWithName(
                    new QName(INFLOWST));
            if(inFlow !=null){
                service.setInFlow(processFlow(inFlow,service));
            }

            OMElement outFlow = service_element.getFirstChildWithName(
                    new QName(OUTFLOWST));
            if(outFlow !=null){
                service.setOutFlow(processFlow(outFlow,service));
            }

            OMElement inFaultFlow = service_element.getFirstChildWithName(
                    new QName(IN_FAILTFLOW));
            if(inFaultFlow !=null){
                service.setFaultInFlow(processFlow(inFaultFlow,service));
            }

            OMElement outFaultFlow = service_element.getFirstChildWithName(
                    new QName(OUT_FAILTFLOW));
            if(outFaultFlow !=null){
                service.setFaultOutFlow(processFlow(outFaultFlow,service));
            }

            //processing operations
            Iterator opeartinsItr = service_element.getChildrenWithName(
                    new QName(OPRATIONST));
            ArrayList ops = processOpeartions(opeartinsItr);
            for (int i = 0; i < ops.size(); i++) {
                OperationDescription opeartionDesc = (OperationDescription) ops.get(i);
                ArrayList paramters = opeartionDesc.getParameters();

                // Adding wsa-maping into service
                for (int j = 0; j < paramters.size(); j++) {
                    Parameter parameter = (Parameter) paramters.get(j);
                    if(parameter.getName().equals(Constants.WSA_ACTION)){
                        service.addMapping((String)parameter.getValue(),opeartionDesc);
                    }
                }
                service.addOperation(opeartionDesc);
            }


        } catch (XMLStreamException e) {
            throw new DeploymentException(e);
        }
    }

    private ArrayList processOpeartions(Iterator opeartinsItr) throws DeploymentException {
        ArrayList operations = new ArrayList();
        while (opeartinsItr.hasNext()) {
            OMElement operation = (OMElement) opeartinsItr.next();

            // /getting opeartion name
            OMAttribute op_name_att = operation.getAttribute(
                    new QName(ATTNAME));
            if(op_name_att == null){
                throw new DeploymentException(Messages.getMessage("Invalide Operations"));
            }
            String opname = op_name_att.getValue();
            OperationDescription op_descrip = service.getOperation(new QName(opname));
            if(op_descrip == null){
                op_descrip = new OperationDescription();
                op_descrip.setName(new QName(opname));
                log.info(Messages.getMessage(DeploymentErrorMsgs.OP_NOT_FOUN_IN_WSDL, opname));
            }

            //setting the mep of the operation
            OMAttribute op_mep_att = operation.getAttribute(
                    new QName(MEP));
            if(op_mep_att !=null){
                String mep = op_mep_att.getValue();
                op_descrip.setMessageExchangePattern(mep);
            }

             //Opeartion Paramters
            Iterator paramters = operation.getChildrenWithName(
                    new QName(PARAMETERST));
            processParameters(paramters,op_descrip,service);

            // loading the message recivers
            OMElement receiverElement = operation.getFirstChildWithName(
                    new QName(MESSAGERECEIVER));
            if(receiverElement !=null){
                MessageReceiver messageReceiver = loadMessageReceiver(
                        service.getClassLoader(),receiverElement);
                op_descrip.setMessageReceiver(messageReceiver);
            }  else {
                //setting default message reciver
                MessageReceiver msgReceiver = loadDefaultMessageReciver();
                op_descrip.setMessageReceiver(msgReceiver);
            }

            //Process Module Refs
            Iterator modules = operation.getChildrenWithName(
                    new QName(MODULEST));
            processOpeasrtionModuleRefs(modules, op_descrip);

            //setting Operation phase
            if (engine !=null) {
                PhasesInfo info = engine.getPhasesinfo();
                info.setOperationPhases(op_descrip);
            }

            //adding the opeartion
            operations.add(op_descrip);
        }
        return operations;
    }

}
