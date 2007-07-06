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
package org.apache.axis2.transport.jms;

import edu.emory.mathcs.backport.java.util.concurrent.Executor;
import org.apache.axiom.om.util.UUIDGenerator;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.addressing.RelatesTo;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.engine.AxisEngine;
import org.apache.axis2.util.MessageContextBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Queue;
import javax.jms.Topic;
import javax.naming.Context;
import javax.xml.stream.XMLStreamException;
import java.io.InputStream;

/**
 * This is the actual receiver which listens for and accepts JMS messages, and
 * hands them over to be processed by a worker thread. An instance of this
 * class is created for each JMSConnectionFactory, but all instances may and
 * will share the same worker thread pool.
 */
public class JMSMessageReceiver implements MessageListener {

    private static final Log log = LogFactory.getLog(JMSMessageReceiver.class);

    /**
     * The thread pool of workers
     */
    private Executor workerPool = null;
    /**
     * The Axis configuration context
     */
    private ConfigurationContext axisConf = null;
    /**
     * A reference to the JMS Connection Factory
     */
    private JMSConnectionFactory jmsConFac = null;

    /**
     * Create a new JMSMessage receiver
     *
     * @param jmsConFac  the JMS connection factory associated with
     * @param workerPool the worker thead pool to be used
     * @param axisConf   the Axis2 configuration
     */
    JMSMessageReceiver(JMSConnectionFactory jmsConFac,
                       Executor workerPool, ConfigurationContext axisConf) {
        this.jmsConFac = jmsConFac;
        this.workerPool = workerPool;
        this.axisConf = axisConf;
    }

    /**
     * Return the Axis configuration
     *
     * @return the Axis configuration
     */
    public ConfigurationContext getAxisConf() {
        return axisConf;
    }

    /**
     * Set the worker thread pool
     *
     * @param workerPool the worker thead pool
     */
    public void setWorkerPool(Executor workerPool) {
        this.workerPool = workerPool;
    }

    /**
     * The entry point on the recepit of each JMS message
     *
     * @param message the JMS message received
     */
    public void onMessage(Message message) {
        // directly create a new worker and delegate processing
        try {
            if (log.isDebugEnabled()) {
                StringBuffer sb = new StringBuffer();
                sb.append("Received JMS message to destination : " + message.getJMSDestination());                
                sb.append("\nMessage ID : " + message.getJMSMessageID());
                sb.append("\nCorrelation ID : " + message.getJMSCorrelationID());
                sb.append("\nReplyTo ID : " + message.getJMSReplyTo());
                log.debug(sb.toString());
            }
        } catch (JMSException e) {
            e.printStackTrace();
        }
        workerPool.execute(new Worker(message));
    }

