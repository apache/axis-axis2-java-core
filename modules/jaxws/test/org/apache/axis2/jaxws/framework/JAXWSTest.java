package org.apache.axis2.jaxws.framework;

import org.apache.axis2.jaxws.DispatchTestSuite;
import org.apache.axis2.jaxws.provider.SimpleProvider;



import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

public class JAXWSTest extends TestCase {
    /**
     * suite
     * @return
     */
    public static TestSuite suite() {
        TestSuite suite = new TestSuite();
        
        // Add each of the test suites
        suite = DispatchTestSuite.addTestSuites(suite);
        
        // Add individual test classes
        // TODO: suite.addTestSuite(SimpleProvider.class);

        return suite;
    }
/*
    // TODO:
	protected void setUp() throws Exception {
		super.setUp();
		StartServer startServer = new StartServer("server1");
		startServer.testStartServer();
	}

	// TODO:
	protected void tearDown() throws Exception {
		super.tearDown();
		StopServer stopServer = new StopServer("server1");
		stopServer.testStopServer();
	}
*/
}
