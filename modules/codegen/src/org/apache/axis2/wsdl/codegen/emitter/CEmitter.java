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

package org.apache.axis2.wsdl.codegen.emitter;

import org.apache.axis2.wsdl.codegen.emitter.AxisServiceBasedMultiLanguageEmitter;
import org.apache.axis2.wsdl.codegen.CodeGenerationException;
import org.apache.axis2.wsdl.codegen.writer.CHeaderWriter;
import org.apache.axis2.wsdl.codegen.writer.CSourceWriter;
import org.w3c.dom.Document;

public class CEmitter extends AxisServiceBasedMultiLanguageEmitter {

    public void emitStub() throws CodeGenerationException {

        try {
            // write interface implementations
            writeCSource();

            // write the test classes
            writeCHeader();


        } catch (Exception e) {
            //log the error here
        }
    }


    /**
     * Writes the header.
     *
     * @throws Exception
     */
    protected void writeCHeader() throws Exception {

        // first check for the policies in this service and write them
        Document interfaceImplModel = createDOMDocumentForInterfaceImplementation();

        CHeaderWriter writerH =
                new CHeaderWriter(
                        codeGenConfiguration.isFlattenFiles()?
                                getOutputDirectory(codeGenConfiguration.getOutputLocation(), null):
                                getOutputDirectory(codeGenConfiguration.getOutputLocation(), SRC_DIR_NAME),
                        codeGenConfiguration.getOutputLanguage());

        writeClass(interfaceImplModel, writerH);
    }

    /**
     * Writes the source.
     *
     * @throws Exception
     */
    protected void writeCSource() throws Exception {

        // first check for the policies in this service and write them
        Document interfaceImplModel = createDOMDocumentForInterfaceImplementation();

        CSourceWriter writerC =
                new CSourceWriter(codeGenConfiguration.isFlattenFiles()?
                        getOutputDirectory(codeGenConfiguration.getOutputLocation(), null):
                        getOutputDirectory(codeGenConfiguration.getOutputLocation(), SRC_DIR_NAME),
                        codeGenConfiguration.getOutputLanguage());

        writeClass(interfaceImplModel, writerC);
    }
}
