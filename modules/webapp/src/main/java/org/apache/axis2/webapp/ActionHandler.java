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

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.axis2.Constants;

final class ActionHandler {
    private final Object target;
    private final Method method;
    private final boolean authorizationRequired;
    private final boolean post;
    private final boolean sessionCreationAllowed;

    ActionHandler(Object target, Method method, boolean authorizationRequired, boolean post,
            boolean sessionCreationAllowed) {
        this.target = target;
        this.method = method;
        this.authorizationRequired = authorizationRequired;
        this.post = post;
        this.sessionCreationAllowed = sessionCreationAllowed;
    }

    boolean isMethodAllowed(String method) {
        return post ? method.equals("POST") : method.equals("GET");
    }

    boolean isCSRFTokenRequired() {
        return post && authorizationRequired;
    }

    boolean isSessionCreationAllowed() {
        return sessionCreationAllowed;
    }

    ActionResult handle(HttpServletRequest request, boolean securityEnabled) throws IOException, ServletException {
        HttpSession session = request.getSession(false);
        if (securityEnabled && authorizationRequired && (session == null || session.getAttribute(Constants.LOGGED) == null)) {
            return new Redirect("welcome");
        } else {
            try {
                return (ActionResult)method.invoke(target, request);
            } catch (IllegalAccessException ex) {
                throw new ServletException(ex);
            } catch (IllegalArgumentException ex) {
                throw new ServletException(ex);
            } catch (InvocationTargetException ex) {
                Throwable cause = ex.getCause();
                if (cause instanceof IOException) {
                    throw (IOException)cause;
                } else if (cause instanceof ServletException) {
                    throw (ServletException)cause;
                } else {
                    throw new ServletException(cause);
                }
            }
        }
    }
}
