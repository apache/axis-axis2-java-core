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


package org.apache.axis2.wsdl.codegen.extension;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.OperationClient;
import org.apache.axis2.client.Options;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.OperationContext;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.description.AxisMessage;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.util.CommandLineOption;
import org.apache.axis2.wsdl.codegen.CodeGenConfiguration;
import org.apache.axis2.wsdl.codegen.XMLSchemaTest;
import org.apache.ws.commons.schema.XmlSchema;
import org.junit.Test;

public class JAXWSWapperExtensionTest extends XMLSchemaTest {
    private AxisMessage axisMessage;
    protected AxisService service;
    private ArrayList<XmlSchema> schemas;
    private AxisOperation axisOperation;

    @Override
    public void setUp() throws Exception {
        service = new AxisService();
        schemas = new ArrayList<XmlSchema>();
        loadSampleSchemaFile(schemas);
        service.addSchema(schemas);
        axisMessage = new AxisMessage();
        axisOperation = new AxisOperation() {

            @Override
            public void setRemainingPhasesInFlow(ArrayList list) {
            }

            @Override
            public void setPhasesOutFlow(ArrayList list) {
            }

            @Override
            public AxisService getAxisService() {
                return service;
            }

            @Override
            public void setPhasesOutFaultFlow(ArrayList list) {
            }

            @Override
            public void setPhasesInFaultFlow(ArrayList list) {
            }

            @Override
            public ArrayList getRemainingPhasesInFlow() {
                return null;
            }

            @Override
            public ArrayList getPhasesOutFlow() {
                return null;
            }

            @Override
            public ArrayList getPhasesOutFaultFlow() {
                return null;
            }

            @Override
            public ArrayList getPhasesInFaultFlow() {
                return null;
            }

            @Override
            public AxisMessage getMessage(String label) {
                //A message has to be returned
                return axisMessage;
            }

            @Override
            public OperationClient createClient(ServiceContext sc, Options options) {
                return null;
            }

            @Override
            public void addMessageContext(MessageContext msgContext, OperationContext opContext)
                    throws AxisFault {
            }

            @Override
            public void addMessage(AxisMessage message, String label) {
            }

            @Override
            public void addFaultMessageContext(MessageContext msgContext, OperationContext opContext)
                    throws AxisFault {
            }
        };
      
        axisMessage.setParent(axisOperation);
        axisMessage.setElementQName(new QName("http://www.w3schools.com", "note"));
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        axisMessage = null;
        service = null;
        schemas = null;
        axisOperation=null;
        super.tearDown();
    }

    @Test
    public void testEngage() throws Exception {
        //This is successful if the test is done without any exceptions
     
        axisOperation.addMessage(axisMessage, "test_message");
        service.addOperation(axisOperation);
        JAXWSWapperExtension extension = new JAXWSWapperExtension();
        Map<String, CommandLineOption> optionMap = new HashMap<String, CommandLineOption>();
        CodeGenConfiguration configuration = new CodeGenConfiguration(optionMap);
        configuration.setOutputLanguage("jax-ws");
        configuration.setParametersWrapped(false);
        configuration.addAxisService(service);
        extension.engage(configuration);

    }
    
    @Test
    public void  testWalkSchema() throws Exception{
        JAXWSWapperExtension extension = new JAXWSWapperExtension();
        Map<String, CommandLineOption> optionMap = new HashMap<String, CommandLineOption>();
        CodeGenConfiguration configuration = new CodeGenConfiguration(optionMap);
        configuration.setOutputLanguage("jax-ws");
        configuration.setParametersWrapped(false);
        configuration.addAxisService(service);        
        assertTrue(extension.walkSchema(axisMessage, "test"));
      }
}
