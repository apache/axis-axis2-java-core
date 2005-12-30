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
import org.apache.axis2.util.UUIDGenerator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wsdl.WSDLConstants;

import javax.xml.namespace.QName;
import java.net.URL;
import java.util.ArrayList;

/**
 * A ServiceClient class is used to create a client for a service. More details
 * need to be explained here.
 */
public class ServiceClient {

    private Log log = LogFactory.getLog(getClass());

    // service and operation names used for anonymously stuff
    private static final String ANON_SERVICE = "__ANONYMOUS_SERVICE__";

    private static final QName ANON_OUT_ONLY_OP = new QName(
            "__OPERATION_OUT_ONLY__");

    private static final QName ANON_ROBUST_OUT_ONLY_OP = new QName(
            "__OPERATION_ROBUST_OUT_ONLY__");

    private static final QName ANON_OUT_IN_OP = new QName(
            "__OPERATION_OUT_IN__");

    // the metadata for the service that I'm clienting for
    AxisService axisService;

    // the configuration context in which I live
    ConfigurationContext configContext;

    // service context for this specific service instance
    ServiceContext serviceContext;

    // client options for this service interaction
    Options options = new Options();

    // list of headers to be sent with the simple APIs
    ArrayList headers;
    //to set the name of the operation to be invoked , and this is usefull if the user
    // try to reuse same ServiceClent to invoke more than one operation in the service ,
    //  in that case he can set the current operation name and invoke that.
    private QName currentOperationName = null;

    private CallbackReceiver callbackReceiver;

    /**
     * Create a service client configured to work with a specific AxisService.
     * If this service is already in the world that's handed in (in the form of
     * a ConfigurationContext) then I will happily work in it. If not I will
     * create a small little virtual world and live there.
     *
     * @param configContext The configuration context under which this service lives (may
     *                      be null, in which case a new local one will be created)
     * @throws AxisFault if something goes wrong while creating a config context (if
     *                   needed)
     */
    public ServiceClient(ConfigurationContext configContext) throws AxisFault {
        // create a config context if needed
        this.configContext = (configContext != null) ? configContext
                : createDefaultConfigurationContext();
        // add the service to the config context if it isn't in there already
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
        // TODO: Srinath to write this code :)
        throw new UnsupportedOperationException(
                "ServiceClient currently does not support direct WSDL construction");
    }

    /**
     * Create a service client by assuming an anonymous service and any other
     * necessary information.
     */
    public ServiceClient() throws AxisFault {
        this((ConfigurationContext) null);
    }

    public ServiceClient(ServiceContext serviceContext) {
        this.serviceContext = serviceContext;
        this.configContext = serviceContext.getConfigurationContext();
        this.axisService = serviceContext.getAxisService();
    }

    /**
     * If the AxisService is null this will create an AnonymousService
     */
    private AxisService createAnonymousService() {
        // since I have not been created with real service metadata, let's
        // create an anonymous service and add myself to a newly created default
        // (and lonely) world where I'm the only service around
        axisService = new AxisService(ANON_SERVICE);
        // add anonymous operations as well for use with the shortcut client
        // API. NOTE: We only add the ones we know we'll use later; if you use
        // this constructor then you can't expect any magic!
        axisService.addOperation(new RobustOutOnlyAxisOperation(
                ANON_ROBUST_OUT_ONLY_OP));
        axisService.addOperation(new OutOnlyAxisOperation(ANON_OUT_ONLY_OP));
        axisService.addOperation(new OutInAxisOperation(ANON_OUT_IN_OP));
        return axisService;
    }

    /**
     * If I have not been created with real service metadata, let's create a
     * temporary configuration context for this (lonely) client to live in.
     */
    private ConfigurationContext createDefaultConfigurationContext()
            throws AxisFault {
        return new ConfigurationContextFactory()
                .buildConfigurationContext(null);
    }

