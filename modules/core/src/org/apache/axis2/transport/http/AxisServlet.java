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
import org.apache.axis2.util.UUIDGenerator;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.SessionContext;
import org.apache.axis2.description.TransportInDescription;
import org.apache.axis2.description.TransportOutDescription;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.engine.AxisEngine;
import org.apache.axis2.engine.ListenerManager;
import org.apache.axis2.transport.TransportListener;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.namespace.QName;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * Class AxisServlet
 */
public class AxisServlet extends HttpServlet implements TransportListener {

    private Log log = LogFactory.getLog(getClass());
    private static final long serialVersionUID = -2085869393709833372L;
    public static final String CONFIGURATION_CONTEXT = "CONFIGURATION_CONTEXT";
    public static final String SESSION_ID = "SessionId";
    protected transient ConfigurationContext configContext;
    protected transient AxisConfiguration axisConfiguration;
    protected ListingAgent lister;

    private ServletConfig servletConfig;

    protected MessageContext createAndSetInitialParamsToMsgCtxt(Object sessionContext,
                                                                MessageContext msgContext, HttpServletResponse httpServletResponse,
                                                                HttpServletRequest httpServletRequest)
            throws AxisFault {
        msgContext = new MessageContext();
        msgContext.setConfigurationContext(configContext);
        msgContext.setSessionContext((SessionContext) sessionContext);
        msgContext.setTransportIn(axisConfiguration.getTransportIn(new QName(Constants
                .TRANSPORT_HTTP)));
        msgContext.setTransportOut(axisConfiguration.getTransportOut(new QName(Constants.TRANSPORT_HTTP)));

        msgContext.setProperty(Constants.OUT_TRANSPORT_INFO,
                new ServletBasedOutTransportInfo(httpServletResponse));
        msgContext.setProperty(MessageContext.REMOTE_ADDR, httpServletRequest.getRemoteAddr());
        msgContext.setProperty(MessageContext.TRANSPORT_HEADERS,
                getTransportHeaders(httpServletRequest));
        msgContext.setProperty(SESSION_ID, httpServletRequest.getSession().getId());
        msgContext.setProperty(Constants.Configuration.TRANSPORT_IN_URL, httpServletRequest.getRequestURL().toString());
        msgContext.setIncomingTransportName(Constants.TRANSPORT_HTTP);

        return msgContext;
    }

    public void destroy() {
        super.destroy();
    }

