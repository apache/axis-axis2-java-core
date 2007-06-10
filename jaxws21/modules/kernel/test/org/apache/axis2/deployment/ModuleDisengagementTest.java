package org.apache.axis2.deployment;

import junit.framework.TestCase;
import org.apache.axis2.AxisFault;
import org.apache.axis2.AbstractTestCase;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.description.AxisModule;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.InOutAxisOperation;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.engine.Phase;

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

public class ModuleDisengagementTest extends TestCase {
    AxisConfiguration er;
    String serviceName = "testService";
    QName opName = new QName("testOperation");

    protected void setUp() throws Exception {
        String filename =
                AbstractTestCase.basedir + "/test-resources/deployment/moduleDisEngegeRepo";
        er = ConfigurationContextFactory.
                createConfigurationContextFromFileSystem(filename, null).getAxisConfiguration();
        AxisService testService = new AxisService();
        testService.setName(serviceName);
        AxisOperation testOperation = new InOutAxisOperation();
        testOperation.setName(opName);
        testService.addOperation(testOperation);
        er.addService(testService);
    }

    public void testGloalDisengagement() throws AxisFault {
        AxisModule module = er.getModule("testModule");
        assertNotNull(module);
        Phase predisptah;
        Phase userPhase;
        ArrayList globalinflow = er.getInFlowPhases();
        assertNotNull(globalinflow);
        predisptah = (Phase) globalinflow.get(2);
        assertNotNull(predisptah);
        assertEquals(predisptah.getHandlerCount(), 0);
        AxisService service = er.getService(serviceName);
        assertNotNull(service);
        AxisOperation operation = service.getOperation(opName);
        assertNotNull(operation);
        userPhase = (Phase) operation.getRemainingPhasesInFlow().get(1);
        assertNotNull(userPhase);
        assertEquals(0, userPhase.getHandlerCount());
        er.engageModule(module.getName());
        assertEquals(predisptah.getHandlerCount(), 2);
        assertEquals(1, userPhase.getHandlerCount());
        er.disengageModule(module);
        assertEquals(predisptah.getHandlerCount(), 0);
        assertEquals(0, userPhase.getHandlerCount());
    }

    public void testServiceDisengagement() throws AxisFault {
        AxisModule module = er.getModule("testModule");
        assertNotNull(module);
        Phase predisptah;
        Phase userPhase;
        ArrayList globalinflow = er.getInFlowPhases();
        assertNotNull(globalinflow);
        predisptah = (Phase) globalinflow.get(2);
        assertNotNull(predisptah);
        assertEquals(predisptah.getHandlerCount(), 0);
        AxisService service = er.getService(serviceName);
        assertNotNull(service);
        AxisOperation operation = service.getOperation(opName);
        assertNotNull(operation);
        userPhase = (Phase) operation.getRemainingPhasesInFlow().get(1);
        assertNotNull(userPhase);
        assertEquals(0, userPhase.getHandlerCount());
        er.engageModule(module.getName());
        assertEquals(predisptah.getHandlerCount(), 2);
        assertEquals(1, userPhase.getHandlerCount());
        service.disengageModule(module);
        assertEquals(predisptah.getHandlerCount(), 2);
        assertEquals(0, userPhase.getHandlerCount());
    }


    public void testGlobalChcek() throws AxisFault {
        AxisModule module = er.getModule("testModule");
        assertNotNull(module);
        er.engageModule(module.getName());
        er.disengageModule(module);
        er.engageModule(module.getName());
    }

