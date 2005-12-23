package org.apache.axis2.engine.util.profiling;

import org.apache.axis2.engine.EchoRawXMLTest;

/*
 * Copyright 2001-2004 The Apache Software Foundation.
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

public class ContextMemoryHandlingUtil extends EchoRawXMLTest {

    public ContextMemoryHandlingUtil() {
    }

    public void startup() throws Exception {
        super.setUp();
    }

    public void runOnce() throws Exception {
        super.testEchoXMLSync();
    }

    public void shutdown() throws Exception {
        super.tearDown();


    }

    public static void main(String[] args) throws Exception {
        ContextMemoryHandlingUtil contextMemoryHandlingTest = new ContextMemoryHandlingUtil();

        try {
            long initialMemory = Runtime.getRuntime().freeMemory();
            System.out.println("initialMemory = " + initialMemory);
            int numberOfTimes = 0;

            while (true) {

                System.out.println("Iterations # = " + ++numberOfTimes);
                contextMemoryHandlingTest.runOnce();
                System.out.println("Memory Usage = " + (initialMemory - Runtime.getRuntime().freeMemory()));

            }
        } catch (Exception e) {
            e.printStackTrace();
            contextMemoryHandlingTest.shutdown();
            System.exit(-1);
        }
    }
}
