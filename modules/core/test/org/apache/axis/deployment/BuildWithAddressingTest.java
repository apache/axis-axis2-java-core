package org.apache.axis.deployment;

import org.apache.axis.AbstractTestCase;
import org.apache.axis.context.ConfigurationContext;
import org.apache.axis.context.EngineContextFactory;
import org.apache.axis.description.ModuleDescription;
import org.apache.axis.engine.AxisConfiguration;
import org.apache.axis.engine.Phase;

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

    public void testDeployment()  {
        //todo this test has to be complete   Deepal once addressing module finalize
       /* String filename = "./target/test-resources/deployment";
        EngineContextFactory builder = new EngineContextFactory();
        ConfigurationContext er = builder.buildEngineContext(filename);
        ArrayList phases = er.getPhases(AxisConfiguration.INFLOW);
        ModuleDescription modeule = er.getEngineConfig().getModule(new QName("addressing"));
        assertNotNull(modeule);
        if (phases.size() <= 0) {
            fail("this must failed Since there are addressing handlers ");
        }
        for (int i = 0; i < phases.size(); i++) {
            Phase metadata = (Phase) phases.get(i);
            if ("pre-dispatch".equals(metadata.getPhaseName())) {
                if (metadata.getHandlerCount() <= 0) {
                    fail("this must failed Since there are addressing handlers ");
                } else {
                    System.out.println("Found pre-dispatch handlers");
                }
            }
        }*/

    }
}
