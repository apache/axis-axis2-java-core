package org.apache.axis2.client;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.async.AsyncResult;
import org.apache.axis2.client.async.Callback;
import org.apache.axis2.context.*;
import org.apache.axis2.description.*;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.i18n.Messages;
import org.apache.axis2.om.OMAbstractFactory;
import org.apache.axis2.om.OMElement;
import org.apache.axis2.soap.SOAP12Constants;
import org.apache.axis2.soap.SOAPEnvelope;
import org.apache.axis2.soap.SOAPFactory;
import org.apache.axis2.soap.SOAPHeader;
import org.apache.axis2.util.CallbackReceiver;
import org.apache.wsdl.WSDLConstants;

import javax.xml.namespace.QName;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

/**
 * A ServiceClient class is used to create a client for a service. More details
 * need to be explained here.
 */
public class ServiceClient {

    // service and operation names used for anonymously stuff
    public static final String ANON_SERVICE = "__ANONYMOUS_SERVICE__";

    public static final QName ANON_OUT_ONLY_OP = new QName(
            "__OPERATION_OUT_ONLY__");

    public static final QName ANON_ROBUST_OUT_ONLY_OP = new QName(
            "__OPERATION_ROBUST_OUT_ONLY__");

    public static final QName ANON_OUT_IN_OP = new QName(
            "__OPERATION_OUT_IN__");

    // the metadata for the service that I'm clienting for
    AxisService axisService;

    // the configuration in which my metadata lives
    AxisConfiguration axisConfig;

    // the configuration context in which I live
    ConfigurationContext configContext;

    // service context for this specific service instance
    ServiceContext serviceContext;

    // client options for this service interaction
    Options options = new Options();

    // list of headers to be sent with the simple APIs
    ArrayList headers;

    private CallbackReceiver callbackReceiver;

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
        // create a config context if needed
        this.configContext = (configContext != null) ? configContext
                : new ConfigurationContextFactory()
                .createConfigurationContextFromFileSystem(null, null);

        // save the axisConfig and service
        this.axisConfig = this.configContext.getAxisConfiguration();
        this.axisService = (axisService != null) ? axisService
                : createAnonymousService();

        // add the service to the config context if it isn't in there already
        if (this.axisConfig.getService(this.axisService.getName()) == null) {
            this.axisConfig.addService(this.axisService);
        }

