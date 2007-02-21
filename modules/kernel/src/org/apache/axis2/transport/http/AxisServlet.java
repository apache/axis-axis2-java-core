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


package org.apache.axis2.transport.http;

import edu.emory.mathcs.backport.java.util.concurrent.CountDownLatch;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXBuilder;
import org.apache.axiom.om.util.UUIDGenerator;
import org.apache.axiom.soap.SOAP12Constants;
import org.apache.axiom.soap.SOAPFaultCode;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.AddressingHelper;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.context.*;
import org.apache.axis2.deployment.WarBasedAxisConfigurator;
import org.apache.axis2.description.*;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.engine.AxisEngine;
import org.apache.axis2.engine.Handler.InvocationResponse;
import org.apache.axis2.engine.ListenerManager;
import org.apache.axis2.transport.RequestResponseTransport;
import org.apache.axis2.transport.TransportListener;
import org.apache.axis2.transport.http.server.HttpUtils;
import org.apache.axis2.transport.http.util.RESTUtil;
import org.apache.axis2.util.JavaUtils;
import org.apache.axis2.util.MessageContextBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.namespace.QName;
import java.io.*;
import java.net.SocketException;
import java.util.Map;

/**
 * Class AxisServlet
 */
public class AxisServlet extends HttpServlet implements TransportListener {

    private static final Log log = LogFactory.getLog(AxisServlet.class);
    public static final String CONFIGURATION_CONTEXT = "CONFIGURATION_CONTEXT";
    public static final String SESSION_ID = "SessionId";
    protected transient ConfigurationContext configContext;
    protected transient AxisConfiguration axisConfiguration;

    protected transient ServletConfig servletConfig;

    private transient ListingAgent agent;
    private String contextRoot = null;

    protected boolean disableREST = false;
    private static final String LIST_SERVICES_SUFIX = "/services/listServices";
    private static final String LIST_FAUKT_SERVICES_SUFIX = "/services/ListFaultyServices";
    private boolean closeReader = true;

    private static final int BUFFER_SIZE = 1024 * 8;

