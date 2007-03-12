package org.apache.axis2.cluster.context;


public interface ContextManagerListener {
    public void contextAdded(ContextEvent event);
    public void contextRemoved(ContextEvent event);
    public void contextUpdated(ContextEvent event);
}
