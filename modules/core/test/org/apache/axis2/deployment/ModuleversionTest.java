package org.apache.axis2.deployment;

import junit.framework.TestCase;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.description.ModuleDescription;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.util.Utils;

import javax.xml.namespace.QName;
import java.util.Iterator;
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

public class ModuleversionTest extends TestCase {

    public void testDefautModuleVersion() throws AxisFault {
        String filename = "./test-resources/deployment/moduleVersion/Test1/axis2.xml";
        ConfigurationContextFactory builder = new ConfigurationContextFactory();
        AxisConfiguration ac = builder.createConfigurationContextFromFileSystem(null,filename)
                .getAxisConfiguration();
        assertNotNull(ac);
        assertEquals(ac.getDefaultModuleVersion("abc"), "1.23");
        assertEquals(ac.getDefaultModuleVersion("foo"), "0.89");
    }

    public void testCalculateDefaultModuleVersions() throws AxisFault {
        AxisConfiguration axiConfiguration = new AxisConfiguration();
        ModuleDescription module1 = new ModuleDescription();
        module1.setName(new QName("Module1"));
        axiConfiguration.addModule(module1);

        ModuleDescription module2 = new ModuleDescription();
        module2.setName(new QName("Module2-0.94"));
        axiConfiguration.addModule(module2);

        ModuleDescription module3 = new ModuleDescription();
        module3.setName(new QName("Module2-0.95"));
        axiConfiguration.addModule(module3);

        ModuleDescription module4 = new ModuleDescription();
        module4.setName(new QName("Module2-0.93"));
        axiConfiguration.addModule(module4);

        ModuleDescription module5 = new ModuleDescription();
        module5.setName(new QName("testModule-1.93"));
        axiConfiguration.addModule(module5);

        Utils.calculateDefaultModuleVersion(axiConfiguration.getModules(), axiConfiguration);
        assertEquals(module1, axiConfiguration.getDefaultModule("Module1"));
        assertEquals(module3, axiConfiguration.getDefaultModule("Module2"));
        assertEquals(module5, axiConfiguration.getDefaultModule("testModule"));
        axiConfiguration.engageModule(new QName("Module2"));
        axiConfiguration.engageModule(new QName("Module1"));
        axiConfiguration.engageModule("testModule", "1.93");

        Iterator engaeModuels = axiConfiguration.getEngagedModules().iterator();
        boolean found1 = false;
        boolean found2 = false;
        boolean found3 = false;
        while (engaeModuels.hasNext()) {
            QName qName = (QName) engaeModuels.next();
            if (qName.getLocalPart().equals("Module2-0.95")) {
                found1 = true;
            }
        }
        engaeModuels = axiConfiguration.getEngagedModules().iterator();
        while (engaeModuels.hasNext()) {
            QName qName = (QName) engaeModuels.next();
            if (qName.getLocalPart().equals("Module1")) {
                found2 = true;
            }
        }
        engaeModuels = axiConfiguration.getEngagedModules().iterator();
        while (engaeModuels.hasNext()) {
            QName qName = (QName) engaeModuels.next();
            if (qName.getLocalPart().equals("testModule-1.93")) {
                found3 = true;
            }
        }


        if (!found1) {
            fail("this should fail");
        }
        if (!found2) {
            fail("this should fail");
        }
        if (!found3) {
            fail("this should fail");
        }
    }
}
