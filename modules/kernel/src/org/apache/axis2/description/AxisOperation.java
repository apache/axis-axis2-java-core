/*
* Copyright 2004,2006 The Apache Software Foundation.
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

package org.apache.axis2.description;

import org.apache.axiom.om.util.UUIDGenerator;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.deployment.DeploymentConstants;
import org.apache.axis2.util.WSDLSerializationUtil;
import org.apache.axis2.util.JavaUtils;
import org.apache.axis2.client.OperationClient;
import org.apache.axis2.client.Options;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.OperationContext;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.engine.AxisError;
import org.apache.axis2.engine.MessageReceiver;
import org.apache.axis2.i18n.Messages;
import org.apache.axis2.modules.Module;
import org.apache.axis2.phaseresolver.PhaseResolver;
import org.apache.axis2.wsdl.WSDLConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.Map;
import java.net.URI;

public abstract class AxisOperation extends AxisDescription
        implements WSDLConstants {

    public static final String STYLE_RPC = "rpc";
    public static final String STYLE_MSG = "msg";
    public static final String STYLE_DOC = "doc";

    private static final Log log = LogFactory.getLog(AxisOperation.class);
    /**
     * message exchange pattern
     */
    private int mep = WSDLConstants.MEP_CONSTANT_INVALID;

    /**
     * list of engaged modules
     */
    private ArrayList engagedModules = new ArrayList();

    // to hide control operation , operation which added by RM like module
    private boolean controlOperation = false;
    private String style = STYLE_DOC;

    // to store mepURL
    private String mepURI;

    private MessageReceiver messageReceiver;

    private HashMap moduleConfigmap;

    // To store deploy-time module refs
    private ArrayList modulerefs;

    private ArrayList faultMessages;

    private QName name;

    private ArrayList wsamappingList;
    private String outputAction;
    private HashMap faultActions = new HashMap();

    private String soapAction;


    /**
     * constructor
     */
    public AxisOperation() {
        mepURI = WSDL2Constants.MEP_URI_IN_OUT;
        modulerefs = new ArrayList();
        moduleConfigmap = new HashMap();
        faultMessages = new ArrayList();
        //setup a temporary name
        QName tmpName = new QName(this.getClass().getName() + "_" + UUIDGenerator.getUUID());
        this.setName(tmpName);
    }

    public AxisOperation(QName name) {
        this();
        this.setName(name);
    }

    public abstract void addMessage(AxisMessage message, String label);

    /**
     * Adds a message context into an operation context. Depending on MEPs, this
     * method has to be overridden.
     * Depending on the MEP operation description know how to fill the message context map
     * in operationContext.
     * As an example, if the MEP is IN-OUT then depending on messagable operation description
     * should know how to keep them in correct locations.
     *
     * @param msgContext <code>MessageContext</code>
     * @param opContext  <code>OperationContext</code>
     * @throws AxisFault <code>AxisFault</code>
     */
    public abstract void addMessageContext(MessageContext msgContext, OperationContext opContext)
            throws AxisFault;

    public abstract void addFaultMessageContext(MessageContext msgContext,
                                                OperationContext opContext)
            throws AxisFault;

    public void addModule(String moduleName) {
        modulerefs.add(moduleName);
    }

    /**
     * Adds module configuration, if there is moduleConfig tag in operation.
     *
     * @param moduleConfiguration
     */
    public void addModuleConfig(ModuleConfiguration moduleConfiguration) {
        moduleConfigmap.put(moduleConfiguration.getModuleName(), moduleConfiguration);
    }

    /**
     * Engages a module. It is required to use this method.
     *
     * @param moduleref
     * @throws AxisFault
     */
    public final void engageModule(AxisModule moduleref)
            throws AxisFault {
        ArrayList moduleOperations = engageModuleToOperation(moduleref);
        AxisService service = (AxisService) getParent();
        if (service != null) {
            for (int i = 0; i < moduleOperations.size(); i++) {
                AxisOperation axisOperation = (AxisOperation) moduleOperations.get(i);
                service.addOperation(axisOperation);
            }
        }
    }

    private ArrayList engageModuleToOperation(AxisModule moduleref)
            throws AxisFault {
        if (moduleref == null) {
            return null;
        }
        Iterator module_itr = engagedModules.iterator();
        boolean isEngagable;
        String moduleName = moduleref.getName();
        while (module_itr.hasNext()) {
            AxisModule module = (AxisModule) module_itr.next();
            String modu = module.getName();
            isEngagable = org.apache.axis2.util.Utils.checkVersion(moduleName, modu);
            if (!isEngagable) {
                return new ArrayList();
            }
        }
        AxisConfiguration axisConfig = getAxisConfiguration();
        PhaseResolver phaseResolver = new PhaseResolver(axisConfig);
        phaseResolver.engageModuleToOperation(this, moduleref);
        Module module = moduleref.getModule();
        if (module != null) {
            module.engageNotify(this);
        }
        engagedModules.add(moduleref);
        return addModuleOperations(moduleref, axisConfig, (AxisService) getParent());
    }

    public void disengageModule(AxisModule module) {
        if (module != null) {
            if (getParent() != null) {
                AxisService service = (AxisService) getParent();
                AxisConfiguration axiConfiguration = service.getAxisConfiguration();
                PhaseResolver phaseResolver = new PhaseResolver(axiConfiguration);
                if (service.isEngaged(module.getName())) {
                    phaseResolver.disengageModuleFromOperationChain(module, this);
                } else if (axiConfiguration != null &&
                        axiConfiguration.isEngaged(module.getName())) {
                    phaseResolver.disengageModuleFromOperationChain(module, this);
                } else {
                    if (axiConfiguration != null) {
                        phaseResolver.disengageModuleFromGlobalChains(module);
                    }
                    phaseResolver.disengageModuleFromOperationChain(module, this);
                    //removing operations added at the time of module engagemnt
                    HashMap moduleOperations = module.getOperations();
                    if (moduleOperations != null) {
                        Iterator moduleOperations_itr = moduleOperations.values().iterator();
                        while (moduleOperations_itr.hasNext()) {
                            AxisOperation operation = (AxisOperation) moduleOperations_itr.next();
                            service.removeOperation(operation.getName());
                        }
                    }
                }
            }
            engagedModules.remove(module);
            log.debug("removed module from engaged modules list " + module.getName());
        }
    }

    /**
     * To remove module from engage  module list
     *
     * @param module
     */
    public void removeFromEngagedModuleList(AxisModule module) {
        engagedModules.remove(module);
        log.debug("removed module from engaged modules list " + module.getName());
    }


    /**
     * Adds an operation to a service if a module is required to do so.
     *
     * @param module
     */
    public ArrayList addModuleOperations(AxisModule module, AxisConfiguration axisConfig,
                                         AxisService service)
            throws AxisFault {
        HashMap map = module.getOperations();
        Collection col = map.values();
        PhaseResolver phaseResolver = new PhaseResolver(axisConfig);
        //this array list is return , to avoid concurrent modifications , in the deployment engine
        ArrayList ops = new ArrayList();
        for (Iterator iterator = col.iterator(); iterator.hasNext();) {
            AxisOperation axisOperation = copyOperation((AxisOperation) iterator.next());
            axisOperation.setParent(service);
            ArrayList wsamappings = axisOperation.getWsamappingList();
            if (service.getOperation(axisOperation.getName()) == null) {
                // this operation is a control operation.
                Parameter expose = axisOperation.getParameter(DeploymentConstants.TAG_EXPOSE);
                if(expose!=null){
                    if(JavaUtils.isTrue(expose.getValue(), false)){
                        axisOperation.setControlOperation(true);
                    }
                } else {
                    axisOperation.setControlOperation(true);
                }
                Module moduleclazz = module.getModule();
                if (moduleclazz != null) {
                    moduleclazz.engageNotify(axisOperation);
                }
                phaseResolver.engageModuleToOperation(axisOperation, module);
                ops.add(axisOperation);
                if (wsamappings != null) {
                    for (int j = 0; j < wsamappings.size(); j++) {
                        String mapping = (String) wsamappings.get(j);

                        service.mapActionToOperation(mapping, axisOperation);
                    }
                }
            }
        }
        return ops;
    }

    /**
     * Gets a copy from module operation.
     *
     * @param axisOperation
     * @return Returns AxisOperation.
     * @throws AxisFault
     */
    private AxisOperation copyOperation(AxisOperation axisOperation) throws AxisFault {
        AxisOperation operation =
                AxisOperationFactory
                        .getOperationDescription(axisOperation.getMessageExchangePattern());

        operation.setMessageReceiver(axisOperation.getMessageReceiver());
        operation.setName(axisOperation.getName());

        Iterator parameters = axisOperation.getParameters().iterator();

        while (parameters.hasNext()) {
            Parameter parameter = (Parameter) parameters.next();

            operation.addParameter(parameter);
        }

        operation.setWsamappingList(axisOperation.getWsamappingList());
        operation.setOutputAction(axisOperation.getOutputAction());
        String[] faultActionNames = axisOperation.getFaultActionNames();
        for (int i = 0; i < faultActionNames.length; i++) {
            operation.addFaultAction(faultActionNames[i],
                                     axisOperation.getFaultAction(faultActionNames[i]));
        }
        operation.setRemainingPhasesInFlow(axisOperation.getRemainingPhasesInFlow());
        operation.setPhasesInFaultFlow(axisOperation.getPhasesInFaultFlow());
        operation.setPhasesOutFaultFlow(axisOperation.getPhasesOutFaultFlow());
        operation.setPhasesOutFlow(axisOperation.getPhasesOutFlow());

        return operation;
    }


    /**
     * Returns as existing OperationContext related to this message if one exists.
     *
     * @param msgContext
     * @return Returns OperationContext.
     * @throws AxisFault
     */
    public OperationContext findForExistingOperationContext(MessageContext msgContext)
            throws AxisFault {
        OperationContext operationContext;

        if ((operationContext = msgContext.getOperationContext()) != null) {
            return operationContext;
        }

        // If this message is not related to another one, or it is but not one emitted
        // from the same operation, don't further look for an operation context or fault.
        if (null != msgContext.getRelatesTo()) {
            // So this message may be part of an ongoing MEP
            ConfigurationContext configContext = msgContext.getConfigurationContext();

            operationContext =
                    configContext.getOperationContext(msgContext.getRelatesTo().getValue());

            if (null == operationContext && log.isDebugEnabled()) {
                log.debug(msgContext.getLogIDString() +
                        " Cannot correlate inbound message RelatesTo value [" +
                        msgContext.getRelatesTo() + "] to in-progree MEP");
            }
        }

        return operationContext;
    }

    /**
     * Finds a MEPContext for an incoming message. An incoming message can be
     * of two states.
     * <p/>
     * 1)This is a new incoming message of a given MEP. 2)This message is a
     * part of an MEP which has already begun.
     * <p/>
     * The method is special cased for the two MEPs
     * <p/>
     * #IN_ONLY #IN_OUT
     * <p/>
     * for two reasons. First reason is the wide usage and the second being that
     * the need for the MEPContext to be saved for further incoming messages.
     * <p/>
     * In the event that MEP of this operation is different from the two MEPs
     * defaulted above the decision of creating a new or this message relates
     * to a MEP which already in business is decided by looking at the WSA
     * Relates TO of the incoming message.
     *
     * @param msgContext
     */
    public OperationContext findOperationContext(MessageContext msgContext,
                                                 ServiceContext serviceContext)
            throws AxisFault {
        OperationContext operationContext;

        if (null == msgContext.getRelatesTo()) {

            // Its a new incoming message so get the factory to create a new
            // one
            operationContext = serviceContext.createOperationContext(this);
        } else {

            // So this message is part of an ongoing MEP
            ConfigurationContext configContext = msgContext.getConfigurationContext();

            operationContext =
                    configContext.getOperationContext(msgContext.getRelatesTo().getValue());

            if (null == operationContext) {
                throw new AxisFault(Messages.getMessage("cannotCorrelateMsg",
                                                        this.name.toString(),
                                                        msgContext.getRelatesTo().getValue()));
            }
        }
        return operationContext;
    }

    public void registerOperationContext(MessageContext msgContext,
                                         OperationContext operationContext)
            throws AxisFault {
        msgContext.setAxisOperation(this);
        msgContext.getConfigurationContext().registerOperationContext(msgContext.getMessageID(),
                                                                      operationContext);
        operationContext.addMessageContext(msgContext);
        msgContext.setOperationContext(operationContext);
        if (operationContext.isComplete()) {
            operationContext.cleanup();
        }
    }

    public void registerMessageContext(MessageContext msgContext,
                                       OperationContext operationContext) throws AxisFault {
        msgContext.setAxisOperation(this);
        operationContext.addMessageContext(msgContext);
        msgContext.setOperationContext(operationContext);
        if (operationContext.isComplete()) {
            operationContext.cleanup();
        }
    }

    /**
     * Maps the String URI of the Message exchange pattern to a integer.
     * Further, in the first lookup, it will cache the looked
     * up value so that the subsequent method calls are extremely efficient.
     */
    public int getAxisSpecificMEPConstant() {
        if (this.mep != WSDLConstants.MEP_CONSTANT_INVALID) {
            return this.mep;
        }

        int temp = WSDLConstants.MEP_CONSTANT_INVALID;

        if (WSDL2Constants.MEP_URI_IN_OUT.equals(mepURI)) {
            temp = WSDLConstants.MEP_CONSTANT_IN_OUT;
        } else if (WSDL2Constants.MEP_URI_IN_ONLY.equals(mepURI)) {
            temp = WSDLConstants.MEP_CONSTANT_IN_ONLY;
        } else if (WSDL2Constants.MEP_URI_IN_OPTIONAL_OUT.equals(mepURI)) {
            temp = WSDLConstants.MEP_CONSTANT_IN_OPTIONAL_OUT;
        } else if (WSDL2Constants.MEP_URI_OUT_IN.equals(mepURI)) {
            temp = WSDLConstants.MEP_CONSTANT_OUT_IN;
        } else if (WSDL2Constants.MEP_URI_OUT_ONLY.equals(mepURI)) {
            temp = WSDLConstants.MEP_CONSTANT_OUT_ONLY;
        } else if (WSDL2Constants.MEP_URI_OUT_OPTIONAL_IN.equals(mepURI)) {
            temp = WSDLConstants.MEP_CONSTANT_OUT_OPTIONAL_IN;
        } else if (WSDL2Constants.MEP_URI_ROBUST_IN_ONLY.equals(mepURI)) {
            temp = WSDLConstants.MEP_CONSTANT_ROBUST_IN_ONLY;
        } else if (WSDL2Constants.MEP_URI_ROBUST_OUT_ONLY.equals(mepURI)) {
            temp = WSDLConstants.MEP_CONSTANT_ROBUST_OUT_ONLY;
        }

        if (temp == WSDLConstants.MEP_CONSTANT_INVALID) {
            throw new AxisError(Messages.getMessage("mepmappingerror"));
        }

        this.mep = temp;

        return this.mep;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.apache.axis2.description.AxisService#getEngadgedModules()
     */

    /**
     * Method getEngagedModules.
     */
    public Collection getEngagedModules() {
        return engagedModules;
    }

    public abstract AxisMessage getMessage(String label);

    public String getMessageExchangePattern() {
        return mepURI;
    }

    public MessageReceiver getMessageReceiver() {
        return messageReceiver;
    }

    public ModuleConfiguration getModuleConfig(String moduleName) {
        return (ModuleConfiguration) moduleConfigmap.get(moduleName);
    }

    public ArrayList getModuleRefs() {
        return modulerefs;
    }

    public QName getName() {
        return name;
    }

    public abstract ArrayList getPhasesInFaultFlow();

    public abstract ArrayList getPhasesOutFaultFlow();

    public abstract ArrayList getPhasesOutFlow();

    public abstract ArrayList getRemainingPhasesInFlow();

    public String getStyle() {
        return style;
    }

    public ArrayList getWsamappingList() {
        return wsamappingList;
    }

    public boolean isControlOperation() {
        return controlOperation;
    }

    // to check whether a given parameter is locked
    public boolean isParameterLocked(String parameterName) {

        // checking the locked value of parent
        boolean locked = false;

        if (getParent() != null) {
            locked = getParent().isParameterLocked(parameterName);
        }

        if (locked) {
            return true;
        } else {
            Parameter parameter = getParameter(parameterName);

            return (parameter != null) && parameter.isLocked();
        }
    }

    public void setControlOperation(boolean controlOperation) {
        this.controlOperation = controlOperation;
    }

    public void setMessageExchangePattern(String mepURI) {
        this.mepURI = mepURI;
    }

    public void setMessageReceiver(MessageReceiver messageReceiver) {
        this.messageReceiver = messageReceiver;
    }

    public void setName(QName name) {
        this.name = name;
    }

    public abstract void setPhasesInFaultFlow(ArrayList list);

    public abstract void setPhasesOutFaultFlow(ArrayList list);

    public abstract void setPhasesOutFlow(ArrayList list);

    public abstract void setRemainingPhasesInFlow(ArrayList list);

    public void setStyle(String style) {
        if (!"".equals(style)) {
            this.style = style;
        }
    }

    public void setWsamappingList(ArrayList wsamappingList) {
        this.wsamappingList = wsamappingList;
    }

    /**
     * 
     */
    public OperationClient createClient(ServiceContext sc, Options options) {
        throw new UnsupportedOperationException(
                Messages.getMessage("mepnotyetimplemented", mepURI));
    }

    public Object getKey() {
        return this.name;
    }

    public ArrayList getFaultMessages() {
        return faultMessages;
    }

    public void setFaultMessages(AxisMessage faultMessage) {
        faultMessages.add(faultMessage);
    }

    public void setSoapAction(String soapAction) {
        this.soapAction = soapAction;
    }

    public String getInputAction() {
        return soapAction;
    }

    public String getOutputAction() {
        return outputAction;
    }

    public void setOutputAction(String act) {
        outputAction = act;
    }

    public void addFaultAction(String faultName, String action) {
        faultActions.put(faultName, action);
    }

    public void removeFaultAction(String faultName) {
        faultActions.remove(faultName);
    }

    public String getFaultAction(String faultName) {
        return (String) faultActions.get(faultName);
    }

    public String[] getFaultActionNames() {
        Set keys = faultActions.keySet();
        String[] faultActionNames = new String[keys.size()];
        faultActionNames = (String[]) keys.toArray(faultActionNames);
        return faultActionNames;
    }

    public String getFaultAction() {
        String result = null;
        Iterator iter = faultActions.values().iterator();
        if (iter.hasNext()) {
            result = (String) iter.next();
        }
        return result;
    }

    public boolean isEngaged(String moduleName) {
        Iterator engagedModuleItr = engagedModules.iterator();
        while (engagedModuleItr.hasNext()) {
            AxisModule axisModule = (AxisModule) engagedModuleItr.next();
            if (axisModule.getName().equals(moduleName)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * All childerns of a AxisOperation must be Messages. So we just return it. 
     * @return
     */
    
    public Iterator getMessages(){
        return getChildren();
    }
    
 }
