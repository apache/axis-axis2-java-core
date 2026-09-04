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
import java.io.Serializable;
import java.lang.reflect.Proxy;
import java.util.HashMap;

import junit.framework.TestCase;

/**
 * Context externalization carries application objects on purpose -- self-managed
 * data and Parameter values can be anything serializable -- so Axis2 cannot ship a
 * list of permitted classes for it. These tests pin what the filter does enforce
 * regardless of the application's types, and that an integrator who does know their
 * types can restrict them.
 */
public class ContextDeserializationFilterTest extends TestCase {

    private String savedFilter;
    private String savedProxies;

    @Override
    protected void setUp() throws Exception {
        savedFilter = System.getProperty(ContextDeserializationFilter.SERIAL_FILTER_PROPERTY);
        savedProxies = System.getProperty(ContextDeserializationFilter.ALLOW_PROXIES_PROPERTY);
        System.clearProperty(ContextDeserializationFilter.SERIAL_FILTER_PROPERTY);
        System.clearProperty(ContextDeserializationFilter.ALLOW_PROXIES_PROPERTY);
    }

    @Override
    protected void tearDown() throws Exception {
        restore(ContextDeserializationFilter.SERIAL_FILTER_PROPERTY, savedFilter);
        restore(ContextDeserializationFilter.ALLOW_PROXIES_PROPERTY, savedProxies);
    }

    private void restore(String name, String value) {
        if (value == null) {
            System.clearProperty(name);
        } else {
            System.setProperty(name, value);
        }
    }

    /** The gadget-chain entry point Axis2 itself never writes. */
    public void testDynamicProxiesAreRefused() {
        assertEquals(ObjectInputFilter.Status.REJECTED,
                check(ContextDeserializationFilter.create(), proxyClass()));
    }

    public void testProxyArraysAreRefusedToo() {
        Class<?> proxyArray = java.lang.reflect.Array.newInstance(proxyClass(), 0).getClass();
        assertEquals(ObjectInputFilter.Status.REJECTED,
                check(ContextDeserializationFilter.create(), proxyArray));
    }

    /**
     * Application data has to keep working: this feature exists to carry it, so an
     * ordinary serializable class is not the filter's business.
     */
    public void testApplicationClassesAreNotRefused() {
        ContextDeserializationFilter filter = ContextDeserializationFilter.create();
        assertEquals(ObjectInputFilter.Status.UNDECIDED, check(filter, HashMap.class));
        assertEquals(ObjectInputFilter.Status.UNDECIDED, check(filter, String.class));
        assertEquals(ObjectInputFilter.Status.UNDECIDED,
                check(filter, ApplicationPayload.class));
    }

    /** An escape hatch, for an application that really does serialize a proxy. */
    public void testProxiesCanBeAllowedBack() {
        System.setProperty(ContextDeserializationFilter.ALLOW_PROXIES_PROPERTY, "true");
        assertEquals(ObjectInputFilter.Status.UNDECIDED,
                check(ContextDeserializationFilter.create(), proxyClass()));
    }

    /** An integrator who knows their own types can name them. */
    public void testAConfiguredPatternIsEnforced() {
        System.setProperty(ContextDeserializationFilter.SERIAL_FILTER_PROPERTY,
                "java.util.HashMap;!*");
        ContextDeserializationFilter filter = ContextDeserializationFilter.create();
        assertEquals(ObjectInputFilter.Status.ALLOWED, check(filter, HashMap.class));
        assertEquals(ObjectInputFilter.Status.REJECTED,
                check(filter, ApplicationPayload.class));
    }

    /**
     * A pattern that will not compile must not be ignored: whoever set it believes
     * something is restricted.
     */
    public void testAnUnparseablePatternIsRefusedLoudly() {
        // An unknown limit, and a bad limit value: the second throws
        // NumberFormatException, which is an IllegalArgumentException.
        for (String bad : new String[] {"bogus=1", "maxdepth=abc", "maxdepth=-5"}) {
            System.setProperty(ContextDeserializationFilter.SERIAL_FILTER_PROPERTY, bad);
            try {
                ContextDeserializationFilter.create();
                fail("an invalid filter pattern must not be silently ignored: " + bad);
            } catch (IllegalArgumentException expected) {
                assertTrue(expected.getMessage()
                        .contains(ContextDeserializationFilter.SERIAL_FILTER_PROPERTY));
            }
        }
    }

    /**
     * A pattern that compiles to nothing reads as success while restricting nothing,
     * which is the same trap as one that will not compile.
     */
    public void testAPatternDefiningNoFilterIsRefusedLoudly() {
        System.setProperty(ContextDeserializationFilter.SERIAL_FILTER_PROPERTY, ";;");
        try {
            ContextDeserializationFilter.create();
            fail("a pattern defining no filter must not pass for a restriction");
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage().contains("defines no filter"));
        }
    }

    private ObjectInputFilter.Status check(ObjectInputFilter filter, Class<?> serialClass) {
        return filter.checkInput(new StubFilterInfo(serialClass));
    }

    private Class<?> proxyClass() {
        return Proxy.getProxyClass(getClass().getClassLoader(), new Class[] {Runnable.class});
    }

    /** A stand-in for an application object in self-managed data. */
    private static class ApplicationPayload implements Serializable {
        private static final long serialVersionUID = 1L;
    }

    /** Reports one class; the counters are what a stream would supply. */
    private static class StubFilterInfo implements ObjectInputFilter.FilterInfo {
        private final Class<?> serialClass;

        StubFilterInfo(Class<?> serialClass) {
            this.serialClass = serialClass;
        }

        public Class<?> serialClass() {
            return serialClass;
        }

        public long arrayLength() {
            return -1;
        }

        public long depth() {
            return 1;
        }

        public long references() {
            return 1;
        }

        public long streamBytes() {
            return 1;
        }
    }

}
