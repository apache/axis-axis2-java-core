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

package org.temp;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.axis2.jaxbri.CodeGenerationUtility;
import org.apache.axis2.util.CommandLineOption;
import org.apache.axis2.wsdl.codegen.CodeGenConfiguration;
import org.apache.axis2.wsdl.databinding.TypeMapper;
import org.apache.ws.commons.schema.XmlSchema;
import org.junit.Test;

public class CodeGenerationUtilityTest extends XMLSchemaTest {

    @Test
    public void testProcessSchemas() throws Exception {
        ArrayList<XmlSchema> list = new ArrayList<XmlSchema>();
        Map<String, CommandLineOption> optionMap = new HashMap<String, CommandLineOption>();
        loadSampleSchemaFile(list);
        CodeGenConfiguration codeGenConfiguration = new CodeGenConfiguration(optionMap);
        codeGenConfiguration.setBaseURI("localhost/test");
        codeGenConfiguration.setOutputLocation(new File("target"));
        TypeMapper mapper = CodeGenerationUtility.processSchemas(list, null, codeGenConfiguration);
        Map map = mapper.getAllMappedNames();
        String s = map.get(new QName("http://www.w3schools.com", "note")).toString();
        assertEquals("com.w3schools.Note", s);
    }
}
