package org.apache.axis2.phaserule;

import org.apache.axis2.AbstractTestCase;
import org.apache.axis2.description.HandlerDescription;
import org.apache.axis2.description.PhaseRule;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.engine.DispatchPhase;
import org.apache.axis2.engine.Handler;
import org.apache.axis2.engine.Phase;
import org.apache.axis2.phaseresolver.PhaseHolder;

import javax.xml.namespace.QName;
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
        ArrayList inPhase = axisSytem.getInPhasesUptoAndIncludingPostDispatch();
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
        hm.setName(new QName("H1"));
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
        hm1.setName(new QName("H2"));
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
        hm2.setName(new QName("H3"));
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
        hm3.setName(new QName("H4"));
        PhaseRule rule3 = new PhaseRule();
        rule3.setPhaseName("Dispatch");
        hm3.setRules(rule3);
        ph.addHandler(hm3);

        /*ArrayList oh = ph.getOrderHandler();
        for (int i = 0; i < oh.size(); i++) {
            HandlerDescription metadata = (HandlerDescription) oh.get(i);
            System.out.println("Name:" + metadata.getName().getLocalPart());
        }*/
    }
}
