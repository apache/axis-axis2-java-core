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


package org.apache.axis2.wsdl.codegen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.axis2.description.AxisService;
import org.apache.axis2.wsdl.codegen.XMLSchemaTest;
import org.apache.axis2.util.CommandLineOption;
import org.apache.ws.commons.schema.XmlSchema;
import org.junit.Test;

public class CodeGenConfigurationTest extends XMLSchemaTest{
    
    protected AxisService service;
    private ArrayList<XmlSchema> schemas;
    
    @Override
    protected void setUp() throws Exception {
        service = new AxisService();
        schemas = new ArrayList<XmlSchema>();
        loadSampleSchemaFile(schemas);
        service.addSchema(schemas);
        super.setUp();
    }
    
    @Override
    protected void tearDown() throws Exception {
        service=null;
        schemas=null;
        
        super.tearDown();
    }
    
    @Test
    public void testGetSchemaListForAllServices(){
        Map<String, CommandLineOption> optionMap = new HashMap<String, CommandLineOption>();
        CodeGenConfiguration configuration = new CodeGenConfiguration(optionMap);
        configuration.addAxisService(service);
        List<XmlSchema> list=configuration.getSchemaListForAllServices();
        assertEquals(schemas.get(0), list.get(0));
    }
    
    
}
