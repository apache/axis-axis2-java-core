/*
 * Copyright 2005 The Apache Software Foundation.
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
package org.apache.axis2.om.util;

import org.apache.axis2.om.OMElement;
import org.apache.axis2.om.OMNamespace;

import javax.xml.namespace.QName;

/**
 * helper class to provide extra utility stuff against elements.
 * The code is designed to work with any element implementation.
 */

public class ElementHelper {

    private OMElement element;

    /**
     * construct and bind to an element
     * @param element element to work with
     */
    public ElementHelper(OMElement element) {
        this.element = element;
    }

    /**
     * Turn a prefix:local qname string into a proper QName, evaluating it in the OMElement context
     *
     * @param qname                    qname to resolve
     * @param defaultToParentNameSpace flag that controls behaviour when there is no namespace.
     * @return null for any failure to extract a qname.
     */
    public QName resolveQName(String qname, boolean defaultToParentNameSpace) {
        int colon = qname.indexOf(':');
        if (colon < 0) {
            if (defaultToParentNameSpace) {
                //get the parent ns and use it for the child
                OMNamespace namespace = element.getNamespace();
                return new QName(namespace.getName(), qname, namespace.getPrefix());
            } else {
                //else things without no prefix are local.
                return new QName(qname);
            }
        }
        String prefix = qname.substring(0, colon);
        String local = qname.substring(colon + 1);
        if (local.length() == 0) {
            //empy local, exit accordingly
            return null;
        }

        OMNamespace namespace = element.findNamespace(null, prefix);
        if (namespace == null) {
            return null;
        }
        return new QName(namespace.getName(), local, prefix);
    }

    /**
     * Turn a prefix:local qname string into a proper QName, evaluating it in the OMElement context
     * unprefixed qnames resolve to the local namespace
     *
     * @param qname prefixed qname string to resolve
     * @return null for any failure to extract a qname.
     */
    public QName resolveQName(String qname) {
        return resolveQName(qname, true);
    }

}
