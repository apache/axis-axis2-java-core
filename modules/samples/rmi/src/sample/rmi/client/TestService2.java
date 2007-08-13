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
import sample.rmi.server.Service2;
import sample.rmi.server.dto.ChildClass;
import sample.rmi.server.dto.ParentClass;
import sample.rmi.server.dto.TestClass1;

import java.util.ArrayList;
import java.util.List;

public class TestService2 {

    private Configurator configurator;

    public TestService2() {
        this.configurator = new Configurator();
        this.configurator.addPackageToNamespaceMaping("sample.rmi.server", "http://sample/service");
        this.configurator.addPackageToNamespaceMaping("sample.rmi.server.dto", "http://sample/service/types");
        this.configurator.addPackageToNamespaceMaping("sample.rmi.server.exception", "http://sample/service/exception");
        this.configurator.addExtension(ParentClass.class);
        this.configurator.addExtension(ChildClass.class);
    }

    public void testMethod11() {

        try {
            RMIClient rmiClient = new RMIClient(Service2.class, configurator, "http://localhost:8080/axis2/services/Service2");
            List inputObjects = new ArrayList();

            ParentClass parentClass = new ParentClass();
            parentClass.setParam1("test param1");
            parentClass.setParam2(10);
            inputObjects.add(parentClass);
            ParentClass result = (ParentClass) rmiClient.invokeMethod("method1", inputObjects);
            System.out.println("Result param 1 ==> " + result.getParam1());
            System.out.println("Result param 2 ==> " + result.getParam2());
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    public void testMethod12() {

        try {
            RMIClient rmiClient = new RMIClient(Service2.class, configurator, "http://localhost:8080/axis2/services/Service2");
            List inputObjects = new ArrayList();

            ChildClass childClass = new ChildClass();
            childClass.setParam1("test param1");
            childClass.setParam2(10);
            childClass.setParam3("test param3");
            childClass.setParam4(new Integer(12));
            inputObjects.add(childClass);
            ChildClass result = (ChildClass) rmiClient.invokeMethod("method1", inputObjects);
            System.out.println("Result param 1 ==> " + result.getParam1());
            System.out.println("Result param 2 ==> " + result.getParam2());
            System.out.println("Result param 3 ==> " + result.getParam3());
            System.out.println("Result param 4 ==> " + result.getParam4());

        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    public void testMethod2() {

        try {
            RMIClient rmiClient = new RMIClient(Service2.class, configurator, "http://localhost:8080/axis2/services/Service2");
            List inputObjects = new ArrayList();

            TestClass1 testClass1 = new TestClass1();
            testClass1.setParam1("test param1");
            testClass1.setParma2("test param2");
            inputObjects.add(testClass1);
            TestClass1 result = (TestClass1) rmiClient.invokeMethod("method2", inputObjects);
            System.out.println("Result param 1 ==> " + result.getParam1());
            System.out.println("Result param 2 ==> " + result.getParma2());
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    public static void main(String[] args) {
        TestService2 testService2 = new TestService2();
        testService2.testMethod11();
        testService2.testMethod12();
        testService2.testMethod2();
    }

}
