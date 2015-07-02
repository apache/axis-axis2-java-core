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
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.axis2.engine.AxisConfiguration;

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
}
