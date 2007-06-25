/*
 * Copyright 2004,2005 The Apache Software Foundation.
 * Copyright 2006 International Business Machines Corp.
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

package org.apache.axis2.jaxws.util;

import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

import org.apache.axis2.java.security.AccessController;
import org.apache.axis2.jaxws.ExceptionFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ClassLoaderUtils {

    private static final Log log = LogFactory.getLog(ClassLoaderUtils.class);
    
    private ClassLoaderUtils() {        
    }
    
    /** @return ClassLoader */
    public static ClassLoader getContextClassLoader() {
        ClassLoader cl = null;
        try {
            cl = (ClassLoader)AccessController.doPrivileged(
                    new PrivilegedExceptionAction() {
                        public Object run() throws ClassNotFoundException {
                            return Thread.currentThread().getContextClassLoader();
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
    public static Class forName(final String className, final boolean initialize,
                                final ClassLoader classloader) throws ClassNotFoundException {
        Class cl = null;
        try {
            cl = (Class)AccessController.doPrivileged(
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
            throw (ClassNotFoundException)e.getException();
        }

        return cl;
    }

    /**
     * Return the class for this name
     *
     * @return Class
     */
    public static Class forName(final String className) throws ClassNotFoundException {
        Class cl = null;
        try {
            cl = (Class)AccessController.doPrivileged(
                    new PrivilegedExceptionAction() {
                        public Object run() throws ClassNotFoundException {
                            return Class.forName(className);
                        }
                    }
            );
        } catch (PrivilegedActionException e) {
            if (log.isDebugEnabled()) {
                log.debug("Exception thrown from AccessController: " + e.getMessage(), e);
            }
            throw (ClassNotFoundException)e.getException();
        }

        return cl;
    }
}
