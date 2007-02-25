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

import java.io.InputStream;
import java.io.StringWriter;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.Iterator;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;



import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.util.Loader;
import org.apache.axis2.deployment.util.PhasesInfo;
import org.apache.axis2.description.AxisModule;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisOperationFactory;
import org.apache.axis2.description.InOnlyAxisOperation;
import org.apache.axis2.description.PolicyInclude;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.engine.MessageReceiver;
import org.apache.axis2.i18n.Messages;
import org.apache.axis2.java.security.AccessController;
import org.apache.axis2.modules.Module;

/**
 * Builds a module description from OM
 */
public class ModuleBuilder extends DescriptionBuilder {
    private AxisModule module;

    public ModuleBuilder(InputStream serviceInputStream, AxisModule module,
                         AxisConfiguration axisConfig) {
        super(serviceInputStream, axisConfig);
        this.module = module;
    }

    private void loadModuleClass(AxisModule module, String moduleClassName)
            throws DeploymentException {
        Class moduleClass;

        try {
            if ((moduleClassName != null) && !"".equals(moduleClassName)) {
                moduleClass = Loader.loadClass(module.getModuleClassLoader(), moduleClassName);
                final Class fmoduleClass = moduleClass;
                final AxisModule fmodule = module;
                try {
                    AccessController.doPrivileged( new PrivilegedExceptionAction() {
                        public Object run() throws IllegalAccessException, InstantiationException {
                            Module new_module = (Module) fmoduleClass.newInstance();
                            fmodule.setModule(new_module);
                            return null;
                        }
                    });      
                } catch (PrivilegedActionException e) {
                    throw e.getException();
                }   
            }
        } catch (Exception e) {
            throw new DeploymentException(e.getMessage(), e);
        }
    }

    public void populateModule() throws DeploymentException {
        try {
            OMElement moduleElement = buildOM();
            // Setting Module Class , if it is there
            OMAttribute moduleClassAtt = moduleElement.getAttribute(new QName(TAG_CLASS_NAME));

            if (moduleClassAtt != null) {
                String moduleClass = moduleClassAtt.getAttributeValue();

                if ((moduleClass != null) && !"".equals(moduleClass)) {
                    loadModuleClass(module, moduleClass);
                }
            }

// process service description
            OMElement descriptionElement =
                    moduleElement.getFirstChildWithName(new QName(TAG_DESCRIPTION));

            if (descriptionElement != null) {
                OMElement descriptionValue = descriptionElement.getFirstElement();

                if (descriptionValue != null) {
                    StringWriter writer = new StringWriter();

                    descriptionValue.build();
                    descriptionValue.serialize(writer);
                    writer.flush();
                    module.setModuleDescription(writer.toString());
                } else {
                    module.setModuleDescription(descriptionElement.getText());
                }
            } else {
                module.setModuleDescription("module description not found");
            }

            // setting the PolicyInclude

            // processing <wsp:Policy> .. </..> elements
            Iterator policyElements = moduleElement.getChildrenWithName(new QName(POLICY_NS_URI, TAG_POLICY));

            if (policyElements != null && policyElements.hasNext()) {
                processPolicyElements(PolicyInclude.AXIS_MODULE_POLICY, policyElements, module.getPolicyInclude());
            }

            // processing <wsp:PolicyReference> .. </..> elements
            Iterator policyRefElements = moduleElement.getChildrenWithName(new QName(POLICY_NS_URI, TAG_POLICY_REF));

            if (policyRefElements != null && policyElements.hasNext()) {
                processPolicyRefElements(PolicyInclude.AXIS_MODULE_POLICY, policyRefElements, module.getPolicyInclude());
            }

            // processing Parameters
            // Processing service level parameters
            Iterator itr = moduleElement.getChildrenWithName(new QName(TAG_PARAMETER));

            processParameters(itr, module, module.getParent());

            // process INFLOW
            OMElement inFlow = moduleElement.getFirstChildWithName(new QName(TAG_FLOW_IN));

            if (inFlow != null) {
                module.setInFlow(processFlow(inFlow, module));
            }

            OMElement outFlow = moduleElement.getFirstChildWithName(new QName(TAG_FLOW_OUT));

            if (outFlow != null) {
                module.setOutFlow(processFlow(outFlow, module));
            }

            OMElement inFaultFlow = moduleElement.getFirstChildWithName(new QName(TAG_FLOW_IN_FAULT));

            if (inFaultFlow != null) {
                module.setFaultInFlow(processFlow(inFaultFlow, module));
            }

            OMElement outFaultFlow = moduleElement.getFirstChildWithName(new QName(TAG_FLOW_OUT_FAULT));

            if (outFaultFlow != null) {
                module.setFaultOutFlow(processFlow(outFaultFlow, module));
            }

            OMElement supportedPolicyNamespaces = moduleElement.getFirstChildWithName(new QName(TAG_SUPPORTED_POLICY_NAMESPACES));

            if (supportedPolicyNamespaces != null) {
                module.setSupportedPolicyNamespaces(processSupportedPolicyNamespaces(supportedPolicyNamespaces));
            }
            
            
            /*
             * Module description should contain a list of QName of the assertions that are local to the system.
             * These assertions are not exposed to the outside.
             */
            OMElement localPolicyAssertionElement = moduleElement.getFirstChildWithName(new QName("local-policy-assertions"));
            
            if (localPolicyAssertionElement != null) {
                module.setLocalPolicyAssertions(getLocalPolicyAssertionNames(localPolicyAssertionElement));
            }
            

            // processing Operations
            Iterator op_itr = moduleElement.getChildrenWithName(new QName(TAG_OPERATION));
            ArrayList operations = processOperations(op_itr);

            for (int i = 0; i < operations.size(); i++) {
                AxisOperation operation = (AxisOperation) operations.get(i);

                module.addOperation(operation);
            }
        } catch (XMLStreamException e) {
            throw new DeploymentException(e);
        }
    }

