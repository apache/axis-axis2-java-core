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

package org.apache.axis2.om.infoset;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.io.File;

public class XMLConformanceTest extends TestCase {

    public static Test suite() throws Exception {
        TestSuite suite = new TestSuite();

        File testSuiteDirectory = new File("test-resources/XMLSuite/xmlconf");
        if (testSuiteDirectory.exists()) {
            ProcessDir(testSuiteDirectory, suite);
        }
        return suite;
    }

    private static void ProcessDir(File dir, TestSuite suite) throws Exception {
        if (dir.isDirectory()) {
            //process all children
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                File child = (new File(dir, children[i]));
                ProcessDir(child, suite);
            }
        } else {
            //check if it's xml file
            String absPath = dir.getAbsolutePath();
            if (absPath.endsWith(".xml")) {
                suite.addTest(new XMLConformanceUnit(absPath, "testSingleFileConformance"));
            }
        }
    }
}
