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

package org.apache.axis2.description;

import javax.xml.namespace.QName;

import org.apache.axis2.AbstractTestCase;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.description.java2wsdl.XMLSchemaTest;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.wsdl.WSDLConstants;
import org.apache.ws.commons.schema.XmlSchemaElement;

public class AxisMessageTest extends XMLSchemaTest {

    private AxisMessage axisMessage;
    protected AxisService service;

    private XmlSchemaElement element;

    @Override
    public void setUp() throws Exception {
        String filename = AbstractTestCase.basedir
                + "/test-resources/deployment/AxisMessageTestRepo";
        AxisConfiguration er = ConfigurationContextFactory
                .createConfigurationContextFromFileSystem(filename,
                        filename + "/axis2.xml").getAxisConfiguration();

        assertNotNull(er);
        service = er.getService("MessagetestService");
        assertNotNull(service);
        AxisOperation op = service.getOperation(new QName("echoString"));
        assertNotNull(op);
        axisMessage = op.getMessage("In");
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        axisMessage = null;
        super.tearDown();
    }

    public void testGetSchemaElement() throws Exception {
        element = axisMessage.getSchemaElement();
        assertEquals(element.getName(), "echoString");
        assertEquals(element.getQName(), new QName(
                "http://echo.sample.axis2.apache.org", "echoString"));
    }

}
