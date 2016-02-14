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
package org.apache.axis2.description;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.wsdl.xml.WSDLLocator;
import javax.xml.namespace.QName;

import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.wsdl.WSDLConstants;
import org.xml.sax.InputSource;

import junit.framework.TestCase;

public class WSDL11ToAxisServiceBuilderTest extends TestCase {
    /**
     * Tests processing of an operation that declares multiple faults referring to the same message.
     * In this case, {@link WSDL11ToAxisServiceBuilder} must correctly populate the
     * {@link AxisMessage} object for both faults. In particular,
     * {@link AxisMessage#getElementQName()} must return consistent information. This is a
     * regression test for AXIS2-4533.
     * 
     * @throws Exception
     */
    public void testMultipleFaultsWithSameMessage() throws Exception {
        InputStream in = new FileInputStream("test-resources/wsdl/faults.wsdl");
        try {
            AxisService service = new WSDL11ToAxisServiceBuilder(in).populateService();
            AxisOperation operation = service.getOperation(new QName("urn:test", "test"));
            assertNotNull(operation);
            List<AxisMessage> faultMessages = operation.getFaultMessages();
            assertEquals(2, faultMessages.size());
            AxisMessage error1 = faultMessages.get(0);
            AxisMessage error2 = faultMessages.get(1);
            assertEquals("errorMessage", error1.getName());
            assertEquals("errorMessage", error2.getName());
            assertEquals(new QName("urn:test", "error"), error1.getElementQName());
            assertEquals(new QName("urn:test", "error"), error2.getElementQName());
        } finally {
            in.close();
        }
    }
    
    private AxisService populateAxisService(AxisConfiguration axisConf, File wsdlFile) throws IOException {
        InputStream in = null;
        try {
            in = new FileInputStream(wsdlFile);
            WSDL11ToAxisServiceBuilder wsdl11Builder = new WSDL11ToAxisServiceBuilder(in);
            if (axisConf != null) {
            	wsdl11Builder.useAxisConfiguration(axisConf);
            }
            AxisService service = wsdl11Builder.populateService();
            assertNotNull("Could not load AxisService from wsdl: " + wsdlFile.getAbsolutePath(), service);
            
            return service;
        } finally {
            if (in != null) {
                in.close();
            }
        }
    }
    
    private void checkOperationActions(AxisService service, AxisOperation operation, String expectedInputAction, String expectedOutputAction, String expectedFaultAction) {
        assertEquals(String.format("Expected that operation '%s' of service '%s' defines an input action of '%s' but it defines '%s' instead.", operation.getName(), service.getName(), expectedInputAction, operation.getInputAction()), expectedInputAction, operation.getInputAction());
        assertEquals(String.format("Expected that operation '%s' of service '%s' defines an output action of '%s' but it defines '%s' instead.", operation.getName(), service.getName(), expectedOutputAction, operation.getOutputAction()), expectedOutputAction, operation.getOutputAction());
        assertEquals(String.format("Expected that operation '%s' of service '%s' defines an input action of '%s' but it defines '%s' instead.", operation.getName(), service.getName(), expectedFaultAction, operation.getFaultAction()), expectedFaultAction, operation.getFaultAction());
        
        ArrayList<String> wsaMappingList = operation.getWSAMappingList();
        assertEquals(String.format("Expected that operation '%s' of service '%s' has a 'wsaMappingList' of size '%d' but size is '%d' instead.", operation.getName(), service.getName(), 1, wsaMappingList.size()), 1, wsaMappingList.size());
        assertTrue(String.format("Expected that the 'wsaMappingList' of operation '%s' of service '%s' contains an entry of '%s' but it does not.", operation.getName(), service.getName(), expectedInputAction), wsaMappingList.contains(expectedInputAction));
        
        assertSame(String.format("Expected that 'operationsAliasesMap' of service '%s' contains a '%s' operation for action '%s'.", service.getName(), operation.getName(), expectedInputAction), service.getOperationByAction(expectedInputAction), operation);
    }
    
    public void testGetVersionActions() throws IOException {
    	AxisConfiguration axisConf = new AxisConfiguration();
        AxisService service = populateAxisService(axisConf, new File("test-resources/wsdl/Version.wsdl"));
        
        String[] operationNames = new String[] { 
        		"getVersionWSAW", "getVersionWSA", "getVersionWSAM", "getVersionWSAWSubmission", 
        		"getVersionWSAWURI", "getVersionWSAURI", "getVersionWSAMURI", "getVersionWSAWSubmissionURI" 
        };
        
        for (String operationName : operationNames) {
	        QName operationQName = new QName("http://axisversion.sample", operationName);
	        AxisOperation operation = service.getOperation(operationQName);
	        assertNotNull(String.format("Could not find AxisOperation '%s' in service: %s", operationQName.toString(), service.getName()), operation);
	
	        String prefix = operationName.endsWith("URI") ? "ns:Version" : "http://axisversion.sample";
	        String expectedInputAction = String.format("%s/VersionPortType/%sRequest", prefix, operationQName.getLocalPart());
	        String expectedOutputAction = String.format("%s/VersionPortType/%sResponse", prefix, operationQName.getLocalPart());
	        String expectedFaultAction = String.format("%s/VersionPortType/Fault/%sException", prefix, operationQName.getLocalPart());
	        
	        checkOperationActions(service, operation, expectedInputAction,expectedOutputAction,expectedFaultAction);
        }
    }
    
