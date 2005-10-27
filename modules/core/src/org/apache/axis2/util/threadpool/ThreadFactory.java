package org.apache.axis2.util.threadpool;

public interface ThreadFactory {
    //public void newThread(java.lang.Runnable runnable);
    public void execute(java.lang.Runnable runnable);
}