    /**
     * Creates an Axis MessageContext for the received JMS message and
     * sets up the transports and various properties
     *
     * @param message the JMS message
     * @return the Axis MessageContext
     */
    private MessageContext createMessageContext(Message message) {

        InputStream in = JMSUtils.getInputStream(message);

        try {
            MessageContext msgContext = axisConf.createMessageContext();

            // get destination and create correct EPR
            Destination dest = message.getJMSDestination();
            String destinationName = null;
            if (dest instanceof Queue) {
                destinationName = ((Queue) dest).getQueueName();
            } else if (dest instanceof Topic) {
                destinationName = ((Topic) dest).getTopicName();
            }

            String serviceName = jmsConFac.getServiceByDestination(destinationName);

            // hack to get around the crazy Active MQ dynamic queue and topic issues
            if (serviceName == null) {
                String provider = (String) jmsConFac.getProperties().get(
                        Context.INITIAL_CONTEXT_FACTORY);
                if (provider.indexOf("activemq") != -1) {
                    serviceName = jmsConFac.getServiceNameForDestination(
                            ((dest instanceof Queue ?
                                    JMSConstants.ACTIVEMQ_DYNAMIC_QUEUE :
                                    JMSConstants.ACTIVEMQ_DYNAMIC_TOPIC) + destinationName));
                }
            }


            if (serviceName != null) {
                // set to bypass dispatching and handover directly to this service
                msgContext.setAxisService(
                        axisConf.getAxisConfiguration().getService(serviceName));
            }

            msgContext.setIncomingTransportName(Constants.TRANSPORT_JMS);
            msgContext.setTransportIn(
                    axisConf.getAxisConfiguration().getTransportIn(Constants.TRANSPORT_JMS));

            msgContext.setTransportOut(
                    axisConf.getAxisConfiguration().getTransportOut(Constants.TRANSPORT_JMS));
            // the reply is assumed to be on the JMSReplyTo destination, using
            // the same incoming connection factory
            
            
            JMSOutTransportInfo jmsOutTransportInfo;
            
            if ((jmsConFac.getJndiUser() == null) || (jmsConFac.getJndiPass() == null))
            	jmsOutTransportInfo= new JMSOutTransportInfo(jmsConFac.getConFactory(), message.getJMSReplyTo());
            else
            	jmsOutTransportInfo= new JMSOutTransportInfo(jmsConFac.getConFactory(), jmsConFac.getUser(), jmsConFac.getPass(), message.getJMSReplyTo());
            
            msgContext.setProperty(Constants.OUT_TRANSPORT_INFO, jmsOutTransportInfo);

            msgContext.setServerSide(true);
            msgContext.setMessageID(message.getJMSMessageID());

            Destination replyTo = message.getJMSReplyTo();
            String jndiDestinationName = null;
            if (replyTo == null) {
                Parameter param = msgContext.getAxisService().getParameter(JMSConstants.REPLY_PARAM);
                if (param != null && param.getValue() != null) {
                    jndiDestinationName = (String) param.getValue();
                }
            }

            if (jndiDestinationName != null) {
                msgContext.setReplyTo(jmsConFac.getEPRForDestination(jndiDestinationName));
            }

            String soapAction = JMSUtils.getProperty(message, JMSConstants.SOAPACTION);
            if (soapAction != null) {
                msgContext.setSoapAction(soapAction);
            }

            msgContext.setEnvelope(
                    JMSUtils.getSOAPEnvelope(message, msgContext, in));

            // set correlation id
            String correlationId = message.getJMSCorrelationID();
            if (correlationId != null && correlationId.length() > 0) {
                msgContext.setProperty(JMSConstants.JMS_COORELATION_ID, correlationId);
                msgContext.setRelationships(
                    new RelatesTo[] { new RelatesTo(correlationId) });
            }

            return msgContext;

        } catch (JMSException e) {
            handleException("JMS Exception reading the destination name", e);
        } catch (AxisFault e) {
            handleException("Axis fault creating the MessageContext", e);
        } catch (XMLStreamException e) {
            handleException("Error reading the SOAP envelope", e);
        }
        return null;
    }

    private void handleException(String msg, Exception e) {
        log.error(msg, e);
        throw new AxisJMSException(msg, e);
    }

    /**
     * The actual Runnable Worker implementation which will process the
     * received JMS messages in the worker thread pool
     */
    class Worker implements Runnable {

        private Message message = null;

        Worker(Message message) {
            this.message = message;
        }

        public void run() {
            MessageContext msgCtx = createMessageContext(message);

            AxisEngine engine = new AxisEngine(msgCtx.getConfigurationContext());
            try {
                log.debug("Delegating JMS message for processing to the Axis engine");
                try {
                    engine.receive(msgCtx);
                } catch (AxisFault e) {
                    log.debug("Exception occured when receiving the SOAP message", e);
                    if (msgCtx.isServerSide()) {
                        MessageContext faultContext = MessageContextBuilder.createFaultMessageContext(msgCtx, e);
                        engine.sendFault(faultContext);
                    }
                }
            } catch (AxisFault af) {
                log.error("JMS Worker [" + Thread.currentThread().getName() +
                        "] Encountered an Axis Fault : " + af.getMessage(), af);
            }
        }
    }
}
