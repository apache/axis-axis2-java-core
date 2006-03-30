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
import org.apache.axis2.context.ConfigurationContext;
import org.apache.log4j.Logger;

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
    private Logger log = Logger.getLogger(AxisSOAPServlet.class);

    protected void doGet(HttpServletRequest httpServletRequest,
                         HttpServletResponse httpServletResponse) throws ServletException, IOException {

        // This should not be implemented, This is not supported.
    }

    protected void doPost(HttpServletRequest req,
                          HttpServletResponse resp) throws ServletException, IOException {
        try {
            new SOAPUtil().processPostRequest(createMessageContext(req, resp),
                                              req,
                                              resp);
        } catch (Exception e) {
//            e.printStackTrace();
            log.error(e);
            throw new ServletException(e);
        }
    }

    public void init(ServletConfig config) throws ServletException {
        ServletContext servletContext = config.getServletContext();
        this.configContext =
                (ConfigurationContext) servletContext.getAttribute(CONFIGURATION_CONTEXT);
        servletContext.setAttribute("AxisSOAPServlet" + System.currentTimeMillis(),
                                    this);
        System.err.println("####### AxisSOAPServlet inited");
    }
}
