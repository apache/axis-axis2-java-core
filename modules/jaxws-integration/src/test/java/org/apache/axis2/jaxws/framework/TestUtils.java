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

package org.apache.axis2.jaxws.framework;

import static org.apache.axis2.jaxws.framework.TestUtils.withRetry;
import static org.junit.Assert.assertTrue;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.concurrent.Future;

import org.apache.axis2.testutils.RuntimeIgnoreException;

public final class TestUtils {
    private TestUtils() {}

    /**
     * Check that the given URL refers to an unknown host. More precisely, this method checks that
     * the DNS resolver will not be able to resolve the host name. If the expectation is not met,
     * the method throws a {@link RuntimeIgnoreException} so that the test will be skipped. Note
     * that this will only work if the test is configured with the appropriate test runner.
     * <p>
     * Some systems may be configured with a search domain that has a wildcard entry. On these
     * systems it is virtually impossible to have a host name that will trigger a host not found
     * error. This is a problem for tests that contain assertions for {@link UnknownHostException}.
     * This method can be used to skip these tests dynamically on this kind of systems.
     * 
     * @param url
     * @throws MalformedURLException
     */
    public static void checkUnknownHostURL(String url) throws MalformedURLException {
        String host = new URL(url).getHost();
        InetAddress addr;
        try {
            addr = InetAddress.getByName(host);
        } catch (UnknownHostException ex) {
            // This is what we expect
            return;
        }
        throw new RuntimeIgnoreException(host + " resolves to " + addr.getHostAddress() + "; skipping test case");
    }

    public static void withRetry(Runnable runnable) throws InterruptedException {
        int elapsedTime = 0;
        int interval = 1;
        while (true) {
            try {
                runnable.run();
                return;
            } catch (AssertionError ex) {
                if (elapsedTime > 30000) {
                    throw ex;
                } else {
                    Thread.sleep(interval);
                    elapsedTime += interval;
                    interval = Math.min(500, interval*2);
                }
            }
        }
    }

    public static void await(final Future<?> future) throws InterruptedException {
        withRetry(new Runnable() {
            public void run() {
                // check the Future
                assertTrue("Response is not done!", future.isDone());
            }
        });
    }
}
