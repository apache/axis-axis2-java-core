/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package test;

import junit.framework.TestCase;
import test.stub.SoapInteropCompound2PortTypeStub;
import test.stub.databinding.org.soapinterop.Employee;
import test.stub.databinding.org.soapinterop.Person;
import test.stub.databinding.org.soapinterop.ResultEmployeeDocument;
import test.stub.databinding.org.soapinterop.XEmployeeDocument;

public class SunRound3Compound2InteropTest extends TestCase{

    SoapInteropCompound2PortTypeStub stub = null;
    XEmployeeDocument xEmpDoc = null;
    Employee emp = null;
    Person person = null;
    String name = "Gayan Asanka";
    boolean male = true;
    float personID = (float)456.3123;
    int id = 123456;
    double sal = 15000;
    ResultEmployeeDocument retEmpDoc = null;
    Employee retEmployee = null;


     public void setUp() throws Exception {
       stub = new SoapInteropCompound2PortTypeStub();
    }

    public void testEchoEmployee() throws Exception {
        person = Person.Factory.newInstance();
        person.setName(name);
        person.setMale(male);
        emp = Employee.Factory.newInstance();
        emp.setPerson(person);
        emp.setID(id);
        emp.setSalary(sal);
        xEmpDoc = XEmployeeDocument.Factory.newInstance();
        xEmpDoc.setXEmployee(emp);
        retEmpDoc = stub.echoEmployee(xEmpDoc);
        retEmployee = retEmpDoc.getResultEmployee();
        assertEquals(name,retEmployee.getPerson().getName());
        assertEquals(male,retEmployee.getPerson().getMale());
        assertEquals(id,retEmployee.getID());
        assertEquals(sal,retEmployee.getSalary(),0);

    }
}
