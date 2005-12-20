package org.apache.axis2.client;

import java.net.URL;
import java.util.ArrayList;

import javax.xml.namespace.QName;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.async.Callback;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.context.ServiceGroupContext;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.OutInAxisOperation;
import org.apache.axis2.description.OutOnlyAxisOperation;
import org.apache.axis2.description.RobustOutOnlyAxisOperation;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.om.OMAbstractFactory;
import org.apache.axis2.om.OMElement;
import org.apache.axis2.soap.SOAP12Constants;
import org.apache.axis2.soap.SOAPEnvelope;
import org.apache.axis2.soap.SOAPFactory;
import org.apache.axis2.soap.SOAPHeader;

/**
 * A ServiceClient class is used to create a client for a service. More details
 * need to be explained here.
 */
public class ServiceClient {
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

    /**
     * Create a service client configured to work with a specific AxisService.
     * If this service is already in the world that's handed in (in the form of
     * a ConfigurationContext) then I will happily work in it. If not I will
     * create a small little virtual world and live there.
     * 
     * @param configContext
     *            The configuration context under which this service lives (may
     *            be null, in which case a new local one will be created)
     * @param service
     *            The AxisService to create a client for. Must not be null; bad
     *            things will happen if it is.
     * @throws AxisFault
     *             if something goes wrong while creating a config context (if
     *             needed)
     */
    public ServiceClient(ConfigurationContext configContext,
            AxisService axisService) throws AxisFault {
        // create a config context if needed
        this.configContext = (configContext != null) ? configContext
                : createDefaultConfigurationContext();
        // add the service to the config context if it isn't in there already
        AxisConfiguration axisConfig = this.configContext
                .getAxisConfiguration();
        if (axisConfig.getService(axisService.getName()) != null) {
            axisConfig.addService(axisService);
        }
        this.axisService = axisService;
        this.serviceContext = createServiceContext();
    }

    /**
     * Create a service client for WSDL service identified by the QName of the
     * wsdl:service element in a WSDL document.
     * 
     * @param configContext
     *            The configuration context under which this service lives (may
     *            be null, in which case a new local one will be created) *
     * @param wsdlURL
     *            The URL of the WSDL document to read
     * @param wsdlServiceName
     *            The QName of the WSDL service in the WSDL document to create a
     *            client for
     * @param portName
     *            The name of the WSDL 1.1 port to create a client for. May be
     *            null (if WSDL 2.0 is used or if only one port is there). .
     * @throws AxisFault
     *             if something goes wrong while creating a config context (if
     *             needed)
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
        // since I have not been created with real service metadata, let's
        // create an anonymous service and add myself to a newly created default
        // (and lonely) world where I'm the only service around
        this(null, new AxisService(ANON_SERVICE));
        // add anonymous operations as well for use with the shortcut client
        // API. NOTE: We only add the ones we know we'll use later; if you use
        // this constructor then you can't expect any magic!
        axisService.addOperation(new RobustOutOnlyAxisOperation(
                ANON_ROBUST_OUT_ONLY_OP));
        axisService.addOperation(new OutOnlyAxisOperation(ANON_OUT_ONLY_OP));
        axisService.addOperation(new OutInAxisOperation(ANON_OUT_IN_OP));
    }

    /**
     * If I have not been created with real service metadata, let's create a
     * temporary configuration context for this (lonely) client to live in.
     */
    private ConfigurationContext createDefaultConfigurationContext()
            throws AxisFault {
        return new ConfigurationContextFactory()
                .buildClientConfigurationContext(null);
    }

    /**
     * Create the service context for myself
     */
    private ServiceContext createServiceContext() {
        // create a new service group context and then get the service context
        // for myself as I'll need that later for stuff that I gotta do
        ServiceGroupContext sgc = new ServiceGroupContext(configContext,
                axisService.getParent());
        return sgc.getServiceContext(axisService.getName());
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
     * @param header
     *            The header to be added for interactions. Must not be null.
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
     * @see addHeader(OMElement)
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
     * @param elem
     *            The XML to send
     * @throws AxisFault
     *             if something goes wrong while sending it or if a fault is
     *             received in response (per the Robust In-Only MEP).
     */
    public void sendRobust(OMElement elem) throws AxisFault {
        // look up the appropriate axisop and create the client
        OperationClient mepClient = axisService.getOperation(
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
     * @param elem
     *            The XML element to send to the service
     * @throws AxisFault
     *             If something goes wrong trying to send the XML
     */
    public void fireAndForget(OMElement elem) throws AxisFault {
        // create a message context and put the payload in there along with any
        // headers
        MessageContext mc = new MessageContext();
        mc.setServiceContext (serviceContext);
        SOAPFactory sf = getSOAPFactory();
        SOAPEnvelope se = sf.getDefaultEnvelope();
        se.getBody().addChild(elem);
        if (headers != null) {
            SOAPHeader sh = se.getHeader();
            for (int i = 0; i < headers.size(); i++) {
                OMElement headerBlock = (OMElement) headers.get(i);
                sh.addChild(headerBlock);
            }
        }

        // look up the appropriate axisop and create the client
        OperationClient mepClient = axisService.getOperation(ANON_OUT_ONLY_OP)
                .createClient(serviceContext, options);

        // add the message context there and have it go
        mepClient.addMessageContext(mc);
        mepClient.execute(true);
    }

    public OMElement sendReceive(OMElement elem) {
        // look up the appropriate axisop and create the client
        OperationClient mepClient = axisService.getOperation(ANON_OUT_IN_OP)
                .createClient(serviceContext, options);
        // TODO
        throw new UnsupportedOperationException(
                "ServiceClient.sendReceive() is not yet implemented");
    }

    public void sendReceiveNonblocking(OMElement elem, Callback callback) {
        // look up the appropriate axisop and create the client
        OperationClient mepClient = axisService.getOperation(ANON_OUT_IN_OP)
                .createClient(serviceContext, options);
        // TODO
        throw new UnsupportedOperationException(
                "ServiceClient.sendReceiveNonblocking() is not yet implemented");
    }

    /**
     * Create a MEP client for a specific operation. This is the way one can
     * create a full function MEP client which can be used to exchange messages
     * for this specific operation. If you're using this then you must know what
     * you're doing and need the full capabilities of Axis2's client
     * architecture. This is meant for people with deep skin and not the light
     * user.
     * 
     * @param operation
     *            The QName of the operation to create a client for.
     * @return a MEP client configured to talk to the given operation or null if
     *         the operation name is not found.
     */
    public OperationClient createClient(QName operation) {
        AxisOperation axisOp = axisService.getOperation(operation);
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
}
