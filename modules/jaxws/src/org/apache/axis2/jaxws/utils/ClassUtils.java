/*
 * Copyright 2001-2004 The Apache Software Foundation.
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
package org.apache.axis2.jaxws.utils;

import java.io.InputStream;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.security.PrivilegedAction;

/**
 * Utility methods for Class Loading.
 *
 * @author Davanum Srinvas (dims@yahoo.com)
 * @author Matthew Pocock (matthew_pocock@yahoo.co.uk)
 */
public final class ClassUtils {
    /** default class loader */
    private static ClassLoader defaultClassLoader
            = ClassUtils.class.getClassLoader();

    /** hash table of class loaders */
    private static java.util.Hashtable classloaders = new java.util.Hashtable();

    /**
     * Set the default ClassLoader. If loader is null, the default loader is
     * not changed.
     *
     * @param loader  the new default ClassLoader
     */
    public static void setDefaultClassLoader(ClassLoader loader) {
      if (loader != null)
          defaultClassLoader = loader;
    }

    public static ClassLoader getDefaultClassLoader() {
        return defaultClassLoader;
    }

    /**
     * Set the ClassLoader associated with the given className. If either the
     * class name or the loader are null, no action is performed.
     *
     * @param className the name of a class
     * @param loader the ClassLoader for the class
     */
    public static void setClassLoader(String className, ClassLoader loader) {
        if (className != null && loader != null)
            classloaders.put(className, loader);
    }

    /**
     * Obtain the ClassLoader (if any) associated with the given
     * className.
     *
     * @param className the name of a class
     * @return class loader
     */
    public static ClassLoader getClassLoader(String className) {
        if (className == null) {
            return null;
        }
        return (ClassLoader) classloaders.get(className);
    }

    /**
     * Deregister the ClassLoader for a given className.
     *
     * @param className the name of a class
     */
    public static void removeClassLoader(String className) {
        classloaders.remove(className);
    }


    /**
     * Use this method instead of Class.forName
     *
     * @param className Class name
     * @return java class
     * @throws ClassNotFoundException if the class is not found
     */
    public static Class forName(String className)
            throws ClassNotFoundException {
        return loadClass(className);
    }

    /**
     * Use this method instead of Class.forName (String className, boolean init, ClassLoader loader)
     *
     * @param _className Class name
     * @param init initialize the class
     * @param _loader class loader
     * @return java class
     *
     * @throws ClassNotFoundException if the class is not found
     */
    public static Class forName(
            String _className, boolean init, ClassLoader _loader)
            throws ClassNotFoundException {

        // Create final vars for doPrivileged block
        final String className = _className;
        final ClassLoader loader = _loader;
        try {
            // Get the class within a doPrivleged block
            Object ret =
                AccessController.doPrivileged(
                    new PrivilegedAction() {
                        public Object run() {
                            try {
                                return Class.forName(className, true, loader);
                            } catch (Throwable e) {
                                return e;
                            }
                        }
                    });
            // If the class was located, return it.  Otherwise throw exception
            if (ret instanceof Class) {
                return (Class) ret;
            } else if (ret instanceof ClassNotFoundException) {
                throw (ClassNotFoundException) ret;
            } else {
                throw new ClassNotFoundException(_className);
            }
        } catch (ClassNotFoundException cnfe) {
            return loadClass(className);
        }
    }

