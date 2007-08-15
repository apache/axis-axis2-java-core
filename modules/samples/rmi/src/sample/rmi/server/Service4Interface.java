package sample.rmi.server;

import java.util.List;
import java.util.Map;


public interface Service4Interface {
    public Object method1(Object param1);

    public List method2(List param1, List param2);

    public String[] method3(String param1,String param2,String param3);

    public Map method4(Map param1);
}
