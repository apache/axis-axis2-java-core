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

package org.apache.axis2.jaxbri;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.transform.stream.StreamSource;

import org.apache.axis2.jaxbri.CodeGenerationUtility;
import org.apache.axis2.wsdl.codegen.CodeGenConfiguration;
import org.apache.axis2.wsdl.databinding.TypeMapper;
import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.junit.Test;

public class CodeGenerationUtilityTest {
    private static List<XmlSchema> loadSampleSchemaFile() throws Exception {
        return Collections.singletonList(new XmlSchemaCollection().read(new StreamSource(
                CodeGenerationUtilityTest.class.getResource("sampleSchema1.xsd").toString())));
    }

    @Test
    public void testProcessSchemas() throws Exception {
        CodeGenConfiguration codeGenConfiguration = new CodeGenConfiguration();
        codeGenConfiguration.setBaseURI("localhost/test");
        codeGenConfiguration.setOutputLocation(new File("target"));
        TypeMapper mapper = CodeGenerationUtility.processSchemas(loadSampleSchemaFile(), null, codeGenConfiguration);
        Map map = mapper.getAllMappedNames();
        String s = map.get(new QName("http://www.w3schools.com", "note")).toString();
        assertEquals("com.w3schools.Note", s);
    }
}
