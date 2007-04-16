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
package org.apache.axis2.jaxws.handler.context;

import java.io.ByteArrayOutputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.ws.LogicalMessage;
import javax.xml.ws.handler.LogicalMessageContext;

import junit.framework.TestCase;

import org.apache.axis2.jaxws.context.factory.MessageContextFactory;
import org.apache.axis2.jaxws.core.MessageContext;
import org.apache.axis2.jaxws.message.Block;
import org.apache.axis2.jaxws.message.Message;
import org.apache.axis2.jaxws.message.Protocol;
import org.apache.axis2.jaxws.message.databinding.JAXBBlockContext;
import org.apache.axis2.jaxws.message.factory.JAXBBlockFactory;
import org.apache.axis2.jaxws.message.factory.MessageFactory;
import org.apache.axis2.jaxws.registry.FactoryRegistry;

import test.EchoString;
import test.ObjectFactory;

/**
 * Unit tests for the creation and usage of the LogicalMessageContext that is
 * used for handler processing.
 */
public class LogicalMessageContextTests extends TestCase {
    
    private final String INPUT = "sample input";
    
    public LogicalMessageContextTests(String name) {
        super(name);
    }
    
    /**
     * Test the javax.xml.transform.Source based APIs on the LogicalMessage interface.
     * @throws Exception
     */
    public void testLogicalMessageContextWithSource() throws Exception {
        MessageContext mc = createSampleMessageContext();
        LogicalMessageContext lmc = MessageContextFactory.createLogicalMessageContext(mc);
        
        LogicalMessage msg = lmc.getMessage();
        assertTrue("The returned LogicalMessage was null", msg != null);
        
        Source payload = msg.getPayload();
        assertTrue("The returned payload (Source) was null", payload != null);
        
        TransformerFactory factory = TransformerFactory.newInstance();
        Transformer trans = factory.newTransformer();
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        StreamResult result = new StreamResult(baos);
     
        trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        trans.transform(payload, result);
        
        String resultContent = new String(baos.toByteArray());
        assertTrue("The content returned was null", resultContent != null);
        assertTrue("The content returned was incomplete, unexpected element", resultContent.indexOf("echoString") > -1);
        assertTrue("The content returned was incomplete, unexpected content", resultContent.indexOf(INPUT) > -1);
    }
    
    /**
     * Test the JAXB based APIs on the LogicalMessage interface.
     * @throws Exception
     */
    public void testLogicalMessageContextWithJAXB() throws Exception {
        MessageContext mc = createSampleMessageContext();
        LogicalMessageContext lmc = MessageContextFactory.createLogicalMessageContext(mc);
        
        LogicalMessage msg = lmc.getMessage();
        assertTrue("The returned LogicalMessage was null", msg != null);
        
        JAXBContext jbc = JAXBContext.newInstance("test");
        
        Object obj = msg.getPayload(jbc);
        //assertTrue("The returned payload (Object) was null", obj != null);
    }
    
    private MessageContext createSampleMessageContext() throws Exception {
        MessageFactory factory = (MessageFactory) FactoryRegistry.getFactory(MessageFactory.class);
        Message msg = factory.create(Protocol.soap11);
        
        // Create a jaxb object
        ObjectFactory objFactory = new ObjectFactory();
        EchoString echo = objFactory.createEchoString();
        echo.setInput(INPUT);
        
        // Create the necessary JAXBContext
        JAXBContext jbc = JAXBContext.newInstance("test");
        JAXBBlockContext blockCtx = new JAXBBlockContext(jbc);
        
        // Create the Block 
        JAXBBlockFactory blockFactory = (JAXBBlockFactory) FactoryRegistry.getFactory(JAXBBlockFactory.class);
        Block block = blockFactory.createFrom(echo, blockCtx, null);
        
        msg.setBodyBlock(block);
        
        MessageContext mc = new MessageContext();
        mc.setMessage(msg);
        
        return mc;
    }
}