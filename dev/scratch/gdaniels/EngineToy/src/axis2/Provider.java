package axis2;

import axis2.handlers.BasicHandler;
import axis2.om.OMElement;

/**
 * Created by IntelliJ IDEA.
 * User: Glen
 * Date: Aug 22, 2004
 * Time: 10:11:48 PM
 * To change this template use File | Settings | File Templates.
 */
public class Provider extends BasicHandler {
    public boolean invoke(MessageContext msgContext) throws Exception {
        // Make sure there's a responder, if we're expecting to generate a response
        Responder resp = msgContext.getResponder();
        if (resp == null) {
            throw new Exception("No responder in MessageContext!");
        }

        // call java class
        System.out.println("Provider calling Java class...");

        // make response message
        String msg = msgContext.getMessage().getContent().getObjectValue().toString();

        Message response = new Message();
        OMElement om = new OMElement();
        om.setObjectValue("Response - you said '" + msg + "'");
        response.setContent(om);
        msgContext.setMessage(response);

        resp.invoke(msgContext);
        return true;
    }
}
