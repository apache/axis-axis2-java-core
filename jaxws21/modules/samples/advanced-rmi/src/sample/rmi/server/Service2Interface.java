package sample.rmi.server;

import sample.rmi.server.dto.TestRestrictionBean;
import sample.rmi.server.dto.TestComplexBean;


public interface Service2Interface {

    public TestRestrictionBean method1(TestRestrictionBean param1);

    public TestComplexBean method2(TestComplexBean param1);
}
