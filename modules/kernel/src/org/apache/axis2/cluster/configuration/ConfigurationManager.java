package org.apache.axis2.cluster.configuration;

import org.apache.axis2.cluster.ClusteringFault;
import org.apache.axis2.description.AxisDescription;
import org.apache.neethi.Policy;

public interface ConfigurationManager {
	
	/*
	 * Configuration management methods
	 */
    void loadServiceGroup(String serviceGroupName);
    void unloadServiceGroup(String serviceGroupName);
    void applyPolicy(String serviceGroupName, Policy policy);
    void reloadConfiguration();
    
    /*
     * Transaction management methods
     */
    void prepare();
    void rollback();
    void commit();
    
    /**
     * For registering a configuration event listener.
     */
    void addConfigurationManagerListener(ConfigurationManagerListener listener);
}
