package org.apache.ws.java2wsdl;

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
*
*/


public class Java2WSDLUtils {
    public static final String HTTP = "http://";
    public static final char PACKAGE_CLASS_DELIMITER = '.';
    public static final String SCHEMA_NAMESPACE_EXTN = "/xsd";

    /**
     * check the entry for a URL. This is a simple check and need to be improved
     *
     * @param entry
     */

    public static boolean isURL(String entry) {
        return entry.startsWith("http://");
    }

    /**
     * A method to strip the fully qualified className to a simple classname
     *
     * @param qualifiedName
     */
    public static String getSimpleClassName(String qualifiedName) {
        int index = qualifiedName.lastIndexOf(".");
        if (index > 0) {
            return qualifiedName.substring(index + 1, qualifiedName.length());
        }
        return qualifiedName;
    }

    public static StringBuffer namespaceFromPackageName(String packageName) {

        StringBuffer strBuf = new StringBuffer();
        int prevIndex = packageName.length();
        int currentIndex = packageName.lastIndexOf(PACKAGE_CLASS_DELIMITER);
        if (currentIndex > 0) {
            strBuf.append(HTTP);
        } else if (prevIndex > 0) {
            strBuf.append(HTTP);
            strBuf.append(packageName);
            return strBuf;
        } else if (currentIndex == -1) {
//            strBuf.append(HTTP);
//            strBuf.append(packageName);
            return strBuf;
        }
        while (currentIndex != -1) {
            strBuf.append(packageName.substring(currentIndex + 1, prevIndex));
            prevIndex = currentIndex;
            currentIndex = packageName.lastIndexOf(PACKAGE_CLASS_DELIMITER, prevIndex - 1);
            strBuf.append(PACKAGE_CLASS_DELIMITER);

            if (currentIndex == -1) {
                strBuf.append(packageName.substring(0, prevIndex));
            }
        }
        return strBuf;
    }

    public static StringBuffer schemaNamespaceFromClassName(String packageName, ClassLoader loader) throws Exception {
        StringBuffer stringBuffer = namespaceFromClassName(packageName, loader);
        if (stringBuffer.length() == 0) {
            stringBuffer.append(Java2WSDLConstants.DEFAULT_TARGET_NAMESPACE);
        }
        stringBuffer.append(SCHEMA_NAMESPACE_EXTN);
        return stringBuffer;
    }

    public static StringBuffer namespaceFromClassName(String className, ClassLoader classLoader) throws Exception {
        Class clazz = Class.forName(className, true, classLoader);
        Package pkg = clazz.getPackage();
        String name;

        if (pkg != null)
            name = pkg.getName();
        else
            name = packageNameFromClass(clazz.getName());

        return namespaceFromPackageName(name);
    }

    public static String getPackageName(String className, ClassLoader classLoader) throws Exception {
        Class clazz = Class.forName(className, true, classLoader);
        Package pkg = clazz.getPackage();
        String name;

        if (pkg != null)
            name = pkg.getName();
        else
            name = packageNameFromClass(clazz.getName());
        return name;
    }

    protected static String packageNameFromClass(String name) {
        String ret = "";
        int lastDot = name.lastIndexOf('.');

        if (lastDot != -1)
            ret = name.substring(lastDot + 1);
        return ret;
    }

    public static StringBuffer schemaNamespaceFromPackageName(String packageName) {
        if (packageName.length() > 0) {
            return namespaceFromPackageName(packageName).append(SCHEMA_NAMESPACE_EXTN);
        } else {
            StringBuffer buffer = new StringBuffer();
            buffer.append(Java2WSDLConstants.DEFAULT_TARGET_NAMESPACE);
            buffer.append(SCHEMA_NAMESPACE_EXTN);
            return buffer;
        }
    }
}
