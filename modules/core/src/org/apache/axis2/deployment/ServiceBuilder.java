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

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.deployment.util.PhasesInfo;
import org.apache.axis2.deployment.util.Utils;
import org.apache.axis2.description.*;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.engine.MessageReceiver;
import org.apache.axis2.i18n.Messages;
import org.apache.axis2.wsdl.WSDLConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ws.java2wsdl.Java2WSDLConstants;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Builds a service description from OM
 */
public class ServiceBuilder extends DescriptionBuilder {
    private static final Log log = LogFactory.getLog(ServiceBuilder.class);
    private AxisService service;

    public ServiceBuilder(AxisConfiguration axisConfig, AxisService service) {
        this.service = service;
        this.axisConfig = axisConfig;
    }

    public ServiceBuilder(InputStream serviceInputStream, AxisConfiguration axisConfig,
                          AxisService service) {
        super(serviceInputStream, axisConfig);
        this.service = service;
    }

    /**
     * Populates service from corresponding OM.
     */
    public AxisService populateService(OMElement service_element) throws DeploymentException {
        try {
            // Processing service level parameters
            Iterator itr = service_element.getChildrenWithName(new QName(TAG_PARAMETER));
            processParameters(itr, service, service.getParent());
            // process service description
            OMElement descriptionElement =
                    service_element.getFirstChildWithName(new QName(TAG_DESCRIPTION));
            if (descriptionElement != null) {
                OMElement descriptionValue = descriptionElement.getFirstElement();
                if (descriptionValue != null) {
                    StringWriter writer = new StringWriter();
                    descriptionValue.build();
                    descriptionValue.serialize(writer);
                    writer.flush();
                    service.setServiceDescription(writer.toString());
                } else {
                    service.setServiceDescription(descriptionElement.getText());
                }
            } else {
                OMAttribute serviceNameatt = service_element.getAttribute(new QName(ATTRIBUTE_NAME));

                if (serviceNameatt != null) {
                    service.setName(serviceNameatt.getAttributeValue());
                }
            }

            // Process WS-Addressing flag attribute
            OMAttribute addressingRequiredatt = service_element.getAttribute(new QName(ATTRIBUTE_WSADDRESSING));
            if (addressingRequiredatt != null) {
                String addressingRequiredString = addressingRequiredatt.getAttributeValue();
                service.setWSAddressingFlag(addressingRequiredString);
            }

            //Setting service tratget namespace if any
            OMAttribute targetNameSpace = service_element.
                    getAttribute(new QName(TARGET_NAME_SPACE));
            if (targetNameSpace != null) {
                String nameSpeceVale = targetNameSpace.getAttributeValue();
                if (nameSpeceVale != null && !"".equals(nameSpeceVale)) {
                    service.setTargetNamespace(nameSpeceVale);
                }
            } else {
                if (service.getTargetNamespace() == null ||
                        "".equals(service.getTargetNamespace())) {
                    service.setTargetNamespace(Java2WSDLConstants.DEFAULT_TARGET_NAMESPACE);
                }
            }
            //Setting schema namespece if any
            OMElement schemaElement = service_element.getFirstChildWithName(new QName(SCHEMA));
            if (schemaElement != null) {
                OMAttribute schemaNameSpace = schemaElement.
                        getAttribute(new QName(SCHEMA_NAME_SPACE));
                if (schemaNameSpace != null) {
                    String nameSpeceVale = schemaNameSpace.getAttributeValue();
                    if (nameSpeceVale != null && !"".equals(nameSpeceVale)) {
                        service.setSchematargetNamespace(nameSpeceVale);
                    }
                }
            }

            //processing Default Message receivers
            OMElement messageReceiver = service_element.getFirstChildWithName(
                    new QName(TAG_MESSAGE_RECEIVERS));
            if (messageReceiver != null) {
                HashMap mrs = processMessageReceivers(service.getClassLoader(), messageReceiver);
                Iterator keys = mrs.keySet().iterator();
                while (keys.hasNext()) {
                    String key = (String) keys.next();
                    service.addMessageReceiver(key, (MessageReceiver) mrs.get(key));
                }
            }

            //Removing exclude operations
            OMElement excludeOperations = service_element.getFirstChildWithName(
                    new QName(TAG_EXCLUDE_OPERATIONS));
            ArrayList excludeops = null;
            if (excludeOperations != null) {
                excludeops = processExcludeOperations(excludeOperations);
            }
            if (excludeops == null) {
                excludeops = new ArrayList();
            }
            excludeops.add("init");
            excludeops.add("setOperationContext");
            excludeops.add("destroy");

            //<schema targetNamespace="http://x.y.z"/>
            // setting the PolicyInclude
            // processing <wsp:Policy> .. </..> elements
            Iterator policyElements = service_element.getChildrenWithName(
                    new QName(POLICY_NS_URI, TAG_POLICY));

            if (policyElements != null && policyElements.hasNext()) {
                processPolicyElements(
                        PolicyInclude.AXIS_SERVICE_POLICY, policyElements,
                        service.getPolicyInclude());
            }

            // processing <wsp:PolicyReference> .. </..> elements
            Iterator policyRefElements = service_element.getChildrenWithName(
                    new QName(POLICY_NS_URI, TAG_POLICY_REF));

            if (policyRefElements != null && policyRefElements.hasNext()) {
                processPolicyRefElements(PolicyInclude.AXIS_SERVICE_POLICY,
                        policyRefElements, service.getPolicyInclude());
            }

            //processin Service Scop
            String sessionScope = service_element.getAttributeValue(new QName(ATTRIBUTE_SCOPE));
            if (sessionScope != null) {
                service.setScope(sessionScope);
            }

            // processing servicewide modules which required to engage gloabally
            Iterator moduleRefs = service_element.getChildrenWithName(new QName(TAG_MODULE));

            processModuleRefs(moduleRefs);

            //processing transports
            OMElement transports = service_element.getFirstChildWithName(new QName(TAG_TRANSPORTS));
            if (transports != null) {
                Iterator transport_itr = transports.getChildrenWithName(new QName(TAG_TRANSPORT));
                ArrayList trs = new ArrayList();
                while (transport_itr.hasNext()) {
                    OMElement trsEle = (OMElement) transport_itr.next();
                    trs.add(trsEle.getText());
                }
                service.setExposeTransports((String[]) trs.toArray(new String[trs.size()]));
            }
            // processing operations
            Iterator operationsIterator =
                    service_element.getChildrenWithName(new QName(TAG_OPERATION));
            ArrayList ops = processOperations(operationsIterator);

            for (int i = 0; i < ops.size(); i++) {
                AxisOperation operationDesc = (AxisOperation) ops.get(i);
                ArrayList wsamappings = operationDesc.getWsamappingList();
                if (wsamappings == null) {
                    continue;
                }
                if (service.getOperation(operationDesc.getName()) == null) {
                    service.addOperation(operationDesc);
                }
                for (int j = 0; j < wsamappings.size(); j++) {
                    String mapping = (String) wsamappings.get(j);
                    if (mapping.length() > 0) {
                        service.mapActionToOperation(mapping, operationDesc);
                    }
                }
            }

            // Generating schema for the service if the imple class is JAVA
            if (!service.isWsdlfound()) {
                //trying to generate WSDL for the service using JAM  and Java refelection
                try {
                    if (generateWsdl(service)) {
                        Utils.fillAxisService(service, axisConfig, excludeops);
                    }
                } catch (Exception e) {
                    /**
                     * I have log here if some error occours , since service impl
                     * class can alos be non-java class , so in that case
                     * it is not posible to generate scheam, so no pint of throwing that
                     * error ,  I know we have to handle this , untill that I have
                     * to log this
                     */
                    log.error(Messages.getMessage("errorinschemagen", e.getMessage()), e);
                }
            }

            for (int i = 0; i < excludeops.size(); i++) {
                String opName = (String) excludeops.get(i);
                service.removeOperation(new QName(opName));
            }

            // Set the default message receiver for the operations that were 
            // not listed in the services.xml
            Iterator operations = service.getPublishedOperations().iterator();
            while (operations.hasNext()) {
                AxisOperation operation = (AxisOperation) operations.next();
                if (operation.getMessageReceiver() == null) {
                    operation.setMessageReceiver(loadDefaultMessageReceiver(
                            operation.getMessageExchangePattern(), service));
                }
            }
            Iterator moduleConfigs = service_element.getChildrenWithName(new QName(TAG_MODULE_CONFIG));
            processServiceModuleConfig(moduleConfigs, service, service);
        } catch (XMLStreamException e) {
            throw new DeploymentException(e);
        } catch (AxisFault axisFault) {
            throw new DeploymentException(
                    Messages.getMessage(
                            DeploymentErrorMsgs.OPERATION_PROCESS_ERROR, axisFault.getMessage()));
        }
        return service;
    }


