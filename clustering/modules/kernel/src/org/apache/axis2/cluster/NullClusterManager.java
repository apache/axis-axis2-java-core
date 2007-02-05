package org.apache.axis2.cluster;

import org.apache.axis2.context.AbstractContext;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ServiceContext;

public class NullClusterManager implements ClusterManager{

    public void addContext(String contextId, String parentContextId, AbstractContext context) {
    }

    public void init(ConfigurationContext context) {
    }

    public void removeContext(String contextId, String parentContextId, AbstractContext context) {
    }

	public void updateState(ServiceContext ctx) {
	}

}
