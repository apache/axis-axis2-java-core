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

package org.apache.axis2.jaxws.util;

import org.apache.axis2.java.security.AccessController;
import org.apache.axis2.jaxws.ExceptionFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import java.io.IOException;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Properties;

public class WSToolingUtils {

    private static final Log log = LogFactory.getLog(WSToolingUtils.class);

    /**
     * A handy function to check for empty or null string
     * 
     * @param str
     * @return boolean
     * 
     */
    public static boolean hasValue(String str) {
        return ((str != null) && (str.length() > 0));
    }

    /**
     * Retrieves the major version number of the WsGen class that we're using
     * 
     * @return String
     * 
     */
    public static String getWsGenVersion() throws ClassNotFoundException, IOException {

        Class clazz = null;
        try {

            clazz = forName("com.sun.tools.ws.WsGen", false,
                getContextClassLoader(null));

        } catch (ClassNotFoundException e1) {

            try {

                clazz = forName("com.sun.tools.internal.ws.WsGen", false,
                    getContextClassLoader(null));

            } catch (ClassNotFoundException e2) {
                if (log.isDebugEnabled()) {
                    log.debug("Exception thrown from getWsGenVersion: " + e2.getMessage(), e2);
                }
                throw (ClassNotFoundException) e2.getException();
            }
        }

        Properties p = new Properties();

        try {

            p.load(clazz.getResourceAsStream("version.properties"));

        } catch (IOException ioex) {
            if (log.isDebugEnabled()) {
                log.debug("Exception thrown from getWsGenVersion: " + ioex.getMessage(), ioex);

            }
            throw (IOException) ioex.getCause();
        }

        return (p.getProperty("major-version"));
    }

    /**
     * @return ClassLoader
     */
    private static ClassLoader getContextClassLoader(final ClassLoader classLoader) {
        ClassLoader cl;
        try {
            cl = (ClassLoader) AccessController.doPrivileged(
                new PrivilegedExceptionAction() {
                    public Object run() throws ClassNotFoundException {
                        return classLoader != null ? classLoader : Thread.currentThread().getContextClassLoader();
                    }
                }
            );
        } catch (PrivilegedActionException e) {
            if (log.isDebugEnabled()) {
                log.debug("Exception thrown from AccessController: " + e.getMessage(), e);
            }
            throw ExceptionFactory.makeWebServiceException(e.getException());
        }

        return cl;
    }

    /**
     * Return the class for this name
     *
     * @return Class
     */
    private static Class forName(final String className, final boolean initialize,
        final ClassLoader classloader) throws ClassNotFoundException {
        Class cl = null;
        try {
            cl = (Class) AccessController.doPrivileged(
                new PrivilegedExceptionAction() {
                    public Object run() throws ClassNotFoundException {
                        return Class.forName(className, initialize, classloader);
                    }
                }
            );
        } catch (PrivilegedActionException e) {
            if (log.isDebugEnabled()) {
                log.debug("Exception thrown from AccessController: " + e.getMessage(), e);
            }
            throw (ClassNotFoundException) e.getException();
        }

        return cl;
    }

    public static boolean isValidVersion(String wsGenVersion) {

        if (log.isDebugEnabled()) {
            log.debug("isValidVersion: Determining if WsGen version: " +wsGenVersion
                +" is appropriate version for using new SUN RI behavior");
        }

        // Beginning of reuseable code
        final int VERSION_FIELD_1 = 2;
        final int VERSION_FIELD_2 = 1;
        final int VERSION_FIELD_3 = 6;

        String version = wsGenVersion.trim();

        //Algorithm assumption 1: We are assuming that the version string will always be 
        // of the form "x.x.x" where x is a character (or series of characters) with each digit
        // having a converted integer value of 0 - 9. 
        //Assumption 2: We are assuming thatWsgen version 2.1.6 is the starting version 
        // for being able to use the new Sun behavior

        int dotIndex = version.indexOf(".");        
        int subField = Integer.valueOf(version.substring(0, dotIndex));

        if (subField < VERSION_FIELD_1) {
            return false;
        } else if (subField > VERSION_FIELD_1) {
            //If we are greater than 2.x.x (i.e. 3.x.x) then version is valid
            return true;
        }

        String subString2 = version.substring(dotIndex + 1);
        dotIndex = subString2.indexOf(".");
        subField = Integer.valueOf(subString2.substring(0, dotIndex));

        if (subField < VERSION_FIELD_2) {
            return false;
        } else if (subField > VERSION_FIELD_2) {
            //If we are greater than 2.1.x (i.e. 2.2.x) then version is valid
            return true;
        }

        //Final substring, will probably hit end of string. But, check to make sure there is not
        // another "." (i.e. We could have "2.1.6.1")yes.
        String subString3 = subString2.substring(dotIndex + 1);

        //Does string contain another dot? if so, just read up to that dot. Otherwise, assume that 
        //this is the last sub-string
        dotIndex = subString3.indexOf(".");
        if (dotIndex == -1) {
            subField = Integer.valueOf(subString3);
        } else {
            subField = Integer.valueOf(subString3.substring(0, dotIndex));
        }

        if (subField < VERSION_FIELD_3) {
            return false;
        }

        return true;
    }
}
