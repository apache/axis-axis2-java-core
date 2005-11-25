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
import org.apache.axis2.addressing.RelatesTo;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.AxisServiceGroup;
import org.apache.axis2.description.HandlerDescription;
import org.apache.axis2.description.ModuleConfiguration;
import org.apache.axis2.description.ModuleDescription;
import org.apache.axis2.description.Parameter;
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
 * MessageContext holds service specific state information. 
 */
public class MessageContext extends AbstractContext {

    public static final String TRANSPORT_HEADERS = "TRANSPORT_HEADERS";
    /**
     * Field TRANSPORT_OUT
     */
    public static final String TRANSPORT_OUT = "TRANSPORT_OUT";

    /**
     * Field TRANSPORT_IN
     */
    public static final String TRANSPORT_IN = "TRANSPORT_IN";

    /**
     * Field  CHARACTER_SET_ENCODING
     */
    public static final String CHARACTER_SET_ENCODING =
            "CHARACTER_SET_ENCODING";

    /**
     * Field UTF_8.
     * This is the 'utf-8' value for CHARACTER_SET_ENCODING property.
     */
    public static final String UTF_8 = "UTF-8";

    /**
     * Field UTF_16.
     * This is the 'utf-16' value for CHARACTER_SET_ENCODING property.
     */
    public static final String UTF_16 = "utf-16";

    /**
     * Field DEFAULT_CHAR_SET_ENCODING.
     * This is the default value for CHARACTER_SET_ENCODING property.
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

    private transient AxisOperation axisOperation;
    private transient AxisService axisService;
    private transient AxisServiceGroup axisServiceGroup;
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

    String serviceGroupId = null;

    QName serviceDescName = null;

    QName axisOperationName = null;

    /**
     * Initializes Axis Engine Context.
     *
     * @throws AxisFault
     */
    public void init(AxisConfiguration axisConfiguration) throws AxisFault {
        if (transportInName != null)
            transportIn = axisConfiguration.getTransportIn(transportInName);
        if (transportOutname != null)
            transportOut = axisConfiguration.getTransportOut(transportOutname);
        if (serviceGroupId != null)
            axisServiceGroup = axisConfiguration.getServiceGroup(serviceGroupId);
        if (serviceDescName != null)
            axisService = axisConfiguration.getService(serviceDescName.getLocalPart());
        if (axisOperationName != null)
            axisOperation = axisService.getOperation(axisOperationName);
    }

    /**
     * Convenience Constructor. Before calling engine.send() or  engine.receive(), one must send 
     * transport in/out
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
     * @return Returns EndpointReference. 
     */
    public EndpointReference getFaultTo() {
        return messageInformationHeaders.getFaultTo();
    }

    /**
     * @return Returns EndpointReference.
     */
    public EndpointReference getFrom() {
        return messageInformationHeaders.getFrom();
    }

    /**
     * @return Returns boolean.
     */
    public boolean isInFaultFlow() {
        return inFaultFlow;
    }

    /**
     * @return Returns SOAPEnvelope.
     */
    public SOAPEnvelope getEnvelope() {
        return envelope;
    }

    /**
     * @return Returns message id.
     */
    public String getMessageID() {
        return messageInformationHeaders.getMessageId();
    }

    /**
     * @return Returns boolean.
     */
    public boolean isProcessingFault() {
        return processingFault;
    }

    /**
     * @return Returns RelatesTo.
     */
    public RelatesTo getRelatesTo() {
        return messageInformationHeaders.getRelatesTo();
    }

    /**
     * @return  Returns EndpointReference.
     */
    public EndpointReference getReplyTo() {
        return messageInformationHeaders.getReplyTo();
    }

    /**
     * @return Returns boolean.
     */
    public boolean isResponseWritten() {
        return responseWritten;
    }

    /**
     * @return Returns boolean.
     */
    public boolean isServerSide() {
        return serverSide;
    }

    /**
     * @return  Returns SessionContext.
     */
    public SessionContext getSessionContext() {
        return sessionContext;
    }

