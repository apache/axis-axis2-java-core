package org.apache.axis.deployment;

import org.apache.axis.AbstractTestCase;
import org.apache.axis.description.AxisGlobal;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * Copyright 2001-2004 The Apache Software Foundation.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * <p/>
 * Dec 24, 2004
 * 10:15:33 AM
 */
public class BadModuleTest extends AbstractTestCase {
    /**
     * Constructor.
     */
    public BadModuleTest(String testName) {
        super(testName);
    }

    public void testBadModuleXML()  {
        try {
            InputStream in = new FileInputStream(getTestResourceFile("deployment/Badmodule.xml"));
            DeploymentParser parser = new  DeploymentParser(in, null);
            AxisGlobal glabl = new AxisGlobal();
            parser.procesServerXML(glabl);
            fail("this must failed gracefully with DeploymentException or FileNotFoundException");
        } catch (FileNotFoundException e) {
            return;
        } catch (DeploymentException e) {
            return;
        } catch (Exception e) {
            return ;
        }

    }
}
