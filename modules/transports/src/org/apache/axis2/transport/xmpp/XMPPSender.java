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

package org.apache.axis2.transport.xmpp;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.client.Options;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.description.TransportOutDescription;
import org.apache.axis2.description.WSDL2Constants;
import org.apache.axis2.handlers.AbstractHandler;
import org.apache.axis2.transport.OutTransportInfo;
import org.apache.axis2.transport.TransportSender;
import org.apache.axis2.transport.xmpp.util.XMPPClientSidePacketListener;
import org.apache.axis2.transport.xmpp.util.XMPPConnectionFactory;
import org.apache.axis2.transport.xmpp.util.XMPPConstants;
import org.apache.axis2.transport.xmpp.util.XMPPOutTransportInfo;
import org.apache.axis2.transport.xmpp.util.XMPPServerCredentials;
import org.apache.axis2.transport.xmpp.util.XMPPUtils;
import org.apache.axis2.util.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.Message;

public class XMPPSender extends AbstractHandler implements TransportSender {
	Log log = null;
    XMPPConnectionFactory connectionFactory;
	
    public XMPPSender() {
        log = LogFactory.getLog(XMPPSender.class);
    }
    
	public void cleanup(MessageContext msgContext) throws AxisFault {	
	}

    /**
     * Initialize the transport sender by reading pre-defined connection factories for
     * outgoing messages. These will create sessions (one per each destination dealt with)
     * to be used when messages are being sent.
     * @param confContext the configuration context
     * @param transportOut the transport sender definition from axis2.xml
     * @throws AxisFault on error
     */	
	public void init(ConfigurationContext confContext,
			TransportOutDescription transportOut) throws AxisFault {
		//if connection details are available from axis configuration
		//use those & connect to jabber server(s)
		XMPPServerCredentials serverCredentials = new XMPPServerCredentials();
		getConnectionDetailsFromAxisConfiguration(serverCredentials,transportOut);
		connectionFactory = new XMPPConnectionFactory();
		connectionFactory.connect(serverCredentials);		
	}

	/**
	 * Extract connection details from Client options
	 * @param msgCtx
	 */
	private void connectUsingClientOptions(MessageContext msgCtx) throws AxisFault{		
		XMPPServerCredentials serverCredentials = new XMPPServerCredentials();
		getConnectionDetailsFromClientOptions(serverCredentials,msgCtx);
		connectionFactory = new XMPPConnectionFactory();
		connectionFactory.connect(serverCredentials);
	}
	
	public void stop() {}

	public InvocationResponse invoke(MessageContext msgContext)
			throws AxisFault {
        String targetAddress = (String) msgContext.getProperty(
                Constants.Configuration.TRANSPORT_URL);
            if (targetAddress != null) {
                sendMessage(msgContext, targetAddress, null);
            } else if (msgContext.getTo() != null && !msgContext.getTo().hasAnonymousAddress()) {
                targetAddress = msgContext.getTo().getAddress();

                if (!msgContext.getTo().hasNoneAddress()) {
                    sendMessage(msgContext, targetAddress, null);
                } else {
                    //Don't send the message.
                    return InvocationResponse.CONTINUE;
                }
            } else if (msgContext.isServerSide()) {
                // get the out transport info for server side when target EPR is unknown
                sendMessage(msgContext, null,
                    (OutTransportInfo) msgContext.getProperty(Constants.OUT_TRANSPORT_INFO));
            }
            return InvocationResponse.CONTINUE;
	}

