package org.apache.axis2.jaxws.handler;

import java.util.ArrayList;
import java.util.List;

import javax.xml.ws.ProtocolException;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.LogicalHandler;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.apache.axis2.jaxws.ExceptionFactory;
import org.apache.axis2.jaxws.i18n.Messages;


public class HandlerChainProcessor {

	public enum Direction {
		IN, OUT
	};

	// the type of message, not indicative of one-way vs. request-response
	public enum MEP {
		REQUEST, RESPONSE
	};
	
	private MessageContext mc;
	private ArrayList<Handler> handlers = null;
	
	// track start/end of logical and protocol handlers in the list
	// The two scenarios are:  1) run logical handlers only, 2) run all handlers
	// logical start is always 0
	// protocol start is always logicalLength + 1
	// list end is always handlers.size()-1
	private int logicalLength = 0; 
	
	private final static int SUCCESSFUL = 0;
	private final static int FAILED = 1;
	private final static int PROTOCOL_EXCEPTION = 2;
	private final static int OTHER_EXCEPTION = 3;
	// save it if Handler.handleMessage throws one in HandlerChainProcessor.handleMessage
	private RuntimeException savedException;

	/*
	 * HandlerChainProcess expects null, empty list, or an already-sorted
	 * list.  If the chain passed into here came from our HandlerChainResolver,
	 * it is sorted already.  If a client app created or manipulated the list,
	 * it may not be sorted.  The processChain and processFault methods check
	 * for this by calling verifyChain.
	 */
	public HandlerChainProcessor(ArrayList<Handler> chain) {
		if (chain == null) {
			handlers = new ArrayList<Handler>();
		}
		else
			handlers = chain;
	}
	
