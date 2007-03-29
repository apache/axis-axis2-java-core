/*
 * Copyright 2004,2005 The Apache Software Foundation.
 * Copyright 2006 International Business Machines Corp.
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
package org.apache.axis2.jaxws.handler;

import java.util.ArrayList;
import java.util.Set;

import javax.xml.ws.ProtocolException;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.LogicalHandler;
import javax.xml.ws.handler.soap.SOAPHandler;

import junit.framework.TestCase;
import org.apache.axis2.jaxws.core.MessageContext;

/*
 * There are myriad scenarios to test here:
 * Handler implementations can implement two classes:  SOAPHandler or LogicalHandler (2)
 * They implement two critical methods:  handleMessage and handleFault (2)
 * These methods have four possible results:  true, false, ProtocolException, other exception
 * 
 * Besides the possible behaviors of the Handler implementations, we also
 * have to consider whether the message is incoming or outgoing, whether
 * it's a response or a request, and if a response is expected.
 * 
 * Do our best to cover all scenarios.
 * 
 * The testHandleMessage_* methods test the HandlerChainProcessor.processChain() method
 * The testHandleFault_* methods test the HandlerChainProcessor.processFault() method
 * 
 */
public class HandlerChainProcessorTests extends TestCase {

	// String result is how we'll verify the right methods from
	// the Handler implementations were called
	private String result = new String();

	private enum ResultDesired { TRUE, FALSE, PROTOCOL_EXCEPTION, OTHER_EXCEPTION };
	
	// use the following to dictate how the Handler methods behave
	private ResultDesired soaphandler1_MessageResultDesired;
	private ResultDesired soaphandler1_FaultResultDesired;
	private ResultDesired soaphandler2_MessageResultDesired;
	private ResultDesired soaphandler2_FaultResultDesired;
	private ResultDesired logicalhandler1_MessageResultDesired;
	private ResultDesired logicalhandler1_FaultResultDesired;
	private ResultDesired logicalhandler2_MessageResultDesired;
	private ResultDesired logicalhandler2_FaultResultDesired;

	ArrayList<Handler> handlers = new ArrayList<Handler>();
	
	@Override
	protected void setUp() throws Exception {
		// HandlerChainProcessor expects a sorted chain
		handlers.add(new LogicalHandler2());
		handlers.add(new LogicalHandler1());
		handlers.add(new SOAPHandler1());
		handlers.add(new SOAPHandler2());
	}
	
	/*
	 * empty list
	 */
	public void testHandleMessage_empty1() {
		
		Exception local_exception = null;
		
		HandlerChainProcessor processor1 = new HandlerChainProcessor(null);
		HandlerChainProcessor processor2 = new HandlerChainProcessor(new ArrayList<Handler>());
		try {
			processor1.processChain(new SoapMessageContext(new MessageContext()), HandlerChainProcessor.Direction.IN, HandlerChainProcessor.MEP.REQUEST, true);
			processor2.processChain(new SoapMessageContext(new MessageContext()), HandlerChainProcessor.Direction.IN, HandlerChainProcessor.MEP.REQUEST, true);
		} catch (Exception e) {
			local_exception = e;
		}
		
		// no exceptions!
		assertNull(local_exception);
	}
	
	/*
	 * one protocol handler
	 * processing expected:  Logical and SOAP, reverse order, close
	 */
	public void testHandleMessage_oneproto1() {
		
		// reset result
		result = "";
		
		// use a local list
		ArrayList<Handler> local_list = new ArrayList<Handler>();
		local_list.add(new SOAPHandler1());
		
		// we want all good responses:
		soaphandler1_MessageResultDesired = ResultDesired.TRUE;
		soaphandler1_FaultResultDesired = ResultDesired.TRUE;
	
		HandlerChainProcessor processor = new HandlerChainProcessor(local_list);
		
		processor.processChain(new SoapMessageContext(new MessageContext()), HandlerChainProcessor.Direction.IN, HandlerChainProcessor.MEP.REQUEST, false);

		assertEquals("S1m:S1c:", result);
		
	}
	
	/*
	 * one protocol handler in a logical context
	 * no handlers will be processed
	 */
	public void testHandleMessage_oneproto2() {
		
		// reset result
		result = "";
		
		// use a local list
		ArrayList<Handler> local_list = new ArrayList<Handler>();
		local_list.add(new SOAPHandler1());
		
		// we want all good responses:
		soaphandler1_MessageResultDesired = ResultDesired.TRUE;
		soaphandler1_FaultResultDesired = ResultDesired.TRUE;
	
		HandlerChainProcessor processor = new HandlerChainProcessor(local_list);
		
		processor.processChain(new LogicalMessageContext(new MessageContext()), HandlerChainProcessor.Direction.IN, HandlerChainProcessor.MEP.REQUEST, false);

		assertEquals("", result);
	}
	
