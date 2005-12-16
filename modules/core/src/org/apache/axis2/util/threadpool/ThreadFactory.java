package org.apache.axis2.util.threadpool;

public interface ThreadFactory {
    public void execute(java.lang.Runnable runnable);
}
