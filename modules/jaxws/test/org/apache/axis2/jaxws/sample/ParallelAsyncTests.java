package org.apache.axis2.jaxws.sample;

import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;

import javax.xml.ws.BindingProvider;
import javax.xml.ws.Response;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.axis2.jaxws.sample.parallelasync.server.AsyncPort;
import org.apache.axis2.jaxws.sample.parallelasync.server.AsyncService;
import org.test.parallelasync.CustomAsyncResponse;
import org.test.parallelasync.SleepResponse;

/**
 * Tests for Asynchrony in JAX-WS. Most of the simple invokeAsync/async
 * exceptions have been covered under jaxws.dispatch and jaxws.proxy test suites
 * 
 * ExecutionException tests are covered in jaxws.dispatch and jaxws.proxy
 */
public class ParallelAsyncTests extends TestCase {

    private static final String DOCLITWR_ASYNC_ENDPOINT =
        "http://localhost:8080/axis2/services/AsyncService";

    public ParallelAsyncTests(String str) {
        super(str);
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(ParallelAsyncTests.class);
        return suite;
        
    }

    public void setUp() {
        System.out.println("==================== " + getName());
    }
    
    // TODO delete this test once the next is running.  This test prevents a
    // failure because JUNIT doesn't like when a test suite has no tests
    public void testPREVENT_FAILURE() throws Exception {
        
    }
    
    /**
     * @testStrategy Test that the service is up and running before running any
     *               other tests
     * @wsdl async.wsdl + async.xml
     * @target AsyncPortImpl
     */
    public void _intestService_isAlive() throws Exception {
        final String MESSAGE = "testServiceAlive";

        AsyncPort port = getPort(null);

        String req1 = "sleepAsync";
        String req2 = "remappedAsync";

        for (int i = 0; i < 10; i++) {
            Response<SleepResponse> resp1 = port.sleepAsync(req1);
            Response<CustomAsyncResponse> resp2 = port.remappedAsync(req2);

            waitBlocking(resp2);
            port.wakeUp();

            waitBlocking(resp1);
        
            try {
                String req1_result = resp1.get().getMessage();
                String req2_result = resp2.get().getResponse();
            } catch (Exception e) {
                e.printStackTrace();
                fail(e.toString());
            }

            assertEquals("sleepAsync did not return expected response ", req1, resp1.get().getMessage());
            assertEquals("remappedAsync did not return expected response", req2, resp2.get().getResponse());
        }
        
    }

    /**
     * Auxiliary method used for doiing isAsleep checks. Will perform isAsleep
     * up to a MAX_ISASLEEP_CHECK number of checks. Will sleep for
     * SLEEP_ISASLEEP_SEC seconds in between requests. If reaches maximum number
     * fo retries then will fail the test
     */
    private boolean isAsleepCheck(String MESSAGE, AsyncPort port) {
        boolean asleep = false;
        int check = 30;
        String msg = null;
        do {
            msg = port.isAsleep();
            asleep = (msg != null);

            // fail the test if we ran out of checks
            if ((check--) == 0)
                fail("Serve did not receive sleep after several retries");

            // sleep for a bit
            try {
                Thread.sleep(30);
            } catch (InterruptedException e) {
            }

        } while (!asleep);

        if (asleep) {
            assertTrue("Sleeping on an incorrect message", MESSAGE.equals(msg));
        }

        return true;
    }
    
    /**
     * Auxiliary method used for obtaining a proxy pre-configured with a
     * specific Executor
     */
    private AsyncPort getPort(Executor ex) {
        AsyncService service = new AsyncService();

        if (ex!= null)
            service.setExecutor(ex);
        
        AsyncPort port = service.getAsyncPort();
        assertNotNull("Port is null", port);

        Map<String, Object> rc = ((BindingProvider) port).getRequestContext();
        rc.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
                DOCLITWR_ASYNC_ENDPOINT);
        
        return port;
    }
    
    private void waitBlocking(Future<?> monitor){
        while (!monitor.isDone()){
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
            }
        }
    }
}
