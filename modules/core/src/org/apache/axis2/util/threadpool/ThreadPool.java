package org.apache.axis2.util.threadpool;

import org.apache.axis2.i18n.Messages;
import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * This the thread pool for axis2. This class will be used a singleton
 * across axis2 engine. <code>ThreadPool</code> is accepts <code>AxisWorkers</code> which has
 * doWork method on them and execute this method, using one of the threads
 * in the thread pool.
 */

public class ThreadPool {

    protected static Log log = LogFactory.getLog(ThreadPool.class.getName());

    private static int MAX_THREAD_COUNT = 10;
    protected static long SLEEP_INTERVAL = 1000;
    private static List threads;
    private static List tasks;
    private static boolean shoutDown;

    public ThreadPool() {
        threads = new ArrayList();
        tasks = new ArrayList();

        for (int i = 0; i < MAX_THREAD_COUNT; i++) {
            ThreadWorker threadWorker = new ThreadWorker();
            threadWorker.setPool(this);
            threads.add(threadWorker);
            threadWorker.start();
        }

    }

    public void addWorker(AxisWorker worker) throws AxisFault {
        if (shoutDown)
            throw new AxisFault(Messages.getMessage("threadpoolshutdown"));
        tasks.add(worker);
    }

    public synchronized AxisWorker getWorker() {
        if (!tasks.isEmpty()) {
            AxisWorker worker = (AxisWorker) tasks.get(0);
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
            shoutDown = true;
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