    private boolean generateWsdl(AxisService axisService) {
        Iterator operatins = axisService.getOperations();
        if (operatins.hasNext()) {
            while (operatins.hasNext()) {
                AxisOperation axisOperation = (AxisOperation) operatins
                        .next();
                if (axisOperation.getMessageReceiver() == null) {
                    continue;
                }
                String messageReceiverClass = axisOperation
                        .getMessageReceiver().getClass().getName();
                if (!("org.apache.axis2.rpc.receivers.RPCMessageReceiver"
                        .equals(messageReceiverClass)
                        || "org.apache.axis2.rpc.receivers.RPCInOnlyMessageReceiver"
                        .equals(messageReceiverClass) || "org.apache.axis2.rpc.receivers.RPCInOutAsyncMessageReceiver"
                        .equals(messageReceiverClass))) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * If there is <excludeOperations>
     * <operation>foo</operation>
     * </excludeOperations>
     * <p/>
     * Then the operation object will be removed from the AxisService , so that
     * the operation wont be exposed
     *
     * @param exculeOperations
     */
    private ArrayList processExcludeOperations(OMElement exculeOperations) {
        ArrayList exOps = new ArrayList();
        Iterator excludeOp_itr = exculeOperations.getChildrenWithName(new QName(TAG_OPERATION));
        while (excludeOp_itr.hasNext()) {
            OMElement opName = (OMElement) excludeOp_itr.next();
            exOps.add(opName.getText().trim());
            // service.removeOperation(new QName(opName.getText().trim()));
        }
        return exOps;
    }

    private void processMessages(Iterator messages, AxisOperation operation)
            throws DeploymentException {
        while (messages.hasNext()) {
            OMElement messageElement = (OMElement) messages.next();
            OMAttribute lable = messageElement.getAttribute(new QName(TAG_LABEL));

            if (lable == null) {
                throw new DeploymentException(Messages.getMessage("messagelabelcannotfound"));
            }

            AxisMessage message = operation.getMessage(lable.getAttributeValue());

            Iterator parameters = messageElement.getChildrenWithName(new QName(TAG_PARAMETER));

            // processing <wsp:Policy> .. </..> elements
            Iterator policyElements = messageElement.getChildrenWithName(new QName(POLICY_NS_URI, TAG_POLICY));

            if (policyElements != null) {
                processPolicyElements(PolicyInclude.AXIS_MESSAGE_POLICY, policyElements, message.getPolicyInclude());
            }

            // processing <wsp:PolicyReference> .. </..> elements
            Iterator policyRefElements = messageElement.getChildrenWithName(new QName(POLICY_NS_URI, TAG_POLICY_REF));

            if (policyRefElements != null) {
                processPolicyRefElements(PolicyInclude.AXIS_MESSAGE_POLICY, policyRefElements, message.getPolicyInclude());
            }

            processParameters(parameters, message, operation);

        }
    }

    /**
     * Gets the list of modules that is required to be engaged globally.
     *
     * @param moduleRefs <code>java.util.Iterator</code>
     * @throws DeploymentException <code>DeploymentException</code>
     */
    protected void processModuleRefs(Iterator moduleRefs) throws DeploymentException {
        try {
            while (moduleRefs.hasNext()) {
                OMElement moduleref = (OMElement) moduleRefs.next();
                OMAttribute moduleRefAttribute = moduleref.getAttribute(new QName(TAG_REFERENCE));

                if (moduleRefAttribute != null) {
                    String refName = moduleRefAttribute.getAttributeValue();

                    if (axisConfig.getModule(new QName(refName)) == null) {
                        throw new DeploymentException(
                                Messages.getMessage(DeploymentErrorMsgs.MODULE_NOT_FOUND, refName));
                    } else {
                        service.addModuleref(new QName(refName));
                    }
                }
            }
        } catch (AxisFault axisFault) {
            throw new DeploymentException(axisFault);
        }
    }

    protected void processOperationModuleConfig(Iterator moduleConfigs, ParameterInclude parent,
                                                AxisOperation operation)
            throws DeploymentException {
        while (moduleConfigs.hasNext()) {
            OMElement moduleConfig = (OMElement) moduleConfigs.next();
            OMAttribute moduleName_att = moduleConfig.getAttribute(new QName(ATTRIBUTE_NAME));

            if (moduleName_att == null) {
                throw new DeploymentException(
                        Messages.getMessage(DeploymentErrorMsgs.INVALID_MODULE_CONFIG));
            } else {
                String module = moduleName_att.getAttributeValue();
                ModuleConfiguration moduleConfiguration =
                        new ModuleConfiguration(new QName(module), parent);
                Iterator parameters = moduleConfig.getChildrenWithName(new QName(TAG_PARAMETER));

                processParameters(parameters, moduleConfiguration, parent);
                operation.addModuleConfig(moduleConfiguration);
            }
        }
    }

    private ArrayList processOperations(Iterator operationsIterator) throws AxisFault {
        ArrayList operations = new ArrayList();
        while (operationsIterator.hasNext()) {
            OMElement operation = (OMElement) operationsIterator.next();
            // /getting operation name
            OMAttribute op_name_att = operation.getAttribute(new QName(ATTRIBUTE_NAME));
            if (op_name_att == null) {
                throw new DeploymentException(
                        Messages.getMessage(
                                Messages.getMessage(
                                        DeploymentErrorMsgs.INVALID_OP, "operation name missing")));
            }

            // setting the mep of the operation
            OMAttribute op_mep_att = operation.getAttribute(new QName(TAG_MEP));
            String mepurl = null;

            if (op_mep_att != null) {
                mepurl = op_mep_att.getAttributeValue();
            }

            String opname = op_name_att.getAttributeValue();
            AxisOperation op_descrip;
            op_descrip = service.getOperation(new QName(opname));
            if (op_descrip == null) {
                if (mepurl == null) {
                    // assumed MEP is in-out
                    op_descrip = new InOutAxisOperation();
                    op_descrip.setParent(service);

                } else {
                    op_descrip = AxisOperationFactory.getOperationDescription(mepurl);
                }
                op_descrip.setName(new QName(opname));
                String MEP = op_descrip.getMessageExchangePattern();
                if (WSDLConstants.WSDL20_2004Constants.MEP_URI_IN_ONLY.equals(MEP) ||
                        WSDLConstants.WSDL20_2004Constants.MEP_URI_IN_OPTIONAL_OUT.equals(MEP) ||
                        WSDLConstants.WSDL20_2004Constants.MEP_URI_OUT_OPTIONAL_IN.equals(MEP) ||
                        WSDLConstants.WSDL20_2004Constants.MEP_URI_ROBUST_OUT_ONLY.equals(MEP) ||
                        WSDLConstants.WSDL20_2004Constants.MEP_URI_ROBUST_IN_ONLY.equals(MEP) ||
                        WSDLConstants.WSDL20_2004Constants.MEP_URI_IN_OUT.equals(MEP)) {
                    AxisMessage inaxisMessage = op_descrip
                            .getMessage(WSDLConstants.MESSAGE_LABEL_IN_VALUE);
                    if (inaxisMessage != null) {
                        inaxisMessage.setName(opname + Java2WSDLConstants.MESSAGE_SUFFIX);
                    }
                }

                if (WSDLConstants.WSDL20_2004Constants.MEP_URI_OUT_ONLY.equals(MEP) ||
                        WSDLConstants.WSDL20_2004Constants.MEP_URI_OUT_OPTIONAL_IN.equals(MEP) ||
                        WSDLConstants.WSDL20_2004Constants.MEP_URI_IN_OPTIONAL_OUT.equals(MEP) ||
                        WSDLConstants.WSDL20_2004Constants.MEP_URI_ROBUST_OUT_ONLY.equals(MEP) ||
                        WSDLConstants.WSDL20_2004Constants.MEP_URI_ROBUST_IN_ONLY.equals(MEP) ||
                        WSDLConstants.WSDL20_2004Constants.MEP_URI_IN_OUT.equals(MEP)) {
                    AxisMessage outAxisMessage = op_descrip
                            .getMessage(WSDLConstants.MESSAGE_LABEL_OUT_VALUE);
                    if (outAxisMessage != null) {
                        outAxisMessage.setName(opname + Java2WSDLConstants.RESPONSE);
                    }
                }
            }

            // setting the PolicyInclude

            // processing <wsp:Policy> .. </..> elements
            Iterator policyElements = operation.getChildrenWithName(new QName(POLICY_NS_URI, TAG_POLICY));

            if (policyElements != null && policyElements.hasNext()) {
                processPolicyElements(PolicyInclude.AXIS_OPERATION_POLICY, policyElements, op_descrip.getPolicyInclude());
            }

            // processing <wsp:PolicyReference> .. </..> elements
            Iterator policyRefElements = operation.getChildrenWithName(new QName(POLICY_NS_URI, TAG_POLICY_REF));

            if (policyRefElements != null && policyRefElements.hasNext()) {
                processPolicyRefElements(PolicyInclude.AXIS_OPERATION_POLICY, policyRefElements, op_descrip.getPolicyInclude());
            }

            // Operation Parameters
            Iterator parameters = operation.getChildrenWithName(new QName(TAG_PARAMETER));
            processParameters(parameters, op_descrip, service);
            //To process wsamapping;
            Iterator mappingIterator = operation.getChildrenWithName(new QName(Constants.ACTION_MAPPING));
            if (mappingIterator != null) {
                ArrayList wsamappings = processWsaMapping(mappingIterator);
                op_descrip.setWsamappingList(wsamappings);
            }

            // loading the message receivers
            OMElement receiverElement = operation.getFirstChildWithName(new QName(TAG_MESSAGE_RECEIVER));

            if (receiverElement != null) {
                MessageReceiver messageReceiver = loadMessageReceiver(service.getClassLoader(),
                        receiverElement);

                op_descrip.setMessageReceiver(messageReceiver);
            } else {
                // setting default message receiver
                MessageReceiver msgReceiver = loadDefaultMessageReceiver(
                        op_descrip.getMessageExchangePattern()
                        , service);
                op_descrip.setMessageReceiver(msgReceiver);
            }

            // Process Module Refs
            Iterator modules = operation.getChildrenWithName(new QName(TAG_MODULE));

            processOperationModuleRefs(modules, op_descrip);

            // processing Messages
            Iterator messages = operation.getChildrenWithName(new QName(TAG_MESSAGE));

            processMessages(messages, op_descrip);

            // setting Operation phase
            if (axisConfig != null) {
                PhasesInfo info = axisConfig.getPhasesInfo();

                info.setOperationPhases(op_descrip);
            }
            Iterator moduleConfigs = operation.getChildrenWithName(new QName(TAG_MODULE_CONFIG));
            processOperationModuleConfig(moduleConfigs, op_descrip, op_descrip);
            // adding the operation
            operations.add(op_descrip);
        }
        return operations;
    }


    protected void processServiceModuleConfig(Iterator moduleConfigs, ParameterInclude parent,
                                              AxisService service)
            throws DeploymentException {
        while (moduleConfigs.hasNext()) {
            OMElement moduleConfig = (OMElement) moduleConfigs.next();
            OMAttribute moduleName_att = moduleConfig.getAttribute(new QName(ATTRIBUTE_NAME));

            if (moduleName_att == null) {
                throw new DeploymentException(
                        Messages.getMessage(DeploymentErrorMsgs.INVALID_MODULE_CONFIG));
            } else {
                String module = moduleName_att.getAttributeValue();
                ModuleConfiguration moduleConfiguration =
                        new ModuleConfiguration(new QName(module), parent);
                Iterator parameters = moduleConfig.getChildrenWithName(new QName(TAG_PARAMETER));

                processParameters(parameters, moduleConfiguration, parent);
                service.addModuleConfig(moduleConfiguration);
            }
        }
    }

}
