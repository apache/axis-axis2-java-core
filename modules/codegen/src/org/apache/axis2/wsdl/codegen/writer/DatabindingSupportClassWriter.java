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

import org.apache.axis2.wsdl.util.ConfigPropertyFileLoader;

import java.io.File;
import java.util.Iterator;
import java.util.Map;

public class DatabindingSupportClassWriter extends ClassWriter {

    private String databindingFramework = ConfigPropertyFileLoader.getDefaultDBFrameworkName();

    public DatabindingSupportClassWriter(String outputFileLocation) {
        this.outputFileLocation = new File(outputFileLocation);
    }

    public DatabindingSupportClassWriter(File outputFileLocation,
                                         String language,
                                         String databindingFramework) {
        this.outputFileLocation = outputFileLocation;
        this.language = language;
        this.databindingFramework = databindingFramework;
    }

    public void setDatabindingFramework(String databindingFramework) {
        this.databindingFramework = databindingFramework;
    }

    //overridden to get the correct behavior
    protected String findTemplate(Map languageSpecificPropertyMap) {
        String ownClazzName =  this.getClass().getName();
        String key;
        String propertyValue;
        String templateName = null;
        Iterator keys = languageSpecificPropertyMap.keySet().iterator();
        String databindString;


        while (keys.hasNext()) {
            //check for template entries
            key = keys.next().toString();
            if (key.endsWith(TEMPLATE_SUFFIX)){
                // check if the class name is there
                propertyValue = languageSpecificPropertyMap.get(key).toString();
                if (propertyValue.startsWith(ownClazzName)){
                    if (key.indexOf(databindingFramework)!=-1){
                        templateName = propertyValue.substring(propertyValue.indexOf(SEPERATOR_STRING)+1) ;
                        break;
                    }
                }
            }

        }

        return templateName;

    }
}
