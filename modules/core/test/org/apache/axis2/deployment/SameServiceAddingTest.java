package org.apache.axis2.deployment;

import junit.framework.TestCase;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.engine.AxisConfiguration;
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
 * Author: Deepal Jayasinghe
 * Date: Sep 16, 2005
 * Time: 11:29:06 PM
 */
public class SameServiceAddingTest extends TestCase {
    AxisConfiguration ar;
    String repo ="./test-resources/deployment/ServiceGroup";


    public void testServiceGroup() throws AxisFault {
        ConfigurationContextFactory builder = new ConfigurationContextFactory();
        ar = builder.buildConfigurationContext(repo).getAxisConfiguration();
        AxisService servie = ar.getService("serevice1");
        assertNotNull(servie);
        servie = ar.getService("serevice4");
        assertNotNull(servie);

        servie = ar.getService("serevice2");
        assertEquals(null,servie);
        assertEquals(null,ar.getServiceGroup("service2"));
    }

}
