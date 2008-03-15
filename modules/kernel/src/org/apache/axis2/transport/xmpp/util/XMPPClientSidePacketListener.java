package org.apache.axis2.transport.xmpp.util;

import org.apache.axis2.context.MessageContext;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class XMPPClientSidePacketListener implements PacketListener {
	private Log log = LogFactory.getLog(XMPPClientSidePacketListener.class);
	private MessageContext messageContext = null;
	private boolean responseReceived;

	public XMPPClientSidePacketListener(MessageContext messageContext){
		this.messageContext = messageContext;
	}

	/**
	 * This method will be triggered, when a message is arrived at client side
	 */
	public void processPacket(Packet packet) {		
		Message message = (Message)packet;
		String xml = StringEscapeUtils.unescapeXml(message.getBody());
		log.info("Client received message : "+xml);
		this.responseReceived = true;
		InputStream inputStream = new ByteArrayInputStream(xml.getBytes());
		messageContext.setProperty(MessageContext.TRANSPORT_IN, inputStream);
	}

	/**
	 * Indicates response message is received at client side.
	 * @see processPacket(Packet packet)
	 * @return
	 */
	public boolean isResponseReceived() {
		return responseReceived;
	}

}
