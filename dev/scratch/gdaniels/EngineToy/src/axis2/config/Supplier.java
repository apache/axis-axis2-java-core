package axis2.config;

import axis2.ExecutionChain;

/**
 * Supplier
 */
public interface Supplier {
    void deployToExecutionChain(ExecutionChain chain);
    void deployToExecutionChainPhase(ExecutionChain chain, String phase)
            throws Exception;
}
