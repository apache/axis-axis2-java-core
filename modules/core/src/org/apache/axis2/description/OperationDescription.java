package org.apache.axis2.description;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.*;
import org.apache.axis2.engine.AxisError;
import org.apache.axis2.engine.MessageReceiver;
import org.apache.axis2.engine.Phase;
import org.apache.axis2.engine.SOAPProcessingModelChecker;
import org.apache.axis2.i18n.Messages;
import org.apache.axis2.phaseresolver.PhaseMetadata;
import org.apache.axis2.phaseresolver.PhaseResolver;
import org.apache.wsdl.WSDLConstants;
import org.apache.wsdl.WSDLOperation;
import org.apache.wsdl.impl.WSDLOperationImpl;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

/**
 * @author chathura@opensource.lk
 */
public class OperationDescription extends WSDLOperationImpl implements
        ParameterInclude, WSDLOperation, DescriptionConstants,
        WSDLConstants {

    private MessageReceiver messageReceiver;
    private ArrayList remainingPhasesInFlow;
    private ArrayList phasesOutFlow;
    private ArrayList phasesInFaultFlow;
    private ArrayList phasesOutFaultFlow;

    private HashMap moduleConfigmap;

    private int mep = MEP_CONSTANT_INVALID;

    private ServiceDescription parent;

    //To store deploytime module refs
    private ArrayList modulerefs;

    public OperationDescription() {
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
    }

    public OperationDescription(QName name) {
        this();
        this.setName(name);
    }

    /**
     * To ebgage a module it is reuired to use this method
     *
     * @param moduleref
     * @throws AxisFault
     */
    public void engageModule(ModuleDescription moduleref) throws AxisFault {
        if (moduleref == null) {
            return;
        }
        if (moduleref != null) {
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
        }
        new PhaseResolver().engageModuleToOperation(this, moduleref);
        Collection collectionModule = (Collection) this.getComponentProperty(
                MODULEREF_KEY);
        collectionModule.add(moduleref);
    }

    public void addToEngageModuleList(ModuleDescription moduleName) {
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
    * @see org.apache.axis2.description.ServiceDescription#getEngadgedModules()
    */

    /**
     * Method getEngadgedModules
     *
     * @return
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
        if(isParamterLocked(param.getName())){
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
     * @return
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
     * @return
     */
    public OperationContext findOperationContext(MessageContext msgContext, ServiceContext serviceContext) throws AxisFault {
        OperationContext operationContext = null;

        if (null == msgContext.getRelatesTo()) {
            //Its a new incomming message so get the factory to create a new
            // one
            operationContext =
                    OperationContextFactory.createOperationContext(
                            getAxisSpecifMEPConstant(),  this, serviceContext);

        } else {
            // So this message is part of an ongoing MEP
            //			operationContext =
            ConfigurationContext configContext = msgContext.getSystemContext();
            operationContext =
                    configContext.getOperationContext(
                            msgContext.getRelatesTo().getValue());

            if (null == operationContext) {
                throw new AxisFault(Messages.getMessage("cannotCorrealteMsg",
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

        if (null == msgContext.getRelatesTo()) {
//            //Its a new incomming message so get the factory to create a new
//            // one
//            operationContext =
//                    OperationContextFactory.createOperationContext(
//                            getAxisSpecifMEPConstant(), this);
            return null;

        } else {
            // So this message is part of an ongoing MEP
            //			operationContext =
            ConfigurationContext configContext = msgContext.getSystemContext();
            operationContext =
                    configContext.getOperationContext(
                            msgContext.getRelatesTo().getValue());

            if (null == operationContext) {
                throw new AxisFault(Messages.getMessage("cannotCorrealteMsg",
                        this.getName().toString(),msgContext.getRelatesTo().getValue()));
            }

        }

        registerOperationContext(msgContext, operationContext);

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
     * @return
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


    /**
     * @return
     */
    public ArrayList getPhasesInFaultFlow() {
        return phasesInFaultFlow;
    }

    /**
     * @return
     */
    public ArrayList getPhasesOutFaultFlow() {
        return phasesOutFaultFlow;
    }

    /**
     * @return
     */
    public ArrayList getPhasesOutFlow() {
        return phasesOutFlow;
    }

    /**
     * @return
     */
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

    public ServiceDescription getParent() {
        return parent;
    }

    public void setParent(ServiceDescription parent) {
        this.parent = parent;
    }

    //to check whether a given paramter is locked
    public boolean isParamterLocked(String paramterName) {
        // checking the locked value of parent
        boolean loscked =  false;
        if (getParent() !=null) {
            loscked=    getParent().isParamterLocked(paramterName);
        }
        if(loscked){
            return true;
        } else {
            Parameter parameter = getParameter(paramterName);
            if(parameter != null && parameter.isLocked()){
                return true;
            } else {
                return false;
            }
        }
    }

    /**
     * Adding module configuration , if there is moduleConfig tag in operation
     * @param moduleConfiguration
     */
    public void addModuleConfig(ModuleConfiguration moduleConfiguration){
        if(moduleConfigmap == null){
            moduleConfigmap = new HashMap();
        }
        moduleConfigmap.put(moduleConfiguration.getModuleName(),moduleConfiguration);
    }

    public ModuleConfiguration getModuleConfig(QName moduleName){
        return  (ModuleConfiguration)moduleConfigmap.get(moduleName);
    }

}

