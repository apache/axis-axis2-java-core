package axis2.phases;

import axis2.Phase;
import axis2.MessageContext;
import axis2.Handler;
import axis2.description.Service;

/**
 * The Dispatch Phase implementation
 */
public class DispatchPhase extends Phase {
    public void checkPreconditions(MessageContext context) throws Exception {
        System.out.println("Checking preconditions for DispatchPhase...");
    }

    public void checkPostconditions(MessageContext context) throws Exception {
        System.out.println("Checking postconditions for DispatchPhase...");
        Service service = context.getService();
        if (service == null)
            throw new Exception("No service by end of Dispatch phase!");
        System.out.println("Service is '" + service.getName() + "'");
    }
}
