package org.apache.axis2.description;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.OperationContext;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.engine.AxisError;
import org.apache.axis2.engine.MessageReceiver;
import org.apache.axis2.engine.Phase;
import org.apache.axis2.engine.SOAPProcessingModelChecker;
import org.apache.axis2.i18n.Messages;
import org.apache.axis2.phaseresolver.PhaseMetadata;
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

public abstract class AxisOperation  implements
        ParameterInclude, WSDLOperation, DescriptionConstants,
        WSDLConstants {

    private MessageReceiver messageReceiver;
    private ArrayList remainingPhasesInFlow;
    private ArrayList phasesOutFlow;
    private ArrayList phasesInFaultFlow;
    private ArrayList phasesOutFaultFlow;

    private HashMap moduleConfigmap;

    private int mep = MEP_CONSTANT_INVALID;

    private WSDLOperationImpl wsdlopeartion;

    private AxisService parent;
    private ArrayList wsamappingList;

    //To store deploytime module refs
    private ArrayList modulerefs;



    public AxisOperation(WSDLOperation wsdlopeartion) {
        this.wsdlopeartion = (WSDLOperationImpl)wsdlopeartion;
        this.setMessageExchangePattern(MEP_URI_IN_OUT);
        this.setComponentProperty(PARAMETER_KEY, new ParameterIncludeImpl());
        this.setComponentProperty(MODULEREF_KEY, new ArrayList());

        remainingPhasesInFlow = new ArrayList();
        remainingPhasesInFlow.add(
                new Phase(PhaseMetadata.PHASE_POLICY_DETERMINATION));
        Phase messageProcessing = new Phase(PhaseMetadata.PHASE_MESSAGE_PROCESSING);
        messageProcessing.addHandler(new SOAPProcessingModelChecker());
        remainingPhasesInFlow.add(messageProcessing);

        phasesOutFlow = new ArrayList();
        phasesOutFlow.add(new Phase(PhaseMetadata.PHASE_POLICY_DETERMINATION));
        phasesOutFlow.add(new Phase(PhaseMetadata.PHASE_MESSAGE_OUT));

        phasesInFaultFlow = new ArrayList();
        phasesOutFaultFlow = new ArrayList();
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
     * @throws org.apache.axis2.AxisFault
     */
    public final void engageModule(ModuleDescription moduleref) throws AxisFault {
        if (moduleref == null) {
            return;
        }
        Collection collectionModule = (Collection) this.getComponentProperty(
                MODULEREF_KEY);
        for (Iterator iterator = collectionModule.iterator();
             iterator.hasNext();) {
            ModuleDescription modu = (ModuleDescription) iterator.next();
            if (modu.getName().equals(moduleref.getName())) {
                throw new AxisFault(moduleref.getName().getLocalPart() +
                        " module has alredy engaged to the operation" +
                        "  operation terminated !!!");
            }

        }
        //    new PhaseResolver().engageModuleToOperation(this, moduleref);
        collectionModule.add(moduleref);
    }

    public final void addToEngageModuleList(ModuleDescription moduleName) {
        Collection collectionModule = (Collection) this.getComponentProperty(
                MODULEREF_KEY);
        for (Iterator iterator = collectionModule.iterator();
             iterator.hasNext();) {
            ModuleDescription moduleDescription = (ModuleDescription) iterator.next();
            if (moduleName.getName().equals(moduleDescription.getName())) {
                return;
            }
        }
        collectionModule.add(moduleName);
    }




    /*
    * (non-Javadoc)
    *
    * @see org.apache.axis2.description.AxisService#getEngadgedModules()
    */

    /**
     * Method getEngadgedModules
     *
     */
    public Collection getModules() {
        return (Collection) this.getComponentProperty(MODULEREF_KEY);
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
        if(isParameterLocked(param.getName())){
            throw new AxisFault("Parmter is locked can not overide: " + param.getName());
        } else{
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
        return  paramInclude.getParameters();
    }
    public MessageReceiver getMessageReceiver() {
        return messageReceiver;
    }

    public void setMessageReceiver(MessageReceiver messageReceiver) {
        this.messageReceiver = messageReceiver;
    }

    /**
     * This method will simply map the String URI of the Message exchange
     * pattern to a integer. Further in the first lookup it will cash the looked
     * up value so that the subsequent method calls will be extremely efficient.
     *
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


    public ArrayList getPhasesInFaultFlow() {
        return phasesInFaultFlow;
    }


    public ArrayList getPhasesOutFaultFlow() {
        return phasesOutFaultFlow;
    }


    public ArrayList getPhasesOutFlow() {
        return phasesOutFlow;
    }


    public ArrayList getRemainingPhasesInFlow() {
        return remainingPhasesInFlow;
    }

    /**
     * @param list
     */
    public void setPhasesInFaultFlow(ArrayList list) {
        phasesInFaultFlow = list;
    }

    /**
     * @param list
     */
    public void setPhasesOutFaultFlow(ArrayList list) {
        phasesOutFaultFlow = list;
    }

    /**
     * @param list
     */
    public void setPhasesOutFlow(ArrayList list) {
        phasesOutFlow = list;
    }

    /**
     * @param list
     */
    public void setRemainingPhasesInFlow(ArrayList list) {
        remainingPhasesInFlow = list;
    }

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

    //to check whether a given paramter is locked
    public boolean isParameterLocked(String paramterName) {
        // checking the locked value of parent
        boolean loscked =  false;
        if (getParent() !=null) {
            loscked=    getParent().isParameterLocked(paramterName);
        }
        if(loscked){
            return true;
        } else {
            Parameter parameter = getParameter(paramterName);
            return parameter != null && parameter.isLocked();
        }
    }

    /**
     * Adding module configuration , if there is moduleConfig tag in operation
     * @param moduleConfiguration
     */
    public void addModuleConfig(ModuleConfiguration moduleConfiguration){
        moduleConfigmap.put(moduleConfiguration.getModuleName(),moduleConfiguration);
    }

    public ModuleConfiguration getModuleConfig(QName moduleName){
        return  (ModuleConfiguration)moduleConfigmap.get(moduleName);
    }


    /**
     * To add a message Context into a operation context depending on MEPs this method has to
     * be overided.
     * Depending on the mep operation description know how to fill the message conetxt map
     * in operationContext.
     * As an exmple if the MEP is IN-OUT then depending on messagelbl operation description
     * should know how to keep them in corret locations 
     * @param msgContext <code>MessageContext</code>
     * @param opContext  <code>OperationContext</code>
     * @throws AxisFault <code>AxisFault</code>
     */
    public abstract void addMessageContext(MessageContext msgContext, OperationContext opContext)
            throws AxisFault ;

    public List getInfaults() {
        return wsdlopeartion.getInfaults();
    }

    public void setInfaults(List infaults) {
        wsdlopeartion.setInfaults(infaults);
    }

    public MessageReference getInputMessage() {
        return wsdlopeartion.getInputMessage();
    }

    public void setInputMessage(MessageReference inputMessage) {
        wsdlopeartion.setInputMessage(inputMessage);
    }

    public String getMessageExchangePattern() {
        return wsdlopeartion.getMessageExchangePattern();
    }

    public void setMessageExchangePattern(String messageExchangePattern) {
        wsdlopeartion.setMessageExchangePattern(messageExchangePattern);
    }

    public QName getName() {
        return wsdlopeartion.getName();
    }

    public void setName(QName name) {
        wsdlopeartion.setName(name);
    }

    public List getOutfaults() {
        return wsdlopeartion.getOutfaults();
    }

    public void setOutfaults(List outfaults) {
        wsdlopeartion.setOutfaults(outfaults);
    }

    public MessageReference getOutputMessage() {
        return wsdlopeartion.getOutputMessage();
    }

    public void setOutputMessage(MessageReference outputMessage) {
        wsdlopeartion.setOutputMessage(outputMessage);
    }

    public boolean isSafe() {
        return wsdlopeartion.isSafe();
    }

    public void setSafety(boolean safe) {
        wsdlopeartion.setSafety(safe);
    }

    public String getStyle() {
        return wsdlopeartion.getStyle();
    }

    public void setStyle(String style) {
        wsdlopeartion.setStyle(style);
    }

    public String getTargetnamespace() {
        return wsdlopeartion.getTargetnamespace();
    }

    public void addInFault(WSDLFaultReference inFault) {
        wsdlopeartion.addInFault(inFault);
    }

    public void addOutFault(WSDLFaultReference outFault) {
        wsdlopeartion.addOutFault(outFault);
    }

    public void addFeature(WSDLFeature feature) {
        wsdlopeartion.addFeature(feature);
    }

    public List getFeatures() {
        return wsdlopeartion.getFeatures();
    }

    public void addProperty(WSDLProperty wsdlProperty) {
        wsdlopeartion.addProperty(wsdlProperty);
    }

    public List getProperties() {
        return wsdlopeartion.getProperties();
    }

    public Document getDocumentation() {
        return wsdlopeartion.getDocumentation();
    }

    public void setDocumentation(Document documentation) {
        wsdlopeartion.setDocumentation(documentation);
    }

    public HashMap getComponentProperties() {
        return wsdlopeartion.getComponentProperties();
    }

    public void setComponentProperties(HashMap properties) {
        wsdlopeartion.setComponentProperties(properties);
    }

    public void setComponentProperty(Object key, Object obj) {
        wsdlopeartion.setComponentProperty(key, obj);
    }

    public Object getComponentProperty(Object key) {
        return wsdlopeartion.getComponentProperty(key);
    }

    public void addExtensibilityElement(WSDLExtensibilityElement element) {
        wsdlopeartion.addExtensibilityElement(element);
    }

    public List getExtensibilityElements() {
        return wsdlopeartion.getExtensibilityElements();
    }

    public void addExtensibleAttributes(WSDLExtensibilityAttribute attribute) {
        wsdlopeartion.addExtensibleAttributes(attribute);
    }

    public List getExtensibilityAttributes() {
        return wsdlopeartion.getExtensibilityAttributes();
    }

    public Map getMetadataBag() {
        return wsdlopeartion.getMetadataBag();
    }

    public void setMetadataBag(Map meMap){
         wsdlopeartion.setMetadataBag(meMap);
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
    public OperationContext findOperationContext(MessageContext msgContext, ServiceContext serviceContext) throws AxisFault {
        OperationContext operationContext ;

        if (null == msgContext.getRelatesTo()) {
            //Its a new incomming message so get the factory to create a new
            // one
           operationContext =  new OperationContext(this,serviceContext);
        } else {
            // So this message is part of an ongoing MEP
            //			operationContext =
            ConfigurationContext configContext = msgContext.getSystemContext();
            operationContext =
                    configContext.getOperationContext( msgContext.getRelatesTo().getValue());

            if (null == operationContext) {
                throw new AxisFault(Messages.getMessage("cannotCorrelateMsg",
                        this.getName().toString(),msgContext.getRelatesTo().getValue()));
            }

        }

        registerOperationContext(msgContext, operationContext);

        return operationContext;

    }

    /**
     * This will not create a new operation context if there is no one already.
     * @param msgContext
     * @return
     * @throws AxisFault
     */
    public OperationContext findForExistingOperationContext(MessageContext msgContext) throws AxisFault {
        OperationContext operationContext = null;

        if((operationContext = msgContext.getOperationContext()) != null) {
        	return operationContext;
        }

        if (null == msgContext.getRelatesTo()) {
            return null;
        } else {
            // So this message is part of an ongoing MEP
            //			operationContext =
            ConfigurationContext configContext = msgContext.getSystemContext();
            operationContext = configContext.getOperationContext(msgContext.getRelatesTo().getValue());

            if (null == operationContext) {
                throw new AxisFault(Messages.getMessage("cannotCorrealteMsg",
                        this.getName().toString(),msgContext.getRelatesTo().getValue()));
            }

        }


        return operationContext;

    }

    public void registerOperationContext(MessageContext msgContext, OperationContext operationContext) throws AxisFault {
        msgContext.getSystemContext().registerOperationContext(
                msgContext.getMessageID(), operationContext);
        operationContext.addMessageContext(msgContext);
        msgContext.setOperationContext(operationContext);
        if (operationContext.isComplete()) {
            operationContext.cleanup();
        }
    }

    public void setWsdlopeartion(WSDLOperationImpl wsdlopeartion) {
        this.wsdlopeartion = wsdlopeartion;
    }

    public ArrayList getWsamappingList() {
        return wsamappingList;
    }

    public void setWsamappingList(ArrayList wsamappingList) {
        this.wsamappingList = wsamappingList;
    }
}