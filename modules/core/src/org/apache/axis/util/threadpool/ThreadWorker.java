package org.apache.axis.util.threadpool;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This is the thread worker for the Axis2's thread pool
 * This will pick a worker from the thread pool and executes its
 * <code>doWork()</code> method of the particular worker.
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
            AxisWorker axisWorker = null;
            try {
                axisWorker = pool.getWorker();
                if (axisWorker != null)
                    axisWorker.doWork();
                sleep(ThreadPool.SLEEP_INTERVAL);
            } catch (InterruptedException e) {
                log.error(e);
            }
        }
    }
}
