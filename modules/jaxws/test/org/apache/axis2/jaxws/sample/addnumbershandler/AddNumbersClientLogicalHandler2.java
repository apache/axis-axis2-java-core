package org.apache.axis2.jaxws.sample.addnumbershandler;

import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.ws.handler.MessageContext;

import org.apache.axis2.jaxws.handler.SoapMessageContext;

/*
 * You can't actually specify whether a handler is for client or server,
 * you just have to check in the handleMessage and/or handleFault to make
 * sure what direction we're going.
 */

public class AddNumbersClientLogicalHandler2 implements javax.xml.ws.handler.LogicalHandler {
    
    public void close(MessageContext messagecontext) {
        // TODO Auto-generated method stub
    }

    public boolean handleFault(MessageContext messagecontext) {
        return true;
    }

    public boolean handleMessage(MessageContext messagecontext) {
        Boolean outbound = (Boolean)messagecontext.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
        if (outbound) {  // outbound request on the client
            SOAPMessage msg = ((SoapMessageContext)messagecontext).getMessage();
            SOAPPart part = msg.getSOAPPart();
            // hack-ish change, but it's for testing, so who cares.
            String txt = part.getFirstChild().getFirstChild().getFirstChild().getFirstChild().getTextContent();
            txt = String.valueOf(Integer.valueOf(txt) * 2);
            part.getFirstChild().getFirstChild().getFirstChild().getFirstChild().setTextContent(txt);
        }
        return true;
    }

}
