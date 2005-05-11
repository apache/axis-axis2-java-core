package org.apache.axis.deployment;

import org.apache.axis.AbstractTestCase;
import org.apache.axis.phaseresolver.PhaseMetadata;
import org.apache.axis.description.AxisService;
import org.apache.axis.description.Flow;
import org.apache.axis.description.AxisOperation;
import org.apache.axis.description.AxisModule;
import org.apache.axis.engine.AxisSystem;
import org.apache.axis.engine.SimplePhase;
import org.apache.axis.context.EngineContextFactory;
import org.apache.axis.context.SystemContext;
import org.apache.axis.context.MessageContext;

import javax.xml.namespace.QName;
import java.util.ArrayList;

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
 * Time: 5:44:58 PM
 */
public class BuildWithAddressingTest extends AbstractTestCase {

    public BuildWithAddressingTest(String testName) {
        super(testName);
    }

    public void testDeployment() throws Exception {
        //TODO fix this test case
//        String filename = "./target/test-resources/deployment";
//        EngineContextFactory builder = new EngineContextFactory();
//        SystemContext er = builder.buildEngineContext(filename);
//        ArrayList phases = er.getPhases(AxisSystem.INFLOW);
//        AxisModule modeule = er.getEngineConfig().getModule(new QName("addressing"));
//        assertNotNull(modeule);
//        if (phases.size() <= 0) {
//            fail("this must failed Since there are addressing handlers ");
//        }
//        for (int i = 0; i < phases.size(); i++) {
//            SimplePhase metadata = (SimplePhase) phases.get(i);
//            if ("pre-dispatch".equals(metadata.getPhaseName())) {
//                if (metadata.getHandlerCount() <= 0) {
//                    fail("this must failed Since there are addressing handlers ");
//                } else {
//                    System.out.println("Found pre-dispatch handlers");
//                }
//            }
//        }

    }
}
