package sample.rmi.server;

import java.util.Date;

/**
 * Author: amila
 * Date: Aug 14, 2007
 */
public interface Service1Interface {

    public String method1(String param1, String param2);

    public int method2(int param1, int param2);

    public Date method3(Date param1);
}
