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
 
package org.apache.axis.description;

import javax.xml.namespace.QName;

import org.apache.axis.AbstractTestCase;
import org.apache.axis.context.MessageContext;
import org.apache.axis.engine.AxisFault;
import org.apache.axis.engine.AxisConfiguration;
import org.apache.axis.engine.AxisSystemImpl;
import org.apache.axis.engine.Handler;
import org.apache.axis.handlers.AbstractHandler;

public class RegistryTest extends AbstractTestCase {
    private AxisConfiguration reg;

    public RegistryTest(String testName) {
        super(testName);
    }


    public void testRegistry() throws Exception {
        GlobalDescription ag = new GlobalDescription();
        testParameteInClude(ag);
        reg = new AxisSystemImpl(ag);

        QName moduleName = new QName("module1");
        ModuleDescription modlue = new ModuleDescription(moduleName);
        reg.addMdoule(modlue);

        QName serviceName = new QName("service");
        ServiceDescription service = new ServiceDescription(serviceName);
        reg.addService(service);

        assertSame(modlue, reg.getModule(moduleName));
        assertSame(service, reg.getService(serviceName));
        reg.removeService(serviceName);
        assertSame(ag, reg.getGlobal());
        assertNull(reg.getService(serviceName));

    }

    public void testHandlerMedatata() {
        HandlerDescription hmd = new HandlerDescription();
        testParameteInClude(hmd);
    }

    public void testService() {
        ServiceDescription service = new ServiceDescription(new QName("Service1"));
        testParameteInClude(service);
        testFlowIncludeTest(service);


    }

    public void testModule() {
        ModuleDescription module = new ModuleDescription(new QName("module1"));
        testParameteInClude(module);
        testFlowIncludeTest(module);
    }

    public void testOpeartion() {
        OperationDescription op = new OperationDescription(new QName("op"));
        testParameteInClude(op);
    }


    public void testParameteInClude(ParameterInclude parmInclude) {
        String key = "value1";
        Parameter p = new ParameterImpl(key, "value2");
        parmInclude.addParameter(p);
        assertEquals(p, parmInclude.getParameter(key));
    }

    public void testFlowIncludeTest(FlowInclude flowInclude) {
        Flow flow1 = new FlowImpl();
        Flow flow2 = new FlowImpl();
        Flow flow3 = new FlowImpl();

        flowInclude.setInFlow(flow1);
        flowInclude.setFaultInFlow(flow2);
        flowInclude.setOutFlow(flow3);
        assertSame(flow1, flowInclude.getInFlow());
        assertSame(flow2, flowInclude.getFaultInFlow());
        assertSame(flow3, flowInclude.getOutFlow());
    }

 

    public void testHandlers() throws AxisFault {
        Handler handler = new AbstractHandler() {
            public void invoke(MessageContext msgContext) throws AxisFault {
            }
        };
        handler.init(new HandlerDescription());
        assertNull(handler.getName());
        assertNull(handler.getParameter("hello"));
        handler.cleanup();
    }


}
