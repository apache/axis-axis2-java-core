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

package org.apache.axis2.jaxws.description.impl;

import java.net.URL;

import javax.xml.namespace.QName;

import junit.framework.TestCase;

import org.apache.axis2.jaxws.description.ServiceDescription;

public class DescriptionFactoryImplTests extends TestCase {
    
    private static final String namespaceURI =
            "http://org.apache.axis2.jaxws.description.ServiceDescriptionTests";
    private static final String localPart = "EchoService";
    private static final QName serviceQName = new QName(namespaceURI, localPart);

    public void testServiceDescriptionCaching() {        
        QName uniqueQName = new QName(namespaceURI, localPart + "_testValidServiceSubclass");
        
        ServiceDescription desc1 = 
            DescriptionFactoryImpl.createServiceDescription(null, uniqueQName, ServiceSubclass.class);
                    
        /*
        int size = 5;
        ServiceDescription desc2; 
        for (int i = 0; i < size; i++) {
            desc2 = 
                DescriptionFactoryImpl.createServiceDescription(null, uniqueQName, ServiceSubclass.class);
            assertTrue("service description was not reused", desc1 == desc2);
        } 
        */     
    }   

    private static class ServiceSubclass extends javax.xml.ws.Service {

        protected ServiceSubclass(URL wsdlDocumentLocation, QName serviceName) {
            super(wsdlDocumentLocation, serviceName);
        }
    }
}
