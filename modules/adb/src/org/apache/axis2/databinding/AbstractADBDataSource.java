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
package org.apache.axis2.databinding;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMDataSourceExt;
import org.apache.axiom.om.QNameAwareOMDataSource;
import org.apache.axiom.om.ds.AbstractPushOMDataSource;

public abstract class AbstractADBDataSource extends AbstractPushOMDataSource implements QNameAwareOMDataSource {
    protected QName parentQName;

    public AbstractADBDataSource(QName parentQName) {
        this.parentQName = parentQName;
    }

    public final String getLocalName() {
        return parentQName.getLocalPart();
    }

    public final String getNamespaceURI() {
        return parentQName.getNamespaceURI();
    }

    public final String getPrefix() {
        return parentQName.getPrefix();
    }

    /**
     * Returns true if writing the backing object is destructive.
     * An example of an object with a destructive write is an InputStream.
     * The owning OMSourcedElement uses this information to detemine if OM tree
     * expansion is needed when writing the OMDataSourceExt.
     * @return boolean
     */
    public boolean isDestructiveWrite() {
        return false;
    }
    
    public OMDataSourceExt copy() {
        return null;
    }
}
