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

package org.apache.axis2.datasource.jaxb;


import org.apache.axiom.om.OMDataSourceExt;
import org.apache.axiom.om.ds.AbstractPushOMDataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 * OMDataSource backed by a jaxb object
 */
public class JAXBDataSource extends AbstractPushOMDataSource {
    
    private static final Log log = LogFactory.getLog(JAXBDataSource.class);
    
    private final Object jaxb;
    private final JAXBDSContext context;

    public JAXBDataSource(Object jaxb, JAXBDSContext context) {
        this.jaxb = jaxb;
        this.context = context;
    }

    public OMDataSourceExt copy() {
        return new JAXBDataSource(jaxb, context);
    }

    public Object getObject() {
        return jaxb;
    }
    
    public JAXBDSContext getContext() {
        return context;
    }

    public void serialize(XMLStreamWriter xmlWriter) throws XMLStreamException {
        try {
            context.marshal(jaxb, xmlWriter);
        } catch (JAXBException je) {
            if (log.isDebugEnabled()) {
                try {
                    log.debug("JAXBContext for marshal failure:" + 
                              context.getJAXBContext(context.getClassLoader()));
                } catch (Exception e) {
                }
            }
            throw new XMLStreamException(je);
        }
    }

    public boolean isDestructiveWrite() {
        return false;
    }
}
