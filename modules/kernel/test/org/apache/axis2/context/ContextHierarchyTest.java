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

package org.apache.axis2.context;

import junit.framework.TestCase;
import org.apache.axis2.AxisFault;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.AxisServiceGroup;
import org.apache.axis2.description.InOutAxisOperation;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.engine.AxisConfiguration;

import javax.xml.namespace.QName;

public class ContextHierarchyTest extends TestCase {
    private AxisOperation axisOperation;
    private AxisService axisService;
    private AxisConfiguration axisConfiguration;
    private ConfigurationContext configurationContext;
    private MessageContext msgctx;

    protected void setUp() throws Exception {
        axisOperation = new InOutAxisOperation(new QName("Temp"));
        axisService = new AxisService("Temp");
        axisConfiguration = new AxisConfiguration();
        axisService.addOperation(axisOperation);
        axisConfiguration.addService(axisService);
        configurationContext = new ConfigurationContext(axisConfiguration);
        msgctx = configurationContext.createMessageContext();
    }

    public void testCompleteHierarchy() throws AxisFault {
        ServiceGroupContext serviceGroupContext = configurationContext.createServiceGroupContext(
                (AxisServiceGroup) axisService.getParent());
        ServiceContext serviceContext = serviceGroupContext.getServiceContext(axisService);
        OperationContext opContext = axisOperation.findOperationContext(msgctx,
                                                                        serviceContext);
        axisOperation.registerOperationContext(msgctx, opContext);
        msgctx.setServiceContext(serviceContext);

        // test the complte Hierarchy built
        assertEquals(msgctx.getParent(), opContext);
        assertEquals(opContext.getParent(), serviceContext);
        assertEquals(serviceContext.getParent(), serviceGroupContext);

        String key1 = "key1";
        String value1 = "Val1";
        String value2 = "value2";
        String key2 = "key2";
        String value3 = "value";
        String key3 = "key3";

        configurationContext.setProperty(key1, value1);
        assertEquals(value1, msgctx.getProperty(key1));

        axisConfiguration.addParameter(new Parameter(key2, value2));
        assertEquals(value2, msgctx.getParameter(key2).getValue());

        opContext.setProperty(key1, value3);
        assertEquals(value3, msgctx.getProperty(key1));

        serviceContext.setProperty(key3, value3);
        assertEquals(value3, msgctx.getProperty(key3));
    }

    public void testDisconntectedHierarchy() throws AxisFault {
        // test the complete Hierarchy built
        assertEquals(msgctx.getParent(), null);

        String key1 = "key1";
        String value1 = "Val1";
        String value2 = "value2";
        String key2 = "key2";

        configurationContext.setProperty(key1, value1);
        assertEquals(value1, msgctx.getProperty(key1));

        axisConfiguration.addParameter(new Parameter(key2, value2));
        assertEquals(value2, msgctx.getParameter(key2).getValue());
    }
}
