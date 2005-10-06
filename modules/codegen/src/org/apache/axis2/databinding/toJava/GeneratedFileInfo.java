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
package org.apache.axis2.databinding.toJava;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * File info available after emit to describe what
 * exactly was created by the Emitter.
 * 
 * @author Tom Jordahl (tomj@macromedia.com)
 */
public class GeneratedFileInfo {

    /** Field list */
    protected ArrayList list = new ArrayList();

    /**
     * Structure to hold entries.
     * There are three public data members:
     * <ul>
     * <li><code>fileName</code> - A relative path of the generated file.</li>
     * <li><code>className</code> - The name of the class in the file.</li>
     * <li><code>type</code> - The type of the file.<br>
     * Valid types are:<br>
     * <code>
     * stub, interface, complexType, enumType, fault, holder, skeleton,
     * skeletonImpl, service, deploy, undeploy, testCase
     * </code></li>
     * </ul>
     */
    public class Entry {

        // relative path of the emitted file

        /** Field fileName */
        public String fileName;

        // name of emitted class

        /** Field className */
        public String className;

        // function of the emitted class

        /** Field type */
        public String type;

        /**
         * Constructor Entry
         * 
         * @param name      
         * @param className 
         * @param type      
         */
        public Entry(String name, String className, String type) {

            this.fileName = name;
            this.className = className;
            this.type = type;
        }

        /**
         * Method toString
         * 
         * @return 
         */
        public String toString() {
            return "Name: " + fileName + " Class: " + className + " Type: "
                    + type;
        }
    }    // Entry

    /**
     * Construct an empty file info list.
     */
    public GeneratedFileInfo() {
    }

    /**
     * Return the entire list of generated files
     * 
     * @return 
     */
    public List getList() {
        return list;
    }

    /**
     * Add an entry
     * 
     * @param name      
     * @param className 
     * @param type      
     */
    public void add(String name, String className, String type) {
        list.add(new Entry(name, className, type));
    }

    /**
     * Lookup an entry by type.
     * <br>
     * Valid type values are:
     * stub, interface, complexType, enumType, fault, holder, skeleton,
     * skeletonImpl, service, deploy, undeploy, testCase
     * 
     * @param type of objects you want info about
     * @return A list of <code>org.apache.axis.wsdl.toJava.GeneratedFileInfo.Entry</code> objects.  Null if no objects found.
     */
    public List findType(String type) {

        // look at each entry for the type we want
        ArrayList ret = null;

        for (Iterator i = list.iterator(); i.hasNext();) {
            Entry e = (Entry) i.next();

            if (e.type.equals(type)) {
                if (ret == null) {
                    ret = new ArrayList();
                }

                ret.add(e);
            }
        }

        return ret;
    }

    /**
     * Lookup an entry by file name
     * 
     * @param file     name you want info about
     * @param fileName 
     * @return The entry for the file name specified.  Null if not found
     */
    public Entry findName(String fileName) {

        // look at each entry for the type we want
        for (Iterator i = list.iterator(); i.hasNext();) {
            Entry e = (Entry) i.next();

            if (e.fileName.equals(fileName)) {
                return e;
            }
        }

        return null;
    }

    /**
     * Lookup an entry by class name
     * 
     * @param class     name you want info about
     * @param className 
     * @return The entry for the class specified.  Null if not found
     */
    public Entry findClass(String className) {

        // look at each entry for the type we want
        for (Iterator i = list.iterator(); i.hasNext();) {
            Entry e = (Entry) i.next();

            if (e.className.equals(className)) {
                return e;
            }
        }

        return null;
    }

    /**
     * Get the list of generated classes
     * 
     * @return 
     */
    public List getClassNames() {

        // is there a better way to do this?
        ArrayList ret = new ArrayList(list.size());

        for (Iterator i = list.iterator(); i.hasNext();) {
            Entry e = (Entry) i.next();

            ret.add(e.className);
        }

        return ret;
    }

    /**
     * Get the list of generated filenames
     * 
     * @return 
     */
    public List getFileNames() {

        // is there a better way to do this?
        ArrayList ret = new ArrayList(list.size());

        for (Iterator i = list.iterator(); i.hasNext();) {
            Entry e = (Entry) i.next();

            ret.add(e.fileName);
        }

        return ret;
    }

    /**
     * Convert all entries in the list to a string
     * 
     * @return 
     */
    public String toString() {

        String s = "";

        for (Iterator i = list.iterator(); i.hasNext();) {
            Entry entry = (Entry) i.next();

            s += entry.toString() + "\n";
        }

        return s;
    }
}
