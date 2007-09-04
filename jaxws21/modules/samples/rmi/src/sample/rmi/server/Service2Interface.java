package sample.rmi.server;

import sample.rmi.server.dto.ParentClass;
import sample.rmi.server.dto.TestClass1;


public interface Service2Interface {

    public ParentClass method1(ParentClass param1);

    public TestClass1 method2(TestClass1 param1);
}
