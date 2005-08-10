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
package org.apache.axis2.saaj;

import javax.xml.namespace.QName;
import javax.xml.soap.Name;

/**
 * Class Prefixed QName
 * <p/>
 * Took this implementation from Axis 1.2 code
 */
public class PrefixedQName implements Name {
    /**
     * comment/shared empty string
     */
    private static final String emptyString = "".intern();

    /**
     * Field prefix
     */
    private String prefix;
    /**
     * Field qName
     */
    private QName qName;

    /**
     * Constructor PrefixedQName
     *
     * @param uri
     * @param localName
     * @param pre
     */
    public PrefixedQName(String uri, String localName, String pre) {
        qName = new QName(uri, localName);
        prefix = (pre == null)
                ? emptyString
                : pre.intern();
    }

    /**
     * Constructor qname
     *
     * @param qname
     * @return
     */
    public PrefixedQName(QName qname) {
        this.qName = qname;
        prefix = emptyString;
    }

    /**
     * Method getLocalName
     *
     * @return
     */
    public String getLocalName() {
        return qName.getLocalPart();
    }

    /**
     * Method getQualifiedName
     *
     * @return
     */
    public String getQualifiedName() {
        StringBuffer buf = new StringBuffer(prefix);
        if (!prefix.equals(emptyString))
            buf.append(':');
        buf.append(qName.getLocalPart());
        return buf.toString();
    }

    /**
     * Method getURI
     *
     * @return
     */
    public String getURI() {
        return qName.getNamespaceURI();
    }

    /**
     * Method getPrefix
     *
     * @return
     */
    public String getPrefix() {
        return prefix;
    }

    /**
     * Method equals
     *
     * @param obj
     * @return
     */
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof PrefixedQName)) {
            return false;
        }
        if (!qName.equals(((PrefixedQName) obj).qName)) {
            return false;
        }
        return prefix.equals(((PrefixedQName) obj).prefix);
    }

    /**
     * Method hasCode
     *
     * @return
     */
    public int hashCode() {
        return prefix.hashCode() + qName.hashCode();
    }

    /**
     * Method toString
     *
     * @return
     */
    public String toString() {
        return qName.toString();
    }
}
