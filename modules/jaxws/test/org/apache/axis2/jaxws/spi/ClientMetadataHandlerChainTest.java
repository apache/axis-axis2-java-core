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
package org.apache.axis2.jaxws.spi;

import org.apache.axis2.jaxws.description.builder.DescriptionBuilderComposite;
import org.apache.axis2.jaxws.description.impl.DescriptionUtils;
import org.apache.axis2.jaxws.description.xml.handler.HandlerChainsType;

import javax.jws.WebService;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.HandlerResolver;
import javax.xml.ws.handler.PortInfo;
import javax.xml.ws.soap.SOAPBinding;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import junit.framework.TestCase;

/**
 * Verify that handler chains specified using the HandlerChainsType in a sparse
 * composite are correctly applied to Services and Ports on the client requester. 
 */
public class ClientMetadataHandlerChainTest extends TestCase {
    
    private String namespaceURI = "http://www.apache.org/test/namespace";
    private String svcLocalPart = "DummyService";
    private String portLocalPart = "DummyPort";
    
    /**
     *  Test creating a service without a sparse composite.  This verifies pre-existing default
     *  behavior.
     */
    public void testServiceAndPortNoComposite() {
        QName serviceQName = new QName(namespaceURI, svcLocalPart);
        QName portQName = new QName(namespaceURI, portLocalPart);

        Service service = Service.create(serviceQName);
        HandlerResolver resolver = service.getHandlerResolver();
        assertNotNull(resolver);
        PortInfo pi = new DummyPortInfo();
        List<Handler> list = resolver.getHandlerChain(pi);
        assertEquals(0, list.size());
        
        ClientMetadataHandlerChainTestSEI port = service.getPort(portQName, ClientMetadataHandlerChainTestSEI.class);
        // Verify that ports created under the service have no handlers from the sparse composite
        BindingProvider bindingProvider = (BindingProvider) port;
        Binding binding = (Binding) bindingProvider.getBinding();
        List<Handler> portHandlers = binding.getHandlerChain();
        assertEquals(0, portHandlers.size());
    }
    
    
    /**
     * Test creating a service with a sparse composite that contains handler configuration
     * information for this service delegate.  Verify that the handlers are included in the 
     * chain.
     */
    public void testServiceWithComposite() {
        QName serviceQName = new QName(namespaceURI, svcLocalPart);
        QName portQName = new QName(namespaceURI, portLocalPart);
        // Create a composite with a JAXB Handler Config 
        DescriptionBuilderComposite sparseComposite = new DescriptionBuilderComposite();
        HandlerChainsType handlerChainsType = getHandlerChainsType();
        sparseComposite.setHandlerChainsType(handlerChainsType);

        ServiceDelegate.setServiceMetadata(sparseComposite);
        Service service = Service.create(serviceQName);
        ClientMetadataHandlerChainTestSEI port = service.getPort(portQName, ClientMetadataHandlerChainTestSEI.class);

        // Verify the HandlerResolver on the service knows about the handlers in the sparse composite
        HandlerResolver resolver = service.getHandlerResolver();
        assertNotNull(resolver);
        PortInfo pi = new DummyPortInfo();
        List<Handler> list = resolver.getHandlerChain(pi);
        assertEquals(2, list.size());
        
        // Verify that ports created under the service have handlers
        BindingProvider bindingProvider = (BindingProvider) port;
        Binding binding = (Binding) bindingProvider.getBinding();
        List<Handler> portHandlers = binding.getHandlerChain();
        assertEquals(2, portHandlers.size());
        assertTrue(containSameHandlers(portHandlers, list));
        
        // Verify that a subsequent port are different and that they also gets the correct handlers
        ClientMetadataHandlerChainTestSEI port2 = service.getPort(portQName, ClientMetadataHandlerChainTestSEI.class);
        BindingProvider bindingProvider2 = (BindingProvider) port2;
        Binding binding2 = (Binding) bindingProvider2.getBinding();
        List<Handler> portHandlers2 = binding2.getHandlerChain();
        assertNotSame(port, port2);
        assertEquals(2, portHandlers2.size());
        assertTrue(containSameHandlers(portHandlers2, list));
    }
    
