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
package org.apache.axis2.transport.jms;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.naming.Context;

import org.apache.axis2.transport.base.BaseUtils;

/**
 * Screens a JMS target address for JNDI names that resolve somewhere remote.
 * <p>
 * A JMS EPR is turned into two JNDI lookups and one JNDI environment:
 * <ul>
 * <li>the destination name, which is everything between {@code jms:/} and the query
 *     string ({@link JMSUtils#getDestination}),</li>
 * <li>the reply destination named by {@code transport.jms.ReplyDestination},</li>
 * <li>{@code java.naming.provider.url}, which says where names resolve.</li>
 * </ul>
 * JNDI resolves a name carrying a URL scheme through that scheme's URL context
 * factory, and several of those factories fetch and deserialise a remote object --
 * the JNDI injection route to code execution. So {@code jms:/ldap://host/obj} is a
 * lookup against an attacker's directory, whatever the configured provider is.
 * <p>
 * This replaces an earlier guard (AXIS2-6062) that searched the whole address for
 * the substrings LDAP, RMI, JMX, JRMP, DNS, IIOP and CORBANAME. That caught the
 * shapes above, but it also refused any legitimate destination whose name happened
 * to contain one -- a queue called {@code alarming} contains "RMI" -- while missing
 * a hostile provider URL naming no such scheme. Matching the scheme position of the
 * fields that are actually resolved is both stricter where it matters and free of
 * those false positives. Do not replace it with a substring search again.
 */
final class JMSTargetAddressPolicy {

    /**
     * Schemes whose JNDI URL context factories reach out to a remote naming service
     * and can return a deserialised or remotely-loaded object.
     */
    private static final Set<String> REMOTE_JNDI_SCHEMES = Collections.unmodifiableSet(
            new HashSet<String>(Arrays.asList(
                    "ldap", "ldaps", "rmi", "iiop", "iiopname",
                    "corbaname", "corbaloc", "jrmp", "dns", "jmx")));

    /** A leading URL scheme, per RFC 3986. */
    private static final Pattern SCHEME = Pattern.compile("^([A-Za-z][A-Za-z0-9+.\\-]*):");

    private JMSTargetAddressPolicy() {
    }

    /**
     * @param targetAddress the JMS EPR about to be resolved
     * @return why the address must be refused, or {@code null} if it is acceptable
     */
    static String rejectionReason(String targetAddress) {
        if (targetAddress == null || !targetAddress.startsWith(JMSConstants.JMS_PREFIX)) {
            return null;
        }

        String reason = checkLookupName("destination name",
                JMSUtils.getDestination(targetAddress));
        if (reason != null) {
            return reason;
        }

        Map<String, String> properties = BaseUtils.getEPRProperties(targetAddress);
        reason = checkLookupName("reply destination name",
                properties.get(JMSConstants.PARAM_REPLY_DESTINATION));
        if (reason != null) {
            return reason;
        }

        String providerUrl = properties.get(Context.PROVIDER_URL);
        String providerScheme = schemeOf(providerUrl);
        if (providerScheme != null && REMOTE_JNDI_SCHEMES.contains(providerScheme)) {
            return "the JNDI provider URL uses the remote naming scheme '"
                    + providerScheme + "'";
        }

        return null;
    }

    private static String checkLookupName(String what, String name) {
        String scheme = schemeOf(name);
        if (scheme != null && REMOTE_JNDI_SCHEMES.contains(scheme)) {
            return "the " + what + " is a '" + scheme + "' URL, which JNDI would "
                    + "resolve through a remote naming service";
        }
        return null;
    }

    private static String schemeOf(String value) {
        if (value == null) {
            return null;
        }
        Matcher matcher = SCHEME.matcher(value.trim());
        return matcher.find() ? matcher.group(1).toLowerCase(Locale.ENGLISH) : null;
    }
}
