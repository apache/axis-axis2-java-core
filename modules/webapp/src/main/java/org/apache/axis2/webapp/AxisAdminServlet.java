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

package org.apache.axis2.webapp;

import org.apache.axis2.Constants;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.transport.http.AxisServlet;
import org.apache.axis2.transport.http.ForbidSessionCreationWrapper;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import java.io.IOException;
import java.lang.reflect.Method;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 *
 */
public class AxisAdminServlet extends AxisServlet {
    private static final long serialVersionUID = -6740625806509755370L;
    
    private final Random random = new SecureRandom();
    private final Map<String,ActionHandler> actionHandlers = new HashMap<String,ActionHandler>();

    private boolean axisSecurityEnabled() {
        Parameter parameter = configContext.getAxisConfiguration()
                .getParameter(Constants.ADMIN_SECURITY_DISABLED);
        return parameter == null || !"true".equals(parameter.getValue());
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action;
        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.isEmpty() || pathInfo.equals("/")) {
            action = "index";
        } else if (pathInfo.charAt(0) == '/') {
            action = pathInfo.substring(1);
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        ActionHandler actionHandler = actionHandlers.get(action);
        if (actionHandler != null) {
            if (actionHandler.isMethodAllowed(request.getMethod())) {
                if (!actionHandler.isSessionCreationAllowed()) {
                    request = new ForbidSessionCreationWrapper(request);
                }
                HttpSession session = request.getSession(false);
                if (actionHandler.isCSRFTokenRequired()) {
                    boolean tokenValid;
                    if (session == null) {
                        tokenValid = false;
                    } else {
                        CSRFTokenCache tokenCache = (CSRFTokenCache)session.getAttribute(CSRFTokenCache.class.getName());
                        if (tokenCache == null) {
                            tokenValid = false;
                        } else {
                            String token = request.getParameter("token");
                            tokenValid = token != null && tokenCache.isValid(token);
                        }
                    }
                    if (!tokenValid) {
                        response.sendError(HttpServletResponse.SC_FORBIDDEN, "No valid CSRF token found in request");
                        return;
                    }
                }
                request.setAttribute(Constants.SERVICE_PATH, configContext.getServicePath());
                if (session != null) {
                    String statusKey = request.getParameter("status");
                    if (statusKey != null) {
                        StatusCache statusCache = (StatusCache)session.getAttribute(StatusCache.class.getName());
                        if (statusCache != null) {
                            Status status = statusCache.get(statusKey);
                            if (status != null) {
                                request.setAttribute("status", status);
                            }
                        }
                    }
                }
                ActionResult result = actionHandler.handle(request, axisSecurityEnabled());
                result.process(request, new CSRFPreventionResponseWrapper(request, response, actionHandlers, random));
            } else {
                response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            }
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        ServletContext servletContext = config.getServletContext();
        this.configContext =
                (ConfigurationContext) servletContext.getAttribute(CONFIGURATION_CONTEXT);
        servletContext.setAttribute(this.getClass().getName(), this);
        AdminActions actions = new AdminActions(configContext);
        for (Method method : actions.getClass().getMethods()) {
            Action actionAnnotation = method.getAnnotation(Action.class);
            if (actionAnnotation != null) {
                Class<?>[] parameterTypes = method.getParameterTypes();
                if (parameterTypes.length != 1
                        || parameterTypes[0] != HttpServletRequest.class
                        || !ActionResult.class.isAssignableFrom(method.getReturnType())) {
                    throw new ServletException("Invalid method signature");
                }
                actionHandlers.put(
                        actionAnnotation.name(),
                        new ActionHandler(actions, method, actionAnnotation.authorizationRequired(),
                                actionAnnotation.post(), actionAnnotation.sessionCreationAllowed()));
            }
        }
        this.servletConfig = config;
    }

    @Override
    public void init() throws ServletException {
        if (this.servletConfig != null) {
            init(this.servletConfig);
        }
    }
}
