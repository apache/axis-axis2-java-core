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

import org.apache.axis2.Constants;
import org.apache.axis2.oasis.ping.PingPortStub;
import org.apache.axis2.oasis.ping.databinding.org.xmlsoap.Ping;
import org.apache.axis2.oasis.ping.databinding.org.xmlsoap.PingDocument;
import org.apache.axis2.oasis.ping.databinding.org.xmlsoap.PingResponse;
import org.apache.axis2.oasis.ping.databinding.org.xmlsoap.PingResponseDocument;
import org.apache.axis2.oasis.ping.databinding.org.xmlsoap.TicketType;
import org.apache.axis2.security.handler.WSSHandlerConstants;
import org.apache.axis2.security.handler.config.InflowConfiguration;
import org.apache.axis2.security.handler.config.OutflowConfiguration;


/**
 * Client for the interop service
 * This MUST be used with the codegen'ed classes
 */
public class InteropScenarioClient {


    public void invokeWithStaticConfig(String clientRepo, String url) throws Exception {
        TicketType ticket = TicketType.Factory.newInstance();
        ticket.setId("My ticket Id");

        Ping ping = Ping.Factory.newInstance();
        ping.setText("Testing axis2-wss4j module");
        ping.setTicket(ticket);

        PingDocument pingDoc = PingDocument.Factory.newInstance();
        pingDoc.setPing(ping);

        PingPortStub stub = new PingPortStub(clientRepo,url);

        //Enable MTOM to those scenarios where they are configured using:
        //<optimizeParts>xpathExpression</optimizeParts>
        stub._getClientOptions().setProperty(Constants.Configuration.ENABLE_MTOM, Constants.VALUE_TRUE);

        PingResponseDocument pingResDoc = stub.Ping(pingDoc);

        PingResponse pingRes = pingResDoc.getPingResponse();

        System.out.println(pingRes.getText());
    }

    public void invokeWithGivenConfig(String clientRepo, String url, OutflowConfiguration outflowConfig, InflowConfiguration inflowConfig) throws Exception {
        TicketType ticket = TicketType.Factory.newInstance();
        ticket.setId("My ticket Id");

        Ping ping = Ping.Factory.newInstance();
        ping.setText("Testing axis2-wss4j module");
        ping.setTicket(ticket);

        PingDocument pingDoc = PingDocument.Factory.newInstance();
        pingDoc.setPing(ping);

        PingPortStub stub = new PingPortStub(clientRepo,url);

        //Enable MTOM to those scenarios where they are configured using:
        //<optimizeParts>xpathExpression</optimizeParts>
        stub._getClientOptions().setProperty(Constants.Configuration.ENABLE_MTOM, Constants.VALUE_TRUE);

        //Engage the security module
        stub.engageModule("security");

        if(outflowConfig !=null){
        	stub._getClientOptions().setProperty(WSSHandlerConstants.OUTFLOW_SECURITY, outflowConfig.getProperty());
        }
        if(inflowConfig != null) {
        	stub._getClientOptions().setProperty(WSSHandlerConstants.INFLOW_SECURITY, inflowConfig.getProperty());
        }
        PingResponseDocument pingResDoc = stub.Ping(pingDoc);

        PingResponse pingRes = pingResDoc.getPingResponse();

        System.out.println(pingRes.getText());
    }

}
