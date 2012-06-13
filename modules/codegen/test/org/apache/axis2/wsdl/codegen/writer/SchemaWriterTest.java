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

package org.apache.axis2.wsdl.codegen.writer;

import java.io.File;

import org.apache.axis2.wsdl.codegen.XMLSchemaTest;
import org.apache.ws.commons.schema.XmlSchema;
import org.junit.Test;

public class SchemaWriterTest extends XMLSchemaTest{
    private SchemaWriter writer;
    

    @Override
    protected void setUp() throws Exception {
        writer=new SchemaWriter(new File(customDirectoryLocation));
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        writer=null;
        super.tearDown();
    }

    @Test
    public void testWriteSchema() throws Exception{
        XmlSchema schema=loadSingleSchemaFile(1);
        writer.writeSchema(schema, "generated.xsd");
        String s1=readXMLfromSchemaFile(customDirectoryLocation+"generated.xsd");
        String s2=readXMLfromSchemaFile(customDirectoryLocation+"sampleSchema1.xsd");
        assertSimilarXML(s1, s2);
        
    }
}
