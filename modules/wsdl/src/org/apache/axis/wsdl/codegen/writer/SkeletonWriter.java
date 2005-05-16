package org.apache.axis.wsdl.codegen.writer;

import org.apache.axis.wsdl.codegen.XSLTConstants;

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
 *
 * 
 */
public class SkeletonWriter extends ClassWriter{


    public SkeletonWriter(String outputFileLocation) {
        this.outputFileLocation = new File(outputFileLocation);
    }

    public SkeletonWriter(File outputFileLocation,int language) {
        this.outputFileLocation = outputFileLocation;
        this.language = language;
    }

    /**
     * @see org.apache.axis.wsdl.codegen.writer.ClassWriter#loadTemplate()
     */
     public void loadTemplate(){
        Class clazz = this.getClass();
        switch (language){
            case XSLTConstants.LanguageTypes.JAVA:
                this.xsltStream = clazz.getResourceAsStream(XSLTConstants.XSLTSkeletonTemplates.JAVA_TEMPLATE);
                break;
            case XSLTConstants.LanguageTypes.C_SHARP:
                this.xsltStream = clazz.getResourceAsStream(XSLTConstants.XSLTSkeletonTemplates.CSHARP_TEMPLATE);
                break;
            case XSLTConstants.LanguageTypes.C_PLUS_PLUS:
            case XSLTConstants.LanguageTypes.VB_DOT_NET:
            default:
                throw new UnsupportedOperationException();
        }

    }

}
