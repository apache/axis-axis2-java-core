/*
 * Copyright 2004,2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.axis2.phaserule;

import junit.framework.TestCase;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ContextFactory;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.HandlerDescription;
import org.apache.axis2.description.PhaseRule;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.engine.Handler;
import org.apache.axis2.engine.Phase;
import org.apache.axis2.phaseresolver.PhaseHolder;

import javax.xml.namespace.QName;
import java.util.ArrayList;

public class InvalidPhaselastTest extends TestCase {

    public void testInvalidPhaseLast() {
        try {
            ArrayList phases = new ArrayList();
            Phase p1 = new Phase("PhaseA");
            phases.add(p1);
            Phase p2 = new Phase("PhaseB");
            phases.add(p2);

            MessageContext msg = ContextFactory.createMessageContext(
                    new ConfigurationContext(new AxisConfiguration()));

            PhaseHolder ph = new PhaseHolder(phases);
            HandlerDescription hm = new HandlerDescription();
            hm.setClassName("org.apache.axis2.phaserule.PhaseRuleHandlers");
            Handler h1 = new PhaseRuleHandlers();
            h1.init(hm);
            ((PhaseRuleHandlers) h1).setName(new QName("PhaseLast"));
            hm.setHandler(h1);
            hm.setName("H1");
            PhaseRule rule = new PhaseRule();
            rule.setPhaseName("PhaseA");
            rule.setPhaseLast(true);
            hm.setRules(rule);
            ph.addHandler(hm);

            HandlerDescription hm1 = new HandlerDescription();
            hm1.setClassName("org.apache.axis2.phaserule.PhaseRuleHandlers");
            Handler h2 = new PhaseRuleHandlers();
            ((PhaseRuleHandlers) h2).setName(new QName("Second Handler"));
            h2.init(hm1);
            hm1.setHandler(h2);
            hm1.setName("H2");
            PhaseRule rule1 = new PhaseRule();
            rule1.setPhaseName("PhaseA");
            rule1.setPhaseLast(true);
            hm1.setRules(rule1);
            ph.addHandler(hm1);
            fail("This should be faild with Phaselast already has been set, cannot have two " +
                    "phaseFirst Handler for same phase ");
        } catch (AxisFault axisFault) {
            return;
        }

    }
}