        // create a service context for myself: create a new service group
        // context and then get the service context for myself as I'll need that
        // later for stuff that I gotta do
        ServiceGroupContext sgc = new ServiceGroupContext(this.configContext,
                (AxisServiceGroup) this.axisService.getParent());
        this.serviceContext = sgc.getServiceContext(this.axisService);
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
        // create a config context if needed
        this.configContext = (configContext != null) ? configContext
                : new ConfigurationContextFactory()
                .createConfigurationContextFromFileSystem(null, null);
        try {
            this.axisConfig = this.configContext.getAxisConfiguration();
            axisService = ClientUtils.creatAxisService(wsdlURL, wsdlServiceName, portName, options);
            // add the service to the config context if it isn't in there already
            if (this.axisConfig.getService(this.axisService.getName()) == null) {
                this.axisConfig.addService(this.axisService);
            }
            ServiceGroupContext sgc = new ServiceGroupContext(this.configContext,
                    (AxisServiceGroup) this.axisService.getParent());
            this.serviceContext = sgc.getServiceContext(this.axisService);
        } catch (IOException e) {
            throw new AxisFault(e);
        }
    }

    /**
     * Create a service client by assuming an anonymous service and any other
     * necessary information.
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
        // now add anonymous operations to the axis service for use with the
        // shortcut client API. NOTE: We only add the ones we know we'll use
        // later in the convenience API; if you use
        // this constructor then you can't expect any magic!
        AxisService axisService = new AxisService(ANON_SERVICE);
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
     * Engage a module for this service client.
     *
     * @param moduleName Name of the module to engage
     * @throws AxisFault if something goes wrong
     */
    public void engageModule(QName moduleName) throws AxisFault {
        axisService.engageModule(axisConfig.getModule(moduleName), axisConfig);
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
     * Add a simple header consisting of some text (and a header name; duh) to
     * be sent with interactions.
     *
     * @param headerName
     * @param headerText
     */
    public void addStringHeader(QName headerName, String headerText) {
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
        // look up the appropriate axisop and create the client
        // OperationClient mepClient = createClient(operation);

        // create a message context with elem as the payload
        /*
         * MessageContext mc = new MessageContext(); SOAPEnvelope se =
         * createEmptySOAPEnvelope(); se.getBody().addChild(se);
         * mc.setEnvelope(se); // create a client and have it do the work of
         * sending this out InOnlyMEPClient mepClient = new
         * InOnlyMEPClient(serviceContext); mepClient.send("foo", mc);
         */
        throw new UnsupportedOperationException(
                "ServiceClient.sendRobust is not yet implemented");

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
        mepClient.execute(true);
    }

    public OMElement sendReceive(OMElement elem) throws AxisFault {
        return sendReceive(ANON_OUT_IN_OP, elem);
    }

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

            if (timeout < 0) {
                while (!callback.isComplete()) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        throw new AxisFault(e);
                    }
                }
            } else {
                long index = timeout / 100;

                while (!callback.isComplete()) {

                    // wait till the reponse arrives
                    if (index-- >= 0) {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            throw new AxisFault(e);
                        }
                    } else {
                        throw new AxisFault(Messages
                                .getMessage("responseTimeOut"));
                    }
                }
            }
            // process the resule of the invocation
            if (callback.envelope != null) {
                MessageContext resMsgctx = callback.getMsgctx();
                //building soap enevlop
                callback.envelope.build();
                //closing tranport
                finalizeInvoke(resMsgctx);
                return callback.envelope.getBody().getFirstElement();
            } else {
                if (callback.error instanceof AxisFault) {
                    throw (AxisFault) callback.error;
                } else {
                    throw new AxisFault(callback.error);
                }
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

    public void sendReceiveNonblocking(OMElement elem, Callback callback)
            throws AxisFault {
        sendReceiveNonBlocking(ANON_OUT_IN_OP, elem, callback);
    }

    public void sendReceiveNonBlocking(QName operation, OMElement elem,
                                       Callback callback) throws AxisFault {
        MessageContext mc = new MessageContext();
        fillSoapEnvelope(mc, elem);
        OperationClient mepClient = createClient(operation);
        // here a bloking invocation happens in a new thread, so the
        // progamming model is non blocking
        mepClient.setCallback(callback);
        mepClient.addMessageContext(mc);
        if (options.isUseSeparateListener()) {
            if (callbackReceiver == null) {
                callbackReceiver = new CallbackReceiver();
            }
            axisService.getOperation(operation).setMessageReceiver(
                    callbackReceiver);
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
            throw new AxisFault("Operation '" + operation + "' not found");
        }
        return axisOp.createClient(serviceContext, options);
    }

    /**
     * This will close the out put stream or , and remove entry from waiting
     * queue of the transport Listener queue
     *
     * @throws AxisFault
     */
    private void finalizeInvoke(MessageContext msgCtx) throws AxisFault {
        if (options.getTransportInProtocol() != null) {
            ListenerManager.stop(msgCtx.getConfigurationContext(),
                    msgCtx.getTransportIn().getName().getLocalPart());
        }
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
            // if its not SOAP 1.2 just assume SOAP 1.1
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
        SOAPFactory sf = getSOAPFactory();
        SOAPEnvelope se = sf.getDefaultEnvelope();
        if (elem != null) {
            se.getBody().addChild(elem);
        }
        if (headers != null) {
            SOAPHeader sh = se.getHeader();
            for (int i = 0; i < headers.size(); i++) {
                sh.addChild((OMElement) headers.get(i));
            }
        }
        mc.setEnvelope(se);
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

        public void onError(Exception e) {
            error = e;
        }

        public MessageContext getMsgctx() {
            return msgctx;
        }
    }
}