	/*
	 * verifyChain will check that the chain is properly sorted, since it may be
	 * a chain built or modified by a client application.  Also keep track of
	 * start/end for each type of handler.
	 */
	private void verifyChain() throws WebServiceException {
		boolean protocolHandlersStarted = false;
		for (Handler handlerClass : handlers) {
			if (LogicalHandler.class.isAssignableFrom(handlerClass.getClass())) {
				if (protocolHandlersStarted)
					throw ExceptionFactory.makeWebServiceException(Messages.getMessage("handlerChainErr0", handlerClass.getClass().getName()));
				else {
					logicalLength++;
				}
			}
			else if (SOAPHandler.class.isAssignableFrom(handlerClass.getClass()))
				protocolHandlersStarted = true;
			else if (Handler.class.isAssignableFrom(handlerClass.getClass())) {
				throw ExceptionFactory.makeWebServiceException(Messages.getMessage("handlerChainErr1", handlerClass.getClass().getName()));
			} else {
				throw ExceptionFactory.makeWebServiceException(Messages.getMessage("handlerChainErr2", handlerClass.getClass().getName()));
			}

		}
	}
	

	
	/**
	 * @param mc
	 * By the time processChain method is called, we already have the sorted chain,
	 * and now we have the direction, MEP, MessageContext, and if a response is expected.  We should
	 * be able to handle everything from here, no pun intended.
	 * 
	 * Two things a user of processChain should check when the method completes:
	 * 1.  Has the MessageContext.MESSAGE_OUTBOUND_PROPERTY changed, indicating reversal of message direction
	 * 2.  Has the message been converted to a fault message? (indicated by a flag in the message)
	 */
	public void processChain(MessageContext mc, Direction direction, MEP mep, boolean expectResponse) {
		// make sure it's set:
		mc.put(MessageContext.MESSAGE_OUTBOUND_PROPERTY, (direction == Direction.OUT));
		
		this.mc = mc;
		verifyChain();
		
		if (SOAPMessageContext.class.isAssignableFrom(mc.getClass())) {  // all handlers
			if (direction == Direction.OUT) {  // 9.3.2 outbound
				callGenericHandlers(mep, expectResponse, 0, handlers.size()-1, direction);
			}
			else { // IN case - 9.3.2 inbound
				callGenericHandlers(mep, expectResponse, handlers.size()-1, 0, direction);
			}
		}
		else {  // logical handlers only
			if (direction == Direction.OUT) {  // 9.3.2 outbound
				callGenericHandlers(mep, expectResponse, 0, logicalLength-1, direction);
			}
			else { // IN case - 9.3.2 inbound
				callGenericHandlers(mep, expectResponse, logicalLength-1, 0, direction);
			}
		}
	}
	
	
	/*
	 * This is the implementation of JAX-WS 2.0 section 9.3.2.1
	 */
	private void callGenericHandlers(MEP mep, boolean expectResponse, int start, int end, Direction direction) throws RuntimeException {
		
		// if this is a response message, expectResponse should always be false
		if (mep == MEP.RESPONSE)
			expectResponse = false;
		
		int i = start;
		int result = SUCCESSFUL;
		
		// declared and initialized just in case we need them
		// in a reverse flow situation
		int newStart = 0, newStart_inclusive = 0, newEnd = 0;
		Direction newDirection = direction;

		if (direction == Direction.OUT) {
			while ((i <= end) && (result == SUCCESSFUL)) {
				result = handleMessage(((Handler)handlers.get(i)), mc, direction, expectResponse);
				newStart = i-1;
				newStart_inclusive = i;
				newEnd = 0;
				newDirection = Direction.IN;
				i++;
			}
		}
		else { // IN case
			while ((i >= end) && (result == SUCCESSFUL)) {
				result = handleMessage(((Handler)handlers.get(i)), mc, direction, expectResponse);
				newStart = i+1;
				newStart_inclusive = i;
				newEnd = handlers.size()-1;
				newDirection = Direction.OUT;
				i--;
			}
		}
		
		if (newDirection == direction) // we didn't actually process anything, probably due to empty list
			return;  // no need to continue
		
		// 9.3.2.3 in all situations, we want to close as many handlers as
		// were invoked prior to completion or exception throwing
		if (expectResponse) {
			if (result == FAILED) {
				// we should only use callGenericHandlers_avoidRecursion in this case
				callGenericHandlers_avoidRecursion(newStart, newEnd, newDirection);
				callCloseHandlers(newStart_inclusive, newEnd, newDirection);
			} else if (result == PROTOCOL_EXCEPTION) {
				try {
					callGenericHandleFault(newStart, newEnd, newDirection);
					callCloseHandlers(newStart_inclusive, newEnd, newDirection);
				} catch (RuntimeException re) {
					callCloseHandlers(newStart_inclusive, newEnd, newDirection);
					// TODO: NLS log and throw
					throw re;
				}
			} else if (result == OTHER_EXCEPTION) {
				callCloseHandlers(newStart_inclusive, newEnd, newDirection);
				// savedException initialized in HandlerChainProcessor.handleMessage
				// TODO: NLS log and throw
				throw savedException;
			}
		} else { // everything was successful OR finished processing handlers
			callCloseHandlers(newStart_inclusive, newEnd, newDirection);
		}
		
	}

	
	/*
	 * callGenericHandlers_avoidRecursion should ONLY be called from one place.
	 * We can safely assume no false returns and no exceptions will be thrown
	 * from here since the handlers we will be calling have all already
	 * succeeded in callGenericHandlers.
	 */
	private void callGenericHandlers_avoidRecursion(int start,
			int end, Direction direction) {
		int i = start;

		if (direction == Direction.OUT) {
			for (; i <= end; i++) {
				((Handler) handlers.get(i)).handleMessage(mc);
			}
		} else { // IN case
			for (; i >= end; i--) {
				((Handler) handlers.get(i)).handleMessage(mc);
			}
		}
	}
	
	
	/**
	 * Calls handleMessage on the Handler.
	 * If an exception is thrown and a response is expected, the MessageContext is updated with the handler information
	 * @returns SUCCESSFUL if successfully, UNSUCCESSFUL if false, EXCEPTION if exception thrown
	 */
	private int handleMessage(Handler handler, MessageContext mc, Direction direction,
			boolean expectResponse) throws RuntimeException {
		try {
			boolean success = handler.handleMessage(mc);
			if (success)
				return SUCCESSFUL;
			else {
				if (expectResponse)
					mc.put(MessageContext.MESSAGE_OUTBOUND_PROPERTY, (direction != Direction.OUT));
				return FAILED;
			}
		} catch (RuntimeException re) {  // RuntimeException and ProtocolException
			savedException = re;
			if (expectResponse)
				mc.put(MessageContext.MESSAGE_OUTBOUND_PROPERTY, (direction != Direction.OUT));
			if (ProtocolException.class.isAssignableFrom(re.getClass())) {
				convertToFaultMessage(mc, re);
				return PROTOCOL_EXCEPTION;
			}
			return OTHER_EXCEPTION;
		}

	}
	
	
	/*
	 * start and end should be INclusive of the handlers that have already been
	 * invoked on Handler.handleMessage or Handler.handleFault
	 */
	private void callCloseHandlers(int start, int end,
			Direction direction) {

		if (direction == Direction.OUT) {
			for (int i = start; i <= end; i++) {
				try {
					((Handler) handlers.get(i)).close(mc);
				} catch (Exception e) {
					// TODO: log it, but otherwise ignore
				}
			}
		} else { // IN case
			for (int i = start; i >= end; i--) {
				try {
					((Handler) handlers.get(i)).close(mc);
				} catch (Exception e) {
					// TODO: log it, but otherwise ignore
				}
			}
		}
	}
	
