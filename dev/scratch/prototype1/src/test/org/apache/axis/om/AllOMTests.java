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
* Author: Eran Chinthaka - Lanka Software Foundation
* Date: Nov 29, 2004
* Time: 2:35:38 PM
*/
package org.apache.axis.om;

import junit.framework.Test;
import junit.framework.TestSuite;


public class AllOMTests {

    public static void main(String[] args) {
        junit.textui.TestRunner.run(AllOMTests.suite());
    }

    public static Test suite() {
        TestSuite suite = new TestSuite("Test for org.apache.axis.om");
        //$JUnit-BEGIN$
        suite.addTestSuite(OMTest.class);
        suite.addTestSuite(IteratorTester.class);
        suite.addTestSuite(OMnavigatorTest.class);
        suite.addTestSuite(Tester.class);
        //$JUnit-END$
        return suite;
    }
}