	/*
	 * one logical handler
	 * processing expected:  Logical and SOAP, reverse order, close
	 */
	public void testHandleMessage_onelogical() {
		
		// reset result
		result = "";
		
		// use a local list
		ArrayList<Handler> local_list = new ArrayList<Handler>();
		local_list.add(new LogicalHandler1());
		
		// we want all good responses:
		logicalhandler1_MessageResultDesired = ResultDesired.TRUE;
		logicalhandler1_FaultResultDesired = ResultDesired.TRUE;
	
		HandlerChainProcessor processor = new HandlerChainProcessor(local_list);
		
		processor.processChain(new LogicalMessageContext(new MessageContext()), HandlerChainProcessor.Direction.IN, HandlerChainProcessor.MEP.REQUEST, false);

		assertEquals("L1m:L1c:", result);
	}

	/*
	 * incoming request (we must be on the server), response expected
	 * processing expected:  Logical and SOAP, reverse order, no closing
	 */
	public void testHandleMessage_true1() {

		// reset result
		result = "";
		
		// we want all good responses:
		soaphandler1_MessageResultDesired = ResultDesired.TRUE;
		soaphandler1_FaultResultDesired = ResultDesired.TRUE;
		soaphandler2_MessageResultDesired = ResultDesired.TRUE;
		soaphandler2_FaultResultDesired = ResultDesired.TRUE;
		logicalhandler1_MessageResultDesired = ResultDesired.TRUE;
		logicalhandler1_FaultResultDesired = ResultDesired.TRUE;
		logicalhandler2_MessageResultDesired = ResultDesired.TRUE;
		logicalhandler2_FaultResultDesired = ResultDesired.TRUE;
		
		HandlerChainProcessor processor = new HandlerChainProcessor(handlers);
		
		processor.processChain(new SoapMessageContext(new MessageContext()), HandlerChainProcessor.Direction.IN, HandlerChainProcessor.MEP.REQUEST, true);

		assertEquals("S2m:S1m:L1m:L2m:", result);
		
	}
		
	/*
	 * incoming request (we must be on the server), response NOT expected
	 * processing expected:  Logical and SOAP, reverse order, close
	 */
	public void testHandleMessage_true2() {
		
		// reset result
		result = "";
		
		// we want all good responses:
		soaphandler1_MessageResultDesired = ResultDesired.TRUE;
		soaphandler1_FaultResultDesired = ResultDesired.TRUE;
		soaphandler2_MessageResultDesired = ResultDesired.TRUE;
		soaphandler2_FaultResultDesired = ResultDesired.TRUE;
		logicalhandler1_MessageResultDesired = ResultDesired.TRUE;
		logicalhandler1_FaultResultDesired = ResultDesired.TRUE;
		logicalhandler2_MessageResultDesired = ResultDesired.TRUE;
		logicalhandler2_FaultResultDesired = ResultDesired.TRUE;
		
		HandlerChainProcessor processor = new HandlerChainProcessor(handlers);
		
		processor.processChain(new SoapMessageContext(new MessageContext()), HandlerChainProcessor.Direction.IN, HandlerChainProcessor.MEP.REQUEST, false);

		assertEquals("S2m:S1m:L1m:L2m:L2c:L1c:S1c:S2c:", result);
		
	}
	
	/*
	 * incoming response (we must be on the client), response expected (ignored)
	 * processing expected:  Logical and SOAP, reverse order, close
	 */
	public void testHandleMessage_true3() {
		
		// reset result
		result = "";
		
		// we want all good responses:
		soaphandler1_MessageResultDesired = ResultDesired.TRUE;
		soaphandler1_FaultResultDesired = ResultDesired.TRUE;
		soaphandler2_MessageResultDesired = ResultDesired.TRUE;
		soaphandler2_FaultResultDesired = ResultDesired.TRUE;
		logicalhandler1_MessageResultDesired = ResultDesired.TRUE;
		logicalhandler1_FaultResultDesired = ResultDesired.TRUE;
		logicalhandler2_MessageResultDesired = ResultDesired.TRUE;
		logicalhandler2_FaultResultDesired = ResultDesired.TRUE;
		
		HandlerChainProcessor processor = new HandlerChainProcessor(handlers);
		
		processor.processChain(new SoapMessageContext(new MessageContext()), HandlerChainProcessor.Direction.IN, HandlerChainProcessor.MEP.RESPONSE, true);

		assertEquals("S2m:S1m:L1m:L2m:L2c:L1c:S1c:S2c:", result);

	}
	
