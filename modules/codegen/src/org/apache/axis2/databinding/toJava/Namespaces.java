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

//import org.apache.axis.utils.JavaUtils;
import org.apache.axis2.databinding.utils.JavaUtils;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 * This class is essentially a HashMap of <namespace, package name> pairs with
 * a few extra wizzbangs.
 */
public class Namespaces extends HashMap {

    /** Field root */
    private String root;

    /** Field defaultPackage */
    private String defaultPackage = null;

    /** Toknens in a namespace that are treated as package name part separators. */
    private static final char[] pkgSeparators = {'.', ':'};

    /** Field javaPkgSeparator */
    private static final char javaPkgSeparator = pkgSeparators[0];
    
    /** Field pkg2Namespaces : reverse mapping of Namespaces */
    private Map pkg2NamespacesMap = new HashMap();

    /**
     * Method normalizePackageName
     * 
     * @param pkg       
     * @param separator 
     * @return 
     */
    private static String normalizePackageName(String pkg, char separator) {

        for (int i = 0; i < pkgSeparators.length; i++) {
            pkg = pkg.replace(pkgSeparators[i], separator);
        }

        return pkg;
    }

    /**
     * Instantiate a Namespaces object whose packages will all reside under root.
     * 
     * @param root 
     */
    public Namespaces(String root) {

        super();

        this.root = root;
    }    // ctor

    /**
     * Instantiate a clone of an existing Namespaces object.
     * 
     * @param clone 
     */
    private Namespaces(Namespaces clone) {

        super(clone);

        this.root = clone.root;
        this.defaultPackage = clone.defaultPackage;
    }    // ctor

    /**
     * Instantiate a clone of this Namespaces object.
     * 
     * @return 
     */
    public Object clone() {
        return new Namespaces(this);
    }    // clone

    /**
     * Get the package name for the given namespace.  If there is no entry in the HashMap for
     * this namespace, create one.
     * 
     * @param key 
     * @return 
     */
    public String getCreate(String key) {
        return getCreate(key, true);
    }    // getCreate

    /**
     * Get the package name for the given namespace.  If there is no entry in the HashMap for
     * this namespace, create one if create flag is on, return <tt>null</tt> otherwise.
     * 
     * @param key    
     * @param create 
     * @return 
     */
    String getCreate(String key, boolean create) {

        if (defaultPackage != null) {
            put(key, defaultPackage);
            return defaultPackage;
        }

        String value = (String) super.get(key);

        if ((value == null) && create) {
            value = normalizePackageName((String) Utils.makePackageName(key),
                    javaPkgSeparator);

            put(key, value);
        }

        return (String) value;
    }    // getCreate

    /**
     * Get the package name in directory format (dots replaced by slashes).  If the package name
     * doesn't exist in the HashMap, return "".
     * 
     * @param key 
     * @return 
     */
    public String getAsDir(String key) {

        if (defaultPackage != null) {
            return toDir(defaultPackage);
        }

        String pkg = (String) get(key);

        return toDir(pkg);
    }    // getAsDir

    /**
     * Return the given package name in directory format (dots replaced by slashes).  If pkg is null,
     * "" is returned.
     * 
     * @param pkg 
     * @return 
     */
    public String toDir(String pkg) {

        String dir = null;

        if (pkg != null) {
            pkg = normalizePackageName(pkg, File.separatorChar);
        }

        if (root == null) {
            dir = pkg;
        } else {
            dir = root + File.separatorChar + pkg;
        }

        return (dir == null)
                ? ""
                : dir + File.separatorChar;
    }    // toDir

    /**
     * Like HashMap's putAll, this adds the given map's contents to this map.  But it
     * also makes sure the value strings are javified.
     * 
     * @param map 
     */
    public void putAll(Map map) {

        Iterator i = map.entrySet().iterator();

        while (i.hasNext()) {
            Map.Entry entry = (Map.Entry) i.next();
            Object key = entry.getKey();
            String pkg = (String) entry.getValue();

            pkg = javify(pkg);

            put(key, pkg);
        }
    }    // putAll

    /**
     * Make sure each package name doesn't conflict with a Java keyword.
     * Ie., org.apache.import.test becomes org.apache.import_.test.
     * 
     * @param pkg 
     * @return 
     */
    private String javify(String pkg) {

        StringTokenizer st = new StringTokenizer(pkg, ".");

        pkg = "";

        while (st.hasMoreTokens()) {
            String token = st.nextToken();

            if (JavaUtils.isJavaKeyword(token)) {
                token = JavaUtils.makeNonJavaKeyword(token);
            }

            pkg = pkg + token;

            if (st.hasMoreTokens()) {
                pkg = pkg + '.';
            }
        }

        return pkg;
    }    // javify

    /**
     * Make a directory for the given package under root.
     * 
     * @param pkg 
     */
    public void mkdir(String pkg) {

        String pkgDirString = toDir(pkg);
        File packageDir = new File(pkgDirString);

        packageDir.mkdirs();
    }    // mkdir

    /**
     * Set a package name that overrides the namespace map
     * 
     * @param defaultPackage a java package name (e.g. com.foo)
     */
    public void setDefaultPackage(String defaultPackage) {
        this.defaultPackage = defaultPackage;
    }
    
    public Object put(Object key, Object value) {
        // Store pakcage->namespaces vector mapping
        Vector v = null;
        if (!pkg2NamespacesMap.containsKey(value)) {
            v = new Vector();                       
        } else {
            v = (Vector)pkg2NamespacesMap.get(value);
        }
        // NOT need to add an input key (namespace value) to v (package vector) 
        if (!v.contains(key)) { 
            v.add(key); 
        }
        pkg2NamespacesMap.put(value, v);
         
        return super.put(key, value);
    }
    
    public Map getPkg2NamespacesMap() {
        return pkg2NamespacesMap;
    }
}    // class Namespaces
