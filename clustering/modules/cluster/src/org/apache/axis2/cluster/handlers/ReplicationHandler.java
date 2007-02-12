package org.apache.axis2.cluster.handlers;

import org.apache.axis2.AxisFault;
import org.apache.axis2.cluster.ClusterManager;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.context.ServiceGroupContext;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.handlers.AbstractHandler;

public class ReplicationHandler extends AbstractHandler {

	public InvocationResponse invoke(MessageContext msgContext)
			throws AxisFault {
		
		replicateState(msgContext);
		
		return InvocationResponse.CONTINUE;
	}

	public void flowComplete(MessageContext msgContext) {
		super.flowComplete(msgContext);
		
		replicateState(msgContext);
	}

	private void replicateState (MessageContext message) {
		
        ConfigurationContext configurationContext = message.getConfigurationContext();
        AxisConfiguration axisConfiguration = configurationContext.getAxisConfiguration();
        ClusterManager clusterManager = axisConfiguration.getClusterManager();
        
		if (clusterManager != null){
			ServiceContext serviceContext = message.getServiceContext();
			ServiceGroupContext serviceGroupContext = message.getServiceGroupContext();
			
			clusterManager.updateState(configurationContext);
			
			if (serviceGroupContext!=null) {
				clusterManager.updateState(serviceGroupContext);
			}
			
			if (serviceContext!=null) {
				clusterManager.updateState(serviceContext);
			}
			
		}
		
	}
	
}
