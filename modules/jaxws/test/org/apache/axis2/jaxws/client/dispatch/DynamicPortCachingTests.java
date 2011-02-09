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

import junit.framework.TestCase;

import org.apache.axis2.AxisFault;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.jaxws.Constants;
import org.apache.axis2.jaxws.description.DescriptionTestUtils2;
import org.apache.axis2.jaxws.description.EndpointDescription;
import org.apache.axis2.jaxws.description.ServiceDescription;
import org.apache.axis2.jaxws.description.builder.MDQConstants;
import org.apache.axis2.jaxws.description.impl.ServiceDescriptionImpl;
import org.apache.axis2.jaxws.spi.ClientMetadataTest;
import org.apache.axis2.jaxws.spi.ServiceDelegate;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Tests the caching and isolation of dynamic ports,i.e. those created with
 * Service.addPort(...).  Dynamic ports should
 * 1) Only be visible to the service instances on which an addPort was done
 * 2) Share instances of the description objects (e.g. AxisService) for ports
 * added to the same instance of a service
 * 3) Different service instances of the same-named service should not share the 
 * the list of added dynamic ports.  Even if the same named port is added to each
 * service, they should not share metadata objects (e.g. EndpointDescription, AxisService) 
 * 
 * Also validate the property that enables the previous behavior that allowed
 * sharing of dyamic ports across services on the same AxisConfiguration.  With
 * that property enabled, Dynamic ports should
 * 1) Be shared across all services on the AxisConfiguration based on the key
 * (PortQName, BindingId, EndpointAddress).
 */
public class DynamicPortCachingTests extends TestCase {
    static final String namespaceURI = "http://dispatch.client.jaxws.axis2.apache.org";
    static final String svcLocalPart = "svcLocalPart";
    
    static final String dynamicPort1 = "dynamicPort1";
    static final String bindingID1 = null;
    static final String epr1 = null;

