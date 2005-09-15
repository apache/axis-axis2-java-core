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
import org.apache.axis2.description.OperationDescription;
import org.apache.axis2.description.ParameterImpl;
import org.apache.axis2.description.ServiceDescription;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.engine.AxisConfigurationImpl;

import javax.xml.namespace.QName;

/**
 * @author srinath
 *         <p/>
 *         To change the template for this generated type comment go to
 *         Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class ContextHierarchyTest extends TestCase {
    private OperationDescription operationDescription;
    private ServiceDescription serviceDescription;
    private AxisConfiguration axisConfiguration;

    public ContextHierarchyTest(String arg0) {
        super(arg0);
    }

    protected void setUp() throws Exception {
        operationDescription = new OperationDescription(new QName("Temp"));
        serviceDescription = new ServiceDescription(new QName("Temp"));
        axisConfiguration = new AxisConfigurationImpl();
        serviceDescription.addOperation(operationDescription);
        axisConfiguration.addService(serviceDescription);
    }

    public void testCompleteHiracy() throws AxisFault {
        ConfigurationContext configurationContext =
                new ConfigurationContext(axisConfiguration);
        ServiceGroupContext serviceGroupContext = serviceDescription.getParent().getServiceGroupContext(configurationContext);
        ServiceContext serviceCOntext =
                serviceGroupContext.getServiceContext(serviceDescription.getName().getLocalPart());
        MessageContext msgctx =
                new MessageContext(configurationContext);
        OperationContext opContext =
                operationDescription.findOperationContext(msgctx,
                        serviceCOntext);
        msgctx.setServiceContext(serviceCOntext);

        //test the complte Hisracy built
        assertEquals(msgctx.getParent(), opContext);
        assertEquals(opContext.getParent(), serviceCOntext);
        assertEquals(serviceCOntext.getParent(), serviceGroupContext);

        String key1 = "key1";
        String value1 = "Val1";
        String value2 = "value2";
        String key2 = "key2";
        String value3 = "value";

        configurationContext.setProperty(key1, value1);
        assertEquals(value1, msgctx.getProperty(key1));

        axisConfiguration.addParameter(new ParameterImpl(key2, value2));
//        assertEquals(value2, msgctx.getParameter(key2).getValue());

        opContext.setProperty(key1, value3);
        assertEquals(value3, msgctx.getProperty(key1));

    }

    public void testDisconntectedHiracy() throws AxisFault {
        ConfigurationContext configurationContext =
                new ConfigurationContext(axisConfiguration);

        MessageContext msgctx =
                new MessageContext(configurationContext);
  
        //test the complte Hisracy built
        assertEquals(msgctx.getParent(), null);

        String key1 = "key1";
        String value1 = "Val1";
        String value2 = "value2";
        String key2 = "key2";
        String value3 = "value";

        configurationContext.setProperty(key1, value1);
        assertEquals(value1, msgctx.getProperty(key1));

        axisConfiguration.addParameter(new ParameterImpl(key2, value2));
//        assertEquals(value2, msgctx.getParameter(key2).getValue());
    }

    protected void tearDown() throws Exception {
    }

}
