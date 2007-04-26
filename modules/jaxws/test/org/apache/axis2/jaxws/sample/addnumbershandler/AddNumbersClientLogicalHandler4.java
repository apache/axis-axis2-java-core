package org.apache.axis2.jaxws.sample.addnumbershandler;

import javax.xml.ws.handler.MessageContext;

import org.apache.axis2.jaxws.handler.LogicalMessageContext;

/*
 * You can't actually specify whether a handler is for client or server,
 * you just have to check in the handleMessage and/or handleFault to make
 * sure what direction we're going.
 */

public class AddNumbersClientLogicalHandler4  implements javax.xml.ws.handler.LogicalHandler<LogicalMessageContext> {

    public void close(MessageContext messagecontext) {
        // TODO Auto-generated method stub        
    }

    public boolean handleFault(LogicalMessageContext messagecontext) {
        return true;
    }

    public boolean handleMessage(LogicalMessageContext mc) {
        return true;
    }
    
}
