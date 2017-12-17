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
import java.net.URLEncoder;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

final class Redirect extends ActionResult {
    private final String action;
    private final Map<String,String> parameters = new LinkedHashMap<String,String>();
    private Status status;

    Redirect(String action) {
        this.action = action;
    }

    Redirect withParameter(String name, String value) {
        parameters.put(name, value);
        return this;
    }

    Redirect withStatus(boolean success, String message) {
        this.status = new Status(success, message);
        return this;
    }

    @Override
    void process(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        StringBuilder url = new StringBuilder(response.encodeRedirectURL(action));
        if (status != null) {
            HttpSession session = request.getSession();
            StatusCache statusCache = (StatusCache)session.getAttribute(StatusCache.class.getName());
            if (statusCache == null) {
                statusCache = new StatusCache();
            }
            parameters.put("status", statusCache.add(status));
            session.setAttribute(StatusCache.class.getName(), statusCache);
        }
        boolean first = true;
        for (Map.Entry<String,String> parameter : parameters.entrySet()) {
            if (first) {
                url.append('?');
                first = false;
            } else {
                url.append('&');
            }
            url.append(parameter.getKey());
            url.append('=');
            url.append(URLEncoder.encode(parameter.getValue(), "UTF-8"));
        }
        response.sendRedirect(url.toString());
    }
}
