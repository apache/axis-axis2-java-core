/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *   * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.apache.axis2.transport.jms;

import javax.jms.Destination;
import javax.jms.Queue;
import javax.jms.Topic;

import org.apache.axis2.transport.testkit.name.Name;
import org.apache.axis2.transport.testkit.tests.Setup;
import org.apache.axis2.transport.testkit.tests.TearDown;
import org.apache.axis2.transport.testkit.tests.Transient;
import org.apache.axis2.transport.testkit.util.PortAllocator;
import org.apache.qpid.AMQException;
import org.apache.qpid.client.AMQConnectionFactory;
import org.apache.qpid.client.AMQDestination;
import org.apache.qpid.client.AMQQueue;
import org.apache.qpid.client.AMQTopic;
import org.apache.qpid.exchange.ExchangeDefaults;
import org.apache.qpid.server.Broker;
import org.apache.qpid.server.BrokerOptions;
import org.apache.qpid.server.registry.ApplicationRegistry;
import org.apache.qpid.server.virtualhost.VirtualHost;

@Name("qpid")
public class QpidTestEnvironment extends JMSTestEnvironment {
    private @Transient PortAllocator portAllocator;
    private @Transient Broker broker;
    private @Transient VirtualHost virtualHost;
    private int port;
    
    @Setup @SuppressWarnings("unused")
    private void setUp(PortAllocator portAllocator) throws Exception {
        this.portAllocator = portAllocator;
        port = portAllocator.allocatePort();
        broker = new Broker();
        BrokerOptions options = new BrokerOptions();
        options.setConfigFile("src/test/conf/qpid/config.xml");
        options.setLogConfigFile("src/test/conf/qpid/log4j.xml");
        options.addPort(port);
        broker.startup(options);
        // null means the default virtual host
        virtualHost = ApplicationRegistry.getInstance().getVirtualHostRegistry().getVirtualHost(null);
        connectionFactory = new AMQConnectionFactory("amqp://guest:guest@clientid/" + virtualHost.getName() + "?brokerlist='tcp://localhost:" + port + "'");
    }

    @TearDown @SuppressWarnings("unused")
    private void tearDown() throws Exception {
        broker.shutdown();
        portAllocator.releasePort(port);
    }

    @Override
    public Queue createQueue(String name) throws AMQException {
        QpidUtil.createQueue(virtualHost, ExchangeDefaults.DIRECT_EXCHANGE_NAME, name);
        return new AMQQueue(ExchangeDefaults.DIRECT_EXCHANGE_NAME, name);
    }

    @Override
    public Topic createTopic(String name) throws AMQException {
        QpidUtil.createQueue(virtualHost, ExchangeDefaults.TOPIC_EXCHANGE_NAME, name);
        return new AMQTopic(ExchangeDefaults.TOPIC_EXCHANGE_NAME, name);
    }

    @Override
    public void deleteDestination(Destination destination) throws Exception {
        QpidUtil.deleteQueue(virtualHost, ((AMQDestination)destination).getRoutingKey().asString());
    }
}
