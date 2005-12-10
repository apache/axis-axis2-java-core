/**
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
import org.apache.axis2.description.AxisMessage;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisOperationFactory;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.InOutAxisOperation;
import org.apache.axis2.description.ModuleConfiguration;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.description.ParameterInclude;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.engine.MessageReceiver;
import org.apache.axis2.i18n.Messages;
import org.apache.axis2.om.OMAttribute;
import org.apache.axis2.om.OMElement;
import org.apache.wsdl.WSDLOperation;
import org.apache.wsdl.impl.WSDLOperationImpl;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;


/**
 * This class is to convert OM->ServiceDescrption , where first create OM from services.xml and
 * then populate service description by using OM
 */


public class ServiceBuilder extends DescriptionBuilder {
    private AxisService service;
    private AxisConfiguration axisConfig;

    public ServiceBuilder(InputStream serviceInputSteram,
                          AxisConfiguration axisConfig, AxisService service) {
        super(serviceInputSteram, axisConfig);
        this.service = service;
    }

    public ServiceBuilder(AxisConfiguration axisConfig, AxisService service) {
        this.service = service;
        this.axisConfig = axisConfig;
        super.axisConfig = axisConfig;
    }

    /**
     * top most method that used to populate service from corresponding OM
     */
    public AxisService populateService(OMElement service_element) throws DeploymentException {
        try {

            //Processing service level parameters
            Iterator itr = service_element.getChildrenWithName(
                    new QName(PARAMETER));
            processParameters(itr, service, service.getParent());

            //process service description
            OMElement descriptionElement = service_element.getFirstChildWithName(
                    new QName(DESCRIPTION));
            if (descriptionElement != null) {
                OMElement descriptionValue = descriptionElement.getFirstElement();
                if (descriptionValue != null) {
                    StringWriter writer = new StringWriter();
                    descriptionValue.build();
                    descriptionValue.serialize(writer);
                    writer.flush();
                    service.setAxisServiceName(writer.toString());
                } else {
                    service.setAxisServiceName(descriptionElement.getText());
                }
            } else {
                OMAttribute serviceNameatt = service_element.getAttribute(
                        new QName(ATTNAME));
                if(serviceNameatt != null) {
                    service.setAxisServiceName(serviceNameatt.getAttributeValue());
                }                
            }

            //processing servicewide modules which required to engage gloabbly
            Iterator moduleRefs = service_element.getChildrenWithName(
                    new QName(MODULEST));
            processModuleRefs(moduleRefs);

            //process INFLOW
            OMElement inFlow = service_element.getFirstChildWithName(
                    new QName(INFLOWST));
            if (inFlow != null) {
                service.setInFlow(processFlow(inFlow, service));
            }

            OMElement outFlow = service_element.getFirstChildWithName(
                    new QName(OUTFLOWST));
            if (outFlow != null) {
                service.setOutFlow(processFlow(outFlow, service));
            }

            OMElement inFaultFlow = service_element.getFirstChildWithName(
                    new QName(IN_FAILTFLOW));
            if (inFaultFlow != null) {
                service.setFaultInFlow(processFlow(inFaultFlow, service));
            }

            OMElement outFaultFlow = service_element.getFirstChildWithName(
                    new QName(OUT_FAILTFLOW));
            if (outFaultFlow != null) {
                service.setFaultOutFlow(processFlow(outFaultFlow, service));
            }

            //processing operations
            Iterator operationsIterator = service_element.getChildrenWithName(
                    new QName(OPRATIONST));
            ArrayList ops = processOperations(operationsIterator);
            for (int i = 0; i < ops.size(); i++) {
                AxisOperation operationDesc = (AxisOperation) ops.get(i);
                ArrayList wsamappings = operationDesc.getWsamappingList();
                for (int j = 0; j < wsamappings.size(); j++) {
                    Parameter parameter = (Parameter) wsamappings.get(j);
                    service.addMapping((String) parameter.getValue(), operationDesc);
                }
                service.addOperation(operationDesc);
            }

            Iterator moduleConfigs = service_element.getChildrenWithName(new QName(MODULECONFIG));
            processServiceModuleConfig(moduleConfigs, service, service);


        } catch (XMLStreamException e) {
            throw new DeploymentException(e);
        } catch (AxisFault axisFault) {
            throw new DeploymentException(Messages.getMessage(
                    DeploymentErrorMsgs.OPERATION_PROCESS_ERROR, axisFault.getMessage()));
        }
        return service;
    }

