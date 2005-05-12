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

/**
 * Created by IntelliJ IDEA.
 * Author : Deepal Jayasinghe
 * Date: Apr 26, 2005
 * Time: 4:31:18 PM
 */
public class PhaseRuleTest extends AbstractTestCase {

    PhaseRuleTest phaserul;
    AxisConfiguration registry;

    public PhaseRuleTest(String testName) {
        super(testName);
    }

    public void testPhaseRules() throws Exception {
        super.setUp();
        phaserul = new PhaseRuleTest("");
        GlobalDescription global = new GlobalDescription();
        registry = new AxisSystemImpl(global);
        ArrayList inPhase = new ArrayList();

        inPhase.add("global");
        inPhase.add("transport");
        inPhase.add("Logging");
        inPhase.add("service");
        ((AxisSystemImpl) registry).setInPhases(inPhase);
        ((AxisSystemImpl) registry).setInFaultPhases(inPhase);
        ((AxisSystemImpl) registry).setOutFaultPhases(inPhase);
        ((AxisSystemImpl) registry).setOutPhases(inPhase);

        Handler han = null;//(Handler)Class.forName("org.apache.axis.handlers.AbstractHandler").newInstance();
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

        HandlerDescription hm2 = new HandlerDescription();
        hm2.setClassName("org.apache.axis.handlers.AbstractHandler");
        hm2.setHandler(han);
        hm2.setName(new QName("H3"));
        PhaseRule rule2 = new PhaseRule();
        rule2.setPhaseName("global");
        rule2.setAfter("H1");
        rule2.setBefore("H2");
        hm2.setRules(rule2);
        ph.addHandler(hm2);

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
    }

    //
}
