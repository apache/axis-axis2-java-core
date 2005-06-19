package org.apache.axis.wsdl.codegen.writer;

import java.io.File;
import java.io.FileOutputStream;

import org.apache.axis.wsdl.codegen.XSLTConstants;
import org.apache.axis.wsdl.util.FileWriter;

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
*
*
*/
public class ServiceXMLWriter extends ClassWriter{


    public ServiceXMLWriter(String outputFileLocation) {
        this.outputFileLocation = new File(outputFileLocation);
    }

    public ServiceXMLWriter(File outputFileLocation,int language) {
        this.outputFileLocation = outputFileLocation;
        this.language = language;
    }

    /**
     * @see org.apache.axis.wsdl.codegen.writer.ClassWriter#loadTemplate()
     */
    public void loadTemplate(){
        Class clazz = this.getClass();
        this.xsltStream = clazz.getResourceAsStream(XSLTConstants.XSLTServiceXMLTemplates.GENERAL_SERVICE_TEMPLATE);
    }

    public void createOutFile(String packageName, String fileName) throws Exception {
        File outputFile = FileWriter.createClassFile(outputFileLocation,packageName,"service",".xml");
        this.stream = new FileOutputStream(outputFile);
    }
}

