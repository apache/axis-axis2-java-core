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
import org.apache.axis2.engine.Handler;
import org.apache.axis2.phaseresolver.PhaseHolder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;

public class InvalidPhaseRuleTest extends AbstractTestCase {

    InvalidPhaseRuleTest phaserul;
    AxisConfiguration axisSytem;
    private static final Log log = LogFactory.getLog(InvalidPhaseRuleTest.class);

    public InvalidPhaseRuleTest(String testName) {
        super(testName);
    }

    public void testInvalidPhaseRule1() {
        try {
            super.setUp();
            phaserul = new InvalidPhaseRuleTest("");
            axisSytem = new AxisConfiguration();
            ArrayList inPhase = axisSytem.getGlobalInFlow();

            Handler han = null;
            PhaseHolder ph = new PhaseHolder(inPhase);


            HandlerDescription hm = new HandlerDescription();
            hm.setClassName("org.apache.axis2.handlers.AbstractHandler");
            hm.setHandler(han);
            hm.setName("H1");
            PhaseRule rule = new PhaseRule();
            rule.setPhaseName("global");
            rule.setPhaseFirst(true);
            hm.setRules(rule);
            ph.addHandler(hm);

            HandlerDescription hm1 = new HandlerDescription();
            hm1.setClassName("org.apache.axis2.handlers.AbstractHandler");
            hm1.setHandler(han);
            hm1.setName("H2");
            PhaseRule rule1 = new PhaseRule();
            rule1.setPhaseName("global");
            rule1.setAfter("H1");
            hm1.setRules(rule1);
            ph.addHandler(hm1);

            HandlerDescription hm3 = new HandlerDescription();
            hm3.setClassName("org.apache.axis2.handlers.AbstractHandler");
            hm3.setHandler(han);
            hm3.setName("H4");
            PhaseRule rule3 = new PhaseRule();
            rule3.setPhaseName("Logging");
            hm3.setRules(rule3);
            ph.addHandler(hm3);
            fail("this must failed gracefully with PhaseException ");
        } catch (Exception e) {
            log.info(e.getMessage());
        }
    }


    public void testInvalidPhaseRule2() {
        try {
            super.setUp();

            phaserul = new InvalidPhaseRuleTest("");
            axisSytem = new AxisConfiguration();
            ArrayList inPhase = axisSytem.getGlobalInFlow();

            inPhase.add("global");
            axisSytem.setInFaultPhases(inPhase);
            axisSytem.setOutFaultPhases(inPhase);
            axisSytem.setGlobalOutPhase(inPhase);

            Handler han = null;
            PhaseHolder ph = new PhaseHolder(inPhase);


            HandlerDescription hm = new HandlerDescription();
            hm.setClassName("org.apache.axis2.handlers.AbstractHandler");
            hm.setHandler(han);
            hm.setName("H1");
            PhaseRule rule = new PhaseRule();
            rule.setPhaseName("global");
            rule.setPhaseFirst(true);
            rule.setPhaseLast(true);
            hm.setRules(rule);
            ph.addHandler(hm);

            HandlerDescription hm1 = new HandlerDescription();
            hm1.setClassName("org.apache.axis2.handlers.AbstractHandler");
            hm1.setHandler(han);
            hm1.setName("H2");
            PhaseRule rule1 = new PhaseRule();
            rule1.setPhaseName("global");
            rule1.setAfter("H1");
            hm1.setRules(rule1);
            ph.addHandler(hm1);

            fail("this must failed gracefully with PhaseException ");
        } catch (Exception e) {
            log.info(e.getMessage());
        }
    }


}
