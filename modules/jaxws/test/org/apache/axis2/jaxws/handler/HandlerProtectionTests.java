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
package org.apache.axis2.jaxws.handler;

import java.util.ArrayList;
import java.util.Set;

import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import junit.framework.TestCase;

import org.apache.axis2.jaxws.core.MessageContext;
import org.apache.axis2.jaxws.handler.factory.HandlerPostInvokerFactory;
import org.apache.axis2.jaxws.handler.factory.HandlerPreInvokerFactory;
import org.apache.axis2.jaxws.message.Block;
import org.apache.axis2.jaxws.message.Message;
import org.apache.axis2.jaxws.message.Protocol;
import org.apache.axis2.jaxws.message.factory.MessageFactory;
import org.apache.axis2.jaxws.message.factory.XMLStringBlockFactory;
import org.apache.axis2.jaxws.registry.FactoryRegistry;

public class HandlerProtectionTests extends TestCase {

	private MessageContext mc = null;
	
	private static final String soap11env = "http://schemas.xmlsoap.org/soap/envelope/";
	
    public static final String SOAP11_ENVELOPE = 
        "<?xml version='1.0' encoding='utf-8'?>" + 
        "<soapenv:Envelope xmlns:soapenv=\"" + soap11env + "\">" +
        "<soapenv:Header />" + 
        "<soapenv:Body>" +
        "</soapenv:Body>" + 
        "</soapenv:Envelope>";

	//@Override
	protected void setUp() throws Exception {

		try {
			FactoryRegistry.setFactory(HandlerPreInvokerFactory.class, Class.forName("com.ibm.ws.webservices.engine.xmlsoap.saaj12.HandlerPreInvokerFactoryImpl").newInstance());
			FactoryRegistry.setFactory(HandlerPostInvokerFactory.class, Class.forName("com.ibm.ws.webservices.engine.xmlsoap.saaj12.HandlerPostInvokerFactoryImpl").newInstance());
			FactoryRegistry.setFactory(javax.xml.soap.SOAPFactory.class, Class.forName("com.ibm.ws.webservices.engine.xmlsoap.SOAPFactory").newInstance());
			FactoryRegistry.setFactory(javax.xml.soap.SOAPConnectionFactory.class, Class.forName("com.ibm.ws.webservices.engine.soap.SOAPConnectionFactoryImpl").newInstance());
			FactoryRegistry.setFactory(javax.xml.soap.SAAJMetaFactory.class, Class.forName("com.ibm.ws.webservices.engine.soap.SAAJMetaFactoryImpl").newInstance());
			FactoryRegistry.setFactory(javax.xml.parsers.SAXParserFactory.class, Class.forName("com.ibm.xml.xlxp.api.was.WSSAXParserFactoryImpl").newInstance());
		} catch (Throwable e) {
			e.printStackTrace();
		}
        // Create a SOAP 1.1 Message and MessageContext
		// I just grabbed this code from the JAXWS MessageTests
        MessageFactory mf = (MessageFactory) FactoryRegistry.getFactory(MessageFactory.class);
        Message m = mf.create(Protocol.soap11);
        XMLStringBlockFactory f =
                (XMLStringBlockFactory) FactoryRegistry.getFactory(XMLStringBlockFactory.class);
        Block block = f.createFrom(SOAP11_ENVELOPE, null, null);
        m.setBodyBlock(block);

        mc = new MessageContext();
        mc.setMessage(m);
        mc.setMEPContext(new MEPContext(mc));
	}

	public void _testProtectionViolation01() {
		
		ArrayList<Handler> handlers = new ArrayList<Handler>();
		handlers.add(new SOAPHandler1());
        HandlerChainProcessor processor =
                new HandlerChainProcessor(handlers, Protocol.soap11);
        Exception local_exception = null;
        boolean success = true;
        try {
            // server-side incoming request
            success = processor.processChain(mc.getMEPContext(),
                                    HandlerChainProcessor.Direction.IN,
                                    HandlerChainProcessor.MEP.REQUEST,
                                    true);
        } catch (Exception e) {
            assertNull(e);  // should not get exception
        }
        
        assertTrue("process chain should returned false due to illegal message modification by handler", !success);

        // we want an exception due to JSR109 handler violation
        //assertNotNull(local_exception);
	}
	
    private class SOAPHandler1 implements SOAPHandler<SOAPMessageContext> {

        public Set getHeaders() {
            return null;
        }

        public void close(javax.xml.ws.handler.MessageContext messagecontext) {
        }

        public boolean handleFault(SOAPMessageContext messagecontext) {
            return true;
        }

        public boolean handleMessage(SOAPMessageContext messagecontext) {
        	try {
        		// should cause a protection state violation
        		SOAPMessage msg = messagecontext.getMessage();
        		SOAPBody body = messagecontext.getMessage().getSOAPPart().getEnvelope().getBody();
        		body.addNamespaceDeclaration("blarg", "blarg");

        		return true;
        	} catch (SOAPException e) {
        		throw new RuntimeException(e);
        	}
        }

    }
}


