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
import org.apache.axis2.Constants;
import org.apache.axis2.description.*;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.engine.MessageReceiver;
import org.apache.axis2.i18n.Messages;
import org.apache.axis2.om.OMAbstractFactory;
import org.apache.axis2.om.OMAttribute;
import org.apache.axis2.om.OMElement;
import org.apache.axis2.om.OMFactory;
import org.apache.axis2.om.impl.llom.builder.StAXOMBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ws.policy.Policy;
import org.apache.ws.policy.PolicyReference;
import org.apache.ws.policy.util.OMPolicyReader;
import org.apache.ws.policy.util.PolicyFactory;
import org.apache.wsdl.WSDLConstants;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * This class does the common tasks for all *Builder class.
 */
public class DescriptionBuilder implements DeploymentConstants {

    protected Log log = LogFactory.getLog(getClass());
    protected AxisConfiguration axisConfig;

    protected InputStream descriptionStream;

    public DescriptionBuilder() {
    }

    public DescriptionBuilder(InputStream serviceInputStream, AxisConfiguration axisConfig) {
        this.descriptionStream = serviceInputStream;
        this.axisConfig = axisConfig;
    }

    /**
     * Creates OMElement for a given description document (axis2.xml , services.xml and
     * module.xml).
     *
     * @return Returns <code>OMElement</code> .
     * @throws javax.xml.stream.XMLStreamException
     *
     */
    public OMElement buildOM() throws XMLStreamException {
        XMLStreamReader xmlReader =
                XMLInputFactory.newInstance().createXMLStreamReader(descriptionStream);
        OMFactory fac = OMAbstractFactory.getOMFactory();
        StAXOMBuilder staxOMBuilder = new StAXOMBuilder(fac, xmlReader);
        OMElement element = staxOMBuilder.getDocumentElement();

        element.build();

        return element;
    }

    /**
     * Loads default message receivers. First searches in Axiservice for the
     * given mepURL, if not found searches in AxisConfiguration with the given mepURL.
     *
     * @param mepURL  : can be null
     * @param service :  This can be null <code>AxisService</code>
     */
    protected MessageReceiver loadDefaultMessageReceiver(String mepURL, AxisService service) {
        MessageReceiver messageReceiver;
        if (mepURL == null) {
            mepURL = WSDLConstants.MEP_URI_IN_OUT;
        }
        if (service != null) {
            messageReceiver = service.getMessageReceiver(mepURL);
            if (messageReceiver != null)
                return messageReceiver;
        }
        return axisConfig.getMessageReceiver(mepURL);
    }

    /**
     * Processes default message receivers specified either in axis2.xml or services.xml.
     *
     * @param messageReceivers
     */
    protected HashMap processMessageReceivers(OMElement messageReceivers) throws DeploymentException {
        HashMap mr_mep = new HashMap();
        Iterator msgRecives = messageReceivers.getChildrenWithName(new QName(TAG_MESSAGE_RECEIVER));
        while (msgRecives.hasNext()) {
            OMElement msgRev = (OMElement) msgRecives.next();
            MessageReceiver msgrecivere =
                    loadMessageReceiver(Thread.currentThread().getContextClassLoader(), msgRev);
            OMAttribute mepAtt = msgRev.getAttribute(new QName(TAG_MEP));
            mr_mep.put(mepAtt.getAttributeValue(), msgrecivere);
        }
        return mr_mep;
    }

    protected MessageReceiver loadMessageReceiver(ClassLoader loader, OMElement reciverElement)
            throws DeploymentException {
        OMAttribute recieverName = reciverElement.getAttribute(new QName(TAG_CLASS_NAME));
        String className = recieverName.getAttributeValue();
        MessageReceiver receiver = null;

        try {
            Class messageReceiver;

            if ((className != null) && !"".equals(className)) {
                messageReceiver = Class.forName(className, true, loader);
                receiver = (MessageReceiver) messageReceiver.newInstance();
            }
        } catch (ClassNotFoundException e) {
            throw new DeploymentException(
                    Messages.getMessage(
                            DeploymentErrorMsgs.ERROR_IN_LOADING_MESSAGE_RECEIVER, "ClassNotFoundException", className));
        } catch (IllegalAccessException e) {
            throw new DeploymentException(
                    Messages.getMessage(
                            DeploymentErrorMsgs.ERROR_IN_LOADING_MESSAGE_RECEIVER, "IllegalAccessException", className));
        } catch (InstantiationException e) {
            throw new DeploymentException(
                    Messages.getMessage(
                            DeploymentErrorMsgs.ERROR_IN_LOADING_MESSAGE_RECEIVER, "InstantiationException", className));
        }

        return receiver;
    }

