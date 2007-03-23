/*
 * Copyright 2001-2004 The Apache Software Foundation.
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
package sample.jms.client;

import javax.naming.Context;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.StAXUtils;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.client.async.AsyncResult;
import org.apache.axis2.client.async.Callback;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.transport.jms.JMSConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class JMSEchoClient {
    
private static final Log log = LogFactory.getLog(JMSEchoClient.class);
    
    private static final String INITIAL_CONTEXT_FACTORY = "org.apache.activemq.jndi.ActiveMQInitialContextFactory";
    private static final String PROVIDER_URL = "tcp://localhost:61616";
    private static final String CONNECTION_FACTORY_NAME = "QueueConnectionFactory";
    private static final String DESTINATION_NAME = "dynamicQueues/TestQueue";
    // USERNAME and PASSWORD are required only if broker has been 
    // configured for authanticated connection to the context.
    // Works for SonicMQ but, couldn't find a way to do this for ActiveMQ :)
    private static final String USERNAME = null;      
    private static final String PASSWORD = null;

    // Client (service consumer/JMS sender) variables
//    private String targetEprUrl;
//    private ConfigurationContext clientConfigContext;
    private static final String CLIENT_REPOSITORY = "client_repository";
    public static void main(String[] args) throws Exception {
        
        String repository = (args.length != 0) ? args[0] : CLIENT_REPOSITORY;
        ConfigurationContext configurationContext = ClientUtil.createConfigurationContext(repository);
        
//      Service and transport variables
        
        QName serviceName = new QName("EchoXMLService");
        QName operationName = new QName("echoOMElement");

            ////////////////////////////////////////////////////
            // Client side processes. 
            ////////////////////////////////////////////////////
            
            OMElement payload = ClientUtil.createPayload(serviceName, operationName);
            
            Options options = new Options();
            String jmsEndpointURL = createJmsEndpointURL();
            options.setTo(new EndpointReference(jmsEndpointURL));
            options.setTransportInProtocol(Constants.TRANSPORT_JMS);
            options.setAction("urn:echoOMElement");

            JMSClientCallback callback = new JMSClientCallback();
                        
            //create out-in service for the client
            AxisService clientService = ClientUtil.createOutInService(serviceName,
                    operationName); 
            
            ServiceClient sender = new ServiceClient(configurationContext, clientService );
            sender.setOptions(options);
            
            sender.sendReceiveNonBlocking(operationName, payload, callback);

            int index = 0;
            while (! callback.isFinish()) {
                Thread.sleep(1000);
                index++;
                if (index > 100) {
                    throw new AxisFault(
                            "Server was shutdown as the async response take too long to complete");
                }
            }        
    }
        
  
    // Creates vendor specific JMS endpoint URL.
    private static String createJmsEndpointURL () {
        return new StringBuffer("jms:/").append(DESTINATION_NAME)
        .append("?").append(JMSConstants.CONFAC_JNDI_NAME_PARAM).append("=").append(CONNECTION_FACTORY_NAME)
        .append("&").append(Context.INITIAL_CONTEXT_FACTORY).append("=").append(INITIAL_CONTEXT_FACTORY)
        .append("&").append(Context.PROVIDER_URL).append("=").append(PROVIDER_URL)
        .append("&").append(Context.SECURITY_PRINCIPAL).append("=").append(USERNAME)
        .append("&").append(Context.SECURITY_CREDENTIALS).append("=").append(PASSWORD)
        .toString();
    }
    
    
}