    /**
     * Tests parsing of <code>EchoService.wsdl</code> into an AxisService instance. The <code>EchoService.wsdl</code>
     * imports <code>EchoBindings.wsdl</code>, which contains the binding definitions (note that it does not reverse-import the EchoService.wsdl).
     * Parsing the wsdl should cause neither a "There is no port type associated with the binding" exception, nor a "Cannot determine the MEP" exception,
     * provided that Axis2 does not recursively search the port type in all imported wsdls, but looks it up via {@link javax.wsdl.Definition#getPortType(QName)}, 
     * falling back to {@link javax.wsdl.Binding#getPortType()} if not available (will be the case when port type is imported through another document but not directly)
     *
     * The test verifies that no exceptions are thrown and additionally checks that the endpoint, binding, operation and its in/out messages are correctly populated from the WSDL definition.
     * @throws IOException
     */
    public void testImportedBindings() throws IOException {
        AxisConfiguration axisConf = new AxisConfiguration();
        WSDLLocator wsdlLocator = new CustomWSDLLocator("test-resources/wsdl/imports/binding/", "EchoService.wsdl");
        AxisService service = populateAxisService(axisConf, new File(wsdlLocator.getBaseURI()), wsdlLocator);
        assertValidEchoService(service, wsdlLocator);
    }
    
    /**
     * Tests parsing of <code>EchoService.wsdl</code> into an AxisService instance. The <code>EchoService.wsdl</code>
     * imports <code>EchoBindings.wsdl</code>, which reverse-imports the EchoService.wsdl (i.e. we have a circular import).
     * Parsing the wsdl should cause neither a "There is no port type associated with the binding" exception, nor a "Cannot determine the MEP" exception,
     * provided that Axis2 does not recursively search the port type in all imported wsdls, but looks it up via {@link javax.wsdl.Definition#getPortType(QName)}, 
     * falling back to {@link javax.wsdl.Binding#getPortType()} if not available (will be the case when port type is imported through another document but not directly)
     *
     * The test verifies that no exceptions are thrown and additionally checks that the endpoint, binding, operation and its in/out messages are correctly populated from the WSDL definition.
     * @throws IOException
     */
    public void testCircularImportedBindings() throws IOException {
        AxisConfiguration axisConf = new AxisConfiguration();
        WSDLLocator wsdlLocator = new CustomWSDLLocator("test-resources/wsdl/imports/binding_recursive/", "EchoService.wsdl");
        AxisService service = populateAxisService(axisConf, new File(wsdlLocator.getBaseURI()), wsdlLocator);
        assertValidEchoService(service, wsdlLocator);
    }
    
    /**
     * Tests parsing of <code>EchoService.wsdl</code> into an AxisService instance. The <code>EchoService.wsdl</code>
     * imports <code>EchoBindings.wsdl</code>, which in turn imports the <code>EchoPortType.wsdl</code>.
     * Parsing the wsdl should cause neither a "There is no port type associated with the binding" exception, nor a "Cannot determine the MEP" exception,
     * provided that Axis2 does not recursively search the port type in all imported wsdls, but looks it up via {@link javax.wsdl.Definition#getPortType(QName)}, 
     * falling back to {@link javax.wsdl.Binding#getPortType()} if not available (will be the case when port type is imported through another document but not directly)
     *
     * The test verifies that no exceptions are thrown and additionally checks that the endpoint, binding, operation and its in/out messages are correctly populated from the WSDL definition.
     * @throws IOException
     */
    public void testImportedPortType() throws IOException {
        AxisConfiguration axisConf = new AxisConfiguration();
        WSDLLocator wsdlLocator = new CustomWSDLLocator("test-resources/wsdl/imports/portType/", "EchoService.wsdl");
        AxisService service = populateAxisService(axisConf, new File(wsdlLocator.getBaseURI()), wsdlLocator);
        assertValidEchoService(service, wsdlLocator);
    }
    
