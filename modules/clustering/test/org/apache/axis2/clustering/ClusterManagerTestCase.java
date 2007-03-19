/*
 * Copyright 2004,2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.axis2.clustering;

import junit.framework.TestCase;

import org.apache.axis2.cluster.ClusterManager;
import org.apache.axis2.cluster.ClusteringFault;
import org.apache.axis2.cluster.configuration.ConfigurationManagerListener;
import org.apache.axis2.cluster.context.ContextManagerListener;
import org.apache.axis2.cluster.listeners.DefaultContextManagerListener;
import org.apache.axis2.clustering.configuration.TestConfigurationManagerListener;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.AxisServiceGroup;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public abstract class ClusterManagerTestCase extends TestCase {

	protected ClusterManager clusterManager1 = null;
	protected ClusterManager clusterManager2 = null;
	protected AxisConfiguration axisConfiguration1 = null;
	protected AxisConfiguration axisConfiguration2 = null;
	protected ConfigurationContext configurationContext1 = null;
	protected ConfigurationContext configurationContext2 = null;
	protected AxisServiceGroup serviceGroup1 = null;
	protected AxisServiceGroup serviceGroup2 = null;
	protected AxisService service1 = null;
	protected AxisService service2 = null;
	protected String serviceName = "testService";
	protected abstract ClusterManager getClusterManager();
	protected boolean skipChannelTests = false; 
	protected TestConfigurationManagerListener configurationManagerListener1 = null;
	protected TestConfigurationManagerListener configurationManagerListener2 = null;
	protected DefaultContextManagerListener contextManagerListener1 = null;
	protected DefaultContextManagerListener contextManagerListener2 = null;
	
	private static final Log log = LogFactory.getLog(ClusterManagerTestCase.class);
    
	protected void setUp() throws Exception {

		clusterManager1 = getClusterManager();
		clusterManager2 = getClusterManager();

		configurationContext1 = ConfigurationContextFactory.createDefaultConfigurationContext();
		configurationContext2 = ConfigurationContextFactory.createDefaultConfigurationContext();

		clusterManager1.getContextManager().setConfigurationContext(configurationContext1);
		clusterManager2.getContextManager().setConfigurationContext(configurationContext2);
		
		contextManagerListener1 = new DefaultContextManagerListener ();
		clusterManager1.getContextManager(). addContextManagerListener (contextManagerListener1);
		contextManagerListener2 = new DefaultContextManagerListener ();
		clusterManager2.getContextManager(). addContextManagerListener (contextManagerListener2);	

		clusterManager1.getConfigurationManager().setAxisConfiguration(configurationContext1.getAxisConfiguration());
		clusterManager2.getConfigurationManager().setAxisConfiguration(configurationContext2.getAxisConfiguration());
		
		configurationManagerListener1 = new TestConfigurationManagerListener ();
		clusterManager1.getConfigurationManager().addConfigurationManagerListener(configurationManagerListener1);
		configurationManagerListener2 = new TestConfigurationManagerListener ();
		clusterManager2.getConfigurationManager().addConfigurationManagerListener(configurationManagerListener2);

		
		//giving both Nodes the same deployment configuration

		axisConfiguration1 = configurationContext1.getAxisConfiguration();
		serviceGroup1 = new AxisServiceGroup(axisConfiguration1);
		service1 = new AxisService(serviceName);
		serviceGroup1.addService(service1);
		axisConfiguration1.addServiceGroup(serviceGroup1);

		axisConfiguration2 = configurationContext2.getAxisConfiguration();
		serviceGroup2 = new AxisServiceGroup(axisConfiguration2);
		service2 = new AxisService(serviceName);
		serviceGroup2.addService(service2);
		axisConfiguration2.addServiceGroup(serviceGroup2);

		//Initiating ClusterManagers
		try {
			clusterManager1.init(configurationContext1);
			clusterManager2.init(configurationContext2);
		} catch (ClusteringFault e) {
			String message = "Could not initialize ClusterManagers. Please check the network connection";
			if (log.isErrorEnabled())
				log.error(message);
			
			skipChannelTests = true;
		}
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

}