    public void testPortWithComposite() {
        QName serviceQName = new QName(namespaceURI, svcLocalPart);
        QName portQName = new QName(namespaceURI, portLocalPart);

        Service service = Service.create(serviceQName);
        
        // Create a composite with a JAXB Handler Config 
        DescriptionBuilderComposite sparseComposite = new DescriptionBuilderComposite();
        HandlerChainsType handlerChainsType = getHandlerChainsType();
        sparseComposite.setHandlerChainsType(handlerChainsType);
        ServiceDelegate.setPortMetadata(sparseComposite);
        ClientMetadataHandlerChainTestSEI port = service.getPort(portQName, ClientMetadataHandlerChainTestSEI.class);

        // Verify the HandlerResolver on the service knows about the handlers in the sparse composite
        HandlerResolver resolver = service.getHandlerResolver();
        assertNotNull(resolver);
        PortInfo pi = new DummyPortInfo();
        List<Handler> list = resolver.getHandlerChain(pi);
        assertEquals(2, list.size());
        
        // Verify that the port created with the sparse metadata has those handlers
        BindingProvider bindingProvider = (BindingProvider) port;
        Binding binding = (Binding) bindingProvider.getBinding();
        List<Handler> portHandlers = binding.getHandlerChain();
        assertEquals(2, portHandlers.size());
        assertTrue(containSameHandlers(portHandlers, list));
        
        // Verify that a creating another instance of the same port also gets those handlers
        ClientMetadataHandlerChainTestSEI port2 = service.getPort(portQName, ClientMetadataHandlerChainTestSEI.class);
        BindingProvider bindingProvider2 = (BindingProvider) port2;
        Binding binding2 = (Binding) bindingProvider2.getBinding();
        List<Handler> portHandlers2 = binding2.getHandlerChain();
        assertNotSame(port, port2);
        assertEquals(2, portHandlers2.size());
        assertTrue(containSameHandlers(portHandlers2, list));
        
        // Verify that createing a different port doesn't get the handlers
        QName portQName3 = new QName(namespaceURI, portLocalPart + "3");
        ClientMetadataHandlerChainTestSEI port3 = service.getPort(portQName3, ClientMetadataHandlerChainTestSEI.class);
        BindingProvider bindingProvider3 = (BindingProvider) port3;
        Binding binding3 = (Binding) bindingProvider3.getBinding();
        List<Handler> portHandlers3 = binding3.getHandlerChain();
        assertEquals(0, portHandlers3.size());
        
        // Verify setting the metadata on a port (with a different name) will get handlers.
        QName portQName4 = new QName(namespaceURI, portLocalPart + "4");
        ServiceDelegate.setPortMetadata(sparseComposite);
        ClientMetadataHandlerChainTestSEI port4 = service.getPort(portQName4, ClientMetadataHandlerChainTestSEI.class);
        BindingProvider bindingProvider4 = (BindingProvider) port4;
        Binding binding4 = (Binding) bindingProvider4.getBinding();
        List<Handler> portHandlers4 = binding4.getHandlerChain();
        assertEquals(2, portHandlers4.size());
        
        // Verify the service handler resolver knows about boths sets of handlers
        // attached to the two different port QNames and none are attached for the third port
        List<Handler> listForPort = resolver.getHandlerChain(pi);
        assertEquals(2, listForPort.size());

        PortInfo pi4 = new DummyPortInfo(portQName4);
        List<Handler> listForPort4 = resolver.getHandlerChain(pi4);
        assertEquals(2, listForPort4.size());
        
        PortInfo pi3 = new DummyPortInfo(portQName3);
        List<Handler> listForPort3 = resolver.getHandlerChain(pi3);
        assertEquals(0, listForPort3.size());
    }
    
