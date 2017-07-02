/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.axis2.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.google.googlejavaformat.java.Formatter;

import java.io.File;

/**
 * Tidies up the java source code.
 */
public class PrettyPrinter {
    private static final Log log = LogFactory.getLog(PrettyPrinter.class);


    /**
     * Pretty prints contents of the java source file.
     *
     * @param file
     */
    public static void prettify(File file) {
        File formattedFile = new File(file.getParentFile(), file.getName() + ".new");
        try {
            new Formatter().formatSource(
                    Files.asCharSource(file, Charsets.UTF_8),
                    Files.asCharSink(formattedFile, Charsets.UTF_8));
        } catch (Exception e) {
            log.warn("Exception occurred while trying to pretty print file " + file, e);
        }
        file.delete();
        formattedFile.renameTo(file);
    }
}
