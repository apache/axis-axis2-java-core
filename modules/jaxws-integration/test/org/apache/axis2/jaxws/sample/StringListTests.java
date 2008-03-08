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
package org.apache.axis2.jaxws.sample;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.apache.axis2.jaxws.TestLogger;
import org.apache.axis2.jaxws.framework.AbstractTestCase;
import org.apache.axis2.jaxws.stringlist.sei.StringListPortType;
import org.apache.axis2.jaxws.stringlist.sei.StringListService;

import javax.xml.ws.BindingProvider;


public class StringListTests extends AbstractTestCase {
    String axisEndpoint = "http://localhost:6060/axis2/services/StringListPortTypeImpl.StringListPortType";
    
    public static Test suite() {
        return getTestSetup(new TestSuite(StringListTests.class));
    }
	
	public void testStringListScenario() throws Exception {
        TestLogger.logger.debug("----------------------------------");
        TestLogger.logger.debug("test: " + getName());
        StringListService sls = new StringListService();
        StringListPortType portType =sls.getStringListPort();
        BindingProvider p =	(BindingProvider)portType;
        p.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, axisEndpoint);
        String[] retString = portType.stringList(new String[]{"String1","String2","String3"});
        assertNotNull(retString);
        assertTrue(retString.length == 3);
    }
}
