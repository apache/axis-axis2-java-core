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

import org.apache.axiom.om.OMContainer;
import org.apache.axiom.om.OMDataSource;
import org.apache.axiom.om.OMDocument;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMException;
import org.apache.axiom.om.ds.custombuilder.CustomBuilder;
import org.apache.axiom.soap.SOAPBody;
import org.apache.axis2.jaxws.handler.HandlerUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.bind.JAXBException;

/**
 * JAXBCustomBuilder creates an OMSourcedElement backed by a JAXBDataSource
 * for the specified namespace and localPart.
 */
public class JAXBCustomBuilder implements CustomBuilder, CustomBuilder.Selector {

    private static final Log log = LogFactory.getLog(JAXBCustomBuilder.class);
    
    JAXBDSContext jdsContext;
    
    /**
     * Create a JAXBCustomBuilder
     * @param context JAXBDSContext
     */
    public JAXBCustomBuilder(JAXBDSContext context) {
        super();
        this.jdsContext = context;
        JAXBCustomBuilderMonitor.updateTotalBuilders();
    }

    @Override
    public OMDataSource create(OMElement element) throws OMException {
        try {
            if (log.isDebugEnabled()) {
                log.debug("create namespace = " + element.getNamespaceURI());
                log.debug("  localPart = " + element.getLocalName());
            }
        
            // Create an OMSourcedElement backed by an unmarshalled JAXB object
            
            Object jaxb = jdsContext.unmarshal(element);
            if (log.isDebugEnabled()) {
                log.debug("Successfully unmarshalled jaxb object " + jaxb);
            }
            
            OMDataSource ds = new JAXBDataSource(jaxb, jdsContext);
            if (log.isDebugEnabled()) {
                log.debug("The JAXBDataSource is " + ds);
            }
            JAXBCustomBuilderMonitor.updateTotalCreates();
            return ds;
        } catch (JAXBException e) {
            JAXBCustomBuilderMonitor.updateTotalFailedCreates();
            throw new OMException(e);
        }
    }
    
    @Override
    public boolean accepts(OMContainer parent, int depth, String namespaceURI, String localName) {
        if (parent instanceof OMDocument || parent instanceof SOAPBody) {
            boolean shouldUnmarshal;
            if (HandlerUtils.isHighFidelity(jdsContext.getMessageContext())) {
                log.debug("JAXB payload streaming disabled because high fidelity messages are requested.");
                shouldUnmarshal = false;
            } else {
                // Don't unmarshal if this looks like encrypted data
                shouldUnmarshal = !localName.equals("EncryptedData");
            }
            if (!shouldUnmarshal) {
                JAXBCustomBuilderMonitor.updateTotalFailedCreates();
            }
            return shouldUnmarshal;
        } else {
            return false;
        }
    }
}
