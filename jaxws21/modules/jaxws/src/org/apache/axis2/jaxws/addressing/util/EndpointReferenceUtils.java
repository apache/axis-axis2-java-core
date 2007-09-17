/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 *      
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.axis2.jaxws.addressing.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.addressing.EndpointReferenceHelper;
import org.apache.axis2.jaxws.addressing.factory.Axis2EndpointReferenceFactory;
import org.apache.axis2.jaxws.addressing.factory.JAXWSEndpointReferenceFactory;
import org.apache.axis2.jaxws.registry.FactoryRegistry;
import org.apache.axis2.util.XMLUtils;
import org.w3c.dom.Element;

public final class EndpointReferenceUtils {
    
    private static OMFactory omFactory = OMAbstractFactory.getOMFactory();
    private static JAXWSEndpointReferenceFactory jaxwsEPRFactory =
        (JAXWSEndpointReferenceFactory) FactoryRegistry.getFactory(JAXWSEndpointReferenceFactory.class);
    private static Axis2EndpointReferenceFactory axis2EPRFactory =
    	(Axis2EndpointReferenceFactory) FactoryRegistry.getFactory(Axis2EndpointReferenceFactory.class);

    private EndpointReferenceUtils() {
    }

    /**
     * Convert from a {@link EndpointReference} to a
     * subclass of {@link javax.xml.ws.EndpointReference}.
     * 
     * @param <T>
     * @param axis2EPR
     * @param addressingNamespace
     * @return
     * @throws AxisFault
     */
    public static javax.xml.ws.EndpointReference convertFromAxis2(EndpointReference axis2EPR, String addressingNamespace)
    throws AxisFault, Exception {
        QName qname = new QName(addressingNamespace, "EndpointReference", "wsa");
        OMElement omElement =
            EndpointReferenceHelper.toOM(omFactory, axis2EPR, qname, addressingNamespace);
        Element eprElement = XMLUtils.toDOM(omElement);
        Source eprInfoset = new DOMSource(eprElement);
        
        return jaxwsEPRFactory.createEndpointReference(eprInfoset, addressingNamespace);
    }
    
    /**
     * Convert from a {@link javax.xml.ws.EndpointReference} to a an instance of
     * {@link EndpointReference}.
     * 
     * @param jaxwsEPR
     * @return
     * @throws AxisFault
     * @throws XMLStreamException
     */
    public static EndpointReference convertToAxis2(javax.xml.ws.EndpointReference jaxwsEPR)
    throws AxisFault, XMLStreamException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        jaxwsEPR.writeTo(new StreamResult(baos));
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        OMElement eprElement = (OMElement) XMLUtils.toOM(bais);
        
        return EndpointReferenceHelper.fromOM(eprElement);
    }

    public static String getAddressingNamespace(Class clazz) {
        return jaxwsEPRFactory.getAddressingNamespace(clazz);
    }
    
    public static EndpointReference createAxis2EndpointReference(String address, QName serviceName, QName portName, String wsdlDocumentLocation, String addressingNamespace) {
    	return axis2EPRFactory.createEndpointReference(address, serviceName, portName, wsdlDocumentLocation, addressingNamespace);
    }
    
    public static EndpointReference createAxis2EndpointReference(String address) {
    	return axis2EPRFactory.createEndpointReference(address);
    }
    
    public static void addReferenceParameters(EndpointReference axis2EPR, Element...referenceParameters)
    throws Exception {
        if (referenceParameters != null) {
            for (Element element : referenceParameters) {
                OMElement omElement = XMLUtils.toOM(element);
                axis2EPR.addReferenceParameter(omElement);
            }            
        }    	
    }
    
    public static void addMetadata(EndpointReference axis2EPR, Element...metadata)
    throws Exception {
        if (metadata != null) {
            for (Element element : metadata) {
                OMElement omElement = XMLUtils.toOM(element);
                axis2EPR.addMetaData(omElement);
            }
        }
    }
}
