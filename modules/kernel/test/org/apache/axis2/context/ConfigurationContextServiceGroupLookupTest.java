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

package org.apache.axis2.context;

import junit.framework.TestCase;

import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.AxisServiceGroup;
import org.apache.axis2.engine.AxisConfiguration;

/**
 * Covers the AXIS2-5788 fix: ConfigurationContext.getServiceGroupContext must
 * no longer unconditionally touch the returned context -- callers must be
 * able to opt out of the touch via the new
 * {@code getServiceGroupContext(String, boolean)} overload.
 */
public class ConfigurationContextServiceGroupLookupTest extends TestCase {

    private static final String SOAP_SESSION_ID = "soap-session-sgc";
    private static final String APP_SESSION_SG_NAME = "app-session-sg";

    private ConfigurationContext configurationContext;
    private ServiceGroupContext soapSessionContext;
    private ServiceGroupContext appSessionContext;

    @Override
    protected void setUp() throws Exception {
        AxisConfiguration axisConfiguration = new AxisConfiguration();

        // A SOAP-session-scoped group (ends up in serviceGroupContextMap).
        AxisServiceGroup soapSessionGroup = new AxisServiceGroup(axisConfiguration);
        soapSessionGroup.setServiceGroupName("soap-session-group");
        soapSessionGroup.addService(new AxisService("soap-svc"));
        axisConfiguration.addServiceGroup(soapSessionGroup);

        // An application-session-scoped group (ends up in
        // applicationSessionServiceGroupContexts).
        AxisServiceGroup appSessionGroup = new AxisServiceGroup(axisConfiguration);
        appSessionGroup.setServiceGroupName(APP_SESSION_SG_NAME);
        appSessionGroup.addService(new AxisService("app-svc"));
        axisConfiguration.addServiceGroup(appSessionGroup);

        configurationContext = new ConfigurationContext(axisConfiguration);

        soapSessionContext = new ServiceGroupContext(configurationContext, soapSessionGroup);
        soapSessionContext.setId(SOAP_SESSION_ID);
        configurationContext.addServiceGroupContextIntoSoapSessionTable(soapSessionContext);

        appSessionContext = new ServiceGroupContext(configurationContext, appSessionGroup);
        configurationContext.addServiceGroupContextIntoApplicationScopeTable(appSessionContext);
    }

    /** Null id must short-circuit to null without mutation or NPE. */
    public void testNullIdReturnsNull() {
        assertNull(configurationContext.getServiceGroupContext(null));
        assertNull(configurationContext.getServiceGroupContext(null, true));
        assertNull(configurationContext.getServiceGroupContext(null, false));
    }

    /** Unknown id returns null from both overloads. */
    public void testUnknownIdReturnsNull() {
        assertNull(configurationContext.getServiceGroupContext("no-such-id"));
        assertNull(configurationContext.getServiceGroupContext("no-such-id", true));
        assertNull(configurationContext.getServiceGroupContext("no-such-id", false));
    }

    /** Legacy single-arg lookup must still touch (back-compat). */
    public void testSingleArgLookupTouchesSoapSessionContext() {
        soapSessionContext.setLastTouchedTime(0L);

        ServiceGroupContext found = configurationContext.getServiceGroupContext(SOAP_SESSION_ID);

        assertSame(soapSessionContext, found);
        assertTrue("single-arg lookup must update lastTouchedTime",
                   found.getLastTouchedTime() > 0L);
    }

    /** Two-arg lookup with touch=true matches the legacy behaviour. */
    public void testTwoArgLookupWithTouchTrueTouchesSoapSessionContext() {
        soapSessionContext.setLastTouchedTime(0L);

        ServiceGroupContext found =
                configurationContext.getServiceGroupContext(SOAP_SESSION_ID, true);

        assertSame(soapSessionContext, found);
        assertTrue("touch=true must update lastTouchedTime",
                   found.getLastTouchedTime() > 0L);
    }

    /**
     * The AXIS2-5788 fix proper: touch=false must NOT mutate lastTouchedTime
     * on the returned context.
     */
    public void testTwoArgLookupWithTouchFalseDoesNotTouchSoapSessionContext() {
        soapSessionContext.setLastTouchedTime(0L);

        ServiceGroupContext found =
                configurationContext.getServiceGroupContext(SOAP_SESSION_ID, false);

        assertSame(soapSessionContext, found);
        assertEquals("touch=false must leave lastTouchedTime unchanged",
                     0L, found.getLastTouchedTime());
    }

    /** The same no-touch contract must hold for application-session contexts. */
    public void testTwoArgLookupWithTouchFalseDoesNotTouchAppSessionContext() {
        appSessionContext.setLastTouchedTime(0L);

        ServiceGroupContext found =
                configurationContext.getServiceGroupContext(APP_SESSION_SG_NAME, false);

        assertSame(appSessionContext, found);
        assertEquals("touch=false must leave lastTouchedTime unchanged",
                     0L, found.getLastTouchedTime());
    }

    /** And the legacy-touching contract must also hold for app-session contexts. */
    public void testSingleArgLookupTouchesAppSessionContext() {
        appSessionContext.setLastTouchedTime(0L);

        ServiceGroupContext found =
                configurationContext.getServiceGroupContext(APP_SESSION_SG_NAME);

        assertSame(appSessionContext, found);
        assertTrue("single-arg lookup must update lastTouchedTime for app-session SGC",
                   found.getLastTouchedTime() > 0L);
    }

    /**
     * Calling the no-touch variant repeatedly must continue to leave
     * lastTouchedTime alone -- guards against a future refactor that
     * re-introduces the observer effect for only the first call.
     */
    public void testRepeatedNoTouchLookupsNeverMutate() {
        soapSessionContext.setLastTouchedTime(0L);

        for (int i = 0; i < 5; i++) {
            ServiceGroupContext found =
                    configurationContext.getServiceGroupContext(SOAP_SESSION_ID, false);
            assertSame(soapSessionContext, found);
            assertEquals("iteration " + i + ": touch=false must stay read-only",
                         0L, found.getLastTouchedTime());
        }
    }
}
