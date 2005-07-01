package org.apache.axis.util.threadpool;

import org.apache.axis.AbstractTestCase;
import org.apache.axis.engine.AxisFault;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Jaliya
 * Date: Jun 23, 2005
 * Time: 3:58:45 PM
 * To change this template use File | Settings | File Templates.
 */
public class TestThreadPool extends AbstractTestCase {
    /**
     * @param testName
     */
    public TestThreadPool(String testName) {
        super(testName);
    }

    class TestWorker implements AxisWorker {
        private boolean workDone;

        public void doWork() {
            workDone = true;
        }

        public boolean isWorkDone() {
            return workDone;
        }
    }


    public void testPool() throws AxisFault {
        ThreadPool tPool = new ThreadPool();
        List workerList = new ArrayList();

        for (int i = 0; i < 5; i++) {
            TestWorker worker = new TestWorker();
            workerList.add(worker);
            tPool.addWorker(worker);
        }

        tPool.safeShutDown();

        for (int i = 0; i < 5; i++) {
            assertEquals(true, ((TestWorker) workerList.get(i)).isWorkDone());
        }

    }

}
