package org.apache.axis.deployment.test;

import org.apache.axis.deployment.phaserule.HandlerChain;
import org.apache.axis.deployment.phaserule.HandlerChainImpl;
import org.apache.axis.deployment.phaserule.PhaseException;
import org.apache.axis.deployment.util.Handler;

/**
 * Copyright 2001-2004 The Apache Software Foundation.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * @author Deepal Jayasinghe
 *         Nov 9, 2004
 *         4:58:58 PM
 *
 */
public class PhaseRuletest {

    public void displayPhaserules() throws PhaseException{
        HandlerChain handlerChain = new HandlerChainImpl();
        Handler h1 = new Handler();

        h1.setName("A");
        h1.setRef("abc");
        h1.setClassName("abc.java");
        h1.setPhase("P1");
        //   h1.setPhaseFirst(true);
        //    h1.setPhaseLast(false);
        handlerChain.addHandler(h1);




        Handler h3 = new Handler();
        h3.setName("C");
        h3.setRef("ddd");
        h3.setClassName("ddd.java");
        h3.setPhase("P2");
        h3.setPhaseFirst(true);
        h3.setPhaseLast(true);
        handlerChain.addHandler(h3);

        Handler h4 = new Handler();
        h4.setName("D");
        h4.setRef("xxx");
        h4.setClassName("xxx.java");
        h4.setPhase("P3");
        h4.setPhaseFirst(true);
        handlerChain.addHandler(h4);

        Handler h5 = new Handler();
        h5.setName("E");
        h5.setRef("yyy");
        h5.setClassName("yyy.java");
        h5.setPhase("P3");
        h5.setBefore("D");
        h5.setPhaseLast(true);
        handlerChain.addHandler(h5);

        Handler h6 = new Handler();
        h6.setName("F");
        h6.setRef("zzzz");
        h6.setClassName("zzzz.java");
        h6.setPhase("P3");
        h6.setBefore("E");
        handlerChain.addHandler(h6);

        //M
        Handler h8 = new Handler();
        h8.setName("P");
        h8.setRef("lll");
        h8.setClassName("lll.java");
        h8.setPhase("P4");
        h8.setBefore("M");
        handlerChain.addHandler(h8);


        Handler h10 = new Handler();
        h10.setName("N");
        h10.setRef("lll");
        h10.setClassName("lll.java");
        h10.setPhase("P4");
        h10.setAfter("M");
        h10.setBefore("K");
        handlerChain.addHandler(h10);

        Handler h7 = new Handler();
        h7.setName("M");
        h7.setRef("mmm");
        h7.setClassName("mmmm.java");
        h7.setPhase("P4");
        handlerChain.addHandler(h7);


        Handler h11 = new Handler();
        h11.setName("L");
        h11.setRef("any'");
        h11.setClassName("any");
        h11.setPhase("P4");
        h11.setAfter("N");
        handlerChain.addHandler(h11);

         Handler h9 = new Handler();
        h9.setName("K");
        h9.setRef("lll");
        h9.setClassName("lll.java");
        h9.setPhase("P4");
        h9.setAfter("M");
        handlerChain.addHandler(h9);

        Handler h2 = new Handler();
        h2.setName("B");
        h2.setRef("xyz");
        h2.setClassName("xyz.java");
        h2.setPhase("P1");
        //h2.setAfter("A");
        h2.setBefore("A");
        handlerChain.addHandler(h2);

        Handler [] handlers = handlerChain.getOrderdHandlers();

        for (int i = 0; i < handlers.length; i++) {
            Handler handler = handlers[i];
            handler.printMe();
        }
    }

    public static void main(String args []){
        PhaseRuletest phaseRuletest = new PhaseRuletest();

        try {
            phaseRuletest.displayPhaserules();
        } catch (PhaseException e) {
            e.printStackTrace();  //To change body of catch statement use Options | File Templates.
        }

    }
}
