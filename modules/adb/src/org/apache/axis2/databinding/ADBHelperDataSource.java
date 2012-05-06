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

import org.apache.axiom.om.OMDataSource;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

public class ADBHelperDataSource<T> extends AbstractADBDataSource {
    private T bean;
    private final ADBHelper<T> helper;

    /**
     * Constructor taking in an ADBBean
     *
     * @param bean
     */
    public ADBHelperDataSource(T bean, QName parentQName, ADBHelper<T> helper) {
        super(parentQName);
        this.bean = bean;
        this.helper = helper;
    }

    /**
     * This needs to be generated inside the ADB bean
     *
     * @param xmlWriter
     * @throws XMLStreamException
     * @see OMDataSource#serialize(javax.xml.stream.XMLStreamWriter)
     */
    public void serialize(XMLStreamWriter xmlWriter) throws XMLStreamException{
        helper.serialize(bean, parentQName, xmlWriter);
        xmlWriter.flush();
    }

    /**
     * Returns the backing Object.
     * @return Object
     */
    public Object getObject() {
        return bean;
    }
    
    /**
     * Close the DataSource and free its resources.
     */
    public void close() {
        parentQName = null;
        bean = null;
    }
}
