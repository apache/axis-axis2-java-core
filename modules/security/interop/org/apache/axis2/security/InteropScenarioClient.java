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
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.oasis.ping.PingPortStub;
import org.apache.axis2.security.handler.WSSHandlerConstants;
import org.apache.axis2.security.handler.config.InflowConfiguration;
import org.apache.axis2.security.handler.config.OutflowConfiguration;
import org.apache.ws.commons.soap.SOAP11Constants;
import org.apache.ws.commons.soap.SOAP12Constants;
import org.xmlsoap.ping.*;

/**
 * Client for the interop service
 * This MUST be used with the codegen'ed classes
 */
public class InteropScenarioClient {

    String soapNsURI = SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI;

    public InteropScenarioClient(boolean useSOAP12InStaticConfigTest) {
        if (useSOAP12InStaticConfigTest) {
            soapNsURI = SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI;
        }
    }

    public void invokeWithStaticConfig(String clientRepo, String url) throws Exception {
        TicketType ticket = TicketType.Factory.newInstance();
        ticket.setId("My ticket Id");

        Ping ping = Ping.Factory.newInstance();
        ping.setText("Testing axis2-wss4j module");
        ping.setTicket(ticket);

        PingDocument pingDoc = PingDocument.Factory.newInstance();
        pingDoc.setPing(ping);

        PingPortStub stub = new PingPortStub(
                ConfigurationContextFactory.createConfigurationContextFromFileSystem(
                        clientRepo, null), url);

        //Enable MTOM to those scenarios where they are configured using:
        //<optimizeParts>xpathExpression</optimizeParts>
        stub._getServiceClient().getOptions().setProperty(Constants.Configuration.ENABLE_MTOM, Constants.VALUE_TRUE);
        stub._getServiceClient().getOptions().setSoapVersionURI(soapNsURI);

        PingResponseDocument pingResDoc = stub.Ping(pingDoc);

        PingResponse pingRes = pingResDoc.getPingResponse();

        System.out.println(pingRes.getText());
    }

    public void invokeWithGivenConfig(String clientRepo,
                                      String url, OutflowConfiguration outflowConfig,
                                      InflowConfiguration inflowConfig) throws Exception {
        TicketType ticket = TicketType.Factory.newInstance();
        ticket.setId("My ticket Id");

        Ping ping = Ping.Factory.newInstance();
        ping.setText("Testing axis2-wss4j module");
        ping.setTicket(ticket);

        PingDocument pingDoc = PingDocument.Factory.newInstance();
        pingDoc.setPing(ping);

        PingPortStub stub = new PingPortStub(
                ConfigurationContextFactory.createConfigurationContextFromFileSystem(clientRepo, null), url);

        //Enable MTOM to those scenarios where they are configured using:
        //<optimizeParts>xpathExpression</optimizeParts>
        stub._getServiceClient().getOptions().setProperty(Constants.Configuration.ENABLE_MTOM, Constants.VALUE_TRUE);

        //Engage the security module
        stub._getServiceClient().engageModule(new javax.xml.namespace.QName("security"));

        if (outflowConfig != null) {
            stub._getServiceClient().getOptions().setProperty(WSSHandlerConstants.OUTFLOW_SECURITY, outflowConfig.getProperty());
        }
        if (inflowConfig != null) {
            stub._getServiceClient().getOptions().setProperty(WSSHandlerConstants.INFLOW_SECURITY, inflowConfig.getProperty());
        }
        PingResponseDocument pingResDoc = stub.Ping(pingDoc);

        PingResponse pingRes = pingResDoc.getPingResponse();

        System.out.println(pingRes.getText());
        stub = null;
    }

}
