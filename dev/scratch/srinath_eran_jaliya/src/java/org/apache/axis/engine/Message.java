/*
 * Created on Oct 18, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.apache.axis.engine;

import org.apache.axis.encoding.DeseializationContext;
import org.apache.axis.encoding.SerializationContext;
import org.apache.axis.message.SOAPEnvelope;
import org.apache.axis.message.SOAPHeaders;

/**
 * @author hemapani
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class Message {
    private SOAPEnvelope envelope;
    private SOAPHeaders headers; 
    
	
    /**
     * @return Returns the envelope.
     */
    public SOAPEnvelope getEnvelope() {
        return envelope;
    }
    /**
     * @param envelope The envelope to set.
     */
    public void setEnvelope(SOAPEnvelope envelope) {
        this.envelope = envelope;
    }
    /**
     * @return Returns the headers.
     */
    public SOAPHeaders getHeaders() {
        return headers;
    }
    /**
     * @param headers The headers to set.
     */
    public void setHeaders(SOAPHeaders headers) {
        this.headers = headers;
    }
	public Message(DeseializationContext dc){
		
	}
	
	private String charSetEncoding ="";
	/**
	 * @return Returns the charSetEncoding.
	 */
	public String getCharSetEncoding() {
		return charSetEncoding;
	}
	/**
	 * @param charSetEncoding The charSetEncoding to set.
	 */
	public void setCharSetEncoding(String charSetEncoding) {
		this.charSetEncoding = charSetEncoding;
	}
	/**
	 * @param contentType The contentType to set.
	 */
	public void setContentType(String contentType) {
		this.contentType = contentType;
	}
	private String contentType ="txt/xml";
	public String getContentType(){
		return contentType;
	}
	public void serialize(SerializationContext sc){
		
	}
}
