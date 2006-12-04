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

import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.AddressingHelper;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.SessionContext;
import org.apache.axis2.deployment.WarBasedAxisConfigurator;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.description.TransportInDescription;
import org.apache.axis2.description.TransportOutDescription;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.engine.AxisEngine;
import org.apache.axis2.engine.ListenerManager;
import org.apache.axis2.transport.RequestResponseTransport;
import org.apache.axis2.transport.TransportListener;
import org.apache.axis2.transport.http.server.HttpUtils;
import org.apache.axis2.transport.http.util.RESTUtil;
import org.apache.axis2.util.JavaUtils;
import org.apache.axis2.util.UUIDGenerator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.axiom.om.impl.builder.StAXBuilder;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.namespace.QName;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * Class AxisServlet
 */
public class AxisServlet extends HttpServlet implements TransportListener {

    private static final Log log = LogFactory.getLog(AxisServlet.class);
    private static final long serialVersionUID = -2085869393709833372L;
    public static final String CONFIGURATION_CONTEXT = "CONFIGURATION_CONTEXT";
    public static final String SESSION_ID = "SessionId";
    protected transient ConfigurationContext configContext;
    protected transient AxisConfiguration axisConfiguration;

    protected transient ServletConfig servletConfig;

    private transient ListingAgent agent;
    private String contextRoot = null;

    protected boolean enableRESTInAxis2MainServlet = false;
    protected boolean disableREST = false;
    protected boolean disableSeperateEndpointForREST = false;
    private static final String LIST_SERVICES_SUFIX = "/services/listServices";
    private static final String LIST_FAUKT_SERVICES_SUFIX = "/services/ListFaultyServices";
    private boolean closeReader = true;

    protected MessageContext
    createAndSetInitialParamsToMsgCtxt(HttpServletResponse resp,
                                       HttpServletRequest req) throws AxisFault {
        MessageContext msgContext = new MessageContext();
        if (axisConfiguration.isManageTransportSession()) {
            // We need to create this only if transport session is enabled.
            Object sessionContext = getSessionContext(req);
            msgContext.setSessionContext((SessionContext) sessionContext);
            msgContext.setProperty(SESSION_ID, req.getSession().getId());
        }

        msgContext.setConfigurationContext(configContext);
        msgContext.setTransportIn(axisConfiguration.getTransportIn(new QName(Constants
                .TRANSPORT_HTTP)));
        msgContext.setTransportOut(axisConfiguration.getTransportOut(new QName(Constants.TRANSPORT_HTTP)));

        msgContext.setProperty(Constants.OUT_TRANSPORT_INFO,
                new ServletBasedOutTransportInfo(resp));
        msgContext.setProperty(RequestResponseTransport.TRANSPORT_CONTROL,
                               new ServletRequestResponseTransport(resp));
        msgContext.setProperty(MessageContext.REMOTE_ADDR, req.getRemoteAddr());
        msgContext.setFrom(new EndpointReference(req.getRemoteAddr()));
        msgContext.setProperty(MessageContext.TRANSPORT_HEADERS,
                getTransportHeaders(req));
        msgContext.setProperty(Constants.Configuration.TRANSPORT_IN_URL, req.getRequestURL().toString());
        msgContext.setIncomingTransportName(Constants.TRANSPORT_HTTP);
        msgContext.setProperty(HTTPConstants.MC_HTTP_SERVLETREQUEST, req);
//        msgContext.setProperty(HTTPConstants.MC_HTTP_SERVLETCONTEXT, servletConfig.getServletContext());
        return msgContext;
    }

