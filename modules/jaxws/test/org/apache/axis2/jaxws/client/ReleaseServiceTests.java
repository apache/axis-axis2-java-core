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
package org.apache.axis2.jaxws.client;

import org.apache.axis2.description.AxisService;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.jaxws.description.DescriptionTestUtils2;
import org.apache.axis2.jaxws.description.EndpointDescription;
import org.apache.axis2.jaxws.description.ServiceDescription;
import org.apache.axis2.jaxws.spi.ClientMetadataTest;
import org.apache.axis2.jaxws.spi.ServiceDelegate;

import javax.jws.WebService;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.WebServiceClient;
import javax.xml.ws.WebServiceException;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

/**
 * Verify that when a Service (i.e. a JAXWS Service Delegate instance) is no longer needed
 * the resources it uses are cleaned up (i.e the EndpointDescription, AxisService, etc).
 * The release can be driven by an explicit proprietary call or via the finalizer on the
 * ServiceDelegate that is run during garbage collection.
 */
public class ReleaseServiceTests extends TestCase {
    static final String namespaceURI = "http://dispatch.client.jaxws.axis2.apache.org";
    static final String svcLocalPart = "svcLocalPart";
    
    static final String dynamicPort1 = "dynamicPort1";
    static final String bindingID1 = null;
    static final String epr1 = null;

    /**
     * When a ServiceDelegate will not be used anymore, a close call on it should release
     * the AxisServices and such it holds. Verify this for dynamic ports.
     */
    public void testServiceReleaseDynamicPort() {
        QName svcQN = new QName(namespaceURI, svcLocalPart);
        try {
            ClientMetadataTest.installCachingFactory();

            Service svc1 = Service.create(svcQN);
            QName portQN = new QName(namespaceURI, dynamicPort1);
            svc1.addPort(portQN, bindingID1, epr1);

            // User internal state to verify the port information before and after the close
            ServiceDelegate delegate = DescriptionTestUtils2.getServiceDelegate(svc1);
            ServiceDescription svcDesc = delegate.getServiceDescription();
            EndpointDescription epDesc= svcDesc.getEndpointDescription(portQN, delegate);
            assertNotNull(epDesc);
            AxisConfiguration axisConfig = svcDesc.getAxisConfigContext().getAxisConfiguration();
            HashMap axisServices = axisConfig.getServices();
            assertEquals(1, axisServices.size());

            org.apache.axis2.jaxws.spi.ServiceDelegate.releaseService(svc1);

            axisServices = axisConfig.getServices();
            assertEquals(0, axisServices.size());

            epDesc= svcDesc.getEndpointDescription(portQN, delegate);
            assertNull(epDesc);                

        } finally {
            ClientMetadataTest.restoreOriginalFactory();
        }
    }

    /**
     * Simple test to create a service, add quite a few dynamic ports under it, then release
     * the service. This test is mostly for debugging the release logic outside the tests that 
     * create a lot of services to test an OOM isn't produced.
     */
    public void testServiceReleaseSingleServiceDescriptionRelease() {
        try {
            ClientMetadataTest.installCachingFactory();

            QName svcQN = new QName(namespaceURI, svcLocalPart);
            Service svc1 = Service.create(svcQN);
            for (int i = 0; i < 100; i++) {
                QName portQN = new QName(namespaceURI, dynamicPort1 + "_" + i);
                svc1.addPort(portQN, bindingID1, epr1);
            }
            org.apache.axis2.jaxws.spi.ServiceDelegate.releaseService(svc1);
        } finally {
            ClientMetadataTest.restoreOriginalFactory();
        }

    }
    
