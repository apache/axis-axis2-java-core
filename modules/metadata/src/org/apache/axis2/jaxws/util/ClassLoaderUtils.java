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

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLDecoder;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.HashSet;
import java.util.StringTokenizer;
import java.util.Iterator;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;

public class ClassLoaderUtils {

    private static final Log log = LogFactory.getLog(ClassLoaderUtils.class);

    /**
     * @return ClassLoader
     */
    public static ClassLoader getClassLoader(final Class cls) {
        ClassLoader cl = null;
        try {
            cl = (ClassLoader) AccessController.doPrivileged(
                    new PrivilegedExceptionAction() {
                        public Object run() throws ClassNotFoundException {
                            return cls.getClassLoader();
                        }
                    }
            );
        } catch (PrivilegedActionException e) {
            if (log.isDebugEnabled()) {
                log.debug("Exception thrown from AccessController: " + e);
            }
            throw ExceptionFactory.makeWebServiceException(e.getException());
        }

        return cl;
    }

    /**
     * @return ClassLoader
     */
    public static ClassLoader getContextClassLoader(final ClassLoader classLoader) {
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
    public static Class forName(final String className, final boolean initialize,
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

    /**
     * Return the class for this name
     *
     * @return Class
     */
    public static Class forName(final String className) throws ClassNotFoundException {
        Class cl = null;
        try {
            cl = (Class) AccessController.doPrivileged(
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
            throw (ClassNotFoundException) e.getException();
        }

        return cl;
    }

    /**
     * Expand a directory path or list of directory paths (File.pathSeparator
     * delimited) into a list of file paths of all the jar files in those
     * directories.
     *
     * @param dirPaths The string containing the directory path or list of
     *                 directory paths.
     * @return The file paths of the jar files in the directories. This is an
     *         empty string if no files were found, and is terminated by an
     *         additional pathSeparator in all other cases.
     */
    public static String expandDirs(String dirPaths) {
        StringTokenizer st = new StringTokenizer(dirPaths, File.pathSeparator);
        StringBuffer buffer = new StringBuffer();
        while (st.hasMoreTokens()) {
            String d = st.nextToken();
            File dir = new File(d);
            if (dir.isDirectory()) {
                File[] files = dir.listFiles(new JavaArchiveFilter());
                for (int i = 0; i < files.length; i++) {
                    buffer.append(files[i]).append(File.pathSeparator);
                }
            }
        }
        return buffer.toString();
    }

    /**
     * Check if this inputstream is a jar/zip
     *
     * @param is
     * @return true if inputstream is a jar
     */
    public static boolean isJar(InputStream is) {
        try {
            JarInputStream jis = new JarInputStream(is);
            if (jis.getNextEntry() != null) {
                return true;
            }
        } catch (IOException ioe) {
        }
        return false;
    }

    /**
     * Get the default classpath from various thingies in the message context
     *
     * @param msgContext
     * @return default classpath
     */
    public static String getDefaultClasspath(String webBase) {
        HashSet classpath = new HashSet();
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        fillClassPath(cl, classpath);

        // Just to be safe (the above doesn't seem to return the webapp
        // classpath in all cases), manually do this:
        if (webBase != null) {
            addPath(classpath, webBase + File.separatorChar + "classes");
            try {
                String libBase = webBase + File.separatorChar + "lib";
                File libDir = new File(libBase);
                String[] jarFiles = libDir.list();
                for (int i = 0; i < jarFiles.length; i++) {
                    String jarFile = jarFiles[i];
                    if (jarFile.endsWith(".jar")) {
                        addPath(classpath, libBase +
                                File.separatorChar +
                                jarFile);
                    }
                }
            } catch (Exception e) {
                // Oh well.  No big deal.
            }
        }

        // axis.ext.dirs can be used in any appserver
        getClassPathFromDirectoryProperty(classpath, "axis.ext.dirs");

        // classpath used by Jasper 
        getClassPathFromProperty(classpath, "org.apache.catalina.jsp_classpath");

        // websphere stuff.
        getClassPathFromProperty(classpath, "ws.ext.dirs");
        getClassPathFromProperty(classpath, "com.ibm.websphere.servlet.application.classpath");

        // java class path
        getClassPathFromProperty(classpath, "java.class.path");

        // Load jars from java external directory
        getClassPathFromDirectoryProperty(classpath, "java.ext.dirs");

        // boot classpath isn't found in above search
        getClassPathFromProperty(classpath, "sun.boot.class.path");
        
        StringBuffer path = new StringBuffer();
        for (Iterator iterator = classpath.iterator(); iterator.hasNext();) {
            String s = (String) iterator.next();
            path.append(s);
            path.append(File.pathSeparatorChar);
        }
        log.info(path);
        return path.toString();
    }

    private static void addPath(HashSet classpath, String s) {
        String path = s.replace(((File.separatorChar == '/') ? '\\' : '/'), File.separatorChar).trim();
        File file = new File(path);
        if (file.exists()) {
            path = file.getAbsolutePath();
            classpath.add(path);
        }
    }

    /**
     * Add all files in the specified directory to the classpath
     *
     * @param classpath
     * @param property
     */
    private static void getClassPathFromDirectoryProperty(HashSet classpath, String property) {
        String dirs = System.getProperty(property);
        String path = null;
        try {
            path = expandDirs(dirs);
        } catch (Exception e) {
            // Oh well.  No big deal.
        }
        if (path != null) {
            addPath(classpath, path);
        }
    }

    /**
     * Add a classpath stored in a property.
     *
     * @param classpath
     * @param property
     */
    private static void getClassPathFromProperty(HashSet classpath, String property) {
        String path = System.getProperty(property);
        if (path != null) {
            addPath(classpath, path);
        }
    }

    /**
     * Walk the classloader hierarchy and add to the classpath
     *
     * @param cl
     * @param classpath
     */
    private static void fillClassPath(ClassLoader cl, HashSet classpath) {
        while (cl != null) {
            if (cl instanceof URLClassLoader) {
                URL[] urls = ((URLClassLoader) cl).getURLs();
                for (int i = 0; (urls != null) && i < urls.length; i++) {
                    String path = urls[i].getPath();
                    //If it is a drive letter, adjust accordingly.
                    if (path.length() >= 3 && path.charAt(0) == '/' && path.charAt(2) == ':')
                        path = path.substring(1);
                    addPath(classpath, URLDecoder.decode(path));

                    // if its a jar extract Class-Path entries from manifest
                    File file = new File(urls[i].getFile());
                    if (file.isFile()) {
                        FileInputStream fis = null;
                        try {
                            fis = new FileInputStream(file);
                            if (isJar(fis)) {
                                JarFile jar = new JarFile(file);
                                Manifest manifest = jar.getManifest();
                                if (manifest != null) {
                                    Attributes attributes = manifest.getMainAttributes();
                                    if (attributes != null) {
                                        String s = attributes.getValue(Attributes.Name.CLASS_PATH);
                                        String base = file.getParent();
                                        if (s != null) {
                                            StringTokenizer st = new StringTokenizer(s, " ");
                                            while (st.hasMoreTokens()) {
                                                String t = st.nextToken();
                                                addPath(classpath, base + File.separatorChar + t);
                                            }
                                        }
                                    }
                                }
                            }
                        } catch (IOException ioe) {
                        } finally {
                            if (fis != null) {
                                try {
                                    fis.close();
                                } catch (IOException ioe2) {
                                }
                            }
                        }
                    }
                }
            }
            cl = cl.getParent();
        }
    }

    /**
     * Filter for zip/jar
     */
    private static class JavaArchiveFilter implements FileFilter {
        public boolean accept(File file) {
            String name = file.getName().toLowerCase();
            return (name.endsWith(".jar") || name.endsWith(".zip"));
        }
    }
}
