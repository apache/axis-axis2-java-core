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
package sample.jms.server;

import org.apache.activemq.broker.BrokerService;
import org.apache.axis2.Constants;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.description.TransportInDescription;
import org.apache.axis2.engine.ListenerManager;
import org.apache.axis2.transport.jms.JMSListener;

public class SampleJMSServer {

    private static final String PROVIDER_URL = "tcp://localhost:61616";
    private static final String DEFAULT_SERVER_REPOSITORY = "server_repository";
    
    public static void main(String[] args) throws Exception {
        
        BrokerService brokerService = new BrokerService();
        brokerService.setUseJmx(false);
        brokerService.addConnector(PROVIDER_URL);
        brokerService.start();
        
        String repository = (args.length != 0) ? args[0] : DEFAULT_SERVER_REPOSITORY;
        String axis2xml = repository + "/conf/axis2.xml";
        
        ConfigurationContext configurationContext = ConfigurationContextFactory
                .createConfigurationContextFromFileSystem(repository, axis2xml);
        
        JMSListener receiver = new JMSListener();
        ListenerManager listenerManager = configurationContext
                .getListenerManager();
        TransportInDescription trsIn = configurationContext
                .getAxisConfiguration().getTransportIn(Constants.TRANSPORT_JMS);
        trsIn.setReceiver(receiver);
        if (listenerManager == null) {
            listenerManager = new ListenerManager();
            listenerManager.init(configurationContext);
        }
        listenerManager.addListener(trsIn, true);
        receiver.init(configurationContext, trsIn);
        receiver.start();
    }
}
