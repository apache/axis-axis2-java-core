package axis2;

import axis2.config.Registry;
import axis2.config.Supplier;
import axis2.config.HandlerSupplier;
import axis2.handlers.NullHandler;
import axis2.phases.DispatchPhase;
import axis2.transport.Transport;

import java.util.List;

/**
 * Toy AxisEngine
 */
public class AxisEngine {
    static final NullHandler nullHandler = new NullHandler();

    Registry registry;

    public void init() {
    }

    public void setRegistry(Registry registry) {
        this.registry = registry;
    }

    public void send(MessageContext context) throws Exception {
        
    }

    public boolean receive(MessageContext context) throws Exception {
        // An ExecutionChain is the set of Handlers which will be invoked
        // for THIS particular interaction/MessageContext.  This is a
        // particular mechanism for acheiving stateful pause/restart, and
        // not necessarily the best one.
        ExecutionChain chain = new ExecutionChain();

        String transportName = context.getTransport();

        // Receiving is always a matter of running the transport handlers first
        Transport transport = registry.getTransport(transportName);
        if (transport != null) {
            List receiveHandlers = transport.getReceiveHandlers();
            if (receiveHandlers != null && !receiveHandlers.isEmpty()) {
                chain.addPhase("transport", new Phase());
                chain.addHandlers(receiveHandlers);
            }
        }

        chain.addPhase(Constants.GLOBAL_RECEIVE, new Phase());
        chain.addPhase("dispatch", new DispatchPhase());
        chain.addPhase("service", new Phase());

        Supplier supplier = registry.getSupplier(Constants.GLOBAL_RECEIVE);
        supplier.deployToExecutionChainPhase(chain, Constants.GLOBAL_RECEIVE);

        supplier = registry.getSupplier("dispatch");
        supplier.deployToExecutionChainPhase(chain, "dispatch");

        Provider provider = new Provider();
        supplier = new HandlerSupplier(provider);
        supplier.deployToExecutionChainPhase(chain, "service"); 

        context.setChain(chain);

        // Set the chain back to the beginning.
        chain.reset();

        return context.start();
    }
}
