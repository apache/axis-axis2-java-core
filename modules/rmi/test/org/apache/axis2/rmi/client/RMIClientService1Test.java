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
package org.apache.axis2.rmi.client;

import org.apache.axis2.rmi.server.services.Service1;
import org.apache.axis2.AxisFault;

import java.util.List;
import java.util.ArrayList;

import junit.framework.TestCase;


public class RMIClientService1Test extends TestCase {

    public void testMethod11(){

        try {
            RMIClient rmiClient = new RMIClient(Service1.class,
                    "http://localhost:8085/axis2/services/Service1");
            List inputObject = new ArrayList();
            inputObject.add("Hellow world");
            String returnString = (String) rmiClient.invokeMethod("method1",inputObject);
            assertEquals(returnString,"Hellow world");
        } catch (Exception e) {
            fail();
        }
    }

    public void testMethod12(){

        try {
            RMIClient rmiClient = new RMIClient(Service1.class,
                    "http://localhost:8085/axis2/services/Service1");
            List inputObject = new ArrayList();
            inputObject.add(null);
            String returnString = (String) rmiClient.invokeMethod("method1",inputObject);
            assertNull(returnString);
        } catch (Exception e) {
            fail();
        }
    }
}
