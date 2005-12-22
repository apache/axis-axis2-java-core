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

import org.apache.axis2.util.Loader;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * tidies up the java source code using Jalopy
 */
public class PrettyPrinter {
    private static Log log = LogFactory.getLog(PrettyPrinter.class);


    /**
     * Pretty print contents of the java source file
     *
     * @param file
     */
    public static void prettify(File file) {
        try {
            // Create an instance of the Jalopy bean
            Class clazz = Loader.loadClass("de.hunsicker.jalopy.Jalopy");
            Object prettifier = clazz.newInstance();

            // Set the input file
            Method input = clazz.getMethod("setInput", new Class[]{File.class});
            input.invoke(prettifier, new Object[]{file});

            // Set the output file
            Method output = clazz.getMethod("setOutput", new Class[]{File.class});
            output.invoke(prettifier, new Object[]{file});

            Class clazz2 = Loader.loadClass("de.hunsicker.jalopy.storage.Convention");
            Method instance = clazz2.getMethod("getInstance", new Class[]{});
            Object settings = instance.invoke(null, new Object[]{});

            Class clazz3 = Loader.loadClass("de.hunsicker.jalopy.storage.ConventionKeys");
            Field field = clazz3.getField("COMMENT_JAVADOC_PARSE");
            Object key = field.get(null);
            
            Method put = clazz2.getMethod("put", new Class[]{ key.getClass(), String.class});
            put.invoke(settings, new Object[]{key, "true"});

            // format and overwrite the given input file
            Method format = clazz.getMethod("format", new Class[]{});
            format.invoke(prettifier, new Object[]{});
            log.info("Pretty print file : " + file);
        } catch (ClassNotFoundException e) {
            log.info("Jalopy not found - unable to pretty print " + file);
        } catch (Exception e) {
            log.info(e);
        }
    }
}
