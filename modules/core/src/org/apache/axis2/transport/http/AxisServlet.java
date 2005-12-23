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
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.SessionContext;
import org.apache.axis2.engine.AxisEngine;

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
public class AxisServlet extends HttpServlet {
    private static final String CONFIGURATION_CONTEXT = "CONFIGURATION_CONTEXT";
    public static final String SESSION_ID = "SessionId";
    private ConfigurationContext configContext;
    private ListingAgent lister;

    private MessageContext createAndSetInitialParamsToMsgCtxt(Object sessionContext,
                                                              MessageContext msgContext, HttpServletResponse httpServletResponse,
                                                              HttpServletRequest httpServletRequest)
            throws AxisFault {
        msgContext =
                new MessageContext(configContext, (SessionContext) sessionContext,
                        configContext.getAxisConfiguration()
                                .getTransportIn(new QName(Constants
                                .TRANSPORT_HTTP)), configContext.getAxisConfiguration()
                        .getTransportOut(new QName(Constants.TRANSPORT_HTTP)));
        msgContext.setProperty(Constants.OUT_TRANSPORT_INFO,
                new ServletBasedOutTransportInfo(httpServletResponse));
        msgContext.setProperty(MessageContext.TRANSPORT_HEADERS,
                getTransportHeaders(httpServletRequest));
        msgContext.setProperty(SESSION_ID, httpServletRequest.getSession().getId());

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
                res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                handleFault(msgContext, out, e);
            } else {
                throw new ServletException(e);
            }
        }
    }

    private void handleFault(MessageContext msgContext, OutputStream out, AxisFault e)
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
            configContext = initConfigContext(config);
            lister = new ListingAgent(configContext);
            config.getServletContext().setAttribute(CONFIGURATION_CONTEXT, configContext);
        } catch (Exception e) {
            throw new ServletException(e);
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
            String repoDir = context.getRealPath("/WEB-INF");
            ConfigurationContextFactory erfac = new ConfigurationContextFactory();
            ConfigurationContext configContext = erfac.buildConfigurationContext(repoDir);
            configContext.setProperty(Constants.CONTAINER_MANAGED, Constants.VALUE_TRUE);
            configContext.setRootDir(new File(context.getRealPath("/WEB-INF")));
            return configContext;
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    private HashMap getHTTPParameters(HttpServletRequest httpServletRequest) {
        HashMap map = new HashMap();
        Enumeration enu = httpServletRequest.getParameterNames();

        while (enu.hasMoreElements()) {
            String name = (String) enu.nextElement();
            String value = httpServletRequest.getParameter(name);

            map.put(name, value);
        }

        return map;
    }

    private Object getSessionContext(HttpServletRequest httpServletRequest) {
        Object sessionContext =
                httpServletRequest.getSession(true).getAttribute(Constants.SESSION_CONTEXT_PROPERTY);

        if (sessionContext == null) {
            sessionContext = new SessionContext(null);
            httpServletRequest.getSession().setAttribute(Constants.SESSION_CONTEXT_PROPERTY,
                    sessionContext);
        }

        return sessionContext;
    }

    private Map getTransportHeaders(HttpServletRequest req) {
        HashMap headerMap = new HashMap();
        Enumeration headerNames = req.getHeaderNames();

        while (headerNames.hasMoreElements()) {
            String key = (String) headerNames.nextElement();
            String value = req.getHeader(key);

            headerMap.put(key, value);
        }

        return headerMap;
    }
}
