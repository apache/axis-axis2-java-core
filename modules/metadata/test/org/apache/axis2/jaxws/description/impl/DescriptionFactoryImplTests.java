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

import java.lang.reflect.Field;
import java.net.URL;

import javax.xml.namespace.QName;

import junit.framework.TestCase;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.jaxws.ClientConfigurationFactory;
import org.apache.axis2.jaxws.description.ServiceDescription;
import org.apache.axis2.metadata.registry.MetadataFactoryRegistry;

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

    public void testClearServiceDescriptionCache() throws Exception {        
        QName uniqueQName1 = new QName(namespaceURI, localPart + "_testClearCache1");
        QName uniqueQName2 = new QName(namespaceURI, localPart + "_testClearCache2");
  
        // the ClientConfigFactory instance is stored DescriptionFactoryImpl clientConfigFactory 
        // field and for this test we need to clear it, so that a custom version of 
        // ClientConfigurationFactory can be used.
        resetClientConfigFactory();
        
        // install caching factory        
        ClientConfigurationFactory oldFactory = 
            (ClientConfigurationFactory)MetadataFactoryRegistry.getFactory(ClientConfigurationFactory.class);
        CachingClientContextFactory newFactory = new CachingClientContextFactory();
        MetadataFactoryRegistry.setFactory(ClientConfigurationFactory.class, newFactory);
        
        try {
            ServiceDescription desc1 = 
                DescriptionFactoryImpl.createServiceDescription(null, uniqueQName1, ServiceSubclass.class);
                        
            ServiceDescription desc2 = 
                DescriptionFactoryImpl.createServiceDescription(null, uniqueQName1, ServiceSubclass.class);
            
            newFactory.reset();
            
            ServiceDescription desc3 = 
                DescriptionFactoryImpl.createServiceDescription(null, uniqueQName2, ServiceSubclass.class);
            
            assertTrue(desc1 == desc2);
            assertTrue(desc1 != desc3);
                        
            // should clear one
            DescriptionFactoryImpl.clearServiceDescriptionCache(desc2.getAxisConfigContext());
            
            ServiceDescription desc4 = 
                DescriptionFactoryImpl.createServiceDescription(null, uniqueQName1, ServiceSubclass.class);
                        
            ServiceDescription desc5 = 
                DescriptionFactoryImpl.createServiceDescription(null, uniqueQName2, ServiceSubclass.class);
            
            assertTrue(desc1 != desc4);
            assertTrue(desc3 == desc5);
                       
            // should clear both
            DescriptionFactoryImpl.clearServiceDescriptionCache();
            
            ServiceDescription desc6 = 
                DescriptionFactoryImpl.createServiceDescription(null, uniqueQName1, ServiceSubclass.class);
            
            ServiceDescription desc7 = 
                DescriptionFactoryImpl.createServiceDescription(null, uniqueQName2, ServiceSubclass.class);
            
            assertTrue(desc4 != desc6);
            assertTrue(desc3 != desc7);
            
            // this should do nothing
            DescriptionFactoryImpl.clearServiceDescriptionCache(null);
            
        } finally {
            // restore old factory
            MetadataFactoryRegistry.setFactory(ClientConfigurationFactory.class, oldFactory);
        }                          
    }
    
    private void resetClientConfigFactory() throws Exception {
        Field field = DescriptionFactoryImpl.class.getDeclaredField("clientConfigFactory");
        field.setAccessible(true);
        field.set(null, null);
    }
    
    private static class ServiceSubclass extends javax.xml.ws.Service {

        protected ServiceSubclass(URL wsdlDocumentLocation, QName serviceName) {
            super(wsdlDocumentLocation, serviceName);
        }
    }
    
    private static class CachingClientContextFactory extends ClientConfigurationFactory {
        ConfigurationContext context;
        
        public ConfigurationContext getClientConfigurationContext() {
            if (context == null) {
                context = super.getClientConfigurationContext();
            }
            System.out.println(context);
            return context;
        }
        
        public void reset() {
            context = null;
        }
        
    }
}
