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
package org.apache.axis2.jaxws.client.dispatch;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.jaxws.ClientConfigurationFactory;
import org.apache.axis2.jaxws.description.DescriptionTestUtils2;
import org.apache.axis2.jaxws.description.ServiceDescription;
import org.apache.axis2.jaxws.spi.ServiceDelegate;
import org.apache.axis2.metadata.registry.MetadataFactoryRegistry;
import org.apache.axis2.engine.AxisConfigurator;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.AxisFault;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import junit.framework.TestCase;

/**
 * Tests the caching and isolation of dynamic ports,i.e. those created with
 * Service.addPort(...).  Dynamic ports should
 * 1) Only be visible to services on which an addPort was done
 * 2) Share instances of the description objects (e.g. AxisService) for ports
 * added to different instances of the same service that use the same client
 * configuration
 * 3) Identical ports on services using different client configuration should
 * not be shared 
 */
public class DynamicPortCachingTests extends TestCase {
    static final String namespaceURI = "http://dispatch.client.jaxws.axis2.apache.org";
    static final String svcLocalPart = "svcLocalPart";
    
    static final String dynamicPort1 = "dynamicPort1";
    static final String bindingID1 = null;
    static final String epr1 = null;

    /**
     * Two different instances of the same service should share the same
     * description information (e.g. AxisService) if the same port is added
     * to both 
     */
    public void _testSamePortsSameService() {
        try {
            installCachingFactory();
            QName svcQN = new QName(namespaceURI, svcLocalPart);
            
            Service svc1 = Service.create(svcQN);
            assertNotNull(svc1);
            ServiceDelegate svcDlg1 = DescriptionTestUtils2.getServiceDelegate(svc1);
            assertNotNull(svcDlg1);
            ServiceDescription svcDesc1 = svcDlg1.getServiceDescription();
            assertNotNull(svcDesc1);

            Service svc2 = Service.create(svcQN);
            assertNotNull(svc2);
            ServiceDelegate svcDlg2 = DescriptionTestUtils2.getServiceDelegate(svc2);
            assertNotNull(svcDlg2);
            ServiceDescription svcDesc2 = svcDlg2.getServiceDescription();
            assertNotNull(svcDesc2);
            
            assertNotSame("Service instances should not be the same", svc1, svc2);
            assertNotSame("Service delegates should not be the same", svcDlg1, svcDlg2);
            assertSame("Instance of ServiceDescription should be the same", svcDesc1, svcDesc2);
            
            // Add a port to 1st service, should not be visible under the 2nd service
            svc1.addPort(new QName(namespaceURI, dynamicPort1),
                         bindingID1,
                         epr1);
            assertEquals(1, getList(svc1.getPorts()).size());
            assertEquals(0, getList(svc2.getPorts()).size());
            
            // Add the same port to 2nd service, should now have same ports and description
            // objects
            svc2.addPort(new QName(namespaceURI, dynamicPort1),
                         bindingID1,
                         epr1);
            assertEquals(1, getList(svc1.getPorts()).size());
            assertEquals(1, getList(svc2.getPorts()).size());
            
            
            
            
        } finally {
            restoreOriginalFactory();
        }
        
    }
    
    public void testAddPortOOM() {
        System.out.println("testAddPortOOM");

        ClientConfigurationFactory oldFactory = setClientConfigurationFactory();
        QName svcQN = new QName(namespaceURI, svcLocalPart);
        
        try {
            for (int i = 0; i < 5000 ; i++) {
                Service svc1 = Service.create(svcQN);
                System.out.println("Port number " + i);
                svc1.addPort(new QName(namespaceURI, dynamicPort1 + "_" /*+ i*/),
                             bindingID1,
                             epr1);
            }
        } catch (Throwable t) {
            fail("Caught throwable " + t);
        } finally {
            MetadataFactoryRegistry.setFactory(ClientConfigurationFactory.class, oldFactory);
        }
    }

    private ClientConfigurationFactory setClientConfigurationFactory() {
        ClientConfigurationFactory oldFactory = (ClientConfigurationFactory) MetadataFactoryRegistry.getFactory(ClientConfigurationFactory.class);
        ClientConfigurationFactory factory = new ClientConfigurationFactory(new AxisConfigurator() {
            public AxisConfiguration getAxisConfiguration()  {
                try {
                    return ConfigurationContextFactory.createDefaultConfigurationContext().getAxisConfiguration();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            public void loadServices() {
            }
            public void engageGlobalModules() throws AxisFault {
            }
            public void cleanup() {
            }
        });
        MetadataFactoryRegistry.setFactory(ClientConfigurationFactory.class, factory);
        return oldFactory;
    }

    private List getList(Iterator it) {
        List returnList = new ArrayList();
        while (it != null && it.hasNext()) {
            returnList.add(it.next());
        }
        return returnList;
    }
    
    /**
     * Methods to install a client configuration factory that will return the same AxisConfiguration
     * each time.  This is used so that the ServiceDescriptions will be cached in the DescriptionFactory.
     * 
     * IMPORTANT!!!
     * If you install a caching factory, you MUST restore the original factory before your test
     * exits, otherwise it will remain installed when subsequent tests run and cause REALLY STRANGE
     * failures.  Use restoreOriginalFactory() INSIDE A finally() block to restore the factory.
     */
    static private ClientConfigurationFactory originalFactory = null;
    static void installCachingFactory() {
        // install caching factory
        if (originalFactory != null) {
            throw new UnsupportedOperationException("Attempt to install the caching factory when the original factory has already been overwritten");
        }
        originalFactory = 
            (ClientConfigurationFactory)MetadataFactoryRegistry.getFactory(ClientConfigurationFactory.class);
        DynamicPortCachingClientContextFactory newFactory = new DynamicPortCachingClientContextFactory();
        MetadataFactoryRegistry.setFactory(ClientConfigurationFactory.class, newFactory);
        resetClientConfigFactory();
    }
    static void restoreOriginalFactory() {
        if (originalFactory == null) {
            throw new UnsupportedOperationException("Attempt to restore original factory to a null value");
        }
        MetadataFactoryRegistry.setFactory(ClientConfigurationFactory.class, originalFactory);
        resetClientConfigFactory();
        originalFactory = null;
    }
    static void resetClientConfigFactory() {
//        Field field;
//        try {
//            field = DescriptionFactoryImpl.class.getDeclaredField("clientConfigFactory");
//            field.setAccessible(true);
//            field.set(null, null);
//        } catch (Exception e) {
//            throw new UnsupportedOperationException("Unable to reset client config factory; caught " + e);
//        }
    }
    
}

class DynamicPortCachingClientContextFactory extends ClientConfigurationFactory {
    ConfigurationContext context;
    
    public ConfigurationContext getClientConfigurationContext() {
        if (context == null) {
            context = super.getClientConfigurationContext();
        }
        System.out.println("Test version of DynamicPortCachingClientContextFactory: " + context);
        return context;
    }
    
    public void reset() {
        context = null;
    }
}


