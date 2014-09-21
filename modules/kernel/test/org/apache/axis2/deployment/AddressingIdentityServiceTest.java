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

package org.apache.axis2.deployment;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Map;

import javax.xml.namespace.QName;

import junit.framework.TestCase;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.addressing.AddressingConstants;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.description.AxisEndpoint;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.AxisService2WSDL11;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.description.TransportInDescription;
import org.apache.axis2.description.WSDL11ToAxisServiceBuilder;
import org.apache.axis2.description.java2wsdl.Java2WSDLConstants;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.wsdl.WSDLConstants;

public class AddressingIdentityServiceTest extends TestCase {
    private static final String HTTP_ENDPOINT_NAME = "EchoHttpEndpoint";
    
    private static final String EXPECTED_SPN = "http/server.example.com";
    private static final String EXPECTED_UPN = "bob@EXAMPLE.COM";
    
    private static final String IDENTITY_SERVICE_XML = "test-resources/deployment/AddressingIdentityServiceTest/service.xml";
    private static final String IDENTITY_SERVICE_WSDL = "test-resources/wsdl/identity.wsdl";
                    
    private AxisConfiguration axisConfig;
    private ConfigurationContext configContext;
    

    protected void setUp() throws Exception {
        this.configContext = ConfigurationContextFactory.createEmptyConfigurationContext();
        this.axisConfig = configContext.getAxisConfiguration();
        TransportInDescription httpReceiver = new TransportInDescription("http");
        httpReceiver.setReceiver(new DummyTransportListener());
        this.axisConfig.addTransportIn(httpReceiver);
    }

    /**
     * Tests AxisService construction from a services.xml containing a 
     * {@link AddressingConstants#ADDRESSING_IDENTITY_PARAMETER ADDRESSING_IDENTITY_PARAMETER} parameter
     * containing a &lt;wsa:EndpointReference&gt; element with an UPN identity.
     * The test will then generate the wsdl using {@link AxisService2WSDL11} API and verify that the port
     * contains the &lt;wsa:EndpointReference&gt; extensibility element with the same UPN identity.
     * 
     * @throws Exception
     */
    public void testEndpointReferenceWithUPNIdentityService() throws Exception {
        InputStream in = null;
        try {
            assertNotNull(axisConfig);
            
            AxisService service = new AxisService();

            in = new FileInputStream(IDENTITY_SERVICE_XML);
            ServiceBuilder serviceBuilder = new ServiceBuilder(in, configContext, service);
            service = serviceBuilder.populateService(serviceBuilder.buildOM());
            
            axisConfig.addService(service);

            AxisService2WSDL11 wsdlGenerator = new AxisService2WSDL11(service);
            wsdlGenerator.setCheckIfEndPointActive(false);
            OMElement wsdl = wsdlGenerator.generateOM();
            
            Map<String, AxisEndpoint> endpoints = service.getEndpoints();
            assertEquals(String.format("Expected to find %d endpoints for service %s, but found: %d", 3, service.getName(), endpoints.size()), 3, endpoints.size());
            
            for (Iterator<String> it = endpoints.keySet().iterator(); it.hasNext(); ) {
                AxisEndpoint endpoint = service.getEndpoint(it.next());
                if (HTTP_ENDPOINT_NAME.equals(endpoint.getName())) {
                    //Axis2 does not attach EPRs to http endpoint, therefore we skip it
                    continue;
                }
                OMElement identityElement = checkWsdlContainsIdentityElement(wsdl, service, endpoint);
                
                OMElement upnElement = identityElement.getFirstChildWithName(AddressingConstants.QNAME_IDENTITY_UPN);
                assertNotNull(String.format("Could not find any '%s' claim in Identity element of endpoint '%s': %s",
                    AddressingConstants.QNAME_IDENTITY_UPN, endpoint.getName(), identityElement.toString()), upnElement);
                
                String upn = upnElement.getText();
                assertTrue(String.format("Expected to find UPN of '%s' but got: %s", EXPECTED_UPN, upn),
                    EXPECTED_UPN.equals(upn)); 
            }
        } finally {
            if (in != null) {
                in.close();
            }
        }
    }
    
