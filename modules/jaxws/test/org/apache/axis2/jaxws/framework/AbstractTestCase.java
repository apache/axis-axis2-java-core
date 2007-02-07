package org.apache.axis2.jaxws.framework;

import junit.framework.TestCase;
import junit.framework.Test;
import junit.framework.TestSuite;
import junit.extensions.TestSetup;

public abstract class AbstractTestCase extends TestCase {

    public AbstractTestCase() {
        super(AbstractTestCase.class.getName());
    }

    public AbstractTestCase(java.lang.String string) {
        super(string);
    }

    protected static Test getTestSetup(Test test) {
        return new TestSetup(test) {
            public void setUp() {
                System.out.println("Starting the server.");
                StartServer startServer = new StartServer("server1");
                startServer.testStartServer();
            }
            public void tearDown() {
                System.out.println("Stopping the server");
                StopServer stopServer = new StopServer("server1");
                stopServer.testStopServer();
            }
        };
    }
}
