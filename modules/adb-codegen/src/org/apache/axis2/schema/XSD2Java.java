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

package org.apache.axis2.schema;

import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.IOException;

import static org.apache.axis2.schema.i18n.SchemaCompilerMessages.getMessage;

public class XSD2Java {

    /**
     * for now the arguments this main method accepts is the source schema and the output
     * location
     *
     * @param args
     */
    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.out.println(getMessage("schema.usage"));
            System.exit(1);
        } else {
            File outputFolder = new File(args[args.length-1]);
            for (int i=0; i<args.length-1; i++) {
                File xsdFile = new File(args[i]);
                // Only output a message if the user has specified more than one schema file
                if (args.length > 2) {
                    System.out.println(getMessage("schema.compiling", xsdFile.getName()));
                }
                compile(xsdFile, outputFolder);
            }
        }
    }

    /**
     * @param xsdName
     * @param outputLocation
     */
    private static void compile(File xsdFile, File outputFolder) throws Exception {
            //load the current Schema through a file
            //first read the file into a DOM
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            documentBuilderFactory.setNamespaceAware(true);

            DocumentBuilder builder = documentBuilderFactory.newDocumentBuilder();
            Document doc = builder.parse(xsdFile);

            //now read it to a schema
            XmlSchemaCollection schemaCol = new XmlSchemaCollection();
            XmlSchema currentSchema = schemaCol.read(doc, xsdFile.toURI().toString(), null);

            if (outputFolder.exists()) {
                if (outputFolder.isFile()) {
                    throw new IOException(getMessage("schema.locationNotFolder"));
                }
            } else {
                outputFolder.mkdirs();
            }

            CompilerOptions compilerOptions = new CompilerOptions();
            compilerOptions.setOutputLocation(outputFolder);
            compilerOptions.setGenerateAll(true);

//            Map namespace2PackageMap = new HashMap();
//            namespace2PackageMap.put("http://www.w3.org/2001/XMLSchema/schema",
//                    "org.apache.axis2.databinding.types.xsd");
//            compilerOptions.setNs2PackageMap(namespace2PackageMap);
//            compilerOptions.setMapperClassPackage("org.apache.axis2.databinding.types.xsd");

            //todo - this should come from the users preferences
             compilerOptions.setWrapClasses(false);

            //there's no point in not writing the classes here.
             compilerOptions.setWriteOutput(true);

//             compilerOptions.setUseWrapperClasses(true);

            SchemaCompiler compiler = new SchemaCompiler(compilerOptions);
            compiler.compile(currentSchema);
    }
}