	/*
	 * outgoing request (we must be on the client), response expected
	 * processing expected:  Logical and SOAP, normal order, no closing
	 */
	public void testHandleMessage_true4() {
		
		// reset result
		result = "";
		
		// we want all good responses:
		soaphandler1_MessageResultDesired = ResultDesired.TRUE;
		soaphandler1_FaultResultDesired = ResultDesired.TRUE;
		soaphandler2_MessageResultDesired = ResultDesired.TRUE;
		soaphandler2_FaultResultDesired = ResultDesired.TRUE;
		logicalhandler1_MessageResultDesired = ResultDesired.TRUE;
		logicalhandler1_FaultResultDesired = ResultDesired.TRUE;
		logicalhandler2_MessageResultDesired = ResultDesired.TRUE;
		logicalhandler2_FaultResultDesired = ResultDesired.TRUE;
		
		HandlerChainProcessor processor = new HandlerChainProcessor(handlers);
		
		processor.processChain(new SoapMessageContext(new MessageContext()), HandlerChainProcessor.Direction.OUT, HandlerChainProcessor.MEP.REQUEST, true);

		assertEquals("L2m:L1m:S1m:S2m:", result);
	}
	
	/*
	 * outgoing request (we must be on the client), response NOT expected
	 * processing expected:  Logical and SOAP, normal order, close
	 */
	public void testHandleMessage_true5() {
		
		// reset result
		result = "";
		
		// we want all good responses:
		soaphandler1_MessageResultDesired = ResultDesired.TRUE;
		soaphandler1_FaultResultDesired = ResultDesired.TRUE;
		soaphandler2_MessageResultDesired = ResultDesired.TRUE;
		soaphandler2_FaultResultDesired = ResultDesired.TRUE;
		logicalhandler1_MessageResultDesired = ResultDesired.TRUE;
		logicalhandler1_FaultResultDesired = ResultDesired.TRUE;
		logicalhandler2_MessageResultDesired = ResultDesired.TRUE;
		logicalhandler2_FaultResultDesired = ResultDesired.TRUE;
		
		HandlerChainProcessor processor = new HandlerChainProcessor(handlers);
		
		processor.processChain(new SoapMessageContext(new MessageContext()), HandlerChainProcessor.Direction.OUT, HandlerChainProcessor.MEP.REQUEST, false);

		assertEquals("L2m:L1m:S1m:S2m:S2c:S1c:L1c:L2c:", result);
	}
	
	/*
	 * outgoing response (we must be on the server), response expected (ignored)
	 * processing expected:  Logical and SOAP, normal order, close
	 */
	public void testHandleMessage_true6() {

		// reset result
		result = "";
		
		// we want all good responses:
		soaphandler1_MessageResultDesired = ResultDesired.TRUE;
		soaphandler1_FaultResultDesired = ResultDesired.TRUE;
		soaphandler2_MessageResultDesired = ResultDesired.TRUE;
		soaphandler2_FaultResultDesired = ResultDesired.TRUE;
		logicalhandler1_MessageResultDesired = ResultDesired.TRUE;
		logicalhandler1_FaultResultDesired = ResultDesired.TRUE;
		logicalhandler2_MessageResultDesired = ResultDesired.TRUE;
		logicalhandler2_FaultResultDesired = ResultDesired.TRUE;
		
		HandlerChainProcessor processor = new HandlerChainProcessor(handlers);
		
		processor.processChain(new SoapMessageContext(new MessageContext()), HandlerChainProcessor.Direction.OUT, HandlerChainProcessor.MEP.RESPONSE, true);

		assertEquals("L2m:L1m:S1m:S2m:S2c:S1c:L1c:L2c:", result);
	}
	
	/*
	 * At this point we know the sorting and closing logic is all good,
	 * all that's left is to make sure the SOAP handlers are excluded when
	 * we're in a LogicalMessageContext.
	 * 
	 * outgoing response (we must be on the server), response expected (ignored)
	 * processing expected:  Logical only, normal order, close
	 */
	public void testHandleMessage_true7() {
		
		// reset result
		result = "";
		
		// we want all good responses:
		soaphandler1_MessageResultDesired = ResultDesired.TRUE;
		soaphandler1_FaultResultDesired = ResultDesired.TRUE;
		soaphandler2_MessageResultDesired = ResultDesired.TRUE;
		soaphandler2_FaultResultDesired = ResultDesired.TRUE;
		logicalhandler1_MessageResultDesired = ResultDesired.TRUE;
		logicalhandler1_FaultResultDesired = ResultDesired.TRUE;
		logicalhandler2_MessageResultDesired = ResultDesired.TRUE;
		logicalhandler2_FaultResultDesired = ResultDesired.TRUE;
		
		HandlerChainProcessor processor = new HandlerChainProcessor(handlers);
		
		processor.processChain(new LogicalMessageContext(new MessageContext()), HandlerChainProcessor.Direction.OUT, HandlerChainProcessor.MEP.RESPONSE, true);
		assertEquals("L2m:L1m:L1c:L2c:", result);
	}
	
