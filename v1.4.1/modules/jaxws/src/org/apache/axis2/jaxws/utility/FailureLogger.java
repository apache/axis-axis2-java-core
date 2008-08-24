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

package org.apache.axis2.jaxws.utility;

import org.apache.axis2.jaxws.i18n.Messages;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Hashtable;

/**
 * Logs an error to a specified limit.
 */
public class FailureLogger {
    private static final Log log = LogFactory.getLog(FailureLogger.class);
    private static Integer MAX = 100; // Maximum number of logs per key
    
    private static Hashtable<String, Integer> errorCount = new Hashtable<String, Integer>();
    
    /**
     * Logs an error up to a MAX limit.
     * @param t
     * @param logFully (if true then the exception is logged, otherwise only the class and stack is logged)
     */
    public static void logError(Throwable t, boolean logFully) {
        
        // Construct a key that will be used to limit the logging.
        String name = t.getClass().getName();
        String stack = stackToString(t);
        String key = name + "   " + stack;
        
        // Get the error count for this message
        Integer count = errorCount.get(key);
        if (count == null) {
            count = 0;
        }
        count ++;
        if (count <= MAX) {
            errorCount.put(key, count);
            String text = null;
            if (logFully) {
                text = Messages.getMessage("failureLogger", name, t.toString());
               
            } else {
                text = Messages.getMessage("failureLogger", name, stack);
            }
            log.error(t);
        }
        
    }
    
    /**
     * Get a string containing the stack of the specified exception
     *
     * @param e
     * @return
     */
    private static String stackToString(Throwable e) {
        java.io.StringWriter sw = new java.io.StringWriter();
        java.io.BufferedWriter bw = new java.io.BufferedWriter(sw);
        java.io.PrintWriter pw = new java.io.PrintWriter(bw);
        e.printStackTrace(pw);
        pw.close();
        String text = sw.getBuffer().toString();
        // Jump past the throwable
        text = text.substring(text.indexOf("at"));
        return text;
    }
}
