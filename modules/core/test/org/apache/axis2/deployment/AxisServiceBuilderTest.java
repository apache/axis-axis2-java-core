package org.apache.axis2.deployment;

import java.io.FileInputStream;

import javax.xml.namespace.QName;

import org.apache.axis2.description.AxisMessage;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.wsdl.WSDLConstants;

import junit.framework.TestCase;

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

/**
 * TestCase for AxisServiceBuilder.
 * 
 * @author Thilini Gunawardhana (thilini@wso2.com)
 */
public class AxisServiceBuilderTest extends TestCase {
	private AxisServiceBuilder builder;

	public AxisServiceBuilderTest() {
		super("AxisServiceBuilderTest");
	}

	protected void setUp() throws Exception {
		builder = new AxisServiceBuilder();
		super.setUp();
	}

	public void testAxisServiceBuilder() throws Exception {

		assertNotNull(builder);

		AxisService service = builder.getAxisService(new FileInputStream(
				"./test-resources/PingService.wsdl"));

		assertNotNull(service);
		assertEquals(service.getName(), "PingService");
		AxisOperation operation = service.getOperation(new QName("Ping"));
		assertNotNull(operation);
		AxisMessage inMessage = operation
				.getMessage(WSDLConstants.MESSAGE_LABEL_IN_VALUE);
		assertNotNull(inMessage);
		assertEquals("PingRequest", inMessage.getElementQName().getLocalPart());
		AxisMessage outMessage = operation
				.getMessage(WSDLConstants.MESSAGE_LABEL_OUT_VALUE);
		assertNotNull(outMessage);
		assertEquals("PingResponse", outMessage.getElementQName()
				.getLocalPart());
	}
}