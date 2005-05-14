package org.apache.axis.phaserule;

import org.apache.axis.AbstractTestCase;
import org.apache.axis.phaseresolver.PhaseHolder;
import org.apache.axis.description.GlobalDescription;
import org.apache.axis.description.HandlerDescription;
import org.apache.axis.description.PhaseRule;
import org.apache.axis.engine.AxisConfiguration;
import org.apache.axis.engine.AxisSystemImpl;
import org.apache.axis.engine.Handler;

import javax.xml.namespace.QName;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * Author : Deepal Jayasinghe
 * Date: Apr 26, 2005
 * Time: 4:31:18 PM
 */
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
        GlobalDescription global = new GlobalDescription();
        axisSytem = new AxisSystemImpl(global);
        ArrayList inPhase = axisSytem.getInPhasesUptoAndIncludingPostDispatch();
       
        Handler han = null;//(Handler)Class.forName("org.apache.axis.handlers.AbstractHandler").newInstance();
        PhaseHolder ph = new PhaseHolder(inPhase);

        HandlerDescription hm = new HandlerDescription();
        hm.setClassName("org.apache.axis.handlers.AbstractHandler");
        hm.setHandler(han);
        hm.setName(new QName("H1"));
        PhaseRule rule = new PhaseRule();
        rule.setPhaseName("PreDispatch");
        rule.setPhaseFirst(true);
        hm.setRules(rule);
        ph.addHandler(hm);

        HandlerDescription hm1 = new HandlerDescription();
        hm1.setClassName("org.apache.axis.handlers.AbstractHandler");
        hm1.setHandler(han);
        hm1.setName(new QName("H2"));
        PhaseRule rule1 = new PhaseRule();
        rule1.setPhaseName("Dispatch");
        rule1.setAfter("H1");
        hm1.setRules(rule1);
        ph.addHandler(hm1);

        HandlerDescription hm2 = new HandlerDescription();
        hm2.setClassName("org.apache.axis.handlers.AbstractHandler");
        hm2.setHandler(han);
        hm2.setName(new QName("H3"));
        PhaseRule rule2 = new PhaseRule();
        rule2.setPhaseName("PreDispatch");
        rule2.setAfter("H1");
        rule2.setBefore("H2");
        hm2.setRules(rule2);
        ph.addHandler(hm2);

        HandlerDescription hm3 = new HandlerDescription();
        hm3.setClassName("org.apache.axis.handlers.AbstractHandler");
        hm3.setHandler(han);
        hm3.setName(new QName("H4"));
        PhaseRule rule3 = new PhaseRule();
        rule3.setPhaseName("PreDispatch");
        hm3.setRules(rule3);
        ph.addHandler(hm3);


        ArrayList oh = ph.getOrderHandler();
        for (int i = 0; i < oh.size(); i++) {
            HandlerDescription metadata = (HandlerDescription) oh.get(i);
            System.out.println("Name:" + metadata.getName().getLocalPart());
        }
    }
}
