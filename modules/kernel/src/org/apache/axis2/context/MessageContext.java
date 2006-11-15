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

package org.apache.axis2.context;

import org.apache.axiom.attachments.Attachments;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.UUIDGenerator;
import org.apache.axiom.soap.SOAP11Constants;
import org.apache.axiom.soap.SOAP12Constants;
import org.apache.axiom.soap.SOAPConstants;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.addressing.RelatesTo;
import org.apache.axis2.client.Options;
import org.apache.axis2.description.*;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.neethi.Policy;

import javax.activation.DataHandler;
import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * MessageContext holds service specific state information.
 */
public class MessageContext extends AbstractContext {

    /**
     * A place to store the current MessageContext
     */
    public static ThreadLocal currentMessageContext = new ThreadLocal();

    public static MessageContext getCurrentMessageContext() {
        return (MessageContext) currentMessageContext.get();
    }

    public static void setCurrentMessageContext(MessageContext ctx) {
        currentMessageContext.set(ctx);
    }

    protected Options options;

    public final static int IN_FLOW = 1;
    public final static int IN_FAULT_FLOW = 3;

    public final static int OUT_FLOW = 2;
    public final static int OUT_FAULT_FLOW = 4;

    public static final String REMOTE_ADDR = "REMOTE_ADDR";

    public static final String TRANSPORT_HEADERS = "TRANSPORT_HEADERS";

    public Attachments attachments = new Attachments();

    /**
     * Field TRANSPORT_OUT
     */
    public static final String TRANSPORT_OUT = "TRANSPORT_OUT";

    /**
     * Field TRANSPORT_IN
     */
    public static final String TRANSPORT_IN = "TRANSPORT_IN";

    /**
     * Field CHARACTER_SET_ENCODING
     */
    public static final String CHARACTER_SET_ENCODING = "CHARACTER_SET_ENCODING";

    /**
     * Field UTF_8. This is the 'utf-8' value for CHARACTER_SET_ENCODING
     * property.
     */
    public static final String UTF_8 = "UTF-8";

    /**
     * Field UTF_16. This is the 'utf-16' value for CHARACTER_SET_ENCODING
     * property.
     */
    public static final String UTF_16 = "utf-16";

    /**
     * Field TRANSPORT_SUCCEED
     */
    public static final String TRANSPORT_SUCCEED = "TRANSPORT_SUCCEED";

    /**
     * Field DEFAULT_CHAR_SET_ENCODING. This is the default value for
     * CHARACTER_SET_ENCODING property.
     */
    public static final String DEFAULT_CHAR_SET_ENCODING = UTF_8;

    // to keep a ref to figure out which path your are in the execution (send or
    // receive)
    public int FLOW = IN_FLOW;

    /**
     * To invoke fireAndforget method we have to hand over transport sending logic to a thread
     * other wise user has to wait till it get transport response (in the case of HTTP its HTTP
     * 202)
     */
    public static final String TRANSPORT_NON_BLOCKING = "transportNonBlocking";

    /**
     * Field processingFault
     */
    private boolean processingFault;

    private boolean paused;

    public boolean outputWritten;

    /**
     * Field newThreadRequired
     */
    private boolean newThreadRequired;

    private boolean isSOAP11 = true;

    /**
     * The chain of Handlers/Phases for processing this message
     */
    private ArrayList executionChain = new ArrayList();

    // Are we doing REST now?
    private boolean doingREST;

    // Are we doing MTOM now?
    private boolean doingMTOM;

    // Are we doing SwA now?
    private boolean doingSwA;

    private transient AxisMessage axisMessage;

    private transient AxisOperation axisOperation;

    private transient AxisService axisService;

    private transient AxisServiceGroup axisServiceGroup;

    private ConfigurationContext configurationContext;

    /**
     * Index into the execution chain of the currently executing handler
     */
    private int currentHandlerIndex;

    /**
     * Index into the current Phase of the currently executing handler (if any)
     */
    private int currentPhaseIndex;

    /**
     * Field service
     */

    /**
     * Field envelope
     */
    private SOAPEnvelope envelope;

    private OperationContext operationContext;

