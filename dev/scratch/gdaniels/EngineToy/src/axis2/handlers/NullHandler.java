package axis2.handlers;

import axis2.MessageContext;

/**
 * Created by IntelliJ IDEA.
 * User: Glen
 * Date: Sep 5, 2004
 * Time: 6:01:19 PM
 * To change this template use File | Settings | File Templates.
 */
public class NullHandler extends BasicHandler {
    public boolean invoke(MessageContext context) throws Exception {
        return true; // keep going, do nothing
    }
}
