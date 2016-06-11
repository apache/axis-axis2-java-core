/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.axis2.deployment;

import org.apache.axis2.Axis2Repo;
import org.apache.axis2.AxisFault;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.engine.Handler;
import org.apache.axis2.engine.Phase;
import org.apache.axis2.registry.Handler3;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import javax.xml.stream.XMLStreamException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

public class DeploymentTotalTest {
    @Rule
    public Axis2Repo repo = new Axis2Repo("target/test-resources/deployment");

    @Before
    public void setUp() throws Exception {
        repo.getAxisConfiguration().engageModule("module1");
         // OK, no exceptions.  Now make sure we read the correct file...
    }

    @Test
    public void testparseService1() throws AxisFault, XMLStreamException {
        Parameter param = repo.getAxisConfiguration().getParameter("FavoriteColor");
        assertNotNull("No FavoriteColor parameter in axis2.xml!", param);
        assertEquals("purple", param.getValue());
    }

    @Test
    public void testDynamicPhase() {
        AxisConfiguration axisConfig = repo.getAxisConfiguration();

        List inFlow = axisConfig.getInFlowPhases();
        for (int i = 0; i < inFlow.size(); i++) {
            Phase phase = (Phase) inFlow.get(i);
            if (phase.getName().equals("NewPhase")) {
                assertEquals("Wrong index for NewPhase!", 3, i);
                assertEquals("Wrong # of handlers in NewPhase", 3, phase.getHandlerCount());
                Handler h6 = (Handler)phase.getHandlers().get(0);
                assertTrue("Wrong type for handler", h6 instanceof Handler3);
            }
        }

        inFlow = axisConfig.getInFaultFlowPhases();
        assertTrue("NewPhase wasn't found in InFaultFlow", isPhaseInFlow(inFlow, "NewPhase"));

        List outFlow = axisConfig.getInFaultFlowPhases();
        assertTrue("NewPhase wasn't found in OutFlow", isPhaseInFlow(outFlow, "NewPhase"));        
    }

    private boolean isPhaseInFlow(List inFlow, String phaseName) {
        boolean found = false;
        for (Object anInFlow : inFlow) {
            Phase phase = (Phase)anInFlow;
            if (phase.getName().equals(phaseName)) {
                found = true;
            }
        }
        return found;
    }

}
