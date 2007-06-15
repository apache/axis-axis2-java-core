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

package org.apache.axis2.clustering.tribes;

import org.apache.axis2.clustering.ClusteringConstants;
import org.apache.axis2.clustering.configuration.ConfigurationClusteringCommand;
import org.apache.axis2.clustering.configuration.DefaultConfigurationManager;
import org.apache.axis2.clustering.context.ContextClusteringCommand;
import org.apache.axis2.clustering.context.DefaultContextManager;
import org.apache.axis2.clustering.context.commands.ContextClusteringCommandCollection;
import org.apache.axis2.clustering.context.commands.UpdateContextCommand;
import org.apache.axis2.clustering.control.AckCommand;
import org.apache.axis2.clustering.control.ControlCommand;
import org.apache.axis2.clustering.control.GetStateResponseCommand;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.catalina.tribes.Member;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import sun.misc.Queue;

import java.io.Serializable;


public class ChannelListener implements org.apache.catalina.tribes.ChannelListener {
    private static final Log log = LogFactory.getLog(ChannelListener.class);

    private DefaultContextManager contextManager;
    private DefaultConfigurationManager configurationManager;
    private TribesControlCommandProcessor controlCommandProcessor;
    private ChannelSender sender;

    /**
     * The messages received are enqued. Another thread, messageProcessor, will
     * process these messages in the order that they were received.
     */
    private final Queue cmdQueue = new Queue();

    /**
     * The thread which picks up messages from the cmdQueue and processes them.
     */
    private Thread messageProcessor;

    private ConfigurationContext configurationContext;

    public ChannelListener(ConfigurationContext configurationContext,
                           DefaultConfigurationManager configurationManager,
                           DefaultContextManager contextManager,
                           TribesControlCommandProcessor controlCommandProcessor,
                           ChannelSender sender) {
        this.configurationManager = configurationManager;
        this.contextManager = contextManager;
        this.controlCommandProcessor = controlCommandProcessor;
        this.sender = sender;
        this.configurationContext = configurationContext;
        startMessageProcessor();
    }

    public void setContextManager(DefaultContextManager contextManager) {
        this.contextManager = contextManager;
    }

    public void setConfigurationManager(DefaultConfigurationManager configurationManager) {
        this.configurationManager = configurationManager;
    }

    public void setConfigurationContext(ConfigurationContext configurationContext) {
        this.configurationContext = configurationContext;
    }

    public boolean accept(Serializable msg, Member sender) {
        return true;
    }

    public void messageReceived(Serializable msg, Member sender) {

        // If the system has not still been intialized, reject all incoming messages, except the
        // GetStateResponseCommand message
        if (configurationContext.
                getPropertyNonReplicable(ClusteringConstants.CLUSTER_INITIALIZED) == null
            && !(msg instanceof GetStateResponseCommand)) {
            return;
        }
        log.debug("RECEIVED MESSAGE " + msg + " from " + TribesUtil.getHost(sender));

        // Need to process ACKs as soon as they are received since otherwise,
        // unnecessary retransmissions will take place
        if(msg instanceof AckCommand){
            try {
                controlCommandProcessor.process((AckCommand) msg, sender);
            } catch (Exception e) {
                log.error(e);
            }
            return;
        }

        // Add the commands to be precessed to the cmdQueue
        synchronized (cmdQueue) {
            cmdQueue.enqueue(new MemberMessage(msg, sender));
        }
        if (!messageProcessor.isAlive()) {
            startMessageProcessor();
        }
    }

    private void startMessageProcessor() {
        messageProcessor = new Thread(new MessageProcessor(), "ClusteringInComingMessageProcessor");
        messageProcessor.setDaemon(true);
        messageProcessor.setPriority(Thread.MAX_PRIORITY);
        messageProcessor.start();
    }

    /**
     * A container to hold a message and its sender
     */
    private class MemberMessage {
        private Serializable message;
        private Member sender;

        public MemberMessage(Serializable msg, Member sender) {
            this.message = msg;
            this.sender = sender;
        }

        public Serializable getMessage() {
            return message;
        }

        public Member getSender() {
            return sender;
        }
    }

    /**
     * A processor which continuously polls for messages in the cmdQueue and processes them
     */
    private class MessageProcessor implements Runnable {
        public void run() {
            while (true) {
                MemberMessage memberMessage = null;
                try {
                    if (!cmdQueue.isEmpty()) {
                        memberMessage = (MemberMessage) cmdQueue.dequeue();
                    } else {
                        Thread.sleep(1);
                        continue;
                    }

                    Serializable msg = memberMessage.getMessage();
                    if (msg instanceof ContextClusteringCommand && contextManager != null) {
                        ContextClusteringCommand ctxCmd = (ContextClusteringCommand) msg;
                        contextManager.process(ctxCmd);

                        // Sending ACKs for ContextClusteringCommandCollection or
                        // UpdateContextCommand is sufficient
                        if (msg instanceof ContextClusteringCommandCollection ||
                            msg instanceof UpdateContextCommand) {
                            AckCommand ackCmd = new AckCommand(ctxCmd.getUniqueId());

                            // Send the ACK
                            sender.sendToMember(ackCmd, memberMessage.getSender());
                        }
                    } else if (msg instanceof ConfigurationClusteringCommand &&
                               configurationManager != null) {
                        configurationManager.process((ConfigurationClusteringCommand) msg);
                    } else if (msg instanceof ControlCommand && controlCommandProcessor != null) {
                        controlCommandProcessor.process((ControlCommand) msg,
                                                        memberMessage.getSender());
                    }
                } catch (Throwable e) {
                    log.error("Could not process message ", e);
                }
            }
        }
    }
}
