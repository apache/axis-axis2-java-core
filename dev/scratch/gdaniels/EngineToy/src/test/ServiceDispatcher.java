package test;

import axis2.handlers.BasicHandler;
import axis2.MessageContext;
import axis2.description.Service;

/**
 * ServiceDispatcher
 */
public class ServiceDispatcher extends BasicHandler {
    public static final String SERVICE_NAME = "serviceName";

    /**
     * @param context
     * @return
     * @throws Exception
     */
    public boolean invoke(MessageContext context) throws Exception {
        System.out.println(name + ": invoke()");
        Service service = new Service();
        service.setName((String)context.getProperty(SERVICE_NAME));
        context.setService(service);
        return true;
    }
}
