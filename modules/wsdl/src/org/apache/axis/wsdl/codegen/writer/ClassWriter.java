package org.apache.axis.wsdl.codegen.writer;

import org.apache.axis.wsdl.codegen.XSLTConstants;
import org.apache.axis.wsdl.util.FileWriter;
import org.apache.axis.wsdl.util.XSLTTemplateProcessor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

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
* Abstract writer to be extended by writers. To use a class writer one needs to call the
* methods in order
* ususally it is setLanguage() ->loadTemplate() ->createOutFile() ->WriteOutFile()
*/
public abstract class ClassWriter {

    protected File outputFileLocation = null;
    protected FileOutputStream stream = null;
    protected InputStream xsltStream = null;
    protected int language = XSLTConstants.LanguageTypes.JAVA; //default is again java

    /**
     * Sets the language
     * @param language
     */
    public void setLanguage(int language) {
        this.language = language;
    }

    /**
     * Load the template
     */
    public abstract void loadTemplate();

    /**
     * Creates the output file
     * @param packageName
     * @param fileName
     * @throws Exception
     */
    public void createOutFile(String packageName,String fileName) throws Exception{
        File outputFile = FileWriter.createClassFile(outputFileLocation,packageName,fileName,language);
        this.stream = new FileOutputStream(outputFile);
    }

    /**
     * Writes the output file
     * @param documentStream
     * @throws Exception
     */
    public void writeOutFile(InputStream documentStream) throws Exception{
        XSLTTemplateProcessor.parse(this.stream,documentStream,this.xsltStream);
        this.stream.flush();
        this.stream.close();


    }


}
