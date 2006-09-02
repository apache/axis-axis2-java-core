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
import org.apache.axis2.description.HandlerDescription;
import org.apache.axis2.description.PhaseRule;
import org.apache.axis2.engine.Handler;
import org.apache.axis2.engine.Phase;
import org.apache.axis2.phaseresolver.PhaseHolder;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Iterator;

public class BeforeAfterTest extends TestCase {

    public void testBeforeAfter() throws Exception {
        ArrayList phases = new ArrayList();
        Phase p1 = new Phase("PhaseA");
        phases.add(p1);
        Phase p2 = new Phase("PhaseB");
        phases.add(p2);

        PhaseHolder ph = new PhaseHolder(phases);
        HandlerDescription hm = new HandlerDescription();
        hm.setClassName("org.apache.axis2.phaserule.PhaseRuleHandlers");
        Handler h1 = new PhaseRuleHandlers();
        h1.init(hm);
        ((PhaseRuleHandlers) h1).setName(new QName("First"));
        hm.setHandler(h1);
        hm.setName("H1");
        PhaseRule rule = new PhaseRule();
        rule.setPhaseName("PhaseA");
        hm.setRules(rule);
        ph.addHandler(hm);

        HandlerDescription hm1 = new HandlerDescription();
        hm1.setClassName("org.apache.axis2.phaserule.PhaseRuleHandlers");
        Handler h2 = new PhaseRuleHandlers();
        ((PhaseRuleHandlers) h2).setName(new QName("Forth"));
        h2.init(hm1);
        hm1.setHandler(h2);
        hm1.setName("H2");
        PhaseRule rule1 = new PhaseRule();
        rule1.setPhaseName("PhaseA");
        hm1.setRules(rule1);
        ph.addHandler(hm1);


        HandlerDescription hm3 = new HandlerDescription();
        hm3.setClassName("org.apache.axis2.phaserule.PhaseRuleHandlers");
        Handler h3 = new PhaseRuleHandlers();
        ((PhaseRuleHandlers) h3).setName(new QName("Second"));
        h3.init(hm3);
        hm3.setHandler(h3);
        hm3.setName("H3");
        PhaseRule rule3 = new PhaseRule();
        rule3.setPhaseName("PhaseA");
        rule3.setAfter("H1");
        hm3.setRules(rule3);
        ph.addHandler(hm3);

        HandlerDescription hm4 = new HandlerDescription();
        hm4.setClassName("org.apache.axis2.phaserule.PhaseRuleHandlers");
        Handler h4 = new PhaseRuleHandlers();
        ((PhaseRuleHandlers) h4).setName(new QName("Third"));
        h4.init(hm4);
        hm4.setHandler(h4);
        hm4.setName("H4");
        PhaseRule rule4 = new PhaseRule();
        rule4.setPhaseName("PhaseA");
        rule4.setAfter("H1");
        rule4.setBefore("H2");
        hm4.setRules(rule4);
        ph.addHandler(hm4);

        ArrayList handlers = p1.getHandlers();
        boolean foundH1 = false;
        boolean foundH4 = false;

        for (Iterator iterator = handlers.iterator(); iterator.hasNext();) {
            Handler handler = (Handler) iterator.next();
            if (h3 == handler) {
                if (!foundH1)
                    fail("H3 found before H1");
            }
            if (h1 == handler)
                foundH1 = true;
            if (h2 == handler) {
                if (!foundH4) {
                    fail("H2 found before H4");
                }
            }
            if (h4 == handler) {
                if (!foundH1) {
                    fail("H4 found before H1");
                }
                foundH4 = true;
            }
        }
    }
}
