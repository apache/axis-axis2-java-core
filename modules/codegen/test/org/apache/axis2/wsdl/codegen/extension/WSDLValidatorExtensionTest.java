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


package org.apache.axis2.wsdl.codegen.extension;

import org.apache.axis2.description.AxisService;
import org.apache.axis2.util.CommandLineOption;
import org.apache.axis2.wsdl.codegen.CodeGenConfiguration;
import org.apache.axis2.wsdl.codegen.CodeGenerationException;
import org.apache.axis2.wsdl.codegen.XMLSchemaTest;
import org.apache.ws.commons.schema.XmlSchema;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class WSDLValidatorExtensionTest extends XMLSchemaTest {

    @Test
    public void testWSDLValidation() throws Exception {
        XmlSchema schema = loadSingleSchemaFile(SampleSchemasDirectory + "schemaIncludes.xsd");
        if (!schema.getExternals().isEmpty() && schema.getElements().isEmpty()
                && schema.getSchemaTypes().isEmpty() && schema.getGroups().isEmpty()
                && schema.getTargetNamespace() == null) {
            try {
                AxisService service = new AxisService();
                ArrayList<XmlSchema> list = new ArrayList<XmlSchema>();
                Map<String, CommandLineOption> optionMap = new HashMap<String, CommandLineOption>();
                list.add(schema);
                service.addSchema(list);
                CodeGenConfiguration configuration = new CodeGenConfiguration(optionMap);
                configuration.addAxisService(service);
                WSDLValidatorExtension extension = new WSDLValidatorExtension();

                extension.engage(configuration);
                fail("No exception caught!");
            } catch (Exception e) {
                assertEquals(e.getClass(), CodeGenerationException.class);
            }
        }
    }

}
