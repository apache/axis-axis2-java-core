package axis2.config;

import axis2.ExecutionChain;
import axis2.Handler;

/**
 * HandlerSupplier
 */
public class HandlerSupplier implements Supplier {
    Handler handler;

    public HandlerSupplier(Handler handler) {
        this.handler = handler;
    }

    public void deployToExecutionChain(ExecutionChain chain) {
        chain.addHandler(handler);
    }

    public void deployToExecutionChainPhase(ExecutionChain chain, String phase) throws Exception {
        chain.addHandlerToPhase(phase, handler);
    }
}