    /**
     * Validate setting the property enables the old behavior, which is that dynamic ports are 
     * shared across all services on an AxisConfiguration based on the key
     * (PortQName, BindingId, EndpointAddress).  This test validates that two ports that share the
     * same Service QName will be share the same dynamic port objects.
     * 
     * NOTE!!! This test exists for validating backwards compatability.  This behavior was NOT
     * intended in the runtime, but since it existed that way, customers could be depending on it.
     */
    public void testSamePortsSameServiceName_AxisConfig_PropertyTrue() {
        try {
            ClientMetadataTest.installCachingFactory();
            QName svcQN = new QName(namespaceURI, svcLocalPart);
            
            Service svc1 = Service.create(svcQN);
            assertNotNull(svc1);
            ServiceDelegate svcDlg1 = DescriptionTestUtils2.getServiceDelegate(svc1);
            assertNotNull(svcDlg1);
            ServiceDescription svcDesc1 = svcDlg1.getServiceDescription();
            assertNotNull(svcDesc1);

            // Set the property to revert the behavior.  Note that although we are passing ni 
            // a particular service, the property is set on the AxisConfig shared by all 
            // services.
            setAxisConfigParameter(svc1, MDQConstants.SHARE_DYNAMIC_PORTS_ACROSS_SERVICES, "true");

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
            
            // Make sure the EndpointDescription objects are shared.
            Collection<?> epDesc1Collection = 
                ((ServiceDescriptionImpl) svcDesc1).getDynamicEndpointDescriptions_AsCollection(svcDlg1);
            Collection<?> epDesc2Collection = 
                ((ServiceDescriptionImpl) svcDesc2).getDynamicEndpointDescriptions_AsCollection(svcDlg2);
            assertEquals("Wrong number of dynamic endpoints", 1, epDesc1Collection.size());
            assertEquals("Wrong number of dynamic endpoints", 1, epDesc2Collection.size());
           
            EndpointDescription epDesc1 = (EndpointDescription) epDesc1Collection.toArray()[0];
            EndpointDescription epDesc2 = (EndpointDescription) epDesc2Collection.toArray()[0];
            assertSame("EndpointDescriptions not shared", epDesc1, epDesc2);
            
        } finally {
            ClientMetadataTest.restoreOriginalFactory();
        }
        
    }
    /**
     * Validate setting the property enables the old behavior, which is that dynamic ports are 
     * shared across all services on an AxisConfiguration based on the key
     * (PortQName, BindingId, EndpointAddress).  This test validates that two ports that have
     * different Service QNames will still share the same dynamic port objects.  
     * 
     * NOTE!!! This test exists for validating backwards compatability.  This behavior was NOT
     * intended in the runtime, but since it existed that way, customers could be depending on it.
     */
    public void testSamePortsDifferentServiceName_AxisConfig_PropertyTrue() {
        try {
            ClientMetadataTest.installCachingFactory();
            QName svcQN = new QName(namespaceURI, svcLocalPart);
            
            Service svc1 = Service.create(svcQN);
            assertNotNull(svc1);
            ServiceDelegate svcDlg1 = DescriptionTestUtils2.getServiceDelegate(svc1);
            assertNotNull(svcDlg1);
            ServiceDescription svcDesc1 = svcDlg1.getServiceDescription();
            assertNotNull(svcDesc1);

            // Set the property to revert the behavior.  Note that although we are passing ni 
            // a particular service, the property is set on the AxisConfig shared by all 
            // services.
            setAxisConfigParameter(svc1, MDQConstants.SHARE_DYNAMIC_PORTS_ACROSS_SERVICES, "true");

            QName svcQN2 = new QName(namespaceURI, svcLocalPart + "2");
            Service svc2 = Service.create(svcQN2);
            assertNotNull(svc2);
            ServiceDelegate svcDlg2 = DescriptionTestUtils2.getServiceDelegate(svc2);
            assertNotNull(svcDlg2);
            ServiceDescription svcDesc2 = svcDlg2.getServiceDescription();
            assertNotNull(svcDesc2);
            
            assertNotSame("Service instances should not be the same", svc1, svc2);
            assertNotSame("Service delegates should not be the same", svcDlg1, svcDlg2);
            assertNotSame("Instance of ServiceDescription should be the same", svcDesc1, svcDesc2);
            
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
            
            // Make sure the EndpointDescription objects are shared.
            Collection<?> epDesc1Collection = 
                ((ServiceDescriptionImpl) svcDesc1).getDynamicEndpointDescriptions_AsCollection(svcDlg1);
            Collection<?> epDesc2Collection = 
                ((ServiceDescriptionImpl) svcDesc2).getDynamicEndpointDescriptions_AsCollection(svcDlg2);
            assertEquals("Wrong number of dynamic endpoints", 1, epDesc1Collection.size());
            assertEquals("Wrong number of dynamic endpoints", 1, epDesc2Collection.size());
           
            EndpointDescription epDesc1 = (EndpointDescription) epDesc1Collection.toArray()[0];
            EndpointDescription epDesc2 = (EndpointDescription) epDesc2Collection.toArray()[0];
            assertSame("EndpointDescriptions not shared", epDesc1, epDesc2);
            
        } finally {
            ClientMetadataTest.restoreOriginalFactory();
        }
        
    }
    /**
     * Validate that without the property set to revert the behavior, the default is that the ports are
     * NOT shared across different instances of services with the same name 
     */
    public void testSamePortsSameServiceNameDifferentInstances() {
        try {
            ClientMetadataTest.installCachingFactory();
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
            
            // Make sure the EndpointDescription objects are not shared.
            Collection<?> epDesc1Collection = 
                ((ServiceDescriptionImpl) svcDesc1).getDynamicEndpointDescriptions_AsCollection(svcDlg1);
            Collection<?> epDesc2Collection = 
                ((ServiceDescriptionImpl) svcDesc2).getDynamicEndpointDescriptions_AsCollection(svcDlg2);
            assertEquals("Wrong number of dynamic endpoints", 1, epDesc1Collection.size());
            assertEquals("Wrong number of dynamic endpoints", 1, epDesc2Collection.size());
           
            EndpointDescription epDesc1 = (EndpointDescription) epDesc1Collection.toArray()[0];
            EndpointDescription epDesc2 = (EndpointDescription) epDesc2Collection.toArray()[0];
            assertNotSame("EndpointDescriptions not shared", epDesc1, epDesc2);
            
        } finally {
            ClientMetadataTest.restoreOriginalFactory();
        }
        
    }
    
