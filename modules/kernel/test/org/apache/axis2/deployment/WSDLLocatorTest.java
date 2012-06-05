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

package org.apache.axis2.deployment;

import org.apache.axis2.deployment.resolver.AARBasedWSDLLocator;
import org.apache.axis2.deployment.resolver.WarBasedWSDLLocator;
import org.xml.sax.InputSource;

import junit.framework.TestCase;

public class WSDLLocatorTest extends TestCase {
    
    public void testGetInputSource() {
        AARBasedWSDLLocator aarBasedWSDLLocator=new AARBasedWSDLLocator(null, null, null);
        WarBasedWSDLLocator warBasedWSDLLocator=new WarBasedWSDLLocator(null, null, null);
        InputSource inputSource=aarBasedWSDLLocator.resolveEntity(null, "http://www.test.org/test.xsd", "http://www.test.org/schema.xsd");
        assertNotNull(inputSource);
        assertEquals(inputSource.getSystemId(), "http://www.test.org/test.xsd");
        inputSource=warBasedWSDLLocator.resolveEntity(null, "http://www.test.org/test.xsd", "http://www.test.org/schema.xsd");
        assertNotNull(inputSource);
        assertEquals(inputSource.getSystemId(), "http://www.test.org/test.xsd");
    }

}
