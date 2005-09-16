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

import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.addressing.MessageInformationHeaders;
import org.apache.axis2.addressing.miheaders.RelatesTo;
import org.apache.axis2.description.HandlerDescription;
import org.apache.axis2.description.ModuleConfiguration;
import org.apache.axis2.description.ModuleDescription;
import org.apache.axis2.description.OperationDescription;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.description.ServiceDescription;
import org.apache.axis2.description.ServiceGroupDescription;
import org.apache.axis2.description.TransportInDescription;
import org.apache.axis2.description.TransportOutDescription;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.engine.AxisConfigurationImpl;
import org.apache.axis2.soap.SOAP11Constants;
import org.apache.axis2.soap.SOAP12Constants;
import org.apache.axis2.soap.SOAPEnvelope;

import javax.xml.namespace.QName;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * The palce where all the service specific states are kept.
 * All the Global states kept in the <code>EngineRegistry</code> and all the
 * Service states kept in the <code>MessageContext</code>. Other runtime
 * artifacts does not keep states foward from the execution.
 */
public class MessageContext extends AbstractContext {

    public static final String TRANSPORT_HEADERS = "TRANSPORT_HEADERS";
    /**
     * Field TRANSPORT_WRITER
     */
    public static final String TRANSPORT_OUT = "TRANSPORT_OUT";

    /**
     * Field TRANSPORT_READER
     */
    public static final String TRANSPORT_IN = "TRANSPORT_IN";

    /**
     * Field  CHARACTER_SET_ENCODING
     */
    public static final String CHARACTER_SET_ENCODING =
            "CHARACTER_SET_ENCODING";

    /**
     * Field UTF_8
     * This is the 'utf-8' value for CHARACTER_SET_ENCODING property
     */
    public static final String UTF_8 = "UTF-8";

    /**
     * Field UTF_8
     * This is the 'utf-8' value for CHARACTER_SET_ENCODING property
     */
    public static final String UTF_16 = "utf-16";

    /**
     * Field DEFAULT_CHAR_SET_ENCODING
     * This is the default value for CHARACTER_SET_ENCODING property
     */
    public static final String DEFAULT_CHAR_SET_ENCODING = UTF_8;

    /**
     * Field TRANSPORT_SUCCEED
     */
    public static final String TRANSPORT_SUCCEED = "TRANSPORT_SUCCEED";

    /**
     * Field processingFault
     */
    private boolean processingFault = false;

    /**
     * Addressing Information for Axis 2
     * Following Properties will be kept inside this, these fields will be initially filled by
     * the transport. Then later a addressing handler will make relevant changes to this, if addressing
     * information is present in the SOAP header.
     */

    private MessageInformationHeaders messageInformationHeaders;

    private OperationContext operationContext;
    private ServiceContext serviceContext;
    private ServiceGroupContext serviceGroupContext;

    private transient OperationDescription operationDescription;
    private transient ServiceDescription serviceDescription;
    private transient ServiceGroupDescription serviceGroupDescription;
    private ConfigurationContext configurationContext;

    private transient TransportInDescription transportIn;
    private transient TransportOutDescription transportOut;

    /**
     * Field sessionContext
     */
    private final SessionContext sessionContext;

    /**
     * Field service
     */

    /**
     * Field envelope
     */
    private SOAPEnvelope envelope;

    /**
     * Field responseWritten
     */
    private boolean responseWritten;

    /**
     * Field inFaultFlow
     */
    private boolean inFaultFlow;

    /**
     * Field serverSide
     */
    private boolean serverSide;

    /**
     * Field messageID
     */
    private String messageID;

    /**
     * Field newThreadRequired
     */
    private boolean newThreadRequired = false;

    private boolean paused = false;

    public boolean outPutWritten = false;

    private String serviceContextID;

    private String pausedPhaseName;

    private QName pausedHandlerName;

    private String soapAction;

    //Are we doing MTOM now?
    private boolean doingMTOM = false;
    //Are we doing REST now?
    private boolean doingREST = false;
    //Rest through GET of HTTP
    private boolean doRESTthroughPOST = true;

    private boolean isSOAP11 = true;

    /**
     * This will hold a key to retrieve the correct ServiceGroupContext.
     */
    private String serviceGroupContextId;

    QName transportInName = null;

    QName transportOutname = null;

    String serviceGroupDescId = null;

    QName serviceDescName = null;

    QName operationDescName = null;

