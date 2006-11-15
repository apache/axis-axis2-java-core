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

package org.apache.axis2.client;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.*;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.async.AsyncResult;
import org.apache.axis2.client.async.Callback;
import org.apache.axis2.context.*;
import org.apache.axis2.description.*;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.engine.ListenerManager;
import org.apache.axis2.engine.MessageReceiver;
import org.apache.axis2.i18n.Messages;
import org.apache.axis2.util.CallbackReceiver;
import org.apache.axis2.wsdl.WSDLConstants;

import javax.wsdl.Definition;
import javax.xml.namespace.QName;
import java.net.URL;
import java.util.ArrayList;

/**
 * Client access to a service. Each instance of this class is associated with a
 * particular {@link org.apache.axis2.description.AxisService}, and the methods
 * support operations using that service. {@link Options} instances are used to
 * configure various aspects of the service access.
 */
public class ServiceClient {

    /**
     * Base name used for a service created without an existing configuration.
     */
    public static final String ANON_SERVICE = "anonService";

    /**
     * Operation name used for an anonymous out-only operation (meaning we send
     * a message with no response allowed from the service, equivalent to a WSDL
     * In-Only operation).
     */
    public static final QName ANON_OUT_ONLY_OP = new QName(
            "anonOutonlyOp");

    /**
     * Operation name used for an anonymous robust-out-only operation
     * (meaning we send a message, with the only possible response a fault,
     * equivalent to a WSDL Robust-In-Only operation).
     */
    public static final QName ANON_ROBUST_OUT_ONLY_OP = new QName(
            "anonRobustOp");

    /**
     * Operation name used for an anonymous in-out operation (meaning we sent a
     * message and receive a response, equivalent to a WSDL In-Out operation).
     */
    public static final QName ANON_OUT_IN_OP = new QName("anonOutInOp");

    // the meta-data of the service that this client access
    private AxisService axisService;

    // the configuration in which my meta-data lives
    private AxisConfiguration axisConfig;

    // the configuration context in which I live
    private ConfigurationContext configContext;

    // service context for this specific service instance
    private ServiceContext serviceContext;

    // client options for this service interaction
    private Options options = new Options();

    // options that must override those of the child operation client also
    private Options overrideOptions;

    // list of headers to be sent with the simple APIs
    private ArrayList headers;

    //whether we create configctx or not
    private boolean createConfigCtx;

    /**
     * Create a service client configured to work with a specific AxisService.
     * If this service is already in the world that's handed in (in the form of
     * a ConfigurationContext) then I will happily work in it. If not I will
     * create a small little virtual world and live there.
     *
     * @param configContext The configuration context under which this service lives (may
     *                      be null, in which case a new local one will be created)
     * @param axisService   The service for which this is the client (may be
     *                      <code>null</code>, in which case an anonymous service will be created)
     * @throws AxisFault if something goes wrong while creating a config context (if
     *                   needed)
     */
    public ServiceClient(ConfigurationContext configContext,
                         AxisService axisService) throws AxisFault {
        configureServiceClient(configContext, axisService);
    }

    private void configureServiceClient(ConfigurationContext configContext, AxisService axisService) throws AxisFault {
        initializeTransports(configContext);
        // save the axisConfig and service
        this.axisConfig = this.configContext.getAxisConfiguration();
        if (axisService != null) {
            this.axisService = axisService;
        } else {
            this.axisService = createAnonymousService();
        }
        if (this.axisConfig.getService(this.axisService.getName()) == null) {
            this.axisService.setClientSide(true);
            this.axisConfig.addService(this.axisService);
        } else {
            throw new AxisFault(Messages.getMessage(
                    "twoservicecannothavesamename",
                    this.axisService.getName()));
        }
        AxisServiceGroup axisServiceGroup = (AxisServiceGroup) this.axisService.getParent();
        ServiceGroupContext sgc = new ServiceGroupContext(this.configContext,
                axisServiceGroup);
        this.serviceContext = sgc.getServiceContext(this.axisService);
    }


    /**
     * This is WSDL4J based constructor to configure the Service Client/
     * We are going to make this policy aware
     *
     * @param configContext
     * @param wsdl4jDefinition
     * @param wsdlServiceName
     * @param portName
     * @throws AxisFault
     */