    /**
     * @return Returns EndpointReference.
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
     * @return Returns boolean.
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
     * @return Returns boolean.
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
            this.setAxisOperation(operationContext.getAxisOperation());
        }
    }

    /**
     * @return Returns boolean.
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
     * Sets the service context id.
     * @param serviceContextID
     */
    public void setServiceContextID(String serviceContextID) {
        this.serviceContextID = serviceContextID;
    }

    public ConfigurationContext getSystemContext() {
        return configurationContext;
    }

    /**
     * @return Returns ServiceContext.
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
        this.setAxisService(context.getAxisService());
    }

    /**
     * @param collection
     */
    public void setMessageInformationHeaders(MessageInformationHeaders collection) {
        messageInformationHeaders = collection;
    }


    /**
     * Retrieves configuration descriptor parameters at any level. The order of search is
     * as follows: 
     * <ol>
     * <li> Search in operation description if it exists </li>
     * <li> If parameter is not found or if operationContext is null, search in
     * AxisService </li>
     * <li> If parameter is not found or if axisService is null, search in
     * AxisConfiguration </li>
     * </ol>
     * @param key
     * @return Parameter <code>Parameter</code>
     */
    public Parameter getParameter(String key) {
        Parameter param = null;
        if (getAxisOperation() != null) {
            AxisOperation opDesc = getAxisOperation();
            param = opDesc.getParameter(key);
            if(param !=null){
                return param;
            }
        }
        if (getAxisService() != null) {
            AxisService axisService = getAxisService();
            param = axisService.getParameter(key);
            if(param !=null){
                return param;
            }
        }
        if (getAxisServiceGroup() != null) {
            AxisServiceGroup axisServiceDesc = getAxisServiceGroup();
            param = axisServiceDesc.getParameter(key);
            if(param !=null){
                return param;
            }
        }
        if (configurationContext != null) {
            AxisConfiguration baseConfig =
                    configurationContext.getAxisConfiguration();
            param = baseConfig.getParameter(key);
        }
        return param;
    }


    /**
     * Retrieves both module specific configuration parameters as well as other parameters. 
     * The order of search is as follows:
     * <ol>
     * <li> Search in module configurations inside corresponding operation descripton if its there </li>
     * <li> Search in corresponding operation if its there </li>
     * <li> Search in module configurations inside corresponding service description if its there </li>
     * <li> Next search in Corresponding Service description if its there </li>
     * <li> Next search in module configurations inside axisConfiguration </li>
     * <li> Search in AxisConfiguration for parameters </li>
     * <li> Next get the corresponding module and search for the parameters </li>
     * <li> Search in HandlerDescription for the parameter </li>
     * </ol>
     * <p/>
     * and the way of specifing module configuration is as follows
     * <moduleConfig name="addressing">
     * <parameter name="addressingPara" locked="false">N/A</parameter>
     * </moduleConfig>
     *
     * @param key        : Parameter Name
     * @param moduleName : Name of the module
     * @param handler    <code>HandlerDescription</code>
     * @return Parameter <code>Parameter</code>
     */
    public Parameter getModuleParameter(String key, String moduleName, HandlerDescription handler) {
        Parameter param = null;
        ModuleConfiguration moduleConfig = null;
        if (getAxisOperation() != null) {
            AxisOperation opDesc = getAxisOperation();
            moduleConfig = opDesc.getModuleConfig(new QName(moduleName));
            if (moduleConfig != null) {
                param = moduleConfig.getParameter(key);
                if(param !=null){
                    return param;
                } else {
                    param = opDesc.getParameter(key);
                    if(param !=null){
                        return param;
                    }
                }
            }
        }
        if (getAxisService() != null) {
            AxisService axisService = getAxisService();
            moduleConfig = axisService.getModuleConfig(new QName(moduleName));
            if (moduleConfig != null) {
                param = moduleConfig.getParameter(key);
                if(param !=null){
                    return param;
                } else {
                    param = axisService.getParameter(key);
                    if(param !=null){
                        return param;
                    }
                }
            }
        }
        if (getAxisServiceGroup() != null) {
            AxisServiceGroup axisServiceDesc = getAxisServiceGroup();
            moduleConfig = axisServiceDesc.getModuleConfig(new QName(moduleName));
            if (moduleConfig != null) {
                param = moduleConfig.getParameter(key);
                if(param !=null){
                    return param;
                } else {
                    param = axisServiceDesc.getParameter(key);
                    if(param !=null){
                        return param;
                    }
                }
            }
        }
        AxisConfiguration baseConfig = configurationContext.getAxisConfiguration();
        moduleConfig = ((AxisConfigurationImpl) baseConfig).getModuleConfig(new QName(moduleName));
        if (moduleConfig != null) {
            param = moduleConfig.getParameter(key);
            if(param !=null){
                return param;
            } else {
                param = baseConfig.getParameter(key);
                if(param !=null){
                    return param;
                }
            }
        }
        ModuleDescription module = baseConfig.getModule(new QName(moduleName));
        if (module != null) {
            param = module.getParameter(key);
            if(param !=null){
                return param;
            }
        }
        param = handler.getParameter(key);
        return param;
    }