	/*
	 * incoming request (we must be on the server), response expected
	 * a middle Handler.handleMessage returns false
	 * processing expected:  Logical and SOAP, reverse order, message reversed, close
	 */
	public void testHandleMessage_false1() {
		
		// reset result
		result = "";
		
		// we want one false response:
		soaphandler1_MessageResultDesired = ResultDesired.TRUE;
		soaphandler1_FaultResultDesired = ResultDesired.TRUE;
		soaphandler2_MessageResultDesired = ResultDesired.TRUE;
		soaphandler2_FaultResultDesired = ResultDesired.TRUE;
		logicalhandler1_MessageResultDesired = ResultDesired.FALSE;
		logicalhandler1_FaultResultDesired = ResultDesired.TRUE;
		logicalhandler2_MessageResultDesired = ResultDesired.TRUE;
		logicalhandler2_FaultResultDesired = ResultDesired.TRUE;
		
		HandlerChainProcessor processor = new HandlerChainProcessor(handlers);
		SoapMessageContext context = new SoapMessageContext(new MessageContext());
		processor.processChain(context, HandlerChainProcessor.Direction.IN, HandlerChainProcessor.MEP.REQUEST, true);

		assertEquals("S2m:S1m:L1m:S1m:S2m:L1c:S1c:S2c:", result);
		assertTrue((Boolean)(context.get(SoapMessageContext.MESSAGE_OUTBOUND_PROPERTY)));
	}
	
	/*
	 * outgoing request (we must be on the client), response expected
	 * a middle Handler.handleMessage returns false
	 * processing expected:  Logical and SOAP, normal order, message reversed, close
	 */
	public void testHandleMessage_false2() {
		
		// reset result
		result = "";
		
		// we want one false response:
		soaphandler1_MessageResultDesired = ResultDesired.TRUE;
		soaphandler1_FaultResultDesired = ResultDesired.TRUE;
		soaphandler2_MessageResultDesired = ResultDesired.TRUE;
		soaphandler2_FaultResultDesired = ResultDesired.TRUE;
		logicalhandler1_MessageResultDesired = ResultDesired.FALSE;
		logicalhandler1_FaultResultDesired = ResultDesired.TRUE;
		logicalhandler2_MessageResultDesired = ResultDesired.TRUE;
		logicalhandler2_FaultResultDesired = ResultDesired.TRUE;
		
		HandlerChainProcessor processor = new HandlerChainProcessor(handlers);
		SoapMessageContext context = new SoapMessageContext(new MessageContext());
		processor.processChain(context, HandlerChainProcessor.Direction.OUT, HandlerChainProcessor.MEP.REQUEST, true);

		assertEquals("L2m:L1m:L2m:L1c:L2c:", result);
		assertTrue(!(Boolean)(context.get(SoapMessageContext.MESSAGE_OUTBOUND_PROPERTY)));
	}
	
	/*
	 * outgoing request (we must be on the client), response NOT expected
	 * a middle Handler.handleMessage returns false
	 * processing expected:  Logical and SOAP, normal order, message NOT reversed, close
	 */
	public void testHandleMessage_false3() {
		
		// reset result
		result = "";
		
		// we want one false response:
		soaphandler1_MessageResultDesired = ResultDesired.TRUE;
		soaphandler1_FaultResultDesired = ResultDesired.TRUE;
		soaphandler2_MessageResultDesired = ResultDesired.TRUE;
		soaphandler2_FaultResultDesired = ResultDesired.TRUE;
		logicalhandler1_MessageResultDesired = ResultDesired.FALSE;
		logicalhandler1_FaultResultDesired = ResultDesired.TRUE;
		logicalhandler2_MessageResultDesired = ResultDesired.TRUE;
		logicalhandler2_FaultResultDesired = ResultDesired.TRUE;
		
		HandlerChainProcessor processor = new HandlerChainProcessor(handlers);
		SoapMessageContext context = new SoapMessageContext(new MessageContext());
		processor.processChain(context, HandlerChainProcessor.Direction.OUT, HandlerChainProcessor.MEP.REQUEST, false);

		assertEquals("L2m:L1m:L1c:L2c:", result);
		assertTrue((Boolean)(context.get(SoapMessageContext.MESSAGE_OUTBOUND_PROPERTY)));
	}
	
	/*
	 * incoming request (we must be on the server), response expected
	 * a middle Handler.handleMessage throws ProtocolException
	 * processing expected:  Logical and SOAP, reverse order, message reversed, handleFault, close
	 */
	public void testHandleMessage_protocolex_true1() {
		
		// reset result
		result = "";
		
		// we want one false response:
		soaphandler1_MessageResultDesired = ResultDesired.TRUE;
		soaphandler1_FaultResultDesired = ResultDesired.TRUE;
		soaphandler2_MessageResultDesired = ResultDesired.TRUE;
		soaphandler2_FaultResultDesired = ResultDesired.TRUE;
		logicalhandler1_MessageResultDesired = ResultDesired.PROTOCOL_EXCEPTION;
		logicalhandler1_FaultResultDesired = ResultDesired.TRUE;
		logicalhandler2_MessageResultDesired = ResultDesired.TRUE;
		logicalhandler2_FaultResultDesired = ResultDesired.TRUE;
		
		HandlerChainProcessor processor = new HandlerChainProcessor(handlers);
		SoapMessageContext context = new SoapMessageContext(new MessageContext());
		processor.processChain(context, HandlerChainProcessor.Direction.IN, HandlerChainProcessor.MEP.REQUEST, true);

		// handleFault processing
		assertEquals("S2m:S1m:L1m:S1f:S2f:L1c:S1c:S2c:", result);
		assertTrue((Boolean)(context.get(SoapMessageContext.MESSAGE_OUTBOUND_PROPERTY)));
	}
	
