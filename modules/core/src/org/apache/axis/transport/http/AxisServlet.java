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
package org.apache.axis.transport.http;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.namespace.QName;
import javax.xml.stream.FactoryConfigurationError;

import org.apache.axis.Constants;
import org.apache.axis.context.ConfigurationContext;
import org.apache.axis.context.ConfigurationContextFactory;
import org.apache.axis.context.MessageContext;
import org.apache.axis.context.SessionContext;
import org.apache.axis.engine.AxisFault;
import org.apache.axis.om.OMException;

/**
 * Class AxisServlet
 */
public class AxisServlet extends HttpServlet {
    /**
     * Field engineRegistry
     */

    private ConfigurationContext configContext;

    private ListingAgent lister;

    /**
     * Method init
     *
     * @param config
     * @throws ServletException
     */
    public void init(ServletConfig config) throws ServletException {
        try {
            ServletContext context = config.getServletContext();
            String repoDir = context.getRealPath("/WEB-INF");
            ConfigurationContextFactory erfac = new ConfigurationContextFactory();
            configContext = erfac.buildConfigurationContext(repoDir);
            configContext.setProperty(Constants.CONTAINER_MANAGED, Constants.VALUE_TRUE);
            lister = new ListingAgent(configContext);
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    /**
     * Method doGet
     *
     * @param httpServletRequest
     * @param httpServletResponse
     * @throws ServletException
     * @throws IOException
     */
    protected void doGet(
        HttpServletRequest httpServletRequest,
        HttpServletResponse httpServletResponse)
        throws ServletException, IOException {
            httpServletResponse.setContentType("text/xml; charset=utf-8");
        try {

            Object sessionContext =
                httpServletRequest.getSession().getAttribute(Constants.SESSION_CONTEXT_PROPERTY);
            if (sessionContext == null) {
                sessionContext = new SessionContext(null);
                httpServletRequest.getSession().setAttribute(
                    Constants.SESSION_CONTEXT_PROPERTY,
                    sessionContext);
            }

            Enumeration enu = httpServletRequest.getParameterNames();
            HashMap map = new HashMap();
            while (enu.hasMoreElements()) {
                String name = (String) enu.nextElement();
                String value = httpServletRequest.getParameter(name);
                map.put(name, value);
            }

            MessageContext msgContext =
                new MessageContext(
                    configContext,
                    (SessionContext) sessionContext,
                    configContext.getAxisConfiguration().getTransportIn(
                        new QName(Constants.TRANSPORT_HTTP)),
                    configContext.getAxisConfiguration().getTransportOut(
                        new QName(Constants.TRANSPORT_HTTP)));
                    msgContext.setProperty(Constants.Configuration.DO_REST, Constants.VALUE_TRUE);
            msgContext.setServerSide(true);

            boolean processed =
                HTTPTransportUtils.processHTTPGetRequest(
                    msgContext,
                    httpServletRequest.getInputStream(),
                    httpServletResponse.getOutputStream(),
                    httpServletRequest.getContentType(),
                    httpServletRequest.getHeader(HTTPConstants.HEADER_SOAP_ACTION),
                    httpServletRequest.getRequestURL().toString(),
                    configContext,
                    map);
            if (!processed) {
                lister.handle(httpServletRequest, httpServletResponse);
            }
        } catch (OMException e) {
            throw new AxisFault(e);
        } catch (FactoryConfigurationError e) {
            throw new AxisFault(e);
        } catch (IOException e) {
            throw new AxisFault(e);
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
        try {
            System.out.println("came here");
            Object sessionContext =
                req.getSession().getAttribute(Constants.SESSION_CONTEXT_PROPERTY);
            if (sessionContext == null) {
                sessionContext = new SessionContext(null);
                req.getSession().setAttribute(Constants.SESSION_CONTEXT_PROPERTY, sessionContext);
            }
            MessageContext msgContext =
                new MessageContext(
                    configContext,
                    (SessionContext) sessionContext,
                    configContext.getAxisConfiguration().getTransportIn(
                        new QName(Constants.TRANSPORT_HTTP)),
                    configContext.getAxisConfiguration().getTransportOut(
                        new QName(Constants.TRANSPORT_HTTP)));

            res.setContentType("text/xml; charset=utf-8");
            HTTPTransportUtils.processHTTPPostRequest(
                msgContext,
                req.getInputStream(),
                res.getOutputStream(),
                req.getContentType(),
                req.getHeader(HTTPConstants.HEADER_SOAP_ACTION),
                req.getRequestURL().toString(),
                configContext);
            Object contextWritten = msgContext.getOperationContext().getProperty(Constants.RESPONSE_WRITTEN);
            if (contextWritten == null || !Constants.VALUE_TRUE.equals(contextWritten)) {
                res.setStatus(HttpServletResponse.SC_ACCEPTED);
            }
        } catch (AxisFault e) {
            throw new ServletException(e);
        }
    }

  

}
