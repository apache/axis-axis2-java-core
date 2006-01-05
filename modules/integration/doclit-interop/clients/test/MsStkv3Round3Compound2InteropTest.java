package test;

import junit.framework.TestCase;
import test.stub.SoapInteropCompound2PortTypeStub;
import test.stub.databinding.org.soapinterop.Employee;
import test.stub.databinding.org.soapinterop.Person;
import test.stub.databinding.org.soapinterop.ResultEmployeeDocument;
import test.stub.databinding.org.soapinterop.XEmployeeDocument;

public class MsStkv3Round3Compound2InteropTest extends TestCase{

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