    /**
     * Loads the class from the context class loader and then falls back to
     * getDefaultClassLoader().forName
     *
     * @param _className Class name
     * @return java class
     * @throws ClassNotFoundException if the class is not found
     */
    private static Class loadClass(String _className)
            throws ClassNotFoundException {
        // Create final vars for doPrivileged block
        final String className = _className;

        // Get the class within a doPrivleged block
        Object ret =
            AccessController.doPrivileged(
                    new PrivilegedAction() {
                        public Object run() {
                            try {
                                // Check if the class is a registered class then
                                // use the classloader for that class.
                                ClassLoader classLoader = getClassLoader(className);
                                return Class.forName(className, true, classLoader);
                            } catch (ClassNotFoundException cnfe) {
                            }

                            try {
                                // Try the context class loader
                                ClassLoader classLoader =
                                    Thread.currentThread().getContextClassLoader();
                                return Class.forName(className, true, classLoader);
                            } catch (ClassNotFoundException cnfe2) {
                                try {
                                    // Try the classloader that loaded this class.
                                    ClassLoader classLoader =
                                        ClassUtils.class.getClassLoader();
                                    return Class.forName(className, true, classLoader);
                                } catch (ClassNotFoundException cnfe3) {
                                    // Try the default class loader.
                                    try {
                                        return defaultClassLoader.loadClass(
                                                className);
                                    } catch (Throwable e) {
                                        // Still not found, return exception
                                        return e;
                                    }
                                }
                            }
                        }
                    });

        // If the class was located, return it.  Otherwise throw exception
        if (ret instanceof Class) {
            return (Class) ret;
        } else if (ret instanceof ClassNotFoundException) {
            throw (ClassNotFoundException) ret;
        } else {
            throw new ClassNotFoundException(_className);
        }
    }

    /**
     * Get an input stream from a named resource.
     * Tries
     * <ol>
     * <li>the classloader that loaded "clazz" first,
     * <li>the system classloader
     * <li>the class "clazz" itself
     * </ol>
     * @param clazz class to use in the lookups
     * @param resource resource string to look for
     * @param checkThreadContextFirst check the thread context first?
     * @return input stream if found, or null
     */
    public static InputStream getResourceAsStream(Class clazz, String resource, boolean checkThreadContextFirst) {
        InputStream myInputStream = null;

        if (checkThreadContextFirst &&
                Thread.currentThread().getContextClassLoader() != null) {
            // try the context class loader.
            myInputStream =
                    Thread.currentThread().getContextClassLoader()
                    .getResourceAsStream(resource);
        }
        if (myInputStream == null) {
            // if not found in context class loader fall back to default
            myInputStream = getResourceAsStream(clazz, resource);
        }
        return myInputStream;
    }

    /**
     * Get an input stream from a named resource.
     * Tries
     * <ol>
     * <li>the classloader that loaded "clazz" first,
     * <li>the system classloader
     * <li>the class "clazz" itself
     * </ol>
     * @param clazz class to use in the lookups
     * @param resource resource string to look for
     * @return input stream if found, or null
     */
    public static InputStream getResourceAsStream(Class clazz, String resource) {
        InputStream myInputStream = null;

        if(clazz.getClassLoader()!=null) {
            // Try the class loader that loaded this class.
            myInputStream = clazz.getClassLoader().getResourceAsStream(resource);
        } else {
            // Try the system class loader.
            myInputStream = ClassLoader.getSystemClassLoader().getResourceAsStream(resource);
        }
        if (myInputStream == null && Thread.currentThread().getContextClassLoader() != null) {
            // try the context class loader.
            myInputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(resource);
        }
        if (myInputStream == null) {
            // if not found in classpath fall back to default
            myInputStream = clazz.getResourceAsStream(resource);
        }
        return myInputStream;
    }

    /**
     * Creates a new ClassLoader from a classpath specification and a parent
     * class loader.
     * The classpath string will be split using the system path seperator
     * character (e.g. : or ;), just as the java system-wide class path is
     * processed.
     *
     * @param classpath  the classpath String
     * @param parent  the parent ClassLoader, or null if the default is to be
     *     used
     * @throws SecurityException if you don't have privilages to create
     *         class loaders
     * @throws IllegalArgumentException if your classpath string is silly
     */
    public static ClassLoader createClassLoader(String classpath,
                                                ClassLoader parent)
            throws SecurityException
    {
        String[] names = StringUtils.split(classpath, System.getProperty("path.separator").charAt(0));

        URL[] urls = new URL[names.length];
        try {
            for(int i = 0; i < urls.length; i++)
                urls[i] = new File(names[i]).toURL();
        }
        catch (MalformedURLException e) {
          // I don't think this is possible, so I'm throwing this as an
          // un-checked exception
          throw (IllegalArgumentException) new IllegalArgumentException(
                  "Unable to parse classpath: " + classpath);
        }

        return new URLClassLoader(urls, parent);
    }
}