    public ServiceClient(ConfigurationContext configContext, Definition wsdl4jDefinition,
                         QName wsdlServiceName, String portName) throws AxisFault {
        configureServiceClient(configContext, AxisService.createClientSideAxisService(
                wsdl4jDefinition, wsdlServiceName, portName, options));
    }

    /**
     * Create a service client for WSDL service identified by the QName of the
     * wsdl:service element in a WSDL document.
     *
     * @param configContext   The configuration context under which this service lives (may
     *                        be <code>null</code>, in which case a new local one will be created) *
     * @param wsdlURL         The URL of the WSDL document to read
     * @param wsdlServiceName The QName of the WSDL service in the WSDL document to create a
     *                        client for
     * @param portName        The name of the WSDL 1.1 port to create a client for. May be
     *                        null (if WSDL 2.0 is used or if only one port is there). .
     * @throws AxisFault if something goes wrong while creating a config context (if
     *                   needed)
     */
    public ServiceClient(ConfigurationContext configContext, URL wsdlURL,
                         QName wsdlServiceName, String portName) throws AxisFault {
        configureServiceClient(configContext, AxisService.createClientSideAxisService(wsdlURL,
                wsdlServiceName, portName, options));
    }

    private void initializeTransports(ConfigurationContext configContext) throws AxisFault {
        ListenerManager transportManager;
        if (configContext != null) {
            this.configContext = configContext;
            transportManager = configContext.getListenerManager();
            if (transportManager == null) {
                transportManager = new ListenerManager();
                transportManager.init(this.configContext);
            }
        } else {
            if (ListenerManager.defaultConfigurationContext == null) {
                this.configContext = ConfigurationContextFactory.
                        createConfigurationContextFromFileSystem(null, null);
                transportManager = new ListenerManager();
                transportManager.init(this.configContext);
                createConfigCtx = true;
            } else {
                this.configContext = ListenerManager.defaultConfigurationContext;
            }
        }
    }

    /**
     * Create a service client by assuming an anonymous service and any other
     * necessary information.
     *
     * @throws AxisFault
     */
    public ServiceClient() throws AxisFault {
        this(null, null);
    }

    /**
     * Create an anonymous axisService with one (anonymous) operation for each
     * MEP that we support dealing with anonymously using the convenience APIs.
     *
     * @return the minted anonymous service
     */
    private AxisService createAnonymousService() {
        // now add anonymous operations to the axis2 service for use with the
        // shortcut client API. NOTE: We only add the ones we know we'll use
        // later in the convenience API; if you use
        // this constructor then you can't expect any magic!
        AxisService axisService = new AxisService(ANON_SERVICE + this.hashCode() + System.currentTimeMillis());
        RobustOutOnlyAxisOperation robustoutoonlyOperation = new RobustOutOnlyAxisOperation(
                ANON_ROBUST_OUT_ONLY_OP);
        axisService.addOperation(robustoutoonlyOperation);

        OutOnlyAxisOperation outOnlyOperation = new OutOnlyAxisOperation(
                ANON_OUT_ONLY_OP);
        axisService.addOperation(outOnlyOperation);

        OutInAxisOperation outInOperation = new OutInAxisOperation(
                ANON_OUT_IN_OP);
        axisService.addOperation(outInOperation);
        return axisService;
    }

    /**
     * Return the AxisService this is a client for. This is primarily useful
     * when the AxisService is created anonymously or from WSDL as otherwise the
     * user had the AxisService to start with.
     *
     * @return the axisService
     */
    public AxisService getAxisService() {
        return axisService;
    }

    /**
     * Set the basic client configuration related to this service interaction.
     *
     * @param options (non-<code>null</code>)
     */
    public void setOptions(Options options) {
        this.options = options;
    }

    /**
     * Get the basic client configuration from this service interaction.
     *
     * @return options
     */
    public Options getOptions() {
        return options;
    }

    /**
     * Set a client configuration to override the normal options used by an
     * operation client. Any values set in this configuration will be used for
     * each client, with the standard values for the client still used for any
     * values not set in the override configuration.
     *
     * @param overrideOptions
     */
    public void setOverrideOptions(Options overrideOptions) {
        this.overrideOptions = overrideOptions;
    }

    /**
     * Get the client configuration used to override the normal options set by
     * an operation client.
     *
     * @return override options
     */
    public Options getOverrideOptions() {
        return overrideOptions;
    }

