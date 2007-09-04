package sample.rmi.server;

import sample.rmi.server.exception.Exception1;
import sample.rmi.server.exception.Exception2;
import sample.rmi.server.exception.Exception3;


public interface Service3Interface {
    
    public void method1() throws Exception1;

    public String method2(String param1) throws Exception2, Exception1;

    public int method3(int param1) throws Exception3, Exception2, Exception1;
}
