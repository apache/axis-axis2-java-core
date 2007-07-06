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
package org.apache.axis2.jaxws.runtime.description.marshal.impl;

import org.apache.axis2.jaxws.runtime.description.marshal.AnnotationDesc;
import org.apache.axis2.jaxws.utility.XMLRootElementUtil;

import javax.xml.namespace.QName;

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

        QName qName = XMLRootElementUtil.getXmlRootElementQName(cls);
        if (qName == null) {
            return aDesc;
        }
        aDesc._hasXmlRootElement = true;
        aDesc._XmlRootElementName = qName.getLocalPart();
        aDesc._XmlRootElementNamespace = qName.getNamespaceURI();

        return aDesc;
    }

    public String toString() {
        final String newline = "\n";
        StringBuffer string = new StringBuffer();

        string.append(newline);
        string.append("      @XMLRootElement exists = " + this.hasXmlRootElement());

        if (this.hasXmlRootElement()) {
            string.append(newline);
            string.append("      @XMLRootElement namespace = " + this.getXmlRootElementNamespace());
            string.append(newline);
            string.append("      @XMLRootElement name      = " + this.getXmlRootElementName());
        }


        return string.toString();
    }
}
