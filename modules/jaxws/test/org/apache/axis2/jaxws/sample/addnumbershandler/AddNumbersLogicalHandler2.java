package org.apache.axis2.jaxws.sample.addnumbershandler;

import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.ws.handler.MessageContext;

import org.apache.axis2.jaxws.handler.SoapMessageContext;
import org.w3c.dom.Node;

public class AddNumbersLogicalHandler2 implements javax.xml.ws.handler.LogicalHandler {

    public void close(MessageContext messagecontext) {
        // TODO Auto-generated method stub
        
    }

    public boolean handleFault(MessageContext messagecontext) {
        Boolean outbound = (Boolean)messagecontext.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
        if (outbound) {  // outbound response if we're on the server
            SOAPMessage msg = ((SoapMessageContext)messagecontext).getMessage();
            SOAPPart part = msg.getSOAPPart();
            Node node1 = part.getFirstChild();
            if (node1 != null) {
                Node node2 = node1.getFirstChild();
                if (node2 != null) {
                    Node node3 = node2.getFirstChild();
                    if (node3 != null) {
                        Node node4 = node3.getFirstChild();
                        if (node4 != null) {
                            Node node5 = node4.getNextSibling();
                            if (node5 != null)
                                node5.setTextContent("AddNumbersLogicalHandler2 was here");
                        }
                    }
                }
            }
            //part.getFirstChild().getFirstChild().getFirstChild().getFirstChild().getNextSibling().setTextContent("AddNumbersLogicalHandler2 was here");
        }
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
    	return true;
    }

}