    /**
     * Validate that adding the same dynamic port to the same service instance re-uses the same
     * description objects (e.g. EndpointDescription) 
     */
    public void testSamePortsSameServiceInstance() {
        try {
            ClientMetadataTest.installCachingFactory();
            QName svcQN = new QName(namespaceURI, svcLocalPart);
            
            Service svc1 = Service.create(svcQN);
            assertNotNull(svc1);
            ServiceDelegate svcDlg1 = DescriptionTestUtils2.getServiceDelegate(svc1);
            assertNotNull(svcDlg1);
            ServiceDescription svcDesc1 = svcDlg1.getServiceDescription();
            assertNotNull(svcDesc1);

            // Add a port to service, save off the metadata to validate later
            svc1.addPort(new QName(namespaceURI, dynamicPort1),
                         bindingID1,
                         epr1);
            assertEquals(1, getList(svc1.getPorts()).size());
            Collection<?> epDesc1Collection = 
                ((ServiceDescriptionImpl) svcDesc1).getDynamicEndpointDescriptions_AsCollection(svcDlg1);
            assertEquals("Wrong number of dynamic endpoints", 1, epDesc1Collection.size());
            EndpointDescription epDescFirstAddPort = (EndpointDescription) epDesc1Collection.toArray()[0];

            // Add the same port to the same service instance, should use the same description objects
            svc1.addPort(new QName(namespaceURI, dynamicPort1),
                         bindingID1,
                         epr1);
            assertEquals(1, getList(svc1.getPorts()).size());
            
            // Make sure the EndpointDescription object is reused for second port.
            Collection<?> epDesc2Collection = 
                ((ServiceDescriptionImpl) svcDesc1).getDynamicEndpointDescriptions_AsCollection(svcDlg1);
            assertEquals("Wrong number of dynamic endpoints", 1, epDesc2Collection.size());
           
            EndpointDescription epDescSecondAddPort = (EndpointDescription) epDesc1Collection.toArray()[0];
            assertSame("EndpointDescriptions not reused", epDescFirstAddPort, epDescSecondAddPort);
            
        } finally {
            ClientMetadataTest.restoreOriginalFactory();
        }
        
    }
    /**
     * Validate that ports added to services with different service names (and thus different service instances)
     * are not shared.
     */
    public void testSamePortsDifferentServiceNames() {
        try {
            ClientMetadataTest.installCachingFactory();
            QName svcQN = new QName(namespaceURI, svcLocalPart);
            Service svc1 = Service.create(svcQN);
            assertNotNull(svc1);
            ServiceDelegate svcDlg1 = DescriptionTestUtils2.getServiceDelegate(svc1);
            assertNotNull(svcDlg1);
            ServiceDescription svcDesc1 = svcDlg1.getServiceDescription();
            assertNotNull(svcDesc1);

            QName svcQN2 = new QName(namespaceURI, svcLocalPart + "2");
            Service svc2 = Service.create(svcQN2);
            assertNotNull(svc2);
            ServiceDelegate svcDlg2 = DescriptionTestUtils2.getServiceDelegate(svc2);
            assertNotNull(svcDlg2);
            ServiceDescription svcDesc2 = svcDlg2.getServiceDescription();
            assertNotNull(svcDesc2);
            
            assertNotSame("Service instances should not be the same", svc1, svc2);
            assertNotSame("Service delegates should not be the same", svcDlg1, svcDlg2);
            assertNotSame("Instance of ServiceDescription should not be the same", svcDesc1, svcDesc2);
            
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
            
            // Make sure the EndpointDescription objects are NOT shared.
            Collection<?> epDesc1Collection = 
                ((ServiceDescriptionImpl) svcDesc1).getDynamicEndpointDescriptions_AsCollection(svcDlg1);
            Collection<?> epDesc2Collection = 
                ((ServiceDescriptionImpl) svcDesc2).getDynamicEndpointDescriptions_AsCollection(svcDlg2);
            assertEquals("Wrong number of dynamic endpoints", 1, epDesc1Collection.size());
            assertEquals("Wrong number of dynamic endpoints", 1, epDesc2Collection.size());
           
            EndpointDescription epDesc1 = (EndpointDescription) epDesc1Collection.toArray().clone()[0];
            EndpointDescription epDesc2 = (EndpointDescription) epDesc2Collection.toArray().clone()[0];
            assertNotSame("EndpointDescriptions should not be shared across different services", epDesc1, epDesc2);
            
        } finally {
            ClientMetadataTest.restoreOriginalFactory();
        }
        
    }
    
    public void testAddPortOOM() {
        QName svcQN = new QName(namespaceURI, svcLocalPart);
        try {
            ClientMetadataTest.installCachingFactory();
            for (int i = 0; i < 5000 ; i++) {
                Service svc1 = Service.create(svcQN);
                svc1.addPort(new QName(namespaceURI, dynamicPort1 + "_" /*+ i*/),
                             bindingID1,
                             epr1);
            }
        } catch (Throwable t) {
            fail("Caught throwable " + t);
        } finally {
            ClientMetadataTest.restoreOriginalFactory();
        }
    }

    private List getList(Iterator it) {
        List returnList = new ArrayList();
        while (it != null && it.hasNext()) {
            returnList.add(it.next());
        }
        return returnList;
    }
    
    private void setAxisConfigParameter(Service service, String key, String value) {
        ServiceDelegate delegate = DescriptionTestUtils2.getServiceDelegate(service);
        ServiceDescription svcDesc = delegate.getServiceDescription();
        AxisConfiguration axisConfig = svcDesc.getAxisConfigContext().getAxisConfiguration();
        Parameter parameter = new Parameter(key, value);
        try {
            axisConfig.addParameter(parameter);
        } catch (AxisFault e) {
            fail("Unable to set Parameter on AxisConfig due to exception " + e);
        }
    }

}