    public void testOperationDisengagement() throws AxisFault {
        AxisModule module = er.getModule("testModule");
        assertNotNull(module);
        Phase predisptah;
        Phase userPhase;
        ArrayList globalinflow = er.getInFlowPhases();
        assertNotNull(globalinflow);
        predisptah = (Phase) globalinflow.get(2);
        assertNotNull(predisptah);
        assertEquals(predisptah.getHandlerCount(), 0);
        AxisService service = er.getService(serviceName);
        assertNotNull(service);
        AxisOperation operation = service.getOperation(opName);
        assertNotNull(operation);
        userPhase = (Phase) operation.getRemainingPhasesInFlow().get(1);
        assertNotNull(userPhase);
        assertEquals(0, userPhase.getHandlerCount());
        er.engageModule(module.getName());
        assertEquals(predisptah.getHandlerCount(), 2);
        assertEquals(1, userPhase.getHandlerCount());
        operation.disengageModule(module);
        assertEquals(predisptah.getHandlerCount(), 2);
        assertEquals(0, userPhase.getHandlerCount());
    }

    public void testServiceEnageServiceDisengag() throws AxisFault {
        AxisModule module = er.getModule("testModule");
        assertNotNull(module);
        Phase predisptah;
        Phase userPhase;
        ArrayList globalinflow = er.getInFlowPhases();
        assertNotNull(globalinflow);
        predisptah = (Phase) globalinflow.get(2);
        assertNotNull(predisptah);
        assertEquals(predisptah.getHandlerCount(), 0);
        AxisService service = er.getService(serviceName);
        assertNotNull(service);
        AxisOperation operation = service.getOperation(opName);
        assertNotNull(operation);
        userPhase = (Phase) operation.getRemainingPhasesInFlow().get(1);
        assertNotNull(userPhase);
        assertEquals(0, userPhase.getHandlerCount());
        service.engageModule(module);
        assertEquals(predisptah.getHandlerCount(), 2);
        assertEquals(1, userPhase.getHandlerCount());
        service.disengageModule(module);
        assertEquals(predisptah.getHandlerCount(), 0);
        assertEquals(0, userPhase.getHandlerCount());
    }

    public void testServiceEnageOperationDisengag() throws AxisFault {
        AxisModule module = er.getModule("testModule");
        assertNotNull(module);
        Phase predisptah;
        Phase userPhase;
        ArrayList globalinflow = er.getInFlowPhases();
        assertNotNull(globalinflow);
        predisptah = (Phase) globalinflow.get(2);
        assertNotNull(predisptah);
        assertEquals(predisptah.getHandlerCount(), 0);
        AxisService service = er.getService(serviceName);
        assertNotNull(service);
        AxisOperation operation = service.getOperation(opName);
        assertNotNull(operation);
        userPhase = (Phase) operation.getRemainingPhasesInFlow().get(1);
        assertNotNull(userPhase);
        assertEquals(0, userPhase.getHandlerCount());
        service.engageModule(module);
        assertEquals(predisptah.getHandlerCount(), 2);
        assertEquals(1, userPhase.getHandlerCount());
        operation.disengageModule(module);
        assertEquals(predisptah.getHandlerCount(), 2);
        assertEquals(0, userPhase.getHandlerCount());
    }

    public void testOperationEnageOperationDisengage() throws AxisFault {
        AxisModule module = er.getModule("testModule");
        assertNotNull(module);
        Phase predisptah;
        Phase userPhase;
        ArrayList globalinflow = er.getInFlowPhases();
        assertNotNull(globalinflow);
        predisptah = (Phase) globalinflow.get(2);
        assertNotNull(predisptah);
        assertEquals(predisptah.getHandlerCount(), 0);
        AxisService service = er.getService(serviceName);
        assertNotNull(service);
        AxisOperation operation = service.getOperation(opName);
        assertNotNull(operation);
        userPhase = (Phase) operation.getRemainingPhasesInFlow().get(1);
        assertNotNull(userPhase);
        assertEquals(0, userPhase.getHandlerCount());
        operation.engageModule(module);
        assertEquals(predisptah.getHandlerCount(), 2);
        assertEquals(1, userPhase.getHandlerCount());
        operation.disengageModule(module);
        assertEquals(predisptah.getHandlerCount(), 0);
        assertEquals(0, userPhase.getHandlerCount());
    }


}
