package org.apache.axis.engine;

import junit.framework.TestCase;
import org.apache.axis.context.ConfigurationContextFactory;
import org.apache.axis.deployment.DeploymentException;
import org.apache.axis.description.ModuleDescription;
import org.apache.axis.description.OperationDescription;
import org.apache.axis.description.ServiceDescription;
import org.apache.axis.phaseresolver.PhaseException;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

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
 * Date: Jun 21, 2005
 * Time: 2:09:04 PM
 */
public class ModuleEngageTest extends TestCase{
    AxisConfiguration ac;

    public void testModuleEngageMent() throws PhaseException, DeploymentException, AxisFault, XMLStreamException {
        String filename = "./target/test-resources/deployment";
        ConfigurationContextFactory builder = new ConfigurationContextFactory();
        ac = builder.buildConfigurationContext(filename).getAxisConfiguration();
        ModuleDescription module = ac.getModule(new QName("module1"));
        assertNotNull(module);
        ac.engageModule(new QName("module1"));
        ServiceDescription service = ac.getService(new QName("service2"));
        assertNotNull(service);
        OperationDescription moduleOperation = service.getOperation(new QName("creatSeq"));
        assertNotNull(moduleOperation);
    }

}
