/*
 * Copyright 2004,2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.axis2.util.threadpool;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This is the thread worker for the Axis2's thread pool
 * This will pick a worker from the thread pool and executes its
 * <code>run()</code> method of the particular worker.
 */
public class ThreadWorker extends Thread {
    protected static Log log = LogFactory.getLog(ThreadWorker.class.getName());
    private boolean stop;
    private ThreadPool pool;

    public void setPool(ThreadPool pool) {
        this.pool = pool;
    }

    public boolean isStop() {
        return stop;
    }

    public void setStop(boolean stop) {
        this.stop = stop;
    }

    public void run() {
        while (!stop) {
            Runnable axisWorker = null;
            try {
                axisWorker = pool.getWorker();
                if (axisWorker != null)
                    axisWorker.run();
                sleep(ThreadPool.SLEEP_INTERVAL);
            } catch (InterruptedException e) {
                log.error(e);
            }
        }
    }
}