    /**
     * Create a bunch of services with a bunch of ports under each, closing the service
     * before the next one is created.  This should release the resources for that service.  The
     * number of services and ports created is large enough to cause an OOM if the release isn't
     * being done correctly.
     */
    public void testMultipleServiceMultiplePortReleaseLoop() {
        // Create a bunch of different services, make sure the service desc finalizer is called
        try {
            ClientMetadataTest.installCachingFactory();

            for (int i = 0; i < 1000; i++) {
                QName svcQN = new QName(namespaceURI, svcLocalPart + "_" + i);
                Service svc1 = Service.create(svcQN);
                for (int j = 0; j < 200; j++) {
                    QName portQN = new QName(namespaceURI, dynamicPort1 + "_svc_" + i + "_port_" + j);
                    svc1.addPort(portQN, bindingID1, epr1);
                }
                org.apache.axis2.jaxws.spi.ServiceDelegate.releaseService(svc1);
            }
        } finally {
            ClientMetadataTest.restoreOriginalFactory();
        }
    }
    
    /**
     * Test that creating a large number of services and ports, and then having them released by
     * the finalizers during garbage collections does not produce on Out of Memory Error.
     * 
     * NOTE: This test is disabled because forcing garbage collection is an inexact science
     * at best.  You can only ask the JVM to consider doing GC, and that behaves differntly
     * on different JVMS.  So, there's no reliable way to make sure this test runs on various
     * JVMs.  So, it is disabled.  See the test that runns in a similar loop creating lots of 
     * services and ports, and then explicitly calls the relese method.  That test should 
     * reliably and predictably not produce an OOM because of the explicit release call.  
     */
    public void _DISABLED_testServiceReleaseServiceDescriptionFinalizer() {
        // Create a bunch of different services, make sure the service desc finalizer is called
        try {
            ClientMetadataTest.installCachingFactory();

            final int MAX_OOM_COUNT = 5;
            int oomCountService = 0;
            for (int i = 0; i < 1000; i++) {
                try {
                    int oomCount = 0;
                    QName svcQN = new QName(namespaceURI, svcLocalPart + "_" + i);
                    System.out.println("Creating service " + svcQN);
                    Service svc1 = Service.create(svcQN);
                    for (int j = 0; j < 200; j++) {
                        try {
                            QName portQN = new QName(namespaceURI, dynamicPort1 + "_svc_" + i + "_port_" + j);
                            System.out.println("Adding port " + portQN);
                            svc1.addPort(portQN, bindingID1, epr1);
                            // Pause every so often to give the garbage collection thread a chance to run
                            if ((j > 0) && (j % 50) == 0) {
                                System.out.println("Pausing port add for GC to run");
                                Thread.sleep(500);
                            }

                        } catch (OutOfMemoryError e) {
                            System.out.println("Caught OOM number " + ++oomCount);
                            if (oomCount <= MAX_OOM_COUNT) {
                                System.out.println("Sleeping to allow for GC after OOM caught");
                                Thread.sleep(15000);
                                System.out.println("Waking up and focing gc");
                                System.gc();
                                System.out.println("gc() method returned; continuing loop");
                            } else {
                                fail ("Maximum OOM count exceeded " + MAX_OOM_COUNT);
                            }
                        }
                    }
                    // don't call release; the finalizer should do it
//                  org.apache.axis2.jaxws.spi.ServiceDelegate.releaseService(svc1);

                    // Pause to give the garbage collection thread a chance to run
                    System.out.println("Pausing service add for GC to run");
                    Thread.sleep(500);
                } catch (OutOfMemoryError e) {
                    System.out.println("Caught Service OOM number " + ++oomCountService);
                    if (oomCountService <= MAX_OOM_COUNT) {
                        System.out.println("Sleeping to allow for GC after Service OOM caught");
                        Thread.sleep(15000);
                        System.out.println("Waking up and forcing gc");
                        System.gc();
                        System.out.println("Forced gc complete, continuing service loop");
                    } else {
                        fail ("Maximum Servcice OOM count exceeded " + MAX_OOM_COUNT);
                    }
                }
            }

        } catch (Throwable t) {
            System.out.println("Test failed, caught: " + t.toString());
            t.printStackTrace();
            fail("Caught throwable " + t);
        } finally {
            ClientMetadataTest.restoreOriginalFactory();
        }
    }
    
