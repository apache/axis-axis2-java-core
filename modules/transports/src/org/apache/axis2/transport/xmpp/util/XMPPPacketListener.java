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

package org.apache.axis2.transport.xmpp.util;

import org.apache.axiom.om.OMException;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.TransportInDescription;
import org.apache.axis2.description.TransportOutDescription;
import org.apache.axis2.engine.AxisEngine;
import org.apache.axis2.transport.TransportUtils;
import org.apache.axis2.util.MessageContextBuilder;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;

import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.concurrent.Executor;

public class XMPPPacketListener implements PacketListener {
	private static final Log log = LogFactory.getLog(XMPPPacketListener.class);
	private XMPPConnectionFactory xmppConnectionFactory = null;
	private ConfigurationContext configurationContext = null;
	private Executor workerPool = null;
    
    public final static String CONTENT_TYPE = "mail.contenttype";

    public XMPPPacketListener(XMPPConnectionFactory xmppConnectionFactory, ConfigurationContext configurationContext, Executor workerPool) {
		this.xmppConnectionFactory = xmppConnectionFactory;
		this.configurationContext = configurationContext;
		this.workerPool = workerPool;
	}

	/**
	 * This method gets triggered when server side gets a message
	 */
	public void processPacket(Packet packet) {
		log.info("Received : "+packet.toXML());
		if(packet instanceof Message){
			workerPool.execute(new Worker(packet));			
		}
	}

	/**
	 * Creates message context using values received in XMPP packet
	 * @param packet
	 * @return MessageContext
	 * @throws AxisFault
	 */
	private MessageContext createMessageContext(Packet packet) throws AxisFault {
		Message message = (Message) packet;

		Boolean isServerSide = (Boolean) message
				.getProperty(XMPPConstants.IS_SERVER_SIDE);
		String serviceName = (String) message
				.getProperty(XMPPConstants.SERVICE_NAME);
		String action = (String) message.getProperty(XMPPConstants.ACTION);
		MessageContext msgContext = null;

		TransportInDescription transportIn = configurationContext
				.getAxisConfiguration().getTransportIn("xmpp");
		TransportOutDescription transportOut = configurationContext
				.getAxisConfiguration().getTransportOut("xmpp");
		if ((transportIn != null) && (transportOut != null)) {
			msgContext = configurationContext.createMessageContext();
			msgContext.setTransportIn(transportIn);
			msgContext.setTransportOut(transportOut);
			if (isServerSide != null) {
				msgContext.setServerSide(isServerSide.booleanValue());
			}
			msgContext.setProperty(
					CONTENT_TYPE,
					"text/xml");
			msgContext.setProperty(
					Constants.Configuration.CHARACTER_SET_ENCODING, "UTF-8");
			msgContext.setIncomingTransportName("xmpp");

			HashMap services = configurationContext.getAxisConfiguration()
					.getServices();

			AxisService axisService = (AxisService) services.get(serviceName);
			msgContext.setAxisService(axisService);
			msgContext.setSoapAction(action);

			// pass the configurationFactory to transport sender
			msgContext.setProperty("XMPPConfigurationFactory",
					this.xmppConnectionFactory);

			if (packet.getFrom() != null) {
				msgContext.setFrom(new EndpointReference(packet.getFrom()));
			}
			if (packet.getTo() != null) {
				msgContext.setTo(new EndpointReference(packet.getTo()));
			}

			XMPPOutTransportInfo xmppOutTransportInfo = new XMPPOutTransportInfo();
			xmppOutTransportInfo
					.setConnectionFactory(this.xmppConnectionFactory);

			String packetFrom = packet.getFrom();
			if (packetFrom != null) {
				EndpointReference fromEPR = new EndpointReference(packetFrom);
				xmppOutTransportInfo.setFrom(fromEPR);
				xmppOutTransportInfo.setDestinationAccount(packetFrom);
			}

			// Save Message-Id to set as In-Reply-To on reply
			String xmppMessageId = packet.getPacketID();
			if (xmppMessageId != null) {
				xmppOutTransportInfo.setInReplyTo(xmppMessageId);
			}
			msgContext.setProperty(
					org.apache.axis2.Constants.OUT_TRANSPORT_INFO,
					xmppOutTransportInfo);
			buildSOAPEnvelope(packet, msgContext);
		} else {
			throw new AxisFault("Either transport in or transport out is null");
		}
		return msgContext;
	}

    /**
     * builds SOAP envelop using message contained in packet
     * @param packet
     * @param msgContext
     * @throws AxisFault
     */
	private void buildSOAPEnvelope(Packet packet, MessageContext msgContext) throws AxisFault{
		Message message = (Message)packet;
		String xml = StringEscapeUtils.unescapeXml(message.getBody());
		InputStream inputStream = new ByteArrayInputStream(xml.getBytes());
		SOAPEnvelope envelope;
		try {
			envelope = TransportUtils.createSOAPMessage(msgContext, inputStream, "text/xml");
			if(msgContext.isServerSide()){
				log.info("Received Envelope : "+xml);
			}
			msgContext.setEnvelope(envelope);
		}catch (OMException e) {
			log.error("Error occured while trying to create " +
					"message content using XMPP message received :"+packet.toXML(), e);
			throw new AxisFault("Error occured while trying to create " +
					"message content using XMPP message received :"+packet.toXML());
		}catch (XMLStreamException e) {
			log.error("Error occured while trying to create " +
					"message content using XMPP message received :"+packet.toXML(), e);
			throw new AxisFault("Error occured while trying to create " +
					"message content using XMPP message received :"+packet.toXML());
		}catch (FactoryConfigurationError e) {
			log.error("Error occured while trying to create " +
					"message content using XMPP message received :"+packet.toXML(), e);
			throw new AxisFault("Error occured while trying to create " +
					"message content using XMPP message received :"+packet.toXML());
		}catch (AxisFault e){
			log.error("Error occured while trying to create " +
					"message content using XMPP message received :"+packet.toXML(), e);
			throw new AxisFault("Error occured while trying to create " +
					"message content using XMPP message received :"+packet.toXML());
		}
	}


	/**
	 * The actual Runnable Worker implementation which will process the
	 * received XMPP messages in the worker thread pool
	 */
	class Worker implements Runnable {
		private Packet packet = null;
		Worker(Packet packet) {
			this.packet = packet;
		}

		public void run() {
			MessageContext msgCtx = null;
			try {
				msgCtx = createMessageContext(packet);
				if(msgCtx.isProcessingFault() && msgCtx.isServerSide()){
					AxisEngine.sendFault(msgCtx);
				}else{
					AxisEngine.receive(msgCtx);	
				}
			} catch (AxisFault e) {
				log.error("Error occurred while sending message"+e);
   				if (msgCtx != null && msgCtx.isServerSide()) {
    				MessageContext faultContext;
					try {
						faultContext = MessageContextBuilder.createFaultMessageContext(msgCtx, e);
	    				AxisEngine.sendFault(faultContext);
					} catch (AxisFault e1) {
						log.error("Error occurred while creating SOAPFault message"+e1);
					}
   				}
			}
		}
	}
}
