/*
 * Copyright 2001-2004 The Apache Software Foundation.
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

package org.apache.axis.wsdl.codegen.writer;

import java.io.File;

import org.apache.axis.wsdl.codegen.XSLTConstants;

/**
 * @author chathura@opensource.lk
 *
 */
public class TestSkeltonImplWriter extends ClassWriter {

	  public TestSkeltonImplWriter(String outputFileLocation) {
        this.outputFileLocation = new File(outputFileLocation);
    }

    public TestSkeltonImplWriter(File outputFileLocation,int language) {
        this.outputFileLocation = outputFileLocation;
        this.language = language;
    }

    public void loadTemplate() {
        Class clazz = this.getClass();
        switch (language){
            case XSLTConstants.LanguageTypes.JAVA:
                this.xsltStream = clazz.getResourceAsStream(XSLTConstants.XSLTTestSkeltonImplTemplates.JAVA_TEMPLATE);
                break;
            default:
                throw new UnsupportedOperationException();
        }
    }

}
