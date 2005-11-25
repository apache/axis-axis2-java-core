/**
 * 
 */
package org.apache.axis2.saaj2;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

public class MessageFactoryImpl extends MessageFactory {

	/* (non-Javadoc)
	 * @see javax.xml.soap.MessageFactory#createMessage()
	 */
	public SOAPMessage createMessage() throws SOAPException {
		// TODO TODO
		throw new UnsupportedOperationException("TODO");
	}

	/* (non-Javadoc)
	 * @see javax.xml.soap.MessageFactory#createMessage(javax.xml.soap.MimeHeaders, java.io.InputStream)
	 */
	public SOAPMessage createMessage(MimeHeaders mimeheaders,
			InputStream inputstream) throws IOException, SOAPException {
		// TODO TODO
		throw new UnsupportedOperationException("TODO");
	}

}
