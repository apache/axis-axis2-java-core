package axis2.transport.http;

import axis2.MessageContext;
import axis2.Responder;
import axis2.handlers.BasicHandler;

/**
 * HTTPResponder
 */
public class HTTPResponder extends BasicHandler implements Responder, HTTPConstants {
    public boolean invoke(MessageContext context) throws Exception {
        // The message context currently contains the response message,
        // so what we need to do here is send it as the servlet response.
        context.setProperty(RESPONSE_SENT, Boolean.TRUE);

        // put the right transport stuff on the end of the chain

        System.out.println("HTTPResponder : Writing response... (no 202 should appear)");
        return true;
    }
}
