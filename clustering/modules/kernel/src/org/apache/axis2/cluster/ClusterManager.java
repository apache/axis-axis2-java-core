package org.apache.axis2.cluster;

import java.util.Map;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.AbstractContext;
import org.apache.axis2.context.ServiceContext;

public interface ClusterManager {

	public void init(ConfigurationContext context);

	public void addContext(String name, String id, AbstractContext context);
	
	public void updateState(ServiceContext ctx);
	
}
