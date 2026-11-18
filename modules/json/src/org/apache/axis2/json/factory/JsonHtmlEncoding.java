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

package org.apache.axis2.json.factory;

import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.util.JavaUtils;

/**
 * Decides whether the JSON formatters HTML-encode the String values they write.
 *
 * <p>The encoding ({@code &}, {@code <} and {@code >} as entities, applied once per
 * String) is on by default as a reflected-XSS control for consumers that place a
 * response value in an HTML page. It also changes the data for every other consumer,
 * so it can be turned off with the {@link JsonConstant#DISABLE_JSON_HTML_ENCODING}
 * parameter. The parameter is looked up through the message's operation, service,
 * service group and axis configuration, so it can be set globally in axis2.xml or
 * for one service in services.xml.</p>
 */
public final class JsonHtmlEncoding {

    private JsonHtmlEncoding() {
    }

    /**
     * @param msgCtxt the outgoing message context, may be null
     * @return true when {@link JsonConstant#DISABLE_JSON_HTML_ENCODING} is set to true
     *         for this message; false otherwise, which keeps the encoding on
     */
    public static boolean isDisabled(MessageContext msgCtxt) {
        if (msgCtxt == null) {
            return false;
        }
        Parameter parameter = msgCtxt.getParameter(JsonConstant.DISABLE_JSON_HTML_ENCODING);
        return parameter != null && JavaUtils.isTrueExplicitly(parameter.getValue());
    }
}
