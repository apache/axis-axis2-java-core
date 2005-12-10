package org.apache.axis2.description;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.OperationContext;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.engine.AxisError;
import org.apache.axis2.engine.MessageReceiver;
import org.apache.axis2.i18n.Messages;
import org.apache.axis2.om.OMElement;
import org.apache.axis2.phaseresolver.PhaseResolver;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wsdl.MessageReference;
import org.apache.wsdl.WSDLConstants;
import org.apache.wsdl.WSDLExtensibilityAttribute;
import org.apache.wsdl.WSDLExtensibilityElement;
import org.apache.wsdl.WSDLFaultReference;
import org.apache.wsdl.WSDLFeature;
import org.apache.wsdl.WSDLOperation;
import org.apache.wsdl.WSDLProperty;
import org.apache.wsdl.impl.WSDLOperationImpl;
import org.w3c.dom.Document;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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

public abstract class AxisOperation implements
        ParameterInclude, DescriptionConstants,
        WSDLConstants, WSDLOperation {
    private Log log = LogFactory.getLog(getClass());

    private MessageReceiver messageReceiver;
//    private ArrayList remainingPhasesInFlow;
//    private ArrayList phasesOutFlow;
//    private ArrayList phasesInFaultFlow;
//    private ArrayList phasesOutFaultFlow;

    private HashMap moduleConfigmap;

    private int mep = MEP_CONSTANT_INVALID;

    private WSDLOperationImpl wsdloperation;

    private AxisService parent;
    private ArrayList wsamappingList;

    //To store deploytime module refs
    private ArrayList modulerefs;
    //to hide control operation , operation which added by RM like module
    private boolean controlOperation = false;
    //to store engaged modules
    private ArrayList engagedModules = new ArrayList();


    public AxisOperation(WSDLOperation wsdloperation) {
        this.wsdloperation = (WSDLOperationImpl) wsdloperation;
        this.setMessageExchangePattern(MEP_URI_IN_OUT);
        this.setComponentProperty(PARAMETER_KEY, new ParameterIncludeImpl());
        modulerefs = new ArrayList();
        moduleConfigmap = new HashMap();
    }

    public AxisOperation() {
        this(new WSDLOperationImpl());
    }

    public AxisOperation(QName name) {
        this();
        this.setName(name);
    }


    /**
     * To ebgage a module it is reuired to use this method
     *
     * @param moduleref
     * @throws AxisFault
     */
    public final void engageModule(ModuleDescription moduleref,
                                   AxisConfiguration axisConfig) throws AxisFault {
        if (moduleref == null) {
            return;
        }
        boolean needToadd = true;
        Iterator module_itr = engagedModules.iterator();
        while (module_itr.hasNext()) {
            ModuleDescription module = (ModuleDescription) module_itr.next();
            if (module.getName().equals(moduleref.getName())) {
                log.info(moduleref.getName().getLocalPart() +
                        " module has alredy engaged to the operation" +
                        "  operation terminated !!!");
                needToadd = false;
                // return;
            }
        }
        new PhaseResolver(axisConfig).engageModuleToOperation(this, moduleref);
        if (needToadd) {
            engagedModules.add(moduleref);
        }
    }

    /*
    * (non-Javadoc)
    *
    * @see org.apache.axis2.description.AxisService#getEngadgedModules()
    */

    /**
     * Method getEngadgedModules
     */
    public Collection getEngagedModules() {
        return engagedModules;
    }

    /**
     * Method addParameter
     *
     * @param param Parameter that will be added
     */
    public void addParameter(Parameter param) throws AxisFault {
        if (param == null) {
            return;
        }
        if (isParameterLocked(param.getName())) {
            throw new AxisFault("Parmter is locked can not overide: " + param.getName());
        } else {
            ParameterIncludeImpl paramInclude = (ParameterIncludeImpl) this
                    .getComponentProperty(PARAMETER_KEY);
            paramInclude.addParameter(param);
        }
    }

    /**
     * Method getParameter
     *
     * @param name Name of the parameter
     */
    public Parameter getParameter(String name) {
        ParameterIncludeImpl paramInclude = (ParameterIncludeImpl) this
                .getComponentProperty(PARAMETER_KEY);
        return paramInclude.getParameter(name);
    }

    public ArrayList getParameters() {
        ParameterIncludeImpl paramInclude = (ParameterIncludeImpl) this
                .getComponentProperty(PARAMETER_KEY);
        return paramInclude.getParameters();
    }

    public MessageReceiver getMessageReceiver() {
        return messageReceiver;
    }

    public void deserializeParameters(OMElement parameterElement) throws AxisFault {
        ParameterIncludeImpl paramInclude = (ParameterIncludeImpl) this
                .getComponentProperty(PARAMETER_KEY);
        paramInclude.deserializeParameters(parameterElement);
    }

    public void setMessageReceiver(MessageReceiver messageReceiver) {
        this.messageReceiver = messageReceiver;
    }

    /**
     * This method will simply map the String URI of the Message exchange
     * pattern to a integer. Further in the first lookup it will cash the looked
     * up value so that the subsequent method calls will be extremely efficient.
     */
    public int getAxisSpecifMEPConstant() {
        if (this.mep != MEP_CONSTANT_INVALID) {
            return this.mep;
        }

        int temp = MEP_CONSTANT_INVALID;

        if (MEP_URI_IN_OUT.equals(getMessageExchangePattern())) {
            temp = MEP_CONSTANT_IN_OUT;
        } else if (MEP_URI_IN_ONLY.equals(getMessageExchangePattern())) {
            temp = MEP_CONSTANT_IN_ONLY;
        } else if (MEP_URI_IN_OPTIONAL_OUT.equals(getMessageExchangePattern())) {
            temp = MEP_CONSTANT_IN_OPTIONAL_OUT;
        } else if (MEP_URI_OUT_IN.equals(getMessageExchangePattern())) {
            temp = MEP_CONSTANT_OUT_IN;
        } else if (MEP_URI_OUT_ONLY.equals(getMessageExchangePattern())) {
            temp = MEP_CONSTANT_OUT_ONLY;
        } else if (MEP_URI_OUT_OPTIONAL_IN.equals(getMessageExchangePattern())) {
            temp = MEP_CONSTANT_OUT_OPTIONAL_IN;
        } else if (MEP_URI_ROBUST_IN_ONLY.equals(getMessageExchangePattern())) {
            temp = MEP_CONSTANT_ROBUST_IN_ONLY;
        } else if (MEP_URI_ROBUST_OUT_ONLY.equals(getMessageExchangePattern())) {
            temp = MEP_CONSTANT_ROBUST_OUT_ONLY;
        }

        if (temp == MEP_CONSTANT_INVALID) {
            throw new AxisError(
                    "Could not Map the MEP URI to a axis MEP constant value");
        }
        this.mep = temp;
        return this.mep;

    }


    public abstract ArrayList getPhasesInFaultFlow();

    public abstract ArrayList getPhasesOutFaultFlow();

    public abstract ArrayList getPhasesOutFlow();

    public abstract ArrayList getRemainingPhasesInFlow();

    public abstract AxisMessage getMessage(String label);

    public abstract void setPhasesInFaultFlow(ArrayList list);

    public abstract void setPhasesOutFaultFlow(ArrayList list);

    public abstract void setPhasesOutFlow(ArrayList list);

    public abstract void setRemainingPhasesInFlow(ArrayList list);

    public abstract void addMessage(AxisMessage message, String label);


    public void addModule(QName moduleName) {
        modulerefs.add(moduleName);
    }

    public ArrayList getModuleRefs() {
        return modulerefs;
    }

    public AxisService getParent() {
        return parent;
    }

    public void setParent(AxisService parent) {
        this.parent = parent;
    }

    //to check whether a given parameter is locked
    public boolean isParameterLocked(String parameterName) {
        // checking the locked value of parent
        boolean loscked = false;
        if (getParent() != null) {
            loscked = getParent().isParameterLocked(parameterName);
        }
        if (loscked) {
            return true;
        } else {
            Parameter parameter = getParameter(parameterName);
            return parameter != null && parameter.isLocked();
        }
    }

    /**
     * Adding module configuration , if there is moduleConfig tag in operation
     *
     * @param moduleConfiguration
     */
    public void addModuleConfig(ModuleConfiguration moduleConfiguration) {
        moduleConfigmap.put(moduleConfiguration.getModuleName(), moduleConfiguration);
    }

    public ModuleConfiguration getModuleConfig(QName moduleName) {
        return (ModuleConfiguration) moduleConfigmap.get(moduleName);
    }


    /**
     * To add a message Context into a operation context depending on MEPs this method has to
     * be overided.
     * Depending on the mep operation description know how to fill the message conetxt map
     * in operationContext.
     * As an exmple if the MEP is IN-OUT then depending on messagelbl operation description
     * should know how to keep them in corret locations
     *
     * @param msgContext <code>MessageContext</code>
     * @param opContext  <code>OperationContext</code>
     * @throws AxisFault <code>AxisFault</code>
     */
    public abstract void addMessageContext(MessageContext msgContext, OperationContext opContext)
            throws AxisFault;

    public List getInfaults() {
        return wsdloperation.getInfaults();
    }

    public void setInfaults(List infaults) {
        wsdloperation.setInfaults(infaults);
    }

    public MessageReference getInputMessage() {
        return wsdloperation.getInputMessage();
    }

    public void setInputMessage(MessageReference inputMessage) {
        wsdloperation.setInputMessage(inputMessage);
    }

    public String getMessageExchangePattern() {
        return wsdloperation.getMessageExchangePattern();
    }

    public void setMessageExchangePattern(String messageExchangePattern) {
        wsdloperation.setMessageExchangePattern(messageExchangePattern);
    }

    public QName getName() {
        return wsdloperation.getName();
    }

    public void setName(QName name) {
        wsdloperation.setName(name);
    }

    public List getOutfaults() {
        return wsdloperation.getOutfaults();
    }

    public void setOutfaults(List outfaults) {
        wsdloperation.setOutfaults(outfaults);
    }

    public MessageReference getOutputMessage() {
        return wsdloperation.getOutputMessage();
    }

    public void setOutputMessage(MessageReference outputMessage) {
        wsdloperation.setOutputMessage(outputMessage);
    }

    public boolean isSafe() {
        return wsdloperation.isSafe();
    }

    public void setSafety(boolean safe) {
        wsdloperation.setSafety(safe);
    }

    public String getStyle() {
        return wsdloperation.getStyle();
    }

    public void setStyle(String style) {
        wsdloperation.setStyle(style);
    }

    public String getTargetnamespace() {
        return wsdloperation.getTargetnamespace();
    }

    public void addInFault(WSDLFaultReference inFault) {
        wsdloperation.addInFault(inFault);
    }

    public void addOutFault(WSDLFaultReference outFault) {
        wsdloperation.addOutFault(outFault);
    }

    public void addFeature(WSDLFeature feature) {
        wsdloperation.addFeature(feature);
    }

    public List getFeatures() {
        return wsdloperation.getFeatures();
    }

    public void addProperty(WSDLProperty wsdlProperty) {
        wsdloperation.addProperty(wsdlProperty);
    }

    public List getProperties() {
        return wsdloperation.getProperties();
    }

    public Document getDocumentation() {
        return wsdloperation.getDocumentation();
    }

    public void setDocumentation(Document documentation) {
        wsdloperation.setDocumentation(documentation);
    }

    public HashMap getComponentProperties() {
        return wsdloperation.getComponentProperties();
    }

    public void setComponentProperties(HashMap properties) {
        wsdloperation.setComponentProperties(properties);
    }

    public void setComponentProperty(Object key, Object obj) {
        wsdloperation.setComponentProperty(key, obj);
    }

    public Object getComponentProperty(Object key) {
        return wsdloperation.getComponentProperty(key);
    }

    public void addExtensibilityElement(WSDLExtensibilityElement element) {
        wsdloperation.addExtensibilityElement(element);
    }

    public List getExtensibilityElements() {
        return wsdloperation.getExtensibilityElements();
    }

    public void addExtensibleAttributes(WSDLExtensibilityAttribute attribute) {
        wsdloperation.addExtensibleAttributes(attribute);
    }

    public List getExtensibilityAttributes() {
        return wsdloperation.getExtensibilityAttributes();
    }

    public Map getMetadataBag() {
        return wsdloperation.getMetadataBag();
    }

    public void setMetadataBag(Map meMap) {
        wsdloperation.setMetadataBag(meMap);
    }


    /**
     * This method is responsible for finding a MEPContext for an incomming
     * messages. An incomming message can be of two states.
     * <p/>
     * 1)This is a new incomming message of a given MEP. 2)This message is a
     * part of an MEP which has already begun.
     * <p/>
     * The method is special cased for the two MEPs
     * <p/>
     * #IN_ONLY #IN_OUT
     * <p/>
     * for two reasons. First reason is the wide usage and the second being that
     * the need for the MEPContext to be saved for further incomming messages.
     * <p/>
     * In the event that MEP of this operation is different from the two MEPs
     * deafulted above the decession of creating a new or this message relates
     * to a MEP which already in business is decided by looking at the WSA
     * Relates TO of the incomming message.
     *
     * @param msgContext
     */
    public OperationContext findOperationContext(MessageContext msgContext,
                                                 ServiceContext serviceContext) throws AxisFault {
        OperationContext operationContext;

        if (null == msgContext.getRelatesTo()) {
            //Its a new incomming message so get the factory to create a new
            // one
            operationContext = new OperationContext(this, serviceContext);
        } else {
            // So this message is part of an ongoing MEP
            //			operationContext =
            ConfigurationContext configContext = msgContext.getConfigurationContext();
            operationContext =
                    configContext.getOperationContext(msgContext.getRelatesTo().getValue());

            if (null == operationContext) {
                throw new AxisFault(Messages.getMessage("cannotCorrelateMsg",
                        this.getName().toString(), msgContext.getRelatesTo().getValue()));
            }

        }

        registerOperationContext(msgContext, operationContext);

        return operationContext;

    }

    /**
     * This will not create a new operation context if there is no one already.
     *
     * @param msgContext
     * @return
     * @throws AxisFault
     */
    public OperationContext findForExistingOperationContext(
            MessageContext msgContext) throws AxisFault {
        OperationContext operationContext;
        if ((operationContext = msgContext.getOperationContext()) != null) {
            return operationContext;
        }

        if (null == msgContext.getRelatesTo()) {
            return null;
        } else {
            // So this message is part of an ongoing MEP
            //			operationContext =
            ConfigurationContext configContext = msgContext.getConfigurationContext();
            operationContext =
                    configContext.getOperationContext(msgContext.getRelatesTo().getValue());

            if (null == operationContext) {
                throw new AxisFault(Messages.getMessage("cannotCorrealteMsg",
                        this.getName().toString(), msgContext.getRelatesTo().getValue()));
            }

        }


        return operationContext;

    }

    public void registerOperationContext(MessageContext msgContext,
                                         OperationContext operationContext) throws AxisFault {
        msgContext.getConfigurationContext().registerOperationContext(
                msgContext.getMessageID(), operationContext);
        operationContext.addMessageContext(msgContext);
        msgContext.setOperationContext(operationContext);
        if (operationContext.isComplete()) {
            operationContext.cleanup();
        }
    }

    public void setWsdloperation(WSDLOperationImpl wsdloperation) {
        this.wsdloperation = wsdloperation;
    }

    public ArrayList getWsamappingList() {
        return wsamappingList;
    }

    public void setWsamappingList(ArrayList wsamappingList) {
        this.wsamappingList = wsamappingList;
    }

    public boolean isControlOperation() {
        return controlOperation;
    }

    public void setControlOperation(boolean controlOperation) {
        this.controlOperation = controlOperation;
    }


}