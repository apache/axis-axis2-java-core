package axis2.description;

import axis2.Handler;
import axis2.MessageContext;
import axis2.handlers.BasicHandler;

/**
 * Service represents a web service
 */
public class Service extends BasicHandler {
    /**
     * @param context
     * @return
     * @throws Exception
     */
    public boolean invoke(MessageContext context) throws Exception {
        return true;
    }
}
