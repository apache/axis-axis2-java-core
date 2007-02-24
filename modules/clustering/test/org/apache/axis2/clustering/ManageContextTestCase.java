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
import java.util.Iterator;

import org.apache.axiom.om.util.UUIDGenerator;
import org.apache.axis2.context.ContextFactory;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.context.ServiceGroupContext;


public abstract class ManageContextTestCase extends ClusterManagerTestCase {

	public void testAddContext () throws Exception {
		
		//Adding contexts to the Node1
		ServiceGroupContext serviceGroupContext1 = ContextFactory.createServiceGroupContext(configurationContext1, serviceGroup1);
		String sgcID = UUIDGenerator.getUUID();
		serviceGroupContext1.setId(sgcID);
		
		ServiceContext serviceContext1 = ContextFactory.createServiceContext(serviceGroupContext1, service1);
		
		//adding the Contexts to the first configContext 
		clusterManager1.addContext(serviceGroupContext1);
		clusterManager1.addContext(serviceContext1);
		
		//give a time interval
		Thread.sleep(5000);
		
		//The second configContext should have the newly added contexts.
		ServiceGroupContext serviceGroupContext2 = configurationContext2.getServiceGroupContext(sgcID);
		assertNotNull(serviceGroupContext2);
		
		Iterator iter = serviceGroupContext2.getServiceContexts();
		assertNotNull(iter);
		assertTrue(iter.hasNext());
		
		ServiceContext serviceContext2 = (ServiceContext) iter.next();
		assertNotNull(serviceContext2);
		assertEquals(serviceContext2.getName(), serviceName);
		
	}
	
	public void testRemoveContext () {
		
	}
	
	
}
