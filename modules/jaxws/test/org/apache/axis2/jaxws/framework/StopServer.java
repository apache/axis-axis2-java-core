package org.apache.axis2.jaxws.framework;

import org.apache.axis2.jaxws.util.SimpleServer;

import junit.framework.TestCase;

public class StopServer extends TestCase {

    public StopServer(String name) {
        super(name);
    }
    
    public void testStopServer() {
        SimpleServer server = new SimpleServer();
        server.stop();
    }
}