    /**
     * Create the service context for myself
     */
    private void createServiceContext() throws AxisFault {
        if (axisService == null) {
            axisService = getAxisService();
        }
        // create a new service group context and then get the service context
        // for myself as I'll need that later for stuff that I gotta do
        ServiceGroupContext sgc = new ServiceGroupContext(configContext,
                getAxisService().getParent());
        serviceContext = sgc.getServiceContext(getAxisService());
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
        // look up the appropriate axisop and create the client
        OperationClient mepClient = getAxisService().getOperation(
                ANON_ROBUST_OUT_ONLY_OP).createClient(serviceContext, options);

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
        // create a message context and put the payload in there along with any
        // headers
        MessageContext mc = new MessageContext();
        if (serviceContext == null) {
            createServiceContext();
        }
        fillSoapEnevelop(mc, elem);

        // look up the appropriate axisop and create the client
        OperationClient mepClient = getAxisService().getOperation(ANON_OUT_ONLY_OP)
                .createClient(serviceContext, options);

        // add the message context there and have it go
        mepClient.addMessageContext(mc);
        mepClient.execute(true);
    }

    public OMElement sendReceive(OMElement elem) throws AxisFault {
        if (serviceContext == null) {
            createServiceContext();
        }
        if (options.isUseSeparateListener()) {

            // This mean doing a Request-Response invocation using two channel. If the
            // transport is two way transport (e.g. http) Only one channel is used (e.g. in http cases
            // 202 OK is sent to say no repsone avalible). Axis2 get blocked return when the response is avalible.
            SyncCallBack callback = new SyncCallBack();

            // this method call two channel non blocking method to do the work and wait on the callbck
            sendReceiveNonblocking(elem, callback);

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
                        throw new AxisFault(Messages.getMessage("responseTimeOut"));
                    }
                }
            }
            // process the resule of the invocation
            if (callback.envelope != null) {
                MessageContext resMsgctx =
                        new MessageContext(serviceContext.getConfigurationContext());

                resMsgctx.setEnvelope(callback.envelope);

                return callback.envelope.getBody().getFirstElement();
            } else {
                if (callback.error instanceof AxisFault) {
                    throw(AxisFault) callback.error;
                } else {
                    throw new AxisFault(callback.error);
                }
            }
        } else {
            MessageContext mc = new MessageContext();
            fillSoapEnevelop(mc, elem);

            setMessageID(mc);

            OperationClient mepClient;
            if (currentOperationName != null) {
                AxisOperation operation = getAxisService().getOperation(currentOperationName);
                if (operation == null) {
                    throw new AxisFault("Operation " + currentOperationName + " not find in the given service");
                }
                mepClient = operation.createClient(serviceContext, options);
            } else {
                // look up the appropriate axisop and create the client
                mepClient = getAxisService().getOperation(ANON_OUT_IN_OP)
                        .createClient(serviceContext, options);
            }

            mepClient.setOptions(options);
            mepClient.addMessageContext(mc);

            mepClient.execute(false);
            MessageContext response = mepClient.getMessageContext(WSDLConstants.MESSAGE_LABEL_IN_VALUE);
            return response.getEnvelope().getBody().getFirstElement();
        }
    }

    public void sendReceiveNonblocking(OMElement elem, Callback callback) throws AxisFault {
        if (serviceContext == null) {
            createServiceContext();
        }
        MessageContext mc = new MessageContext();
        fillSoapEnevelop(mc, elem);

        setMessageID(mc);

        AxisOperation operation;
        if (currentOperationName != null) {
            operation = getAxisService().getOperation(currentOperationName);
            if (operation == null) {
                throw new AxisFault("Operation " + currentOperationName + " not find in the given service");
            }
        } else {
            // look up the appropriate axisop and create the client
            operation = getAxisService().getOperation(ANON_OUT_IN_OP);
        }
        OperationClient mepClient = operation.createClient(serviceContext, options);
        // here a bloking invocation happens in a new thread, so the
        // progamming model is non blocking
        OperationContext opcontxt = new OperationContext(operation, serviceContext);

        mc.setOperationContext(opcontxt);
        mc.setServiceContext(serviceContext);
        opcontxt.setProperties(options.getProperties());
        options.setCallback(callback);
        mepClient.addMessageContext(mc);
        mepClient.setOptions(options);
        if (options.isUseSeparateListener()) {
            if (callbackReceiver == null) {
                callbackReceiver = new CallbackReceiver();
            }
            mepClient.setMessageReceiver(callbackReceiver);
        }
        mepClient.execute(true);
    }

    private void fillSoapEnevelop(MessageContext mc, OMElement elem) throws AxisFault {
        mc.setServiceContext(serviceContext);
        SOAPFactory sf = getSOAPFactory();
        SOAPEnvelope se = sf.getDefaultEnvelope();
        if (elem != null) {
            se.getBody().addChild(elem);
        }
        if (headers != null) {
            SOAPHeader sh = se.getHeader();
            for (int i = 0; i < headers.size(); i++) {
                OMElement headerBlock = (OMElement) headers.get(i);
                sh.addChild(headerBlock);
            }
        }
        mc.setEnvelope(se);
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
     */
    public OperationClient createClient(QName operation) throws AxisFault {
        AxisOperation axisOp = getAxisService().getOperation(operation);
        if (serviceContext == null) {
            createServiceContext();
        }
        return (axisOp == null) ? null : axisOp.createClient(serviceContext,
                options);
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

    private AxisService getAxisService() {
        if (axisService == null) {
            axisService = createAnonymousService();
            AxisConfiguration axisConfig = this.configContext
                    .getAxisConfiguration();
            if (axisConfig.getService(getAxisService().getName()) == null) {
                try {
                    axisConfig.addService(getAxisService());
                } catch (AxisFault axisFault) {
                    log.info("Error in getAxisService(): " + axisFault.getMessage());
                }
            }
        }
        return axisService;
    }

    public void setAxisService(AxisService axisService) {
        // adding service into system
        AxisConfiguration axisConfig = this.configContext
                .getAxisConfiguration();
        if (axisConfig.getService(getAxisService().getName()) == null) {
            try {
                axisConfig.addService(getAxisService());
            } catch (AxisFault axisFault) {
                log.info("Error in getAxisService(): " + axisFault.getMessage());
            }
        }
        this.axisService = axisService;
    }

    private void setMessageID(MessageContext mc) {
        // now its the time to put the parameters set by the user in to the
        // correct places and to the
        // if there is no message id still, set a new one.
        String messageId = options.getMessageId();
        if (messageId == null || "".equals(messageId)) {
            messageId = UUIDGenerator.getUUID();
            options.setMessageId(messageId);
        }
        mc.setMessageID(messageId);
    }

    /**
     * To set the opration that need to be invoke , as an example say client creat a
     * service with mutiple operation and he need to invoke all of them using one service
     * client in that case he can give the operation name and invoke that operation
     *
     * @param currentOperationName
     */
    public void setCurrentOperationName(QName currentOperationName) {
        //todo : pls ask from Sanjiva about this
        this.currentOperationName = currentOperationName;
    }

    /**
     * This will close the out put stream or , and remove entry from waiting queue of the transport
     * Listener queue
     *
     * @throws AxisFault
     */
    public void finalizeInvoke() throws AxisFault {
        if (options.isUseSeparateListener()) {
            ListenerManager.stop(serviceContext.getConfigurationContext(),
                    options.getTransportInDescription().getName().getLocalPart());
        }
    }

    public void engageModule(QName moduleName) throws AxisFault {
        //TODO : This is a hack pls implement this in properway
        configContext.getAxisConfiguration().engageModule(moduleName);
    }

    /**
     * This class acts as a callback that allows users to wait on the result.
     */
    private class SyncCallBack extends Callback {
        private SOAPEnvelope envelope;
        private Exception error;

        public void onComplete(AsyncResult result) {
            this.envelope = result.getResponseEnvelope();
        }

        public void reportError(Exception e) {
            error = e;
        }
    }
}
