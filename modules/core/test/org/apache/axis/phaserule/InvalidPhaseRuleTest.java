package org.apache.axis.phaserule;

import java.util.ArrayList;

import javax.xml.namespace.QName;

import org.apache.axis.AbstractTestCase;
import org.apache.axis.description.GlobalDescription;
import org.apache.axis.description.HandlerDescription;
import org.apache.axis.description.PhaseRule;
import org.apache.axis.engine.AxisConfiguration;
import org.apache.axis.engine.AxisSystemImpl;
import org.apache.axis.engine.Handler;
import org.apache.axis.phaseresolver.PhaseHolder;

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
 *
 * 
 */

/**
 * Author : Deepal Jayasinghe
 * Date: May 10, 2005
 * Time: 4:35:33 PM
 */
public class InvalidPhaseRuleTest extends AbstractTestCase {

    InvalidPhaseRuleTest phaserul;
    AxisConfiguration registry;

    public InvalidPhaseRuleTest(String testName) {
        super(testName);
    }

    public void testInvalidPhaseRule1() {
        try {
            super.setUp();
            phaserul = new InvalidPhaseRuleTest("");
            GlobalDescription global = new GlobalDescription();
            registry = new AxisSystemImpl(global);
            ArrayList inPhase = new ArrayList();

            inPhase.add("global");
            inPhase.add("service");
            ((AxisSystemImpl) registry).setInPhases(inPhase);
            ((AxisSystemImpl) registry).setInFaultPhases(inPhase);
            ((AxisSystemImpl) registry).setOutFaultPhases(inPhase);
            ((AxisSystemImpl) registry).setOutPhases(inPhase);

            Handler han = null;
            PhaseHolder ph = new PhaseHolder(registry);
            ph.setFlowType(1);


            HandlerDescription hm = new HandlerDescription();
            hm.setClassName("org.apache.axis.handlers.AbstractHandler");
            hm.setHandler(han);
            hm.setName(new QName("H1"));
            PhaseRule rule = new PhaseRule();
            rule.setPhaseName("global");
            rule.setPhaseFirst(true);
            hm.setRules(rule);
            ph.addHandler(hm);

            HandlerDescription hm1 = new HandlerDescription();
            hm1.setClassName("org.apache.axis.handlers.AbstractHandler");
            hm1.setHandler(han);
            hm1.setName(new QName("H2"));
            PhaseRule rule1 = new PhaseRule();
            rule1.setPhaseName("global");
            rule1.setAfter("H1");
            hm1.setRules(rule1);
            ph.addHandler(hm1);

            HandlerDescription hm3 = new HandlerDescription();
            hm3.setClassName("org.apache.axis.handlers.AbstractHandler");
            hm3.setHandler(han);
            hm3.setName(new QName("H4"));
            PhaseRule rule3 = new PhaseRule();
            rule3.setPhaseName("Logging");
            hm3.setRules(rule3);
            ph.addHandler(hm3);


            ArrayList oh = ph.getOrderHandler();
            for (int i = 0; i < oh.size(); i++) {
                HandlerDescription metadata = (HandlerDescription) oh.get(i);
                System.out.println("Name:" + metadata.getName().getLocalPart());
            }
            fail("this must failed gracefully with PhaseException ");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void testInvalidPhaseRule2() {
        try {
            super.setUp();
            phaserul = new InvalidPhaseRuleTest("");
            GlobalDescription global = new GlobalDescription();
            registry = new AxisSystemImpl(global);
            ArrayList inPhase = new ArrayList();

            inPhase.add("global");
            inPhase.add("service");
            ((AxisSystemImpl) registry).setInPhases(inPhase);
            ((AxisSystemImpl) registry).setInFaultPhases(inPhase);
            ((AxisSystemImpl) registry).setOutFaultPhases(inPhase);
            ((AxisSystemImpl) registry).setOutPhases(inPhase);

            Handler han = null;
            PhaseHolder ph = new PhaseHolder(registry);
            ph.setFlowType(1);


            HandlerDescription hm = new HandlerDescription();
            hm.setClassName("org.apache.axis.handlers.AbstractHandler");
            hm.setHandler(han);
            hm.setName(new QName("H1"));
            PhaseRule rule = new PhaseRule();
            rule.setPhaseName("global");
            rule.setPhaseFirst(true);
            rule.setPhaseLast(true);
            hm.setRules(rule);
            ph.addHandler(hm);

            HandlerDescription hm1 = new HandlerDescription();
            hm1.setClassName("org.apache.axis.handlers.AbstractHandler");
            hm1.setHandler(han);
            hm1.setName(new QName("H2"));
            PhaseRule rule1 = new PhaseRule();
            rule1.setPhaseName("global");
            rule1.setAfter("H1");
            hm1.setRules(rule1);
            ph.addHandler(hm1);

            HandlerDescription hm3 = new HandlerDescription();
            hm3.setClassName("org.apache.axis.handlers.AbstractHandler");
            hm3.setHandler(han);
            hm3.setName(new QName("H4"));
            PhaseRule rule3 = new PhaseRule();
            rule3.setPhaseName("Logging");
            hm3.setRules(rule3);
            ph.addHandler(hm3);


            ArrayList oh = ph.getOrderHandler();
            for (int i = 0; i < oh.size(); i++) {
                HandlerDescription metadata = (HandlerDescription) oh.get(i);
                System.out.println("Name:" + metadata.getName().getLocalPart());
            }
            fail("this must failed gracefully with PhaseException ");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
