/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.axis2.util.threadpool;

import org.apache.axis2.AbstractTestCase;
import org.apache.axis2.AxisFault;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class TestThreadPool extends AbstractTestCase {
    /**
     * @param testName
     */
    public TestThreadPool(String testName) {
        super(testName);
    }

    class TestWorker implements Runnable {
        private boolean workDone;

        public void run() {
            workDone = true;
        }

        public boolean isWorkDone() {
            return workDone;
        }
    }


    public void testPool() throws Exception {
        ThreadPool tPool = new ThreadPool();
        List workerList = new ArrayList();

        for (int i = 0; i < 5; i++) {
            TestWorker worker = new TestWorker();
            workerList.add(worker);
            tPool.execute(worker);
        }

        tPool.safeShutDown();
        ThreadPoolExecutor executor = (ThreadPoolExecutor) tPool.getExecutor();
        executor.awaitTermination(5, TimeUnit.SECONDS);

        for (int i = 0; i < 5; i++) {
            assertEquals(true, ((TestWorker) workerList.get(i)).isWorkDone());
        }

    }

    /**
     * Test that core threads time out and terminate after the keepAlive period
     * when allowCoreThreadTimeOut is enabled (AXIS2-5696).
     */
    public void testCoreThreadsTimeOut() throws Exception {
        ThreadPool tPool = new ThreadPool();
        ThreadPoolExecutor executor = (ThreadPoolExecutor) tPool.getExecutor();

        // Verify allowCoreThreadTimeOut is enabled
        assertTrue("allowCoreThreadTimeOut should be enabled",
                executor.allowsCoreThreadTimeOut());

        // Submit a task directly to the executor to create a core thread
        TestWorker worker = new TestWorker();
        executor.execute(worker);

        // Wait briefly for the task to complete and the thread to be created
        Thread.sleep(500);
        assertTrue("Worker should have completed", worker.isWorkDone());
        assertTrue("Pool should have at least one thread",
                executor.getPoolSize() > 0);

        // Wait for keepAlive timeout (10 seconds) plus buffer
        Thread.sleep(12_000);

        // Core threads should have timed out and terminated
        assertEquals("All core threads should have timed out", 0,
                executor.getPoolSize());
    }

}
