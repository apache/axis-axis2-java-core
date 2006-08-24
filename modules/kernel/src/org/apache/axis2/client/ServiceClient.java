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
 * A ServiceClient class is used to create a client for a service. More details
 * need to be explained here.
 */
public class ServiceClient {

    // service and operation names used for anonymous services and operations
    public static final String ANON_SERVICE = "annonService";

    public static final QName ANON_OUT_ONLY_OP = new QName(
            "annonOutonlyOp");

    public static final QName ANON_ROBUST_OUT_ONLY_OP = new QName(
            "annonRobustOp");

    public static final QName ANON_OUT_IN_OP = new QName("annonOutInOp");

    // the metadata for the service that I'm clienting for
    private AxisService axisService;

    // the configuration in which my metadata lives
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

    //whther we creat configctx or not
    private boolean createConfigCtx;

    /**
     * Create a service client configured to work with a specific AxisService.
     * If this service is already in the world that's handed in (in the form of
     * a ConfigurationContext) then I will happily work in it. If not I will
     * create a small little virtual world and live there.
     *
     * @param configContext The configuration context under which this service lives (may
     *                      be null, in which case a new local one will be created)
     * @param axisService   The service for which this is the client.
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
     *                        be null, in which case a new local one will be created) *
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
     * Create an anonymous axisService with one (anonymous) operation each for
     * each MEP that I support dealing with anonymously using the convenience
     * APIs.
     *
     * @return the minted anonymous service
     */
    private AxisService createAnonymousService() {
        // now add anonymous operations to the axis2 service for use with the
        // shortcut client API. NOTE: We only add the ones we know we'll use
        // later in the convenience API; if you use
        // this constructor then you can't expect any magic!
        AxisService axisService = new AxisService(ANON_SERVICE + this.hashCode());
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
     * Set the client configuration related to this service interaction.
     *
     * @param options
     */
    public void setOptions(Options options) {
        this.options = options;
    }

    /**
     * Get the client configuration from this service interaction.
     *
     * @return set of options set earlier.
     */
    public Options getOptions() {
        return options;
    }

    /**
     * Set the client configuration related to this service interaction to
     * override any options that the underlying operation client may have.
     */
    public void setOverrideOptions(Options overrideOptions) {
        this.overrideOptions = overrideOptions;
    }

    /**
     * Get the client configuration from this service interaction which have
     * been used to overide operation client options as well.
     *
     * @return set of options set earlier.
     */
    public Options getOverrideOptions() {
        return overrideOptions;
    }

    /**
     * Engage a module for this service client.
     *
     * @param moduleName Name of the module to engage
     * @throws AxisFault if something goes wrong
     */
    public void engageModule(QName moduleName) throws AxisFault {
        axisService.engageModule(axisConfig.getModule(moduleName), axisConfig);
    }

    /**
     * Disengage a module for this service client
     *
     * @param moduleName
     */
    public void disEngageModule(QName moduleName) {
        AxisModule module = axisConfig.getModule(moduleName);
        if (module != null) {
            axisService.disEngageModule(module);
        }
    }

    /**
     * Add an XML element as a header to be sent with interactions. This allows
     * users to go a bit beyond the dirt simple XML in/out pattern using this
     * simplified API. A header
     *
     * @param header The header to be added for interactions. Must not be null.
     */
    public void addHeader(OMElement header) {
        if (headers == null) {
            headers = new ArrayList();
        }
        headers.add(header);
    }

    /**
     * This will let the user to add SOAP Headers to the out going message
     *
     * @param header
     */
    public void addHeader(SOAPHeaderBlock header) {
        if (headers == null) {
            headers = new ArrayList();
        }
        headers.add(header);
    }

    /**
     * To remove all the headers in ServiceClient
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
     * This is a simple client API to invoke a service operation who's MEP is
     * Robust In-Only. This API can be used to simply send a bit of XML and
     * possibly receive a fault. If you need more control over this interaction
     * then you need to create a client (@see createClient()) for the operation
     * and use that instead.
     *
     * @param elem The XML to send
     * @throws AxisFault if something goes wrong while sending it or if a fault is
     *                   received in response (per the Robust In-Only MEP).
     */
    public void sendRobust(OMElement elem) throws AxisFault {
        sendRobust(ANON_ROBUST_OUT_ONLY_OP, elem);
    }

    /**
     * This is a simple client API to invoke a service operation who's MEP is
     * Robust In-Only. This API can be used to simply send a bit of XML and
     * possibly receive a fault under the guise of a specific operation. If you
     * need more control over this interaction then you need to create a client
     * (@see createClient()) for the operation and use that instead.
     *
     * @param operation The name of the operation to use. Must NOT be null.
     * @param elem      The XML to send
     * @throws AxisFault if something goes wrong while sending it or if a fault is
     *                   received in response (per the Robust In-Only MEP).
     */
    public void sendRobust(QName operation, OMElement elem) throws AxisFault {
        if (options.isUseSeparateListener()) {

            // This mean doing a Fault may come through a differnt channel .
            // If the
            // transport is two way transport (e.g. http) Only one channel is
            // used (e.g. in http cases
            // 202 OK is sent to say no respsone avalible). Axis2 get blocked
            // return when the response is avalible.
            SyncCallBack callback = new SyncCallBack();

            // this method call two channel non blocking method to do the work
            // and wait on the callbck
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
                    // - wait can end prematurly, see Object.wait( int timeout )
                    waitTime = timeout - (System.currentTimeMillis() - startTime);
                }

            }
            SOAPEnvelope envelope = callback.envelope;
            // process the resule of the invocation
            if (envelope != null) {
                // building soap enevlop
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
                    //no exception has occured
                }
            }
        } else {
            MessageContext mc = new MessageContext();
            fillSoapEnvelope(mc, elem);
            OperationClient mepClient = createClient(operation);
            mepClient.addMessageContext(mc);
            mepClient.execute(true);
        }
    }

    /**
     * Send a bit of XML and forget about it. This API is used to interact with
     * a service operation who's MEP is In-Only. That is, there is no
     * opportunity to get an error from the service via this API; one may still
     * get client-side errors, such as host unknown etc.
     *
     * @param elem The XML element to send to the service
     * @throws AxisFault If something goes wrong trying to send the XML
     */
    public void fireAndForget(OMElement elem) throws AxisFault {
        fireAndForget(ANON_OUT_ONLY_OP, elem);
    }

    /**
     * Send a bit of XML and forget about it under the guise of a specific
     * operation. This API is used to interact with a service operation who's
     * MEP is In-Only. That is, there is no opportunity to get an error from the
     * service via this API; one may still get client-side errors, such as host
     * unknown etc.
     *
     * @param operation The operation to send fire the message under
     * @param elem      The XML element to send to the service
     * @throws AxisFault If something goes wrong trying to send the XML
     */
    public void fireAndForget(QName operation, OMElement elem) throws AxisFault {
        // look up the appropriate axisop and create the client
        OperationClient mepClient = createClient(operation);
        // create a message context and put the payload in there along with any
        // headers
        MessageContext mc = new MessageContext();
        fillSoapEnvelope(mc, elem);
        // add the message context there and have it go
        mepClient.addMessageContext(mc);
        mepClient.execute(false);
    }

    /**
     * This will allow user to do a send and receive invocation just providing the payload.
     *
     * @param elem
     * @return
     * @throws AxisFault
     */
    public OMElement sendReceive(OMElement elem) throws AxisFault {
        return sendReceive(ANON_OUT_IN_OP, elem);
    }

    /**
     * Do send receive invocation giving the payload and the operation QName.
     *
     * @param operation
     * @param elem
     * @return
     * @throws AxisFault
     */
    public OMElement sendReceive(QName operation, OMElement elem)
            throws AxisFault {
        if (options.isUseSeparateListener()) {

            // This mean doing a Request-Response invocation using two channel.
            // If the
            // transport is two way transport (e.g. http) Only one channel is
            // used (e.g. in http cases
            // 202 OK is sent to say no respsone avalible). Axis2 get blocked
            // return when the response is avalible.
            SyncCallBack callback = new SyncCallBack();

            // this method call two channel non blocking method to do the work
            // and wait on the callbck
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
                    // - wait can end prematurly, see Object.wait( int timeout )
                    waitTime = timeout - (System.currentTimeMillis() - startTime);
                }

            }
            // process the result of the invocation
            if (callback.envelope != null) {
                // building soap envelop
                callback.envelope.build();
                // closing transport
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
            MessageContext mc = new MessageContext();
            fillSoapEnvelope(mc, elem);
            OperationClient mepClient = createClient(operation);
            mepClient.addMessageContext(mc);
            mepClient.execute(true);
            MessageContext response = mepClient
                    .getMessageContext(WSDLConstants.MESSAGE_LABEL_IN_VALUE);
            return response.getEnvelope().getBody().getFirstElement();
        }
    }

    /**
     * Invoke send and receive just providing the payload and the call back handler. This will ease
     * the user by not requiring him to provide an operation name
     *
     * @param elem
     * @param callback
     * @throws AxisFault
     */
    public void sendReceiveNonBlocking(OMElement elem, Callback callback)
            throws AxisFault {
        sendReceiveNonBlocking(ANON_OUT_IN_OP, elem, callback);
    }

    /**
     * Do a blocking send and receive invocation.
     *
     * @param operation
     * @param elem
     * @param callback
     * @throws AxisFault
     */
    public void sendReceiveNonBlocking(QName operation, OMElement elem,
                                       Callback callback) throws AxisFault {
        MessageContext mc = new MessageContext();
        fillSoapEnvelope(mc, elem);
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
     * Create a MEP client for a specific operation. This is the way one can
     * create a full function MEP client which can be used to exchange messages
     * for this specific operation. If you're using this then you must know what
     * you're doing and need the full capabilities of Axis2's client
     * architecture. This is meant for people with deep skin and not the light
     * user.
     *
     * @param operation The QName of the operation to create a client for.
     * @return a MEP client configured to talk to the given operation or null if
     *         the operation name is not found.
     * @throws AxisFault if the operation is not found or something else goes wrong
     */
    public OperationClient createClient(QName operation) throws AxisFault {
        AxisOperation axisOp = axisService.getOperation(operation);
        if (axisOp == null) {
            throw new AxisFault(Messages
                    .getMessage("operationnotfound", operation.getLocalPart()));
        }

        OperationClient oc = axisOp.createClient(serviceContext, options);

        // if overide options have been set, that means we need to make sure
        // those options override the options of even the operation client. So,
        // what we do is switch the parents around to make that work.
        if (overrideOptions != null) {
            overrideOptions.setParent(oc.getOptions());
            oc.setOptions(overrideOptions);
        }
        return oc;
    }

    /**
     * This will close the out put stream or , and remove entry from waiting
     * queue of the transport Listener queue.
     *
     * @throws AxisFault
     */
    public void finalizeInvoke() throws AxisFault {
        configContext.getListenerManager().stop();
    }

    /**
     * Return the SOAP factory to use depending on what options have been set
     * (or default to SOAP 1.1)
     *
     * @return the SOAP factory
     */
    private SOAPFactory getSOAPFactory() {
        String soapVersionURI = options.getSoapVersionURI();
        if (SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI.equals(soapVersionURI)) {
            return OMAbstractFactory.getSOAP12Factory();
        } else {
            // make the SOAP 1.2 the default SOAP version
            return OMAbstractFactory.getSOAP11Factory();
        }
    }

    /**
     * Prepare a SOAP envelope with the stuff to be sent.
     *
     * @param mc   the message context to be filled
     * @param elem the payload content
     * @throws AxisFault if something goes wrong
     */
    private void fillSoapEnvelope(MessageContext mc, OMElement elem)
            throws AxisFault {
        mc.setServiceContext(serviceContext);
        SOAPFactory soapFactory = getSOAPFactory();
        SOAPEnvelope envelope = soapFactory.getDefaultEnvelope();
        if (elem != null) {
            envelope.getBody().addChild(elem);
        }
        if (headers != null) {
            SOAPHeader sh = envelope.getHeader();
            for (int i = 0; i < headers.size(); i++) {
                sh.addChild((OMElement) headers.get(i));
            }
        }
        mc.setEnvelope(envelope);
    }


    /**
     * To get the EPR that the service is running
     * transport : can be null , if it is null then epr will be craetd using any available
     * transports
     *
     * @throws AxisFault
     */
    public EndpointReference getMyEPR(String transport) throws AxisFault {
        return serviceContext.getMyEPR(transport);
    }

    /**
     * To get the Targert EPR if any in service conetext
     * and reference paramters in TEPR can send back , in the same time this epr can use to manage
     * session across mutiple ServiceClient
     *
     * @return <code>EndpointReference</code>
     */
    public EndpointReference getTargetEPR() {
        return serviceContext.getTargetEPR();
    }

    public void setTargetEPR(EndpointReference targetEpr) {
        serviceContext.setTargetEPR(targetEpr);
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
     * To get the service context.
     *
     * @return ServiceContext
     */
    public ServiceContext getServiceContext() {
        return serviceContext;
    }

    protected void finalize() throws Throwable {
        super.finalize();
        cleanup();
    }

    /**
     * This will remove axissrevice , if it is passed configuration context into it.
     * The problem is if some one keep of on creating service client by giving
     * configuration conetxt and null aixsService , in that case it SC will
     * create new axisServices and add that into axisConfig , so to remove the
     * one that this particular SC instance create one can use this method.
     *
     * @throws AxisFault
     */
    public void cleanup() throws AxisFault {
        if (!createConfigCtx) {
            String serviceGroupName = ((AxisServiceGroup) axisService.getParent()).getServiceGroupName();
            AxisConfiguration axisConfiguration = configContext.getAxisConfiguration();
            AxisServiceGroup asg = axisConfiguration.getServiceGroup(serviceGroupName);
            if (asg != null) {
                axisConfiguration.removeServiceGroup(
                        serviceGroupName);
            }
        }
    }
}
