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

import org.apache.axis2.AxisFault;
import org.apache.axis2.i18n.Messages;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Collections;

/**
 * This the thread pool for axis2. This class will be used a singleton
 * across axis2 engine. <code>ThreadPool</code> is accepts <code>AxisWorkers</code> which has
 * run method on them and execute this method, using one of the threads
 * in the thread pool.
 */

public class ThreadPool implements ThreadFactory {

    protected static Log log = LogFactory.getLog(ThreadPool.class.getName());

    private static int MAX_THREAD_COUNT = 10;
    protected static long SLEEP_INTERVAL = 1000;
    private static List threads;
    private static List tasks;
    private static boolean shutDown;

    public ThreadPool() {
        threads = new ArrayList();
        tasks = Collections.synchronizedList(new ArrayList());

        for (int i = 0; i < MAX_THREAD_COUNT; i++) {
            ThreadWorker threadWorker = new ThreadWorker();
            threadWorker.setPool(this);
            threads.add(threadWorker);
            threadWorker.start();
        }

    }

    public void execute(Runnable worker){
        if (shutDown)
            throw new RuntimeException(Messages.getMessage("threadpoolshutdown"));
        tasks.add(worker);
    }

    public synchronized Runnable getWorker() {
        if (!tasks.isEmpty()) {
            Runnable worker = (Runnable) tasks.get(0);
            tasks.remove(worker);
            return worker;
        } else
            return null;
    }

    /**
     * This is the recommended shutdown method for the thread pool
     * This will wait till all the workers that are already handed over to the
     * thread pool get executed.
     *
     * @throws org.apache.axis2.AxisFault
     */
    public void safeShutDown() throws AxisFault {
        synchronized (this) {
            shutDown = true;
        }
        while (!tasks.isEmpty()) {
            try {
                Thread.sleep(SLEEP_INTERVAL);
            } catch (InterruptedException e) {
                throw new AxisFault(Messages.getMessage("errorWhileSafeShutDown"));
            }
        }
        forceShutDown();

    }

    /**
     * A forceful shutdown mechanism for thread pool.
     */
    public void forceShutDown() {
        if (log.isDebugEnabled())
            log.debug("forceShutDown called. Thread workers will be stopped");
        Iterator ite = threads.iterator();
        while (ite.hasNext()) {
            ThreadWorker worker = (ThreadWorker) ite.next();
            worker.setStop(true);
        }
    }
}
