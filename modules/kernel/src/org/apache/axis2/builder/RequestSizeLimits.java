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

package org.apache.axis2.builder;

import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.Parameter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Size ceilings applied by the message builders that read a request body
 * directly from the transport stream.
 *
 * <p>Those builders consume the raw {@code InputStream} rather than the servlet
 * parameter API, so a container-level post limit (Tomcat's {@code maxPostSize},
 * for example) never sees the body and does not constrain it. Without a ceiling
 * here an anonymous client can size the allocation itself.
 *
 * <p>Each limit resolves through the usual Axis2 parameter chain — message,
 * operation, service, service group, then {@code axis2.xml} — so a deployment
 * that legitimately accepts large uploads can raise it for one service without
 * loosening the global default. A value of {@code -1} restores the unbounded
 * behaviour.
 */
public final class RequestSizeLimits {

    private static final Log log = LogFactory.getLog(RequestSizeLimits.class);

    /** Ceiling on a whole multipart/form-data request. */
    public static final String MULTIPART_MAX_REQUEST_SIZE = "multipartMaxRequestSize";

    /** Ceiling on any single part within a multipart/form-data request. */
    public static final String MULTIPART_MAX_FILE_SIZE = "multipartMaxFileSize";

    /** Ceiling on an application/x-www-form-urlencoded request body. */
    public static final String FORM_URLENCODED_MAX_REQUEST_SIZE = "formUrlEncodedMaxRequestSize";

    /** 100 MB: generous for document and attachment uploads, but finite. */
    public static final long DEFAULT_MULTIPART_MAX_REQUEST_SIZE = 100L * 1024 * 1024;

    /** 100 MB, matching the whole-request ceiling for the single-part case. */
    public static final long DEFAULT_MULTIPART_MAX_FILE_SIZE = 100L * 1024 * 1024;

    /** 2 MB: form encoding is for field data, not bulk transfer. */
    public static final long DEFAULT_FORM_URLENCODED_MAX_REQUEST_SIZE = 2L * 1024 * 1024;

    /** Sentinel for "no ceiling", matching the commons-fileupload2 convention. */
    /**
     * Ceiling on a {@code multipart/related} body: MTOM and SwA.
     * <p>
     * The same reasoning as the form builders, which is why this belongs here: the
     * MIME builder reads the transport stream directly, so a servlet container's
     * post-size limit never sees the body. It also means the caller picks which
     * builder runs by choosing the Content-Type, so bounding only the form builders
     * bounds nothing -- an attacker simply sends {@code multipart/related}.
     */
    public static final String MTOM_MAX_REQUEST_SIZE = "mtomMaxRequestSize";

    /** Ceiling on a plain SOAP or POX body. */
    public static final String SOAP_MAX_REQUEST_SIZE = "soapMaxRequestSize";

    /** Default {@link #MTOM_MAX_REQUEST_SIZE}: 100 MB. */
    public static final long DEFAULT_MTOM_MAX_REQUEST_SIZE = 100L * 1024 * 1024;

    /** Default {@link #SOAP_MAX_REQUEST_SIZE}: 100 MB. */
    public static final long DEFAULT_SOAP_MAX_REQUEST_SIZE = 100L * 1024 * 1024;

    public static final long UNLIMITED = -1L;

    private RequestSizeLimits() {
    }

    /**
     * Resolve a size limit for the message being built.
     *
     * @param messageContext the message being built; null yields the default
     * @param parameterName one of the parameter name constants on this class
     * @param defaultValue the limit to apply when the parameter is not set
     * @return the configured limit in bytes, or {@link #UNLIMITED}
     */
    public static long resolve(MessageContext messageContext, String parameterName, long defaultValue) {
        if (messageContext == null) {
            return defaultValue;
        }
        Parameter parameter = messageContext.getParameter(parameterName);
        if (parameter == null || parameter.getValue() == null) {
            return defaultValue;
        }
        String value = parameter.getValue().toString().trim();
        if (value.isEmpty()) {
            return defaultValue;
        }
        try {
            long limit = Long.parseLong(value);
            return limit < 0 ? UNLIMITED : limit;
        } catch (NumberFormatException e) {
            log.warn("Ignoring non-numeric value '" + value + "' for parameter '"
                    + parameterName + "'; using the default of " + defaultValue + " bytes");
            return defaultValue;
        }
    }
}
