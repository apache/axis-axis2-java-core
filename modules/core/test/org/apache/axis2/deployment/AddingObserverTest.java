package org.apache.axis2.deployment;

import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.context.ConfigurationContextFactory;
import junit.framework.TestCase;

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
 * Date: Jul 19, 2005
 * Time: 10:24:15 AM
 */
public class AddingObserverTest extends TestCase{

     AxisConfiguration er;

    public void testAddingObservs() throws Exception{
        try {
            String filename = "./test-resources/deployment/ConfigWithObservers";
            ConfigurationContextFactory builder = new ConfigurationContextFactory();
            er =  builder.buildConfigurationContext(filename).getAxisConfiguration();
            assertNotNull(er);
        } catch (DeploymentException e) {
            throw new DeploymentException(e);
        }
    }
}