	/*
	 * incoming request (we must be on the server), response NOT expected
	 * a middle Handler.handleMessage throws ProtocolException
	 * processing expected:  Logical and SOAP, reverse order, message NOT reversed, close
	 */
	public void testHandleMessage_protocolex_true2() {
		
		// reset result
		result = "";
		
		// we want one false response:
		soaphandler1_MessageResultDesired = ResultDesired.TRUE;
		soaphandler1_FaultResultDesired = ResultDesired.TRUE;
		soaphandler2_MessageResultDesired = ResultDesired.TRUE;
		soaphandler2_FaultResultDesired = ResultDesired.TRUE;
		logicalhandler1_MessageResultDesired = ResultDesired.PROTOCOL_EXCEPTION;
		logicalhandler1_FaultResultDesired = ResultDesired.TRUE;
		logicalhandler2_MessageResultDesired = ResultDesired.TRUE;
		logicalhandler2_FaultResultDesired = ResultDesired.TRUE;
		
		HandlerChainProcessor processor = new HandlerChainProcessor(handlers);
		SoapMessageContext context = new SoapMessageContext(new MessageContext());
		processor.processChain(context, HandlerChainProcessor.Direction.IN, HandlerChainProcessor.MEP.REQUEST, false);

		// no handleFault calls
		assertEquals("S2m:S1m:L1m:L1c:S1c:S2c:", result);
		assertTrue(!(Boolean)(context.get(SoapMessageContext.MESSAGE_OUTBOUND_PROPERTY)));
	}
	
	/*
	 * incoming request (we must be on the server), response expected
	 * a middle Handler.handleMessage throws RuntimeException
	 * processing expected:  Logical and SOAP, reverse order, message reversed, (no handleFault), close
	 */
	public void testHandleMessage_runtimeex_true() {
		
		// reset result
		result = "";
		
		// we want one false response:
		soaphandler1_MessageResultDesired = ResultDesired.TRUE;
		soaphandler1_FaultResultDesired = ResultDesired.TRUE;
		soaphandler2_MessageResultDesired = ResultDesired.TRUE;
		soaphandler2_FaultResultDesired = ResultDesired.TRUE;
		logicalhandler1_MessageResultDesired = ResultDesired.OTHER_EXCEPTION;
		logicalhandler1_FaultResultDesired = ResultDesired.TRUE;
		logicalhandler2_MessageResultDesired = ResultDesired.TRUE;
		logicalhandler2_FaultResultDesired = ResultDesired.TRUE;
		
		HandlerChainProcessor processor = new HandlerChainProcessor(handlers);
		SoapMessageContext context = new SoapMessageContext(new MessageContext());
		Exception e = null;
		try {
			processor.processChain(context, HandlerChainProcessor.Direction.IN, HandlerChainProcessor.MEP.REQUEST, true);
		} catch (RuntimeException re) {
			e = re;
		}

		assertNotNull(e);
		// no handleFault calls
		assertEquals("S2m:S1m:L1m:L1c:S1c:S2c:", result);
		assertTrue((Boolean)(context.get(SoapMessageContext.MESSAGE_OUTBOUND_PROPERTY)));
	}
	
	/*
	 * incoming request (we must be on the server), response expected
	 * a middle Handler.handleMessage throws ProtocolException, later a Handler.handleFault returns false
	 * processing expected:  Logical and SOAP, reverse order, message reversed, handleFault, close
	 */
	public void testHandleMessage_protocolex_false() {
		
		// reset result
		result = "";
		
		// we want one false response:
		soaphandler1_MessageResultDesired = ResultDesired.TRUE;
		soaphandler1_FaultResultDesired = ResultDesired.FALSE;
		soaphandler2_MessageResultDesired = ResultDesired.TRUE;
		soaphandler2_FaultResultDesired = ResultDesired.TRUE;
		logicalhandler1_MessageResultDesired = ResultDesired.PROTOCOL_EXCEPTION;
		logicalhandler1_FaultResultDesired = ResultDesired.TRUE;
		logicalhandler2_MessageResultDesired = ResultDesired.TRUE;
		logicalhandler2_FaultResultDesired = ResultDesired.TRUE;
		
		HandlerChainProcessor processor = new HandlerChainProcessor(handlers);
		SoapMessageContext context = new SoapMessageContext(new MessageContext());
		processor.processChain(context, HandlerChainProcessor.Direction.IN, HandlerChainProcessor.MEP.REQUEST, true);

		// handleFault processing, but notice S2f does not get called
		assertEquals("S2m:S1m:L1m:S1f:L1c:S1c:S2c:", result);
		assertTrue((Boolean)(context.get(SoapMessageContext.MESSAGE_OUTBOUND_PROPERTY)));
	}
	
