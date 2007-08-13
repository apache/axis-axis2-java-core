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

import org.apache.axis2.rmi.Configurator;
import org.apache.axis2.rmi.client.RMIClient;
import sample.rmi.server.Service1;

import java.util.ArrayList;
import java.util.List;


public class TestService1 {

    private Configurator configurator;

    public TestService1() {
        this.configurator = new Configurator();
        this.configurator.addPackageToNamespaceMaping("sample.rmi.server", "http://sample/service");
        this.configurator.addPackageToNamespaceMaping("sample.rmi.server.dto", "http://sample/service/types");
        this.configurator.addPackageToNamespaceMaping("sample.rmi.server.exception", "http://sample/service/exception");
    }

    public void testMethod1() {
        try {
            RMIClient rmiClient = new RMIClient(Service1.class, this.configurator, "http://localhost:8080/axis2/services/Service1");
            List inputObjects = new ArrayList();
            inputObjects.add("Hellow");
            inputObjects.add(" World");
            String result = (String) rmiClient.invokeMethod("method1", inputObjects);
            System.out.println("Result ==> " + result);
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    public void testMethod2() {

        try {
            RMIClient rmiClient = new RMIClient(Service1.class, configurator, "http://localhost:8080/axis2/services/Service1");
            List inputObjects = new ArrayList();
            inputObjects.add(new Integer(5));
            inputObjects.add(new Integer(15));
            Integer result = (Integer) rmiClient.invokeMethod("method2", inputObjects);
            System.out.println("Result ==> " + result);
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    public static void main(String[] args) {
        TestService1 testService1 = new TestService1();
        testService1.testMethod1();
        testService1.testMethod2();
    }
}
