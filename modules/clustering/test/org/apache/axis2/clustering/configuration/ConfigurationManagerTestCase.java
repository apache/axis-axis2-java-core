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

import java.util.List;
import java.io.ByteArrayOutputStream;
import java.io.StringWriter;

import org.apache.axis2.cluster.ClusteringFault;
import org.apache.axis2.cluster.configuration.ConfigurationEvent;
import org.apache.axis2.clustering.ClusterManagerTestCase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.neethi.Policy;

import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;


public abstract class ConfigurationManagerTestCase extends ClusterManagerTestCase {

    private static final Log log = LogFactory.getLog(ConfigurationManagerTestCase.class);
    
    public void testLoadServiceGroup () throws ClusteringFault {
       	configurationManagerListener2.clearEventList();
       	
    	String serviceGroupName = "testService";
    	clusterManager1.getConfigurationManager().loadServiceGroups(new String[]{serviceGroupName});
    	
    	try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
    	
    	List eventList = configurationManagerListener2.getEventList();
    	assertNotNull(eventList);
    	assertEquals(eventList.size(), 1);
		ConfigurationEvent event = (ConfigurationEvent) eventList.get(0);
		
		assertNotNull(event);
		assertEquals(event.getServiceGroupNames(), serviceGroupName);
    }
    
    public void testUnloadServiceGroup ()  throws ClusteringFault {
    	
    	configurationManagerListener2.clearEventList();
    	
    	String serviceGroupName = "testService";
    	clusterManager1.getConfigurationManager().unloadServiceGroups(new String[]{serviceGroupName});
    	
    	try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
    	
    	List eventList = configurationManagerListener2.getEventList();
    	assertNotNull(eventList);
    	assertEquals(eventList.size(), 1);
		ConfigurationEvent event = (ConfigurationEvent) eventList.get(0);
		
		assertNotNull(event);
		assertEquals(event.getServiceGroupNames(), serviceGroupName);
    }
    
    public void testApplyPolicy () throws ClusteringFault, XMLStreamException {
    	
    	configurationManagerListener2.clearEventList();
    	
    	String serviceGroupName = "testService";
    	String policyID = "policy1";
    	
    	Policy policy = new Policy ();
    	policy.setId(policyID);

        StringWriter writer = new StringWriter();
        XMLStreamWriter xmlStreamWriter = XMLOutputFactory.newInstance()
                .createXMLStreamWriter(writer);

        policy.serialize(xmlStreamWriter);
        xmlStreamWriter.flush();
        
        clusterManager1.getConfigurationManager().applyPolicy (serviceGroupName,writer.toString());
    	
    	try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
    	List eventList = configurationManagerListener2.getEventList();
    	assertNotNull(eventList);
    	assertEquals(eventList.size(), 1);
		ConfigurationEvent event = (ConfigurationEvent) eventList.get(0);
		assertNotNull(event);
		assertEquals(event.getServiceGroupNames(), serviceGroupName);
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
    	
    	List eventList = configurationManagerListener2.getEventList();
    	assertNotNull(eventList);
    	assertEquals(eventList.size(), 1);
		ConfigurationEvent event = (ConfigurationEvent) eventList.get(0);
		
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
    	
    	List eventList = configurationManagerListener2.getEventList();
    	assertNotNull(eventList);
    	assertEquals(eventList.size(), 1);
		ConfigurationEvent event = (ConfigurationEvent) eventList.get(0);
		
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
    	
    	List eventList = configurationManagerListener2.getEventList();
    	assertNotNull(eventList);
    	assertEquals(eventList.size(), 1);
		ConfigurationEvent event = (ConfigurationEvent) eventList.get(0);
		
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
    	
    	List eventList = configurationManagerListener2.getEventList();
    	assertNotNull(eventList);
    	assertEquals(eventList.size(), 1);
		ConfigurationEvent event = (ConfigurationEvent) eventList.get(0);
		
		assertNotNull(event);
    }
    
}
