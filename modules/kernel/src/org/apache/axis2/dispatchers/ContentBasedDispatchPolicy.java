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
package org.apache.axis2.dispatchers;

import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.util.JavaUtils;

/**
 * Whether a service may be selected from the content of the message body.
 * <p>
 * The default inflow phase order is Transport, Addressing, <b>Security</b>,
 * PreDispatch, <b>Dispatch</b>. A service bound in the Dispatch phase is therefore
 * bound after the Security phase has already run, and
 * {@link org.apache.axis2.engine.DispatchPhase} installs only the phases that follow
 * Dispatch, so Security is never revisited. Per-service module handlers -- the
 * WS-Security ones among them -- live in that global Security phase.
 * <p>
 * The body-namespace dispatchers select a service from a string the caller supplies,
 * so with a request URI that names no service they let the caller pick the service
 * only after the phase that would have authenticated the request for it has run
 * against no service at all. That is why this is off unless an operator asks for it:
 * {@code allowContentBasedServiceDispatch}, set on the AxisConfiguration in
 * axis2.xml, defaults to {@code false}.
 * <p>
 * Dispatch by request URI, SOAPAction and WS-Addressing is unaffected; those bind
 * the service before the Security phase runs. Only deployments that genuinely
 * address services by body namespace need to turn this on, and they should not rely
 * on per-service security modules while it is on.
 */
public final class ContentBasedDispatchPolicy {

    /**
     * Name of the AxisConfiguration parameter, default {@code false}.
     */
    public static final String ALLOW_CONTENT_BASED_DISPATCH = "allowContentBasedServiceDispatch";

    private ContentBasedDispatchPolicy() {
    }

    /**
     * @param messageContext the message being dispatched; may be {@code null}
     * @return whether a service may be selected from the message body
     */
    public static boolean isAllowed(MessageContext messageContext) {
        if (messageContext == null) {
            return false;
        }
        // The service is null on this path by definition, so this resolves against
        // the AxisConfiguration -- see MessageContext.getParameter.
        Parameter parameter = messageContext.getParameter(ALLOW_CONTENT_BASED_DISPATCH);
        if (parameter == null || parameter.getValue() == null) {
            return false;
        }
        return JavaUtils.isTrueExplicitly(parameter.getValue());
    }
}
