package axis2;

import axis2.handlers.BasicHandler;

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

        // make response message
        
        resp.invoke(msgContext);
        return true;
    }
}
