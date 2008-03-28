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

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.FromContainsFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.filter.ToContainsFilter;

import java.util.HashMap;

public class XMPPConnectionFactory {
	private static Log log = LogFactory.getLog(XMPPConnectionFactory.class);
	private XMPPConnection xmppConnection = null;
	private PacketFilter packetFilter = null;
	private HashMap xmppConnections = new HashMap();

	public XMPPConnectionFactory(){}

	/**
	 * Connects to a XMPP server based on the details available in serverCredentials object
	 * @param serverCredentials
	 * @throws XMPPException 
	 */
	public void connect(final XMPPServerCredentials serverCredentials) throws AxisFault {
		if(XMPPConstants.XMPP_SERVER_TYPE_JABBER.equals(serverCredentials.getServerType())){
			ConnectionListener connectionListener = null;
			try 
			{
				//XMPPConnection.DEBUG_ENABLED = true;
				xmppConnection = new XMPPConnection(serverCredentials.getServerUrl());
				xmppConnection.connect();
				connectionListener = new ConnectionListener(){

					public void connectionClosed() {
						log.debug("Connection closed normally");
					}

					public void connectionClosedOnError(
							Exception e1) {
						log.debug("Connection to "+serverCredentials.getServerUrl()
								+ " closed with error.",e1);
						log.debug("Retrying to connect in 2 secs");
						try
						{
							Thread.sleep(2000);
							xmppConnection = new XMPPConnection(serverCredentials.getServerUrl());
							log.debug("connected to "+serverCredentials.getServerUrl());
						} catch (InterruptedException e2) {
							log.debug("Sleep interrupted.",e2);
						}
					}

					public void reconnectingIn(int seconds) {			
					}

					public void reconnectionFailed(Exception e) {
					}

					public void reconnectionSuccessful() {
					}
				};
				xmppConnection.addConnectionListener(connectionListener);
			}
			catch(XMPPException e){
				log.error("Failed to connect to server :"+serverCredentials.getServerUrl(), e);
				throw new AxisFault("Failed to connect to server :"+serverCredentials.getServerUrl());
			}

			//Pause for a small time before trying to login.
			//This prevents random ssl exception from Smack API
			try {
				Thread.sleep(100);
			} catch (InterruptedException e5) {
				log.debug("Sleep interrupted ",e5);
			}

			if(xmppConnection.isConnected()){
				if(! xmppConnection.isAuthenticated()){
					try {
						xmppConnection.login(serverCredentials.getAccountName()+"@"+
								serverCredentials.getServerUrl(), 
								serverCredentials.getPassword(),
								serverCredentials.getResource(),
								true);
					} catch (XMPPException e) {
						try {
							log.error("Login failed for "
									+serverCredentials.getAccountName()
									+"@"+serverCredentials.getServerUrl() 
									+".Retrying in 2 secs",e); 
							Thread.sleep(2000);
							xmppConnection.login(serverCredentials.getAccountName()+"@"+
									serverCredentials.getServerUrl(), 
									serverCredentials.getPassword(),
									serverCredentials.getResource(),
									true);
						} catch (InterruptedException e1) {
							log.error("Sleep interrupted.",e1);
						} catch (XMPPException e2) {
							log.error("Login failed for : "+serverCredentials.getAccountName()
									+"@"+serverCredentials.getServerUrl(),e2);
							throw new AxisFault("Login failed for : "+serverCredentials.getAccountName()
									+"@"+serverCredentials.getServerUrl());
						}
					}
					//Listen for Message type packets from specified server url
					//packetFilter = new AndFilter(new PacketTypeFilter(Message.class), 
					//		new FromContainsFilter(serverCredentials.getServerUrl()));
					packetFilter = new FromContainsFilter(serverCredentials.getServerUrl());					
				}
			}
		}else if(XMPPConstants.XMPP_SERVER_TYPE_GOOGLETALK.equals(serverCredentials.getServerType())){
			try {
				ConnectionConfiguration connectionConfiguration = 
					new ConnectionConfiguration(XMPPConstants.GOOGLETALK_URL
						,XMPPConstants.GOOGLETALK_PORT
						,XMPPConstants.GOOGLETALK_SERVICE_NAME);				
				xmppConnection = new XMPPConnection(connectionConfiguration);
				xmppConnection.connect();

				xmppConnection.login(serverCredentials.getAccountName()
						, serverCredentials.getPassword()
						,serverCredentials.getResource(),
						true);
				//packetFilter = new AndFilter(new PacketTypeFilter(Message.class), 
				//		new FromContainsFilter(XMPPConstants.GOOGLETALK_FROM));
				//packetFilter = new FromContainsFilter(XMPPConstants.GOOGLETALK_FROM);
				packetFilter = new ToContainsFilter("@gmail.com");
				
			} catch (XMPPException e1) {
				log.error("Error occured while connecting to Googletalk server.",e1);
				throw new AxisFault("Error occured while connecting to Googletalk server.");
			}
		}
	} 

	public XMPPConnection getConnection(String connectionIdentifier){
		return (XMPPConnection)xmppConnections.get(connectionIdentifier);
	}


	public XMPPConnection getXmppConnection() {
		return xmppConnection;
	}

	public void setXmppConnection(XMPPConnection xmppConnection) {
		this.xmppConnection = xmppConnection;
	}

	public void listen(XMPPPacketListener packetListener){
		xmppConnection.addPacketListener(packetListener,packetFilter);
	}

	public void stop() {}
}