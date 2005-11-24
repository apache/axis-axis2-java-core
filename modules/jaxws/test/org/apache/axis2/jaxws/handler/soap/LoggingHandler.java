package org.apache.axis2.jaxws.handler.soap;

import java.io.PrintStream;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.ProtocolException;
import javax.xml.ws.handler.HandlerInfo;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

public class LoggingHandler implements SOAPHandler<SOAPMessageContext> {

	// change this to redirect output if desired
	private static PrintStream out = System.out;
	
	// used to hold initialization data
	private Map config;
	
	public Set<QName> getHeaders() {
		return null;
	}

	public boolean handleMessage(SOAPMessageContext context)
			throws RuntimeException, ProtocolException {
		logToSystemOut(context);
		return true;
	}

	public boolean handleFault(SOAPMessageContext context) throws RuntimeException,
			ProtocolException {
		logToSystemOut(context);
		return true;
	}

	public void close(MessageContext context) {
		//Nothing to clean up
	}

	public void init(HandlerInfo config) throws WebServiceException {
		this.config = config.getHandlerConfig();
	}

	public void destroy() throws WebServiceException {
		//nothing to clean up
	}
	
	/*
	 * Check the MESSAGE_OUTBOUND_PROPERTY in the context
	 * to see if this is an outgoing or incoming message.
	 * Write a brief message to the print stream and
	 * output the message. The writeTo() method can throw
	 * SOAPException or IOException
	 */
	private void logToSystemOut(SOAPMessageContext context) {
		Boolean outboundProperty = (Boolean)
		context.getProperty(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
		
		if (outboundProperty.booleanValue()) {
			out.println("\nLOG MESSAGE:::Outbound message:");
		} else {
			out.println("\nLOG MESSAGE:::Inbound message:");
		}
		
		SOAPMessage message = context.getMessage();
		try {
			message.writeTo(out);
			out.println("");   // just to add a newline
		} catch (Exception e) {
			out.println("Exception in handler: " + e);
		}
	}

}
