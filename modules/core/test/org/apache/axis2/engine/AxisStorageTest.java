package org.apache.axis2.engine;

import junit.framework.TestCase;
import org.apache.axis2.deployment.DeploymentException;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.AxisFault;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.engine.AxisEngine;

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
 * Author: Deepal Jayasinghe
 * Date: Aug 29, 2005
 * Time: 1:45:17 PM
 */
public class AxisStorageTest extends TestCase {

    ConfigurationContext cc;

    public void testStorage() throws
            DeploymentException,
            AxisFault,
            XMLStreamException {
        String filename = "./target/test-resources/deployment";
        ConfigurationContextFactory builder = new ConfigurationContextFactory();
        cc = builder.buildConfigurationContext(filename);
        AxisEngine engine =new  AxisEngine(cc);
        assertNotNull(engine);
        String myTestVal = "There is a value";
        String key = (String)engine.store(cc,myTestVal);
        assertNotNull(key);

        String val = (String)engine.retrieve(cc,key);
        assertEquals(val,myTestVal);
        Parameter para = cc.getAxisConfiguration().getAxisStorage().getParameter("StoreLocation");
        assertNotNull(para);
    }

}