    /**
     * Tests AxisService construction from a WSDL containing an &lt;wsa:EndpointReference&gt; port
     * extensibility element with an SPN identity.
     * Validates that the AxisEndpoint of the created service contains an 
     * {@link AddressingConstants#ADDRESSING_IDENTITY_PARAMETER ADDRESSING_IDENTITY_PARAMETER} parameter with 
     * an &lt;wsid:Identity&gt; OMElement with the same SPN as in the original wsdl.
     * The test will then re-generate the wsdl using {@link AxisService2WSDL11} API and verify that the port
     * contains the &lt;wsa:EndpointReference&gt; extensibility element with the same SPN identity.
     * 
     * @throws Exception
     */
    public void testEndpointReferenceWithSPNIdentityWSDL() throws Exception {
        InputStream in = null;
        try {
            File wsdlFile = new File(IDENTITY_SERVICE_WSDL);
            in = new FileInputStream(wsdlFile);
            AxisService service = new WSDL11ToAxisServiceBuilder(in).populateService();
            assertNotNull("Could not load AxisService from wsdl: " + wsdlFile.getAbsolutePath(), service);
            
            Map<String, AxisEndpoint> endpoints = service.getEndpoints();
            assertFalse(String.format("No endpoints found for service %s", service.getName()), endpoints.isEmpty());
            
            for (Iterator<String> it = endpoints.keySet().iterator(); it.hasNext(); ) {
                AxisEndpoint endpoint = service.getEndpoint(it.next());
            
                assertNotNull(String.format("Could not find any '%s' endpoint in wsdl: %s", endpoint.getName(),
                    wsdlFile.getAbsolutePath()), endpoint);
                
                Parameter wsIdentityParameter = endpoint.getParameter(AddressingConstants.ADDRESSING_IDENTITY_PARAMETER);
                assertNotNull(String.format("Could not find any '%s' parameter on '%s' endpoint.",
                    AddressingConstants.ADDRESSING_IDENTITY_PARAMETER, endpoint.getName()), wsIdentityParameter);
                
                assertNotNull(String.format("Parameter '%s' parameter on '%s' endpoint has null value.",
                    AddressingConstants.ADDRESSING_IDENTITY_PARAMETER, endpoint.getName()), wsIdentityParameter.getValue());
                
                assertTrue(String.format("Value of parameter '%s' on '%s' endpoint is not an instance of %s but is: %s",
                    AddressingConstants.ADDRESSING_IDENTITY_PARAMETER, endpoint.getName(), OMElement.class, wsIdentityParameter.getValue().getClass().getName()),
                    wsIdentityParameter.getValue() instanceof OMElement);
                
                OMElement identityElement = (OMElement) wsIdentityParameter.getValue();
                OMElement spnElement = identityElement.getFirstChildWithName(AddressingConstants.QNAME_IDENTITY_SPN);
                
                assertNotNull(String.format("Could not find any '%s' child element in Identity element of endpoint '%s': %s",
                    AddressingConstants.QNAME_IDENTITY_SPN, endpoint.getName(), identityElement.toString()), spnElement);
                
                String spn = spnElement.getText();
    
                assertTrue(String.format("Expected to find SPN of '%s' but got: %s", EXPECTED_SPN, spn),
                    EXPECTED_SPN.equals(spn));
            }
            
            axisConfig.addService(service);
            
            AxisService2WSDL11 wsdlGenerator = new AxisService2WSDL11(service);
            OMElement wsdl = wsdlGenerator.generateOM();
            
            endpoints = service.getEndpoints();
            assertFalse(String.format("No endpoints found for service %s", service.getName()), endpoints.isEmpty());
            
            for (Iterator<String> it = endpoints.keySet().iterator(); it.hasNext(); ) {
                AxisEndpoint endpoint = service.getEndpoint(it.next());
                if (HTTP_ENDPOINT_NAME.equals(endpoint.getName())) {
                    //Axis2 does not attach EPRs to http endpoint, therefore we skip it
                    continue;
                }
                OMElement identityElement = checkWsdlContainsIdentityElement(wsdl, service, endpoint);
                
                OMElement spnElement = identityElement.getFirstChildWithName(AddressingConstants.QNAME_IDENTITY_SPN);
                assertNotNull(String.format("Could not find any '%s' element in Identity element of endpoint '%s': %s",
                    AddressingConstants.QNAME_IDENTITY_SPN, endpoint.getName(), identityElement.toString()), spnElement);
                
                String spn = spnElement.getText();
                assertTrue(String.format("Expected to find SPN of '%s' but got: %s", EXPECTED_SPN, spn),
                    EXPECTED_SPN.equals(spn)); 
            }
        } finally {
            if (in != null) {
                in.close();
            }
        }
    }
    
    private OMElement checkWsdlContainsIdentityElement(OMElement wsdl, AxisService service, AxisEndpoint endpoint) {
        QName nameAtt = new QName("", Java2WSDLConstants.ATTRIBUTE_NAME);
        QName serviceQName = new QName(WSDLConstants.WSDL1_1_NAMESPACE, Java2WSDLConstants.SERVICE_LOCAL_NAME);
        
        OMElement serviceElement = wsdl.getFirstChildWithName(serviceQName);
        assertNotNull(String.format("Could not find any '%s' service element in wsdl: \n%s", serviceQName, wsdl.toString()), serviceElement);
        assertEquals("Expected to find a single service with name: " + service.getName(), service.getName(), serviceElement.getAttributeValue(nameAtt));
        
        OMElement portElement = findPort(serviceElement, endpoint.getName());
        assertNotNull(String.format("Could not find any port element with name '%s' in service element: \n%s",
            endpoint.getName(), serviceElement.toString()), portElement);
        
        OMElement eprElement = portElement.getFirstChildWithName(AddressingConstants.Final.WSA_ENDPOINT_REFERENCE);
        assertNotNull(String.format("Could not find any '%s' element in port element: \n%s",
            AddressingConstants.Final.WSA_ENDPOINT_REFERENCE, portElement.toString()), eprElement);
        
        OMElement identityElement = eprElement.getFirstChildWithName(AddressingConstants.QNAME_IDENTITY);
        assertNotNull(String.format("Could not find any '%s' element in EPR element: \n%s",
            AddressingConstants.QNAME_IDENTITY, eprElement.toString(), identityElement));
        
        return identityElement;
    }
    
    private OMElement findPort(OMElement serviceElement, String portName) {
        QName portQName = new QName(WSDLConstants.WSDL1_1_NAMESPACE, Java2WSDLConstants.PORT);
        
        for (@SuppressWarnings("rawtypes")Iterator portIter = serviceElement.getChildrenWithName(portQName); portIter.hasNext(); ) {
            OMElement portElement = (OMElement) portIter.next();
            if (portName.equals(portElement.getAttributeValue(new QName("", Java2WSDLConstants.ATTRIBUTE_NAME)))) {
                return portElement;
            }
        }
        
        return null;
    }
}
