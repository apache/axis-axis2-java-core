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

import org.apache.axis2.transport.http.util.SOAPUtil;
import org.apache.axis2.addressing.AddressingHelper;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.AxisFault;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.StatusLine;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import java.io.IOException;

/**
 * 
 */
public class AxisSOAPServlet extends AxisServlet {
	private static final Log log = LogFactory.getLog(AxisSOAPServlet.class);

    protected void doGet(HttpServletRequest httpServletRequest,
                         HttpServletResponse httpServletResponse) throws ServletException, IOException {

        // This should not be implemented, This is not supported.
    }

    protected void doPost(HttpServletRequest req,
                          HttpServletResponse resp) throws ServletException, IOException {

        MessageContext msgCtx = null;
        try {
            msgCtx = createMessageContext(req, resp);
            new SOAPUtil().processPostRequest(msgCtx,
                                              req,
                                              resp);
        } catch (Exception e) {
            log.error(e);
            if (msgCtx != null) {
                // If the fault is not going along the back channel we should be 202ing
                if(AddressingHelper.isFaultRedirected(msgCtx)){
                    resp.setStatus(HttpServletResponse.SC_ACCEPTED);
                }else{
                    resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                }
                handleFault(msgCtx, resp.getOutputStream(), new AxisFault(e));
            } else {
                throw new ServletException(e);
            }
        }
    }

    public void init(ServletConfig config) throws ServletException {
        ServletContext servletContext = config.getServletContext();
        this.configContext =
                (ConfigurationContext) servletContext.getAttribute(CONFIGURATION_CONTEXT);
        servletContext.setAttribute(this.getClass().getName(), this);
        this.servletConfig = config;
    }

    public void init() throws ServletException {
        if (this.servletConfig != null) {
            init(this.servletConfig);
        }
    }
}