    public void destroy() {
        super.destroy();
        //stoping listner manager
        try {
            configContext.getListenerManager().stop();
        } catch (AxisFault axisFault) {
            log.info(axisFault.getMessage());
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
        if(!findContext) {
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

    /*
    * (non-Javadoc)
    * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
    */


    protected void doGet(HttpServletRequest req,
                         HttpServletResponse resp) throws ServletException, IOException {

        initContextRoot(req);

        // this method is also used to serve for the listServices request.

        String requestURI = req.getRequestURI();
        String query = req.getQueryString();

        // There can be three different request coming to this.
        // 1. wsdl, wsdl2 and xsd requests
        // 2. list services requests
        // 3. REST requests.
        if ((query != null) && (query.indexOf("wsdl2") >= 0 ||
                query.indexOf("wsdl") >= 0 || query.indexOf("xsd") >= 0 || query.indexOf("policy") >= 0)) { // handling meta data exchange stuff
            agent.processListService(req, resp);
        } else
        if (requestURI.endsWith(LIST_SERVICES_SUFIX) || requestURI.endsWith(LIST_FAUKT_SERVICES_SUFIX)) { // handling list services request
            try {
                agent.handle(req, resp);
            } catch (Exception e) {
                throw new ServletException(e);
            }
        } else
        if (!disableREST && enableRESTInAxis2MainServlet) { // if the main servlet should handle REST also
            MessageContext messageContext = null;
            try {
                messageContext = createMessageContext(req, resp);
                new RESTUtil(configContext).processGetRequest(messageContext,
                        req,
                        resp);
            } catch (Exception e) {
                log.error(e);
                if (messageContext != null) {
                    resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    handleFault(messageContext, resp.getOutputStream(), new AxisFault(e));
                } else {
                    throw new ServletException(e);
                }
            }
        } else {
            PrintWriter writer = new PrintWriter(resp.getOutputStream());
            writer.println("<html><body><h2>Please enable REST support in WEB-INF/conf/axis2.xml and WEB-INF/web.xml</h2></body></html>");
            writer.flush();
            resp.setStatus(HttpServletResponse.SC_ACCEPTED);
        }
    }

    /**
     * Method doPost
     *
     * @param req
     * @param res
     * @throws ServletException
     * @throws IOException
     */
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        initContextRoot(req);

        MessageContext msgContext;
        OutputStream out = res.getOutputStream();

        if (!disableREST && enableRESTInAxis2MainServlet && isRESTRequest(req)) {
            msgContext = createMessageContext(req, res);
            try {
                new RESTUtil(configContext).processPostRequest(msgContext,
                        req,
                        res);
            } catch (Exception e) {
                log.error(e);
                if (msgContext != null) {
                    res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    handleFault(msgContext, out, new AxisFault(e));
                } else {
                    throw new ServletException(e);
                }
            }
        } else {
            msgContext = createAndSetInitialParamsToMsgCtxt(res, req);

            try {
                // adding ServletContext into msgContext;
                out = res.getOutputStream();
                HTTPTransportUtils.processHTTPPostRequest(msgContext, req.getInputStream(), out,
                        req.getContentType(), req.getHeader(HTTPConstants.HEADER_SOAP_ACTION),
                        req.getRequestURL().toString());

                Object contextWritten =
                        msgContext.getOperationContext().getProperty(Constants.RESPONSE_WRITTEN);

                res.setContentType("text/xml; charset="
                        + msgContext.getProperty(Constants.Configuration.CHARACTER_SET_ENCODING));

                if ((contextWritten == null) || !Constants.VALUE_TRUE.equals(contextWritten)) {
                    res.setStatus(HttpServletResponse.SC_ACCEPTED);
                }
            } catch (AxisFault e) {
                log.debug(e);
                if (msgContext != null) {
                    try {
                        // If the fault is not going along the back channel we should be 202ing
                        if (AddressingHelper.isFaultRedirected(msgContext)) {
                            res.setStatus(HttpServletResponse.SC_ACCEPTED);
                        } else {
                            res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                        }
                        handleFault(msgContext, out, e);
                    } catch (AxisFault e2) {
                        log.info(e2);
                    }
                } else {
                    throw new ServletException(e);
                }
            } catch (Throwable t) {
                log.error(t);
                if (msgContext != null) {
                    try {
                        // If the fault is not going along the back channel we should be 202ing
                        if (AddressingHelper.isFaultRedirected(msgContext)) {
                            res.setStatus(HttpServletResponse.SC_ACCEPTED);
                        } else {
                            res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                        }
                        handleFault(msgContext, out, new AxisFault(t.toString(), t));
                    } catch (AxisFault e2) {
                        log.info(e2);
                    }
                } else {
                    throw new ServletException(t);
                }
            }
        }
        if(closeReader){
            try {
                ((StAXBuilder)msgContext.getEnvelope().getBuilder()).close();
            } catch (Exception e){
                log.debug(e);
            }
        }
    }

    protected void handleFault(MessageContext msgContext, OutputStream out, AxisFault e)
            throws AxisFault {
        msgContext.setProperty(MessageContext.TRANSPORT_OUT, out);

        AxisEngine engine = new AxisEngine(configContext);
        MessageContext faultContext = engine.createFaultMessageContext(msgContext, e);

        engine.sendFault(faultContext);
    }

    /**
     * Method init
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
            ListenerManager.defaultConfigurationContext = configContext;
            agent = new ListingAgent(configContext);

            initParams();

        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    protected void initParams() {
        // do we need to enable REST in the main servlet so that it handles both REST and SOAP messages
        Parameter parameter = axisConfiguration.getParameter(Constants.Configuration.ENABLE_REST_IN_AXIS2_MAIN_SERVLET);
        if (parameter != null) {
            enableRESTInAxis2MainServlet = !JavaUtils.isFalseExplicitly(parameter.getValue());
        }

        // do we need to completely disable REST support
        parameter = axisConfiguration.getParameter(Constants.Configuration.DISABLE_REST);
        if (parameter != null) {
            disableREST = !JavaUtils.isFalseExplicitly(parameter.getValue());
        }

        // Do we need to have a separate endpoint for REST
        parameter = axisConfiguration.getParameter(Constants.Configuration.DISABLE_SEPARATE_ENDPOINT_FOR_REST);
        if (parameter != null) {
            disableSeperateEndpointForREST = !JavaUtils.isFalseExplicitly(parameter.getValue());
        }

        // Should we close the reader(s)
        parameter = axisConfiguration.getParameter("axis2.close.reader");
        if (parameter != null) {
            closeReader = JavaUtils.isTrueExplicitly(parameter.getValue());
        }
    }

    public void init() throws ServletException {
        if (this.servletConfig != null) {
            init(this.servletConfig);
        }
    }

    /**
     * Initialize the Axis configuration context
     *
     * @param config Servlet configuration
     * @throws ServletException
     */
    protected ConfigurationContext initConfigContext(ServletConfig config) throws ServletException {
        try {
            ConfigurationContext configContext =
                    ConfigurationContextFactory.createConfigurationContext(new WarBasedAxisConfigurator(config));
            configContext.setProperty(Constants.CONTAINER_MANAGED, Constants.VALUE_TRUE);
            return configContext;
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    protected HashMap getHTTPParameters(HttpServletRequest httpServletRequest) {
        HashMap map = new HashMap();
        Enumeration enu = httpServletRequest.getParameterNames();

        while (enu.hasMoreElements()) {
            String name = (String) enu.nextElement();
            String value = httpServletRequest.getParameter(name);

            map.put(name, value);
        }

        return map;
    }

    protected Object getSessionContext(HttpServletRequest httpServletRequest) {
        Object sessionContext =
                httpServletRequest.getSession(true).getAttribute(Constants.SESSION_CONTEXT_PROPERTY);
        if (sessionContext == null) {
            sessionContext = new SessionContext(null);
            httpServletRequest.getSession().setAttribute(Constants.SESSION_CONTEXT_PROPERTY,
                    sessionContext);
        }
        return sessionContext;
    }

    protected Map getTransportHeaders(HttpServletRequest req) {
        return new TransportHeaders(req);
    }

    /**
     * To initilze as TransportListener , not as Servlet
     *
     * @param axisConf
     * @param transprtIn
     * @throws AxisFault
     */
    public void init(ConfigurationContext axisConf,
                     TransportInDescription transprtIn) throws AxisFault {
        // no need to do anything :)
    }

    public void start() throws AxisFault {
        // no need to do anything :) , it is already started
    }

    public void stop() throws AxisFault {
        // no one call thie method
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
                configContext.getServiceContextPath() + "/" + serviceName);

        if (!disableREST && !disableSeperateEndpointForREST) {
            EndpointReference restEndpoint = new EndpointReference("http://" + ip + ":" + port + '/' +
                    configContext.getRESTContextPath() + "/" + serviceName);
            return new EndpointReference[]{soapEndpoint, restEndpoint};
        } else {
            return new EndpointReference[]{soapEndpoint};
        }

    }

    protected MessageContext createMessageContext(HttpServletRequest req,
                                                  HttpServletResponse resp) throws IOException {
        MessageContext msgContext = new MessageContext();
        String trsPrefix = req.getRequestURL().toString();
        int sepindex = trsPrefix.indexOf(':');
        if (sepindex >= 0) {
            trsPrefix = trsPrefix.substring(0, sepindex);
            msgContext.setIncomingTransportName(trsPrefix);
        } else {
            msgContext.setIncomingTransportName(Constants.TRANSPORT_HTTP);
        }
        msgContext.setConfigurationContext(configContext);
        msgContext.setTransportIn(configContext.getAxisConfiguration().
                getTransportIn(new QName(Constants.TRANSPORT_HTTP)));
        TransportOutDescription transportOut =
                configContext.getAxisConfiguration().getTransportOut(
                        new QName(Constants.TRANSPORT_HTTP));
        msgContext.setTransportOut(transportOut);
        msgContext.setServerSide(true);

        String requestURI = req.getRequestURI();
        if (requestURI.indexOf("rest") != -1) {
            requestURI = requestURI.replaceFirst("rest", configContext.getServiceContextPath());
        }
        msgContext.setTo(new EndpointReference(requestURI));
        msgContext.setFrom(new EndpointReference(req.getRemoteAddr()));
        msgContext.setProperty(MessageContext.REMOTE_ADDR, req.getRemoteAddr());
        msgContext.setProperty(Constants.OUT_TRANSPORT_INFO,
                new ServletBasedOutTransportInfo(resp));
//        msgContext.setProperty(MessageContext.TRANSPORT_OUT, resp.getOutputStream());

        // set the transport Headers
        msgContext.setProperty(MessageContext.TRANSPORT_HEADERS, getHeaders(req));
        msgContext.setServiceGroupContextId(UUIDGenerator.getUUID());
        msgContext.setProperty(HTTPConstants.MC_HTTP_SERVLETREQUEST, req);
        return msgContext;
    }

    protected Map getHeaders(HttpServletRequest request) {
        HashMap headerMap = new HashMap();
        Enumeration e = request.getAttributeNames();
        while (e.hasMoreElements()) {
            String field = (String) e.nextElement();
            headerMap.put(field, request.getAttribute(field));
        }
        return headerMap;
    }

    /**
     * Lets only handle
     * - text/xml
     * - application/x-www-form-urlencoded
     * as REST content types in this servlet.
     *
     * @param request
     */
    private boolean isRESTRequest(HttpServletRequest request) {
        String contentType = request.getContentType();
        String soapActionHeader = request.getHeader(HTTPConstants.HEADER_SOAP_ACTION);

        return ((soapActionHeader == null) ||
                (contentType != null && contentType.indexOf(HTTPConstants.MEDIA_TYPE_X_WWW_FORM) > -1));
    }
    
    class ServletRequestResponseTransport implements RequestResponseTransport
    {
      private HttpServletResponse response;
      
      ServletRequestResponseTransport(HttpServletResponse response)
      {
        this.response = response;
      }
      
      public void acknowledgeMessage(MessageContext msgContext) throws AxisFault
      {
        response.setContentType("text/xml; charset="
                                + msgContext.getProperty(Constants.Configuration.CHARACTER_SET_ENCODING));
        
        response.setStatus(HttpServletResponse.SC_ACCEPTED);
        try
        {
          response.flushBuffer();
        }
        catch (IOException e)
        {
          throw new AxisFault("Error sending acknowledgement", e);
        }
      }
    }
}