    private void assertValidEchoService(AxisService echoService, WSDLLocator wsdlLocator) {
        //check soap12 endpoint and binding are available
        String endpointName = "EchoServiceHttpSoap12Endpoint";
        AxisEndpoint soap12Endpoint = echoService.getEndpoint(endpointName);
        assertNotNull(String.format("Cannot find %s endpoint in wsdl definition: %s", endpointName, wsdlLocator.getBaseURI()), soap12Endpoint);
        
        AxisBinding soap12Binding = soap12Endpoint.getBinding();
        assertNotNull(String.format("Binding not set on %s endpoint in wsdl definition: %s", endpointName, wsdlLocator.getBaseURI()), soap12Binding);
        
        //check that policy reference is present on the binding subject
        String bindingPolicyRefId = "#basicAuthPolicy";
        assertNotNull(String.format("Cannot find policy reference %s on binding %s", bindingPolicyRefId, soap12Binding.getName()),
                soap12Binding.getPolicySubject().getAttachedPolicyComponent(bindingPolicyRefId));
        
        //check that binding operation and respective operation are available
        QName echoBindingOpName = new QName("http://tempuri.org/bindings", "echo");
        AxisBindingOperation echoBindingOp = (AxisBindingOperation) soap12Binding.getChild(echoBindingOpName);
        assertNotNull(String.format("Cannot find %s binding operation on binding %s",  echoBindingOpName, soap12Binding), echoBindingOp);
        
        AxisOperation echoOp = echoBindingOp.getAxisOperation();
        assertNotNull(String.format("Operation not set on binding operation %s",  echoBindingOp.getName()), echoOp);
        
        //check that operation style is correctly identified and an operation with respective mep is created
        assertTrue(String.format("Operation %s is not an instance of %s", echoOp.getName(), InOutAxisOperation.class.getName()), echoOp instanceof InOutAxisOperation);
        assertEquals(String.format("Operation %s specifies an unexpected MEP uri: %s", echoOp.getName(), echoOp.getMessageExchangePattern()), 
                WSDL2Constants.MEP_URI_IN_OUT, echoOp.getMessageExchangePattern());
        
        //check in/out messages
        AxisMessage inMessage =  echoOp.getMessage(WSDLConstants.MESSAGE_LABEL_IN_VALUE);
        assertNotNull(String.format("No input message set on operation %s", echoOp.getName()), inMessage);
        QName echoInMessageName = new QName("http://tempuri.org/types", "echo");
        assertEquals(String.format("Unexpected input message QName set on operation %s", echoOp.getName()), echoInMessageName, inMessage.getElementQName());
        //message name is 'echoRequest' whereas the QName's local name is simply 'echo'
        assertEquals(String.format("Unexpected input message name set on operation %s", echoOp.getName()), "echoRequest", inMessage.getName());
        
        AxisMessage outMessage =  echoOp.getMessage(WSDLConstants.MESSAGE_LABEL_OUT_VALUE);
        assertNotNull(String.format("No output message set on operation %s", echoOp.getName()), outMessage);
        QName echoOutMessageName = new QName("http://tempuri.org/types", "echoResponse");
        assertEquals(String.format("Unexpected output message QName set on operation %s", echoOp.getName()), echoOutMessageName, outMessage.getElementQName());
        assertEquals(String.format("Unexpected output message name set on operation %s", echoOp.getName()), echoOutMessageName.getLocalPart(), outMessage.getName());
    }
    
    private AxisService populateAxisService(AxisConfiguration axisConf, File wsdlFile, WSDLLocator wsdlLocator) throws IOException {
        InputStream in = null;
        try {
            in = new FileInputStream(wsdlFile);
            WSDL11ToAxisServiceBuilder wsdl11Builder = new WSDL11ToAxisServiceBuilder(in);
            if (wsdlLocator != null) {
                wsdl11Builder.setCustomWSDLResolver(wsdlLocator);
            }
            wsdl11Builder.setDocumentBaseUri(wsdlFile.getParentFile().toURI().toString());
            if (axisConf != null) {
                wsdl11Builder.useAxisConfiguration(axisConf);
            }
            AxisService service = wsdl11Builder.populateService();
            assertNotNull("Could not load AxisService from wsdl: " + wsdlFile.getAbsolutePath(), service);
            
            return service;
        } finally {
            if (in != null) {
                in.close();
            }
        }
    }
    
    //custom locator that can located imported wsdls relative to the base wsdl uri (code borrowed from wsdl4j)
    class CustomWSDLLocator implements WSDLLocator {
        private String wsdlURI;

        private InputSource baseInputSource;
        private Map<String, InputSource> importInputSources = new HashMap<String, InputSource>();
        private boolean closed = false;
        private String baseURI;
        private String lastImportURI;

        public CustomWSDLLocator(String baseURI, String wsdlURI) {
            this.baseURI = baseURI;
            this.wsdlURI = wsdlURI;
        }

        public InputSource getBaseInputSource() {
            if (baseInputSource == null) {
                lastImportURI = baseURI + wsdlURI;
                baseInputSource = new InputSource(lastImportURI);
            }
            return baseInputSource;
        }

        public InputSource getImportInputSource(String parentLocation, String importLocation) {
            InputSource inSource = (InputSource) importInputSources.get(importLocation);
            if (inSource == null) {
                lastImportURI = baseURI + importLocation;
                inSource = new InputSource(lastImportURI);
                importInputSources.put(importLocation, inSource);
            }
            return inSource;
        }

        public String getBaseURI() {
            return baseURI + wsdlURI;
        }

        public String getLatestImportURI() {
            return lastImportURI;
        }

        public void close() {
            closed = true;
        }

        public boolean isClosed() {
            return closed;
        }
    }
}
