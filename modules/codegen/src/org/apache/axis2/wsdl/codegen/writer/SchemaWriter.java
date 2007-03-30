package org.apache.axis2.wsdl.codegen.writer;

import org.apache.axis2.util.FileWriter;
import org.apache.ws.commons.schema.XmlSchema;

import java.io.File;
import java.io.FileOutputStream;
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

/** A convenient class to write out the schemas into a file in the output location */
public class SchemaWriter {

    private File baseFolder = null;

    public SchemaWriter(File baseFolder) {
        this.baseFolder = baseFolder;
    }

    public void writeSchema(XmlSchema schema, String schemaFileName) {
        try {
            if (schema != null) {
                //create a output file
                File outputFile = FileWriter.createClassFile(baseFolder,
                                                             null,
                                                             schemaFileName.substring(0,
                                                                                      schemaFileName.lastIndexOf(
                                                                                              ".")),
                                                             ".xsd");
                FileOutputStream fos = new FileOutputStream(outputFile);
                schema.write(fos);
                fos.flush();
                fos.close();
            }
        } catch (Exception e) {
            throw new RuntimeException("Schema writing failed!", e);
        }
    }

}
