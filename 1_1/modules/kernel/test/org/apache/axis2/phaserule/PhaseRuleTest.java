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

import org.apache.axis2.AbstractTestCase;
import org.apache.axis2.description.HandlerDescription;
import org.apache.axis2.description.PhaseRule;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.engine.DispatchPhase;
import org.apache.axis2.engine.Handler;
import org.apache.axis2.engine.Phase;
import org.apache.axis2.phaseresolver.PhaseHolder;

import java.util.ArrayList;

public class PhaseRuleTest extends AbstractTestCase {

    PhaseRuleTest phaserul;
    AxisConfiguration axisSytem;

    public PhaseRuleTest(String testName) {
        super(testName);
    }


    public void testPhaseRules() throws Exception {
        super.setUp();
        //TODO fix me
        phaserul = new PhaseRuleTest("");
        axisSytem = new AxisConfiguration();
        ArrayList inPhase = axisSytem.getGlobalInFlow();
        Phase transportIN = new Phase("TransportIn");
        Phase preDispatch = new Phase("PreDispatch");
        DispatchPhase dispatchPhase = new DispatchPhase();
//
        dispatchPhase.setName("Dispatch");
        inPhase.add(transportIN);
        inPhase.add(preDispatch);
        inPhase.add(dispatchPhase);

        PhaseHolder ph = new PhaseHolder(inPhase);

        HandlerDescription hm = new HandlerDescription();
        hm.setClassName("org.apache.axis2.handlers.AbstractHandler");
        Handler h1 = new PhaseRuleHandlers();
        h1.init(hm);
        hm.setHandler(h1);
        hm.setName("H1");
        PhaseRule rule = new PhaseRule();
        rule.setPhaseName("PreDispatch");
        rule.setPhaseFirst(true);
        hm.setRules(rule);
        ph.addHandler(hm);

        HandlerDescription hm1 = new HandlerDescription();
        hm1.setClassName("org.apache.axis2.handlers.AbstractHandler");
        Handler h2 = new PhaseRuleHandlers();
        h2.init(hm1);
        hm1.setHandler(h2);
        hm1.setName("H2");
        PhaseRule rule1 = new PhaseRule();
        rule1.setPhaseName("PreDispatch");
        rule1.setAfter("H1");
        hm1.setRules(rule1);
        ph.addHandler(hm1);

        HandlerDescription hm2 = new HandlerDescription();
        hm2.setClassName("org.apache.axis2.handlers.AbstractHandler");
        Handler h3 = new PhaseRuleHandlers();
        h3.init(hm2);
        hm2.setHandler(h3);
        hm2.setName("H3");
        PhaseRule rule2 = new PhaseRule();
        rule2.setPhaseName("PreDispatch");
        rule2.setAfter("H1");
        rule2.setBefore("H2");
        hm2.setRules(rule2);
        ph.addHandler(hm2);

        HandlerDescription hm3 = new HandlerDescription();
        hm3.setClassName("org.apache.axis2.handlers.AbstractHandler");
        Handler h4 = new PhaseRuleHandlers();
        h4.init(hm3);
        hm3.setHandler(h4);
        hm3.setName("H4");
        PhaseRule rule3 = new PhaseRule();
        rule3.setPhaseName("Dispatch");
        hm3.setRules(rule3);
        ph.addHandler(hm3);
    }
}
