package org.apache.axis2.jaxws.framework;

import org.apache.axis2.jaxws.DispatchTestSuite;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class JAXWSTest extends TestCase {
    /**
     * suite
     * @return
     */
    public static Test suite() {
        TestSuite suite = new TestSuite();
        
        // Add each of the test suites
        suite = DispatchTestSuite.addTestSuites(suite);
        
        // Add individual test classes
        // TODO: suite.addTestSuite(SimpleProvider.class);

        // Start (and stop) the server only once for all the tests
        TestSetup testSetup = new TestSetup(suite) {
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
        return testSetup;
    }
}
