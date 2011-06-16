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

package org.apache.axis2.description.java2wsdl;

import java.math.BigInteger;

import javax.activation.DataHandler;
import javax.xml.namespace.QName;

import junit.framework.TestCase;

import org.apache.ws.commons.schema.constants.Constants;

/**
 * The Class TypeTableTest is used to test
 * {@link org.apache.axis2.description.java2wsdl.TypeTable TypeTable} class.
 * 
 * @since 1.7.0 
 * 
 */
public class TypeTableTest extends TestCase {
	
	/** The type table. */
	private TypeTable typeTable;	
	
	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {		
		super.setUp();
		typeTable = new TypeTable();
	}
 
	/**
	 * Test get class name for QName.
	 */
	public void testGetClassNameForQName() {
		assertEquals("Failed to receive expected Class type",
				String.class.getName(),
				typeTable.getClassNameForQName(Constants.XSD_STRING));
		
		assertEquals("Failed to receive expected Class type",
				BigInteger.class.getName(),
				typeTable.getClassNameForQName(Constants.XSD_INTEGER));
		
		assertEquals("Failed to receive expected Class type",
				QName.class.getName(),
				typeTable.getClassNameForQName(Constants.XSD_QNAME));
		
		assertEquals("Failed to receive expected Class type",
				Object.class.getName(),
				typeTable.getClassNameForQName(Constants.XSD_ANY));
		
		assertEquals("Failed to receive expected Class type",
				DataHandler.class.getName(),
				typeTable.getClassNameForQName(Constants.XSD_BASE64));
		
		assertEquals("Failed to receive expected Class type",
				DataHandler.class.getName(),
				typeTable.getClassNameForQName(Constants.XSD_HEXBIN));
		
		assertNull("NULl value expected",
				typeTable.getClassNameForQName(Constants.XSD_LANGUAGE));
	}

}
