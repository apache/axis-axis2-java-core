package org.apache.axis.util.threadpool;

import sun.misc.Queue;

import java.util.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.axis.engine.AxisFault;


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
    private static ThreadPool instance;

    private ThreadPool() {
        threads = new ArrayList();
        tasks = new ArrayList();
    }


    public static ThreadPool getInstance() {
        if(log.isDebugEnabled())
        log.debug("ThreadPool Created");

        if (instance != null) {
            return instance;
        } else {
            instance = new ThreadPool();
            for (int i = 0; i < MAX_THREAD_COUNT; i++) {
                ThreadWorker threadWorker = new ThreadWorker();
                threadWorker.setPool(instance);
                threads.add(threadWorker);
                threadWorker.start();
            }
            return instance;
        }
    }

    public void addWorker(AxisWorker worker) throws AxisFault {
        if (shoutDown)
            throw new AxisFault("Thread Pool is Shutting Down");
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
     * @throws AxisFault
     */
    public void safeShutDown() throws AxisFault {
        synchronized (this) {
            shoutDown = true;
        }
        while (!tasks.isEmpty()) {
            try {
                Thread.sleep(SLEEP_INTERVAL);
            } catch (InterruptedException e) {
                throw new AxisFault("Error while safeShutDown", e);
            }
        }
        forceShutDown();

    }

    /**
     * A forceful shutdown mechanism for thread pool.
     */
    public void forceShutDown() {
        if(log.isDebugEnabled())
        log.debug("forceShutDown called. Thread workers will be stopped");
        Iterator ite = threads.iterator();
        while (ite.hasNext()) {
            ThreadWorker worker = (ThreadWorker) ite.next();
            worker.setStop(true);
        }
    }
}
