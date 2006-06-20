package org.apache.axis2.jaxws;

import javax.xml.namespace.QName;

public class DispatchTestConstants {

    public static String URL = "http://localhost:8080/axis2/services/EchoService";
    public static QName QNAME_SERVICE = new QName("http://ws.apache.org/axis2", "EchoService");
    public static QName QNAME_PORT = new QName("http://ws.apache.org/axis2", "EchoServiceSOAP11port0");
}
