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

package org.apache.axis2.engine;

import junit.framework.TestCase;
import org.apache.axis2.AxisFault;
import org.apache.axis2.AbstractTestCase;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.deployment.DeploymentException;
import org.apache.axis2.description.AxisModule;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.phaseresolver.PhaseException;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

public class ModuleEngageTest extends TestCase {
    AxisConfiguration ac;

    public void testModuleEngageMent() throws PhaseException,
            DeploymentException,
            AxisFault,
            XMLStreamException {
        String filename = AbstractTestCase.basedir + "/target/test-resources/deployment";
        ac = ConfigurationContextFactory.createConfigurationContextFromFileSystem(filename, null)
                .getAxisConfiguration();
        AxisModule module = ac.getModule("module1");
        assertNotNull(module);
        ac.engageModule("module1");
        AxisService service = ac.getService("service2");
        assertNotNull(service);
        AxisOperation moduleOperation = service.getOperation(
                new QName("creatSeq"));
        assertNotNull(moduleOperation);
    }

}
