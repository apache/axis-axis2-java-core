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
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.transport.http.util.RESTUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 
 */
public class AxisRESTServlet extends AxisServlet {

    private static final Log log = LogFactory.getLog(AxisRESTServlet.class);

    protected void doGet(HttpServletRequest req,
                         HttpServletResponse resp) throws ServletException, IOException {
        if (!disableREST && !disableSeperateEndpointForREST) {
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
        }
    }

    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        if (!disableREST && !disableSeperateEndpointForREST) {
            MessageContext messageContext = null;
            try {
                messageContext = createMessageContext(req, resp);
                new RESTUtil(configContext).processPostRequest(messageContext,
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
        }
    }

    public void init(ServletConfig config) throws ServletException {
        ServletContext servletContext = config.getServletContext();
        this.configContext =
                (ConfigurationContext) servletContext.getAttribute(CONFIGURATION_CONTEXT);
        servletContext.setAttribute(this.getClass().getName(), this);
        this.servletConfig = config;

        axisConfiguration = configContext.getAxisConfiguration();

        initParams();
    }

    public void init() throws ServletException {
        if (this.servletConfig != null) {
            init(this.servletConfig);
        }
    }
}