    /**
     * Verify that after a service is released, it can be re-used with the same dynamic ports
     * being added 
     */
    public void testServiceReuseDynamicPort() {
        QName svcQN = new QName(namespaceURI, svcLocalPart);
        try {
            ClientMetadataTest.installCachingFactory();

            Service svc1 = Service.create(svcQN);
            QName portQN = new QName(namespaceURI, dynamicPort1);
            svc1.addPort(portQN, bindingID1, epr1);

            // Use internal state to verify all is well
            ServiceDelegate delegate1 = DescriptionTestUtils2.getServiceDelegate(svc1);
            ServiceDescription svcDesc1 = delegate1.getServiceDescription();
            assertNotNull(svcDesc1);
            EndpointDescription epDesc1= svcDesc1.getEndpointDescription(portQN, delegate1);
            assertNotNull(epDesc1);
            AxisService axisService1 = epDesc1.getAxisService();
            assertNotNull(axisService1);
            AxisConfiguration axisConfig1 = svcDesc1.getAxisConfigContext().getAxisConfiguration();
            HashMap axisServices1 = axisConfig1.getServices();
            assertEquals(1, axisServices1.size());

            // Close the delegate, which should release resources and remove objects from caches
            org.apache.axis2.jaxws.spi.ServiceDelegate.releaseService(svc1);
            
            Service svc2 = Service.create(svcQN);
            svc2.addPort(portQN, bindingID1, epr1);

            // Use internal state to verify all is well; compare to values from the first time
            // around to make sure the cache values for things like the ServiceDescription got 
            // cleared out when the last ServiceDelegate (in the test the only one) relesaed
            // the resources.
            ServiceDelegate delegate2 = DescriptionTestUtils2.getServiceDelegate(svc2);
            assertNotSame(delegate1, delegate2);
            ServiceDescription svcDesc2 = delegate2.getServiceDescription();
            assertNotNull(svcDesc2);
            assertNotSame(svcDesc1, svcDesc2);
            EndpointDescription epDesc2= svcDesc2.getEndpointDescription(portQN, delegate2);
            assertNotNull(epDesc2);
            assertNotSame(epDesc1, epDesc2);
            AxisService axisService2 = epDesc2.getAxisService();
            assertNotNull(axisService2);
            assertNotSame(axisService1, axisService2);
            AxisConfiguration axisConfig2 = svcDesc2.getAxisConfigContext().getAxisConfiguration();
            HashMap axisServices2 = axisConfig2.getServices();
            assertEquals(1, axisServices2.size());
            
            // Verify the service from the map and EndpointDesc are the same
            // Since there's only one element in the map, we can get it directly off the iterator
            assertSame(axisService2, ((Map.Entry) axisServices2.entrySet().iterator().next()).getValue());

        } finally {
            ClientMetadataTest.restoreOriginalFactory();
        }
        
    }
    
