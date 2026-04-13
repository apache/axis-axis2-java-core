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

package org.apache.axis2.engine;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.HandlerDescription;
import org.apache.axis2.handlers.AbstractHandler;

/**
 * Covers the AXIS2-5862 defensive cap in {@link Phase#flowComplete(MessageContext)}.
 * <p>
 * The ticket reports that {@code flowComplete} can throw
 * {@code ArrayIndexOutOfBoundsException} when {@code MessageContext.currentPhaseIndex}
 * holds a value that exceeds the current phase's handler count -- most visibly when
 * the phase has zero handlers but the inbound index is non-zero. That throw aborts
 * {@code flowComplete} and prevents earlier phases from receiving the callback.
 * <p>
 * The fix is a single-line cap at the top of the unwind loop: if the index exceeds
 * {@code handlers.size()} it is clamped to {@code handlers.size()}. The tests below
 * verify:
 * <ul>
 *   <li>The happy path (index within range) is unchanged and calls handlers in
 *       reverse order.</li>
 *   <li>An empty phase with a stale non-zero index no longer throws.</li>
 *   <li>A non-empty phase with an over-run index completes every real handler in
 *       reverse order and stops there (does not read past the end of the list).</li>
 *   <li>The side effect of resetting {@code currentPhaseIndex} to 0 on entry is
 *       preserved in the broken-index path, so earlier phases still see a clean
 *       slate.</li>
 * </ul>
 */
public class PhaseFlowCompleteTest extends TestCase {

    /** Handler that records whether its {@code flowComplete} was invoked, and when. */
    private static final class RecordingHandler extends AbstractHandler {
        private final String name;
        private final List<String> log;

        RecordingHandler(String name, List<String> log) {
            this.name = name;
            this.log = log;
            HandlerDescription desc = new HandlerDescription(name);
            init(desc);
        }

        @Override
        public InvocationResponse invoke(MessageContext msgContext) throws AxisFault {
            // Not exercised by flowComplete tests.
            return InvocationResponse.CONTINUE;
        }

        @Override
        public void flowComplete(MessageContext msgContext) {
            log.add(name);
        }
    }

    private MessageContext newMessageContext() throws AxisFault {
        // flowComplete() only reads/writes currentPhaseIndex on the MessageContext,
        // so a bare ConfigurationContext-backed MessageContext is enough.
        ConfigurationContext cc = new ConfigurationContext(new AxisConfiguration());
        return cc.createMessageContext();
    }

    /** Happy path: index in range, all handlers get flowComplete in reverse order. */
    public void testFlowCompleteNormalPathUnchanged() throws AxisFault {
        List<String> calls = new ArrayList<String>();
        Phase phase = new Phase("p");
        phase.addHandler(new RecordingHandler("h0", calls));
        phase.addHandler(new RecordingHandler("h1", calls));
        phase.addHandler(new RecordingHandler("h2", calls));

        MessageContext mc = newMessageContext();
        mc.setCurrentPhaseIndex(0);        // phase completed normally

        phase.flowComplete(mc);

        // Reverse order: h2, h1, h0
        assertEquals(3, calls.size());
        assertEquals("h2", calls.get(0));
        assertEquals("h1", calls.get(1));
        assertEquals("h0", calls.get(2));
    }

    /**
     * The documented AXIS2-5862 symptom: empty phase reached with a stale
     * non-zero currentPhaseIndex. Must not throw; must still reset the index.
     */
    public void testFlowCompleteOnEmptyPhaseWithStaleHandlerIndex() throws AxisFault {
        Phase empty = new Phase("empty");  // no handlers added

        MessageContext mc = newMessageContext();
        mc.setCurrentPhaseIndex(1);        // stale carry-over from a prior phase

        // Before the fix this threw ArrayIndexOutOfBoundsException: 0.
        empty.flowComplete(mc);

        // The reset side effect must still happen so earlier phases see a
        // clean slate.
        assertEquals("currentPhaseIndex must be reset to 0 even on the"
                     + " empty-phase / stale-index path",
                     0, mc.getCurrentPhaseIndex());
    }

    /**
     * Phase with real handlers but an inbound index that exceeds handlers.size().
     * Every real handler must still receive flowComplete, in reverse order, and
     * the loop must not read past the end of the list.
     */
    public void testFlowCompleteCapsOverrunHandlerIndex() throws AxisFault {
        List<String> calls = new ArrayList<String>();
        Phase phase = new Phase("p");
        phase.addHandler(new RecordingHandler("h0", calls));
        phase.addHandler(new RecordingHandler("h1", calls));

        MessageContext mc = newMessageContext();
        mc.setCurrentPhaseIndex(99);       // pathological over-run

        // Before the fix this threw ArrayIndexOutOfBoundsException: 98.
        phase.flowComplete(mc);

        // All real handlers should still flowComplete, reverse order, exactly once.
        assertEquals(2, calls.size());
        assertEquals("h1", calls.get(0));
        assertEquals("h0", calls.get(1));
        assertEquals(0, mc.getCurrentPhaseIndex());
    }

    /**
     * Edge case: inbound index exactly equals handlers.size(). Today that path
     * already works; the cap must not alter it (all handlers should still be
     * unwound in reverse order).
     */
    public void testFlowCompleteAtExactBoundaryUnchanged() throws AxisFault {
        List<String> calls = new ArrayList<String>();
        Phase phase = new Phase("p");
        phase.addHandler(new RecordingHandler("h0", calls));
        phase.addHandler(new RecordingHandler("h1", calls));

        MessageContext mc = newMessageContext();
        mc.setCurrentPhaseIndex(2);        // == handlers.size()

        phase.flowComplete(mc);

        assertEquals(2, calls.size());
        assertEquals("h1", calls.get(0));
        assertEquals("h0", calls.get(1));
    }

    /**
     * Mid-phase failure: currentPhaseIndex points at the handler where the fault
     * occurred. flowComplete should roll back just the handlers that ran (in
     * reverse order), not handlers that never got to invoke.
     */
    public void testFlowCompleteMidPhaseFailurePathUnchanged() throws AxisFault {
        List<String> calls = new ArrayList<String>();
        Phase phase = new Phase("p");
        phase.addHandler(new RecordingHandler("h0", calls));
        phase.addHandler(new RecordingHandler("h1", calls));
        phase.addHandler(new RecordingHandler("h2", calls));

        MessageContext mc = newMessageContext();
        // h0 and h1 ran, fault was raised from h2 before it set the index
        // onward; the engine left currentPhaseIndex at 2.
        mc.setCurrentPhaseIndex(2);

        phase.flowComplete(mc);

        // Only h1 then h0 should unwind; h2 never invoke()'d successfully.
        assertEquals(2, calls.size());
        assertEquals("h1", calls.get(0));
        assertEquals("h0", calls.get(1));
        assertEquals(0, mc.getCurrentPhaseIndex());
    }
}
