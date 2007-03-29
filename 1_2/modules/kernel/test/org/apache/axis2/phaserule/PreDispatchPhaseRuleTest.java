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

public class PreDispatchPhaseRuleTest extends AbstractTestCase {

    PreDispatchPhaseRuleTest phaserul;
    AxisConfiguration axisSytem;

    public PreDispatchPhaseRuleTest(String testName) {
        super(testName);
    }

    public void testPhaseRule() throws Exception {
        //TODO Fix me
        phaserul = new PreDispatchPhaseRuleTest("");
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


        HandlerDescription pre = new HandlerDescription();
        pre.setClassName("org.apache.axis2.handlers.AbstractHandler");
        Handler h1 = new PhaseRuleHandlers();
        h1.init(pre);
        pre.setHandler(h1);
        pre.setName("pre-H1");
        PhaseRule pre_rule1 = new PhaseRule();
        pre_rule1.setPhaseName("PreDispatch");
        pre.setRules(pre_rule1);
        ph.addHandler(pre);

        HandlerDescription pre2 = new HandlerDescription();
        pre2.setClassName("org.apache.axis2.handlers.AbstractHandler");
        Handler h2 = new PhaseRuleHandlers();
        h2.init(pre2);
        pre2.setHandler(h2);
        pre2.setName("dispatch");
        PhaseRule prerule2 = new PhaseRule();
        prerule2.setPhaseName("Dispatch");
        pre2.setRules(prerule2);
        ph.addHandler(pre2);


        HandlerDescription hm = new HandlerDescription();
        hm.setClassName("org.apache.axis2.handlers.AbstractHandler");
        Handler h3 = new PhaseRuleHandlers();
        h3.init(hm);
        hm.setHandler(h3);
        hm.setName("pre-H2");
        PhaseRule rule = new PhaseRule();
        rule.setPhaseName("PreDispatch");
        rule.setPhaseFirst(true);
        hm.setRules(rule);
        ph.addHandler(hm);

        HandlerDescription hm1 = new HandlerDescription();
        hm1.setClassName("org.apache.axis2.handlers.AbstractHandler");
        Handler h4 = new PhaseRuleHandlers();
        h4.init(hm1);
        hm1.setHandler(h4);
        hm1.setName("pre-H3");
        PhaseRule rule1 = new PhaseRule();
        rule1.setPhaseName("PreDispatch");
        rule1.setAfter("pre-H2");
        hm1.setRules(rule1);
        ph.addHandler(hm1);

        HandlerDescription hm2 = new HandlerDescription();
        hm2.setClassName("org.apache.axis2.handlers.AbstractHandler");
        Handler h5 = new PhaseRuleHandlers();
        h5.init(hm2);
        hm2.setHandler(h5);
        hm2.setName("H3");
        PhaseRule rule2 = new PhaseRule();
        rule2.setPhaseName("PreDispatch");
        rule2.setAfter("pre-H2");
        rule2.setBefore("pre-H3");
        hm2.setRules(rule2);
        ph.addHandler(hm2);

        /*ArrayList oh = ph.getOrderHandler();
        for (int i = 0; i < oh.size(); i++) {
            HandlerDescription metadata = (HandlerDescription) oh.get(i);
            System.out.println("Name:" + metadata.getName().getLocalPart());
        }*/
    }
}
