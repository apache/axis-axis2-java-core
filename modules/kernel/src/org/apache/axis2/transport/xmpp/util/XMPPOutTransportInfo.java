package org.apache.axis2.transport.xmpp.util;

import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.transport.OutTransportInfo;

/**
 * 
 * Holds XMPP transport out details
 *
 */
public class XMPPOutTransportInfo implements OutTransportInfo{
	private String contentType = null;
	private String destinationAccount = null;
	private String inReplyTo;
	private EndpointReference from;
	private XMPPConnectionFactory connectionFactory = null;
	
	public XMPPOutTransportInfo(){
		
	}
	
	public XMPPOutTransportInfo(String transportUrl) throws AxisFault {
        this.destinationAccount = XMPPUtils.getAccountName(transportUrl);
	}
	public void setContentType(String contentType) {
		this.contentType = contentType;
	}		
		
    public XMPPConnectionFactory getConnectionFactory() {
		return connectionFactory;
	}

    public void setConnectionFactory(XMPPConnectionFactory connectionFactory) {
		this.connectionFactory = connectionFactory;
	}
	
	public String getDestinationAccount() {
		return destinationAccount;
	}

	public EndpointReference getFrom() {
		return from;
	}

	public void setFrom(EndpointReference from) {
		this.from = from;
	}

	public String getInReplyTo() {
		return inReplyTo;
	}

	public void setInReplyTo(String inReplyTo) {
		this.inReplyTo = inReplyTo;
	}

	public void setDestinationAccount(String destinationAccount) {
		this.destinationAccount = destinationAccount;
	}

	public String getContentType() {
		return contentType;
	}	
}
