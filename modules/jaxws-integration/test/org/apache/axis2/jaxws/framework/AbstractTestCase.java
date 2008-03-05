package org.apache.axis2.jaxws.framework;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestCase;
import org.apache.axis2.jaxws.TestLogger;
import org.apache.log4j.BasicConfigurator;

public class AbstractTestCase extends TestCase {
    public AbstractTestCase() {
        super();
    }

    static {
        BasicConfigurator.configure();
    }

    protected static Test getTestSetup(Test test) {
        return new TestSetup(test) {
            public void setUp() throws Exception {
                TestLogger.logger.debug("Starting the server for: " +this.getClass().getName());
                StartServer startServer = new StartServer("server1");
                startServer.testStartServer();
            }

            public void tearDown() throws Exception {
                TestLogger.logger.debug("Stopping the server for: " +this.getClass().getName());
                StopServer stopServer = new StopServer("server1");
                stopServer.testStopServer();
            }
        };
    }
}
