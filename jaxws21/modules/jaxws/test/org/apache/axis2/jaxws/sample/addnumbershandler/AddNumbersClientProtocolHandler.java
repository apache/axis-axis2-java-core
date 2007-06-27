package org.apache.axis2.jaxws.sample.addnumbershandler;

import java.util.Set;

import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.MessageContext.Scope;
import javax.xml.ws.handler.soap.SOAPMessageContext;

/*
 * You can't actually specify whether a handler is for client or server,
 * you just have to check in the handleMessage and/or handleFault to make
 * sure what direction we're going.
 */

public class AddNumbersClientProtocolHandler implements javax.xml.ws.handler.soap.SOAPHandler<SOAPMessageContext> {

    public void close(MessageContext messagecontext) {
    }

    public boolean handleFault(SOAPMessageContext messagecontext) {
        return true;
    }

    public Set getHeaders() {
        return null;
    }

    public boolean handleMessage(SOAPMessageContext messagecontext) {
        Boolean outbound = (Boolean) messagecontext.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
        if (outbound) {

            String appProp1 = "AddNumbersClientProtocolHandlerOutboundAppScopedProperty";
            messagecontext.put(appProp1, "myVal");
            messagecontext.setScope(appProp1, Scope.APPLICATION);
            
            String appProp2 = "AddNumbersClientProtocolHandlerOutboundHandlerScopedProperty";
            messagecontext.put(appProp2, "client apps can't see this");
        }
        else {  // client inbound response
            String appProp1 = "AddNumbersClientProtocolHandlerInboundAppScopedProperty";
            messagecontext.put(appProp1, "myVal");
            messagecontext.setScope(appProp1, Scope.APPLICATION);
            
            String appProp2 = "AddNumbersClientProtocolHandlerInboundHandlerScopedProperty";
            messagecontext.put(appProp2, "client apps can't see this");
        }
        return true;
    }

}
