package axis2.config;

import axis2.ExecutionChain;
import axis2.handlers.Chain;

/**
 * ChainSupplier
 */
public class ChainSupplier implements Supplier {
    Chain chain;

    public ChainSupplier(Chain chain) {
        this.chain = chain;
    }

    public void deployToExecutionChain(ExecutionChain chain) {
        chain.addHandlers(this.chain.getHandlers());
    }

    public void deployToExecutionChainPhase(ExecutionChain chain, String phase)
            throws Exception {
        chain.addHandlersToPhase(phase, this.chain.getHandlers());
    }
}