    /**
     * Field responseWritten
     */
    private boolean responseWritten;

    /**
     * Field serverSide
     */
    private boolean serverSide;

    private ServiceContext serviceContext;

    private String serviceContextID;

    private ServiceGroupContext serviceGroupContext;

    /**
     * This will hold a key to retrieve the correct ServiceGroupContext.
     */
    private String serviceGroupContextId;

    /**
     * Field sessionContext
     */
    private SessionContext sessionContext;

    private transient TransportOutDescription transportOut;
    private transient TransportInDescription transportIn;

    //The value will be set by the transport receiver and there will be validation for the transport
    //at the dispatch phase (its post condition)
    private String incomingTransportName;

    public MessageContext() {
        super(null);
        options = new Options();
    }

    /**
     * Pause the execution of the current handler chain
     */
    public void pause() {
        paused = true;
    }

    public AxisOperation getAxisOperation() {
        return axisOperation;
    }

    public AxisService getAxisService() {
        return axisService;
    }

    public AxisServiceGroup getAxisServiceGroup() {
        return axisServiceGroup;
    }

    public ConfigurationContext getConfigurationContext() {
        return configurationContext;
    }

    public int getCurrentHandlerIndex() {
        return currentHandlerIndex;
    }

    public int getCurrentPhaseIndex() {
        return currentPhaseIndex;
    }

    /**
     * @return Returns SOAPEnvelope.
     */
    public SOAPEnvelope getEnvelope() {
        return envelope;
    }

    public ArrayList getExecutionChain() {
        return executionChain;
    }

    /**
     * @return Returns EndpointReference.
     */
    public EndpointReference getFaultTo() {
        return options.getFaultTo();
    }

    /**
     * @return Returns EndpointReference.
     */
    public EndpointReference getFrom() {
        return options.getFrom();
    }

    /**
     * @return Returns message id.
     */
    public String getMessageID() {
        return options.getMessageId();
    }

    /**
     * Retrieves both module specific configuration parameters as well as other
     * parameters. The order of search is as follows:
     * <ol>
     * <li> Search in module configurations inside corresponding operation
     * description if its there </li>
     * <li> Search in corresponding operation if its there </li>
     * <li> Search in module configurations inside corresponding service
     * description if its there </li>
     * <li> Next search in Corresponding Service description if its there </li>
     * <li> Next search in module configurations inside axisConfiguration </li>
     * <li> Search in AxisConfiguration for parameters </li>
     * <li> Next get the corresponding module and search for the parameters
     * </li>
     * <li> Search in HandlerDescription for the parameter </li>
     * </ol>
     * <p/> and the way of specifying module configuration is as follows
     * <moduleConfig name="addressing"> <parameter name="addressingPara"
     * locked="false">N/A</parameter> </moduleConfig>
     *
     * @param key        :
     *                   Parameter Name
     * @param moduleName :
     *                   Name of the module
     * @param handler    <code>HandlerDescription</code>
     * @return Parameter <code>Parameter</code>
     */
    public Parameter getModuleParameter(String key, String moduleName,
                                        HandlerDescription handler) {
        Parameter param;
        ModuleConfiguration moduleConfig;

        AxisOperation opDesc = getAxisOperation();

        if (opDesc != null) {

            moduleConfig = opDesc.getModuleConfig(new QName(moduleName));

            if (moduleConfig != null) {
                param = moduleConfig.getParameter(key);

                if (param != null) {
                    return param;
                } else {
                    param = opDesc.getParameter(key);

                    if (param != null) {
                        return param;
                    }
                }
            }
        }

        AxisService axisService = getAxisService();

        if (axisService != null) {

            moduleConfig = axisService.getModuleConfig(new QName(moduleName));

            if (moduleConfig != null) {
                param = moduleConfig.getParameter(key);

                if (param != null) {
                    return param;
                } else {
                    param = axisService.getParameter(key);

                    if (param != null) {
                        return param;
                    }
                }
            }
        }

        AxisServiceGroup axisServiceDesc = getAxisServiceGroup();

        if (axisServiceDesc != null) {

            moduleConfig = axisServiceDesc
                    .getModuleConfig(new QName(moduleName));

            if (moduleConfig != null) {
                param = moduleConfig.getParameter(key);

                if (param != null) {
                    return param;
                } else {
                    param = axisServiceDesc.getParameter(key);

                    if (param != null) {
                        return param;
                    }
                }
            }
        }

        AxisConfiguration baseConfig = configurationContext
                .getAxisConfiguration();

        moduleConfig = baseConfig.getModuleConfig(new QName(moduleName));

        if (moduleConfig != null) {
            param = moduleConfig.getParameter(key);

            if (param != null) {
                return param;
            } else {
                param = baseConfig.getParameter(key);

                if (param != null) {
                    return param;
                }
            }
        }

        AxisModule module = baseConfig.getModule(new QName(moduleName));

        if (module != null) {
            param = module.getParameter(key);

            if (param != null) {
                return param;
            }
        }

        param = handler.getParameter(key);

        return param;
    }

