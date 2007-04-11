package org.apache.axis2.jaxws.sample.addnumbershandler;

import javax.annotation.PostConstruct;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.ws.ProtocolException;
import javax.xml.ws.handler.MessageContext;

import org.apache.axis2.jaxws.handler.SoapMessageContext;

public class AddNumbersLogicalHandler implements javax.xml.ws.handler.LogicalHandler {

    private int deduction = 1;
    
    public void close(MessageContext messagecontext) {
        // TODO Auto-generated method stub
        
    }
    
    @PostConstruct
    public void postConstruct() {
        deduction = 2;
    }

    public boolean handleFault(MessageContext messagecontext) {
        /*
        Boolean outbound = (Boolean)messagecontext.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
        if (outbound) {  // outbound response if we're on the server
            SOAPMessage msg = ((SoapMessageContext)messagecontext).getMessage();
            SOAPPart part = msg.getSOAPPart();
            part.getFirstChild().getFirstChild().getFirstChild().setTextContent("a handler was here");
        }
        */
        return true;
    }

    /*
     * this test handleMessage method is obviously not what a customer might write, but it does
     * the trick for kicking the tires in the handler framework.  The AddNumbers service takes two
     * ints as incoming params, adds them, and returns the sum.  This method subtracts 1 from the 
     * first int on the inbound request, and subtracts 1 from the int on the outbound response.
     * So the client app should expect a sum 2 less than a sum without this handler manipulating
     * the SOAP message.
     */
    public boolean handleMessage(MessageContext messagecontext) {
        Boolean outbound = (Boolean)messagecontext.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
        if (!outbound) {  // inbound request if we're on the server
            SOAPMessage msg = ((SoapMessageContext)messagecontext).getMessage();
            SOAPPart part = msg.getSOAPPart();
            // hack-ish change, but it's for testing, so who cares.
            String txt = part.getFirstChild().getFirstChild().getFirstChild().getFirstChild().getTextContent();
            if (txt.equals("99")) {
                throw new ProtocolException("I don't like the value 99");
            }
            txt = String.valueOf(Integer.valueOf(txt) - 1);
            part.getFirstChild().getFirstChild().getFirstChild().getFirstChild().setTextContent(txt);
            return true;
        } else { // outbound response if we're on the server
            SOAPMessage msg = ((SoapMessageContext)messagecontext).getMessage();
            SOAPPart part = msg.getSOAPPart();
            // hack-ish change, but it's for testing, so who cares.
            String txt = part.getFirstChild().getFirstChild().getFirstChild().getTextContent();
            txt = String.valueOf(Integer.valueOf(txt) - deduction);
            part.getFirstChild().getFirstChild().getFirstChild().getFirstChild().setTextContent(txt);
            return true;
        }
    }

}