	/*
	 * incoming request (we must be on the server), response expected
	 * a middle Handler.handleMessage throws ProtocolException, later a Handler.handleFault throws ProtocolException
	 * processing expected:  Logical and SOAP, reverse order, message reversed, handleFault, close
	 */
	public void testHandleMessage_protocolex_protocolex() {
		
		// reset result
		result = "";
		
		// we want one false response:
		soaphandler1_MessageResultDesired = ResultDesired.TRUE;
		soaphandler1_FaultResultDesired = ResultDesired.PROTOCOL_EXCEPTION;
		soaphandler2_MessageResultDesired = ResultDesired.TRUE;
		soaphandler2_FaultResultDesired = ResultDesired.TRUE;
		logicalhandler1_MessageResultDesired = ResultDesired.PROTOCOL_EXCEPTION;
		logicalhandler1_FaultResultDesired = ResultDesired.TRUE;
		logicalhandler2_MessageResultDesired = ResultDesired.TRUE;
		logicalhandler2_FaultResultDesired = ResultDesired.TRUE;
		
		HandlerChainProcessor processor = new HandlerChainProcessor(handlers);
		SoapMessageContext context = new SoapMessageContext(new MessageContext());
		Exception e = null;
		try {
			// handleFault processing, but notice S2f does not get called, and we get an exception
			processor.processChain(context, HandlerChainProcessor.Direction.IN, HandlerChainProcessor.MEP.REQUEST, true);
		} catch (ProtocolException pe) {
			e = pe;
		}

		assertNotNull(e);
		assertEquals("S2m:S1m:L1m:S1f:L1c:S1c:S2c:", result);
		assertTrue((Boolean)(context.get(SoapMessageContext.MESSAGE_OUTBOUND_PROPERTY)));
	}
	
	/*
	 * incoming request (we must be on the server), response expected
	 * a middle Handler.handleMessage throws ProtocolException, later a Handler.handleFault throws ProtocolException
	 * processing expected:  Logical and SOAP, reverse order, handleFault, close
	 */
	public void testHandleMessage_protocolex_runtimeex() {
		
		// reset result
		result = "";
		
		// we want one false response:
		soaphandler1_MessageResultDesired = ResultDesired.TRUE;
		soaphandler1_FaultResultDesired = ResultDesired.OTHER_EXCEPTION;
		soaphandler2_MessageResultDesired = ResultDesired.TRUE;
		soaphandler2_FaultResultDesired = ResultDesired.TRUE;
		logicalhandler1_MessageResultDesired = ResultDesired.PROTOCOL_EXCEPTION;
		logicalhandler1_FaultResultDesired = ResultDesired.TRUE;
		logicalhandler2_MessageResultDesired = ResultDesired.TRUE;
		logicalhandler2_FaultResultDesired = ResultDesired.TRUE;
		
		HandlerChainProcessor processor = new HandlerChainProcessor(handlers);
		SoapMessageContext context = new SoapMessageContext(new MessageContext());
		Exception e = null;
		try {
			// same results as testHandlers_protocolex_protocolex
			processor.processChain(context, HandlerChainProcessor.Direction.IN, HandlerChainProcessor.MEP.REQUEST, true);
		} catch (RuntimeException pe) {
			e = pe;
		}

		assertNotNull(e);
		assertEquals("S2m:S1m:L1m:S1f:L1c:S1c:S2c:", result);
		assertTrue((Boolean)(context.get(SoapMessageContext.MESSAGE_OUTBOUND_PROPERTY)));
	}

	
	/*
	 * empty list
	 */
	public void testHandleFault_empty1() {
		
		Exception local_exception = null;
		
		HandlerChainProcessor processor1 = new HandlerChainProcessor(null);
		HandlerChainProcessor processor2 = new HandlerChainProcessor(new ArrayList<Handler>());
		try {
			processor1.processFault(new SoapMessageContext(new MessageContext()), HandlerChainProcessor.Direction.IN);
			processor2.processFault(new SoapMessageContext(new MessageContext()), HandlerChainProcessor.Direction.IN);
		} catch (Exception e) {
			local_exception = e;
		}
		
		// no exceptions!
		assertNull(local_exception);
	}
	
	
	/*
	 * outgoing response (we must be on the server), response expected (ignored)
	 * processing expected:  Logical and SOAP, normal order, handleFault, close
	 */
	public void testHandleFault_true1() {
		
		// reset result
		result = "";
		
		// we want one false response:
		soaphandler1_MessageResultDesired = ResultDesired.TRUE;
		soaphandler1_FaultResultDesired = ResultDesired.TRUE;
		soaphandler2_MessageResultDesired = ResultDesired.TRUE;
		soaphandler2_FaultResultDesired = ResultDesired.TRUE;
		logicalhandler1_MessageResultDesired = ResultDesired.TRUE;
		logicalhandler1_FaultResultDesired = ResultDesired.TRUE;
		logicalhandler2_MessageResultDesired = ResultDesired.TRUE;
		logicalhandler2_FaultResultDesired = ResultDesired.TRUE;
		
		HandlerChainProcessor processor = new HandlerChainProcessor(handlers);
		
		processor.processFault(new SoapMessageContext(new MessageContext()), HandlerChainProcessor.Direction.OUT);

		assertEquals("L2f:L1f:S1f:S2f:S2c:S1c:L1c:L2c:", result);
	}
	