    private ArrayList processOperations(Iterator operationsIterator) throws DeploymentException {
        ArrayList operations = new ArrayList();

        while (operationsIterator.hasNext()) {
            OMElement operation = (OMElement) operationsIterator.next();

            //getting operation name
            OMAttribute op_name_att = operation.getAttribute(new QName(ATTRIBUTE_NAME));

            if (op_name_att == null) {
                throw new DeploymentException(
                        Messages.getMessage(
                                Messages.getMessage(
                                        DeploymentErrorMsgs.INVALID_OP, "operation name missing")));
            }

            OMAttribute op_mep_att = operation.getAttribute(new QName(TAG_MEP));
            String mepURL = null;
            AxisOperation op_descrip;

            if (op_mep_att != null) {
                mepURL = op_mep_att.getAttributeValue();
            }

            if (mepURL == null) {

                // assuming in-out MEP
                op_descrip = new InOnlyAxisOperation();
            } else {
                try {
                    op_descrip = AxisOperationFactory.getOperationDescription(mepURL);
                } catch (AxisFault axisFault) {
                    throw new DeploymentException(
                            Messages.getMessage(
                                    Messages.getMessage(
                                            DeploymentErrorMsgs.OPERATION_PROCESS_ERROR,
                                            axisFault.getMessage())));
                }
            }

            String opname = op_name_att.getAttributeValue();

            op_descrip.setName(new QName(opname));

            // Operation Parameters
            Iterator parameters = operation.getChildrenWithName(new QName(TAG_PARAMETER));
            processParameters(parameters, op_descrip, module);

            //To process wsamapping;
            processActionMappings(operation, op_descrip);
            
            // setting the MEP of the operation
            // loading the message receivers
            OMElement receiverElement = operation.getFirstChildWithName(new QName(TAG_MESSAGE_RECEIVER));

            if (receiverElement != null) {
                MessageReceiver messageReceiver =
                        loadMessageReceiver(module.getModuleClassLoader(), receiverElement);
                op_descrip.setMessageReceiver(messageReceiver);
            } else {
                // setting default message receiver
                MessageReceiver msgReceiver = loadDefaultMessageReceiver(mepURL, null);
                op_descrip.setMessageReceiver(msgReceiver);
            }
            // Process Module Refs
            Iterator modules = operation.getChildrenWithName(new QName(TAG_MODULE));
            processOperationModuleRefs(modules, op_descrip);

            // setting Operation phase
            PhasesInfo info = axisConfig.getPhasesInfo();
            try {
                info.setOperationPhases(op_descrip);
            } catch (AxisFault axisFault) {
                throw new DeploymentException(axisFault);
            }

            // adding the operation
            operations.add(op_descrip);
        }

        return operations;
    }
}
