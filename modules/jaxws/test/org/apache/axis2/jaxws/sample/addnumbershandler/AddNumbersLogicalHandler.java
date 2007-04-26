package org.apache.axis2.jaxws.sample.addnumbershandler;

import java.io.ByteArrayOutputStream;
import java.io.StringBufferInputStream;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.LogicalMessage;
import javax.xml.ws.ProtocolException;
import javax.xml.ws.handler.MessageContext;

import org.apache.axis2.jaxws.handler.LogicalMessageContext;

public class AddNumbersLogicalHandler implements javax.xml.ws.handler.LogicalHandler<LogicalMessageContext> {

    private int deduction = 1;
    
    public void close(MessageContext messagecontext) {
        
    }
    
    @PostConstruct
    public void postConstruct() {
        deduction = 2;
    }

    public boolean handleFault(LogicalMessageContext messagecontext) {
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
    public boolean handleMessage(LogicalMessageContext messagecontext) {
        Boolean outbound = (Boolean)messagecontext.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
        if (!outbound) {  // inbound request if we're on the server
            LogicalMessage msg = messagecontext.getMessage();
            String st = getStringFromSourcePayload(msg.getPayload());
            if (st.contains("<arg0>99</arg0>"))
                throw new ProtocolException("I don't like the value 99");
            String txt = String.valueOf(Integer.valueOf(getFirstArg(st)) - 1);
            st = replaceFirstArg(st, txt);
            msg.setPayload(new StreamSource(new StringBufferInputStream(st)));
            
        } else { // outbound response if we're on the server
            LogicalMessage msg = messagecontext.getMessage();
            String st = getStringFromSourcePayload(msg.getPayload());
            String txt = String.valueOf(Integer.valueOf(getFirstArg(st)) - deduction);
            st = replaceFirstArg(st, txt);
            msg.setPayload(new StreamSource(new StringBufferInputStream(st)));
        }
        return true;
    }
    
    private static String getFirstArg(String payloadString) {
        StringTokenizer st = new StringTokenizer(payloadString, ">");
        st.nextToken();  // skip first token.
        st.nextToken();  // skip second
        String tempString = st.nextToken();
        String returnString = new StringTokenizer(tempString, "<").nextToken();
        return returnString;
    }
    
    private static String replaceFirstArg(String payloadString, String newArg) {
        String firstArg = getFirstArg(payloadString);
        payloadString = payloadString.replaceFirst(firstArg, newArg);
        return payloadString;
    }
    
    private static String getStringFromSourcePayload(Source payload) {
        try {

            TransformerFactory factory = TransformerFactory.newInstance();
            Transformer trans = factory.newTransformer();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            StreamResult result = new StreamResult(baos);

            trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            trans.transform(payload, result);

            return new String(baos.toByteArray());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
