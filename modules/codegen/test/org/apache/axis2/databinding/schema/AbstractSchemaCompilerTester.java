package org.apache.axis2.databinding.schema;

import junit.framework.TestCase;
import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
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

/* Maven is a little dumb when it comes to executing test cases. It tries to run everything
  that ends with 'test' even if the class is abstract or non junit!!!!!
  Rather than putting an explicit exclude, this is a cheaper way out
*/
public abstract class AbstractSchemaCompilerTester extends TestCase {

    //this should be an xsd name in the test-resource directory
    protected String fileName = "";
    protected  XmlSchema currentSchema;

    protected void setUp() throws Exception {
        //load the current Schema through a file
        //first read the file into a DOM
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);

        DocumentBuilder builder =  documentBuilderFactory.newDocumentBuilder();
        Document doc = builder.parse(new File(fileName));

        //now read it to a schema
        XmlSchemaCollection schemaCol =  new XmlSchemaCollection();
        currentSchema = schemaCol.read(doc,null);
    }


    public void testSchema() throws Exception{
        SchemaCompiler compiler = new SchemaCompiler(null);
        compiler.compile(currentSchema);
    }


}
