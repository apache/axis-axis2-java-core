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

package org.apache.axis2.deployment;

import org.apache.axis2.AbstractTestCase;
import org.apache.axis2.engine.AxisConfigurationImpl;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class BadServerXMLTest extends AbstractTestCase {
    /**
     * Constructor.
     */
    public BadServerXMLTest(String testName) {
        super(testName);
    }

    public void testBadServerXML() {
        try {
            InputStream in = new FileInputStream(getTestResourceFile("deployment/BadServer.xml"));
            DeploymentParser parser = new DeploymentParser(in, null);
            AxisConfigurationImpl glabl = new AxisConfigurationImpl();
            parser.processGlobalConfig(glabl, DeploymentConstants.AXIS2CONFIG);
            fail("this must failed gracefully with DeploymentException or FileNotFoundException");
        } catch (FileNotFoundException e) {
            return;
        } catch (DeploymentException e) {
            return;
        } catch (Exception e) {
            return;
        }

    }
}
