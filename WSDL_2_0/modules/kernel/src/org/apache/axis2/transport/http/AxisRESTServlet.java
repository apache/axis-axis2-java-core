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
import org.apache.axis2.description.AxisBindingOperation;
import org.apache.axis2.description.AxisBindingMessage;
import org.apache.axis2.description.WSDL2Constants;
import org.apache.axis2.addressing.AddressingHelper;
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
import java.io.OutputStream;
import java.io.PrintWriter;

/**
 * 
 */
public class AxisRESTServlet extends AxisServlet {

    private static final Log log = LogFactory.getLog(AxisRESTServlet.class);

    protected void doGet(HttpServletRequest req,
                         HttpServletResponse resp) throws ServletException, IOException {
        if (!disableREST && !disableSeperateEndpointForREST) {
            MessageContext messageContext = null;
            OutputStream out = resp.getOutputStream();
            try {
                messageContext = createMessageContext(req, resp);
                messageContext.setProperty(org.apache.axis2.transport.http.HTTPConstants.HTTP_METHOD, Constants.Configuration.HTTP_METHOD_GET);
                new RESTUtil(configContext).processGetRequest(messageContext,
                        req,
                        resp);
                Object contextWritten =
                        messageContext.getOperationContext().getProperty(Constants.RESPONSE_WRITTEN);
                if ((contextWritten == null) || !Constants.VALUE_TRUE.equals(contextWritten)) {
                    resp.setStatus(HttpServletResponse.SC_ACCEPTED);
                }
            } catch (AxisFault e) {
                log.debug(e);
                if (messageContext != null) {
                    processAxisFault(messageContext, resp, out, e);
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

    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        initContextRoot(req);

        MessageContext msgContext;
        OutputStream out = resp.getOutputStream();
        if (!disableREST && !disableSeperateEndpointForREST) {
            msgContext = createMessageContext(req, resp);
            try {

                new RESTUtil(configContext).processPostRequest(msgContext, req, resp);
                Object contextWritten =
                        msgContext.getOperationContext().getProperty(Constants.RESPONSE_WRITTEN);
                if ((contextWritten == null) || !Constants.VALUE_TRUE.equals(contextWritten)) {
                    resp.setStatus(HttpServletResponse.SC_ACCEPTED);
                }
            } catch (AxisFault e) {
                log.debug(e);
                if (msgContext != null) {
                    processAxisFault(msgContext, resp, out, e);
                } else {
                    throw new ServletException(e);
                }
            }
        }
    }

    protected void doDelete(HttpServletRequest req,
                         HttpServletResponse resp) throws ServletException, IOException {

        initContextRoot(req);

        // this method is also used to serve for the listServices request.

        if (!disableREST && enableRESTInAxis2MainServlet) { // if the main servlet should handle REST also
            MessageContext messageContext = null;
            OutputStream out = resp.getOutputStream();
            try {
                messageContext = createMessageContext(req, resp);
                messageContext.setProperty(org.apache.axis2.transport.http.HTTPConstants.HTTP_METHOD, Constants.Configuration.HTTP_METHOD_DELETE);
                new RESTUtil(configContext).processGetRequest(messageContext,
                        req,
                        resp);
                Object contextWritten =
                        messageContext.getOperationContext().getProperty(Constants.RESPONSE_WRITTEN);
                if ((contextWritten == null) || !Constants.VALUE_TRUE.equals(contextWritten)) {
                    resp.setStatus(HttpServletResponse.SC_ACCEPTED);
                }
            } catch (AxisFault e) {
                                log.debug(e);
                if (messageContext != null) {
                    processAxisFault(messageContext, resp, out, e);
                }}
        } else {
            PrintWriter writer = new PrintWriter(resp.getOutputStream());
            writer.println("<html><body><h2>Please enable REST support in WEB-INF/conf/axis2.xml and WEB-INF/web.xml</h2></body></html>");
            writer.flush();
            resp.setStatus(HttpServletResponse.SC_ACCEPTED);
        }
    }

    protected void doPut(HttpServletRequest req,
                         HttpServletResponse resp) throws ServletException, IOException {

        initContextRoot(req);

        // this method is also used to serve for the listServices request.

        if (!disableREST && enableRESTInAxis2MainServlet) { // if the main servlet should handle REST also
            MessageContext messageContext = null;
            OutputStream out = resp.getOutputStream();
            try {
                messageContext = createMessageContext(req, resp);
                messageContext.setProperty(org.apache.axis2.transport.http.HTTPConstants.HTTP_METHOD, Constants.Configuration.HTTP_METHOD_PUT);
                new RESTUtil(configContext).processPostRequest(messageContext,
                        req,
                        resp);
                Object contextWritten =
                        messageContext.getOperationContext().getProperty(Constants.RESPONSE_WRITTEN);
                if ((contextWritten == null) || !Constants.VALUE_TRUE.equals(contextWritten)) {
                    resp.setStatus(HttpServletResponse.SC_ACCEPTED);
                }
            } catch (AxisFault e) {
                                log.debug(e);
                if (messageContext != null) {
                    processAxisFault(messageContext, resp, out, e);
                }}
        } else {
            PrintWriter writer = new PrintWriter(resp.getOutputStream());
            writer.println("<html><body><h2>Please enable REST support in WEB-INF/conf/axis2.xml and WEB-INF/web.xml</h2></body></html>");
            writer.flush();
            resp.setStatus(HttpServletResponse.SC_ACCEPTED);
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

     private void processAxisFault(MessageContext msgContext, HttpServletResponse res, OutputStream out, AxisFault e) {
        try {
            // If the fault is not going along the back channel we should be 202ing
            if (AddressingHelper.isFaultRedirected(msgContext)) {
                res.setStatus(HttpServletResponse.SC_ACCEPTED);
            } else {

                res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

                AxisBindingOperation axisBindingOperation = (AxisBindingOperation) msgContext.getProperty(Constants.AXIS_BINDING_OPERATION);
                if (axisBindingOperation != null) {
                    AxisBindingMessage fault = axisBindingOperation.getFault((String) msgContext.getProperty(Constants.FAULT_NAME));
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
}
