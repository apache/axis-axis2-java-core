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

package org.apache.axis2.transport.server;

import org.apache.axis2.context.SessionContext;
import org.apache.axis2.transport.http.server.SessionManager;

import junit.framework.TestCase;

/**
 * The Class SessionManagerTest.
 */
public class SessionManagerTest extends TestCase {

    SessionManager manager;

    /*
     * (non-Javadoc)
     * 
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        manager = new SessionManager();
    }

    /*
     * (non-Javadoc)
     * 
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();
        manager = null;
    }

    /**
     * Test get session context.
     */
    public void testGetSessionContext() {
        String sessionKey = null;
        SessionContext ctx1 = manager.getSessionContext(sessionKey);
        sessionKey = ctx1.getCookieID();
        assertNotNull(ctx1);
        assertNotNull(sessionKey);

        SessionContext ctx2 = manager.getSessionContext(sessionKey);
        assertNotNull(ctx2);
        assertEquals(ctx1, ctx2);
        assertEquals(sessionKey, ctx2.getCookieID());

        SessionContext ctx3 = manager.getSessionContext(null);
        assertNotNull(ctx3);
        assertNotSame(ctx1, ctx3);
        assertFalse(sessionKey.equals(ctx3.getCookieID()));
    }

    /**
     * The session key is the only credential a caller needs to be bound to a
     * SessionContext, so the next one must not follow from the last. A key built
     * from a per-thread counter differs from its predecessor only by the low bits
     * that change on increment, which leaves at most 64 candidates.
     */
    public void testSessionKeyNotPredictableFromPreviousKey() {
        for (int i = 0; i < 100; i++) {
            String previous = manager.getSessionContext(null).getCookieID();
            String next = manager.getSessionContext(null).getCookieID();
            for (int t = 0; t < 64; t++) {
                long mask = t == 63 ? -1L : (1L << (t + 1)) - 1;
                assertFalse(next.equals(flipCounterBits(previous, mask)));
            }
        }
    }

    // Applies mask to the reversed-hex counter that leads a counter-based key.
    private static String flipCounterBits(String key, long mask) {
        if (key.length() < 16) {
            return null;
        }
        long counter = 0;
        for (int i = 0; i < 16; i++) {
            int nibble = Character.digit(key.charAt(i), 16);
            if (nibble < 0) {
                return null;
            }
            counter |= (long) nibble << (4 * i);
        }
        counter ^= mask;
        StringBuilder buffer = new StringBuilder(key.length());
        for (int i = 0; i < 16; i++) {
            buffer.append(Character.forDigit((int) (counter >> (4 * i)) & 15, 16));
        }
        return buffer.append(key.substring(16)).toString();
    }

}
