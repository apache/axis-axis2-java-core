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
import org.apache.axiom.om.OMOutputFormat;
import org.apache.axiom.om.impl.MTOMConstants;
import org.apache.axiom.om.impl.builder.StAXBuilder;
import org.apache.axiom.om.util.StAXUtils;
import org.apache.axiom.om.util.UUIDGenerator;
import org.apache.axiom.soap.SOAP11Constants;
import org.apache.axiom.soap.SOAP12Constants;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.impl.builder.StAXSOAPModelBuilder;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.addressing.RelatesTo;
import org.apache.axis2.builder.BuilderUtil;
import org.apache.axis2.client.Options;
import org.apache.axis2.description.AxisMessage;
import org.apache.axis2.description.AxisModule;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.AxisServiceGroup;
import org.apache.axis2.description.HandlerDescription;
import org.apache.axis2.description.ModuleConfiguration;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.description.TransportInDescription;
import org.apache.axis2.description.TransportOutDescription;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.engine.Handler;
import org.apache.axis2.engine.Phase;
import org.apache.axis2.util.LoggingControl;
import org.apache.axis2.util.MetaDataEntry;
import org.apache.axis2.util.ObjectStateUtils;
import org.apache.axis2.util.SelfManagedDataHolder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.neethi.Policy;

import javax.activation.DataHandler;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * MessageContext holds service specific state information.
 */
public class MessageContext extends AbstractContext implements Externalizable {

    /*
     * setup for logging
     */
    private static final Log log = LogFactory.getLog(MessageContext.class);

    /**
     * @serial An ID which can be used to correlate operations on a single
     * message in the log files, irrespective of thread switches, persistence,
     * etc.
     */
    private String logCorrelationID = null;

    /**
     * This string will be used to hold a form of the logCorrelationID that
     * is more suitable for output than its generic form.
     */
    private transient String logCorrelationIDString = null;

    private static final String myClassName = "MessageContext";

    /**
     * @serial The serialization version ID tracks the version of the class.
     * If a class definition changes, then the serialization/externalization
     * of the class is affected. If a change to the class is made which is
     * not compatible with the serialization/externalization of the class,
     * then the serialization version ID should be updated.
     * Refer to the "serialVer" utility to compute a serialization
     * version ID.
     */
    private static final long serialVersionUID = -7753637088257391858L;

    /**
     * @serial Tracks the revision level of a class to identify changes to the
     * class definition that are compatible to serialization/externalization.
     * If a class definition changes, then the serialization/externalization
     * of the class is affected.
     * Refer to the writeExternal() and readExternal() methods.
     */
    // supported revision levels, add a new level to manage compatible changes
    private static final int REVISION_1 = 1;
    // current revision level of this object
    private static final int revisionID = REVISION_1;


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


    /**
     * @serial Options on the message
     */
    protected Options options;

    public final static int IN_FLOW = 1;
    public final static int IN_FAULT_FLOW = 3;

    public final static int OUT_FLOW = 2;
    public final static int OUT_FAULT_FLOW = 4;

    public static final String REMOTE_ADDR = "REMOTE_ADDR";
    public static final String TRANSPORT_ADDR = "TRANSPORT_ADDR";
    public static final String TRANSPORT_HEADERS = "TRANSPORT_HEADERS";


    /**
     * message attachments
     * NOTE: Serialization of message attachments is handled as part of the
     * overall message serialization.  If this needs to change, then
     * investigate having the Attachment class implement the
     * java.io.Externalizable interface.
     */
    public transient Attachments attachments;

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

    /**
     * @serial The direction flow in use to figure out which path the message is in
     * (send or receive)
     */
    public int FLOW = IN_FLOW;

    /**
     * To invoke fireAndforget method we have to hand over transport sending logic to a thread
     * other wise user has to wait till it get transport response (in the case of HTTP its HTTP
     * 202)
     */
    public static final String TRANSPORT_NON_BLOCKING = "transportNonBlocking";

    /**
     * This property allows someone (e.g. RM) to disable an async callback from
     * being invoked if a fault occurs during message transmission.  If this is
     * not set, it can be assumed that the fault will be delivered via
     * Callback.onError(...).
     */
    public static final String DISABLE_ASYNC_CALLBACK_ON_TRANSPORT_ERROR =
            "disableTransmissionErrorCallback";

    /**
     * @serial processingFault
     */
    private boolean processingFault;

    /**
     * @serial paused
     */
    private boolean paused;

    /**
     * @serial outputWritten
     */
    public boolean outputWritten;

    /**
     * @serial newThreadRequired
     */
    private boolean newThreadRequired;

    /**
     * @serial isSOAP11
     */
    private boolean isSOAP11 = true;

    /**
     * @serial The chain of Handlers/Phases for processing this message
     */
    private ArrayList executionChain;

    /**
     * @serial The chain of executed Handlers/Phases from inbound processing
     */
    private LinkedList inboundExecutedPhases;

    /**
     * @serial The chain of executed Handlers/Phases from outbound processing
     */
    private LinkedList outboundExecutedPhases;

    /**
     * @serial Flag to indicate if we are doing REST
     */
    private boolean doingREST;

    /**
     * @serial Flag to indicate if we are doing MTOM
     */
    private boolean doingMTOM;

    /**
     * @serial Flag to indicate if we are doing SWA
     */
    private boolean doingSwA;

    /**
     * AxisMessage associated with this message context
     */
    private transient AxisMessage axisMessage;

    /**
     * AxisOperation associated with this message context
     */
    private transient AxisOperation axisOperation;

    /**
     * AxisService
     */
    private transient AxisService axisService;

    /**
     * AxisServiceGroup
     * <p/>
     * Note the service group can be set independently of the service
     * so the service might not match up with this serviceGroup
     */
    private transient AxisServiceGroup axisServiceGroup;

    /**
     * ConfigurationContext
     */
    private transient ConfigurationContext configurationContext;

    /**
     * @serial Index into the executuion chain of the currently executing handler
     */
    private int currentHandlerIndex;

    /**
     * @serial Index into the current Phase of the currently executing handler (if any)
     */
    private int currentPhaseIndex;

    /**
     * @serial SOAP envelope
     */
    private SOAPEnvelope envelope;

    /**
     * @serial OperationContext
     */
    private OperationContext operationContext;

    /**
     * @serial responseWritten
     */
    private boolean responseWritten;

    /**
     * @serial serverSide
     */
    private boolean serverSide;

    /**
     * @serial ServiceContext
     */
    private ServiceContext serviceContext;

    /**
     * @serial service context ID
     */
    private String serviceContextID;

    /**
     * @serial service group context
     */
    private ServiceGroupContext serviceGroupContext;

    /**
     * @serial Holds a key to retrieve the correct ServiceGroupContext.
     */
    private String serviceGroupContextId;

    /**
     * @serial sessionContext
     */
    private SessionContext sessionContext;


    /**
     * transport out description
     */
    private transient TransportOutDescription transportOut;

    /**
     * transport in description
     */
    private transient TransportInDescription transportIn;


    /**
     * @serial incoming transport name
     */
    //The value will be set by the transport receiver and there will be validation for the transport
    //at the dispatch phase (its post condition)
    private String incomingTransportName;


    /*
     * SelfManagedData will hold message-specific data set by handlers
     * Note that this list is not explicitly saved by the MessageContext, but
     * rather through the SelfManagedDataManager interface implemented by handlers
     */
    private transient LinkedHashMap selfManagedDataMap = null;

    //-------------------------------------------------------------------------
    // MetaData for data to be restored in activate() after readExternal()
    //-------------------------------------------------------------------------

    /**
     * Indicates whether the message context has been reconstituted
     * and needs to have its object references reconciled
     */
    private transient boolean needsToBeReconciled = false;

    /**
     * selfManagedDataHandlerCount is a count of the number of handlers
     * that actually saved data during serialization
     */
    private transient int selfManagedDataHandlerCount = 0;

    /**
     * SelfManagedData cannot be restored until the configurationContext
     * is available, so we have to hold the data from readExternal until
     * activate is called.
     */
    private transient ArrayList selfManagedDataListHolder = null;

    /**
     * The ordered list of metadata for handlers/phases
     * used during re-constitution of the message context
     */
    private transient ArrayList metaExecutionChain = null;

    /**
     * The ordered list of metadata for inbound executed phases
     * used during re-constitution of the message context
     */
    private transient LinkedList metaInboundExecuted = null;

    /**
     * The ordered list of metadata for outbound executed phases
     * used during re-constitution of the message context
     */
    private transient LinkedList metaOutboundExecuted = null;

    /**
     * Index into the executuion chain of the currently executing handler
     */
    private transient int metaHandlerIndex = 0;

    /**
     * Index into the current Phase of the currently executing handler (if any)
     */
    private transient int metaPhaseIndex = 0;

    /**
     * The AxisOperation metadata will be used during
     * activate to match up with an existing object
     */
    private transient MetaDataEntry metaAxisOperation = null;

    /**
     * The AxisService metadata will be used during
     * activate to match up with an existing object
     */
    private transient MetaDataEntry metaAxisService = null;

    /**
     * The AxisServiceGroup metadata will be used during
     * activate to match up with an existing object
     */
    private transient MetaDataEntry metaAxisServiceGroup = null;

    /**
     * The TransportOutDescription metadata will be used during
     * activate to match up with an existing object
     */
    private transient MetaDataEntry metaTransportOut = null;

    /**
     * The TransportInDescription metadata will be used during
     * activate to match up with an existing object
     */
    private transient MetaDataEntry metaTransportIn = null;

    /**
     * The AxisMessage metadata will be used during
     * activate to match up with an existing object
     */
    private transient MetaDataEntry metaAxisMessage = null;

    /**
     * Indicates whether this message context has an
     * AxisMessage object associated with it that needs to
     * be reconciled
     */
    private transient boolean reconcileAxisMessage = false;

    /**
     * Indicates whether the inbound executed phase list
     * was reset before the restored list has been reconciled
     */
    private transient boolean inboundReset = false;

    /**
     * Indicates whether the outbound executed phase list
     * was reset before the restored list has been reconciled
     */
    private transient boolean outboundReset = false;

    //----------------------------------------------------------------
    // end MetaData section
    //----------------------------------------------------------------


    /**
     * Constructor
     */
    public MessageContext() {
        super(null);
        options = new Options();
    }

    /**
     * Constructor has package access
     *
     * @param configContext the associated ConfigurationContext
     */
    MessageContext(ConfigurationContext configContext) {
        this();
        setConfigurationContext(configContext);
    }

    public String toString() {
        return getLogIDString();
    }

    /**
     * Get a "raw" version of the logCorrelationID.  The logCorrelationID
     * is guaranteed to be unique and may be persisted along with the rest
     * of the message context.
     *
     * @return A string that can be output to a log file as an identifier
     *         for this MessageContext.  It is suitable for matching related log
     *         entries.
     */
    public String getLogCorrelationID() {
        if (logCorrelationID == null) {
            logCorrelationID = UUIDGenerator.getUUID();
        }
        return logCorrelationID;
    }

    /**
     * Get a formatted version of the logCorrelationID.
     *
     * @return A string that can be output to a log file as an identifier
     *         for this MessageContext.  It is suitable for matching related log
     *         entries.
     */
    public String getLogIDString() {
        if (logCorrelationIDString == null) {
            logCorrelationIDString = "[MessageContext: logID=" + getLogCorrelationID() + "]";
        }
        return logCorrelationIDString;
    }


    /**
     * Pause the execution of the current handler chain
     */
    public void pause() {
        paused = true;
    }

    public AxisOperation getAxisOperation() {
        if (LoggingControl.debugLoggingAllowed) {
            checkActivateWarning("getAxisOperation");
        }
        return axisOperation;
    }

    public AxisService getAxisService() {
        if (LoggingControl.debugLoggingAllowed) {
            checkActivateWarning("getAxisService");
        }
        return axisService;
    }

    /*
     * <P>
     * Note the service group can be set independently of the service
     * so the service might not match up with this serviceGroup
    */
    public AxisServiceGroup getAxisServiceGroup() {
        if (LoggingControl.debugLoggingAllowed) {
            checkActivateWarning("getAxisServiceGroup");
        }
        return axisServiceGroup;
    }

    public ConfigurationContext getConfigurationContext() {
        if (LoggingControl.debugLoggingAllowed) {
            checkActivateWarning("getConfigurationContext");
        }
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
        if (LoggingControl.debugLoggingAllowed) {
            checkActivateWarning("getExecutionChain");
        }
        return executionChain;
    }

    /**
     * Add a Phase to the collection of executed phases for the inbound path.
     * Phases will be inserted in a LIFO data structure.
     *
     * @param phase The phase to add to the list.
     */
    public void addInboundExecutedPhase(Handler phase) {
        if (inboundExecutedPhases == null) {
            inboundExecutedPhases = new LinkedList();
        }
        inboundExecutedPhases.addFirst(phase);
    }

    /**
     * Remove the first Phase in the collection of executed phases for the
     * inbound path.
     */
    public void removeFirstInboundExecutedPhase() {
        if (inboundExecutedPhases != null) {
            inboundExecutedPhases.removeFirst();
        }
    }

    /**
     * Get an iterator over the inbound executed phase list.
     *
     * @return An Iterator over the LIFO data structure.
     */
    public Iterator getInboundExecutedPhases() {
        if (LoggingControl.debugLoggingAllowed) {
            checkActivateWarning("getInboundExecutedPhases");
        }
        if (inboundExecutedPhases == null) {
            inboundExecutedPhases = new LinkedList();
        }
        return inboundExecutedPhases.iterator();
    }

    /**
     * Reset the list of executed inbound phases.
     * This is needed because the OutInAxisOperation currently invokes
     * receive() even when a fault occurs, and we will have already executed
     * the flowComplete on those before receiveFault() is called.
     */
    public void resetInboundExecutedPhases() {
        inboundReset = true;
        inboundExecutedPhases = new LinkedList();
    }

    /**
     * Add a Phase to the collection of executed phases for the outbound path.
     * Phases will be inserted in a LIFO data structure.
     *
     * @param phase The phase to add to the list.
     */
    public void addOutboundExecutedPhase(Handler phase) {
        if (outboundExecutedPhases == null) {
            outboundExecutedPhases = new LinkedList();
        }
        outboundExecutedPhases.addFirst(phase);
    }