    public OperationContext getOperationContext() {
        return operationContext;
    }

    /**
     * Retrieves configuration descriptor parameters at any level. The order of
     * search is as follows:
     * <ol>
     * <li> Search in operation description if it exists </li>
     * <li> If parameter is not found or if operationContext is null, search in
     * AxisService </li>
     * <li> If parameter is not found or if axisService is null, search in
     * AxisConfiguration </li>
     * </ol>
     *
     * @param key
     * @return Parameter <code>Parameter</code>
     */
    public Parameter getParameter(String key) {
        if (axisOperation != null) {
            return axisOperation.getParameter(key);
        }

        if (axisService != null) {
            return axisService.getParameter(key);
        }

        if (axisServiceGroup != null) {
            return axisServiceGroup.getParameter(key);
        }

        if (configurationContext != null) {
            AxisConfiguration baseConfig = configurationContext
                    .getAxisConfiguration();
            return baseConfig.getParameter(key);
        }
        return null;
    }

    /**
     * Set a property for this message context.
     *
     * @param name  name of the property
     * @param value the value to set
     */
    public void setProperty(String name, Object value) {
        // we override this method here to make sure the properties are set on
        // options rather than in the inherited property bag.
        options.setProperty(name, value);
    }

    /**
     * Retrieves a property value. The order of search is as follows: search in
     * my own options and then look in my context hierarchy. Since its possible
     * that the entire hierarchy is not present, I will start at whatever level
     * has been set and start there.
     *
     * @param name name of the property to search for
     * @return the value of the property, or null if the property is not found
     */
    public Object getProperty(String name) {
        // search in my own options
        Object obj = options.getProperty(name);
        if (obj != null) {
            return obj;
        }

        // My own context hierarchy may not all be present. So look for whatever
        // nearest level is present and ask that to find the property.
        if (operationContext != null) {
            return operationContext.getProperty(name);
        }
        if (serviceContext != null) {
            return serviceContext.getProperty(name);
        }
        if (serviceGroupContext != null) {
            return serviceGroupContext.getProperty(name);
        }
        if (configurationContext != null) {
            return configurationContext.getProperty(name);
        }

        // tough
        return null;
    }

    /**
     * Retrieves all property values. The order of search is as follows: search in
     * my own options and then look in my context hierarchy. Since its possible
     * that the entire hierarchy is not present, it will start at whatever level
     * has been set and start there.
     * The returned map is unmodifiable, so any changes to the properties have
     * to be done by calling {@link #setProperty(String, Object)}. In addition,
     * any changes to the properties are not reflected on this map.
     *
     * @return An unmodifiable map containing the combination of all available
     *         properties or an empty map.
     */
    public Map getProperties() {
        final Map resultMap = new HashMap();

        // My own context hierarchy may not all be present. So look for whatever
        // nearest level is present and add the properties
        // We have to access the contexts in reverse order, in order to allow
        // a nearer context to overwrite values from a more distant context
        if (configurationContext != null) {
            resultMap.putAll(configurationContext.getProperties());
        }
        if (serviceGroupContext != null) {
            resultMap.putAll(serviceGroupContext.getProperties());
        }
        if (serviceContext != null) {
            resultMap.putAll(serviceContext.getProperties());
        }
        if (operationContext != null) {
            resultMap.putAll(operationContext.getProperties());
        }
        // and now add options
        resultMap.putAll(options.getProperties());
        return Collections.unmodifiableMap(resultMap);
    }