    /**
     * Engage a module for this service client.
     *
     * @param moduleName name of the module to engage
     * @throws AxisFault if something goes wrong
     */
    public void engageModule(QName moduleName) throws AxisFault {
        AxisModule module = axisConfig.getModule(moduleName);
        if (module != null) {
            axisService.engageModule(module, axisConfig);
        } else {
            throw new AxisFault("Unable to engage module : " +
                    moduleName.getLocalPart());
        }
    }

    /**
     * Disengage a module for this service client
     *
     * @param moduleName
     */
    public void disengageModule(QName moduleName) {
        AxisModule module = axisConfig.getModule(moduleName);
        if (module != null) {
            axisService.disengageModule(module);
        }
    }

    /**
     * Add an arbitrary XML element as a header to be sent with outgoing
     * messages.
     *
     * @param header header to be sent (non-<code>null</code>)
     */
    public void addHeader(OMElement header) {
        if (headers == null) {
            headers = new ArrayList();
        }
        headers.add(header);
    }

    /**
     * Add SOAP Header to be sent with outgoing messages.
     *
     * @param header header to be sent (non-<code>null</code>)
     */
    public void addHeader(SOAPHeaderBlock header) {
        if (headers == null) {
            headers = new ArrayList();
        }
        headers.add(header);
    }

    /**
     * Remove all headers for outgoing message.
     */
    public void removeHeaders() {
        if (headers != null) {
            headers.clear();
        }
    }


    /**
     * Add a simple header consisting of some text (and a header name; duh) to
     * be sent with interactions.
     *
     * @param headerName
     * @param headerText
     * @throws AxisFault
     */
    public void addStringHeader(QName headerName, String headerText) throws AxisFault {
        if (headerName.getNamespaceURI() == null || "".equals(headerName.getNamespaceURI())) {
            throw new AxisFault("Failed to add string header , you have to have namespaceURI for the QName");
        }
        OMElement omElement = OMAbstractFactory.getOMFactory().createOMElement(
                headerName, null);
        omElement.setText(headerText);
        addHeader(omElement);
    }

    /**
     * Directly invoke an anonymous operation with a Robust In-Only MEP. This
     * method just sends your supplied XML and possibly receives a fault. For
     * more control, you can instead create a client for the operation and use
     * that client to execute the send.
     *
     * @param elem XML to send
     * @throws AxisFault if something goes wrong while sending, or if a fault is
     *                   received in response (per the Robust In-Only MEP).
     * @see #createClient(QName)
     */
    public void sendRobust(OMElement elem) throws AxisFault {
        sendRobust(ANON_ROBUST_OUT_ONLY_OP, elem);
    }

    /**
     * Directly invoke a named operation with a Robust In-Only MEP. This method
     * just sends your supplied XML and possibly receives a fault. For more
     * control, you can instead create a client for the operation and use that
     * client to execute the send.
     *
     * @param operation name of operation to be invoked (non-<code>null</code>)
     * @param elem      XML to send
     * @throws AxisFault if something goes wrong while sending it or if a fault is
     *                   received in response (per the Robust In-Only MEP).
     * @see #createClient(QName)
     */
    public void sendRobust(QName operation, OMElement elem) throws AxisFault {
        if (options.isUseSeparateListener()) {

            // This mean doing a Fault may come through a different channel .
            // If the
            // transport is two way transport (e.g. http) Only one channel is
            // used (e.g. in http cases
            // 202 OK is sent to say no response available). Axis2 get blocked
            // return when the response is available.
            SyncCallBack callback = new SyncCallBack();

            // this method call two channel non blocking method to do the work
            // and wait on the callback
            sendReceiveNonBlocking(operation, elem, callback);

            long timeout = options.getTimeOutInMilliSeconds();
            long waitTime = timeout;
            long startTime = System.currentTimeMillis();

            synchronized (callback) {
                while (! callback.isComplete() && waitTime >= 0) {
                    try {
                        callback.wait(timeout);
                    } catch (InterruptedException e) {
                        // We were interrupted for some reason, keep waiting
                        // or throw new AxisFault( "Callback was interrupted by someone?" );
                    }
                    // The wait finished, compute remaining time
                    // - wait can end prematurely, see Object.wait( int timeout )
                    waitTime = timeout - (System.currentTimeMillis() - startTime);
                }

            }
            SOAPEnvelope envelope = callback.envelope;
            // process the result of the invocation
            if (envelope != null) {
                // building soap envelope
                envelope.build();
                // closing transport
                if (envelope.getBody().hasFault()) {
                    SOAPFault soapFault = envelope.getBody().getFault();
                    throw new AxisFault(soapFault.getCode(), soapFault.getReason(),
                            soapFault.getNode(), soapFault.getRole(), soapFault.getDetail());
                }
            } else {
                if (callback.error instanceof AxisFault) {
                    throw (AxisFault) callback.error;
                } else if (callback.error != null) {
                    throw new AxisFault(callback.error);
                } else if (! callback.isComplete()) {
                    //no exception has occurred
                }
            }
        } else {
            MessageContext mc = new MessageContext();
            fillSOAPEnvelope(mc, elem);
            OperationClient mepClient = createClient(operation);
            mepClient.addMessageContext(mc);
            mepClient.execute(true);
        }
    }

