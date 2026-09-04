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
package org.apache.axis2.context.externalize;

import java.io.ObjectInputFilter;
import java.io.ObjectInputStream;
import java.lang.reflect.Proxy;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * The deserialization filter applied to the object streams context externalization
 * creates.
 * <p>
 * <b>What this can and cannot be.</b> Axis2 cannot ship a list of permitted classes
 * for this data. Context externalization deliberately carries application objects:
 * {@link org.apache.axis2.util.SelfManagedDataHolder} exists to hold whatever a
 * service put there, and a {@link org.apache.axis2.description.Parameter} value can
 * be any serializable object. A list restricted to Axis2's own context classes would
 * refuse the data the feature is for. So the useful properties are the ones that hold
 * without knowing the application's types:
 * <ul>
 * <li>dynamic proxies are refused. Axis2's own externalization never writes one, and
 *     a proxy backed by an attacker-chosen invocation handler is the entry point of
 *     the classic gadget chains. Set
 *     {@code org.apache.axis2.context.externalize.allowProxies=true} if an
 *     application genuinely serializes a proxy into its self-managed data.</li>
 * <li>an integrator who knows their own types can name them:
 *     {@code org.apache.axis2.context.externalize.serialFilter} takes a JEP 290
 *     pattern and is applied to these streams only, leaving the rest of the JVM
 *     alone. The JVM-wide {@code jdk.serialFilter} continues to apply as well.</li>
 * </ul>
 * <p>
 * <b>What is not covered.</b> {@code SafeObjectInputStream} has a second path:
 * where the writer chose object form, it calls {@code readObject()} on the
 * {@code ObjectInput} its caller supplied. That stream belongs to the caller --
 * a container persisting a session, or an application's own code -- and by the time
 * {@code readExternal} runs it has already read objects, so a filter cannot be
 * installed on it ({@code setObjectInputFilter} rejects that). Restricting that path
 * is the caller's to do, with {@code jdk.serialFilter} or a filter of their own.
 * <p>
 * Deserializing bytes an attacker can influence remains dangerous whatever is set
 * here. Nothing in Axis2 feeds these streams from the network; the risk arrives when
 * an integrator persists or replicates contexts.
 */
final class ContextDeserializationFilter implements ObjectInputFilter {

    /** A JEP 290 pattern applied to context deserialization only. */
    static final String SERIAL_FILTER_PROPERTY =
            "org.apache.axis2.context.externalize.serialFilter";

    /** Set true to permit dynamic proxies in externalized context data. */
    static final String ALLOW_PROXIES_PROPERTY =
            "org.apache.axis2.context.externalize.allowProxies";

    private static final Log log = LogFactory.getLog(ContextDeserializationFilter.class);

    private final ObjectInputFilter configured;
    private final boolean allowProxies;

    private ContextDeserializationFilter(ObjectInputFilter configured, boolean allowProxies) {
        this.configured = configured;
        this.allowProxies = allowProxies;
    }

    /**
     * Installs the filter on a stream this package created.
     *
     * @param stream a freshly created stream, before anything has been read from it
     */
    static void apply(ObjectInputStream stream) {
        stream.setObjectInputFilter(create());
    }

    static ContextDeserializationFilter create() {
        ObjectInputFilter configured = null;
        String pattern = getProperty(SERIAL_FILTER_PROPERTY);
        if (pattern != null && !pattern.trim().isEmpty()) {
            try {
                // NumberFormatException for a bad limit is an IllegalArgumentException.
                configured = ObjectInputFilter.Config.createFilter(pattern.trim());
            } catch (IllegalArgumentException e) {
                // Fail loudly on a pattern that will not compile: whoever set one
                // meant to restrict something, and ignoring it silently would leave
                // them believing a restriction is in force.
                throw new IllegalArgumentException("The value of "
                        + SERIAL_FILTER_PROPERTY + " is not a valid filter pattern", e);
            }
            if (configured == null) {
                // A degenerate pattern such as ";;" compiles to no filter at all,
                // which reads as success but restricts nothing. Same reasoning.
                throw new IllegalArgumentException("The value of "
                        + SERIAL_FILTER_PROPERTY + " defines no filter: " + pattern);
            }
        }
        return new ContextDeserializationFilter(configured,
                Boolean.parseBoolean(getProperty(ALLOW_PROXIES_PROPERTY)));
    }

    public Status checkInput(FilterInfo filterInfo) {
        Class<?> serialClass = filterInfo.serialClass();
        if (serialClass == null) {
            // A stream-size, depth or reference count check rather than a class.
            return delegate(filterInfo);
        }

        Class<?> componentType = serialClass;
        while (componentType.isArray()) {
            componentType = componentType.getComponentType();
        }
        if (!allowProxies && Proxy.isProxyClass(componentType)) {
            if (log.isDebugEnabled()) {
                log.debug("Refusing a dynamic proxy in externalized context data: "
                        + componentType.getName());
            }
            return Status.REJECTED;
        }

        return delegate(filterInfo);
    }

    private Status delegate(FilterInfo filterInfo) {
        if (configured != null) {
            return configured.checkInput(filterInfo);
        }
        return Status.UNDECIDED;
    }

    private static String getProperty(String name) {
        try {
            return System.getProperty(name);
        } catch (SecurityException e) {
            return null;
        }
    }
}
