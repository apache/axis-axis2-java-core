/*
 * Created on Apr 8, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.apache.axis.saaj;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

/**
 * @author Ashutosh Shahi ashutosh.shahi@gmail.com
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class MessageFactoryImpl extends MessageFactory {

	/* (non-Javadoc)
	 * @see javax.xml.soap.MessageFactory#createMessage()
	 */
	public SOAPMessage createMessage() throws SOAPException {
		SOAPEnvelopeImpl env = new SOAPEnvelopeImpl();
		SOAPMessageImpl message = new SOAPMessageImpl(env);
		return message;
	}

	/* (non-Javadoc)
	 * @see javax.xml.soap.MessageFactory#createMessage(javax.xml.soap.MimeHeaders, java.io.InputStream)
	 */
	public SOAPMessage createMessage(MimeHeaders mimeheaders,
			InputStream inputstream) throws IOException, SOAPException {
		// TODO Auto-generated method stub
		SOAPMessageImpl message = new SOAPMessageImpl(inputstream, false,mimeheaders);
		return message;
	}

}
