package test;

import axis2.transport.http.HTTPListener;
import axis2.transport.Transport;
import axis2.AxisEngine;
import axis2.Handler;
import axis2.Constants;
import axis2.handlers.Chain;
import axis2.config.Registry;
import axis2.config.ChainSupplier;
import axis2.config.HandlerSupplier;

/**
 * Simple test simulating an HTTP request
 */
public class Tester {
    public static void main(String[] args) {
        HTTPListener listener = new HTTPListener();

        AxisEngine engine = new AxisEngine();
        Registry registry = new Registry();
        engine.setRegistry(registry);

        Transport httpTransport = new Transport();
        Handler h = new SampleHandler();
        h.setName("HTTP Transport Handler");
        httpTransport.addReceiveHandler(h);
        registry.deployTransport("http", httpTransport);

        // Create a few handlers for the global Phase
        h = new SampleHandler();
        Chain chain = new Chain();
        h.setName("Handler 1");
        chain.addHandler(h);

        h = new SampleHandler();
        h.setName("Handler 2");
        // Uncomment this to play with pausing the MC
        // h.setOption(SampleHandler.PAUSE, Boolean.TRUE);
        chain.addHandler(h);

        h = new SampleHandler();
        h.setName("Handler 3");
        chain.addHandler(h);

        registry.deployHandler(Constants.GLOBAL_RECEIVE, new ChainSupplier(chain));

        // Create a dispatcher which goes into the dispatch Phase
        h = new ServiceDispatcher();
        h.setName("Dispatcher");
        registry.deployHandler("dispatch", new HandlerSupplier(h));

        listener.setEngine(engine);

        listener.test("hey there");
    }
}
