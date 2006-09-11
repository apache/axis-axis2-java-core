/*
 * Copyright 2004,2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.rampart;

import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.soap.impl.builder.StAXSOAPModelBuilder;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisService;
import org.apache.neethi.Policy;
import org.apache.neethi.PolicyEngine;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import java.io.ByteArrayInputStream;

import junit.framework.TestCase;

/**
 *
 * @author Ruchith Fernando (ruchith.fernando@gmail.com)
 */
public class MessageBuilderTest extends TestCase {

    static final String soapMsg = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">" + "<SOAP-ENV:Body>" + "<add xmlns=\"http://ws.apache.org/counter/counter_port_type\">" + "<value xmlns=\"\">15</value>" + "</add>" + "</SOAP-ENV:Body>\r\n       \r\n" + "</SOAP-ENV:Envelope>";

    public MessageBuilderTest() {
        super();
    }

    public MessageBuilderTest(String arg0) {
        super(arg0);
    }

    
    
    public void testTransportBinding() {
        try {
            MessageContext ctx = getMsgCtx();
            
            String policyXml = "test-resources/policy/rampart-transport-binding.xml";
            Policy policy = this.loadPolicy(policyXml);
            
            ctx.setProperty(RampartMessageData.KEY_RAMPART_POLICY, policy);
            
            MessageBuilder builder = new MessageBuilder();
            builder.build(ctx);
            
            System.out.println(ctx.getEnvelope());
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }
    
    public void testTransportBindingNoBST() {
        try {
            MessageContext ctx = getMsgCtx();
            
            String policyXml = "test-resources/policy/rampart-transport-binding-no-bst.xml";
            Policy policy = this.loadPolicy(policyXml);
            
            ctx.setProperty(RampartMessageData.KEY_RAMPART_POLICY, policy);
            
            MessageBuilder builder = new MessageBuilder();
            builder.build(ctx);
            
            System.out.println(ctx.getEnvelope());
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    public void testTransportBindingWithDK() {
        try {
            MessageContext ctx = getMsgCtx();
            
            String policyXml = "test-resources/policy/rampart-transport-binding-dk.xml";
            Policy policy = this.loadPolicy(policyXml);
            
            ctx.setProperty(RampartMessageData.KEY_RAMPART_POLICY, policy);
            
            MessageBuilder builder = new MessageBuilder();
            builder.build(ctx);
            
            System.out.println(ctx.getEnvelope());
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }
    
    /**
     * @throws XMLStreamException
     * @throws FactoryConfigurationError
     * @throws AxisFault
     */
    private MessageContext getMsgCtx() throws XMLStreamException, FactoryConfigurationError, AxisFault {
        MessageContext ctx = new MessageContext();
        ctx.setAxisService(new AxisService("TestService"));
        
        XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(new ByteArrayInputStream(soapMsg.getBytes()));
        ctx.setEnvelope(new StAXSOAPModelBuilder(reader, null).getSOAPEnvelope());
        return ctx;
    }
    
    private Policy loadPolicy(String xmlPath) throws Exception {
        StAXOMBuilder builder = new StAXOMBuilder(xmlPath);
        return PolicyEngine.getPolicy(builder.getDocumentElement());
    }
    
}
