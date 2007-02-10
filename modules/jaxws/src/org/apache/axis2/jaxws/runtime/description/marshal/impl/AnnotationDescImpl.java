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
package org.apache.axis2.jaxws.runtime.description.marshal.impl;

import java.util.StringTokenizer;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchema;

import org.apache.axis2.jaxws.runtime.description.marshal.AnnotationDesc;

/**
 *
 */
class AnnotationDescImpl implements AnnotationDesc {

    private boolean _hasXmlRootElement = false;
    private String _XmlRootElementName = null;
    private String _XmlRootElementNamespace = null;
    
    private AnnotationDescImpl() {
        super();
    }

    public boolean hasXmlRootElement() {
        return _hasXmlRootElement;
    }

    public String getXmlRootElementName() {
       return _XmlRootElementName;
    }

    public String getXmlRootElementNamespace() {
        return _XmlRootElementNamespace;
    }

    static AnnotationDesc create(Class cls) {
        AnnotationDescImpl aDesc = new AnnotationDescImpl();
        
        XmlRootElement root = (XmlRootElement) cls.getAnnotation(XmlRootElement.class);
        if (root == null) {
            return aDesc;
        }
        aDesc._hasXmlRootElement = true;
        String name = root.name();
        String namespace = root.namespace();
        
        // The name may need to be defaulted
        if (name == null || name.length() == 0 || namespace.equals("##default")) {
            name = getSimpleName(cls.getCanonicalName());
        }
        
        // The namespace may need to be defaulted
        if (namespace == null || namespace.length() == 0 || namespace.equals("##default")) {
            Package pkg = cls.getPackage();
            XmlSchema schema = (XmlSchema) pkg.getAnnotation(XmlSchema.class);
            if (schema != null) {
                namespace = schema.namespace();
            } else {
                namespace = "";
            }
        }
        
        aDesc._XmlRootElementName = name;
        aDesc._XmlRootElementNamespace = namespace;
          
        return aDesc;
    }
    
    /**
     * utility method to get the last token in a "."-delimited package+classname string
     * @return
     */
    private static String getSimpleName(String in) {
        if (in == null || in.length() == 0) {
            return in;
        }
        String out = null;
        StringTokenizer tokenizer = new StringTokenizer(in, ".");
        if (tokenizer.countTokens() == 0)
            out = in;
        else {
            while (tokenizer.hasMoreTokens()) {
                out = tokenizer.nextToken();
            }
        }
        return out;
    }
}
