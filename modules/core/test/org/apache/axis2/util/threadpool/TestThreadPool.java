package org.apache.axis2.util.threadpool;

import org.apache.axis2.AbstractTestCase;
import org.apache.axis2.AxisFault;

import java.util.ArrayList;
import java.util.List;

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
