package org.apache.axis2.cluster.context;

import org.apache.axis2.cluster.ClusteringFault;
import org.apache.axis2.context.AbstractContext;

public interface ContextManager {
    public void addContext(AbstractContext context) throws ClusteringFault;
    public void removeContext(AbstractContext context) throws ClusteringFault;
    public void updateState(AbstractContext context) throws ClusteringFault;
    public boolean isContextClusterable (AbstractContext context) throws ClusteringFault;
    public void addContextManagerListener (ContextManagerListener listener);
}
