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

package org.apache.axis2.clustering.configuration;

import org.apache.axis2.cluster.ClusteringFault;
import org.apache.axis2.cluster.configuration.ConfigurationEvent;
import org.apache.axis2.clustering.ClusterManagerTestCase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.neethi.Policy;


public abstract class ConfigurationManagerTestCase extends ClusterManagerTestCase {

    private static final Log log = LogFactory.getLog(ConfigurationManagerTestCase.class);
    
    public void testLoadServiceGroup () throws ClusteringFault {
       	configurationManagerListener2.clearEventList();
       	
    	String serviceGroupName = "testService";
    	clusterManager1.getConfigurationManager().loadServiceGroup(serviceGroupName);
    	
    	try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
    	
		ConfigurationEvent event = (ConfigurationEvent) configurationManagerListener2.getEventList().get(0);
		assertNotNull(event);
		assertEquals(event.getConfigurationName(), serviceGroupName);
    }
    
    public void testUnloadServiceGroup ()  throws ClusteringFault {
    	
    	configurationManagerListener2.clearEventList();
    	
    	String serviceGroupName = "testService";
    	clusterManager1.getConfigurationManager().unloadServiceGroup (serviceGroupName);
    	
    	try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
    	
		ConfigurationEvent event = (ConfigurationEvent) configurationManagerListener2.getEventList().get(0);
		assertNotNull(event);
		assertEquals(event.getConfigurationName(), serviceGroupName);
    }
    
    public void testApplyPolicy () throws ClusteringFault {
    	
    	configurationManagerListener2.clearEventList();
    	
    	String serviceGroupName = "testService";
    	String policyID = "policy1";
    	
    	Policy policy = new Policy ();
    	policy.setId(policyID);
    	
    	clusterManager1.getConfigurationManager().applyPolicy (serviceGroupName,policy);
    	
    	try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
    	
		ConfigurationEvent event = (ConfigurationEvent) configurationManagerListener2.getEventList().get(0);
		assertNotNull(event);
		assertEquals(event.getConfigurationName(), serviceGroupName);
		assertEquals(event.getPolicyId(), policyID);
		
    }
    
    public void testPrepare ()  throws ClusteringFault {
    	
    	configurationManagerListener2.clearEventList();
    	
    	String serviceGroupName = "testService";
    	clusterManager1.getConfigurationManager().prepare();
    	
    	try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
    	
		ConfigurationEvent event = (ConfigurationEvent) configurationManagerListener2.getEventList().get(0);
		assertNotNull(event);
    }
    
    public void testCommit ()  throws ClusteringFault {
    	
    	configurationManagerListener2.clearEventList();
    	
    	String serviceGroupName = "testService";
    	clusterManager1.getConfigurationManager().commit();
    	
    	try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
    	
		ConfigurationEvent event = (ConfigurationEvent) configurationManagerListener2.getEventList().get(0);
		assertNotNull(event);
    }
    
    public void testRollback () throws ClusteringFault {
    	
    	configurationManagerListener2.clearEventList();
    	
    	String serviceGroupName = "testService";
    	clusterManager1.getConfigurationManager().rollback();
    	
    	try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
    	
		ConfigurationEvent event = (ConfigurationEvent) configurationManagerListener2.getEventList().get(0);
		assertNotNull(event);
    }
	
    public void testReloadConfiguration () throws ClusteringFault {
    	
    	configurationManagerListener2.clearEventList();
    	
    	String serviceGroupName = "testService";
    	clusterManager1.getConfigurationManager().reloadConfiguration();
    	
    	try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
    	
		ConfigurationEvent event = (ConfigurationEvent) configurationManagerListener2.getEventList().get(0);
		assertNotNull(event);
    }
    
}