    /**
     * Implementaion of POST interface
     *
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        //set the initial buffer for a larger value
        response.setBufferSize(BUFFER_SIZE);

        initContextRoot(request);

        MessageContext msgContext;
        OutputStream out = response.getOutputStream();
        String contentType = request.getContentType();
        if (!isRESTRequest(contentType)) {
            msgContext = createMessageContext(request, response);
            try {
                // adding ServletContext into msgContext;
                InvocationResponse pi = HTTPTransportUtils.
                        processHTTPPostRequest(msgContext,
                                               new BufferedInputStream(request.getInputStream()),
                                               new BufferedOutputStream(out),
                                               contentType,
                                               request.getHeader(HTTPConstants.HEADER_SOAP_ACTION),
                                               request.getRequestURL().toString());

                Boolean holdResponse =
                        (Boolean) msgContext.getProperty(RequestResponseTransport.HOLD_RESPONSE);

                if (pi.equals(InvocationResponse.SUSPEND) ||
                    (holdResponse != null && Boolean.TRUE.equals(holdResponse))) {
                    ((RequestResponseTransport) msgContext
                            .getProperty(RequestResponseTransport.TRANSPORT_CONTROL))
                            .awaitResponse();
                }

                Object contextWritten = null;
                OperationContext operationContext = msgContext.getOperationContext();

                if (operationContext != null)
                    contextWritten = operationContext.getProperty(Constants.RESPONSE_WRITTEN);

                response.setContentType("text/xml; charset="
                                        + msgContext
                        .getProperty(Constants.Configuration.CHARACTER_SET_ENCODING));

                if ((contextWritten == null) || !Constants.VALUE_TRUE.equals(contextWritten)) {
                    response.setStatus(HttpServletResponse.SC_ACCEPTED);
                }

            } catch (AxisFault e) {
                log.debug(e);
                if (msgContext != null) {
                    processAxisFault(msgContext, response, out, e);
                } else {
                    throw new ServletException(e);
                }
            } catch (Throwable t) {
                log.error(t);
                if (msgContext != null) {
                    try {
                        // If the fault is not going along the back channel we should be 202ing
                        if (AddressingHelper.isFaultRedirected(msgContext)) {
                            response.setStatus(HttpServletResponse.SC_ACCEPTED);
                        } else {
                            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

                            AxisBindingOperation axisBindingOperation =
                                    (AxisBindingOperation) msgContext
                                            .getProperty(Constants.AXIS_BINDING_OPERATION);
                            if (axisBindingOperation != null) {
                                Integer code = (Integer) axisBindingOperation.getFault(
                                        (String) msgContext.getProperty(Constants.FAULT_NAME))
                                        .getProperty(WSDL2Constants.ATTR_WHTTP_CODE);
                                if (code != null) {
                                    response.setStatus(code.intValue());
                                }
                            }
                        }
                        handleFault(msgContext, out, new AxisFault(t.toString(), t));
                    } catch (AxisFault e2) {
                        log.info(e2);
                        throw new ServletException(e2);
                    }
                } else {
                    throw new ServletException(t);
                }
                closeStaxBuilder(msgContext);
            }
        } else {
            if (!disableREST) {
                new ProcessRESTRequest(Constants.Configuration.HTTP_METHOD_POST, request, response)
                        .processXMLRequest();
            } else {
                disableRESTErrorMessage(response);
            }
        }
    }

    /**
     * Implementation for GET interface
     *
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException
     */

    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response) throws ServletException, IOException {

        initContextRoot(request);

        // this method is also used to serve for the listServices request.

        String requestURI = request.getRequestURI();
        String query = request.getQueryString();

        // There can be three different request coming to this.
        // 1. wsdl, wsdl2 and xsd requests
        // 2. list services requests
        // 3. REST requests.
        if ((query != null) && (query.indexOf("wsdl2") >= 0 ||
                                query.indexOf("wsdl") >= 0 || query.indexOf("xsd") >= 0 ||
                                query.indexOf("policy") >= 0)) {
            // handling meta data exchange stuff
            agent.processListService(request, response);
        } else if (requestURI.endsWith(LIST_SERVICES_SUFIX) ||
                   requestURI.endsWith(LIST_FAUKT_SERVICES_SUFIX)) {
            // handling list services request
            try {
                agent.handle(request, response);
            } catch (Exception e) {
                throw new ServletException(e);
            }
        } else if (!disableREST) {
            new ProcessRESTRequest(Constants.Configuration.HTTP_METHOD_GET, request, response)
                    .processURLRequest();
        } else {
            disableRESTErrorMessage(response);
        }
    }

    /**
     * Implementation of DELETE interface
     *
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException
     */

    protected void doDelete(HttpServletRequest request,
                            HttpServletResponse response) throws ServletException, IOException {

        initContextRoot(request);
        // this method is also used to serve for the listServices request.
        if (!disableREST) {
            new ProcessRESTRequest(Constants.Configuration.HTTP_METHOD_DELETE, request, response)
                    .processURLRequest();
        } else {
            disableRESTErrorMessage(response);
        }
    }

    /**
     * Implementation of PUT interface
     *
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException
     */
    protected void doPut(HttpServletRequest request,
                         HttpServletResponse response) throws ServletException, IOException {

        initContextRoot(request);
        // this method is also used to serve for the listServices request.
        if (!disableREST) {
            new ProcessRESTRequest(Constants.Configuration.HTTP_METHOD_PUT, request, response)
                    .processXMLRequest();
        } else {
            disableRESTErrorMessage(response);
        }
    }

    /**
     * Private method that deals with disabling of REST support.
     *
     * @param response
     * @throws IOException
     */
    private void disableRESTErrorMessage(HttpServletResponse response) throws IOException {
        PrintWriter writer = new PrintWriter(response.getOutputStream());
        writer.println("<html><body><h2>Please enable REST support in WEB-INF/conf/axis2.xml " +
                       "and WEB-INF/web.xml</h2></body></html>");
        writer.flush();
        response.setStatus(HttpServletResponse.SC_ACCEPTED);

    }

    /**
     * Close the builders.
     *
     * @param messageContext
     * @throws ServletException
     */
    private void closeStaxBuilder(MessageContext messageContext) throws ServletException {
        if (closeReader && messageContext != null) {
            try {
                StAXBuilder builder = (StAXBuilder) messageContext.getEnvelope().getBuilder();
                if (builder != null) {
                    builder.close();
                }
            } catch (Exception e) {
                log.debug(e);
                throw new ServletException(e);
            }
        }

    }

    /**
     * Processing for faults
     *
     * @param msgContext
     * @param res
     * @param out
     * @param e
     */
    private void processAxisFault(MessageContext msgContext, HttpServletResponse res,
                                  OutputStream out, AxisFault e) {
        try {
            // If the fault is not going along the back channel we should be 202ing
            if (AddressingHelper.isFaultRedirected(msgContext)) {
                res.setStatus(HttpServletResponse.SC_ACCEPTED);
            } else {

                res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

                AxisBindingOperation axisBindingOperation = (AxisBindingOperation) msgContext
                        .getProperty(Constants.AXIS_BINDING_OPERATION);
                if (axisBindingOperation != null) {
                    AxisBindingMessage fault = axisBindingOperation
                            .getFault((String) msgContext.getProperty(Constants.FAULT_NAME));
                    if (fault != null) {
                        Integer code = (Integer) fault.getProperty(WSDL2Constants.ATTR_WHTTP_CODE);
                        if (code != null) {
                            res.setStatus(code.intValue());
                        }
                    }
                }
            }
            handleFault(msgContext, out, e);
        } catch (AxisFault e2) {
            log.info(e2);
        }
    }

    protected void handleFault(MessageContext msgContext, OutputStream out, AxisFault e)
            throws AxisFault {
        msgContext.setProperty(MessageContext.TRANSPORT_OUT, out);

        AxisEngine engine = new AxisEngine(configContext);
        MessageContext faultContext =
                MessageContextBuilder.createFaultMessageContext(msgContext, e);

        // SOAP 1.2 specification mentions that we should send HTTP code 400 in a fault if the
        // fault code Sender
        HttpServletResponse response =
                (HttpServletResponse) msgContext.getProperty(HTTPConstants.MC_HTTP_SERVLETRESPONSE);
        if (response != null) {
            SOAPFaultCode code = faultContext.getEnvelope().getBody().getFault().getCode();
            OMElement valueElement = null;
            if (code != null) {
                valueElement = code.getFirstChildWithName(new QName(
                        SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI,
                        SOAP12Constants.SOAP_FAULT_VALUE_LOCAL_NAME));
            }

            if (valueElement != null) {
                if (valueElement.getText().trim().indexOf(SOAP12Constants.FAULT_CODE_SENDER) >
                    -1 && !msgContext.isDoingREST()) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                }
            }
        }


        engine.sendFault(faultContext);
    }

    /**
     * Main init method
     *
     * @param config
     * @throws ServletException
     */
    public void init(ServletConfig config) throws ServletException {
        try {
            this.servletConfig = config;
            configContext = initConfigContext(config);

            axisConfiguration = configContext.getAxisConfiguration();
            config.getServletContext().setAttribute(CONFIGURATION_CONTEXT, configContext);

            ListenerManager listenerManager = new ListenerManager();
            listenerManager.init(configContext);
            TransportInDescription transportInDescription = new TransportInDescription(
                    new QName(Constants.TRANSPORT_HTTP));
            transportInDescription.setReceiver(this);
            listenerManager.addListener(transportInDescription, true);
            listenerManager.start();
            ListenerManager.defaultConfigurationContext = configContext;
            agent = new ListingAgent(configContext);

            initParams();

        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    /**
     * distroy the ConfigurationContext
     */
    public void destroy() {
        //stoping listner manager
        try {
            if (configContext != null) {
                configContext.terminate();
            }
        } catch (AxisFault axisFault) {
            log.info(axisFault.getMessage());
        }
        try {
            super.destroy();
        } catch (Exception e) {
            log.info(e.getMessage());
        }
    }

    /**
     * Initializes the Axis2 parameters.
     */
    protected void initParams() {
        Parameter parameter;
        // do we need to completely disable REST support
        parameter = axisConfiguration.getParameter(Constants.Configuration.DISABLE_REST);
        if (parameter != null) {
            disableREST = !JavaUtils.isFalseExplicitly(parameter.getValue());
        }

        // Should we close the reader(s)
        parameter = axisConfiguration.getParameter("axis2.close.reader");
        if (parameter != null) {
            closeReader = JavaUtils.isTrueExplicitly(parameter.getValue());
        }

    }

    /**
     * Convenient method to re-initialize the ConfigurationContext
     *
     * @throws ServletException
     */
    public void init() throws ServletException {
        if (this.servletConfig != null) {
            init(this.servletConfig);
        }
    }

    /**
     * Initialize the Axis configuration context
     *
     * @param config Servlet configuration
     * @return ConfigurationContext
     * @throws ServletException
     */
    protected ConfigurationContext initConfigContext(ServletConfig config) throws ServletException {
        try {
            ConfigurationContext configContext =
                    ConfigurationContextFactory
                            .createConfigurationContext(new WarBasedAxisConfigurator(config));
            configContext.setProperty(Constants.CONTAINER_MANAGED, Constants.VALUE_TRUE);
            return configContext;
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    /**
     * Set the context root if it is not set already.
     *
     * @param req
     */
    public void initContextRoot(HttpServletRequest req) {
        boolean findContext = true;
        String findContextParameter = servletConfig.getInitParameter("axis2.find.context");
        if (findContextParameter != null) {
            findContextParameter = findContextParameter.trim();
            findContext = JavaUtils.isTrue(findContextParameter);
        }
        if (!findContext) {
            if (contextRoot == null) {
                contextRoot = configContext.getContextRoot();
            }
        }
        if (contextRoot == null || "".equals(contextRoot)) {
            String[] parts = JavaUtils.split(req.getContextPath(), '/');
            if (parts != null) {
                for (int i = 0; i < parts.length; i++) {
                    if (parts[i].length() > 0) {
                        contextRoot = parts[i];
                        break;
                    }
                }
            }
            if (contextRoot == null || req.getContextPath().equals("/")) {
                contextRoot = "/";
            }
            configContext.setContextRoot(contextRoot);
        }
    }

    /**
     * Get all transport headers.
     *
     * @param req
     * @return Map
     */
    protected Map getTransportHeaders(HttpServletRequest req) {
        return new TransportHeaders(req);
    }


    public EndpointReference getEPRForService(String serviceName, String ip) throws AxisFault {
        return getEPRsForService(serviceName, ip)[0];
    }

    public EndpointReference[] getEPRsForService(String serviceName, String ip) throws AxisFault {
        //RUNNING_PORT
        String port = (String) configContext.getProperty(ListingAgent.RUNNING_PORT);
        if (port == null) {
            port = "8080";
        }
        if (ip == null) {
            try {
                ip = HttpUtils.getIpAddress();
                if (ip == null) {
                    ip = "localhost";
                }
            } catch (SocketException e) {
                throw new AxisFault(e);
            }
        }


        EndpointReference soapEndpoint = new EndpointReference("http://" + ip + ":" + port + '/' +
                                                               configContext
                                                                       .getServiceContextPath() +
                                                                                                "/" +
                                                                                                serviceName);

        if (!disableREST) {
            EndpointReference restEndpoint =
                    new EndpointReference("http://" + ip + ":" + port + '/' +
                                          configContext.getRESTContextPath() + "/" + serviceName);
            return new EndpointReference[]{soapEndpoint, restEndpoint};
        } else {
            return new EndpointReference[]{soapEndpoint};
        }

    }

    /**
     * init(); start() and stop() wouldn't do anything.
     *
     * @param axisConf
     * @param transprtIn
     * @throws AxisFault
     */
    public void init(ConfigurationContext axisConf,
                     TransportInDescription transprtIn) throws AxisFault {
    }

    public void start() throws AxisFault {
    }

    public void stop() throws AxisFault {
    }

    /**
     * @param request
     * @param response
     * @param invocationType : If invocationType=true; then this will be used in SOAP message
     *                       invocation. If invocationType=false; then this will be used in REST message invocation.
     * @return MessageContext
     * @throws IOException
     */
    protected MessageContext createMessageContext(HttpServletRequest request,
                                                  HttpServletResponse response,
                                                  boolean invocationType) throws IOException {
        MessageContext msgContext = ContextFactory.createMessageContext(configContext);
        String requestURI = request.getRequestURI();

        String trsPrefix = request.getRequestURL().toString();
        int sepindex = trsPrefix.indexOf(':');
        if (sepindex > -1) {
            trsPrefix = trsPrefix.substring(0, sepindex);
            msgContext.setIncomingTransportName(trsPrefix);
        } else {
            msgContext.setIncomingTransportName(Constants.TRANSPORT_HTTP);
        }
        TransportInDescription transportIn =
                axisConfiguration.getTransportIn(new QName(msgContext.getIncomingTransportName()));
        //set the default output description. This will be http
        TransportOutDescription transportOut = configContext.getAxisConfiguration()
                .getTransportOut(new QName(Constants.TRANSPORT_HTTP));

        msgContext.setTransportIn(transportIn);
        msgContext.setTransportOut(transportOut);
        msgContext.setServerSide(true);

        if (!invocationType) {
            String restPath = configContext.getRESTPath();
            if (restPath != null && requestURI.indexOf(restPath) != -1) {
                requestURI =
                        requestURI.replaceFirst(restPath, configContext.getServiceContextPath());
            }
            String query = request.getQueryString();
            if (query != null) {
                requestURI = requestURI + "?" + query;
            }
        }

        msgContext.setTo(new EndpointReference(requestURI));
        msgContext.setFrom(new EndpointReference(request.getRemoteAddr()));
        msgContext.setProperty(MessageContext.REMOTE_ADDR, request.getRemoteAddr());
        msgContext.setProperty(Constants.OUT_TRANSPORT_INFO,
                               new ServletBasedOutTransportInfo(response));
        // set the transport Headers
        msgContext.setProperty(MessageContext.TRANSPORT_HEADERS, getTransportHeaders(request));
        msgContext.setServiceGroupContextId(UUIDGenerator.getUUID());
        msgContext.setProperty(HTTPConstants.MC_HTTP_SERVLETREQUEST, request);
        msgContext.setProperty(HTTPConstants.MC_HTTP_SERVLETRESPONSE, response);

        return msgContext;
    }

    /**
     * This method assumes, that the created MessageContext will be used in only SOAP invocation.
     *
     * @param req
     * @param resp
     * @return MessageContext
     * @throws IOException
     */

    protected MessageContext createMessageContext(HttpServletRequest req,
                                                  HttpServletResponse resp) throws IOException {
        return createMessageContext(req, resp, true);
    }

    /**
     * This will match for content types that will be regarded as REST in WSDL2.0.
     * This contains,
     * 1. application/xml
     * 2. application/x-www-form-urlencoded
     * 3. multipart/form-data
     * <p/>
     * If the request doesnot contain a content type; this will return true.
     *
     * @param contentType
     * @return Boolean
     */
    private boolean isRESTRequest(String contentType) {
        return ((contentType == null ||
                 contentType.indexOf(HTTPConstants.MEDIA_TYPE_APPLICATION_XML) > -1 ||
                 contentType.indexOf(HTTPConstants.MEDIA_TYPE_X_WWW_FORM) > -1 ||
                 contentType.indexOf(HTTPConstants.MEDIA_TYPE_MULTIPART_FORM_DATA) > -1));
    }

    /**
     * Transport session management.
     *
     * @param messageContext
     * @return SessionContext
     */
    public SessionContext getSessionContext(MessageContext messageContext) {
        HttpServletRequest req = (HttpServletRequest) messageContext.getProperty(
                HTTPConstants.MC_HTTP_SERVLETREQUEST);
        SessionContext sessionContext =
                (SessionContext) req.getSession(true).getAttribute(
                        Constants.SESSION_CONTEXT_PROPERTY);
        String sessionId = req.getSession().getId();
        if (sessionContext == null) {
            sessionContext = new SessionContext(null);
            sessionContext.setCookieID(sessionId);
            req.getSession().setAttribute(Constants.SESSION_CONTEXT_PROPERTY,
                                          sessionContext);
        }
        messageContext.setSessionContext(sessionContext);
        messageContext.setProperty(SESSION_ID, sessionId);
        return sessionContext;
    }

    class ServletRequestResponseTransport implements RequestResponseTransport {
        private HttpServletResponse response;
        private CountDownLatch responseReadySignal = new CountDownLatch(1);
        RequestResponseTransportStatus status = RequestResponseTransportStatus.INITIAL;

        ServletRequestResponseTransport(HttpServletResponse response) {
            this.response = response;
        }

        public void acknowledgeMessage(MessageContext msgContext) throws AxisFault {
            log.debug("Acking one-way request");
            response.setContentType("text/xml; charset="
                                    + msgContext
                    .getProperty(Constants.Configuration.CHARACTER_SET_ENCODING));

            response.setStatus(HttpServletResponse.SC_ACCEPTED);
            try {
                response.flushBuffer();
            }
            catch (IOException e) {
                throw new AxisFault("Error sending acknowledgement", e);
            }

            signalResponseReady();
        }

        public void awaitResponse()
                throws InterruptedException {
            log.debug("Blocking servlet thread -- awaiting response");
            status = RequestResponseTransportStatus.WAITING;
            responseReadySignal.await();
        }

        public void signalResponseReady() {
            log.debug("Signalling response available");
            status = RequestResponseTransportStatus.SIGNALLED;
            responseReadySignal.countDown();
        }

        public RequestResponseTransportStatus getStatus() {
            return status;
        }

    }

    /**
     * Ues in processing REST related Requests.
     * This is the helper Class use in processing of doGet, doPut , doDelete and doPost.
     */
    private class ProcessRESTRequest {
        MessageContext messageContext;
        HttpServletRequest request;
        HttpServletResponse response;

        ProcessRESTRequest(String httpMethodString, HttpServletRequest request,
                           HttpServletResponse response)
                throws IOException {
            this.request = request;
            this.response = response;
            messageContext = createMessageContext(this.request, this.response, false);
            messageContext.setProperty(org.apache.axis2.transport.http.HTTPConstants.HTTP_METHOD,
                                       httpMethodString);


        }

        void processXMLRequest() throws IOException, ServletException {
            try {
                new RESTUtil(configContext).processPostRequest(messageContext, request, response);
                this.checkResponseWritten();
            } catch (AxisFault axisFault) {
                processFault(axisFault);
            }
            closeStaxBuilder(messageContext);


        }

        void processURLRequest() throws IOException, ServletException {
            try {
                new RESTUtil(configContext).processGetRequest(messageContext, request, response);
                this.checkResponseWritten();
            } catch (AxisFault axisFault) {
                processFault(axisFault);
            }
            closeStaxBuilder(messageContext);

        }

        void checkResponseWritten() {
            Object contextWritten =
                    messageContext.getOperationContext()
                            .getProperty(Constants.RESPONSE_WRITTEN);
            if ((contextWritten == null) || !Constants.VALUE_TRUE.equals(contextWritten)) {
                response.setStatus(HttpServletResponse.SC_ACCEPTED);
            }
        }

        void processFault(AxisFault e) throws ServletException, IOException {
            log.debug(e);
            if (messageContext != null) {
                processAxisFault(messageContext, response, response.getOutputStream(), e);
            } else {
                throw new ServletException(e);
            }

        }

    }
}
