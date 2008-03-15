package org.apache.axis2.rmi.server.services;

import java.util.Date;
import java.util.Map;

public interface Service1Interface {

    public String method1(String param1);

    public String[] method2(String[] param1);

    public int mehtod3(int param1);

    public int[] mehtod4(int[] param1);

    public Map method5(Map param1);

    public Date method6(Date param1);
}
