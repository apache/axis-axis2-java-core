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

import java.util.Map;
import java.util.Random;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

final class CSRFPreventionResponseWrapper extends HttpServletResponseWrapper {
    private static final Log log = LogFactory.getLog(CSRFPreventionResponseWrapper.class);

    private final HttpServletRequest request;
    private final Map<String,ActionHandler> actionHandlers;
    private final Random random;
    private String token;

    CSRFPreventionResponseWrapper(HttpServletRequest request, HttpServletResponse response, Map<String,ActionHandler> actionHandlers, Random random) {
        super(response);
        this.request = request;
        this.actionHandlers = actionHandlers;
        this.random = random;
    }

    protected String getToken() {
        if (token == null) {
            HttpSession session = request.getSession(false);
            if (session == null) {
                throw new IllegalStateException();
            }
            CSRFTokenCache tokenCache;
            synchronized (session) {
                tokenCache = (CSRFTokenCache)session.getAttribute(CSRFTokenCache.class.getName());
                if (tokenCache == null) {
                    tokenCache = new CSRFTokenCache();
                    session.setAttribute(CSRFTokenCache.class.getName(), tokenCache);
                }
            }
            byte[] bytes = new byte[16];
            StringBuilder buffer = new StringBuilder();
            random.nextBytes(bytes);
            for (int j = 0; j < bytes.length; j++) {
                byte b1 = (byte)((bytes[j] & 0xf0) >> 4);
                byte b2 = (byte)(bytes[j] & 0x0f);
                if (b1 < 10) {
                    buffer.append((char)('0' + b1));
                } else {
                    buffer.append((char)('A' + (b1 - 10)));
                }
                if (b2 < 10) {
                    buffer.append((char)('0' + b2));
                } else {
                    buffer.append((char)('A' + (b2 - 10)));
                }
            }
            token = buffer.toString();
            tokenCache.add(token);
        }
        return token;
    }

    @Override
    public String encodeUrl(String url) {
        return encodeURL(url);
    }

    @Override
    public String encodeURL(String url) {
        int idx = url.indexOf('?');
        String path = idx == -1 ? url : url.substring(0, idx);
        String action = path.substring(path.lastIndexOf('/')+1);
        ActionHandler actionHandler = actionHandlers.get(action);
        if (actionHandler == null) {
            log.warn("Unknown action: " + action);
        } else if (actionHandler.isCSRFTokenRequired()) {
            url = url + (idx == -1 ? '?' : '&') + "token=" + getToken();
        }
        return super.encodeURL(url);
    }
}
