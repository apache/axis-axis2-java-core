package org.apache.axis2.deployment;

import junit.framework.TestCase;
import org.apache.axis2.description.AxisMessage;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.wsdl.WSDLConstants;

import javax.xml.namespace.QName;

import java.io.File;
import java.io.FileInputStream;

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
 */
public class AxisServiceBuilderTest extends TestCase {
	private AxisServiceBuilder builder;

	public AxisServiceBuilderTest() {
		super("AxisServiceBuilderTest");
	}

	protected void setUp() throws Exception {
		super.setUp();
		builder = new AxisServiceBuilder();
	}

	public void test1() throws Exception {

		AxisService service = builder.getAxisService(new FileInputStream(
				"./test-resources/wsdl/echo.wsdl"));

		assertNotNull(service);
		assertEquals("EchoService", service.getName());

		AxisOperation axisOperation = service.getOperation(new QName("Echo"));
		assertNotNull(axisOperation);

		assertEquals(WSDLConstants.MEP_URI_IN_OUT, axisOperation
				.getMessageExchangePattern());

		AxisMessage input = axisOperation
				.getMessage(WSDLConstants.MESSAGE_LABEL_IN_VALUE);
		assertNotNull(input);
		assertEquals(input.getParent(), axisOperation);
		assertEquals(input.getElementQName(), new QName(
				"http://ws.apache.org/axis2/tests", "Echo"));

		AxisMessage output = axisOperation
				.getMessage(WSDLConstants.MESSAGE_LABEL_OUT_VALUE);
		assertNotNull(output);
		assertEquals(output.getParent(), axisOperation);
		assertEquals(output.getElementQName(), new QName(
				"http://ws.apache.org/axis2/tests", "EchoResponse"));
	}
}