    /**
     * @return Returns RelatesTo array.
     */
    public RelatesTo[] getRelationships() {
        return options.getRelationships();
    }

    /**
     * @return Returns RelatesTo.
     */
    public RelatesTo getRelatesTo(String type) {
        return options.getRelatesTo(type);
    }

    /**
     * @return Returns RelatesTo.
     */
    public RelatesTo getRelatesTo() {
        return options.getRelatesTo();
    }

    /**
     * @return Returns EndpointReference.
     */
    public EndpointReference getReplyTo() {
        return options.getReplyTo();
    }

    /**
     * @return Returns ServiceContext.
     */
    public ServiceContext getServiceContext() {
        return serviceContext;
    }

    /**
     * @return Returns the serviceContextID.
     */
    public String getServiceContextID() {
        return serviceContextID;
    }

    public ServiceGroupContext getServiceGroupContext() {
        return serviceGroupContext;
    }

    public String getServiceGroupContextId() {
        return serviceGroupContextId;
    }

    /**
     * @return Returns SessionContext.
     */
    public SessionContext getSessionContext() {
        return sessionContext;
    }

    public void setSessionContext(SessionContext sessionContext) {
        this.sessionContext = sessionContext;
    }


    /**
     * @return Returns soap action.
     */
    public String getSoapAction() {
        return options.getAction();
    }

    /**
     * @return Returns EndpointReference.
     */
    public EndpointReference getTo() {
        return options.getTo();
    }

    /**
     * @return Returns TransportInDescription.
     */
    public TransportInDescription getTransportIn() {
        return transportIn;
    }

    /**
     * @return Returns TransportOutDescription.
     */
    public TransportOutDescription getTransportOut() {
        return transportOut;
    }

    public String getWSAAction() {
        return options.getAction();
    }

    /**
     * @return Returns boolean.
     */
    public boolean isDoingMTOM() {
        return doingMTOM;
    }

    /**
     * @return Returns boolean.
     */
    public boolean isDoingREST() {
        return doingREST;
    }

    /**
     * @return Returns boolean.
     */
    public boolean isDoingSwA() {
        return doingSwA;
    }

    /**
     * @return Returns boolean.
     */
    public boolean isNewThreadRequired() {
        return newThreadRequired;
    }

    /**
     * @return Returns boolean.
     */
    public boolean isOutputWritten() {
        return outputWritten;
    }

