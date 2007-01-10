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


package org.apache.axis2.jaxws.description;

import javax.xml.namespace.QName;

/**
 * 
 */
public class DescriptionUtils {
    
    public static boolean isEmpty(String string) {
        return (string == null || "".equals(string));
    }
    
    public static boolean isEmpty(QName qname) {
        return qname == null || isEmpty(qname.getLocalPart());
    }

    /**
     * Creat a java class name given a java method name (i.e. capitalize the first letter)
     * @param name
     * @return
     */
    public static String javaMethodtoClassName(String methodName) {
        String className = null;
        if(methodName != null){
            StringBuffer buildClassName = new StringBuffer(methodName);
            buildClassName.replace(0, 1, methodName.substring(0,1).toUpperCase());
            className = buildClassName.toString();
        }
        return className;
    }

}
