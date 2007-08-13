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
package sample.rmi.client;

import sample.rmi.server.Service3;
import sample.rmi.server.exception.Exception1;
import sample.rmi.server.exception.Exception2;
import sample.rmi.server.exception.Exception3;
import org.apache.axis2.rmi.Configurator;
import org.apache.axis2.rmi.client.RMIClient;

import java.util.ArrayList;
import java.util.List;


public class TestService3 {

    private Configurator configurator;

    public TestService3() {
        this.configurator = new Configurator();
        this.configurator.addPackageToNamespaceMaping("sample.rmi.server", "http://sample/service");
        this.configurator.addPackageToNamespaceMaping("sample.rmi.server.dto", "http://sample/service/types");
        this.configurator.addPackageToNamespaceMaping("sample.rmi.server.exception", "http://sample/service/exception");
    }

    public void testMethod1() {

        try {
            RMIClient rmiClient = new RMIClient(Service3.class, configurator, "http://localhost:8080/axis2/services/Service3");
            List inputObjects = new ArrayList();
            rmiClient.invokeMethod("method1", inputObjects);
        } catch (Exception e) {
            if (e instanceof Exception1) {
                System.out.println("Got the exception 1");
            } else {
                e.printStackTrace();
            }
        }
    }

    public void testMethod2() {

        try {
            RMIClient rmiClient = new RMIClient(Service3.class, configurator, "http://localhost:8080/axis2/services/Service3");
            List inputObjects = new ArrayList();
            inputObjects.add("test string");
            rmiClient.invokeMethod("method2", inputObjects);
        } catch (Exception e) {
            if (e instanceof Exception2) {
                System.out.println("Got the exception 2");
            } else {
                e.printStackTrace();
            }
        }
    }

    public void testMethod3() {

        try {
            RMIClient rmiClient = new RMIClient(Service3.class, configurator, "http://localhost:8080/axis2/services/Service3");
            List inputObjects = new ArrayList();
            inputObjects.add(new Integer(5));
            rmiClient.invokeMethod("method3", inputObjects);
        } catch (Exception e) {
            if (e instanceof Exception3) {
                System.out.println("Got the exception 3");
            } else {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        TestService3 testService3 = new TestService3();
        testService3.testMethod1();
        testService3.testMethod2();
        testService3.testMethod3();
    }
}
