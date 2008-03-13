package org.apache.axis2.jaxws.handler.header;

import javax.jws.HandlerChain;
import javax.jws.WebService;

@WebService(serviceName="DemoService",
		targetNamespace="http:demo/",
		portName = "DemoServicePort")
@HandlerChain(file="handler.xml")
public class DemoService {

    public String echo(String msg)  {
        System.out.println("DemoService.echo called with msg: " + msg);
        return "Hello, " + msg;
    }

}