    /**
     * Remove the first Phase in the collection of executed phases for the
     * outbound path.
     */
    public void removeFirstOutboundExecutedPhase() {
        if (outboundExecutedPhases != null) {
            outboundExecutedPhases.removeFirst();
        }
    }

    /**
     * Get an iterator over the outbound executed phase list.
     *
     * @return An Iterator over the LIFO data structure.
     */
    public Iterator getOutboundExecutedPhases() {
        if (LoggingControl.debugLoggingAllowed) {
            checkActivateWarning("getOutboundExecutedPhases");
        }
        if (outboundExecutedPhases == null) {
            outboundExecutedPhases = new LinkedList();
        }
        return outboundExecutedPhases.iterator();
    }

    /**
     * Reset the list of executed outbound phases.
     * This is needed because the OutInAxisOperation currently invokes
     * receive() even when a fault occurs, and we will have already executed
     * the flowComplete on those before receiveFault() is called.
     */
    public void resetOutboundExecutedPhases() {
        outboundReset = true;
        outboundExecutedPhases = new LinkedList();
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

            moduleConfig = opDesc.getModuleConfig(moduleName);

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

            moduleConfig = axisService.getModuleConfig(moduleName);

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

            moduleConfig = axisServiceDesc.getModuleConfig(moduleName);

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

        AxisConfiguration baseConfig = configurationContext.getAxisConfiguration();

        moduleConfig = baseConfig.getModuleConfig(moduleName);

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

        AxisModule module = baseConfig.getModule(moduleName);

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
        if (LoggingControl.debugLoggingAllowed) {
            checkActivateWarning("getOperationContext");
        }
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
        if (LoggingControl.debugLoggingAllowed) {
            checkActivateWarning("getProperty");
        }

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
     * to be done by calling {@link #setProperty(String,Object)}. In addition,
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
        if (LoggingControl.debugLoggingAllowed) {
            checkActivateWarning("getServiceContext");
        }
        return serviceContext;
    }

    /**
     * @return Returns the serviceContextID.
     */
    public String getServiceContextID() {
        return serviceContextID;
    }

    public ServiceGroupContext getServiceGroupContext() {
        if (LoggingControl.debugLoggingAllowed) {
            checkActivateWarning("getServiceGroupContext");
        }
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
        if (LoggingControl.debugLoggingAllowed) {
            checkActivateWarning("getTransportIn");
        }
        return transportIn;
    }

    /**
     * @return Returns TransportOutDescription.
     */
    public TransportOutDescription getTransportOut() {
        if (LoggingControl.debugLoggingAllowed) {
            checkActivateWarning("getTransportOut");
        }
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
        if (reconcileAxisMessage) {
            if (LoggingControl.debugLoggingAllowed && log.isWarnEnabled()) {
                log.warn(this.getLogIDString() +
                    ":getAxisMessage(): ****WARNING**** MessageContext.activate(configurationContext) needs to be invoked.");
            }
        }

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
        if (this.axisService != null) {
            this.axisServiceGroup = (AxisServiceGroup) this.axisService.getParent();
        } else {
            this.axisServiceGroup = null;
        }
    }

    /*
     * note setAxisServiceGroup() does not verify that the service is associated with the service group!
     */
    public void setAxisServiceGroup(AxisServiceGroup axisServiceGroup) {
        // need to set the axis service group object to null when necessary
        // for example, when extracting the message context object from
        // the object graph
        this.axisServiceGroup = axisServiceGroup;
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

        if (this.envelope != null) {
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
     * @param context The OperationContext
     */
    public void setOperationContext(OperationContext context) {
        // allow setting the fields to null
        // useful when extracting the messge context from the object graph
        operationContext = context;

        this.setParent(operationContext);

        if (operationContext != null) {
            if ((serviceContext != null) && (operationContext.getParent() == null)) {
                operationContext.setParent(serviceContext);
            }

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
     * Add a RelatesTo
     *
     * @param reference RelatesTo describing how we relate to another message
     */
    public void addRelatesTo(RelatesTo reference) {
        options.addRelatesTo(reference);
    }

    /**
     * Set ReplyTo destination
     *
     * @param reference the ReplyTo EPR
     */
    public void setReplyTo(EndpointReference reference) {
        options.setReplyTo(reference);
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

        // allow the service context to be set to null
        // this allows the message context object to be extraced from
        // the object graph

        serviceContext = context;

        if (serviceContext != null) {
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
        // allow the service group context to be set to null
        // this allows the message context object to be extraced from
        // the object graph

        this.serviceGroupContext = serviceGroupContext;

        if (this.serviceGroupContext != null) {
            this.axisServiceGroup = serviceGroupContext.getDescription();
        }
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
     * @param to
     */
    public void setTo(EndpointReference to) {
        options.setTo(to);
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
     * setWSAAction
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
        if (LoggingControl.debugLoggingAllowed) {
            checkActivateWarning("getOptions");
        }
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
        if (LoggingControl.debugLoggingAllowed) {
            checkActivateWarning("getEffectivePolicy");
        }
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


    public boolean isEngaged(String moduleName) {
        if (LoggingControl.debugLoggingAllowed) {
            checkActivateWarning("isEngaged");
        }
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
     * @deprecated The bonus you used to get from this is now built in to SOAPEnvelope.getHeader()
     */
    public boolean isHeaderPresent() {
        // If there's no envelope there can't be a header.
        if (this.envelope == null) {
            return false;
        }
        return (this.envelope.getHeader() != null);
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

    /**
     * You can directly access the attachment map of the message context from
     * here. Returned attachment map can be empty.
     *
     * @return attachment
     */
    public Attachments getAttachmentMap() {
        if (attachments == null) {
            attachments = new Attachments();
        }
        return attachments;
    }

    /**
     * Adds an attachment to the attachment Map of this message context. This
     * attachment gets serialised as a MIME attachment when sending the message
     * if SOAP with Attachments is enabled.
     *
     * @param contentID   :
     *                    will be the content ID of the MIME part
     * @param dataHandler
     */
    public void addAttachment(String contentID, DataHandler dataHandler) {
        if (attachments == null) {
            attachments = new Attachments();
        }
        attachments.addDataHandler(contentID, dataHandler);
    }

    /**
     * Adds an attachment to the attachment Map of this message context. This
     * attachment gets serialised as a MIME attachment when sending the message
     * if SOAP with Attachments is enabled. Content ID of the MIME part will be
     * auto generated by Axis2.
     *
     * @param dataHandler
     * @return the auto generated content ID of the MIME attachment
     */
    public String addAttachment(DataHandler dataHandler) {
        String contentID = UUIDGenerator.getUUID();
        addAttachment(contentID, dataHandler);
        return contentID;
    }

    /**
     * Access the DataHandler of the attachment contained in the map corresponding to the given
     * content ID. Returns "NULL" if a attachment cannot be found by the given content ID.
     *
     * @param contentID :
     *                  Content ID of the MIME attachment
     * @return Data handler of the attachment
     */
    public DataHandler getAttachment(String contentID) {
        if (attachments == null) {
            attachments = new Attachments();
        }
        return attachments.getDataHandler(contentID);
    }

    /**
     * Removes the attachment with the given content ID from the Attachments Map
     * Do nothing if a attachment cannot be found by the given content ID.
     *
     * @param contentID of the attachment
     */
    public void removeAttachment(String contentID) {
        if (attachments != null) {
            attachments.removeDataHandler(contentID);
        }
    }

    /*
     * ===============================================================
     * SelfManagedData Section
     * ===============================================================
     */

    /*
    * character to delimit strings
    */
    private String selfManagedDataDelimiter = "*";


    /**
     * Set up a unique key in the form of
     * <OL>
     * <LI>the class name for the class that owns the key
     * <LI>delimitor
     * <LI>the key as a string
     * <LI>delimitor
     * <LI>the key's hash code as a string
     * </OL>
     *
     * @param clazz The class that owns the supplied key
     * @param key   The key
     * @return A string key
     */
    private String generateSelfManagedDataKey(Class clazz, Object key) {
        return clazz.getName() + selfManagedDataDelimiter + key.toString() +
                selfManagedDataDelimiter + Integer.toString(key.hashCode());
    }

    /**
     * Add a key-value pair of self managed data to the set associated with
     * this message context.
     * <p/>
     * This is primarily intended to allow handlers to manage their own
     * message-specific data when the message context is saved/restored.
     *
     * @param clazz The class of the caller that owns the key-value pair
     * @param key   The key for this data object
     * @param value The data object
     */
    public void setSelfManagedData(Class clazz, Object key, Object value) {
        if (selfManagedDataMap == null) {
            selfManagedDataMap = new LinkedHashMap();
        }

        // make sure we have a unique key and a delimiter so we can
        // get the classname and hashcode for serialization/deserialization
        selfManagedDataMap.put(generateSelfManagedDataKey(clazz, key), value);
    }

    /**
     * Retrieve a value of self managed data previously saved with the specified key.
     *
     * @param clazz The class of the caller that owns the key-value pair
     * @param key   The key for the data
     * @return The data object associated with the key, or NULL if not found
     */
    public Object getSelfManagedData(Class clazz, Object key) {
        if (selfManagedDataMap != null) {
            return selfManagedDataMap.get(generateSelfManagedDataKey(clazz, key));
        }
        return null;
    }

    /**
     * Check to see if the key for the self managed data is available
     *
     * @param clazz The class of the caller that owns the key-value pair
     * @param key   The key to look for
     * @return TRUE if the key exists, FALSE otherwise
     */
    public boolean containsSelfManagedDataKey(Class clazz, Object key) {
        if (selfManagedDataMap == null) {
            return false;
        }
        return selfManagedDataMap.containsKey(generateSelfManagedDataKey(clazz, key));
    }

    /**
     * Removes the mapping of the specified key if the specified key
     * has been set for self managed data
     *
     * @param clazz The class of the caller that owns the key-value pair
     * @param key   The key of the object to be removed
     */
    public void removeSelfManagedData(Class clazz, Object key) {
        if (selfManagedDataMap != null) {
            selfManagedDataMap.remove(generateSelfManagedDataKey(clazz, key));
        }
    }

    /**
     * Flatten the phase list into a list of just unique handler instances
     *
     * @param list the list of handlers
     * @param map  users should pass null as this is just a holder for the recursion
     * @return a list of unigue object instances
     */
    private ArrayList flattenPhaseListToHandlers(ArrayList list, LinkedHashMap map) {

        if (map == null) {
            map = new LinkedHashMap();
        }

        Iterator it = list.iterator();
        while (it.hasNext()) {
            Handler handler = (Handler) it.next();

            String key = null;
            if (handler != null) {
                key = handler.getClass().getName() + "@" + handler.hashCode();
            }

            if (handler instanceof Phase) {
                // add its handlers to the list
                flattenHandlerList(((Phase) handler).getHandlers(), map);
            } else {
                // if the same object is already in the list,
                // then it won't be in the list multiple times
                map.put(key, handler);
            }
        }

        if (LoggingControl.debugLoggingAllowed && log.isTraceEnabled()) {
            Iterator it2 = map.keySet().iterator();
            while (it2.hasNext()) {
                Object key = it2.next();
                Handler value = (Handler) map.get(key);
                String name = value.getName();
                log.trace(getLogIDString() + ":flattenPhaseListToHandlers():  key [" + key +
                        "]    handler name [" + name + "]");
            }
        }


        return new ArrayList(map.values());
    }


    /**
     * Flatten the handler list into just unique handler instances
     * including phase instances.
     *
     * @param list the list of handlers/phases
     * @param map  users should pass null as this is just a holder for the recursion
     * @return a list of unigue object instances
     */
    private ArrayList flattenHandlerList(ArrayList list, LinkedHashMap map) {

        if (map == null) {
            map = new LinkedHashMap();
        }

        Iterator it = list.iterator();
        while (it.hasNext()) {
            Handler handler = (Handler) it.next();

            String key = null;
            if (handler != null) {
                key = handler.getClass().getName() + "@" + handler.hashCode();
            }

            if (handler instanceof Phase) {
                // put the phase in the list
                map.put(key, handler);

                // add its handlers to the list
                flattenHandlerList(((Phase) handler).getHandlers(), map);
            } else {
                // if the same object is already in the list,
                // then it won't be in the list multiple times
                map.put(key, handler);
            }
        }

        return new ArrayList(map.values());
    }


    /**
     * Calls the serializeSelfManagedData() method of each handler that
     * implements the <bold>SelfManagedDataManager</bold> interface.
     * Handlers for this message context are identified via the
     * executionChain list.
     *
     * @param out The output stream
     */
    private void serializeSelfManagedData(ObjectOutput out) {
        selfManagedDataHandlerCount = 0;

        try {
            if ((selfManagedDataMap == null)
                    || (executionChain == null)
                    || (selfManagedDataMap.size() == 0)
                    || (executionChain.size() == 0)) {
                out.writeBoolean(ObjectStateUtils.EMPTY_OBJECT);

                if (LoggingControl.debugLoggingAllowed && log.isTraceEnabled()) {
                    log.trace(getLogIDString() + ":serializeSelfManagedData(): No data : END");
                }

                return;
            }

            // let's create a temporary list with the handlers
            ArrayList flatExecChain = flattenPhaseListToHandlers(executionChain, null);

            //ArrayList selfManagedDataHolderList = serializeSelfManagedDataHelper(flatExecChain.iterator(), new ArrayList());
            ArrayList selfManagedDataHolderList = serializeSelfManagedDataHelper(flatExecChain);

            if (selfManagedDataHolderList.size() == 0) {
                out.writeBoolean(ObjectStateUtils.EMPTY_OBJECT);

                if (LoggingControl.debugLoggingAllowed && log.isTraceEnabled()) {
                    log.trace(getLogIDString() + ":serializeSelfManagedData(): No data : END");
                }

                return;
            }

            out.writeBoolean(ObjectStateUtils.ACTIVE_OBJECT);

            // SelfManagedData can be binary so won't be able to treat it as a
            // string - need to treat it as a byte []

            // how many handlers actually
            // returned serialized SelfManagedData
            out.writeInt(selfManagedDataHolderList.size());

            for (int i = 0; i < selfManagedDataHolderList.size(); i++) {
                out.writeObject(selfManagedDataHolderList.get(i));
            }

        }
        catch (IOException e) {
            if (LoggingControl.debugLoggingAllowed && log.isTraceEnabled()) {
                log.trace("MessageContext:serializeSelfManagedData(): Exception [" +
                    e.getClass().getName() + "]  description [" + e.getMessage() + "]", e);
            }
        }

    }


    /**
     * This is the helper method to do the recursion for serializeSelfManagedData()
     *
     * @param handlers
     * @return ArrayList
     */
    private ArrayList serializeSelfManagedDataHelper(ArrayList handlers) {
        ArrayList selfManagedDataHolderList = new ArrayList();
        Iterator it = handlers.iterator();

        try {
            while (it.hasNext()) {
                Handler handler = (Handler) it.next();

                //if (handler instanceof Phase)
                //{
                //    selfManagedDataHolderList = serializeSelfManagedDataHelper(((Phase)handler).getHandlers().iterator(), selfManagedDataHolderList);
                //}
                //else if (SelfManagedDataManager.class.isAssignableFrom(handler.getClass()))
                if (SelfManagedDataManager.class.isAssignableFrom(handler.getClass())) {
                    // only call the handler's serializeSelfManagedData if it implements SelfManagedDataManager

                    if (LoggingControl.debugLoggingAllowed && log.isTraceEnabled()) {
                        log.trace(
                                "MessageContext:serializeSelfManagedDataHelper(): calling handler  [" +
                                        handler.getClass().getName() + "]  name [" +
                                        handler.getName() + "]   serializeSelfManagedData method");
                    }

                    ByteArrayOutputStream baos_fromHandler =
                            ((SelfManagedDataManager) handler).serializeSelfManagedData(this);

                    if (baos_fromHandler != null) {
                        baos_fromHandler.close();

                        try {
                            SelfManagedDataHolder selfManagedDataHolder = new SelfManagedDataHolder(
                                    handler.getClass().getName(), handler.getName(),
                                    baos_fromHandler.toByteArray());
                            selfManagedDataHolderList.add(selfManagedDataHolder);
                            selfManagedDataHandlerCount++;
                        }
                        catch (Exception exc) {
                            if (LoggingControl.debugLoggingAllowed && log.isTraceEnabled()) {
                                log.trace("MessageContext:serializeSelfManagedData(): exception [" +
                                    exc.getClass().getName() + "][" + exc.getMessage() +
                                    "]  in setting up SelfManagedDataHolder object for [" +
                                    handler.getClass().getName() + " / " + handler.getName() + "] ",
                                      exc);
                            }
                        }
                    }
                }
            }

            return selfManagedDataHolderList;
        }
        catch (Exception ex) {
            if (LoggingControl.debugLoggingAllowed && log.isTraceEnabled()) {
                log.trace("MessageContext:serializeSelfManagedData(): exception [" +
                    ex.getClass().getName() + "][" + ex.getMessage() + "]", ex);
            }
            return null;
        }

    }

    /**
     * During deserialization, the executionChain will be
     * re-constituted before the SelfManagedData is restored.
     * This means the handler instances are already available.
     * This method lets us find the handler instance from the
     * executionChain so we can call each one's
     * deserializeSelfManagedData method.
     *
     * @param it            The iterator from the executionChain object
     * @param classname     The class name
     * @param qNameAsString The QName in string form
     * @return SelfManagedDataManager handler
     */
    private SelfManagedDataManager deserialize_getHandlerFromExecutionChain(Iterator it,
                                                                            String classname,
                                                                            String qNameAsString) {
        SelfManagedDataManager handler_toreturn = null;

        try {
            while ((it.hasNext()) && (handler_toreturn == null)) {
                Handler handler = (Handler) it.next();

                if (handler instanceof Phase) {
                    handler_toreturn = deserialize_getHandlerFromExecutionChain(
                            ((Phase) handler).getHandlers().iterator(), classname, qNameAsString);
                } else if ((handler.getClass().getName().equals(classname))
                        && (handler.getName().equals(qNameAsString))) {
                    handler_toreturn = (SelfManagedDataManager) handler;
                }
            }
            return handler_toreturn;
        }
        catch (ClassCastException e) {
            // Doesn't seem likely to happen, but just in case...
            // A handler classname in the executionChain matched up with our parameter
            // classname, but the existing class in the executionChain is a different
            // implementation than the one we saved during serializeSelfManagedData.
            // NOTE: the exception gets absorbed!

            if (LoggingControl.debugLoggingAllowed && log.isTraceEnabled()) {
                log.trace(
                    "MessageContext:deserialize_getHandlerFromExecutionChain(): ClassCastException thrown: " +
                            e.getMessage(), e);
            }
            return null;
        }
    }


    /*
    * We don't need to create new instances of the handlers
    * since the executionChain is rebuilt after readExternal().
    * We just have to find them in the executionChain and
    * call each handler's deserializeSelfManagedData method.
    */
    private void deserializeSelfManagedData() throws IOException {
        try {
            for (int i = 0;
                 (selfManagedDataListHolder != null) && (i < selfManagedDataListHolder.size()); i++)
            {
                SelfManagedDataHolder selfManagedDataHolder =
                        (SelfManagedDataHolder) selfManagedDataListHolder.get(i);

                String classname = selfManagedDataHolder.getClassname();
                String qNameAsString = selfManagedDataHolder.getId();

                SelfManagedDataManager handler = deserialize_getHandlerFromExecutionChain(
                        executionChain.iterator(), classname, qNameAsString);

                if (handler == null) {
                    if (LoggingControl.debugLoggingAllowed && log.isTraceEnabled()) {
                        log.trace(getLogIDString() + ":deserializeSelfManagedData():  [" +
                                classname +
                                "]  was not found in the executionChain associated with the message context.");
                    }

                    throw new IOException("The class [" + classname +
                            "] was not found in the executionChain associated with the message context.");
                }

                ByteArrayInputStream handlerData =
                        new ByteArrayInputStream(selfManagedDataHolder.getData());

                // the handler implementing SelfManagedDataManager is responsible for repopulating
                // the SelfManagedData in the MessageContext (this)

                if (LoggingControl.debugLoggingAllowed && log.isTraceEnabled()) {
                    log.trace(getLogIDString() +
                            ":deserializeSelfManagedData(): calling handler [" + classname + "] [" +
                            qNameAsString + "]  deserializeSelfManagedData method");
                }

                handler.deserializeSelfManagedData(handlerData, this);
                handler.restoreTransientData(this);
            }
        }
        catch (IOException ioe) {
            if (LoggingControl.debugLoggingAllowed && log.isTraceEnabled()) {
                log.trace(getLogIDString() + ":deserializeSelfManagedData(): IOException thrown: " +
                        ioe.getMessage(), ioe);
            }
            throw ioe;
        }

    }

    /* ===============================================================
    * Externalizable support
    * ===============================================================
    */


    /**
     * Save the contents of this MessageContext instance.
     * <p/>
     * NOTE: Transient fields and static fields are not saved.
     * Also, objects that represent "static" data are
     * not saved, except for enough information to be
     * able to find matching objects when the message
     * context is re-constituted.
     *
     * @param out The stream to write the object contents to
     * @throws IOException
     */
    public void writeExternal(ObjectOutput out) throws IOException {
        String logCorrelationIDString = getLogIDString();

        if (LoggingControl.debugLoggingAllowed && log.isTraceEnabled()) {
            log.trace(logCorrelationIDString + ":writeExternal(): writing to output stream");
        }

        //---------------------------------------------------------
        // in order to handle future changes to the message
        // context definition, be sure to maintain the
        // object level identifiers
        //---------------------------------------------------------
        // serialization version ID
        out.writeLong(serialVersionUID);

        // revision ID
        out.writeInt(revisionID);

        //---------------------------------------------------------
        // various simple fields
        //---------------------------------------------------------

        // the type of execution flow for the message context
        out.writeInt(FLOW);

        // various flags
        out.writeBoolean(processingFault);
        out.writeBoolean(paused);
        out.writeBoolean(outputWritten);
        out.writeBoolean(newThreadRequired);
        out.writeBoolean(isSOAP11);
        out.writeBoolean(doingREST);
        out.writeBoolean(doingMTOM);
        out.writeBoolean(doingSwA);
        out.writeBoolean(responseWritten);
        out.writeBoolean(serverSide);

        out.writeLong(getLastTouchedTime());

        ObjectStateUtils.writeString(out, this.getLogCorrelationID(), "logCorrelationID");

        boolean persistWithOptimizedMTOM = (getProperty(MTOMConstants.ATTACHMENTS) != null);
        out.writeBoolean(persistWithOptimizedMTOM);

        //---------------------------------------------------------
        // message
        //---------------------------------------------------------

        // Just in case anything else is added here, notice that in the case
        // of MTOM, something is written to the stream in middle of the envelope
        // serialization logic below

        // make sure message attachments are handled

        if (envelope != null) {
            String msgClass = envelope.getClass().getName();

            ByteArrayOutputStream msgBuffer = new ByteArrayOutputStream();

            try {
                // use a non-destructive method on the soap message

                // We don't need to write to a separate byte array
                // unless we want to log the message
                ByteArrayOutputStream msgData = new ByteArrayOutputStream();

                OMOutputFormat outputFormat = new OMOutputFormat();

                outputFormat.setSOAP11(isSOAP11);

                if (persistWithOptimizedMTOM) {
                    outputFormat.setDoOptimize(true);

                    //Notice that we're writing this next bit out to the
                    //serialized stream and not the baos
                    out.writeUTF(outputFormat.getContentType());
                }

                // this will be expensive because it builds the OM tree
                envelope.serialize(msgData, outputFormat);

                msgBuffer.write(msgData.toByteArray(), 0, msgData.size());

                if (LoggingControl.debugLoggingAllowed && log.isTraceEnabled()) {
                    log.trace(logCorrelationIDString + ":writeExternal(): msg data [" + msgData +
                            "]");
                }

            }
            catch (Exception e) {
                if (LoggingControl.debugLoggingAllowed && log.isTraceEnabled()) {
                    log.trace(logCorrelationIDString +
                        ":writeExternal(): can not serialize the SOAP message ***Exception***  [" +
                        e.getClass().getName() + " : " + e.getMessage() + "]");
                }
            }

            //---------------------------------------------
            // get the character encoding for the message
            //---------------------------------------------
            String charSetEnc = (String) getProperty(MessageContext.CHARACTER_SET_ENCODING);

            if (charSetEnc == null) {
                OperationContext opContext = getOperationContext();
                if (opContext != null) {
                    charSetEnc =
                            (String) opContext.getProperty(MessageContext.CHARACTER_SET_ENCODING);
                }
            }

            if (charSetEnc == null) {
                charSetEnc = MessageContext.DEFAULT_CHAR_SET_ENCODING;
            }

            //---------------------------------------------
            // get the soap namespace uri
            //---------------------------------------------
            String namespaceURI = envelope.getNamespace().getNamespaceURI();

            // write out the following information, IN ORDER:
            //           the class name
            //           the active or empty flag
            //           the data length
            //           the data
            out.writeUTF(msgClass);

            int msgSize = msgBuffer.size();

            if (msgSize != 0) {
                out.writeBoolean(ObjectStateUtils.ACTIVE_OBJECT);
                out.writeUTF(charSetEnc);
                out.writeUTF(namespaceURI);
                out.writeInt(msgSize);
                out.write(msgBuffer.toByteArray());

                if (LoggingControl.debugLoggingAllowed && log.isTraceEnabled()) {
                    log.trace(logCorrelationIDString + ":writeExternal(): msg  charSetEnc=[" +
                            charSetEnc + "]  namespaceURI=[" + namespaceURI + "]  msgSize=[" +
                            msgSize + "]");
                }
            } else {
                // the envelope is null
                out.writeBoolean(ObjectStateUtils.EMPTY_OBJECT);

                if (LoggingControl.debugLoggingAllowed && log.isTraceEnabled()) {
                    log.trace(logCorrelationIDString + ":writeExternal(): msg  is Empty");
                }
            }

            // close out internal stream
            msgBuffer.close();
        } else {
            // the envelope is null
            out.writeUTF("MessageContext.envelope");
            out.writeBoolean(ObjectStateUtils.EMPTY_OBJECT);

            if (LoggingControl.debugLoggingAllowed && log.isTraceEnabled()) {
                log.trace(logCorrelationIDString + ":writeExternal(): msg  is Empty");
            }
        }

        //---------------------------------------------------------
        // ArrayList executionChain
        //     handler and phase related data
        //---------------------------------------------------------
        // The strategy is to save some metadata about each
        // member of the list and the order of the list.
        // Then when the message context is re-constituted,
        // try to match up with phases and handlers on the
        // engine.
        //
        // Non-null list:
        //    UTF          - description string
        //    boolean      - active flag
        //    int          - current handler index
        //    int          - current phase index
        //    int          - expected number of entries in the list
        //    objects      - MetaDataEntry object per list entry
        //                        last entry will be empty MetaDataEntry
        //                        with MetaDataEntry.LAST_ENTRY marker
        //    int          - adjusted number of entries in the list
        //                        includes the last empty entry
        //
        // Empty list:
        //    UTF          - description string
        //    boolean      - empty flag
        //---------------------------------------------------------
        String execChainDesc = logCorrelationIDString + ".executionChain";

        int listSize = 0;

        if (executionChain != null) {
            listSize = executionChain.size();
        }

        if (listSize > 0) {
            // start writing data to the output stream
            out.writeUTF(execChainDesc);
            out.writeBoolean(ObjectStateUtils.ACTIVE_OBJECT);
            out.writeInt(currentHandlerIndex);
            out.writeInt(currentPhaseIndex);
            out.writeInt(listSize);

            // put the metadata on each member of the list into a buffer

            // match the current index with the actual saved list
            int nextIndex = 0;

            Iterator i = executionChain.iterator();

            while (i.hasNext()) {
                Object obj = i.next();
                String objClass = obj.getClass().getName();
                // start the meta data entry for this object
                MetaDataEntry mdEntry = new MetaDataEntry();
                mdEntry.setClassName(objClass);

                // get the correct object-specific name
                String qnameAsString;

                if (obj instanceof Phase) {
                    Phase phaseObj = (Phase) obj;
                    qnameAsString = phaseObj.getName();

                    // add the list of handlers to the meta data
                    setupPhaseList(phaseObj, mdEntry);
                } else if (obj instanceof Handler) {
                    Handler handlerObj = (Handler) obj;
                    qnameAsString = handlerObj.getName();
                } else {
                    // TODO: will there be any other kinds of objects in the execution Chain?
                    qnameAsString = "NULL";
                }

                mdEntry.setQName(qnameAsString);

                // update the index for the entry in the chain

                if (LoggingControl.debugLoggingAllowed && log.isTraceEnabled()) {
                    log.trace(logCorrelationIDString +
                            ":writeExternal(): ***BEFORE OBJ WRITE*** executionChain entry class [" +
                            objClass + "] qname [" + qnameAsString + "]");
                }

                ObjectStateUtils.writeObject(out, mdEntry, logCorrelationIDString +
                        ".executionChain:entry class [" + objClass + "] qname [" + qnameAsString +
                        "]");

                // update the index so that the index
                // now indicates the next entry that
                // will be attempted
                nextIndex++;

                if (LoggingControl.debugLoggingAllowed && log.isTraceEnabled()) {
                    log.trace(logCorrelationIDString +
                            ":writeExternal(): ***AFTER OBJ WRITE*** executionChain entry class [" +
                            objClass + "] qname [" + qnameAsString + "]");
                }

            } // end while entries in execution chain

            // done with the entries in the execution chain
            // add the end-of-list marker
            MetaDataEntry lastEntry = new MetaDataEntry();
            lastEntry.setClassName(MetaDataEntry.END_OF_LIST);

            ObjectStateUtils.writeObject(out, lastEntry,
                                         logCorrelationIDString + ".executionChain:  last entry ");
            nextIndex++;

            // nextIndex also gives us the number of entries
            // that were actually saved as opposed to the
            // number of entries in the executionChain
            out.writeInt(nextIndex);

        } else {
            // general case: handle "null" or "empty"

            out.writeUTF(execChainDesc);
            out.writeBoolean(ObjectStateUtils.EMPTY_OBJECT);

            if (LoggingControl.debugLoggingAllowed && log.isTraceEnabled()) {
                log.trace(logCorrelationIDString + ":writeExternal(): executionChain is NULL");
            }
        }

        //---------------------------------------------------------
        // LinkedList inboundExecutedPhases
        //---------------------------------------------------------
        // The strategy is to save some metadata about each
        // member of the list and the order of the list.
        // Then when the message context is re-constituted,
        // try to match up with phases and handlers on the
        // engine.
        //
        // Non-null list:
        //    UTF          - description string
        //    boolean      - active flag
        //    int          - expected number of entries in the list
        //    objects      - MetaDataEntry object per list entry
        //                        last entry will be empty MetaDataEntry
        //                        with MetaDataEntry.LAST_ENTRY marker
        //    int          - adjusted number of entries in the list
        //                        includes the last empty entry
        //
        // Empty list:
        //    UTF          - description string
        //    boolean      - empty flag
        //---------------------------------------------------------
        String inExecListDesc = logCorrelationIDString + ".inboundExecutedPhases";

        int inExecListSize = 0;

        if (inboundExecutedPhases != null) {
            inExecListSize = inboundExecutedPhases.size();
        }

        if (inExecListSize > 0) {
            // start writing data to the output stream
            out.writeUTF(inExecListDesc);
            out.writeBoolean(ObjectStateUtils.ACTIVE_OBJECT);
            out.writeInt(inExecListSize);

            // put the metadata on each member of the list into a buffer

            int inExecNextIndex = 0;

            Iterator inIterator = inboundExecutedPhases.iterator();

            while (inIterator.hasNext()) {
                Object inObj = inIterator.next();
                String inObjClass = inObj.getClass().getName();
                // start the meta data entry for this object
                MetaDataEntry inMdEntry = new MetaDataEntry();
                inMdEntry.setClassName(inObjClass);

                // get the correct object-specific name
                String inQnameAsString;

                if (inObj instanceof Phase) {
                    Phase inPhaseObj = (Phase) inObj;
                    inQnameAsString = inPhaseObj.getName();

                    // add the list of handlers to the meta data
                    setupPhaseList(inPhaseObj, inMdEntry);
                } else if (inObj instanceof Handler) {
                    Handler inHandlerObj = (Handler) inObj;
                    inQnameAsString = inHandlerObj.getName();
                } else {
                    // TODO: will there be any other kinds of objects in the list
                    inQnameAsString = "NULL";
                }

                inMdEntry.setQName(inQnameAsString);


                if (LoggingControl.debugLoggingAllowed && log.isTraceEnabled()) {
                    log.trace(logCorrelationIDString +
                            ":writeExternal(): ***BEFORE Inbound Executed List OBJ WRITE*** inboundExecutedPhases entry class [" +
                            inObjClass + "] qname [" + inQnameAsString + "]");
                }

                ObjectStateUtils.writeObject(out, inMdEntry, logCorrelationIDString +
                        ".inboundExecutedPhases:entry class [" + inObjClass + "] qname [" +
                        inQnameAsString + "]");

                // update the index so that the index
                // now indicates the next entry that
                // will be attempted
                inExecNextIndex++;

                if (LoggingControl.debugLoggingAllowed && log.isTraceEnabled()) {
                    log.trace(logCorrelationIDString + ":writeExternal(): " +
                            "***AFTER Inbound Executed List OBJ WRITE*** " +
                            "inboundExecutedPhases entry class [" + inObjClass + "] " +
                            "qname [" + inQnameAsString + "]");
                }
            } // end while entries in execution chain

            // done with the entries in the execution chain
            // add the end-of-list marker
            MetaDataEntry inLastEntry = new MetaDataEntry();
            inLastEntry.setClassName(MetaDataEntry.END_OF_LIST);

            ObjectStateUtils.writeObject(out, inLastEntry, logCorrelationIDString +
                    ".inboundExecutedPhases:  last entry ");
            inExecNextIndex++;

            // inExecNextIndex also gives us the number of entries
            // that were actually saved as opposed to the
            // number of entries in the inboundExecutedPhases
            out.writeInt(inExecNextIndex);

        } else {
            // general case: handle "null" or "empty"

            out.writeUTF(inExecListDesc);
            out.writeBoolean(ObjectStateUtils.EMPTY_OBJECT);

            if (LoggingControl.debugLoggingAllowed && log.isTraceEnabled()) {
                log.trace(
                        logCorrelationIDString + ":writeExternal(): inboundExecutedPhases is NULL");
            }
        }

        //---------------------------------------------------------
        // LinkedList outboundExecutedPhases
        //---------------------------------------------------------
        // The strategy is to save some metadata about each
        // member of the list and the order of the list.
        // Then when the message context is re-constituted,
        // try to match up with phases and handlers on the
        // engine.
        //
        // Non-null list:
        //    UTF          - description string
        //    boolean      - active flag
        //    int          - expected number of entries in the list
        //    objects      - MetaDataEntry object per list entry
        //                        last entry will be empty MetaDataEntry
        //                        with MetaDataEntry.LAST_ENTRY marker
        //    int          - adjusted number of entries in the list
        //                        includes the last empty entry
        //
        // Empty list:
        //    UTF          - description string
        //    boolean      - empty flag
        //---------------------------------------------------------
        String outExecListDesc = logCorrelationIDString + ".outboundExecutedPhases";

        int outExecListSize = 0;

        if (outboundExecutedPhases != null) {
            outExecListSize = outboundExecutedPhases.size();
        }

        if (outExecListSize > 0) {
            // start writing data to the output stream
            out.writeUTF(outExecListDesc);
            out.writeBoolean(ObjectStateUtils.ACTIVE_OBJECT);
            out.writeInt(outExecListSize);

            // put the metadata on each member of the list into a buffer

            int outExecNextIndex = 0;

            Iterator outIterator = outboundExecutedPhases.iterator();

            while (outIterator.hasNext()) {
                Object outObj = outIterator.next();
                String outObjClass = outObj.getClass().getName();
                // start the meta data entry for this object
                MetaDataEntry outMdEntry = new MetaDataEntry();
                outMdEntry.setClassName(outObjClass);

                // get the correct object-specific name
                String outQnameAsString;

                if (outObj instanceof Phase) {
                    Phase outPhaseObj = (Phase) outObj;
                    outQnameAsString = outPhaseObj.getName();

                    // add the list of handlers to the meta data
                    setupPhaseList(outPhaseObj, outMdEntry);
                } else if (outObj instanceof Handler) {
                    Handler outHandlerObj = (Handler) outObj;
                    outQnameAsString = outHandlerObj.getName();
                } else {
                    // TODO: will there be any other kinds of objects in the list
                    outQnameAsString = "NULL";
                }

                outMdEntry.setQName(outQnameAsString);

                if (LoggingControl.debugLoggingAllowed && log.isTraceEnabled()) {
                    log.trace(logCorrelationIDString +
                            ":writeExternal(): ***BEFORE Outbound Executed List OBJ WRITE*** outboundExecutedPhases entry class [" +
                            outObjClass + "] qname [" + outQnameAsString + "]");
                }

                ObjectStateUtils.writeObject(out, outMdEntry, logCorrelationIDString +
                        ".outboundExecutedPhases:entry class [" + outObjClass + "] qname [" +
                        outQnameAsString + "]");

                // update the index so that the index
                // now indicates the next entry that
                // will be attempted
                outExecNextIndex++;

                if (LoggingControl.debugLoggingAllowed && log.isTraceEnabled()) {
                    log.trace(logCorrelationIDString +
                            ":writeExternal(): ***AFTER Outbound Executed List OBJ WRITE*** outboundExecutedPhases entry class [" +
                            outObjClass + "] qname [" + outQnameAsString + "]");
                }

            } // end while entries

            // done with the entries
            // add the end-of-list marker
            MetaDataEntry outLastEntry = new MetaDataEntry();
            outLastEntry.setClassName(MetaDataEntry.END_OF_LIST);

            ObjectStateUtils.writeObject(out, outLastEntry, logCorrelationIDString +
                    ".outboundExecutedPhases:  last entry ");
            outExecNextIndex++;

            // outExecNextIndex also gives us the number of entries
            // that were actually saved as opposed to the
            // number of entries in the outboundExecutedPhases
            out.writeInt(outExecNextIndex);

        } else {
            // general case: handle "null" or "empty"

            out.writeUTF(outExecListDesc);
            out.writeBoolean(ObjectStateUtils.EMPTY_OBJECT);

            if (LoggingControl.debugLoggingAllowed && log.isTraceEnabled()) {
                log.trace(logCorrelationIDString +
                        ":writeExternal(): outboundExecutedPhases is NULL");
            }
        }

        //---------------------------------------------------------
        // options
        //---------------------------------------------------------
        // before saving the Options, make sure there is a message ID
        String tmpID = getMessageID();
        if (tmpID == null) {
            // get an id to use when restoring this object
            tmpID = UUIDGenerator.getUUID();
            setMessageID(tmpID);
        }

        if (LoggingControl.debugLoggingAllowed && log.isTraceEnabled()) {
            log.trace(logCorrelationIDString + ":writeExternal():   message ID [" + tmpID + "]");
        }

        ObjectStateUtils.writeObject(out, options, logCorrelationIDString + ".options   for  [" +
                options.getLogCorrelationIDString() + "]");

        //---------------------------------------------------------
        // operation
        //---------------------------------------------------------
        // axis operation
        //---------------------------------------------------------
        String axisOpMarker = logCorrelationIDString + ".axisOperation";
        ObjectStateUtils.writeString(out, axisOpMarker, axisOpMarker);

        if (axisOperation == null) {
            out.writeBoolean(ObjectStateUtils.EMPTY_OBJECT);
        } else {
            // TODO: may need to include the meta data for the axis service that is
            //       the parent of the axis operation

            out.writeBoolean(ObjectStateUtils.ACTIVE_OBJECT);

            // make sure the axis operation has a name associated with it
            QName aoTmpQName = axisOperation.getName();

            if (aoTmpQName == null) {
                aoTmpQName = new QName(ObjectStateUtils.EMPTY_MARKER);
                axisOperation.setName(aoTmpQName);
            }

            metaAxisOperation = new MetaDataEntry(axisOperation.getClass().getName(),
                                                  axisOperation.getName().toString());
            ObjectStateUtils.writeObject(out, metaAxisOperation,
                                         logCorrelationIDString + ".metaAxisOperation");
        }

        //---------------------------------------------------------
        // operation context
        //---------------------------------------------------------
        // The OperationContext has pointers to MessageContext objects.
        // In order to avoid having multiple copies of the object graph
        // being saved at different points in the serialization,
        // it is important to isolate this message context object.
        String oc_desc = logCorrelationIDString + ".operationContext";
        if (operationContext != null) {
            operationContext.isolateMessageContext(this);
            oc_desc = oc_desc + "  for  [" + operationContext.getLogCorrelationIDString() + "]";
        }

        // NOTE: expect this to be the parent of the message context
        ObjectStateUtils.writeObject(out, operationContext, oc_desc);

        //---------------------------------------------------------
        // service
        //---------------------------------------------------------
        // axis service
        //-------------------------
        // this is expected to be the parent of the axis operation object
        String axisServMarker = logCorrelationIDString + ".axisService";
        ObjectStateUtils.writeString(out, axisServMarker, axisServMarker);

        if (axisService == null) {
            out.writeBoolean(ObjectStateUtils.EMPTY_OBJECT);
        } else {
            out.writeBoolean(ObjectStateUtils.ACTIVE_OBJECT);
            metaAxisService =
                    new MetaDataEntry(axisService.getClass().getName(), axisService.getName());
            ObjectStateUtils
                    .writeObject(out, metaAxisService, logCorrelationIDString + ".metaAxisService");
        }

        //-------------------------
        // serviceContextID string
        //-------------------------
        ObjectStateUtils
                .writeString(out, serviceContextID, logCorrelationIDString + ".serviceContextID");

        //-------------------------
        // serviceContext
        //-------------------------
        // is this the same as the parent of the OperationContext?
        boolean isParent = false;

        if (operationContext != null) {
            ServiceContext opctxParent = operationContext.getServiceContext();

            if (serviceContext != null) {
                if (serviceContext.equals(opctxParent)) {
                    // the ServiceContext is the parent of the OperationContext
                    isParent = true;
                }
            }
        }

        String servCtxMarker = logCorrelationIDString + ".serviceContext";
        ObjectStateUtils.writeString(out, servCtxMarker, servCtxMarker);

        if (serviceContext == null) {
            out.writeBoolean(ObjectStateUtils.EMPTY_OBJECT);
        } else {
            out.writeBoolean(ObjectStateUtils.ACTIVE_OBJECT);
            out.writeBoolean(isParent);

            // only write out the object if it is not the parent
            if (!isParent) {
                ObjectStateUtils.writeObject(out, serviceContext,
                                             logCorrelationIDString + ".serviceContext");
            }
        }

        //---------------------------------------------------------
        // axisServiceGroup
        //---------------------------------------------------------

        String axisServGrpMarker = logCorrelationIDString + ".axisServiceGroup";
        ObjectStateUtils.writeString(out, axisServGrpMarker, axisServGrpMarker);

        if (axisServiceGroup == null) {
            out.writeBoolean(ObjectStateUtils.EMPTY_OBJECT);
        } else {
            out.writeBoolean(ObjectStateUtils.ACTIVE_OBJECT);
            metaAxisServiceGroup = new MetaDataEntry(axisServiceGroup.getClass().getName(),
                                                     axisServiceGroup.getServiceGroupName());
            ObjectStateUtils.writeObject(out, metaAxisServiceGroup,
                                         logCorrelationIDString + ".metaAxisServiceGroup");
        }

        //-----------------------------
        // serviceGroupContextId string
        //-----------------------------
        ObjectStateUtils.writeString(out, serviceGroupContextId,
                                     logCorrelationIDString + ".serviceGroupContextId");

        //-------------------------
        // serviceGroupContext
        //-------------------------

        // is this the same as the parent of the ServiceContext?
        isParent = false;

        if (serviceContext != null) {
            ServiceGroupContext srvgrpctxParent = (ServiceGroupContext) serviceContext.getParent();

            if (serviceGroupContext != null) {
                if (serviceGroupContext.equals(srvgrpctxParent)) {
                    // the ServiceGroupContext is the parent of the ServiceContext
                    isParent = true;
                }
            }
        }

        String servGrpCtxMarker = logCorrelationIDString + ".serviceGroupContext";
        ObjectStateUtils.writeString(out, servGrpCtxMarker, servGrpCtxMarker);

        if (serviceGroupContext == null) {
            out.writeBoolean(ObjectStateUtils.EMPTY_OBJECT);
        } else {
            out.writeBoolean(ObjectStateUtils.ACTIVE_OBJECT);
            out.writeBoolean(isParent);

            // only write out the object if it is not the parent
            if (!isParent) {
                ObjectStateUtils.writeObject(out, serviceGroupContext,
                                             logCorrelationIDString + ".serviceGroupContext");
            }
        }

        //---------------------------------------------------------
        // axis message
        //---------------------------------------------------------
        String axisMsgMarker = logCorrelationIDString + ".axisMessage";
        ObjectStateUtils.writeString(out, axisMsgMarker, axisMsgMarker);

        if (axisMessage == null) {
            out.writeBoolean(ObjectStateUtils.EMPTY_OBJECT);
        } else {
            // This AxisMessage is expected to belong to the AxisOperation
            // that has already been recorded for this MessageContext.
            // If an AxisMessage associated with this Messagecontext is
            // associated with a different AxisOperation, then more
            // meta information would need to be saved

            out.writeBoolean(ObjectStateUtils.ACTIVE_OBJECT);

            // make sure the axis message has a name associated with it
            String amTmpName = axisMessage.getName();

            if (amTmpName == null) {
                amTmpName = ObjectStateUtils.EMPTY_MARKER;
                axisMessage.setName(amTmpName);
            }

            // get the element name if there is one
            QName amTmpElementQName = axisMessage.getElementQName();
            String amTmpElemQNameString = null;

            if (amTmpElementQName != null) {
                amTmpElemQNameString = amTmpElementQName.toString();
            }

            metaAxisMessage = new MetaDataEntry(axisMessage.getClass().getName(),
                                                axisMessage.getName(), amTmpElemQNameString);

            ObjectStateUtils
                    .writeObject(out, metaAxisMessage, logCorrelationIDString + ".metaAxisMessage");
        }

        //---------------------------------------------------------
        // configuration context
        //---------------------------------------------------------

        // NOTE: Currently, there does not seem to be any
        //       runtime data important to this message context
        //       in the configuration context.
        //       if so, then need to save that runtime data and reconcile
        //       it with the configuration context on the system when
        //       this message context object is restored

        //---------------------------------------------------------
        // session context
        //---------------------------------------------------------
        ObjectStateUtils
                .writeObject(out, sessionContext, logCorrelationIDString + ".sessionContext");

        //---------------------------------------------------------
        // transport
        //---------------------------------------------------------

        //------------------------------
        // incomingTransportName string
        //------------------------------
        ObjectStateUtils.writeString(out, incomingTransportName,
                                     logCorrelationIDString + ".incomingTransportName");

        // TransportInDescription transportIn
        if (transportIn != null) {
            metaTransportIn = new MetaDataEntry(null, transportIn.getName());
        } else {
            metaTransportIn = null;
        }
        ObjectStateUtils.writeObject(out, metaTransportIn, logCorrelationIDString + ".transportIn");

        // TransportOutDescription transportOut
        if (transportOut != null) {
            metaTransportOut = new MetaDataEntry(null, transportOut.getName());
        } else {
            metaTransportOut = null;
        }
        ObjectStateUtils
                .writeObject(out, metaTransportOut, logCorrelationIDString + ".transportOut");

        //---------------------------------------------------------
        // properties
        //---------------------------------------------------------
        Map tmpMap = getProperties();

        HashMap tmpHashMap = null;

        if ((tmpMap != null) && (!tmpMap.isEmpty())) {
            tmpHashMap = new HashMap(tmpMap);
        }

        ObjectStateUtils.writeHashMap(out, tmpHashMap, logCorrelationIDString + ".properties");

        //---------------------------------------------------------
        // special data
        //---------------------------------------------------------

        String selfManagedDataMarker = logCorrelationIDString + ".selfManagedData";
        ObjectStateUtils.writeString(out, selfManagedDataMarker, selfManagedDataMarker);

        // save the data, which the handlers themselves will serialize
        //ByteArrayOutputStream baos_fromSelfManagedData = serializeSelfManagedData();
        serializeSelfManagedData(out);

        //---------------------------------------------------------
        // done
        //---------------------------------------------------------

        if (LoggingControl.debugLoggingAllowed && log.isTraceEnabled()) {
            log.trace(logCorrelationIDString +
                    ":writeExternal(): completed writing to output stream for " +
                    logCorrelationIDString);
        }

    }


    /**
     * Restore the contents of the MessageContext that was
     * previously saved.
     * <p/>
     * NOTE: The field data must read back in the same order and type
     * as it was written.  Some data will need to be validated when
     * resurrected.
     *
     * @param in The stream to read the object contents from
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        // set the flag to indicate that the message context is being
        // reconstituted and will need to have certain object references
        // to be reconciled with the current engine setup
        needsToBeReconciled = true;

        // trace point
        if (LoggingControl.debugLoggingAllowed && log.isTraceEnabled()) {
            log.trace(myClassName + ":readExternal():  BEGIN  bytes available in stream [" +
                    in.available() + "]  ");
        }

        //---------------------------------------------------------
        // object level identifiers
        //---------------------------------------------------------

        // serialization version ID
        long suid = in.readLong();

        // revision ID
        int revID = in.readInt();

        // make sure the object data is in a version we can handle
        if (suid != serialVersionUID) {
            throw new ClassNotFoundException(ObjectStateUtils.UNSUPPORTED_SUID);
        }

        // make sure the object data is in a revision level we can handle
        if (revID != REVISION_1) {
            throw new ClassNotFoundException(ObjectStateUtils.UNSUPPORTED_REVID);
        }

        //---------------------------------------------------------
        // various simple fields
        //---------------------------------------------------------

        // the type of execution flow for the message context
        FLOW = in.readInt();

        // various flags
        processingFault = in.readBoolean();
        paused = in.readBoolean();
        outputWritten = in.readBoolean();
        newThreadRequired = in.readBoolean();
        isSOAP11 = in.readBoolean();
        doingREST = in.readBoolean();
        doingMTOM = in.readBoolean();
        doingSwA = in.readBoolean();
        responseWritten = in.readBoolean();
        serverSide = in.readBoolean();

        long time = in.readLong();
        setLastTouchedTime(time);

        logCorrelationID = ObjectStateUtils.readString(in, "logCorrelationID");
        logCorrelationIDString = "[MessageContext: logID=" + logCorrelationID + "]";

        // trace point
        if (LoggingControl.debugLoggingAllowed && log.isTraceEnabled()) {
            log.trace(myClassName + ":readExternal():  reading the input stream for  " +
                    logCorrelationIDString);
        }

        boolean persistedWithOptimizedMTOM = in.readBoolean();

        String contentType = null;
        if (persistedWithOptimizedMTOM) {
            contentType = in.readUTF();
        }

        //---------------------------------------------------------
        // message
        //---------------------------------------------------------

        in.readUTF();
        boolean gotMsg = in.readBoolean();

        if (gotMsg == ObjectStateUtils.ACTIVE_OBJECT) {
            String charSetEnc = in.readUTF();
            String namespaceURI = in.readUTF();

            int msgSize = in.readInt();
            byte[] buffer = new byte[msgSize];

            int bytesRead = 0;
            int numberOfBytesLastRead;

            while (bytesRead < msgSize) {
                numberOfBytesLastRead = in.read(buffer, bytesRead, msgSize - bytesRead);

                if (numberOfBytesLastRead == -1) {
                    // TODO: What should we do if the reconstitution fails?
                    // For now, log the event
                    if (LoggingControl.debugLoggingAllowed && log.isTraceEnabled()) {
                        log.trace(logCorrelationIDString +
                                ":readExternal(): ***WARNING*** unexpected end to message   bytesRead [" +
                                bytesRead + "]    msgSize [" + msgSize + "]");
                    }
                    break;
                }

                bytesRead += numberOfBytesLastRead;
            }


            String tmpMsg = new String(buffer);

            if (LoggingControl.debugLoggingAllowed && log.isTraceEnabled()) {
                log.trace(logCorrelationIDString + ":readExternal(): msg  charSetEnc=[" +
                        charSetEnc + "]  namespaceURI=[" + namespaceURI + "]  msgSize=[" + msgSize +
                        "]   bytesRead [" + bytesRead + "]");
                log.trace(logCorrelationIDString + ":readExternal(): msg  [" + tmpMsg + "]");
            }

            ByteArrayInputStream msgBuffer;

            if (bytesRead > 0) {
                msgBuffer = new ByteArrayInputStream(buffer);

                // convert what was saved into the soap envelope

                XMLStreamReader xmlreader = null;

                try {
                    if (persistedWithOptimizedMTOM) {
                        boolean isSOAP = true;
                        StAXBuilder builder = BuilderUtil
                                .getAttachmentsBuilder(this, msgBuffer, contentType, isSOAP);
                        envelope = (SOAPEnvelope) builder.getDocumentElement();
                        // build the OM in order to free the input stream
                        envelope.buildWithAttachments();
                    } else {
                        xmlreader = StAXUtils.createXMLStreamReader(msgBuffer, charSetEnc);
                        StAXBuilder builder = new StAXSOAPModelBuilder(xmlreader, namespaceURI);
                        envelope = (SOAPEnvelope) builder.getDocumentElement();
                        // build the OM in order to free the input stream
                        envelope.build();
                    }
                }
                catch (Exception ex) {
                    // TODO: what to do if can't get the XML stream reader
                    // For now, log the event
                    log.error(logCorrelationIDString +
                            ":readExternal(): Error when deserializing persisted envelope: [" +
                            ex.getClass().getName() + " : " + ex.getLocalizedMessage() + "]", ex);
                    envelope = null;
                }

                if (xmlreader != null) {
                    try {
                        xmlreader.close();
                    } catch (Exception xmlex) {
                        // Can't close down the xml stream reader
                        log.error(logCorrelationIDString+
                                ":readExternal(): Error when closing XMLStreamReader for envelope: ["
                                + xmlex.getClass().getName() + " : " + xmlex.getLocalizedMessage() + "]", xmlex);
                    }
                }

                msgBuffer.close();
            } else {
                // no message
                envelope = null;

                if (LoggingControl.debugLoggingAllowed && log.isTraceEnabled()) {
                    log.trace(logCorrelationIDString +
                            ":readExternal(): no message from the input stream");
                }
            }

        } else {
            // no message
            envelope = null;

            if (LoggingControl.debugLoggingAllowed && log.isTraceEnabled()) {
                log.trace(logCorrelationIDString + ":readExternal(): no message present");
            }
        }

        //---------------------------------------------------------
        // ArrayList executionChain
        //     handler and phase related data
        //---------------------------------------------------------
        // Restore the metadata about each member of the list
        // and the order of the list.
        // This metadata will be used to match up with phases
        // and handlers on the engine.
        //
        // Non-null list:
        //    UTF          - description string
        //    boolean      - active flag
        //    int          - current handler index
        //    int          - current phase index
        //    int          - expected number of entries in the list
        //                        not including the last entry marker
        //    objects      - MetaDataEntry object per list entry
        //                        last entry will be empty MetaDataEntry
        //                        with MetaDataEntry.LAST_ENTRY marker
        //    int          - adjusted number of entries in the list
        //                        includes the last empty entry
        //
        // Empty list:
        //    UTF          - description string
        //    boolean      - empty flag
        //---------------------------------------------------------

        // the local chain is not enabled until the
        // list has been reconstituted
        executionChain = null;
        currentHandlerIndex = -1;
        currentPhaseIndex = 0;
        metaExecutionChain = null;

        in.readUTF();
        boolean gotChain = in.readBoolean();

        if (gotChain == ObjectStateUtils.ACTIVE_OBJECT) {
            metaHandlerIndex = in.readInt();
            metaPhaseIndex = in.readInt();

            int expectedNumberEntries = in.readInt();

            if (LoggingControl.debugLoggingAllowed && log.isTraceEnabled()) {
                log.trace(logCorrelationIDString +
                        ":readExternal(): execution chain:  expected number of entries [" +
                        expectedNumberEntries + "]");
            }

            // setup the list
            metaExecutionChain = new ArrayList();

            // process the objects
            boolean keepGoing = true;
            int count = 0;

            while (keepGoing) {
                // stop when we get to the end-of-list marker

                // get the object
                Object tmpObj = ObjectStateUtils
                        .readObject(in, "MessageContext.metaExecutionChain MetaDataEntry");

                count++;

                MetaDataEntry mdObj = (MetaDataEntry) tmpObj;

                // get the class name, then add it to the list
                String tmpClassNameStr;
                String tmpQNameAsStr;

                if (mdObj != null) {
                    tmpClassNameStr = mdObj.getClassName();

                    if (tmpClassNameStr.equalsIgnoreCase(MetaDataEntry.END_OF_LIST)) {
                        // this is the last entry
                        keepGoing = false;
                    } else {
                        // add the entry to the meta data list
                        metaExecutionChain.add(mdObj);

                        tmpQNameAsStr = mdObj.getQNameAsString();

                        if (LoggingControl.debugLoggingAllowed && log.isTraceEnabled()) {
                            String tmpHasList = mdObj.isListEmpty() ? "no children" : "has children";

                            log.trace(logCorrelationIDString +
                                    ":readExternal(): meta data class [" + tmpClassNameStr +
                                    "] qname [" + tmpQNameAsStr + "]  index [" + count + "]   [" +
                                    tmpHasList + "]");
                        }
                    }
                } else {
                    // some error occurred
                    keepGoing = false;
                }

            } // end while keep going

            int adjustedNumberEntries = in.readInt();

            if (LoggingControl.debugLoggingAllowed && log.isTraceEnabled()) {
                log.trace(logCorrelationIDString +
                        ":readExternal(): adjusted number of entries ExecutionChain [" +
                        adjustedNumberEntries + "]    ");
            }
        }

        if ((metaExecutionChain == null) || (metaExecutionChain.isEmpty())) {
            if (LoggingControl.debugLoggingAllowed && log.isTraceEnabled()) {
                log.trace(logCorrelationIDString +
                        ":readExternal(): meta data for Execution Chain is NULL");
            }
        }

        //---------------------------------------------------------
        // LinkedList inboundExecutedPhases
        //---------------------------------------------------------
        // Restore the metadata about each member of the list
        // and the order of the list.
        // This metadata will be used to match up with phases
        // and handlers on the engine.
        //
        // Non-null list:
        //    UTF          - description string
        //    boolean      - active flag
        //    int          - expected number of entries in the list
        //                        not including the last entry marker
        //    objects      - MetaDataEntry object per list entry
        //                        last entry will be empty MetaDataEntry
        //                        with MetaDataEntry.LAST_ENTRY marker
        //    int          - adjusted number of entries in the list
        //                        includes the last empty entry
        //
        // Empty list:
        //    UTF          - description string
        //    boolean      - empty flag
        //---------------------------------------------------------

        // the local chain is not enabled until the
        // list has been reconstituted
        inboundExecutedPhases = null;
        metaInboundExecuted = null;

        in.readUTF();
        boolean gotInExecList = in.readBoolean();

        if (gotInExecList == ObjectStateUtils.ACTIVE_OBJECT) {
            int expectedNumberInExecList = in.readInt();

            if (LoggingControl.debugLoggingAllowed && log.isTraceEnabled()) {
                log.trace(logCorrelationIDString +
                        ":readExternal(): inbound executed phases:  expected number of entries [" +
                        expectedNumberInExecList + "]");
            }

            // setup the list
            metaInboundExecuted = new LinkedList();

            // process the objects
            boolean keepGoing = true;
            int count = 0;

            while (keepGoing) {
                // stop when we get to the end-of-list marker

                // get the object
                Object tmpObj = ObjectStateUtils
                        .readObject(in, "MessageContext.metaInboundExecuted MetaDataEntry");

                count++;

                MetaDataEntry mdObj = (MetaDataEntry) tmpObj;

                // get the class name, then add it to the list
                String tmpClassNameStr;
                String tmpQNameAsStr;
                String tmpHasList = "no list";

                if (mdObj != null) {
                    tmpClassNameStr = mdObj.getClassName();

                    if (tmpClassNameStr.equalsIgnoreCase(MetaDataEntry.END_OF_LIST)) {
                        // this is the last entry
                        keepGoing = false;
                    } else {
                        // add the entry to the meta data list
                        metaInboundExecuted.add(mdObj);

                        tmpQNameAsStr = mdObj.getQNameAsString();

                        if (!mdObj.isListEmpty()) {
                            tmpHasList = "has list";
                        }

                        if (LoggingControl.debugLoggingAllowed && log.isTraceEnabled()) {
                            log.trace(logCorrelationIDString +
                                    ":readExternal(): meta data class [" + tmpClassNameStr +
                                    "] qname [" + tmpQNameAsStr + "]  index [" + count + "]   [" +
                                    tmpHasList + "]");
                        }
                    }
                } else {
                    // some error occurred
                    keepGoing = false;
                }

            } // end while keep going

            int adjustedNumberInExecList = in.readInt();

            if (LoggingControl.debugLoggingAllowed && log.isTraceEnabled()) {
                log.trace(logCorrelationIDString +
                        ":readExternal(): adjusted number of entries InboundExecutedPhases [" +
                        adjustedNumberInExecList + "]    ");
            }
        }

        if ((metaInboundExecuted == null) || (metaInboundExecuted.isEmpty())) {
            if (LoggingControl.debugLoggingAllowed && log.isTraceEnabled()) {
                log.trace(logCorrelationIDString +
                        ":readExternal(): meta data for InboundExecutedPhases list is NULL");
            }
        }

        //---------------------------------------------------------
        // LinkedList outboundExecutedPhases
        //---------------------------------------------------------
        // Restore the metadata about each member of the list
        // and the order of the list.
        // This metadata will be used to match up with phases
        // and handlers on the engine.
        //
        // Non-null list:
        //    UTF          - description string
        //    boolean      - active flag
        //    int          - expected number of entries in the list
        //                        not including the last entry marker
        //    objects      - MetaDataEntry object per list entry
        //                        last entry will be empty MetaDataEntry
        //                        with MetaDataEntry.LAST_ENTRY marker
        //    int          - adjusted number of entries in the list
        //                        includes the last empty entry
        //
        // Empty list:
        //    UTF          - description string
        //    boolean      - empty flag
        //---------------------------------------------------------

        // the local chain is not enabled until the
        // list has been reconstituted
        outboundExecutedPhases = null;
        metaOutboundExecuted = null;

        in.readUTF();
        boolean gotOutExecList = in.readBoolean();

        if (gotOutExecList == ObjectStateUtils.ACTIVE_OBJECT) {
            int expectedNumberOutExecList = in.readInt();

            if (LoggingControl.debugLoggingAllowed && log.isTraceEnabled()) {
                log.trace(logCorrelationIDString +
                        ":readExternal(): outbound executed phases:  expected number of entries [" +
                        expectedNumberOutExecList + "]");
            }

            // setup the list
            metaOutboundExecuted = new LinkedList();

            // process the objects
            boolean keepGoing = true;
            int count = 0;

            while (keepGoing) {
                // stop when we get to the end-of-list marker

                // get the object
                Object tmpObj = ObjectStateUtils
                        .readObject(in, "MessageContext.metaOutboundExecuted MetaDataEntry");

                count++;

                MetaDataEntry mdObj = (MetaDataEntry) tmpObj;

                // get the class name, then add it to the list
                String tmpClassNameStr;
                String tmpQNameAsStr;
                String tmpHasList = "no list";

                if (mdObj != null) {
                    tmpClassNameStr = mdObj.getClassName();

                    if (tmpClassNameStr.equalsIgnoreCase(MetaDataEntry.END_OF_LIST)) {
                        // this is the last entry
                        keepGoing = false;
                    } else {
                        // add the entry to the meta data list
                        metaOutboundExecuted.add(mdObj);

                        tmpQNameAsStr = mdObj.getQNameAsString();

                        if (!mdObj.isListEmpty()) {
                            tmpHasList = "has list";
                        }

                        if (LoggingControl.debugLoggingAllowed && log.isTraceEnabled()) {
                            log.trace(logCorrelationIDString +
                                    ":readExternal(): OutboundExecutedPhases: meta data class [" +
                                    tmpClassNameStr + "] qname [" + tmpQNameAsStr + "]  index [" +
                                    count + "]   [" + tmpHasList + "]");
                        }
                    }
                } else {
                    // some error occurred
                    keepGoing = false;
                }

            } // end while keep going

            int adjustedNumberOutExecList = in.readInt();

            if (LoggingControl.debugLoggingAllowed && log.isTraceEnabled()) {
                log.trace(logCorrelationIDString +
                        ":readExternal(): adjusted number of entries OutboundExecutedPhases [" +
                        adjustedNumberOutExecList + "]    ");
            }
        }

        if ((metaOutboundExecuted == null) || (metaOutboundExecuted.isEmpty())) {
            if (LoggingControl.debugLoggingAllowed && log.isTraceEnabled()) {
                log.trace(logCorrelationIDString +
                        ":readExternal(): meta data for OutboundExecutedPhases list is NULL");
            }
        }

        //---------------------------------------------------------
        // options
        //---------------------------------------------------------

        options = (Options) ObjectStateUtils.readObject(in, "MessageContext.options");

        if (options != null) {
            if (LoggingControl.debugLoggingAllowed && log.isTraceEnabled()) {
                log.trace(logCorrelationIDString + ":readExternal(): restored Options [" +
                        options.getLogCorrelationIDString() + "]");
            }
        }

        //---------------------------------------------------------
        // operation
        //---------------------------------------------------------

        // axisOperation is not usable until the meta data has been reconciled
        axisOperation = null;

        ObjectStateUtils.readString(in, "MessageContext.axisOperation");

        boolean metaAxisOperationIsActive = in.readBoolean();

        if (metaAxisOperationIsActive == ObjectStateUtils.ACTIVE_OBJECT) {
            metaAxisOperation = (MetaDataEntry) ObjectStateUtils
                    .readObject(in, "MessageContext.metaAxisOperation");
        } else {
            metaAxisOperation = null;
        }

        // operation context is not usable until it has been activated
        // NOTE: expect this to be the parent
        operationContext = (OperationContext) ObjectStateUtils
                .readObject(in, "MessageContext.operationContext");

        if (operationContext != null) {
            if (LoggingControl.debugLoggingAllowed && log.isTraceEnabled()) {
                log.trace(logCorrelationIDString + ":readExternal(): restored OperationContext [" +
                        operationContext.getLogCorrelationIDString() + "]");
            }
        }

        //---------------------------------------------------------
        // service
        //---------------------------------------------------------

        // axisService is not usable until the meta data has been reconciled
        axisService = null;

        ObjectStateUtils.readString(in, "MessageContext.axisService");

        boolean metaAxisServiceIsActive = in.readBoolean();

        if (metaAxisServiceIsActive == ObjectStateUtils.ACTIVE_OBJECT) {
            metaAxisService = (MetaDataEntry) ObjectStateUtils
                    .readObject(in, "MessageContext.metaAxisService");
        } else {
            metaAxisService = null;
        }

        //-------------------------
        // serviceContextID string
        //-------------------------

        serviceContextID = ObjectStateUtils.readString(in, "MessageContext.serviceContextID");

        //-------------------------
        // serviceContext
        //-------------------------

        ObjectStateUtils.readString(in, "MessageContext.serviceContext");

        boolean servCtxActive = in.readBoolean();

        if (servCtxActive == ObjectStateUtils.EMPTY_OBJECT) {
            // empty object

            serviceContext = null;
        } else {
            // active object

            boolean isParent = in.readBoolean();

            // there's an object to read in if it is not the parent of the operation context
            if (!isParent) {
                serviceContext = (ServiceContext) ObjectStateUtils
                        .readObject(in, "MessageContext.serviceContext");
            } else {
                // the service context is the parent of the operation context
                // so get it from the operation context during activate
                serviceContext = null;
            }
        }

        //---------------------------------------------------------
        // serviceGroup
        //---------------------------------------------------------

        // axisServiceGroup is not usable until the meta data has been reconciled
        axisServiceGroup = null;

        ObjectStateUtils.readString(in, "MessageContext.axisServiceGroup");

        boolean metaAxisServiceGrpIsActive = in.readBoolean();

        if (metaAxisServiceGrpIsActive == ObjectStateUtils.ACTIVE_OBJECT) {
            metaAxisServiceGroup = (MetaDataEntry) ObjectStateUtils
                    .readObject(in, "MessageContext.metaAxisServiceGroup");
        } else {
            metaAxisServiceGroup = null;
        }

        //-----------------------------
        // serviceGroupContextId string
        //-----------------------------
        serviceGroupContextId =
                ObjectStateUtils.readString(in, "MessageContext.serviceGroupContextId");

        //-----------------------------
        // serviceGroupContext
        //-----------------------------

        ObjectStateUtils.readString(in, "MessageContext.serviceGroupContext");

        boolean servGrpCtxActive = in.readBoolean();

        if (servGrpCtxActive == ObjectStateUtils.EMPTY_OBJECT) {
            // empty object

            serviceGroupContext = null;
        } else {
            // active object

            boolean isParentSGC = in.readBoolean();

            // there's an object to read in if it is not the parent of the service group context
            if (!isParentSGC) {
                serviceGroupContext = (ServiceGroupContext) ObjectStateUtils
                        .readObject(in, "MessageContext.serviceGroupContext");
            } else {
                // the service group context is the parent of the service context
                // so get it from the service context during activate
                serviceGroupContext = null;
            }
        }

        //---------------------------------------------------------
        // axis message
        //---------------------------------------------------------

        // axisMessage is not usable until the meta data has been reconciled
        axisMessage = null;

        ObjectStateUtils.readString(in, "MessageContext.axisMessage");

        boolean metaAxisMessageIsActive = in.readBoolean();

        if (metaAxisMessageIsActive == ObjectStateUtils.ACTIVE_OBJECT) {
            metaAxisMessage = (MetaDataEntry) ObjectStateUtils
                    .readObject(in, "MessageContext.metaAxisMessage");
            reconcileAxisMessage = true;
        } else {
            metaAxisMessage = null;
            reconcileAxisMessage = false;
        }

        //---------------------------------------------------------
        // configuration context
        //---------------------------------------------------------

        // TODO: check to see if there is any runtime data important to this
        //       message context in the configuration context
        //       if so, then need to restore the saved runtime data and reconcile
        //       it with the configuration context on the system when
        //       this message context object is restored

        //---------------------------------------------------------
        // session context
        //---------------------------------------------------------

        sessionContext =
                (SessionContext) ObjectStateUtils.readObject(in, "MessageContext.sessionContext");

        //---------------------------------------------------------
        // transport
        //---------------------------------------------------------

        //------------------------------
        // incomingTransportName string
        //------------------------------
        incomingTransportName =
                ObjectStateUtils.readString(in, "MessageContext.incomingTransportName");

        // TransportInDescription transportIn
        // is not usable until the meta data has been reconciled
        transportIn = null;
        metaTransportIn =
                (MetaDataEntry) ObjectStateUtils.readObject(in, "MessageContext.metaTransportIn");

        // TransportOutDescription transportOut
        // is not usable until the meta data has been reconciled
        transportOut = null;
        metaTransportOut =
                (MetaDataEntry) ObjectStateUtils.readObject(in, "MessageContext.metaTransportOut");

        //---------------------------------------------------------
        // properties
        //---------------------------------------------------------

        HashMap tmpHashMap = ObjectStateUtils.readHashMap(in, "MessageContext.properties");

        properties = new HashMap();
        if (tmpHashMap != null) {
            setProperties(tmpHashMap);
        }

        //---------------------------------------------------------
        // special data
        //---------------------------------------------------------

        ObjectStateUtils.readString(in, "MessageContext.selfManagedData");

        boolean gotSelfManagedData = in.readBoolean();

        if (gotSelfManagedData == ObjectStateUtils.ACTIVE_OBJECT) {
            selfManagedDataHandlerCount = in.readInt();

            if (selfManagedDataListHolder == null) {
                selfManagedDataListHolder = new ArrayList();
            } else {
                selfManagedDataListHolder.clear();
            }

            for (int i = 0; i < selfManagedDataHandlerCount; i++) {
                selfManagedDataListHolder.add(in.readObject());
            }
        }

        //---------------------------------------------------------
        // done
        //---------------------------------------------------------

        // trace point
        if (LoggingControl.debugLoggingAllowed && log.isTraceEnabled()) {
            log.trace(logCorrelationIDString +
                    ":readExternal():  message context object created for  " +
                    logCorrelationIDString);
        }
    }


    /**
     * This method checks to see if additional work needs to be
     * done in order to complete the object reconstitution.
     * Some parts of the object restored from the readExternal()
     * cannot be completed until we have a configurationContext
     * from the active engine. The configurationContext is used
     * to help this object to plug back into the engine's
     * configuration and deployment objects.
     *
     * @param cc The configuration context object representing the active configuration
     */
    public void activate(ConfigurationContext cc) {
        // see if there's any work to do
        if (!needsToBeReconciled) {
            // return quick
            return;
        }

        // use the supplied configuration context
        setConfigurationContext(cc);

        // get the axis configuration
        AxisConfiguration axisConfig = configurationContext.getAxisConfiguration();

        // We previously saved metaAxisService; restore it
        if (metaAxisService != null) {
            this.setAxisService(ObjectStateUtils.findService(axisConfig,
                                                             metaAxisService.getClassName(),
                                                             metaAxisService.getQNameAsString()));
        }

        // We previously saved metaAxisServiceGroup; restore it
        if (metaAxisServiceGroup != null) {
            this.setAxisServiceGroup(ObjectStateUtils.findServiceGroup(axisConfig,
                                                                       metaAxisServiceGroup.getClassName(),
                                                                       metaAxisServiceGroup.getQNameAsString()));
        }

        // We previously saved metaAxisOperation; restore it
        if (metaAxisOperation != null) {
            AxisService serv = axisService;

            if (serv != null) {
                // TODO: check for the empty name
                this.setAxisOperation(ObjectStateUtils.findOperation(serv,
                                                                     metaAxisOperation.getClassName(),
                                                                     metaAxisOperation.getQName()));
            } else {
                this.setAxisOperation(ObjectStateUtils.findOperation(axisConfig,
                                                                     metaAxisOperation.getClassName(),
                                                                     metaAxisOperation.getQName()));
            }
        }

        // We previously saved metaAxisMessage; restore it
        if (metaAxisMessage != null) {
            AxisOperation op = axisOperation;

            if (op != null) {
                // TODO: check for the empty name
                this.setAxisMessage(ObjectStateUtils.findMessage(op,
                                                                 metaAxisMessage.getQNameAsString(),
                                                                 metaAxisMessage.getExtraName()));
            }
        }

        //---------------------------------------------------------------------
        // operation context
        //---------------------------------------------------------------------
        // this will do a full hierarchy, so do it first
        // then we can re-use its objects

        if (operationContext != null) {
            operationContext.activate(cc);

            // this will be set as the parent of the message context
            // after the other context objects have been activated
        }

        //---------------------------------------------------------------------
        // service context
        //---------------------------------------------------------------------

        if (serviceContext == null) {
            // get the parent serviceContext of the operationContext
            if (operationContext != null) {
                serviceContext = operationContext.getServiceContext();
            }
        }

        // if we have a service context, make sure it is usable
        if (serviceContext != null) {
            // for some reason, the service context might be set differently from
            // the operation context parent
            serviceContext.activate(cc);
        }

        //---------------------------------------------------------------------
        // service group context
        //---------------------------------------------------------------------

        if (serviceGroupContext == null) {
            // get the parent serviceGroupContext of the serviceContext
            if (serviceContext != null) {
                serviceGroupContext = (ServiceGroupContext) serviceContext.getParent();
            }
        }

        // if we have a service group context, make sure it is usable
        if (serviceGroupContext != null) {
            // for some reason, the service group context might be set differently from
            // the service context parent
            serviceGroupContext.activate(cc);
        }

        //---------------------------------------------------------------------
        // other context-related reconciliation
        //---------------------------------------------------------------------

        this.setParent(operationContext);

        //---------------------------------------------------------------------
        // options
        //---------------------------------------------------------------------
        if (options != null) {
            options.activate(cc);
        }

        String tmpID = getMessageID();
        String logCorrelationIDString = getLogIDString();

        if (LoggingControl.debugLoggingAllowed && log.isTraceEnabled()) {
            log.trace(logCorrelationIDString + ":activate():   message ID [" + tmpID + "] for " +
                    logCorrelationIDString);
        }

        //---------------------------------------------------------------------
        // transports
        //---------------------------------------------------------------------

        // We previously saved metaTransportIn; restore it
        if (metaTransportIn != null) {
            QName qin = metaTransportIn.getQName();
            TransportInDescription tmpIn = null;
            try {
                tmpIn = axisConfig.getTransportIn(qin.getLocalPart());
            }
            catch (Exception exin) {
                // if a fault is thrown, log it and continue
                log.trace(logCorrelationIDString +
                        "activate():  exception caught when getting the TransportInDescription [" +
                        qin.toString() + "]  from the AxisConfiguration [" +
                        exin.getClass().getName() + " : " + exin.getMessage() + "]");
            }

            if (tmpIn != null) {
                transportIn = tmpIn;
            } else {
                transportIn = null;
            }
        } else {
            transportIn = null;
        }

        // We previously saved metaTransportOut; restore it
        if (metaTransportOut != null) {
            // TODO : Check if this should really be a QName?
            QName qout = metaTransportOut.getQName();
            TransportOutDescription tmpOut = null;
            try {
                tmpOut = axisConfig.getTransportOut(qout.getLocalPart());
            }
            catch (Exception exout) {
                // if a fault is thrown, log it and continue
                if (LoggingControl.debugLoggingAllowed && log.isTraceEnabled()) {
                    log.trace(logCorrelationIDString +
                        "activate():  exception caught when getting the TransportOutDescription [" +
                        qout.toString() + "]  from the AxisConfiguration [" +
                        exout.getClass().getName() + " : " + exout.getMessage() + "]");
                }
            }

            if (tmpOut != null) {
                transportOut = tmpOut;
            } else {
                transportOut = null;
            }
        } else {
            transportOut = null;
        }

        //-------------------------------------------------------
        // reconcile the execution chain
        //-------------------------------------------------------
        if (metaExecutionChain != null) {
            if (LoggingControl.debugLoggingAllowed && log.isTraceEnabled()) {
                log.trace(
                        logCorrelationIDString + ":activate(): reconciling the execution chain...");
            }

            currentHandlerIndex = metaHandlerIndex;
            currentPhaseIndex = metaPhaseIndex;

            executionChain = restoreHandlerList(metaExecutionChain);

            try {
                deserializeSelfManagedData();
            }
            catch (Exception ex) {
                // log the exception
                if (LoggingControl.debugLoggingAllowed && log.isTraceEnabled()) {
                    log.trace(logCorrelationIDString +
                        ":activate(): *** WARNING *** deserializing the self managed data encountered Exception [" +
                        ex.getClass().getName() + " : " + ex.getMessage() + "]", ex);
                }
            }
        }

        //-------------------------------------------------------
        // reconcile the lists for the executed phases
        //-------------------------------------------------------
        if (metaInboundExecuted != null) {
            if (LoggingControl.debugLoggingAllowed && log.isTraceEnabled()) {
                log.trace(logCorrelationIDString +
                        ":activate(): reconciling the inbound executed chain...");
            }

            if (!(inboundReset)) {
                inboundExecutedPhases =
                        restoreExecutedList(inboundExecutedPhases, metaInboundExecuted);
            }
        }

        if (inboundExecutedPhases == null) {
            inboundExecutedPhases = new LinkedList();
        }


        if (metaOutboundExecuted != null) {
            if (LoggingControl.debugLoggingAllowed && log.isTraceEnabled()) {
                log.trace(logCorrelationIDString +
                        ":activate(): reconciling the outbound executed chain...");
            }

            if (!(outboundReset)) {
                outboundExecutedPhases =
                        restoreExecutedList(outboundExecutedPhases, metaOutboundExecuted);
            }
        }

        if (outboundExecutedPhases == null) {
            outboundExecutedPhases = new LinkedList();
        }

        //-------------------------------------------------------
        // finish up remaining links
        //-------------------------------------------------------
        if (operationContext != null) {
            operationContext.restoreMessageContext(this);
        }

        //-------------------------------------------------------
        // done, reset the flag
        //-------------------------------------------------------
        needsToBeReconciled = false;

    }


    /**
     * This method checks to see if additional work needs to be
     * done in order to complete the object reconstitution.
     * Some parts of the object restored from the readExternal()
     * cannot be completed until we have an object that gives us
     * a view of the active object graph from the active engine.
     * <p/>
     * NOTE: when activating an object, you only need to call
     * one of the activate methods (activate() or activateWithOperationContext())
     * but not both.
     *
     * @param operationCtx The operation context object that is a member of the active object graph
     */
    public void activateWithOperationContext(OperationContext operationCtx) {
        // see if there's any work to do
        if (!(needsToBeReconciled)) {
            // return quick
            return;
        }

        String logCorrelationIDString = getLogIDString();
        // trace point
        if (LoggingControl.debugLoggingAllowed && log.isTraceEnabled()) {
            log.trace(logCorrelationIDString + ":activateWithOperationContext():  BEGIN");
        }

        if (operationCtx == null) {
            // won't be able to finish
            if (LoggingControl.debugLoggingAllowed && log.isTraceEnabled()) {
                log.trace(logCorrelationIDString +
                    ":activateWithOperationContext():  *** WARNING ***  No active OperationContext object is available.");
            }
            return;
        }

        //---------------------------------------------------------------------
        // locate the objects in the object graph
        //---------------------------------------------------------------------
        ConfigurationContext configCtx = operationCtx.getConfigurationContext();

        if (configCtx == null) {
            // won't be able to finish
            if (LoggingControl.debugLoggingAllowed && log.isTraceEnabled()) {
                log.trace(logCorrelationIDString +
                    ":activateWithOperationContext():  *** WARNING ***  No active ConfigurationContext object is available.");
            }
            return;
        }

        AxisConfiguration axisCfg = configCtx.getAxisConfiguration();

        AxisOperation axisOp = operationCtx.getAxisOperation();
        ServiceContext serviceCtx = operationCtx.getServiceContext();

        ServiceGroupContext serviceGroupCtx = null;
        AxisService axisSrv = null;
        AxisServiceGroup axisSG = null;

        if (serviceCtx != null) {
            serviceGroupCtx = serviceCtx.getServiceGroupContext();
            axisSrv = serviceCtx.getAxisService();
        }

        if (serviceGroupCtx != null) {
            axisSG = serviceGroupCtx.getDescription();
        }

        //---------------------------------------------------------------------
        // link to the objects in the object graph
        //---------------------------------------------------------------------

        setConfigurationContext(configCtx);

        setAxisOperation(axisOp);
        setAxisService(axisSrv);
        setAxisServiceGroup(axisSG);

        setServiceGroupContext(serviceGroupCtx);
        setServiceContext(serviceCtx);
        setOperationContext(operationCtx);

        //---------------------------------------------------------------------
        // reconcile the remaining objects
        //---------------------------------------------------------------------

        // We previously saved metaAxisMessage; restore it
        if (metaAxisMessage != null) {
            if (axisOp != null) {
                // TODO: check for the empty name
                this.setAxisMessage(ObjectStateUtils.findMessage(axisOp,
                                                                 metaAxisMessage.getQNameAsString(),
                                                                 metaAxisMessage.getExtraName()));
            }
        }

        //---------------------------------------------------------------------
        // options
        //---------------------------------------------------------------------
        if (options != null) {
            options.activate(configCtx);
        }

        String tmpID = getMessageID();

        if (LoggingControl.debugLoggingAllowed && log.isTraceEnabled()) {
            log.trace(logCorrelationIDString + ":activateWithOperationContext():   message ID [" +
                    tmpID + "]");
        }

        //---------------------------------------------------------------------
        // transports
        //---------------------------------------------------------------------

        // We previously saved metaTransportIn; restore it
        if (metaTransportIn != null) {
            QName qin = metaTransportIn.getQName();
            TransportInDescription tmpIn = null;
            try {
                tmpIn = axisCfg.getTransportIn(qin.getLocalPart());
            }
            catch (Exception exin) {
                // if a fault is thrown, log it and continue
                if (LoggingControl.debugLoggingAllowed && log.isTraceEnabled()) {
                    log.trace(logCorrelationIDString +
                        "activateWithOperationContext():  exception caught when getting the TransportInDescription [" +
                        qin.toString() + "]  from the AxisConfiguration [" +
                        exin.getClass().getName() + " : " + exin.getMessage() + "]");
                }

            }

            if (tmpIn != null) {
                transportIn = tmpIn;
            } else {
                transportIn = null;
            }
        } else {
            transportIn = null;
        }

        // We previously saved metaTransportOut; restore it
        if (metaTransportOut != null) {
            QName qout = metaTransportOut.getQName();
            TransportOutDescription tmpOut = null;
            try {
                tmpOut = axisCfg.getTransportOut(qout.getLocalPart());
            }
            catch (Exception exout) {
                // if a fault is thrown, log it and continue
                if (LoggingControl.debugLoggingAllowed && log.isTraceEnabled()) {
                    log.trace(logCorrelationIDString +
                        "activateWithOperationContext():  exception caught when getting the TransportOutDescription [" +
                        qout.toString() + "]  from the AxisConfiguration [" +
                        exout.getClass().getName() + " : " + exout.getMessage() + "]");
                }
            }

            if (tmpOut != null) {
                transportOut = tmpOut;
            } else {
                transportOut = null;
            }
        } else {
            transportOut = null;
        }

        //-------------------------------------------------------
        // reconcile the execution chain
        //-------------------------------------------------------
        if (metaExecutionChain != null) {
            if (LoggingControl.debugLoggingAllowed && log.isTraceEnabled()) {
                log.trace(logCorrelationIDString +
                        ":activateWithOperationContext(): reconciling the execution chain...");
            }

            currentHandlerIndex = metaHandlerIndex;
            currentPhaseIndex = metaPhaseIndex;

            executionChain = restoreHandlerList(metaExecutionChain);

            try {
                deserializeSelfManagedData();
            }
            catch (Exception ex) {
                // log the exception
                if (LoggingControl.debugLoggingAllowed && log.isTraceEnabled()) {
                    log.trace(logCorrelationIDString +
                        ":activateWithOperationContext(): *** WARNING *** deserializing the self managed data encountered Exception [" +
                        ex.getClass().getName() + " : " + ex.getMessage() + "]", ex);
                }
            }
        }

        //-------------------------------------------------------
        // reconcile the lists for the executed phases
        //-------------------------------------------------------
        if (metaInboundExecuted != null) {
            if (LoggingControl.debugLoggingAllowed && log.isTraceEnabled()) {
                log.trace(logCorrelationIDString +
                        ":activateWithOperationContext(): reconciling the inbound executed chain...");
            }

            if (!(inboundReset)) {
                inboundExecutedPhases =
                        restoreExecutedList(inboundExecutedPhases, metaInboundExecuted);
            }
        }

        if (inboundExecutedPhases == null) {
            inboundExecutedPhases = new LinkedList();
        }


        if (metaOutboundExecuted != null) {
            if (LoggingControl.debugLoggingAllowed && log.isTraceEnabled()) {
                log.trace(logCorrelationIDString +
                        ":activateWithOperationContext(): reconciling the outbound executed chain...");
            }

            if (!(outboundReset)) {
                outboundExecutedPhases =
                        restoreExecutedList(outboundExecutedPhases, metaOutboundExecuted);
            }
        }

        if (outboundExecutedPhases == null) {
            outboundExecutedPhases = new LinkedList();
        }

        //-------------------------------------------------------
        // done, reset the flag
        //-------------------------------------------------------
        needsToBeReconciled = false;

        if (LoggingControl.debugLoggingAllowed && log.isTraceEnabled()) {
            log.trace(logCorrelationIDString + ":activateWithOperationContext():  END");
        }
    }


    /**
     * @param metaDataEntries ArrayList of MetaDataEntry objects
     * @return ArrayList of Handlers based on our list of handlers from the reconstituted deserialized list, and the existing handlers in the AxisConfiguration object.  May return null.
     */
    private ArrayList restoreHandlerList(ArrayList metaDataEntries) {
        AxisConfiguration axisConfig = configurationContext.getAxisConfiguration();

        ArrayList existingHandlers = null;

        // TODO: I'm using clone for the ArrayList returned from axisConfig object.
        //     Does it do a deep clone of the Handlers held there?  Does it matter?
        switch (FLOW) {
            case IN_FLOW:
                existingHandlers = (ArrayList) axisConfig.getGlobalInFlow().clone();
                break;

            case OUT_FLOW:
                existingHandlers = (ArrayList) axisConfig.getGlobalOutPhases().clone();
                break;

            case IN_FAULT_FLOW:
                existingHandlers = (ArrayList) axisConfig.getInFaultFlow().clone();
                break;

            case OUT_FAULT_FLOW:
                existingHandlers = (ArrayList) axisConfig.getOutFaultFlow().clone();
                break;
        }

        existingHandlers = flattenHandlerList(existingHandlers, null);

        ArrayList handlerListToReturn = new ArrayList();

        for (int i = 0; i < metaDataEntries.size(); i++) {
            Handler handler = (Handler) ObjectStateUtils
                    .findHandler(existingHandlers, (MetaDataEntry) metaDataEntries.get(i));

            if (handler != null) {
                handlerListToReturn.add(handler);
            }
        }

        return handlerListToReturn;
    }


    /**
     * Using meta data for phases/handlers, create a linked list of actual
     * phase/handler objects.  The created list is composed of the objects
     * from the base list at the top of the created list followed by the
     * restored objects.
     *
     * @param base            Linked list of phase/handler objects
     * @param metaDataEntries Linked list of MetaDataEntry objects
     * @return LinkedList of objects or NULL if none available
     */
    private LinkedList restoreExecutedList(LinkedList base, LinkedList metaDataEntries) {
        if (metaDataEntries == null) {
            return base;
        }

        // get a list of existing handler/phase objects for the restored objects

        ArrayList tmpMetaDataList = new ArrayList(metaDataEntries);

        ArrayList existingList = restoreHandlerList(tmpMetaDataList);

        if ((existingList == null) || (existingList.isEmpty())) {
            return base;
        }

        // set up a list to return

        LinkedList returnedList = new LinkedList();

        if (base != null) {
            returnedList.addAll(base);
        }

        returnedList.addAll(existingList);

        return returnedList;
    }


    /**
     * Process the list of handlers from the Phase object
     * into the appropriate meta data.
     *
     * @param phase   The Phase object containing a list of handlers
     * @param mdPhase The meta data object associated with the specified Phase object
     */
    private void setupPhaseList(Phase phase, MetaDataEntry mdPhase) {
        // get the list from the phase object
        ArrayList handlers = phase.getHandlers();

        if (handlers.isEmpty()) {
            // done, make sure there is no list in the given meta data
            mdPhase.removeList();
            return;
        }

        // get the metadata on each member of the list

        int listSize = handlers.size();

        if (listSize > 0) {

            Iterator i = handlers.iterator();

            while (i.hasNext()) {
                Object obj = i.next();
                String objClass = obj.getClass().getName();

                // start the meta data entry for this object
                MetaDataEntry mdEntry = new MetaDataEntry();
                mdEntry.setClassName(objClass);

                // get the correct object-specific name
                String qnameAsString;

                if (obj instanceof Phase) {
                    // nested condition, the phase object contains another phase!
                    Phase phaseObj = (Phase) obj;
                    qnameAsString = phaseObj.getName();

                    // add the list of handlers to the meta data
                    setupPhaseList(phaseObj, mdEntry);
                } else if (obj instanceof Handler) {
                    Handler handlerObj = (Handler) obj;
                    qnameAsString = handlerObj.getName();
                } else {
                    // TODO: will there be any other kinds of objects
                    // in the list?
                    qnameAsString = "NULL";
                }

                mdEntry.setQName(qnameAsString);

                // done with setting up the meta data for the list entry
                // so add it to the parent
                mdPhase.addToList(mdEntry);

                if (LoggingControl.debugLoggingAllowed && log.isTraceEnabled()) {
                    log.trace(getLogIDString() + ":setupPhaseList(): list entry class [" +
                            objClass + "] qname [" + qnameAsString + "]");
                }

            } // end while entries in list
        } else {
            // a list with no entries
            // done, make sure there is no list in the given meta data
            mdPhase.removeList();
        }
    }


    /**
     * Return a Read-Only copy of this message context
     * that has been extracted from the object
     * hierachy.  In other words, the message context
     * copy does not have links to the object graph.
     * <p/>
     * NOTE: The copy shares certain objects with the original.
     * The intent is to use the copy to read values but not
     * modify them, especially since the copy is not part
     * of the normal *Context and Axis* object graph.
     *
     * @return A copy of the message context that is not in the object graph
     */
    public MessageContext extractCopyMessageContext() {
        MessageContext copy = new MessageContext();
        String logCorrelationIDString = getLogIDString();
        if (LoggingControl.debugLoggingAllowed && log.isTraceEnabled()) {
            log.trace(logCorrelationIDString + ":extractCopyMessageContext():  based on " +
                    logCorrelationIDString + "   into copy " + copy.getLogIDString());
        }

        //---------------------------------------------------------
        // various simple fields
        //---------------------------------------------------------

        copy.setFLOW(FLOW);

        copy.setProcessingFault(processingFault);
        copy.setPaused(paused);
        copy.setOutputWritten(outputWritten);
        copy.setNewThreadRequired(newThreadRequired);
        copy.setDoingREST(doingREST);
        copy.setDoingMTOM(doingMTOM);
        copy.setDoingSwA(doingSwA);
        copy.setResponseWritten(responseWritten);
        copy.setServerSide(serverSide);

        copy.setLastTouchedTime(getLastTouchedTime());

        //---------------------------------------------------------
        // message
        //---------------------------------------------------------
        try {
            copy.setEnvelope(envelope);
        }
        catch (Exception ex) {
            if (LoggingControl.debugLoggingAllowed && log.isTraceEnabled()) {
                log.trace(logCorrelationIDString +
                    ":extractCopyMessageContext():  Exception caught when setting the copy with the envelope",
                      ex);
            }
        }

        copy.setAttachmentMap(attachments);

        copy.setIsSOAP11Explicit(isSOAP11);

        //---------------------------------------------------------
        // ArrayList executionChain
        //     handler and phase related data
        //---------------------------------------------------------
        copy.setExecutionChain(executionChain);

        // the setting of the execution chain is actually a reset
        // so copy the indices after putting in the execution chain
        copy.setCurrentHandlerIndex(currentHandlerIndex);
        copy.setCurrentPhaseIndex(currentPhaseIndex);

        //---------------------------------------------------------
        // LinkedList inboundExecutedPhases
        //---------------------------------------------------------
        copy.setInboundExecutedPhasesExplicit(inboundExecutedPhases);

        //---------------------------------------------------------
        // LinkedList outboundExecutedPhases
        //---------------------------------------------------------
        copy.setOutboundExecutedPhasesExplicit(outboundExecutedPhases);

        //---------------------------------------------------------
        // options
        //---------------------------------------------------------
        copy.setOptionsExplicit(options);

        //---------------------------------------------------------
        // axis operation
        //---------------------------------------------------------
        copy.setAxisOperation(null);

        //---------------------------------------------------------
        // operation context
        //---------------------------------------------------------
        copy.setOperationContext(null);

        //---------------------------------------------------------
        // axis service
        //---------------------------------------------------------
        copy.setAxisService(null);

        //-------------------------
        // serviceContextID string
        //-------------------------
        copy.setServiceContextID(serviceContextID);

        //-------------------------
        // serviceContext
        //-------------------------
        copy.setServiceContext(null);

        //---------------------------------------------------------
        // serviceGroup
        //---------------------------------------------------------
        copy.setServiceGroupContext(null);

        //-----------------------------
        // serviceGroupContextId string
        //-----------------------------
        copy.setServiceGroupContextId(serviceGroupContextId);

        //---------------------------------------------------------
        // axis message
        //---------------------------------------------------------
        copy.setAxisMessage(axisMessage);

        //---------------------------------------------------------
        // configuration context
        //---------------------------------------------------------
        copy.setConfigurationContext(configurationContext);

        //---------------------------------------------------------
        // session context
        //---------------------------------------------------------
        copy.setSessionContext(sessionContext);

        //---------------------------------------------------------
        // transport
        //---------------------------------------------------------

        //------------------------------
        // incomingTransportName string
        //------------------------------
        copy.setIncomingTransportName(incomingTransportName);

        copy.setTransportIn(transportIn);
        copy.setTransportOut(transportOut);

        //---------------------------------------------------------
        // properties
        //---------------------------------------------------------
        copy.setProperties(getProperties());

        //---------------------------------------------------------
        // special data
        //---------------------------------------------------------

        copy.setSelfManagedDataMapExplicit(selfManagedDataMap);

        //---------------------------------------------------------
        // done
        //---------------------------------------------------------

        return copy;
    }

    //------------------------------------------------------------------------
    // additional setter methods needed to copy the message context object
    //------------------------------------------------------------------------

    public void setIsSOAP11Explicit(boolean t) {
        isSOAP11 = t;
    }


    public void setInboundExecutedPhasesExplicit(LinkedList inb) {
        inboundExecutedPhases = inb;
    }

    public void setOutboundExecutedPhasesExplicit(LinkedList outb) {
        outboundExecutedPhases = outb;
    }

    public void setSelfManagedDataMapExplicit(LinkedHashMap map) {
        selfManagedDataMap = map;
    }

    public void setOptionsExplicit(Options op) {
        this.options = op;
    }


    /**
     * Trace a warning message, if needed, indicating that this
     * object needs to be activated before accessing certain fields.
     *
     * @param methodname The method where the warning occurs
     */
    private void checkActivateWarning(String methodname) {
        if (needsToBeReconciled) {
            if (LoggingControl.debugLoggingAllowed && log.isWarnEnabled()) {
                log.warn(getLogIDString() + ":" + methodname + "(): ****WARNING**** " + myClassName +
                    ".activate(configurationContext) needs to be invoked.");
            }
        }
    }

    public ConfigurationContext getRootContext() {
        return configurationContext;
    }


}