    /**
     * Verify that if multiple service are sharing a service description, the release of
     * resources does not happen on the first close.
     */
    public void testMultipleServiceMultiplePortRelease() {
        QName svcQN = new QName(namespaceURI, svcLocalPart);
        try {
            ClientMetadataTest.installCachingFactory();
            Service svc1 = Service.create(svcQN);
            Service svc2 = Service.create(svcQN);

            QName portQN1 = new QName(namespaceURI, dynamicPort1);
            QName portQN2 = new QName(namespaceURI, dynamicPort1 + "_2");
            svc1.addPort(portQN1,bindingID1, epr1);
            svc1.addPort(portQN2, bindingID1, epr1);

            svc2.addPort(portQN1,bindingID1, epr1);
            svc2.addPort(portQN2, bindingID1, epr1);
            
            ServiceDelegate sd1 = DescriptionTestUtils2.getServiceDelegate(svc1);
            ServiceDelegate sd2 = DescriptionTestUtils2.getServiceDelegate(svc2);
            assertNotSame(sd1, sd2);
            
            ServiceDescription svcDesc1 = sd1.getServiceDescription();
            ServiceDescription svcDesc2 = sd2.getServiceDescription();
            AxisConfiguration axisConfig = svcDesc1.getAxisConfigContext().getAxisConfiguration();
            assertSame(svcDesc1, svcDesc2);
            
            EndpointDescription epDesc1_port1 = svcDesc1.getEndpointDescription(portQN1, sd1);
            EndpointDescription epDesc2_port1 = svcDesc1.getEndpointDescription(portQN1, sd2);
            assertSame(epDesc1_port1, epDesc2_port1);
            AxisService axisSvc1_port1 = epDesc1_port1.getAxisService();
            AxisService axisSvc2_port1 = epDesc2_port1.getAxisService();
            assertSame(axisSvc1_port1, axisSvc2_port1);

            EndpointDescription epDesc1_port2 = svcDesc1.getEndpointDescription(portQN2, sd1);
            EndpointDescription epDesc2_port2 = svcDesc1.getEndpointDescription(portQN2, sd2);
            assertSame(epDesc1_port2, epDesc2_port2);
            AxisService axisSvc1_port2 = epDesc1_port2.getAxisService();
            AxisService axisSvc2_port2 = epDesc2_port2.getAxisService();
            assertSame(axisSvc1_port2, axisSvc2_port2);

            // First close should NOT cleanup the endpoints since the other service is
            // still using them.
            org.apache.axis2.jaxws.spi.ServiceDelegate.releaseService(svc1);
            
            ServiceDescription svcDesc2_afterClose = sd2.getServiceDescription();
            assertSame(svcDesc2, svcDesc2_afterClose);
            EndpointDescription epDesc2_port1_afterClose = 
                svcDesc2_afterClose.getEndpointDescription(portQN1, sd2);
            assertSame(epDesc2_port1, epDesc2_port1_afterClose);
            EndpointDescription epDesc2_port2_afterClose = 
                svcDesc2_afterClose.getEndpointDescription(portQN2, sd2);
            assertSame(epDesc2_port2, epDesc2_port2_afterClose);
            
            // Add a third, should use the same
            Service svc3 = Service.create(svcQN);
            svc3.addPort(portQN1,bindingID1, epr1);
            svc3.addPort(portQN2, bindingID1, epr1);
            ServiceDelegate sd3 = DescriptionTestUtils2.getServiceDelegate(svc3);
            assertNotSame(sd2, sd3);
            ServiceDescription svcDesc3 = sd3.getServiceDescription();
            assertSame(svcDesc2_afterClose, svcDesc3);
            EndpointDescription epDesc3_port1 = svcDesc3.getEndpointDescription(portQN1, sd3);
            assertSame(epDesc3_port1, epDesc2_port1_afterClose);
            EndpointDescription epDesc3_port2 = svcDesc3.getEndpointDescription(portQN2, sd3);
            assertSame(epDesc3_port2, epDesc2_port2_afterClose);

            // Close the 2nd delegate and make sure cahced objects are still there
            // since there's a 3rd delegate now
            org.apache.axis2.jaxws.spi.ServiceDelegate.releaseService(svc2);

            ServiceDescription svcDesc3_afterClose = sd3.getServiceDescription();
            assertSame(svcDesc3, svcDesc3_afterClose);
            EndpointDescription epDesc3_port1_afterClose = 
                svcDesc3_afterClose.getEndpointDescription(portQN1, sd3);
            assertSame(epDesc3_port1, epDesc3_port1_afterClose);
            EndpointDescription epDesc3_port2_afterClose = 
                svcDesc3_afterClose.getEndpointDescription(portQN2, sd3);
            assertSame(epDesc3_port2, epDesc3_port2_afterClose);
            
            // Close the last delegate then verify all the services have been removed 
            // from the AxisConfiguration 
            org.apache.axis2.jaxws.spi.ServiceDelegate.releaseService(svc3);
            HashMap axisServices = axisConfig.getServices();
            assertEquals(0, axisServices.size());
            
        } finally {
            ClientMetadataTest.restoreOriginalFactory();
        }
    }
    
