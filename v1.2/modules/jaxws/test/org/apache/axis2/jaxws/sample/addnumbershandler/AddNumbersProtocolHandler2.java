package org.apache.axis2.jaxws.sample.addnumbershandler;

import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPMessageContext;

public class AddNumbersProtocolHandler2 implements javax.xml.ws.handler.soap.SOAPHandler<SOAPMessageContext> {

    public void close(MessageContext messagecontext) {
    }

    public Set<QName> getHeaders() {
        return null;
    }
    
    public boolean handleFault(SOAPMessageContext messagecontext) {
        return true;
    }

    public boolean handleMessage(SOAPMessageContext messagecontext) {
        return true;
    }

}