    // TODO: (JLB) Change this test to check the handlers on the ports via the bindingImpl
    public void _testMultipleServiceDelgates() {
        QName serviceQName = new QName(namespaceURI, svcLocalPart);
        PortInfo pi = new DummyPortInfo();

        // Create a composite with a JAXB Handler Config 
        DescriptionBuilderComposite sparseComposite = new DescriptionBuilderComposite();
        HandlerChainsType handlerChainsType = getHandlerChainsType();
        sparseComposite.setHandlerChainsType(handlerChainsType);

        ServiceDelegate.setServiceMetadata(sparseComposite);
        Service service = Service.create(serviceQName);
        
        // No ports created yet, so there should be no relevant handler chains
        HandlerResolver resolver0 = service.getHandlerResolver();
        List<Handler> list0 = resolver0.getHandlerChain(pi);
        assertEquals(0, list0.size());
        
        QName portQName1 = new QName(namespaceURI, portLocalPart);
        ClientMetadataHandlerChainTestSEI port1 = service.getPort(portQName1, ClientMetadataHandlerChainTestSEI.class);
        HandlerResolver resolver1 = service.getHandlerResolver();
        assertNotNull(resolver1);
        List<Handler> list1 = resolver1.getHandlerChain(pi);
        assertEquals(2, list1.size());

        QName portQName2 = new QName(namespaceURI, "NoHandlerPort");
        ClientMetadataHandlerChainTestSEI port2 = service.getPort(portQName2, ClientMetadataHandlerChainTestSEI.class);
        HandlerResolver resolver2 = service.getHandlerResolver();
        assertNotNull(resolver2);
        // Use the same PortInfo from above
        // TODO: (JLB) this is returning the resolver on the service, which has the correct port from above.
        // the real test is that the handlers don't get attached to this port, which means writing some handlers
        // or maybe checking the handler chain on the binding impl!
        List<Handler> list2 = resolver2.getHandlerChain(pi);
        assertEquals(0, list2.size());
        
        QName portQName3 = new QName(namespaceURI, portLocalPart);
        ClientMetadataHandlerChainTestSEI port3 = service.getPort(portQName1, ClientMetadataHandlerChainTestSEI.class);
        HandlerResolver resolver3 = service.getHandlerResolver();
        assertNotNull(resolver3);
        List<Handler> list3 = resolver3.getHandlerChain(pi);
        assertEquals(2, list3.size());
        
    }
    
    /**
     * Answer if two List<Handler> arguments contain the same handler Class files.
     * @param list1
     * @param list2
     * @return
     */
    private boolean containSameHandlers(List<Handler> list1, List<Handler> list2) {
        if (list1.size() != list2.size()) {
            return false;
        }

        Iterator<Handler> list1Iterator = list1.iterator();
        ArrayList<Class> list1Handlers = new ArrayList<Class>();
        while (list1Iterator.hasNext()) {
            list1Handlers.add(list1Iterator.next().getClass());
        }
        Iterator<Handler> list2Iterator = list2.iterator();
        ArrayList<Class> list2Handlers = new ArrayList<Class>();
        while (list2Iterator.hasNext()) {
            list2Handlers.add(list2Iterator.next().getClass());
        }

        if (list1Handlers.containsAll(list2Handlers)) {
            return true;
        } else {
            return false;
        }
            
    }
    
    private HandlerChainsType getHandlerChainsType() {
        InputStream is = getXMLFileStream();
        assertNotNull(is);
        HandlerChainsType returnHCT = DescriptionUtils.loadHandlerChains(is, this.getClass().getClassLoader());
        assertNotNull(returnHCT);
        return returnHCT;
    }
    private InputStream getXMLFileStream() {
        InputStream is = null;
        String configLoc = null;
        try {
            String sep = "/";
            configLoc = sep + "test-resources" + sep + "configuration" + sep + "handlers" + sep + "handler.xml";
            String baseDir = new File(System.getProperty("basedir",".")).getCanonicalPath();
            is = new File(baseDir + configLoc).toURL().openStream();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return is;
    }

    public class DummyPortInfo implements PortInfo {
        private QName portQN;
        private QName serviceQN;
        
        public DummyPortInfo() {
            this.portQN = new QName("http://www.apache.org/test/namespace", "DummyPort");
            this.serviceQN = new QName("http://www.apache.org/test/namespace", "DummyService");
        }
        
        public DummyPortInfo(QName portQN) {
            this();
            this.portQN = portQN;
        }

        public String getBindingID() {
            return SOAPBinding.SOAP11HTTP_BINDING;
        }

        public QName getPortName() {
            return portQN;
        }
        
        public QName getServiceName() {
            return serviceQN;
        }
    }

}

@WebService
interface ClientMetadataHandlerChainTestSEI {
    public String echo(String toEcho);
}
