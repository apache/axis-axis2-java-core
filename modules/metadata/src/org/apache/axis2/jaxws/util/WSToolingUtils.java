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
import java.util.StringTokenizer;

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
                throw (ClassNotFoundException) e2;
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
        if(log.isDebugEnabled()){
            log.debug("Start isValidVersion(String)");
        }

        if (log.isDebugEnabled()) {
            log.debug("isValidVersion: Determining if WsGen version: " +wsGenVersion
                +" is appropriate version for using new SUN RI behavior");
        }
        if(wsGenVersion == null){
            return false;
        }
        /*
         * This algorithm is improvement over the old algorithm we had to validate the
         * version. In this algorithm we don't assume that the format will be x.x.x.
         * This algorithm looks for versionNumbers in a String token delimited by a
         * ".", the idea is to look for the first digit in each token and compare that
         * with the version validation requirements.
         * we return false if version is less that 2.1.6.
         * possible input version strings could be "JAX-WS RI 2.2-b05-", "2.1.6" "2.1.0" etc.
         */
        // Beginning of reuseable code
        final int VERSION_FIELD_1 = 2;
        final int VERSION_FIELD_2 = 1;
        final int VERSION_FIELD_3 = 6;

        String version = wsGenVersion.trim();
        
        StringTokenizer st = new StringTokenizer(version, ".");
        if(st.countTokens()<=0){
            if(log.isDebugEnabled()){
                log.debug("No Tokens to validate the tooling version, Input version String is invalid.");
            }
            return false;
        }
        for(int tokenCnt = 1;st.hasMoreTokens();tokenCnt++){
            String token = st.nextToken();
            if(token == null){
                return false;
            }
            int versionNumber = getDigit(token);
            if (tokenCnt==1 && versionNumber < VERSION_FIELD_1) {
                if(log.isDebugEnabled()){
                    log.debug("Validation failed of tokenCnt="+tokenCnt);
                    log.debug("Input VersionNumber ="+versionNumber);
                }
                return false;
            } 
            if(tokenCnt == 2 && versionNumber < VERSION_FIELD_2 ){  
                if(log.isDebugEnabled()){
                    log.debug("Validation failed of tokenCnt="+tokenCnt);
                    log.debug("Input VersionNumber ="+versionNumber);
                }
                return false;
            } 
            if (tokenCnt==3 && versionNumber < VERSION_FIELD_3) {
                if(log.isDebugEnabled()){
                    log.debug("Validation failed of tokenCnt="+tokenCnt);
                    log.debug("Input VersionNumber ="+versionNumber);
                }
                return false;
            } 
        }
        if(log.isDebugEnabled()){
            log.debug("Exit isValidVersion(String)");
        }

        return true;
    }
    /**
     * look for first digit in the version token.
     * @param s - version token.
     * @return a digit or -1 if not digit found in the token.
     */
    private static int getDigit(String s){
        for(int i=0;i<s.length();i++){
            char ch = s.charAt(i);
            if(Character.isDigit(ch)){
                return Character.getNumericValue(ch);
            }
        }
        return -1;
    }
}
