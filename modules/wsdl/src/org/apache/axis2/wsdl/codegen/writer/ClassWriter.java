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

import org.apache.axis2.wsdl.codegen.CodeGenerationException;
import org.apache.axis2.wsdl.util.ConfigPropertyFileLoader;
import org.apache.axis2.wsdl.util.FileWriter;
import org.apache.axis2.wsdl.util.XSLTTemplateProcessor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Map;

public abstract class ClassWriter {

    protected File outputFileLocation = null;
    protected FileOutputStream stream = null;
    protected InputStream xsltStream = null;
    protected String language = ConfigPropertyFileLoader.getDefaultLanguage(); //default would java

    protected static final String TEMPLATE_SUFFIX = ".template";
    protected static final String EXTENSION_SUFFIX = ".extension";
    protected static final String SEPERATOR_STRING = ",";

    /**
     * Sets the language
     *
     * @param language
     */
    public void setLanguage(String language) {
        this.language = language;
    }

    /**
     * Load the template
     */
    public void loadTemplate() throws CodeGenerationException{
        // the default behavior for the class writers is to use the property map from the languge specific types
        // The properties are arranged in the following order
        // <lang-name>.* .template=<write-class>,<template-name>

        //first get the language specific property map
        Class clazz = this.getClass();
        Map languageSpecificPropertyMap = (Map)ConfigPropertyFileLoader.getLanguageSpecificPropertiesMap().get(this.language);
        if (languageSpecificPropertyMap==null){
            throw new CodeGenerationException("No language specific properties!!!");
        }

        String templateName = findTemplate(languageSpecificPropertyMap);
        if (templateName!=null){
            this.xsltStream = clazz.getResourceAsStream(templateName);
        }else{
            throw new CodeGenerationException("template for this writer is not found");
        }


    }

    protected String findTemplate(Map languageSpecificPropertyMap) {
        //search through the proprty names to find the template relevant to this class

        String ownClazzName =  this.getClass().getName();
        String key;
        String propertyValue;
        String templateName = null;
        Iterator keys = languageSpecificPropertyMap.keySet().iterator();

        while (keys.hasNext()) {
            //check for template entries
            key = keys.next().toString();
            if (key.endsWith(TEMPLATE_SUFFIX)){
                // check if the class name is there
                propertyValue = languageSpecificPropertyMap.get(key).toString();
                if (propertyValue.startsWith(ownClazzName)){
                    //bingo! we found the right template
                    templateName = propertyValue.substring(propertyValue.indexOf(SEPERATOR_STRING)+1) ;
                    break;
                }
            }

        }
        return templateName;
    }

    /**
     * Creates the output file
     *
     * @param packageName
     * @param fileName
     * @throws Exception
     */
    public void createOutFile(String packageName, String fileName) throws Exception {
        File outputFile = FileWriter.createClassFile(outputFileLocation,
                packageName,
                fileName,
                getFileExtensionForLanguage(language));
        this.stream = new FileOutputStream(outputFile);
    }

    /**
     * Find the file name extension
     * @param language
     * @return
     */
    protected String getFileExtensionForLanguage(String language){
        Map languageSpecificPropertyMap = (Map)ConfigPropertyFileLoader.getLanguageSpecificPropertiesMap().get(this.language);
        Iterator keys = languageSpecificPropertyMap.keySet().iterator();
        String key;
        String extension = null;
        while (keys.hasNext()) {
            //check for template entries
            key = keys.next().toString();
            if (key.endsWith(EXTENSION_SUFFIX)){
                extension = languageSpecificPropertyMap.get(key).toString();
                //add a . to the front
                extension = "." + extension;
            }
        }

        return extension;
    }

    /**
     * Writes the output file
     *
     * @param documentStream
     * @throws Exception
     */
    public void writeOutFile(InputStream documentStream) throws Exception {
        XSLTTemplateProcessor.parse(this.stream,
                documentStream,
                this.xsltStream);
        this.stream.flush();
        this.stream.close();


    }


}
