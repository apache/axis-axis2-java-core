package org.apache.axis2.cluster;

import java.util.Map;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.AbstractContext;
import org.apache.axis2.context.ServiceContext;

public interface ClusterManager {

	public void init(ConfigurationContext context);

	public void addContext(String contextId, String parentContextId, AbstractContext context);
	
	public void removeContext(String contextId, String parentContextId, AbstractContext context);
	
	public void updateState(ServiceContext ctx);
	
}
