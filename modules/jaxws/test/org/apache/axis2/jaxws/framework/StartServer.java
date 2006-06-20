package org.apache.axis2.jaxws.framework;

import org.apache.axis2.jaxws.util.SimpleServer;

import junit.framework.TestCase;

public class StartServer extends TestCase {

    public StartServer(String name) {
        super(name);
    }
    
    public void testStartServer() {
        SimpleServer server = new SimpleServer();
        server.start();
    }
}
