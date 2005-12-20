package org.apache.axis2.schema;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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

/**
 * This is a bean class that captures all the compiler options.
 * Right now the compiler options consist of the following
 * 1. output file location - A folder with necessary rights for the
 * schema compiler to write the files
 */
public class CompilerOptions {

    /**
     * Generated output file
     */
    File outputLocation;
    String packageName=null;

    public String getPackageName() {
        return packageName;
    }

    public CompilerOptions setPackageName(String packageName) {
        //validate the package name
        //should be ***.***.***. type value
        if (packageName!=null && testValue(packageName)){
           this.packageName = packageName;
        }else{
            throw new RuntimeException("Unsupported value!");
        }

        return this;
    }

    public File getOutputLocation() {
        return outputLocation;
    }

    public CompilerOptions setOutputLocation(File outputLocation) {
        this.outputLocation = outputLocation;
        return this;
    }

    private boolean testValue(String wordToMatch){
         Pattern pat = Pattern.compile("^(\\w+\\.)+$");
         Matcher m= pat.matcher(wordToMatch);
         return m.matches();
    }
}
