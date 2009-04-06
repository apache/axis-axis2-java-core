/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.axis2.transport.http;

import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.SessionContext;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.description.TransportInDescription;
import org.apache.axis2.transport.TransportListener;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.http.HttpServletRequest;

/**
 * This class is used just to add additional transports at runtime if user sends a request using
 * alternate schemes, example to handle http/https separately
 */
public class CustomListener implements TransportListener {

    private static final Log log = LogFactory.getLog(CustomListener.class);
    
    private int port;
    private String schema;
    private ConfigurationContext axisConf;

    public CustomListener(int port, String schema) {
        this.port = port;
        this.schema = schema;
    }

    public void init(ConfigurationContext axisConf,
                     TransportInDescription transprtIn) throws AxisFault {
        this.axisConf = axisConf;
        Parameter param = transprtIn.getParameter(PARAM_PORT);
        if (param != null) {
            this.port = Integer.parseInt((String) param.getValue());
        }
    }

    public void start() throws AxisFault {
    }

    public void stop() throws AxisFault {
    }

    public EndpointReference[] getEPRsForService(String serviceName, String ip)
            throws AxisFault {
        String path = axisConf.getServiceContextPath() + "/" + serviceName;
        if(path.charAt(0)!='/'){
            path = '/' + path;
        }
        return new EndpointReference[]{new EndpointReference(schema + "://" + ip + ":" + port + path + "/" )};
    }

    public EndpointReference getEPRForService(String serviceName, String ip) throws AxisFault {
        return getEPRsForService(serviceName, ip)[0];
    }

    public SessionContext getSessionContext(MessageContext messageContext) {
        HttpServletRequest req = (HttpServletRequest) messageContext.getProperty(
                HTTPConstants.MC_HTTP_SERVLETREQUEST);
        SessionContext sessionContext =
                (SessionContext) req.getSession(true).getAttribute(
                        Constants.SESSION_CONTEXT_PROPERTY);
        String sessionId = null;
        try {
            sessionId = req.getSession().getId();
            if (sessionContext == null) {
            sessionContext = new SessionContext(null);
            sessionContext.setCookieID(sessionId);
            req.getSession().setAttribute(Constants.SESSION_CONTEXT_PROPERTY,
                                          sessionContext);
        }
        } catch (Throwable t){
            log.info("Old Servlet API :" + t);
            return null;
        }
        messageContext.setSessionContext(sessionContext);
        messageContext.setProperty(AxisServlet.SESSION_ID, sessionId);
        return sessionContext;
    }

    public void destroy() {
    }

}