    /**
     * The method is used to do the intialization of the EngineContext
     *
     * @throws AxisFault
     */
    public void init(AxisConfiguration axisConfiguration) throws AxisFault {
        if (transportInName != null)
            transportIn = axisConfiguration.getTransportIn(transportInName);
        if (transportOutname != null)
            transportOut = axisConfiguration.getTransportOut(transportOutname);
        if (serviceGroupDescId != null)
            serviceGroupDescription = axisConfiguration.getServiceGroup(serviceGroupDescId);
        if (serviceDescName != null)
            serviceDescription = axisConfiguration.getService(serviceDescName.getLocalPart());
        if (operationDescName != null)
            operationDescription = serviceDescription.getOperation(operationDescName);
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
    	if (transportIn != null)
            transportInName = transportIn.getName();
        if (transportOut != null)
            transportOutname = transportOut.getName();
        if (serviceGroupDescription != null)
            serviceGroupDescId = serviceGroupDescription.getServiceGroupName();
        if (serviceDescription != null)
            serviceDescName = serviceDescription.getName();
        if (operationDescription != null)
            operationDescName = operationDescription.getName();
        
        out.defaultWriteObject();
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
    }


    /**
     * Conveniance Method, but before call engine.send() or  engine.receive() one must send transport in/out
     *
     * @param engineContext
     * @throws AxisFault
     */

    public MessageContext(ConfigurationContext engineContext)
            throws AxisFault {
        this(engineContext, null, null, null);
    }

    public MessageContext(
            ConfigurationContext engineContext,
            TransportInDescription transportIn,
            TransportOutDescription transportOut)
            throws AxisFault {
        this(engineContext, null, transportIn, transportOut);
        this.transportInName = transportIn.getName();
        this.transportOutname = transportOut.getName();
    }

    /**
     * @param sessionContext
     * @param transportIn
     * @param transportOut
     * @throws AxisFault
     */

    public MessageContext(
            ConfigurationContext engineContext,
            SessionContext sessionContext,
            TransportInDescription transportIn,
            TransportOutDescription transportOut)
            throws AxisFault {
        super(null);

        if (sessionContext == null) {
            this.sessionContext = new SessionContext(null);
        } else {
            this.sessionContext = sessionContext;
        }
        messageInformationHeaders = new MessageInformationHeaders();
        this.transportIn = transportIn;
        this.transportOut = transportOut;
        this.configurationContext = engineContext;

        if (transportIn != null)
            this.transportInName = transportIn.getName();
        if (transportOut != null)
            this.transportOutname = transportOut.getName();
    }

    /**
     * @return
     */
    public EndpointReference getFaultTo() {
        return messageInformationHeaders.getFaultTo();
    }

    /**
     * @return
     */
    public EndpointReference getFrom() {
        return messageInformationHeaders.getFrom();
    }

    /**
     * @return
     */
    public boolean isInFaultFlow() {
        return inFaultFlow;
    }

    /**
     * @return
     */
    public SOAPEnvelope getEnvelope() {
        return envelope;
    }

    /**
     * @return
     */
    public String getMessageID() {
        return messageInformationHeaders.getMessageId();
    }

    /**
     * @return
     */
    public boolean isProcessingFault() {
        return processingFault;
    }

    /**
     * @return
     */
    public RelatesTo getRelatesTo() {
        return messageInformationHeaders.getRelatesTo();
    }

    /**
     * @return
     */
    public EndpointReference getReplyTo() {
        return messageInformationHeaders.getReplyTo();
    }

    /**
     * @return
     */
    public boolean isResponseWritten() {
        return responseWritten;
    }

    /**
     * @return
     */
    public boolean isServerSide() {
        return serverSide;
    }

    /**
     * @return
     */
    public SessionContext getSessionContext() {
        return sessionContext;
    }

    /**
     * @return
     */
    public EndpointReference getTo() {
        return messageInformationHeaders.getTo();
    }

    /**
     * @param reference
     */
    public void setFaultTo(EndpointReference reference) {
        messageInformationHeaders.setFaultTo(reference);
    }

    /**
     * @param reference
     */
    public void setFrom(EndpointReference reference) {
        messageInformationHeaders.setFrom(reference);
    }

    /**
     * @param b
     */
    public void setInFaultFlow(boolean b) {
        inFaultFlow = b;
    }

