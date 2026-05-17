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

package org.apache.axis2.description;

import org.apache.neethi.Policy;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Regression test for AXIS2-5904: Intermittent "Rampart policy configuration
 * missing" error caused by millisecond-granularity race in
 * {@link AxisBindingMessage#getEffectivePolicy()}.
 *
 * The old implementation used {@code Date.after()} to detect policy changes.
 * Two policy updates within the same millisecond would produce identical
 * timestamps, causing {@code isPolicyUpdated()} to return false and the
 * cached (potentially stale/null) policy to be returned. The fix replaces
 * Date comparison with a monotonic AtomicLong counter in PolicySubject.
 */
public class PolicyCacheRaceTest {

    @Test
    public void testPolicyVersionIncrements() {
        PolicySubject subject = new PolicySubject();
        long v1 = subject.getVersion();

        Policy policy = new Policy();
        policy.setId("test-policy-1");
        subject.attachPolicy(policy);
        long v2 = subject.getVersion();

        assertTrue("Version must increment after attachPolicy", v2 > v1);

        Policy policy2 = new Policy();
        policy2.setId("test-policy-2");
        subject.attachPolicy(policy2);
        long v3 = subject.getVersion();

        assertTrue("Version must increment on each mutation", v3 > v2);
    }

    @Test
    public void testPolicyVersionIncrementsOnDetach() {
        PolicySubject subject = new PolicySubject();
        Policy policy = new Policy();
        policy.setId("detach-test");
        subject.attachPolicy(policy);
        long vBefore = subject.getVersion();

        subject.detachPolicyComponent("detach-test");
        long vAfter = subject.getVersion();

        assertTrue("Version must increment after detach", vAfter > vBefore);
    }

    @Test
    public void testPolicyVersionIncrementsOnClear() {
        PolicySubject subject = new PolicySubject();
        long vBefore = subject.getVersion();

        subject.clear();
        long vAfter = subject.getVersion();

        assertTrue("Version must increment after clear", vAfter > vBefore);
    }

    @Test
    public void testEffectivePolicyDetectsUpdate() {
        // Build a minimal AxisBindingMessage hierarchy
        AxisBindingMessage bindingMessage = new AxisBindingMessage();

        // First call — should compute and cache (returns null since no
        // policies are attached, but the caching mechanism still runs)
        Policy first = bindingMessage.getEffectivePolicy();

        // Attach a policy to the binding message's own PolicySubject
        Policy policy = new Policy();
        policy.setId("axis2-5904-test");
        bindingMessage.getPolicySubject().attachPolicy(policy);

        // Second call — must detect the version change and recompute.
        // Before the fix, if both calls happened within the same
        // millisecond, isPolicyUpdated() returned false and the stale
        // cached value was returned.
        Policy second = bindingMessage.getEffectivePolicy();

        // The recomputed policy should include the attached policy
        assertNotNull("Effective policy must not be null after attaching a policy", second);
    }

    @Test
    public void testRapidFirePolicyUpdatesAllDetected() {
        // Simulate the AXIS2-5904 scenario: multiple policy mutations
        // within the same millisecond must all be detected
        AxisBindingMessage bindingMessage = new AxisBindingMessage();

        for (int i = 0; i < 100; i++) {
            Policy policy = new Policy();
            policy.setId("rapid-fire-" + i);
            bindingMessage.getPolicySubject().attachPolicy(policy);

            // Every call must see the latest policy, not a stale cache
            Policy effective = bindingMessage.getEffectivePolicy();
            assertNotNull("Effective policy must not be null on iteration " + i, effective);
        }
    }
}
