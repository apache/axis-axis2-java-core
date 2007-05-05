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

import org.apache.axiom.om.util.UUIDGenerator;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.context.ServiceGroupContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Iterator;


public abstract class ManageContextTestCase extends ClusterManagerTestCase {

    private static final Log log = LogFactory.getLog(ClusterManagerTestCase.class);
    
	public void testAddContext () throws Exception {
		
		if (skipChannelTests) {
			String message = "Cannot runc the clustering test.Please make sure that your network service is enabled. Skipping the test...";
			log.error(message);
			return;
		}
		
		//Adding contexts to the Node1
		ServiceGroupContext serviceGroupContext1 =
                configurationContext1.createServiceGroupContext(serviceGroup1);
		String sgcID = UUIDGenerator.getUUID();
		serviceGroupContext1.setId(sgcID);
		
		ServiceContext serviceContext1 = serviceGroupContext1.getServiceContext(service1);
		
		//adding the Contexts to the first configContext 
		clusterManager1.getContextManager().addContext(serviceGroupContext1);
		clusterManager1.getContextManager().addContext(serviceContext1);
		
		//give a time interval
		Thread.sleep(5000);
		
		//The second configContext should have the newly added contexts.
		ServiceGroupContext serviceGroupContext2 = configurationContext2.getServiceGroupContext(sgcID);
		System.out.println("sgs ID 2:" + sgcID);
		assertNotNull(serviceGroupContext2);
		
		Iterator iter = serviceGroupContext2.getServiceContexts();
		assertNotNull(iter);
		assertTrue(iter.hasNext());
		
		ServiceContext serviceContext2 = (ServiceContext) iter.next();
		assertNotNull(serviceContext2);
		assertEquals(serviceContext2.getName(), serviceName);
		
	}
	
	public void testRemoveContext () {
		if (skipChannelTests) {
			String message = "Cannot runc the clustering test.Please make sure that your network service is enabled. Skipping the test...";
			log.error(message);
			return;
		}
	}
	
}
