package axis2.config;

import axis2.transport.Transport;
import axis2.Handler;

import java.util.HashMap;

/**
 * A completely cheesy registry
 */
public class Registry {
    HashMap transports = new HashMap();
    HashMap suppliers = new HashMap();

    public Transport getTransport(String name) {
        return (Transport)transports.get(name);
    }

    public void deployTransport(String name, Transport transport) {
        transports.put(name, transport);
    }

    public Supplier getSupplier(String name) {
        return (Supplier)suppliers.get(name);
    }

    public void deployHandler(String name, Supplier handler) {
        suppliers.put(name, handler);
    }
}