    /**
     * Directly invoke an anonymous operation with an In-Only MEP. This method
     * just sends your supplied XML without the possibility of any response from
     * the service (even an error - though you can still get client-side errors
     * such as "Host not found"). For more control, you can instead create a
     * client for the operation and use that client to execute the send.
     *
     * @param elem XML to send
     * @throws AxisFault ff something goes wrong trying to send the XML
     * @see #createClient(QName)
     */
    public void fireAndForget(OMElement elem) throws AxisFault {
        fireAndForget(ANON_OUT_ONLY_OP, elem);
    }

    /**
     * Directly invoke a named operation with an In-Only MEP. This method just
     * sends your supplied XML without the possibility of any response from the
     * service (even an error - though you can still get client-side errors such
     * as "Host not found"). For more control, you can instead create a client
     * for the operation and use that client to execute the send.
     *
     * @param operation name of operation to be invoked (non-<code>null</code>)
     * @param elem      XML to send
     * @throws AxisFault if something goes wrong trying to send the XML
     * @see #createClient(QName)
     */
    public void fireAndForget(QName operation, OMElement elem) throws AxisFault {
        // look up the appropriate axisop and create the client
        OperationClient mepClient = createClient(operation);
        // create a message context and put the payload in there along with any
        // headers
        MessageContext mc = new MessageContext();
        fillSOAPEnvelope(mc, elem);
        // add the message context there and have it go
        mepClient.addMessageContext(mc);
        mepClient.execute(false);
    }

    /**
     * Directly invoke an anonymous operation with an In-Out MEP. This method
     * sends your supplied XML and receives a response. For more control, you
     * can instead create a client for the operation and use that client to
     * execute the exchange.
     *
     * @param elem
     * @return response
     * @throws AxisFault
     * @see #createClient(QName)
     */
    public OMElement sendReceive(OMElement elem) throws AxisFault {
        return sendReceive(ANON_OUT_IN_OP, elem);
    }

    /**
     * Directly invoke a named operationQName with an In-Out MEP. This method sends
     * your supplied XML and receives a response. For more control, you can
     * instead create a client for the operationQName and use that client to execute
     * the exchange.
     *
     * @param operationQName name of operationQName to be invoked (non-<code>null</code>)
     * @param xmlPayload
     * @return response
     * @throws AxisFault
     */
    public OMElement sendReceive(QName operationQName, OMElement xmlPayload)
            throws AxisFault {
        if (options.isUseSeparateListener()) {

            // Here we are trying to do a request-response invocation using two different channels for the request
            // and the response.
            // For example, if the IN and OUT transports are HTTP, then two different HTTP channels will be used. The first
            // channel will be used to send the request, which the server respond sending HTTP 200, if accepted and uses
            // a completely different channel to send the response. This flag, informs the Axis2 client engine to
            // keep listeners ready to receive the response.

            // even if the client is blocking we use a Callback, internally, to relate the response back to the client.
            SyncCallBack callback = new SyncCallBack();

            // this method call two channel non blocking method to do the work
            // and wait on the callback
            sendReceiveNonBlocking(operationQName, xmlPayload, callback);

            long timeout = options.getTimeOutInMilliSeconds();
            long waitTime = timeout;
            long startTime = System.currentTimeMillis();

            synchronized (callback) {
                while (! callback.isComplete() && waitTime >= 0) {
                    try {
                        callback.wait(timeout);
                    } catch (InterruptedException e) {
                        // We were interrupted for some reason, keep waiting
                        // or throw new AxisFault( "Callback was interrupted by someone?" );
                    }
                    // The wait finished, compute remaining time
                    // - wait can end prematurely, see Object.wait( int timeout )
                    waitTime = timeout - (System.currentTimeMillis() - startTime);
                }

            }
            // process the result of the invocation
            if (callback.envelope != null) {
                // transport was already returned by the call back receiver
                //Building of the Envelope should happen at the setComplete()
                // or onComplete() methods of the Callback class
                return callback.envelope.getBody().getFirstElement();
            } else {
                if (callback.error instanceof AxisFault) {
                    throw (AxisFault) callback.error;
                } else if (callback.error != null) {
                    throw new AxisFault(callback.error);
                } else if (! callback.isComplete()) {
                    throw new AxisFault(Messages.getMessage("responseTimeOut"));
                } else
                    throw new AxisFault(Messages.getMessage("callBackCompletedWithError"));
            }
        } else {
            MessageContext messageContext = new MessageContext();
            fillSOAPEnvelope(messageContext, xmlPayload);
            OperationClient operationClient = createClient(operationQName);
            operationClient.addMessageContext(messageContext);
            operationClient.execute(true);
            MessageContext response = operationClient
                    .getMessageContext(WSDLConstants.MESSAGE_LABEL_IN_VALUE);
            return response.getEnvelope().getBody().getFirstElement();
        }
    }