	/*
	 * outgoing response (we must be on the server)
	 * a middle Handler.handleFault returns false
	 * processing expected:  Logical and SOAP, normal order, handleFault, close (all)
	 */
	public void testHandleFault_false1() {
		
		// reset result
		result = "";
		
		// we want one false response:
		soaphandler1_MessageResultDesired = ResultDesired.TRUE;
		soaphandler1_FaultResultDesired = ResultDesired.TRUE;
		soaphandler2_MessageResultDesired = ResultDesired.TRUE;
		soaphandler2_FaultResultDesired = ResultDesired.TRUE;
		logicalhandler1_MessageResultDesired = ResultDesired.TRUE;
		logicalhandler1_FaultResultDesired = ResultDesired.FALSE;
		logicalhandler2_MessageResultDesired = ResultDesired.TRUE;
		logicalhandler2_FaultResultDesired = ResultDesired.TRUE;
		
		HandlerChainProcessor processor = new HandlerChainProcessor(handlers);
		
		processor.processFault(new SoapMessageContext(new MessageContext()), HandlerChainProcessor.Direction.OUT);

		// notice all handlers are closed in this scenario
		assertEquals("L2f:L1f:S2c:S1c:L1c:L2c:", result);
	}
	
	/*
	 * incoming response (we must be on the client)
	 * a middle Handler.handleFault throws ProtocolException
	 * processing expected:  Logical and SOAP, reverse order, handleFault, close (all)
	 */
	public void testHandleFault_protocolex() {
		
		// reset result
		result = "";
		
		// we want one false response:
		soaphandler1_MessageResultDesired = ResultDesired.TRUE;
		soaphandler1_FaultResultDesired = ResultDesired.TRUE;
		soaphandler2_MessageResultDesired = ResultDesired.TRUE;
		soaphandler2_FaultResultDesired = ResultDesired.TRUE;
		logicalhandler1_MessageResultDesired = ResultDesired.TRUE;
		logicalhandler1_FaultResultDesired = ResultDesired.PROTOCOL_EXCEPTION;
		logicalhandler2_MessageResultDesired = ResultDesired.TRUE;
		logicalhandler2_FaultResultDesired = ResultDesired.TRUE;
		
		HandlerChainProcessor processor = new HandlerChainProcessor(handlers);
		
		Exception e = null;
		try {
			// notice all handlers are closed in this scenario, and we get an exception
			processor.processFault(new SoapMessageContext(new MessageContext()), HandlerChainProcessor.Direction.IN);
		} catch (ProtocolException pe) {
			e = pe;
		}
		
		assertNotNull(e);
		assertEquals("S2f:S1f:L1f:L2c:L1c:S1c:S2c:", result);
	}
	
	
	
	private class SOAPHandler1 implements SOAPHandler {
		
		public Set getHeaders() {
			return null;
		}

		public void close(javax.xml.ws.handler.MessageContext messagecontext) {
			result = result.concat("S1c:");
		}

		public boolean handleFault(javax.xml.ws.handler.MessageContext messagecontext) {
			result = result.concat("S1f:");
			if (soaphandler1_FaultResultDesired == ResultDesired.TRUE)
				return true;
			else if (soaphandler1_FaultResultDesired == ResultDesired.FALSE)
				return false;
			else if (soaphandler1_FaultResultDesired == ResultDesired.PROTOCOL_EXCEPTION)
				throw new ProtocolException();
			else if (soaphandler1_FaultResultDesired == ResultDesired.OTHER_EXCEPTION)
				throw new RuntimeException();
			
			// default
			return true;
		}

