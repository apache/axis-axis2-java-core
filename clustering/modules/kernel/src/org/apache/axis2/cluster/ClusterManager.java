package org.apache.axis2.cluster;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.AbstractContext;

public interface ClusterManager {

	public void init(ConfigurationContext context);

	public void addContext(AbstractContext context);
	
	public void removeContext(AbstractContext context);
	
	public void updateState(AbstractContext context);
	
	/**
	 * This can be used to limit the contexts that get replicated through the 'flush' method.
	 * 
	 * @param context
	 * @return
	 */
	public boolean isContextClusterable (AbstractContext context);
	
}