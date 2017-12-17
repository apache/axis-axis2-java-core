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

package org.apache.axis2.dataretrieval;

import java.io.File;

import javax.xml.namespace.QName;

import org.apache.axis2.AbstractTestCase;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisMessage;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.java2wsdl.XMLSchemaTest;
import org.apache.axis2.engine.AxisConfiguration;

public class SchemaDataLocatorTest extends XMLSchemaTest {

    private AxisService service;
    private AxisMessage axisMessage;
    private SchemaDataLocator schemaDataLocator;

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
        schemaDataLocator = new SchemaDataLocator();
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        axisMessage = null;
        super.tearDown();
    }

    // Tests the Apache XMLSchema usage using a dummy MessageContext object
    public void testOutputInlineForm() throws Exception {
        MessageContext mc = new MessageContext();
        mc.setAxisService(service);
        ;
        mc.setAxisMessage(axisMessage);

        Data[] data = schemaDataLocator
                .outputInlineForm(mc, new ServiceData[0]);
        String s = data[0].getData().toString();
        String expected = readFile("test-resources" + File.separator
                + "SchemaDataLocatorTest.xml");
        assertNotNull(data);
        assertEquals(data.length, 1);
        assertSimilarXML(expected, s);

    }

}
