package org.apache.axis2.description;

import org.apache.axis2.AxisFault;
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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wsdl.WSDLConstants;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

public abstract class AxisOperation extends AxisDescription
        implements WSDLConstants {
    public static final String STYLE_RPC = "rpc";
    public static final String STYLE_MSG = "msg";
    public static final String STYLE_DOC = "doc";
    private Log log = LogFactory.getLog(getClass());
    private int mep = MEP_CONSTANT_INVALID;

    public static final String SOAP_ACTION = "soapaction";

    // to store engaged modules
    private ArrayList engagedModules = new ArrayList();

    // to hide control operation , operation which added by RM like module
    private boolean controlOperation = false;
    private String style = STYLE_DOC;

    // to store mepURL
    private String mepURI;
    private MessageReceiver messageReceiver;
    private HashMap moduleConfigmap;

    // To store deploytime module refs
    private ArrayList modulerefs;

    private QName name;

    private ArrayList wsamappingList;

    public AxisOperation() {
        mepURI = MEP_URI_IN_OUT;
        modulerefs = new ArrayList();
        moduleConfigmap = new HashMap();
    }

    public AxisOperation(QName name) {
        this();
        this.setName(name);
    }

    public abstract void addMessage(AxisMessage message, String label);

    /**
     * Adds a message context into an operation context. Depending on MEPs, this
     * method has to be overridden.
     * Depending on the mep operation description know how to fill the message context map
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

    public abstract void addFaultMessageContext(MessageContext msgContext, OperationContext opContext)
            throws AxisFault;

    public void addModule(QName moduleName) {
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
    public final ArrayList engageModule(AxisModule moduleref, AxisConfiguration axisConfig)
            throws AxisFault {
        if (moduleref == null) {
            return null;
        }

        boolean needToadd = true;
        Iterator module_itr = engagedModules.iterator();

        while (module_itr.hasNext()) {
            AxisModule module = (AxisModule) module_itr.next();

            if (module.getName().equals(moduleref.getName())) {
                log.debug(Messages.getMessage("modulealredyengaged",
                        moduleref.getName().getLocalPart()));
                needToadd = false;
            }
        }
        PhaseResolver phaseResolver = new PhaseResolver(axisConfig);
        phaseResolver.engageModuleToOperation(this, moduleref);
        Module module = moduleref.getModule();
        if (module != null) {
            module.engageNotify(this);
        }

        if (needToadd) {
            engagedModules.add(moduleref);
        }
        return addModuleOperations(moduleref, axisConfig, (AxisService) getParent());
    }

    public void disEngageModule(AxisModule module) {
        if (module != null) {
            if (getParent() != null) {
                AxisService service = (AxisService) getParent();
                AxisConfiguration axiConfiguration = service.getAxisConfiguration();
                PhaseResolver phaseResolver = new PhaseResolver(axiConfiguration);
                if (service.isEngaged(module.getName())) {
                    phaseResolver.disEngageModulefromOperationChian(module, this);
                } else if (axiConfiguration != null &&
                        axiConfiguration.isEngaged(module.getName())) {
                    phaseResolver.disEngageModulefromOperationChian(module, this);
                } else {
                    if (axiConfiguration != null) {
                        phaseResolver.disEngageModulefromGlobalChains(module);
                    }
                    phaseResolver.disEngageModulefromOperationChian(module, this);
                }
            }
            engagedModules.remove(module);
        }
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
        //this arry list is retun , to avoid concurrent modifications , in the deployment engine
        ArrayList ops = new ArrayList();
        for (Iterator iterator = col.iterator(); iterator.hasNext();) {
            AxisOperation axisOperation = copyOperation((AxisOperation) iterator.next());
            ArrayList wsamappings = axisOperation.getWsamappingList();
            if (wsamappings != null) {
                for (int j = 0; j < wsamappings.size(); j++) {
                    String mapping = (String) wsamappings.get(j);

                    service.mapActionToOperation(mapping, axisOperation);
                }
            }
            if (service.getOperation(axisOperation.getName()) == null) {
                // this opration is a control operation.
                axisOperation.setControlOperation(true);
                Module moduleclazz = module.getModule();
                if (moduleclazz != null) {
                    moduleclazz.engageNotify(axisOperation);
                }
                phaseResolver.engageModuleToOperation(axisOperation, module);
                ops.add(axisOperation);
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
                AxisOperationFactory.getOperationDescription(axisOperation.getMessageExchangePattern());

        operation.setMessageReceiver(axisOperation.getMessageReceiver());
        operation.setName(axisOperation.getName());

        Iterator parameters = axisOperation.getParameters().iterator();

        while (parameters.hasNext()) {
            Parameter parameter = (Parameter) parameters.next();

            operation.addParameter(parameter);
        }

        operation.setWsamappingList(axisOperation.getWsamappingList());
        operation.setRemainingPhasesInFlow(axisOperation.getRemainingPhasesInFlow());
        operation.setPhasesInFaultFlow(axisOperation.getPhasesInFaultFlow());
        operation.setPhasesOutFaultFlow(axisOperation.getPhasesOutFaultFlow());
        operation.setPhasesOutFlow(axisOperation.getPhasesOutFlow());

        return operation;
    }


    /**
     * Creates a new operation context if there is not one already.
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

        if (null == msgContext.getRelatesTo()) {
            return null;
        } else {

            // So this message is part of an ongoing MEP
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

    /**
     * Finds a MEPContext for an incoming message. An incoming message can be
     * of two states.
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
                                                 ServiceContext serviceContext)
            throws AxisFault {
        OperationContext operationContext;

        if (null == msgContext.getRelatesTo()) {

            // Its a new incoming message so get the factory to create a new
            // one
            operationContext = new OperationContext(this, serviceContext);
        } else {

            // So this message is part of an ongoing MEP
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

    public void registerOperationContext(MessageContext msgContext,
                                         OperationContext operationContext)
            throws AxisFault {
        msgContext.getConfigurationContext().registerOperationContext(msgContext.getMessageID(),
                operationContext);
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

    public ModuleConfiguration getModuleConfig(QName moduleName) {
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
        this.style = style;
    }

    public void setWsamappingList(ArrayList wsamappingList) {
        this.wsamappingList = wsamappingList;
    }

    /**
     * 
     */
    public OperationClient createClient(ServiceContext sc, Options options) {
        throw new UnsupportedOperationException(Messages.getMessage("mepnotyetimplemented", mepURI));
    }

    public Object getKey() {
        return getName();
    }
}