    /**
     * Retrieves a property. The order of search is as follows:
     *
     * <ol>
     * <li> Search in OperationContext, </li>
     * <li> If OperationContext is null or if property is not found, search in ServiceContext,</li>
     * <li> If ServiceContext is null or if property is not found, search in ServiceGroupContext,</li>
     * <li> If ServiceGroupContext is null or if property is not found, search in ConfigurationContext.</li>
     * </ol>
     * 
     * @param key        property Name
     * @param persistent 
     * @return Object
     */
    public Object getProperty(String key, boolean persistent) {
        // search in MC
        Object obj = super.getProperty(key, persistent);
        if(obj !=null){
            return obj;
        }
        //The context hirachy might not have constructed fully, the check should
        //look for the disconnected grandparents
        // Search in Operation Context
        if (operationContext != null ) {
            obj = operationContext.getProperty(key, persistent);
            if(obj !=null){
                return obj;
            }
        }
        //Search in ServiceContext
        if (serviceContext != null ) {
            obj = serviceContext.getProperty(key, persistent);
            if(obj !=null){
                return obj;
            }
        }
        if (serviceGroupContext != null ) {
            obj = serviceGroupContext.getProperty(key, persistent);
            if(obj !=null){
                return obj;
            }
        }
        if (configurationContext != null ) {
            // search in Configuration Context
            obj = configurationContext.getProperty(key, persistent);
            if(obj !=null){
                return obj;
            }
        }
        return obj;
    }

    /**
     * @return Returns QName.
     */
    public QName getPausedHandlerName() {
        return pausedHandlerName;
    }

    /**
     * @return Returns paused phase name.
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
     * @return Returns soap action.
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
     * @return Returns boolean.
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
     * @return Returns boolean.
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

    public AxisOperation getAxisOperation() {
        return axisOperation;
    }

    public void setAxisOperation(AxisOperation axisOperation) {
        this.axisOperation = axisOperation;
        this.axisOperationName = axisOperation.getName();
        if (axisOperation != null)
            this.axisOperationName = axisOperation.getName();
    }

    public AxisService getAxisService() {
        return axisService;
    }

    public void setAxisService(AxisService axisService) {
        this.axisService = axisService;
        if (axisService != null)
            this.serviceDescName = axisService.getName();
    }

    public AxisServiceGroup getAxisServiceGroup() {
        return axisServiceGroup;
    }

    public void setAxisServiceGroup(AxisServiceGroup axisServiceGroup) {
        if (axisServiceGroup != null) {
            this.serviceGroupId = axisServiceGroup.getServiceGroupName();
            this.axisServiceGroup = axisServiceGroup;
        }
    }

    public String getServiceGroupContextId() {
        return serviceGroupContextId;
    }

    public void setServiceGroupContextId(String serviceGroupContextId) {
        this.serviceGroupContextId = serviceGroupContextId;
    }


}
