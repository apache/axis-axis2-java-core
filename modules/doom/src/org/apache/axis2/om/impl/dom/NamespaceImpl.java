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
package org.apache.axis2.om.impl.dom;

import org.apache.ws.commons.om.OMNamespace;

public class NamespaceImpl implements OMNamespace {

    private String nsUri;

    private String nsPrefix;

    public NamespaceImpl(String uri) {
        this.nsUri = uri;
    }

    public NamespaceImpl(String uri, String prefix) {
        this.nsUri = uri;
        this.nsPrefix = prefix;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.ws.commons.om.OMNamespace#equals(java.lang.String,
     *      java.lang.String)
     */
    public boolean equals(String uri, String prefix) {
        return (this.nsUri == uri && this.nsPrefix == prefix);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.ws.commons.om.OMNamespace#getPrefix()
     */
    public String getPrefix() {
        return this.nsPrefix;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.ws.commons.om.OMNamespace#getName()
     */
    public String getName() {
        return this.nsUri;
    }

}