    /**
     * Processes flow elements in services.xml .
     *
     * @param flowelement <code>OMElement</code>
     * @return Returns Flow.
     * @throws DeploymentException <code>DeploymentException</code>
     */
    protected Flow processFlow(OMElement flowelement, ParameterInclude parent)
            throws DeploymentException {
        Flow flow = new FlowImpl();

        if (flowelement == null) {
            return flow;
        }

        Iterator handlers = flowelement.getChildrenWithName(new QName(TAG_HANDLER));

        while (handlers.hasNext()) {
            OMElement handlerElement = (OMElement) handlers.next();

            flow.addHandler(processHandler(handlerElement, parent));
        }

        return flow;
    }

    /**
     * Processes Handler element.
     *
     * @param handler_element <code>OMElement</code>
     * @return Returns HandlerDescription.
     * @throws DeploymentException <code>DeploymentException</code>
     */
    protected HandlerDescription processHandler(OMElement handler_element, ParameterInclude parent)
            throws DeploymentException {
        HandlerDescription handler = new HandlerDescription();

        // Setting Handler name
        OMAttribute name_attribute = handler_element.getAttribute(new QName(ATTRIBUTE_NAME));

        if (name_attribute == null) {
            throw new DeploymentException(Messages.getMessage(DeploymentErrorMsgs.INVALID_HANDLER,
                    "Name missing"));
        } else {
            handler.setName(new QName(name_attribute.getAttributeValue()));
        }

        // Setting Handler Class name
        OMAttribute class_attribute = handler_element.getAttribute(new QName(TAG_CLASS_NAME));

        if (class_attribute == null) {
            throw new DeploymentException((Messages.getMessage(DeploymentErrorMsgs.INVALID_HANDLER,
                    "class name is missing")));
        } else {
            handler.setClassName(class_attribute.getAttributeValue());
        }

        // processing phase Rules (order)
        OMElement order_element = handler_element.getFirstChildWithName(new QName(TAG_ORDER));

        if (order_element == null) {
            throw new DeploymentException((Messages.getMessage(DeploymentErrorMsgs.INVALID_HANDLER,
                    "phase rule has not been specified")));
        } else {
            Iterator order_itr = order_element.getAllAttributes();

            while (order_itr.hasNext()) {
                OMAttribute orderAttribute = (OMAttribute) order_itr.next();
                String name = orderAttribute.getQName().getLocalPart();
                String value = orderAttribute.getAttributeValue();

                if (TAG_AFTER.equals(name)) {
                    handler.getRules().setAfter(value);
                } else if (TAG_BEFORE.equals(name)) {
                    handler.getRules().setBefore(value);
                } else if (TAG_PHASE.equals(name)) {
                    handler.getRules().setPhaseName(value);
                } else if (TAG_PHASE_FIRST.equals(name)) {
                    String boolval = getValue(value);

                    if (boolval.equals(BOOLEAN_TRUE)) {
                        handler.getRules().setPhaseFirst(true);
                    } else if (boolval.equals(BOOLEAN_FALSE)) {
                        handler.getRules().setPhaseFirst(false);
                    }
                } else if (TAG_PHASE_LAST.equals(name)) {
                    String boolval = getValue(value);

                    if (boolval.equals(BOOLEAN_TRUE)) {
                        handler.getRules().setPhaseLast(true);
                    } else if (boolval.equals(BOOLEAN_FALSE)) {
                        handler.getRules().setPhaseLast(false);
                    }
                }
            }

            Iterator parameters = handler_element.getChildrenWithName(new QName(TAG_PARAMETER));

            processParameters(parameters, handler, parent);
        }

        handler.setParent(parent);

        return handler;
    }

