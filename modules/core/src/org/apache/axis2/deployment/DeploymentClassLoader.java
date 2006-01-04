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


package org.apache.axis2.deployment;

import org.apache.axis2.i18n.Messages;

import java.io.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class DeploymentClassLoader extends URLClassLoader {

    // To keep jar files inside /lib directory in the main jar
    private ArrayList lib_jars_list;
    private HashMap loadedClass;
    //to set the loading ordere parent first or child first : default is child first
    private boolean parentFirst = false;

    // urls which gives to create the classLoader
    private URL[] urls;

    /**
     * DeploymentClassLoader is extended from URLClassLoader. The constructor
     * does not override the super constructor, but does additional steps like find out
     * jar fils inside /lib directory.
     *
     * @param urls   <code>URL</code>
     * @param parent parent classloader <code>ClassLoader</code>
     */
    public DeploymentClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
        this.urls = urls;
        lib_jars_list = new ArrayList();
        loadedClass = new HashMap();
        findLibJars();
    }

    /**
     * @param name <code>String</code>  Name of the file to be loaded
     * @return Returns a  <code>Class</code> object if it found else
     *         returns null or ClassNotFoundException.
     *         <p/>
     *         The method finds the class in the following way:
     *         <br>     
     *         1. Calls the super class to check to see whether the class is there.
     *         If the class is found then return that , else go to step 2 </br>
     *         <br>     
     *         2. Check wether the class name exist in one of jar files
     *         in /lib directory. If it is found, get the byte array and create a Class 
     *         object from it by calling "defineClass()", else throws ClassNotFoundException.
     *         </br>     
     * @throws ClassNotFoundException
     */
    protected Class findClass(final String name) throws ClassNotFoundException {
        Class cla;
        cla = (Class) loadedClass.get(name);
        if (cla != null) {
            //the class is already in the loaded class list so no need to load it again
            return cla;
        }
        if (parentFirst) {
            //if parent first is true then first it is require to check in the parent
            boolean foundClass;
            try {
                cla = super.findClass(name);
                loadedClass.put(name, cla);
                return cla;
            } catch (ClassNotFoundException e) {
                foundClass = false;
            }
            if (!foundClass) {
                try {
                    byte raw[] = getClassByteCodes(name);
                    cla = defineClass(name, raw, 0, raw.length);
                    loadedClass.put(name, cla);
                    return cla;
                } catch (Exception e) {
                    foundClass = false;
                } catch (ClassFormatError classFormatError) {
                    foundClass = false;
                }
            }
            if (!foundClass) {
                throw new ClassNotFoundException(
                        Messages.getMessage(DeploymentErrorMsgs.CLASS_NOT_FOUND, name));
            }
        } else {
            try {
                byte raw[] = getClassByteCodes(name);
                cla = defineClass(name, raw, 0, raw.length);
                loadedClass.put(name, cla);
                return cla;
            } catch (ClassFormatError e) {
                throw e;
            } catch (Exception e) {
                // no need to do any thing
            }
            // finding in parent
            cla = super.findClass(name);
            loadedClass.put(name, cla);
            return cla;
        }
        return null;
    }

    /**
     * Searches for jar files in /lib directory. If they exist, they are 
     * will be added to the arraylist (only the name of the jar file).
     */
    private void findLibJars() {
        /**
         * though the URL array can contains one or more urls , I have only consider the
         * first one , that is this classLoader is only for Axis2 stuff and the classloader
         * is created by Deployment , so there wont be any chance to have more the one urls for
         * the URL array list
         */
        File file = new File(urls[0].getFile());

        try {
            ZipInputStream zin = new ZipInputStream(new FileInputStream(file));
            ZipEntry entry;
            String entryName;
            while ((entry = zin.getNextEntry()) != null) {
                entryName = entry.getName();
                /**
                 * id the entry name start with /lib and end with .jar
                 * then those entry name will be added to the arraylist
                 */
                if ((entryName != null) && entryName.toLowerCase().startsWith("lib/")
                        && entryName.toLowerCase().endsWith(".jar")) {
                    lib_jars_list.add(entryName);
                }
            }
            zin.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Reads jar file (/lib) one by one , then for each file creates <code>ZipInputStream</code>
     * and checks to see wether there is any entry with the given name. If it found then
     * creates ByteArrayOutPutStream and returns that.
     *
     * @param resourceName : Name of the resource that your are going to use
     * @return Returns <code>byte[]</code> .
     */
    private byte[] getBytes(String resourceName) {
        byte raw[];
        ZipInputStream zin = null;
        for (int i = 0; i < lib_jars_list.size(); i++) {
            String libjar_name = (String) lib_jars_list.get(i);
            // seince we are trying to get jar file , we need for sure that all the
            // libs are in parent class loader not in child class loader
            InputStream in = super.getResourceAsStream(libjar_name);
            try {
                zin = new ZipInputStream(in);
                ZipEntry entry;
                String entryName;
                while ((entry = zin.getNextEntry()) != null) {
                    entryName = entry.getName();
                    if ((entryName != null) && entryName.endsWith(resourceName)) {
                        byte data[] = new byte[2048];
                        ByteArrayOutputStream out = new ByteArrayOutputStream();
                        int count;
                        while ((count = zin.read(data, 0, 2048)) != -1) {
                            out.write(data, 0, count);
                        }
                        raw = out.toByteArray();
                        out.close();
                        return raw;
                    }
                }
            } catch (IOException e) {
                return null;
            } finally {
                try {
                    if (zin != null) {
                        zin.close();
                    }
                } catch (IOException e) {
                    // TODO: what to do, better to log
                }
            }
        }

        return null;
    }

    /**
     * Reads the jar file (/lib) one by one. For each file creates <code>ZipInputStream</code>
     * and checks to see wether there is any entry with the name. If it is found then
     * creates ByteArrayOutPutStream and gets the byte code for that. After going through
     * each and every jar file if there is no entry with the name, throws a ClassNotFound exception.
     *
     * @param filename <code>String</code>  Name of the file to be loaded (Class Name)
     * @return Returns byte[].
     * @throws java.io.IOException <code>Exception</code>
     */
    private byte[] getClassByteCodes(String filename) throws Exception {
        String completeFileName = filename;

        /**
         * Replacing org.apache. -> org/apache/...
         */
        completeFileName = completeFileName.replace('.', '/').concat(".class");
        byte[] byteCodes = getBytes(completeFileName);
        if (byteCodes != null) {
            return byteCodes;
        } else {
            throw new ClassNotFoundException(
                    Messages.getMessage(DeploymentErrorMsgs.CLASS_NOT_FOUND, filename));
        }
    }

    /*
     * Locates resources similar to the way that getClassByteCodes() locates classes.
     * The bytes from resources are not stored in memory, as the size of resources is 
     * generally unpredictable.
     *
     * @param name
     * @return inputstream
     */
    public InputStream getResourceAsStream(String name) {
        if (name == null) {
            return null;
        }
        if (parentFirst) {
            InputStream is = super.getResourceAsStream(name);
            // ohh , input stream is null , so need to check whether thats there in lib/*.jar
            if (is == null) {
                byte data[] = getBytes(name);
                if (data == null) {
                    return null;
                } else {
                    return new ByteArrayInputStream(data);
                }
            } else {
                return is;
            }
        } else {
            byte data[] = getBytes(name);
            if (data != null) {
                return new ByteArrayInputStream(data);
            }
            return super.getResourceAsStream(name);
        }
    }

    public void setParentFirst(boolean parentFirst) {
        this.parentFirst = parentFirst;
    }
}
