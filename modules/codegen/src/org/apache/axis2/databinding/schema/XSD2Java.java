package org.apache.axis2.databinding.schema;

import org.w3c.dom.Document;
import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.apache.ws.commons.schema.XmlSchema;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
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

public class XSD2Java {

    /**
     * for now the arguments this main method accepts is the source schema and the output
     * location
     * @param args
     */
    public static void main(String[] args) {
        if (args.length!=2){
            // printout the options
            System.out.println("Argument1 - Source schema file name");
            System.out.println("Argument2 - Output folder name");
        } else{
            compile(args[0],args[1]);
        }

    }

    /**
     *
     * @param xsdName
     * @param outputLocation
     */
    private static void compile(String xsdName,String outputLocation) {
        try {
            //load the current Schema through a file
            //first read the file into a DOM
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            documentBuilderFactory.setNamespaceAware(true);

            DocumentBuilder builder =  documentBuilderFactory.newDocumentBuilder();
            Document doc = builder.parse(new File(xsdName));

            //now read it to a schema
            XmlSchemaCollection schemaCol =  new XmlSchemaCollection();
            XmlSchema currentSchema = schemaCol.read(doc,null);

            File outputFolder = new File(outputLocation);
            if (outputFolder.exists()){
                if (outputFolder.isFile())  {
                    throw new IOException("specified location is not a folder!");
                }
            }else{
                outputFolder.mkdirs();
            }

            SchemaCompiler compiler = new SchemaCompiler(new CompilerOptions().setOutputLocation(
                    outputFolder));
            compiler.compile(currentSchema);

        } catch (Exception e) {
           throw new RuntimeException("Compiler caused an exception",e);
        }
    }
}
