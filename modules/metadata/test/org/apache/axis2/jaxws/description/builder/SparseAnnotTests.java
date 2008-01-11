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
package org.apache.axis2.jaxws.description.builder;

import org.apache.axis2.jaxws.description.DescriptionFactory;
import org.apache.axis2.jaxws.description.DescriptionTestUtils;
import org.apache.axis2.jaxws.description.ServiceDescription;

import javax.xml.namespace.QName;
import javax.xml.ws.WebServiceClient;

import java.net.URL;

import junit.framework.TestCase;

/**
 * 
 */
public class SparseAnnotTests extends TestCase {
    private String namespaceURI = "http://org.apache.axis2.jaxws.description.builder.SparseAnnotTests";
    private String svcLocalPart = "svcLocalPart";

    public void testNoSparseAnnot() {
        QName serviceQName = new QName(namespaceURI, svcLocalPart);
        ServiceDescription svcDesc = 
            DescriptionFactory.createServiceDescription(null, serviceQName, 
                                                        SparseAnnotServiceSubclass.class);
        assertNotNull(svcDesc);
        // A DBC will be created for us
        DescriptionBuilderComposite svcDescComposite = DescriptionTestUtils.getServiceDescriptionComposite(svcDesc);
        assertNotNull(svcDescComposite);
        WebServiceClient wsClient = svcDescComposite.getWebServiceClientAnnot();
        // There is no DBC Annot in this case; it is the class annotation but it should be 
        // returned as an instance of a WebServiceClientAnnot
        assertTrue(wsClient instanceof WebServiceClientAnnot);
    }
    
    public void testAssociatedClass() {
        QName serviceQName = new QName(namespaceURI, svcLocalPart);
        // Create a composite with a WebServiceClient override of the WSDL location.
        DescriptionBuilderComposite composite = new DescriptionBuilderComposite();
        String overridenWsdlLocation = DescriptionTestUtils.getWSDLLocation("ClientEndpointMetadata.wsdl");
        WebServiceClientAnnot wsClientAnno = 
            WebServiceClientAnnot.createWebServiceClientAnnotImpl(null, null, overridenWsdlLocation);
        composite.setWebServiceClientAnnot(wsClientAnno);
        Object compositeKey = "CompositeKey";
        ServiceDescription svcDesc = 
            DescriptionFactory.createServiceDescription(null, serviceQName, 
                                                        SparseAnnotServiceSubclass.class, 
                                                        composite, compositeKey);
        assertNotNull(svcDesc);
        DescriptionBuilderComposite svcDescComposite = DescriptionTestUtils.getServiceDescriptionComposite(svcDesc);
        assertNotNull(svcDescComposite);
        assertNotSame(composite, svcDescComposite);
        assertSame(SparseAnnotServiceSubclass.class, svcDescComposite.getCorrespondingClass());
        assertSame(composite, svcDescComposite.getSparseComposite(compositeKey));
        WebServiceClient wsClient = svcDescComposite.getWebServiceClientAnnot();
        assertTrue(wsClient instanceof WebServiceClientAnnot);
    }

}

@WebServiceClient(targetNamespace="originalTNS", wsdlLocation="originalWsdlLocation")
class SparseAnnotServiceSubclass extends javax.xml.ws.Service {

    protected SparseAnnotServiceSubclass(URL wsdlDocumentLocation, QName serviceName) {
        super(wsdlDocumentLocation, serviceName);
    }
}