    /**
     * Directly invoke an anonymous operation with an In-Out MEP without waiting
     * for a response. This method sends your supplied XML with response
     * notification to your callback handler. For more control, you can instead
     * create a client for the operation and use that client to execute the
     * exchange.
     *
     * @param elem
     * @param callback
     * @throws AxisFault
     * @see #createClient(QName)
     */
    public void sendReceiveNonBlocking(OMElement elem, Callback callback)
            throws AxisFault {
        sendReceiveNonBlocking(ANON_OUT_IN_OP, elem, callback);
    }

    /**
     * Directly invoke a named operation with an In-Out MEP without waiting for
     * a response. This method sends your supplied XML with response
     * notification to your callback handler. For more control, you can instead
     * create a client for the operation and use that client to execute the
     * exchange.
     *
     * @param operation name of operation to be invoked (non-<code>null</code>)
     * @param elem
     * @param callback
     * @throws AxisFault
     * @see #createClient(QName)
     */
    public void sendReceiveNonBlocking(QName operation, OMElement elem,
                                       Callback callback) throws AxisFault {
        MessageContext mc = new MessageContext();
        fillSOAPEnvelope(mc, elem);
        OperationClient mepClient = createClient(operation);
        // here a blocking invocation happens in a new thread, so the
        // progamming model is non blocking
        mepClient.setCallback(callback);
        mepClient.addMessageContext(mc);
        if (options.isUseSeparateListener()) {
            MessageReceiver messageReceiver = axisService.getOperation(operation).getMessageReceiver();
            if (messageReceiver == null || !(messageReceiver instanceof CallbackReceiver)) {
                CallbackReceiver callbackReceiver = new CallbackReceiver();
                axisService.getOperation(operation).setMessageReceiver(callbackReceiver);
            }
        }
        mepClient.execute(false);
    }

    /**
     * Create an operation client with the appropriate message exchange pattern
     * (MEP). This method creates a full-function MEP client which can be used
     * to exchange messages for a specific operation. It configures the
     * constructed operation client to use the current normal and override
     * options. This method is used internally, and also by generated client
     * stub code.
     *
     * @param operationQName qualified name of operation (local name is operation
     *                       name, namespace URI is just the empty string)
     * @return client configured to talk to the given operation
     * @throws AxisFault if the operation is not found
     */
    public OperationClient createClient(QName operationQName) throws AxisFault {
        AxisOperation axisOperation = axisService.getOperation(operationQName);
        if (axisOperation == null) {
            throw new AxisFault(Messages
                    .getMessage("operationnotfound", operationQName.getLocalPart()));
        }

        OperationClient operationClient = axisOperation.createClient(serviceContext, options);

        // if overide options have been set, that means we need to make sure
        // those options override the options of even the operation client. So,
        // what we do is switch the parents around to make that work.
        if (overrideOptions != null) {
            overrideOptions.setParent(operationClient.getOptions());
            operationClient.setOptions(overrideOptions);
        }
        return operationClient;
    }