    /**
     * @return Returns boolean.
     */
    public boolean isPaused() {
        return paused;
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    /**
     * @return Returns boolean.
     */
    public boolean isProcessingFault() {
        return processingFault;
    }

    /**
     * @return Returns boolean.
     */
    public boolean isResponseWritten() {
        return responseWritten;
    }

    public boolean isSOAP11() {
        return isSOAP11;
    }

    /**
     * @return Returns boolean.
     */
    public boolean isServerSide() {
        return serverSide;
    }

    public AxisMessage getAxisMessage() {
        return axisMessage;
    }

    public void setAxisMessage(AxisMessage axisMessage) {
        this.axisMessage = axisMessage;
    }

    public void setAxisOperation(AxisOperation axisOperation) {
        this.axisOperation = axisOperation;
    }

    public void setAxisService(AxisService axisService) {
        this.axisService = axisService;
        this.axisServiceGroup = (AxisServiceGroup) this.axisService.getParent();
    }

    public void setAxisServiceGroup(AxisServiceGroup axisServiceGroup) {
        if (axisServiceGroup != null) {
            this.axisServiceGroup = axisServiceGroup;
        }
    }

    /**
     * @param context
     */
    public void setConfigurationContext(ConfigurationContext context) {
        configurationContext = context;
    }

    public void setCurrentHandlerIndex(int currentHandlerIndex) {
        this.currentHandlerIndex = currentHandlerIndex;
    }

    public void setCurrentPhaseIndex(int currentPhaseIndex) {
        this.currentPhaseIndex = currentPhaseIndex;
    }

    /**
     * @param b
     */
    public void setDoingMTOM(boolean b) {
        doingMTOM = b;
    }

    /**
     * @param b
     */
    public void setDoingREST(boolean b) {
        doingREST = b;
    }

    /**
     * @param b
     */
    public void setDoingSwA(boolean b) {
        doingSwA = b;
    }

    /**
     * @param envelope
     */
    public void setEnvelope(SOAPEnvelope envelope) throws AxisFault {
        this.envelope = envelope;

        String soapNamespaceURI = envelope.getNamespace().getNamespaceURI();

        if (SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI
                .equals(soapNamespaceURI)) {
            isSOAP11 = false;
        } else if (SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI
                .equals(soapNamespaceURI)) {
            isSOAP11 = true;
        } else {
            throw new AxisFault(
                    "Unknown SOAP Version. Current Axis handles only SOAP 1.1 and SOAP 1.2 messages");
        }
    }

    /**
     * Set the execution chain of Handler in this MessageContext. Doing this
     * causes the current handler/phase indexes to reset to 0, since we have new
     * Handlers to execute (this usually only happens at initialization and when
     * a fault occurs).
     *
     * @param executionChain
     */
    public void setExecutionChain(ArrayList executionChain) {
        this.executionChain = executionChain;
        currentHandlerIndex = -1;
        currentPhaseIndex = 0;
    }

    /**
     * @param reference
     */
    public void setFaultTo(EndpointReference reference) {
        options.setFaultTo(reference);
    }

    /**
     * @param reference
     */
    public void setFrom(EndpointReference reference) {
        options.setFrom(reference);
    }

    /**
     * @param messageId
     */
    public void setMessageID(String messageId) {
        options.setMessageId(messageId);
    }

    /**
     * @param b
     */
    public void setNewThreadRequired(boolean b) {
        newThreadRequired = b;
    }

    /**
     * @param context
     */
    public void setOperationContext(OperationContext context) {
        operationContext = context;

        if ((serviceContext != null) && (operationContext.getParent() == null)) {
            operationContext.setParent(serviceContext);
        }

        this.setParent(operationContext);

        if (operationContext != null) {
            this.setAxisOperation(operationContext.getAxisOperation());
        }
    }

    /**
     * @param b
     */
    public void setOutputWritten(boolean b) {
        outputWritten = b;
    }

    /**
     * @param b
     */
    public void setProcessingFault(boolean b) {
        processingFault = b;
    }

    /**
     * @param reference
     */
    public void addRelatesTo(RelatesTo reference) {
        options.addRelatesTo(reference);
    }

    /**
     * @param referance
     */
    public void setReplyTo(EndpointReference referance) {
        options.setReplyTo(referance);
    }

    /**
     * @param b
     */
    public void setResponseWritten(boolean b) {
        responseWritten = b;
    }

    /**
     * @param b
     */
    public void setServerSide(boolean b) {
        serverSide = b;
    }

    /**
     * @param context
     */
    public void setServiceContext(ServiceContext context) {
        serviceContext = context;
        if ((operationContext != null)
                && (operationContext.getParent() != null)) {
            operationContext.setParent(context);
        }
        // setting configcontext using configuration context in service context
        if (configurationContext == null) {
            // setting configcontext
            configurationContext = context.getConfigurationContext();
        }
        if (serviceGroupContext == null) {
            // setting service group context
            serviceGroupContext = context.getServiceGroupContext();
        }
        this.setAxisService(context.getAxisService());
    }

    /**
     * Sets the service context id.
     *
     * @param serviceContextID
     */
    public void setServiceContextID(String serviceContextID) {
        this.serviceContextID = serviceContextID;
    }

    public void setServiceGroupContext(ServiceGroupContext serviceGroupContext) {
        this.serviceGroupContext = serviceGroupContext;
        this.axisServiceGroup = serviceGroupContext.getDescription();
    }

    public void setServiceGroupContextId(String serviceGroupContextId) {
        this.serviceGroupContextId = serviceGroupContextId;
    }

    /**
     * @param soapAction
     */
    public void setSoapAction(String soapAction) {
        options.setAction(soapAction);
    }

    /**
     * @param referance
     */
    public void setTo(EndpointReference referance) {
        options.setTo(referance);
    }

    /**
     * @param in
     */
    public void setTransportIn(TransportInDescription in) {
        this.transportIn = in;
    }

    /**
     * @param out
     */
    public void setTransportOut(TransportOutDescription out) {
        transportOut = out;
    }

    /**
     * Method getExecutionChain
     */
    public void setWSAAction(String actionURI) {
        options.setAction(actionURI);
    }

    public void setWSAMessageId(String messageID) {
        options.setMessageId(messageID);
    }

    // to get the flow inwhich the execution chain below
    public int getFLOW() {
        return FLOW;
    }

    public void setFLOW(int FLOW) {
        this.FLOW = FLOW;
    }

    public Options getOptions() {
        return options;
    }

    /**
     * Set the options for myself. I make the given options my own options'
     * parent so that that becomes the default. That allows the user to override
     * specific options on a given message context and not affect the overall
     * options.
     *
     * @param options the options to set
     */
    public void setOptions(Options options) {
        this.options.setParent(options);
    }

    public String getIncomingTransportName() {
        return incomingTransportName;
    }

    public void setIncomingTransportName(String incomingTransportName) {
        this.incomingTransportName = incomingTransportName;
    }

    public void setRelationships(RelatesTo[] list) {
        options.setRelationships(list);
    }


    public Policy getEffectivePolicy() {
        if (axisMessage != null) {
            return axisMessage.getPolicyInclude().getEffectivePolicy();
        }
        if (axisOperation != null) {
            return axisOperation.getPolicyInclude().getEffectivePolicy();
        }
        if (axisService != null) {
            return axisService.getPolicyInclude().getEffectivePolicy();
        }
        return configurationContext.getAxisConfiguration().getPolicyInclude().getEffectivePolicy();
    }


    public boolean isEngaged(QName moduleName) {
        boolean enegage;
        if (configurationContext != null) {
            AxisConfiguration axisConfig = configurationContext.getAxisConfiguration();
            AxisModule module = axisConfig.getModule(moduleName);
            if (module == null) {
                return false;
            }
            enegage = axisConfig.isEngaged(moduleName);
            if (enegage) {
                return true;
            }
            if (axisServiceGroup != null) {
                enegage = axisServiceGroup.isEngaged(moduleName);
                if (enegage) {
                    return true;
                }
            }
            if (axisService != null) {
                enegage = axisService.isEngaged(moduleName);
                if (enegage) {
                    return true;
                }
            }
            if (axisOperation != null) {
                enegage = axisOperation.isEngaged(module.getName());
                if (enegage) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Gets the first child of the envelope, check if it is a soap:Body, which means there is no header.
     * We do this basically to make sure we don't parse and build the om tree of the whole envelope
     * looking for the soap header. If this method returns true, there still is no guarantee that there is
     * a soap:Header present, use getHeader() and also check for null on getHeader() to be absolutely sure.
     *
     * @return boolean
     */
    public boolean isHeaderPresent() {
        OMElement node = this.envelope.getFirstElement();
        if (node == null) {
            return false;
        } else if (node.getQName().getLocalPart().equals(SOAPConstants.BODY_LOCAL_NAME)) {
            return false;
        }
        return true;
    }

    /**
     * Setting of the attachments map should be performed at the receipt of a
     * message only. This method is only meant to be used by the Axis2
     * internals.
     *
     * @param attachments
     */
    public void setAttachmentMap(Attachments attachments) {
        this.attachments = attachments;
    }

    public Attachments getAttachmentMap() {
        return attachments;
    }

    public void addAttachment(String contentID, DataHandler dataHandler) {
        attachments.addDataHandler(contentID, dataHandler);
    }

    public String addAttachment(DataHandler dataHandler) {
        String contentID = UUIDGenerator.getUUID();
        addAttachment(contentID, dataHandler);
        return contentID;
    }

    public DataHandler getAttachment(String contentID) {
        return attachments.getDataHandler(contentID);
    }
}
