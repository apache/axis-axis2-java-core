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

package org.apache.axis2.wsdl;

import javax.wsdl.WSDLException;
import javax.wsdl.extensions.AttributeExtensible;
import javax.wsdl.extensions.ExtensionRegistry;
import javax.wsdl.xml.WSDLReader;
import javax.xml.namespace.QName;

import junit.framework.TestCase;

import org.apache.axis2.addressing.AddressingConstants;

/**
 * Unit test for {@link WSDLUtil}.
 */
public class WSDLUtilTest extends TestCase {

	/**
	 * Tests whether creating new WSDLReader using {@link WSDLUtil#newWSDLReaderWithPopulatedExtensionRegistry()}
	 * correctly registers extension attributes' types.
	 * @throws WSDLException
	 */
    public void testNewWSDLReaderWithPopulatedExtensionRegistry() throws WSDLException {
    	WSDLReader reader = WSDLUtil.newWSDLReaderWithPopulatedExtensionRegistry();
    	
    	ExtensionRegistry extRegistry = reader.getExtensionRegistry();
    	assertNotNull(extRegistry);
    	
    	checkExtensionAttributeTypes(extRegistry, true);
    }

    /**
     * The method will assert that the default extension attribute types in the given <code>extensionRegistry</code>
     * are registered or not depending on the specified <code>isExpectRegistered</code> argument.
     * See {@link WSDLUtil#registerDefaultExtensionAttributeTypes(ExtensionRegistry)}.
     * 
     * @param extRegistry The extension registry to check.
     * @param isExpectRegistered Whether to expect that default extension types are registered. If set to false,
     * the method will expect that the default extension attribute types are not registered and the 
     * extension registry returns {@link AttributeExtensible.NO_DECLARED_TYPE} when queried for these.
     */
    private void checkExtensionAttributeTypes(ExtensionRegistry extRegistry, boolean isExpectRegistered) {
    	assertNotNull(extRegistry);
    	
    	int expectedType = isExpectRegistered ? AttributeExtensible.STRING_TYPE : AttributeExtensible.NO_DECLARED_TYPE;
    	
	    QName finalWSANS = new QName(AddressingConstants.Final.WSA_NAMESPACE, AddressingConstants.WSA_ACTION);
	    assertEquals(expectedType, extRegistry.queryExtensionAttributeType(javax.wsdl.Input.class, finalWSANS));
	    assertEquals(expectedType, extRegistry.queryExtensionAttributeType(javax.wsdl.Output.class, finalWSANS));
	    assertEquals(expectedType, extRegistry.queryExtensionAttributeType(javax.wsdl.Fault.class, finalWSANS));
	    
	    QName finalWSAWNS = new QName(AddressingConstants.Final.WSAW_NAMESPACE, AddressingConstants.WSA_ACTION);
	    assertEquals(expectedType, extRegistry.queryExtensionAttributeType(javax.wsdl.Input.class, finalWSAWNS));
	    assertEquals(expectedType, extRegistry.queryExtensionAttributeType(javax.wsdl.Output.class, finalWSAWNS));
	    assertEquals(expectedType, extRegistry.queryExtensionAttributeType(javax.wsdl.Fault.class, finalWSAWNS));

	    QName finalWSAMNS = new QName(AddressingConstants.Final.WSAM_NAMESPACE, AddressingConstants.WSA_ACTION);
	    assertEquals(expectedType, extRegistry.queryExtensionAttributeType(javax.wsdl.Input.class, finalWSAMNS));
	    assertEquals(expectedType, extRegistry.queryExtensionAttributeType(javax.wsdl.Output.class, finalWSAMNS));
	    assertEquals(expectedType, extRegistry.queryExtensionAttributeType(javax.wsdl.Fault.class, finalWSAMNS));
	
	    QName submissionWSAWNS = new QName(AddressingConstants.Submission.WSA_NAMESPACE, AddressingConstants.WSA_ACTION);
	    assertEquals(expectedType, extRegistry.queryExtensionAttributeType(javax.wsdl.Input.class, submissionWSAWNS));
	    assertEquals(expectedType, extRegistry.queryExtensionAttributeType(javax.wsdl.Output.class, submissionWSAWNS));
	    assertEquals(expectedType, extRegistry.queryExtensionAttributeType(javax.wsdl.Fault.class, submissionWSAWNS));
    }
}