    /**
     * Return the SOAP factory to use depending on what options have been set.
     * If the SOAP version can not be seen in the options, version 1.1 is the
     * default.
     *
     * @return the SOAP factory
     * @see Options#setSoapVersionURI(String)
     */
    private SOAPFactory getSOAPFactory() {
        String soapVersionURI = options.getSoapVersionURI();
        if (SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI.equals(soapVersionURI)) {
            return OMAbstractFactory.getSOAP12Factory();
        } else {
            // make the SOAP 1.1 the default SOAP version
            return OMAbstractFactory.getSOAP11Factory();
        }
    }

    /**
     * Prepare a SOAP envelope with the stuff to be sent.
     *
     * @param messageContext the message context to be filled
     * @param xmlPayload     the payload content
     * @throws AxisFault if something goes wrong
     */
    private void fillSOAPEnvelope(MessageContext messageContext, OMElement xmlPayload)
            throws AxisFault {
        messageContext.setServiceContext(serviceContext);
        SOAPFactory soapFactory = getSOAPFactory();
        SOAPEnvelope envelope = soapFactory.getDefaultEnvelope();
        if (xmlPayload != null) {
            envelope.getBody().addChild(xmlPayload);
        }
        addHeadersToEnvelope(envelope);
        messageContext.setEnvelope(envelope);
    }


    /**
     * Add all configured headers to a SOAP envelope.
     *
     * @param envelope
     */
    public void addHeadersToEnvelope(SOAPEnvelope envelope) {
        if (headers != null) {
            SOAPHeader soapHeader = envelope.getHeader();
            for (int i = 0; i < headers.size(); i++) {
                soapHeader.addChild((OMElement) headers.get(i));
            }
        }
    }


    /**
     * Get the endpoint reference for this client using a particular transport.
     *
     * @param transport transport name (non-<code>null</code>)
     * @return local endpoint
     * @throws AxisFault
     */
    public EndpointReference getMyEPR(String transport) throws AxisFault {
        return serviceContext.getMyEPR(transport);
    }

    /**
     * Get the endpoint reference for the service.
     *
     * @return service endpoint
     */
    public EndpointReference getTargetEPR() {
        return serviceContext.getTargetEPR();
    }

    /**
     * Set the endpoint reference for the service.
     *
     * @param targetEpr
     */
    public void setTargetEPR(EndpointReference targetEpr) {
        serviceContext.setTargetEPR(targetEpr);
        options.setTo(targetEpr);
    }


    /**
     * This class acts as a callback that allows users to wait on the result.
     */
    private class SyncCallBack extends Callback {
        private SOAPEnvelope envelope;

        private MessageContext msgctx;

        private Exception error;

        public void onComplete(AsyncResult result) {
            this.envelope = result.getResponseEnvelope();
            // Transport input stream gets closed after calling setComplete
            // method. Have to build the whole envelope including the
            // attachments at this stage. Data might get lost if the input
            // stream gets closed before building the whole envelope.
            this.envelope.buildWithAttachments();
            this.msgctx = result.getResponseMessageContext();
        }

        public void setComplete(boolean complete) {
            super.setComplete(complete);
            synchronized (this) {
                notify();
            }
        }

        public void onError(Exception e) {
            error = e;
        }

        public MessageContext getMsgctx() {
            return msgctx;
        }
    }

    /**
     * Get the service context.
     *
     * @return context
     */
    public ServiceContext getServiceContext() {
        return serviceContext;
    }

    protected void finalize() throws Throwable {
        super.finalize();
        cleanup();
    }

    /**
     * Clean up configuration created with this client. Call this method when
     * you're done using the client, in order to discard any associated
     * resources.
     *
     * @throws AxisFault
     */
    public void cleanup() throws AxisFault {
        // if a configuration context was created for this client there'll also
        //  be a service group, so discard that
        if (!createConfigCtx) {
            String serviceGroupName = ((AxisServiceGroup) axisService.getParent()).getServiceGroupName();
            AxisConfiguration axisConfiguration = configContext.getAxisConfiguration();
            AxisServiceGroup asg = axisConfiguration.getServiceGroup(serviceGroupName);
            if (asg != null) {
                axisConfiguration.removeServiceGroup(serviceGroupName);
            }
        } else {
            //Stopping listeners if you start any
            configContext.getListenerManager().stop();
        }
    }
}
