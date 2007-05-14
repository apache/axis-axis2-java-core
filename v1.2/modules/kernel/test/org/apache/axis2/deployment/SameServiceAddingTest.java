package org.apache.axis2.deployment;

import junit.framework.TestCase;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.AxisServiceGroup;
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

    public void testServiceGroup() throws AxisFault {
        ar = ConfigurationContextFactory.createConfigurationContextFromFileSystem(null, null)
                .getAxisConfiguration();
        AxisServiceGroup axisServiceGroup1 = new AxisServiceGroup();
        axisServiceGroup1.setServiceGroupName("ServiceGroup1");
        AxisService service1 = new AxisService();
        service1.setName("serevice1");
        axisServiceGroup1.addService(service1);

        AxisService service4 = new AxisService();
        service4.setName("serevice4");
        axisServiceGroup1.addService(service4);
        ar.addServiceGroup(axisServiceGroup1);


        AxisServiceGroup axisServiceGroup2 = new AxisServiceGroup();
        axisServiceGroup2.setServiceGroupName("ServiceGroup2");
        AxisService service2 = new AxisService();
        service2.setName("serevice2");
        axisServiceGroup2.addService(service2);

        AxisService service24 = new AxisService();
        service24.setName("serevice4");
        axisServiceGroup2.addService(service24);
        try {
            ar.addServiceGroup(axisServiceGroup2);
        } catch (AxisFault axisFault) {
            //I have to ignore this
        }


        AxisService servie = ar.getService("serevice1");
        assertNotNull(servie);
        servie = ar.getService("serevice4");
        assertNotNull(servie);

        servie = ar.getService("serevice2");
        assertEquals(null, servie);
        assertEquals(null, ar.getServiceGroup("service2"));
    }

}