		public boolean handleMessage(javax.xml.ws.handler.MessageContext messagecontext) {
			result = result.concat("S1m:");
			if (soaphandler1_MessageResultDesired == ResultDesired.TRUE)
				return true;
			else if (soaphandler1_MessageResultDesired == ResultDesired.FALSE)
				return false;
			else if (soaphandler1_MessageResultDesired == ResultDesired.PROTOCOL_EXCEPTION)
				throw new ProtocolException();
			else if (soaphandler1_MessageResultDesired == ResultDesired.OTHER_EXCEPTION)
				throw new RuntimeException();
			
			// default
			return true;
		}
		
	}
	

	private class SOAPHandler2 implements SOAPHandler {

		public Set getHeaders() {
			return null;
		}

		public void close(javax.xml.ws.handler.MessageContext messagecontext) {
			result = result.concat("S2c:");
		}

		public boolean handleFault(javax.xml.ws.handler.MessageContext messagecontext) {
			result = result.concat("S2f:");
			if (soaphandler2_FaultResultDesired == ResultDesired.TRUE)
				return true;
			else if (soaphandler2_FaultResultDesired == ResultDesired.FALSE)
				return false;
			else if (soaphandler2_FaultResultDesired == ResultDesired.PROTOCOL_EXCEPTION)
				throw new ProtocolException();
			else if (soaphandler2_FaultResultDesired == ResultDesired.OTHER_EXCEPTION)
				throw new RuntimeException();
			
			// default
			return true;
		}

		public boolean handleMessage(javax.xml.ws.handler.MessageContext messagecontext) {
			result = result.concat("S2m:");
			if (soaphandler2_MessageResultDesired == ResultDesired.TRUE)
				return true;
			else if (soaphandler2_MessageResultDesired == ResultDesired.FALSE)
				return false;
			else if (soaphandler2_MessageResultDesired == ResultDesired.PROTOCOL_EXCEPTION)
				throw new ProtocolException();
			else if (soaphandler2_MessageResultDesired == ResultDesired.OTHER_EXCEPTION)
				throw new RuntimeException();
			
			// default
			return true;
		}
		
	}
	

	private class LogicalHandler1 implements LogicalHandler {

		public void close(javax.xml.ws.handler.MessageContext messagecontext) {
			result = result.concat("L1c:");
		}

		public boolean handleFault(javax.xml.ws.handler.MessageContext messagecontext) {
			result = result.concat("L1f:");
			if (logicalhandler1_FaultResultDesired == ResultDesired.TRUE)
				return true;
			else if (logicalhandler1_FaultResultDesired == ResultDesired.FALSE)
				return false;
			else if (logicalhandler1_FaultResultDesired == ResultDesired.PROTOCOL_EXCEPTION)
				throw new ProtocolException();
			else if (logicalhandler1_FaultResultDesired == ResultDesired.OTHER_EXCEPTION)
				throw new RuntimeException();
			
			// default
			return true;
		}

		public boolean handleMessage(javax.xml.ws.handler.MessageContext messagecontext) {
			result = result.concat("L1m:");
			if (logicalhandler1_MessageResultDesired == ResultDesired.TRUE)
				return true;
			else if (logicalhandler1_MessageResultDesired == ResultDesired.FALSE)
				return false;
			else if (logicalhandler1_MessageResultDesired == ResultDesired.PROTOCOL_EXCEPTION)
				throw new ProtocolException();
			else if (logicalhandler1_MessageResultDesired == ResultDesired.OTHER_EXCEPTION)
				throw new RuntimeException();
			
			// default
			return true;
		}
		
	}
	

	private class LogicalHandler2 implements LogicalHandler {

		public void close(javax.xml.ws.handler.MessageContext messagecontext) {
			result = result.concat("L2c:");
		}

		public boolean handleFault(javax.xml.ws.handler.MessageContext messagecontext) {
			result = result.concat("L2f:");
			if (logicalhandler2_FaultResultDesired == ResultDesired.TRUE)
				return true;
			else if (logicalhandler2_FaultResultDesired == ResultDesired.FALSE)
				return false;
			else if (logicalhandler2_FaultResultDesired == ResultDesired.PROTOCOL_EXCEPTION)
				throw new ProtocolException();
			else if (logicalhandler2_FaultResultDesired == ResultDesired.OTHER_EXCEPTION)
				throw new RuntimeException();
			
			// default
			return true;
		}

		public boolean handleMessage(javax.xml.ws.handler.MessageContext messagecontext) {
			result = result.concat("L2m:");
			if (logicalhandler2_MessageResultDesired == ResultDesired.TRUE)
				return true;
			else if (logicalhandler2_MessageResultDesired == ResultDesired.FALSE)
				return false;
			else if (logicalhandler2_MessageResultDesired == ResultDesired.PROTOCOL_EXCEPTION)
				throw new ProtocolException();
			else if (logicalhandler2_MessageResultDesired == ResultDesired.OTHER_EXCEPTION)
				throw new RuntimeException();
			
			// default
			return true;
		}
		
	}

}
