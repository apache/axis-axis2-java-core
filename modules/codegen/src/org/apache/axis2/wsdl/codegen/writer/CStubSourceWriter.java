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

package org.apache.axis2.wsdl.codegen.writer;

import org.apache.axis2.util.FileWriter;

import java.io.File;
import java.io.FileOutputStream;
import javax.xml.transform.URIResolver;
import org.apache.axis2.util.XSLTTemplateProcessor;
import org.w3c.dom.Document;

public class CStubSourceWriter extends ClassWriter {

     public CStubSourceWriter(String outputFileLocation) {
        this.outputFileLocation = new File(outputFileLocation);
    }

    public CStubSourceWriter(File outputFileLocation, String language) {
        this.outputFileLocation = outputFileLocation;
        this.language = language;
    }

    public void createOutFile(String packageName, String fileName) throws Exception {
        outputFile = FileWriter.createClassFile(outputFileLocation,
                "",
                fileName,
                getFileExtensionForLanguage(language));
        //set the existing flag
        fileExists = outputFile.exists();
        if (!fileExists) {
            this.stream = new FileOutputStream(outputFile);
        }

    }
    
    /**
     * Writes the output file.
     *
     * @param doc
     * @throws Exception
     */
    public void parse(Document doc, URIResolver resolver) throws Exception {
        if (!fileExists) {
            XSLTTemplateProcessor.parse(this.stream,
                    doc,
                    this.xsltStream,
                    resolver);
            this.stream.write('\n');
            this.stream.write('\n');
            this.stream.flush();
            this.stream.close();
        }
    }
}
