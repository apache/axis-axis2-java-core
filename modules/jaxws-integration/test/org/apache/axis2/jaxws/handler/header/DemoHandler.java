package org.apache.axis2.jaxws.handler.header;

import javax.xml.namespace.QName;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import java.util.HashSet;
import java.util.Set;

public class DemoHandler implements SOAPHandler<SOAPMessageContext> {

    public Set<QName> getHeaders() {
        System.out.println("DemoHandler.getHeaders Invoked by JAXWS");
        Set<QName> result = new HashSet<QName>();
        result.add(new QName("http://demo/", "myheader"));
        return result;
    }

    public void close(MessageContext arg0) {

    }

    public boolean handleFault(SOAPMessageContext arg0) {
        return true;
    }

    public boolean handleMessage(SOAPMessageContext arg0) {
        System.out.println("DemoHandler.handleMessage");
        return true;
    }



}
