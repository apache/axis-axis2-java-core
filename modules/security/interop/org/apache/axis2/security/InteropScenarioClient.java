/*
* Copyright 2004,2005 The Apache Software Foundation.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.apache.axis2.security;

import org.apache.axis2.oasis.ping.PingPortStub;
import org.xmlsoap.ping.Ping;
import org.xmlsoap.ping.PingDocument;
import org.xmlsoap.ping.PingResponse;
import org.xmlsoap.ping.PingResponseDocument;
import org.xmlsoap.ping.TicketType;

/**
 * Client for the interop service
 * This MUST be used with the codegen'ed classes
 * @author Ruchith Fernando (ruchith.fernando@gmail.com)
 */
public class InteropScenarioClient {
	
	public static void main(String[] args) throws Exception {
		
		String clientRepo = args[0];
		String url = args[1];
		
		TicketType ticket = TicketType.Factory.newInstance();
		ticket.setStringValue("Ticket string value");
		
		Ping ping = Ping.Factory.newInstance();
		ping.setText("Testing axis2-wss4j module");
		ping.setTicket(ticket);
		
		PingDocument pingDoc = PingDocument.Factory.newInstance();		
		pingDoc.setPing(ping);

		PingPortStub stub = new PingPortStub(clientRepo,url);
		PingResponseDocument pingResDoc = stub.Ping(pingDoc);
		
		PingResponse pingRes = pingResDoc.getPingResponse();
		
		System.out.println(pingRes.getText());
		System.exit(0);
		
	}
    
    
}