    private ArrayList processOperations(Iterator operationsIterator) throws AxisFault {
        ArrayList operations = new ArrayList();
        while (operationsIterator.hasNext()) {
            OMElement operation = (OMElement) operationsIterator.next();

            // /getting operation name
            OMAttribute op_name_att = operation.getAttribute(
                    new QName(ATTNAME));
            if (op_name_att == null) {
                throw new DeploymentException(Messages.getMessage(Messages.getMessage(
                        DeploymentErrorMsgs.INVALID_OP
                        , "operation name missing")));
            }

            //setting the mep of the operation
            OMAttribute op_mep_att = operation.getAttribute(
                    new QName(MEP));
            String mepurl = null;
            if (op_mep_att != null) {
                mepurl = op_mep_att.getAttributeValue();
                //todo value has to be validate
                //todo
                // op_descrip.setMessageExchangePattern(mep);
            }

            String opname = op_name_att.getAttributeValue();
            WSDLOperation wsdlOperation = service.getWSDLOPOperation(new QName(opname));
//            AxisOperation op_descrip = service.getOperation(new QName(opname));
            AxisOperation op_descrip;
            if (wsdlOperation == null) {
                if (mepurl == null) {
                    // assumed MEP is in-out
                    op_descrip = new InOutAxisOperation();
                } else {
                    op_descrip = AxisOperationFactory.getOperetionDescription(mepurl);
                }
//                op_descrip = new AxisOperation();
                op_descrip.setName(new QName(opname));
                log.info(Messages.getMessage(DeploymentErrorMsgs.OP_NOT_FOUN_IN_WSDL, opname));
            } else {
                //craeting operation from existing operation
                String mep = wsdlOperation.getMessageExchangePattern();
                if (mep == null) {
                    op_descrip = new InOutAxisOperation(wsdlOperation);
                } else {
                    op_descrip = AxisOperationFactory.getOperetionDescription(mep);
                    op_descrip.setWsdloperation((WSDLOperationImpl) wsdlOperation);
                }
            }

            //Operation Parameters
            Iterator parameters = operation.getChildrenWithName(
                    new QName(PARAMETER));
            ArrayList wsamappings = processParameters(parameters, op_descrip, service);
            op_descrip.setWsamappingList(wsamappings);
            // loading the message receivers
            OMElement receiverElement = operation.getFirstChildWithName(
                    new QName(MESSAGERECEIVER));
            if (receiverElement != null) {
                MessageReceiver messageReceiver = loadMessageReceiver(
                        service.getClassLoader(), receiverElement);
                op_descrip.setMessageReceiver(messageReceiver);
            } else {
                //setting default message reciver
                MessageReceiver msgReceiver = loadDefaultMessageReceiver();
                op_descrip.setMessageReceiver(msgReceiver);
            }

            //Process Module Refs
            Iterator modules = operation.getChildrenWithName(
                    new QName(MODULEST));
            processOperationModuleRefs(modules, op_descrip);

            //processing Messages
            Iterator messages = operation.getChildrenWithName(new QName(MESSGES));
            processMessages(messages, op_descrip);

            //setting Operation phase
            if (axisConfig != null) {
                PhasesInfo info = axisConfig.getPhasesInfo();
                info.setOperationPhases(op_descrip);
            }

            Iterator moduleConfigs = operation.getChildrenWithName(new QName(MODULECONFIG));
            processOperationModuleConfig(moduleConfigs, op_descrip, op_descrip);

            //adding the operation
            operations.add(op_descrip);
        }
        return operations;
    }

    private void processMessages(Iterator messages, AxisOperation operation)
            throws DeploymentException {
        while (messages.hasNext()) {
            OMElement messageElement = (OMElement) messages.next();
            OMAttribute lable = messageElement.getAttribute(new QName(LABEL));
            if (lable == null) {
                throw new DeploymentException("message lebel can not be null");
            }
            AxisMessage message = new AxisMessage();
            Iterator parameters = messageElement.getChildrenWithName(new QName(PARAMETER));
            processParameters(parameters, message, operation);
            operation.addMessage(message, lable.getAttributeValue().trim());
        }

    }

    protected void processServiceModuleConfig(Iterator moduleConfigs,
                                              ParameterInclude parent, AxisService service)
            throws DeploymentException {
        while (moduleConfigs.hasNext()) {
            OMElement moduleConfig = (OMElement) moduleConfigs.next();
            OMAttribute moduleName_att = moduleConfig.getAttribute(
                    new QName(ATTNAME));
            if (moduleName_att == null) {
                throw new DeploymentException(Messages.getMessage(
                        DeploymentErrorMsgs.INVALID_MODULE_CONFIG));
            } else {
                String module = moduleName_att.getAttributeValue();
                ModuleConfiguration moduleConfiguration =
                        new ModuleConfiguration(new QName(module), parent);
                Iterator parameters = moduleConfig.getChildrenWithName(new QName(PARAMETER));
                processParameters(parameters, moduleConfiguration, parent);
                service.addModuleConfig(moduleConfiguration);
            }
        }
    }

    protected void processOperationModuleConfig(Iterator moduleConfigs,
                                                ParameterInclude parent,
                                                AxisOperation operation)
            throws DeploymentException {
        while (moduleConfigs.hasNext()) {
            OMElement moduleConfig = (OMElement) moduleConfigs.next();
            OMAttribute moduleName_att = moduleConfig.getAttribute(
                    new QName(ATTNAME));
            if (moduleName_att == null) {
                throw new DeploymentException(Messages.getMessage(
                        DeploymentErrorMsgs.INVALID_MODULE_CONFIG));
            } else {
                String module = moduleName_att.getAttributeValue();
                ModuleConfiguration moduleConfiguration =
                        new ModuleConfiguration(new QName(module), parent);
                Iterator parameters = moduleConfig.getChildrenWithName(new QName(PARAMETER));
                processParameters(parameters, moduleConfiguration, parent);
                operation.addModuleConfig(moduleConfiguration);
            }
        }
    }

    /**
     * To get the list og modules that is requird to be engage globally
     *
     * @param moduleRefs <code>java.util.Iterator</code>
     * @throws DeploymentException <code>DeploymentException</code>
     */
    protected void processModuleRefs(Iterator moduleRefs) throws DeploymentException {
        try {
            while (moduleRefs.hasNext()) {
                OMElement moduleref = (OMElement) moduleRefs.next();
                OMAttribute moduleRefAttribute = moduleref.getAttribute(
                        new QName(REF));
                if (moduleRefAttribute != null) {
                    String refName = moduleRefAttribute.getAttributeValue();
                    if (axisConfig.getModule(new QName(refName)) == null) {
                        throw new DeploymentException(Messages.getMessage(
                                DeploymentErrorMsgs.MODULE_NOT_FOUND, refName));
                    } else {
                        service.addModuleref(new QName(refName));
                    }
                }
            }
        } catch (AxisFault axisFault) {
            throw new DeploymentException(axisFault);
        }
    }

}