    public void testSeviceUseAfterClose() {
        QName svcQN = new QName(namespaceURI, svcLocalPart);
        try {
            ClientMetadataTest.installCachingFactory();

            Service svc1 = Service.create(svcQN);
            QName portQN = new QName(namespaceURI, dynamicPort1);
            svc1.addPort(portQN, bindingID1, epr1);
        
            org.apache.axis2.jaxws.spi.ServiceDelegate.releaseService(svc1);
            
            svc1.addPort(portQN, bindingID1, epr1);
            fail("Should have caught an exception");

        } catch (WebServiceException e) {
            // expected path
        } catch (Exception e) {
            fail("Caught wrong exception " + e);
        } finally {
            ClientMetadataTest.restoreOriginalFactory();
        }
    }

    static final String declared_namespaceURI = "http://description.jaxws.axis2.apache.org";
    static final String declared_svcLocalPart = "svcLocalPart";
    static final String multiPortWsdl = "ClientMetadataMultiPort.wsdl";
    static final String multiPortWsdl_portLocalPart1 = "portLocalPartMulti1";
    static final String multiPortWsdl_portLocalPart2 = "portLocalPartMulti2";
    static final String multiPortWsdl_portLocalPart3 = "portLocalPartMulti3";
    public void testMultipleServiceMultipeDeclaredPorts() {
        QName serviceQName = new QName(declared_namespaceURI , declared_svcLocalPart);
        URL wsdlUrl = getWsdlURL(multiPortWsdl);
        QName portQN1 = new QName(namespaceURI, multiPortWsdl_portLocalPart1);
        QName portQN2 = new QName(namespaceURI, multiPortWsdl_portLocalPart2);
        QName portQN3 = new QName(namespaceURI, multiPortWsdl_portLocalPart3);

        try {
            ClientMetadataTest.installCachingFactory();
            // Open 2 services
            Service svc1 = Service.create(wsdlUrl, serviceQName);
            Service svc2 = Service.create(wsdlUrl, serviceQName);

            ClientMetadataPortSEI svc1_port1 = svc1.getPort(portQN1, ClientMetadataPortSEI.class);
            ClientMetadataPortSEI svc1_port2 = svc1.getPort(portQN2, ClientMetadataPortSEI.class);
            ClientMetadataPortSEI svc1_port3 = svc1.getPort(portQN3, ClientMetadataPortSEI.class);

            ClientMetadataPortSEI svc2_port1 = svc2.getPort(portQN1, ClientMetadataPortSEI.class);
            ClientMetadataPortSEI svc2_port2 = svc2.getPort(portQN2, ClientMetadataPortSEI.class);
            ClientMetadataPortSEI svc2_port3 = svc2.getPort(portQN3, ClientMetadataPortSEI.class);

            ServiceDelegate sd1 = DescriptionTestUtils2.getServiceDelegate(svc1);
            ServiceDelegate sd2 = DescriptionTestUtils2.getServiceDelegate(svc2);
            assertNotSame(sd1, sd2);

            ServiceDescription svcDesc1 = sd1.getServiceDescription();
            ServiceDescription svcDesc2 = sd2.getServiceDescription();
            AxisConfiguration axisConfig = svcDesc1.getAxisConfigContext().getAxisConfiguration();
            assertSame(svcDesc1, svcDesc2);
            
            EndpointDescription epDesc1_port1 = svcDesc1.getEndpointDescription(portQN1, sd1);
            EndpointDescription epDesc2_port1 = svcDesc1.getEndpointDescription(portQN1, sd2);
            assertSame(epDesc1_port1, epDesc2_port1);
            AxisService axisSvc1_port1 = epDesc1_port1.getAxisService();
            AxisService axisSvc2_port1 = epDesc2_port1.getAxisService();
            assertSame(axisSvc1_port1, axisSvc2_port1);

            EndpointDescription epDesc1_port2 = svcDesc1.getEndpointDescription(portQN2, sd1);
            EndpointDescription epDesc2_port2 = svcDesc1.getEndpointDescription(portQN2, sd2);
            assertSame(epDesc1_port2, epDesc2_port2);
            AxisService axisSvc1_port2 = epDesc1_port2.getAxisService();
            AxisService axisSvc2_port2 = epDesc2_port2.getAxisService();
            assertSame(axisSvc1_port2, axisSvc2_port2);

            // First close should NOT cleanup the endpoints since the other service is
            // still using them.
            org.apache.axis2.jaxws.spi.ServiceDelegate.releaseService(svc1);
            
            ServiceDescription svcDesc2_afterClose = sd2.getServiceDescription();
            assertSame(svcDesc2, svcDesc2_afterClose);
            EndpointDescription epDesc2_port1_afterClose = 
                svcDesc2_afterClose.getEndpointDescription(portQN1, sd2);
            assertSame(epDesc2_port1, epDesc2_port1_afterClose);
            EndpointDescription epDesc2_port2_afterClose = 
                svcDesc2_afterClose.getEndpointDescription(portQN2, sd2);
            assertSame(epDesc2_port2, epDesc2_port2_afterClose);

            // Add a third, should use the same
            Service svc3 = Service.create(wsdlUrl, serviceQName);
            ClientMetadataPortSEI svc3_port1 = svc3.getPort(portQN1, ClientMetadataPortSEI.class);
            ClientMetadataPortSEI svc3_port2 = svc3.getPort(portQN2, ClientMetadataPortSEI.class);
            ServiceDelegate sd3 = DescriptionTestUtils2.getServiceDelegate(svc3);
            assertNotSame(sd2, sd3);
            ServiceDescription svcDesc3 = sd3.getServiceDescription();
            assertSame(svcDesc2_afterClose, svcDesc3);
            EndpointDescription epDesc3_port1 = svcDesc3.getEndpointDescription(portQN1, sd3);
            assertSame(epDesc3_port1, epDesc2_port1_afterClose);
            EndpointDescription epDesc3_port2 = svcDesc3.getEndpointDescription(portQN2, sd3);
            assertSame(epDesc3_port2, epDesc2_port2_afterClose);

            // Close the 2nd delegate and make sure cahced objects are still there
            // since there's a 3rd delegate now
            org.apache.axis2.jaxws.spi.ServiceDelegate.releaseService(svc2);

            ServiceDescription svcDesc3_afterClose = sd3.getServiceDescription();
            assertSame(svcDesc3, svcDesc3_afterClose);
            EndpointDescription epDesc3_port1_afterClose = 
                svcDesc3_afterClose.getEndpointDescription(portQN1, sd3);
            assertSame(epDesc3_port1, epDesc3_port1_afterClose);
            EndpointDescription epDesc3_port2_afterClose = 
                svcDesc3_afterClose.getEndpointDescription(portQN2, sd3);
            assertSame(epDesc3_port2, epDesc3_port2_afterClose);
            
            // Close the last delegate then verify all the services have been removed 
            // from the AxisConfiguration 
            org.apache.axis2.jaxws.spi.ServiceDelegate.releaseService(svc3);
            HashMap axisServices = axisConfig.getServices();
            assertEquals(0, axisServices.size());

        } finally {
            ClientMetadataTest.restoreOriginalFactory();
        }

    }