    /**
     * Method doGet
     *
     * @param httpServletRequest
     * @param httpServletResponse
     * @throws ServletException
     * @throws IOException
     */
    protected void doGet(HttpServletRequest httpServletRequest,
                         HttpServletResponse httpServletResponse)
            throws ServletException, IOException {

        //TODO: Remove impl after reviewing
        MessageContext msgContext = null;
        OutputStream out = null;

        try {
            Object sessionContext = getSessionContext(httpServletRequest);
            HashMap map = getHTTPParameters(httpServletRequest);

            msgContext = createAndSetInitialParamsToMsgCtxt(sessionContext, msgContext,
                    httpServletResponse, httpServletRequest);
            msgContext.setDoingREST(true);
            msgContext.setServerSide(true);
            out = httpServletResponse.getOutputStream();

            boolean processed = HTTPTransportUtils.processHTTPGetRequest(msgContext,
                    httpServletRequest.getInputStream(), out,
                    httpServletRequest.getContentType(),
                    httpServletRequest.getHeader(HTTPConstants.HEADER_SOAP_ACTION),
                    httpServletRequest.getRequestURL().toString(), configContext,
                    map);

            if (!processed) {
                lister.handle(httpServletRequest, httpServletResponse, out);
            }
        } catch (AxisFault e) {
            if (msgContext != null) {
                handleFault(msgContext, out, e);
            } else {
                throw new ServletException(e);
            }
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    /*
     * (non-Javadoc)
     * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */

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
        MessageContext msgContext = null;
        OutputStream out = null;

        try {
            Object sessionContext = getSessionContext(req);

            msgContext = createAndSetInitialParamsToMsgCtxt(sessionContext, msgContext, res, req);

            // adding ServletContext into msgContext;
            msgContext.setProperty(Constants.SERVLET_CONTEXT, sessionContext);
            out = res.getOutputStream();
            HTTPTransportUtils.processHTTPPostRequest(msgContext, req.getInputStream(), out,
                    req.getContentType(), req.getHeader(HTTPConstants.HEADER_SOAP_ACTION),
                    req.getRequestURL().toString());

            Object contextWritten =
                    msgContext.getOperationContext().getProperty(Constants.RESPONSE_WRITTEN);

            res.setContentType("text/xml; charset="
                    + msgContext.getProperty(MessageContext.CHARACTER_SET_ENCODING));

            if ((contextWritten == null) || !Constants.VALUE_TRUE.equals(contextWritten)) {
                res.setStatus(HttpServletResponse.SC_ACCEPTED);
            }
        } catch (AxisFault e) {
            if (msgContext != null) {
                try {
                    handleFault(msgContext, out, e);
                } catch (AxisFault e2) {
                    log.info(e.getMessage());
                }
            } else {
                throw new ServletException(e);
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
            lister = new ListingAgent(configContext);
            axisConfiguration = configContext.getAxisConfiguration();
            config.getServletContext().setAttribute(CONFIGURATION_CONTEXT, configContext);
            ListenerManager listenerManager = new ListenerManager();
            listenerManager.init(configContext);
            TransportInDescription transportInDescription = new TransportInDescription(
                    new QName(Constants.TRANSPORT_HTTP));
            transportInDescription.setReceiver(this);
            listenerManager.addListener(transportInDescription, true);
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    public void init() throws ServletException {
        if(this.servletConfig != null){
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
            ServletContext context = config.getServletContext();
            String repoDir = config.getInitParameter("repository");
            if(repoDir == null || repoDir.trim().length() == 0){
                repoDir = context.getRealPath("/WEB-INF");
            } else {
                repoDir = context.getRealPath(repoDir);
            }

            //adding weblocation property
            setWebLocationProperty(context);
            ConfigurationContext configContext =
                    ConfigurationContextFactory.createConfigurationContextFromFileSystem(repoDir, null);
            configContext.setProperty(Constants.CONTAINER_MANAGED, Constants.VALUE_TRUE);
            configContext.setRootDir(new File(context.getRealPath("/WEB-INF")));
            return configContext;
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    /**
     * To find out the location where web reposurce need to be coiped, when
     * deployment fine any service aar with web resources.
     *
     * @param context
     */
    private void setWebLocationProperty(ServletContext context) {
        String webpath = context.getRealPath("");
        if (webpath == null || "".equals(webpath)) {
            return;
        }
        File weblocation = new File(webpath);
        System.setProperty("web.location", weblocation.getAbsolutePath());
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
        HashMap headerMap = new HashMap();
        Enumeration headerNames = req.getHeaderNames();

        while (headerNames.hasMoreElements()) {
            String key = (String) headerNames.nextElement();
            String value = req.getHeader(key);

            headerMap.put(key, value);
        }

        return headerMap;
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
        //RUNNING_PORT
        String port = System.getProperty(ListingAgent.RUNNING_PORT);
        if (port == null) {
            port = "8080";
        }
        return new EndpointReference("http://" + ip + ":" + port + "/axis2/services/" + serviceName);
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
        requestURI = requestURI.replaceFirst("rest", "services");
        msgContext.setTo(new EndpointReference(requestURI));
        msgContext.setProperty(Constants.OUT_TRANSPORT_INFO,
                               new ServletBasedOutTransportInfo(resp));
//        msgContext.setProperty(MessageContext.TRANSPORT_OUT, resp.getOutputStream());

        // set the transport Headers
        msgContext.setProperty(MessageContext.TRANSPORT_HEADERS, getHeaders(req));
        msgContext.setServiceGroupContextId(UUIDGenerator.getUUID());
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
}
