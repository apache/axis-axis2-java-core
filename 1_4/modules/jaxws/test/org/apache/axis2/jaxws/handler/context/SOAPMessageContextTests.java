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

package org.apache.axis2.jaxws.handler.context;

import junit.framework.TestCase;
import org.apache.axis2.jaxws.context.factory.MessageContextFactory;
import org.apache.axis2.jaxws.core.MessageContext;
import org.apache.axis2.jaxws.handler.MEPContext;
import org.apache.axis2.jaxws.handler.SoapMessageContext;
import org.apache.axis2.jaxws.message.Block;
import org.apache.axis2.jaxws.message.Message;
import org.apache.axis2.jaxws.message.Protocol;
import org.apache.axis2.jaxws.message.databinding.JAXBBlockContext;
import org.apache.axis2.jaxws.message.factory.JAXBBlockFactory;
import org.apache.axis2.jaxws.message.factory.MessageFactory;
import org.apache.axis2.jaxws.registry.FactoryRegistry;
import test.EchoString;
import test.ObjectFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.soap.SOAPMessage;

public class SOAPMessageContextTests extends TestCase {

	private final String INPUT = "sample input";
	
    private SoapMessageContext createSampleContext() throws Exception {
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
        mc.setMEPContext(new MEPContext(mc));
        mc.setMessage(msg);
        
        return MessageContextFactory.createSoapMessageContext(mc);

    }
    
    public void testGetAsSoapMessageObjectID() {
    	try {
    		SoapMessageContext smc = createSampleContext();
    		SOAPMessage m1 = smc.getMessage();
    		SOAPMessage m2 = smc.getMessage();
    		// not using assertEquals because I want object id equality
    		assertTrue("retrieval of message from SoapMessageContext twice in a row should result in same object", m1 == m2);
    	} catch (Exception e) {
    		assertNull("should not get an exception in this test", e);
    	}
    }
}