    static final String GENERATED_SERVICE_WSDL = "ClientMetadata.wsdl";
    static final String GENERATED_SERVICE_NS = "http://description.jaxws.axis2.apache.org";
    static final String GENERATED_SERVICE_LP = "svcLocalPart";
    
    public void testGeneratedServiceRelease() {
        try {
            ClientMetadataTest.installCachingFactory();
            
            ClientMetadataGeneratedService genSvc = new ClientMetadataGeneratedService();
            assertNotNull(genSvc);
            ClientMetadataPortSEI port = genSvc.getPort(ClientMetadataPortSEI.class);
            assertNotNull(port);
            
            // User internal state to verify the port information before and after the close
            ServiceDelegate delegate = DescriptionTestUtils2.getServiceDelegate(genSvc);
            ServiceDescription svcDesc = delegate.getServiceDescription();
            EndpointDescription[] epDescArray= svcDesc.getEndpointDescriptions();
            assertNotNull(epDescArray);
            assertEquals(1, epDescArray.length);
            
            AxisConfiguration axisConfig = svcDesc.getAxisConfigContext().getAxisConfiguration();
            HashMap axisServices = axisConfig.getServices();
            assertEquals(1, axisServices.size());

            org.apache.axis2.jaxws.spi.ServiceDelegate.releaseService(genSvc);

            axisServices = axisConfig.getServices();
            assertEquals(0, axisServices.size());

            epDescArray= svcDesc.getEndpointDescriptions();
            assertEquals(0, epDescArray.length);                
        } finally {
            ClientMetadataTest.restoreOriginalFactory();
        }
    }
    