    /**
     * @param envelope
     */
    public void setEnvelope(SOAPEnvelope envelope) throws AxisFault {
        this.envelope = envelope;
        String soapNamespaceURI = envelope.getNamespace().getName();
        if (SOAP12Constants
                .SOAP_ENVELOPE_NAMESPACE_URI
                .equals(soapNamespaceURI)) {
            isSOAP11 = false;
        } else if (
                SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI.equals(
                        soapNamespaceURI)) {
            isSOAP11 = true;
        } else {
            throw new AxisFault("Unknown SOAP Version. Current Axis handles only SOAP 1.1 and SOAP 1.2 messages");
        }

    }

    /**
     * @param string
     */
    public void setMessageID(String string) {
        messageInformationHeaders.setMessageId(string);
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
    public void setRelatesTo(RelatesTo reference) {
        messageInformationHeaders.setRelatesTo(reference);
    }

    /**
     * @param referance
     */
    public void setReplyTo(EndpointReference referance) {
        messageInformationHeaders.setReplyTo(referance);
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
     * @param referance
     */
    public void setTo(EndpointReference referance) {
        messageInformationHeaders.setTo(referance);
    }

    /**
     * @return
     */
    public boolean isNewThreadRequired() {
        return newThreadRequired;
    }

    /**
     * @param b
     */
    public void setNewThreadRequired(boolean b) {
        newThreadRequired = b;
    }

    /**
     * Method getExecutionChain
     */

    public void setWSAAction(String actionURI) {
        messageInformationHeaders.setAction(actionURI);
    }

    public String getWSAAction() {
        return messageInformationHeaders.getAction();
    }

    public void setWSAMessageId(String messageID) {
        messageInformationHeaders.setMessageId(messageID);
    }

    public String getWSAMessageId() {
        return messageInformationHeaders.getMessageId();
    }

    public MessageInformationHeaders getMessageInformationHeaders() {
        return messageInformationHeaders;
    }

    /**
     * @return
     */
    public boolean isPaused() {
        return paused;
    }

    /**
     */
    public void setPausedTrue(QName handlerName) {
        paused = true;
        this.pausedHandlerName = handlerName;
    }

    public void setPausedFalse() {
        paused = false;
    }

    /**
     * @return
     */
    public TransportInDescription getTransportIn() {
        return transportIn;
    }

    /**
     * @return
     */
    public TransportOutDescription getTransportOut() {
        return transportOut;
    }

    /**
     * @param in
     */
    public void setTransportIn(TransportInDescription in) {
        transportIn = in;
        if (in != null)
            this.transportInName = in.getName();
    }

    /**
     * @param out
     */
    public void setTransportOut(TransportOutDescription out) {
        transportOut = out;
        if (out != null)
            this.transportOutname = out.getName();
    }

    /**
     * @return
     */
    public OperationContext getOperationContext() {
        return operationContext;
    }

    /**
     * @param context
     */
    public void setOperationContext(OperationContext context) {
        operationContext = context;
        if (serviceContext != null && operationContext.getParent() == null) {
            operationContext.setParent(serviceContext);
        }
        this.setParent(operationContext);
        if (operationContext != null) {
            this.setOperationDescription(operationContext.getAxisOperation());
        }
    }

    /**
     * @return
     */
    public boolean isOutPutWritten() {
        return outPutWritten;
    }

    /**
     * @param b
     */
    public void setOutPutWritten(boolean b) {
        outPutWritten = b;
    }

    /**
     * @return Returns the serviceContextID.
     */
    public String getServiceContextID() {
        return serviceContextID;
    }

    /**
     * @param serviceContextID The serviceContextID to set.
     */
    public void setServiceContextID(String serviceContextID) {
        this.serviceContextID = serviceContextID;
    }

    public ConfigurationContext getSystemContext() {
        return configurationContext;
    }

    /**
     * @return
     */
    public ServiceContext getServiceContext() {
        return serviceContext;
    }

    /**
     * @param context
     */
    public void setConfigurationContext(ConfigurationContext context) {
        configurationContext = context;
    }

    /**
     * @param context
     */
    public void setServiceContext(ServiceContext context) {
        serviceContext = context;
        if (operationContext != null && operationContext.getParent() != null) {
            operationContext.setParent(context);
        }
        this.setServiceDescription(context.getServiceConfig());
    }

    /**
     * @param collection
     */
    public void setMessageInformationHeaders(MessageInformationHeaders collection) {
        messageInformationHeaders = collection;
    }


    /**
     * To retrive configuration descriptor parameters , it is posible to get paramater specify at
     * any levle via this method , and the preferance is as follows,
     * 1. Search in operation description if its there
     * 2. if the paramter not found or operationContext is null will search in
     * ServiceDescription
     * 3. If the serviceDescription is null or , the paramter does not found will serach in
     * AxisConfiguration
     *
     * @param key
     * @return Paramter <code>Paramter</code>
     */
    public Parameter getParameter(String key) {
        Parameter param = null;
        if (getOperationDescription() != null) {
            OperationDescription opDesc = getOperationDescription();
            param = opDesc.getParameter(key);
        }
        if (param == null && getServiceDescription() != null) {
            ServiceDescription serviceDesc = getServiceDescription();
            param = serviceDesc.getParameter(key);
        }
        if (param == null && getServiceGroupDescription() != null) {
            ServiceGroupDescription serviceDesc = getServiceGroupDescription();
            param = serviceDesc.getParameter(key);
        }
        if (param == null && configurationContext != null) {
            AxisConfiguration baseConfig =
                    configurationContext.getAxisConfiguration();
            param = baseConfig.getParameter(key);
        }
        return param;
    }


    /**
     * This method is to retrive both module configuration parameters and othere paramerts
     * The searching procedure is as follows;
     * 1. Search in module configurations inside corresponding operation descripton if its three
     * 2. Search in corresponding operation if its there
     * 3. Search in module configurations inside corresponding service description if its there
     * 4. Next search in Corresponding Service description if its there
     * 5. Next sercah in module configurations inside axisConfiguration
     * 6. Search in AxisConfiguration for paramters
     * 7. Next get the corresponding module and search for the paramters
     * 8. Search in HandlerDescription for the paramter
     * <p/>
     * and the way of specifing mdoule configuration is as follows
     * <moduleConfig name="addressing">
     * <parameter name="addressingPara" locked="false">N/A</parameter>
     * </moduleConfig>
     *
     * @param key        : Paramtre Name
     * @param moduleName : Name of the module
     * @param handler    <code>HandlerDescription</code>
     * @return Parameter <code>Parameter</code>
     */
    public Parameter getModuleParameter(String key, String moduleName, HandlerDescription handler) {
        Parameter param = null;
        ModuleConfiguration moduleConfig = null;
        if (getOperationDescription() != null) {
            OperationDescription opDesc = getOperationDescription();
            moduleConfig = opDesc.getModuleConfig(new QName(moduleName));
            if (moduleConfig != null) {
                param = moduleConfig.getParameter(key);
            }
            if (param == null) {
                param = opDesc.getParameter(key);
            }
        }
        if (param == null && getServiceDescription() != null) {
            ServiceDescription serviceDesc = getServiceDescription();
            moduleConfig = serviceDesc.getModuleConfig(new QName(moduleName));
            if (moduleConfig != null) {
                param = moduleConfig.getParameter(key);
            }
            if (param == null) {
                param = serviceDesc.getParameter(key);
            }
        }

        if (param == null && getServiceGroupDescription() != null) {
            ServiceGroupDescription serviceDesc = getServiceGroupDescription();
            moduleConfig = serviceDesc.getModuleConfig(new QName(moduleName));
            if (moduleConfig != null) {
                param = moduleConfig.getParameter(key);
            }
            if (param == null) {
                param = serviceDesc.getParameter(key);
            }
        }
        if (param == null) {
            AxisConfiguration baseConfig =
                    configurationContext.getAxisConfiguration();

            moduleConfig = ((AxisConfigurationImpl) baseConfig).getModuleConfig(new QName(moduleName));
            if (moduleConfig != null) {
                param = moduleConfig.getParameter(key);
            }
            if (param == null) {
                param = baseConfig.getParameter(key);
            }
        }
        if (param == null) {
            AxisConfiguration baseConfig = configurationContext.getAxisConfiguration();
            ModuleDescription module = baseConfig.getModule(new QName(moduleName));
            if (module != null) {
                param = module.getParameter(key);
            }
        }
        if (param == null) {
            param = handler.getParameter(key);
        }
        return param;
    }

    /* (non-Javadoc)
    * @see org.apache.axis2.context.AbstractContext#getProperty(java.lang.Object, boolean)
    */

    /**
     * To acess any property set at the run time , a handler can add property to wherever he wants
     * to MesageContext , to OperationContext , to ServiceContext and to ConfigurationContext.
     * This method is to retrive those properties NOT paramters
     *
     * @param key        : property Name
     * @param persistent : need to be persistent even when server re-start
     * @return Object
     */
    public Object getProperty(String key, boolean persistent) {
        // search in MC
        Object obj = super.getProperty(key, persistent);

        //The context hirachy might not have constructed fully, the check should
        //look for the disconnected grandparents
        // Search in Operation Context
        if (operationContext != null && obj == null) {
            obj = operationContext.getProperty(key, persistent);
        }
        //Search in ServiceContext
        if (serviceContext != null && obj == null) {
            obj = serviceContext.getProperty(key, persistent);
        }
        if (serviceGroupContext != null && obj == null) {
            obj = serviceGroupContext.getProperty(key, persistent);
        }
        if (obj == null) {
            // search in Configuration Context
            obj = configurationContext.getProperty(key, persistent);
        }

//
//        if (obj == null
//            && operationContext == null
//            && serviceContext != null) {
//            obj = serviceContext.getProperty(key, persistent);
//        }
//        if (obj == null && operationContext == null) {
//            obj = configurationContext.getProperty(key, persistent);
//        }
        //Search the configurations
//        Parameter param = null;
//        if (obj == null && operationContext != null) {
//            OperationDescription opDesc = operationContext.getAxisOperation();
//            param = opDesc.getParameter(key);
//        }
//        if (param == null && serviceContext != null) {
//            ServiceDescription serviceDesc = serviceContext.getServiceConfig();
//            param = serviceDesc.getParameter(key);
//        }
//        if (param == null && configurationContext != null) {
//            AxisConfiguration baseConfig =
//                configurationContext.getAxisConfiguration();
//            param = baseConfig.getParameter(key);
//        }
//        if (param != null) {
//            obj = param.getValue();
//        }
        return obj;
    }

    /**
     * @return
     */
    public QName getPausedHandlerName() {
        return pausedHandlerName;
    }

    /**
     * @return
     */
    public String getPausedPhaseName() {
        return pausedPhaseName;
    }

    /**
     * @param name
     */
    public void setPausedPhaseName(String name) {
        pausedPhaseName = name;
    }

    /**
     * @return
     */
    public String getSoapAction() {
        return soapAction;
    }

    /**
     * @param string
     */
    public void setSoapAction(String string) {
        soapAction = string;
    }

    /**
     * @return
     */
    public boolean isDoingMTOM() {
        return doingMTOM;
    }

    /**
     * @param b
     */
    public void setDoingMTOM(boolean b) {
        doingMTOM = b;
    }

    /**
     * @return
     */
    public boolean isDoingREST() {
        return doingREST;
    }

    /**
     * @param b
     */
    public void setDoingREST(boolean b) {
        doingREST = b;
    }

    public void setRestThroughPOST(boolean b) {
        doRESTthroughPOST = b;
    }

    public boolean isRestThroughPOST() {
        return doRESTthroughPOST;
    }

    public boolean isSOAP11() {
        return isSOAP11;
    }

    public ServiceGroupContext getServiceGroupContext() {
        return serviceGroupContext;
    }

    public void setServiceGroupContext(ServiceGroupContext serviceGroupContext) {
        this.serviceGroupContext = serviceGroupContext;
    }

    public OperationDescription getOperationDescription() {
        return operationDescription;
    }

    public void setOperationDescription(OperationDescription operationDescription) {
        this.operationDescription = operationDescription;
        this.operationDescName = operationDescription.getName();
        if (operationDescription != null)
            this.operationDescName = operationDescription.getName();
    }

    public ServiceDescription getServiceDescription() {
        return serviceDescription;
    }

    public void setServiceDescription(ServiceDescription serviceDescription) {
        this.serviceDescription = serviceDescription;
        if (serviceDescription != null)
            this.serviceDescName = serviceDescription.getName();
    }

    public ServiceGroupDescription getServiceGroupDescription() {
        return serviceGroupDescription;
    }

    public void setServiceGroupDescription(ServiceGroupDescription serviceGroupDescription) {
        this.serviceGroupDescription = serviceGroupDescription;
        this.serviceGroupDescId = serviceGroupDescription.getServiceGroupName();
        if (serviceGroupDescription != null)
            this.serviceGroupDescId = serviceGroupDescription.getServiceGroupName();
    }

    public String getServiceGroupContextId() {
        return serviceGroupContextId;
    }

    public void setServiceGroupContextId(String serviceGroupContextId) {
        this.serviceGroupContextId = serviceGroupContextId;
    }


}