    /**
     * Send the given message over XMPP transport
     *
     * @param msgCtx the axis2 message context
     * @throws AxisFault on error
     */
    public void sendMessage(MessageContext msgCtx, String targetAddress,
        OutTransportInfo outTransportInfo) throws AxisFault {
		XMPPConnection xmppConnection = null;
		XMPPOutTransportInfo xmppOutTransportInfo = null;
		
		//if on client side,create connection to xmpp server
		if(!msgCtx.isServerSide()){
			connectUsingClientOptions(msgCtx);
		}
		
		Message message = new Message();
		Options options = msgCtx.getOptions();    	
    	String serviceName = XMPPUtils.getServiceName(targetAddress);    	
    	
		if (targetAddress != null) {
			xmppOutTransportInfo = new XMPPOutTransportInfo(targetAddress);
			xmppOutTransportInfo.setConnectionFactory(connectionFactory);
		} else if (msgCtx.getTo() != null &&
				!msgCtx.getTo().hasAnonymousAddress()) {
			//TODO 
		} else if (msgCtx.isServerSide()) {
			xmppOutTransportInfo = (XMPPOutTransportInfo)
			msgCtx.getProperty(Constants.OUT_TRANSPORT_INFO);
		}
    	
    	
		if(msgCtx.isServerSide()){
			xmppConnection = xmppOutTransportInfo.getConnectionFactory().getXmppConnection();
			message.setProperty(XMPPConstants.IS_SERVER_SIDE, new Boolean(false));
			message.setProperty(XMPPConstants.IN_REPLY_TO, xmppOutTransportInfo.getInReplyTo());
		}else{
			xmppConnection = xmppOutTransportInfo.getConnectionFactory().getXmppConnection();
			message.setProperty(XMPPConstants.IS_SERVER_SIDE, new Boolean(true));
			message.setProperty(XMPPConstants.SERVICE_NAME, serviceName);
			message.setProperty(XMPPConstants.ACTION, options.getAction());
		}
		
    	if(xmppConnection == null){
    		handleException("Connection to XMPP Server is not established.");    		
    	}
		
		//initialize the chat manager using connection
		ChatManager chatManager = xmppConnection.getChatManager();
		Chat chat = chatManager.createChat(xmppOutTransportInfo.getDestinationAccount(), null);		
		
		try 
		{
			OMElement msgElement = msgCtx.getEnvelope();
			if (msgCtx.isDoingREST()) {
				msgElement = msgCtx.getEnvelope().getBody().getFirstElement();
			}
			boolean waitForResponse =
				msgCtx.getOperationContext() != null &&
				WSDL2Constants.MEP_URI_OUT_IN.equals(
						msgCtx.getOperationContext().getAxisOperation().getMessageExchangePattern());
			
			
			String soapMessage = msgElement.toString();
			//int endOfXMLDeclaration = soapMessage.indexOf("?>");
			//String modifiedSOAPMessage = soapMessage.substring(endOfXMLDeclaration+2);
			message.setBody(soapMessage);	
			
			XMPPClientSidePacketListener xmppClientSidePacketListener = null;
			if(waitForResponse && !msgCtx.isServerSide()){
				PacketFilter filter = new PacketTypeFilter(message.getClass());				
				xmppClientSidePacketListener = new XMPPClientSidePacketListener(msgCtx);
				xmppConnection.addPacketListener(xmppClientSidePacketListener,filter);
			}			

			chat.sendMessage(message);
			log.debug("Sent message :"+message.toXML());

			//If this is on client side, wait for the response from server.
			//Is this the best way to do this?
			if(waitForResponse && !msgCtx.isServerSide()){
				while(! xmppClientSidePacketListener.isResponseReceived()){
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						log.debug("Sleep interrupted",e);
					}		
				}
				xmppConnection.disconnect();
			}

		} catch (XMPPException e) {
			log.error("Error occurred while sending the message : "+message.toXML(),e);
			handleException("Error occurred while sending the message : "+message.toXML(),e);
		}finally{
			if(!msgCtx.isServerSide()){
				xmppConnection.disconnect();
			}
		}
    }	
    
    
    /**
     * Extract connection details from axis2.xml's transportsender section
     * @param serverCredentials
     * @param transportOut
     */
	private void getConnectionDetailsFromAxisConfiguration(XMPPServerCredentials serverCredentials,
			TransportOutDescription transportOut){
		if(transportOut != null){
			Parameter serverUrl = transportOut.getParameter(XMPPConstants.XMPP_SERVER_URL);
			if (serverUrl != null) {
				serverCredentials.setServerUrl(Utils.getParameterValue(serverUrl));
			}
			
			Parameter userName = transportOut.getParameter(XMPPConstants.XMPP_SERVER_USERNAME);
			if (userName != null) {
				serverCredentials.setAccountName(Utils.getParameterValue(userName));
			}
		
			Parameter password = transportOut.getParameter(XMPPConstants.XMPP_SERVER_PASSWORD);
			if (password != null) {
				serverCredentials.setPassword(Utils.getParameterValue(password));
			}

			Parameter serverType = transportOut.getParameter(XMPPConstants.XMPP_SERVER_TYPE);			
			if (serverType != null) {
				serverCredentials.setServerType(Utils.getParameterValue(serverType));
			}			
		}
	}
	
	/**
	 * Extract connection details from client options
	 * @param serverCredentials
	 * @param msgContext
	 */
	private void getConnectionDetailsFromClientOptions(XMPPServerCredentials serverCredentials,
			MessageContext msgContext){
		Options clientOptions = msgContext.getOptions();

		if (clientOptions.getProperty(XMPPConstants.XMPP_SERVER_USERNAME) != null){
			serverCredentials.setAccountName((String)clientOptions.getProperty(XMPPConstants.XMPP_SERVER_USERNAME));
		}
		if (clientOptions.getProperty(XMPPConstants.XMPP_SERVER_PASSWORD) != null){
			serverCredentials.setPassword((String)clientOptions.getProperty(XMPPConstants.XMPP_SERVER_PASSWORD));
		}
		if (clientOptions.getProperty(XMPPConstants.XMPP_SERVER_URL) != null){
			serverCredentials.setServerUrl((String)clientOptions.getProperty(XMPPConstants.XMPP_SERVER_URL));
		}
		if (clientOptions.getProperty(XMPPConstants.XMPP_SERVER_TYPE) != null){
			serverCredentials.setServerType((String)clientOptions.getProperty(XMPPConstants.XMPP_SERVER_TYPE));
		}		
	}  
	
    private void handleException(String msg, Exception e) throws AxisFault {
        log.error(msg, e);
        throw new AxisFault(msg, e);
    }
    private void handleException(String msg) throws AxisFault {
        log.error(msg);
        throw new AxisFault(msg);
    }
}