    protected void processOperationModuleRefs(Iterator moduleRefs, AxisOperation operation)
            throws DeploymentException {
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
                        operation.addModule(new QName(refName));
                    }
                }
            }
        } catch (AxisFault axisFault) {
            throw new DeploymentException(Messages.getMessage(DeploymentErrorMsgs.MODULE_NOT_FOUND,
                    axisFault.getMessage()));
        }
    }

    /**
     * Gets the Parameter object from the OM.
     *
     * @param parameters       <code>Parameter</code>
     * @param parameterInclude <code>ParameterInclude</code>
     * @param parent           <code>ParameterInclude</code>
     * @return  list of WSA action parameters
     */
    protected ArrayList processParameters(Iterator parameters, ParameterInclude parameterInclude,
                                          ParameterInclude parent)
            throws DeploymentException {
        ArrayList wsamapping = new ArrayList();

        while (parameters.hasNext()) {

            // this is to check whether some one has locked the parmter at the top level
            OMElement parameterElement = (OMElement) parameters.next();
            Parameter parameter = new ParameterImpl();

            // setting parameterElement
            parameter.setParameterElement(parameterElement);

            // setting parameter Name
            OMAttribute paramName = parameterElement.getAttribute(new QName(ATTRIBUTE_NAME));

            if (paramName == null) {
                throw new DeploymentException(
                        Messages.getMessage(DeploymentErrorMsgs.BAD_PARAMETER_ARGUMENT));
            }

            parameter.setName(paramName.getAttributeValue());

            // setting parameter Value (the chiled elemnt of the parameter)
            OMElement paramValue = parameterElement.getFirstElement();

            if (paramValue != null) {
                parameter.setValue(parameterElement);
                parameter.setParameterType(Parameter.OM_PARAMETER);
            } else {
                String paratextValue = parameterElement.getText();

                parameter.setValue(paratextValue);
                parameter.setParameterType(Parameter.TEXT_PARAMETER);
            }

            // setting locking attribute
            OMAttribute paramLocked = parameterElement.getAttribute(new QName(ATTRIBUTE_LOCKED));
            Parameter parentParam = null;

            if (parent != null) {
                parentParam = parent.getParameter(parameter.getName());
            }

            if (paramLocked != null) {
                String lockedValue = paramLocked.getAttributeValue();

                if (BOOLEAN_TRUE.equals(lockedValue)) {

                    // if the parameter is locked at some level parameter value replace by that
                    if ((parent != null) && parent.isParameterLocked(parameter.getName())) {
                        throw new DeploymentException(
                                Messages.getMessage(
                                        DeploymentErrorMsgs.CONFIG_NOT_FOUND, parameter.getName()));
                    } else {
                        parameter.setLocked(true);
                    }
                } else {
                    parameter.setLocked(false);
                }
            }

            if (Constants.WSA_ACTION.equals(paramName.getAttributeValue())) {
                wsamapping.add(parameter);

                // no need to add this parameter , since this is just for mapping
                continue;
            }

            try {
                if (parent != null) {
                    if ((parentParam == null) || !parent.isParameterLocked(parameter.getName())) {
                        parameterInclude.addParameter(parameter);
                    }
                } else {
                    parameterInclude.addParameter(parameter);
                }
            } catch (AxisFault axisFault) {
                throw new DeploymentException(axisFault);
            }
        }

        return wsamapping;
    }
    
    protected void processPolicyElements(int type, Iterator policyElements,
            PolicyInclude policyInclude) {
        OMPolicyReader reader = (OMPolicyReader) PolicyFactory
                .getPolicyReader(PolicyFactory.OM_POLICY_READER);
        OMElement policyElement;

        while (policyElements.hasNext()) {
            Policy p = reader.readPolicy((OMElement) policyElements.next());
            policyInclude.addPolicyElement(type, p);
        }
    }

    protected void processPolicyRefElements(int type, Iterator policyRefElements,
            PolicyInclude policyInclude) {
        OMPolicyReader reader = (OMPolicyReader) PolicyFactory
                .getPolicyReader(PolicyFactory.OM_POLICY_READER);
        OMElement policyRefElement;

        while (policyRefElements.hasNext()) {
            PolicyReference policyReference = reader
                    .readPolicyReference((OMElement) policyRefElements.next());
            policyInclude.addPolicyRefElement(type, policyReference);
        }
    }
    

    /**
     * Gets the short file name. Short file name is the name before the dot.
     *
     * @param fileName
     * @return Returns String.
     */
    public static String getShortFileName(String fileName) {
        char seperator = SEPARATOR_DOT;
        String value;
        int index = fileName.lastIndexOf(seperator);

        if (index > 0) {
            value = fileName.substring(0, index);

            return value;
        }

        return fileName;
    }

    /**
     * Gets the value of an attribute.
     * eg xsd:anyVal --> anyVal
     *
     * @return Returns String.
     */
    protected String getValue(String in) {
        char seperator = SEPARATOR_COLON;
        String value;
        int index = in.indexOf(seperator);

        if (index > 0) {
            value = in.substring(index + 1, in.length());

            return value;
        }

        return in;
    }
}
