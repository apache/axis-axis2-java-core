/*
* Copyright 2003,2004 The Apache Software Foundation.
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
package org.apache.axis.deployment;

import javax.xml.namespace.QName;

import org.apache.axis.AbstractTestCase;
import org.apache.axis.description.AxisOperation;
import org.apache.axis.description.Flow;
import org.apache.axis.engine.EngineRegistry;
import org.apache.axis.engine.Provider;
import org.apache.axis.impl.description.AxisService;
import org.apache.axis.impl.providers.RawXMLProvider;


public class BuildERWithDeploymentTest extends AbstractTestCase{
    /**
     * @param testName
     */
    public BuildERWithDeploymentTest(String testName) {
        super(testName);
    }

    public void testDeployment(){
        try{
            String filename = "./target/test-resources/deployment" ;
            DeploymentEngine deploymentEngine = new DeploymentEngine(filename);
            EngineRegistry er = deploymentEngine.start();
            assertNotNull(er);
            assertNotNull(er.getGlobal());

            AxisService service = er.getService(new QName("echo"));
            assertNotNull(service);
            Provider provider = service.getProvider();
            assertNotNull(provider);
            assertTrue(provider instanceof RawXMLProvider);
            ClassLoader cl = service.getClassLoader();
            assertNotNull(cl);
            Class.forName("org.apache.axis.engine.Echo",true,cl);
            assertNotNull(service.getName());
            assertEquals(service.getContextPath(),"axis/service/echo");

            Flow flow = service.getFaultFlow();
            assertTrue(flow == null || flow.getHandlerCount() == 0);
            flow = service.getInFlow();
            assertTrue(flow == null || flow.getHandlerCount() == 0);
            flow = service.getOutFlow();
            assertTrue(flow == null || flow.getHandlerCount() == 0);
            assertNull(service.getParameter("hello"));

            AxisOperation op = service.getOperation(new QName("echo"));
            assertNotNull(op);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
