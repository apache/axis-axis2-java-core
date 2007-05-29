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

import java.util.List;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.addressing.EndpointReferenceHelper;
import org.apache.axis2.addressing.metadata.ServiceName;
import org.apache.axis2.addressing.metadata.WSDLLocation;

public final class EndpointReferenceBuilder {
    
    public EndpointReferenceBuilder() {
    }
    
    public static EndpointReference createEndpointReference(String address) {
        return new EndpointReference(address);
    }
    
    public static EndpointReference createEndpointReference(QName serviceName, QName endpoint, String wsdlDocumentLocation) {
        return null;
    }
    
    public static EndpointReference createEndpointReference(String address,
                                                            QName serviceName,
                                                            QName endpoint,
                                                            List<OMElement> metadata,
                                                            String wsdlDocumentLocation,
                                                            List<OMElement> referenceParameters,
                                                            String addressingNamespace) throws AxisFault {
        EndpointReference epr = new EndpointReference(address);
        ServiceName service = new ServiceName(serviceName, endpoint.getLocalPart());
        WSDLLocation wsdlLocation = new WSDLLocation(endpoint.getNamespaceURI(), wsdlDocumentLocation);
        EndpointReferenceHelper.setServiceNameMetadata(epr, addressingNamespace, service);
        EndpointReferenceHelper.setWSDLLocationMetadata(epr, addressingNamespace, wsdlLocation);

        return epr;
    }
}