    public void testGeneratedServiceRelaseLoop() {
        // Create a bunch of different services, make sure the service desc finalizer is called
        try {
            ClientMetadataTest.installCachingFactory();

            for (int i = 0; i < 1000; i++) {
                ClientMetadataGeneratedService genSvc = new ClientMetadataGeneratedService();
                ClientMetadataPortSEI port = genSvc.getPort(ClientMetadataPortSEI.class);
                org.apache.axis2.jaxws.spi.ServiceDelegate.releaseService(genSvc);
            }
        } finally {
            ClientMetadataTest.restoreOriginalFactory();
        }
    }

    // =============================================================================================
    // Utility methods
    // =============================================================================================
    /**
     * Given a simple file name (with no base dictory or path), returns a URL to the WSDL file
     * with the base directory and path prepended.
     * 
     * @param wsdlFileName
     * @return
     */
    static URL getWsdlURL(String wsdlFileName) {
        URL url = null;
        String wsdlLocation = getWsdlLocation(wsdlFileName);
        try {
            File file = new File(wsdlLocation);
            url = file.toURL();
        } catch (MalformedURLException e) {
            e.printStackTrace();
            fail("Exception converting WSDL file to URL: " + e.toString());
        }
        return url;
    }

    /**
     * Prepends the base directory and the path where the test WSDL lives to a filename.
     * @param wsdlFileName
     * @return
     */
    static String getWsdlLocation(String wsdlFileName) {
        String wsdlLocation = null;
        String baseDir = System.getProperty("basedir",".");
        wsdlLocation = baseDir + "/test-resources/wsdl/" + wsdlFileName;
        return wsdlLocation;
    }
}

@WebService(name="EchoMessagePortType", targetNamespace="http://description.jaxws.axis2.apache.org")
interface ClientMetadataPortSEI {
    public String echoMessage(String string);
}

@WebServiceClient()
class ClientMetadataGeneratedService extends javax.xml.ws.Service {
    public ClientMetadataGeneratedService() {
        super(ReleaseServiceTests.getWsdlURL(ReleaseServiceTests.GENERATED_SERVICE_WSDL),
              new QName(ReleaseServiceTests.GENERATED_SERVICE_NS, ReleaseServiceTests.GENERATED_SERVICE_LP));
    }
    public ClientMetadataGeneratedService(URL wsdlLocation, QName serviceName) {
        super(wsdlLocation, serviceName);
    }
}
