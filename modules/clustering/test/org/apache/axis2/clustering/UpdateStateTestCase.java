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

public abstract class UpdateStateTestCase extends ClusterManagerTestCase {

	ServiceContext serviceContext1 = null;

	ServiceGroupContext serviceGroupContext1 = null;

	String sgcID = null;

	String key1 = "key1";

	String key2 = "key2";

	String key3 = "key3";

	String val1 = "val1";

	String val2 = "val2";

	String val3 = "val3";

	public void setUp() throws Exception {
		super.setUp();

		sgcID = UUIDGenerator.getUUID();

		//Adding contexts to the Node1
		serviceGroupContext1 = ContextFactory.createServiceGroupContext(configurationContext1,
				serviceGroup1);
		serviceGroupContext1.setId(sgcID);

		serviceContext1 = ContextFactory.createServiceContext(serviceGroupContext1, service1);
		serviceGroupContext1.addServiceContext(serviceContext1);

		configurationContext1.setProperty(key1, val1);
		serviceGroupContext1.setProperty(key2, val2);
		serviceContext1.setProperty(key3, val3);

		clusterManager1.addContext(serviceGroupContext1);
		clusterManager1.addContext(serviceContext1);

		clusterManager1.updateState(configurationContext1);
		clusterManager1.updateState(serviceGroupContext1);
		clusterManager1.updateState(serviceContext1);

		Thread.sleep(1000);
	}

	public void testAddProperty() throws Exception {

		//TODO uncomment this when configCtx proeprty updates are supported
		//		Object val = configurationContext2.getProperty(key1);
		//		assertNotNull(val);
		//		assertEquals(val, val1);

		ServiceGroupContext serviceGroupContext2 = configurationContext2
				.getServiceGroupContext(sgcID);
		assertNotNull(serviceGroupContext2);
		Object val = serviceGroupContext2.getProperty(key2);
		assertNotNull(val);
		assertEquals(val, val2);

		Thread.sleep(1000);

		Iterator iter = serviceGroupContext2.getServiceContexts();
		assertNotNull(iter);
		assertTrue(iter.hasNext());
		ServiceContext serviceContext2 = (ServiceContext) iter.next();
		assertNotNull(serviceContext2);
		val = serviceContext2.getProperty(key3);
		assertNotNull(val);
		assertEquals(val, val3);

	}

	public void testRemoveProperty() throws Exception {

		serviceGroupContext1.getProperties().remove(key2);
		serviceContext1.getProperties().remove(key3);

		clusterManager1.updateState(serviceContext1);
		clusterManager1.updateState(serviceGroupContext1);

		Thread.sleep(1000);

		ServiceGroupContext serviceGroupContext2 = configurationContext2
				.getServiceGroupContext(sgcID);
		assertNotNull(serviceGroupContext2);
		Object val = serviceGroupContext2.getProperty(key2);
		assertNull(val);

		Iterator iter = serviceGroupContext2.getServiceContexts();
		assertNotNull(iter);
		assertTrue(iter.hasNext());
		ServiceContext serviceContext2 = (ServiceContext) iter.next();
		assertNotNull(serviceContext2);
		val = serviceContext2.getProperty(key3);
		assertNull(val);

	}

	public void testUpdateProperty() throws Exception {

		serviceGroupContext1.setProperty(key2, val3);
		serviceContext1.setProperty(key3, val2);

		clusterManager1.updateState(serviceContext1);
		clusterManager1.updateState(serviceGroupContext1);

		Thread.sleep(1000);

		ServiceGroupContext serviceGroupContext2 = configurationContext2
				.getServiceGroupContext(sgcID);
		assertNotNull(serviceGroupContext2);
		Object val = serviceGroupContext2.getProperty(key2);
		assertNotNull(val);
		assertEquals(val, val3);

		Iterator iter = serviceGroupContext2.getServiceContexts();
		assertNotNull(iter);
		assertTrue(iter.hasNext());
		ServiceContext serviceContext2 = (ServiceContext) iter.next();
		assertNotNull(serviceContext2);
		val = serviceContext2.getProperty(key3);
		assertNotNull(val);
		assertEquals(val, val2);

	}

}
