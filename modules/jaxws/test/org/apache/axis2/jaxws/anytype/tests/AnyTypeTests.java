/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.axis2.jaxws.anytype.tests;

import junit.framework.TestCase;
import org.apache.axis2.jaxws.anytype.sei.AnyTypeMessagePortType;
import org.apache.axis2.jaxws.anytype.sei.AnyTypeMessageService;
import org.apache.axis2.jaxws.TestLogger;

public class AnyTypeTests extends TestCase {
	
	public void testAnyTypeElementinWrappedWSDL(){
        TestLogger.logger.debug("------------------------------");
        TestLogger.logger.debug("Test : " + getName());
		try{
			AnyTypeMessageService service = new AnyTypeMessageService();
			AnyTypeMessagePortType portType = service.getAnyTypePort();
			String req = new String("Request as String");
			Object response = portType.echoMessage(req);
			assertTrue(response instanceof String);
            TestLogger.logger.debug("Response =" + response);
			System.out.print("---------------------------------");
		}catch(Exception e){
			e.printStackTrace();
			fail();
		}
		
	}
}
