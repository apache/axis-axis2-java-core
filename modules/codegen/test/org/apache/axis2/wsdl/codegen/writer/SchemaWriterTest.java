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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class SchemaWriterTest extends XMLSchemaTest{
    @TempDir
    File tmpFolder;

    @Test
    public void testWriteSchema() throws Exception{
        SchemaWriter writer = new SchemaWriter(tmpFolder);
        XmlSchema schema=loadSingleSchemaFile(1);
        writer.writeSchema(schema, "generated.xsd");
        String s1=readXMLfromSchemaFile(new File(tmpFolder, "generated.xsd").getPath());
        String s2=readXMLfromSchemaFile(customDirectoryLocation+"sampleSchema1.xsd");
        assertSimilarXML(s1, s2);
        
    }
}
