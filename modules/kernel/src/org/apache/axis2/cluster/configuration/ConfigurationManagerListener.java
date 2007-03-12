package org.apache.axis2.cluster.configuration;

public interface ConfigurationManagerListener {
	
    public void serviceGroupLoaded(ConfigurationEvent event);
    public void serviceGroupUnloaded(ConfigurationEvent event);
    public void policyApplied(ConfigurationEvent event);
    public void configurationReloaded (ConfigurationEvent event);
    public void prepareCalled (ConfigurationEvent event);
    public void rollbackCalled (ConfigurationEvent event);
    public void commitCalled (ConfigurationEvent event);
    
}
