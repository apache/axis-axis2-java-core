package org.apache.axis.description;

import org.apache.axis.context.*;
import org.apache.axis.engine.AxisError;
import org.apache.axis.engine.AxisFault;
import org.apache.axis.engine.MessageReceiver;
import org.apache.axis.engine.Phase;
import org.apache.axis.phaseresolver.PhaseMetadata;
import org.apache.axis.phaseresolver.PhaseResolver;
import org.apache.wsdl.WSDLConstants;
import org.apache.wsdl.WSDLOperation;
import org.apache.wsdl.impl.WSDLOperationImpl;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Collection;
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

    private int mep = MEP_CONSTANT_INVALID;

    //To store deploytime module refs
    private ArrayList modulerefs;

    public OperationDescription() {
        this.setMessageExchangePattern(MEP_URI_IN_OUT);
        this.setComponentProperty(PARAMETER_KEY, new ParameterIncludeImpl());
        this.setComponentProperty(MODULEREF_KEY, new ArrayList());

        remainingPhasesInFlow = new ArrayList();
        remainingPhasesInFlow.add(new Phase(PhaseMetadata.PHASE_POLICY_DETERMINATION));

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
            Collection collectionModule = (Collection) this.getComponentProperty(MODULEREF_KEY);
            for (Iterator iterator = collectionModule.iterator(); iterator.hasNext();) {
                ModuleDescription   modu = (ModuleDescription) iterator.next();
                if(modu.getName().equals(moduleref.getName())){
                    throw new AxisFault(moduleref.getName().getLocalPart()+ " module has alredy engaged to the operation" +
                            "  operation terminated !!!");
                }

            }
        }
        new PhaseResolver().engageModuleToOperation(this, moduleref);
        Collection collectionModule = (Collection) this.getComponentProperty(MODULEREF_KEY);
        collectionModule.add(moduleref);
    }

    public void addToEngageModuleList(ModuleDescription moduleName){
        Collection collectionModule = (Collection) this.getComponentProperty(MODULEREF_KEY);
        collectionModule.add(moduleName);
    }




    /*
    * (non-Javadoc)
    *
    * @see org.apache.axis.description.ServiceDescription#getEngadgedModules()
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
    public void addParameter(Parameter param) {
        if (param == null) {
            return;
        }
        ParameterIncludeImpl paramInclude = (ParameterIncludeImpl) this
                .getComponentProperty(PARAMETER_KEY);
        paramInclude.addParameter(param);
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
        return (Parameter) paramInclude.getParameter(name);
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
    public OperationContext findOperationContext(MessageContext msgContext,
                                                 ServiceContext serviceContext, boolean serverside) throws AxisFault {
        OperationContext operationContext = null;

        if (null == msgContext.getRelatesTo()) {
            //Its a new incomming message so get the factory to create a new
            // one
            operationContext = OperationContextFactory.createMEPContext(getAxisSpecifMEPConstant(), serverside, this,
                    serviceContext);

        } else {
            // So this message is part of an ongoing MEP
            //			operationContext =
            ConfigurationContext configContext = msgContext.getSystemContext();
            operationContext = configContext.getOperationContext(msgContext.getRelatesTo().getValue());

            if (null == operationContext) {
                throw new AxisFault("Cannot relate the message in the operation :"
                        + this.getName()
                        + " :Unrelated RelatesTO value "
                        + msgContext.getRelatesTo().getValue());
            }

        }

        msgContext.getSystemContext().registerOperationContext(msgContext.getMessageID(), operationContext);
        operationContext.addMessageContext(msgContext);
        msgContext.setOperationContext(operationContext);
        if (operationContext.isComplete()) {
            operationContext.cleanup();
        }

        return operationContext;

    }

    public MessageReceiver getMessageReciever() {
        return messageReceiver;
    }

    public void setMessageReciever(MessageReceiver messageReceiver) {
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
            throw new AxisError("Could not Map the MEP URI to a axis MEP constant value");
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


}

