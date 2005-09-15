package org.apache.axis2.util.threadpool;

/**
 * Worker interface for Axis Workers.
 * When a worker is handed over to the thread pool the method <code>doWork()</code>
 * is called by the thread pool.
 */
public interface AxisWorker {
    void doWork();
}
