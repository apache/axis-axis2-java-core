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

import java.util.ArrayList;

import javax.xml.namespace.QName;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.OperationClient;
import org.apache.axis2.client.Options;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.OperationContext;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.description.java2wsdl.XMLSchemaTest;
import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaElement;

public class AxisMessageTest extends XMLSchemaTest {

    private AxisMessage axisMessage;
    protected AxisService service;
    private ArrayList<XmlSchema> schemas;
    private XmlSchemaElement element;

    @Override
    public void setUp() throws Exception {
        service=new AxisService();
        schemas=new ArrayList<XmlSchema>();
        loadSampleSchemaFile(schemas);
        service.addSchema(schemas);
        AxisOperation axisOperation=new AxisOperation() {
            
            @Override
            public void setRemainingPhasesInFlow(ArrayList list) {
                // TODO Auto-generated method stub
                
            }
            
            @Override
            public void setPhasesOutFlow(ArrayList list) {
                // TODO Auto-generated method stub
                
            }
            
            @Override
            public AxisService getAxisService() {
                // TODO Auto-generated method stub
                return service;
            }

            @Override
            public void setPhasesOutFaultFlow(ArrayList list) {
                // TODO Auto-generated method stub
                
            }
            
            @Override
            public void setPhasesInFaultFlow(ArrayList list) {
                // TODO Auto-generated method stub
                
            }
            
            @Override
            public ArrayList getRemainingPhasesInFlow() {
                // TODO Auto-generated method stub
                return null;
            }
            
            @Override
            public ArrayList getPhasesOutFlow() {
                // TODO Auto-generated method stub
                return null;
            }
            
            @Override
            public ArrayList getPhasesOutFaultFlow() {
                // TODO Auto-generated method stub
                return null;
            }
            
            @Override
            public ArrayList getPhasesInFaultFlow() {
                // TODO Auto-generated method stub
                return null;
            }
            
            @Override
            public AxisMessage getMessage(String label) {
                // TODO Auto-generated method stub
                return null;
            }
            
            @Override
            public OperationClient createClient(ServiceContext sc, Options options) {
                // TODO Auto-generated method stub
                return null;
            }
            
            @Override
            public void addMessageContext(MessageContext msgContext, OperationContext opContext)
                    throws AxisFault {
                // TODO Auto-generated method stub
                
            }
            
            @Override
            public void addMessage(AxisMessage message, String label) {
                // TODO Auto-generated method stub
                
            }
            
            @Override
            public void addFaultMessageContext(MessageContext msgContext, OperationContext opContext)
                    throws AxisFault {
                // TODO Auto-generated method stub
                
            }
        };
        axisMessage = new AxisMessage();
        axisMessage.setParent(axisOperation);
        axisMessage.setElementQName(new QName("http://www.w3schools.com", "note"));
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        axisMessage = null;
        service=null;
        schemas=null;
        super.tearDown();
    }

    public void testGetSchemaElement() throws Exception {
        element = axisMessage.getSchemaElement();
        assertNotNull(element);
        assertEquals(element.getName(), "note");
        
    }

}
