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
 
package org.apache.axis.deployment;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.apache.axis.AbstractTestCase;
import org.apache.axis.description.AxisService;

public class BadServiceXMLTest extends AbstractTestCase {
    /**
     * Constructor.
     */
    public BadServiceXMLTest(String testName) {
        super(testName);
    }

    public void testBadServiceXML() {
        try {
            InputStream in = new FileInputStream(getTestResourceFile("deployment/BadService.xml"));
            DeploymentParser parser = new DeploymentParser(in, null);
            AxisService axisService = new AxisService();
            parser.parseServiceXML(axisService);
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