	/*
	 * callHandleFault is available for a server to use when the endpoint
	 * throws an exception or a client when it gets a fault response message
	 * 
	 * In both cases, all of the handlers have run successfully in the
	 * opposite direction as this call to callHandleFault, and thus
	 * should be closed.
	 */
	public void processFault(SOAPMessageContext mc, Direction direction) {
		
		// direction.IN = client
		// direction.OUT = server
		
		// make sure it's right:
		mc.put(MessageContext.MESSAGE_OUTBOUND_PROPERTY, (direction == Direction.OUT));

		verifyChain();
		
		try {
			if (direction == Direction.OUT) {
				callGenericHandleFault(0, handlers.size()-1, direction);
			}
			else { // IN case
				callGenericHandleFault(handlers.size()-1, 0, direction);
			}
		} catch (RuntimeException re) {
			// TODO: log it
			throw re;
		} finally {
			// we can close all the Handlers in reverse order
			if (direction == Direction.OUT) {
				callCloseHandlers(handlers.size()-1, 0, Direction.IN);
			}
			else { // IN case
				callCloseHandlers(0, handlers.size()-1, Direction.OUT);
			}
		}
	}
	

	/*
	 * The callGenericHandleFault caller is responsible for closing any invoked
	 * Handlers.  We don't know how far the Handler.handleMessage calls got
	 * before a failure may have occurred.
	 * 
	 * Regardless of the Handler.handleFault result, the flow is the same (9.3.2.2)
	 */
	private void callGenericHandleFault(int start, int end,
			Direction direction) throws RuntimeException {
		
		int i = start;

		if (direction == Direction.OUT) {
			for (; i <= end; i++) {
				if (((Handler) handlers.get(i)).handleFault(mc) == false) {
					break;
				}
			}
		} else { // IN case
			for (; i >= end; i--) {
				if (((Handler) handlers.get(i)).handleFault(mc) == false) {
					break;
				}
			}
		}
	}
	
	
	private void convertToFaultMessage(MessageContext mc, Exception e) {
		// TODO: implement
		// need to check if message is already a fault message or not,
		// probably by way of a flag (isFault) in the MessageContext or Message
	}
	

}
