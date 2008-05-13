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

package org.apache.axis2.clustering.tribes;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.clustering.ClusterManager;
import org.apache.axis2.clustering.ClusteringConstants;
import org.apache.axis2.clustering.ClusteringFault;
import org.apache.axis2.clustering.RequestBlockingHandler;
import org.apache.axis2.clustering.configuration.ConfigurationManager;
import org.apache.axis2.clustering.configuration.DefaultConfigurationManager;
import org.apache.axis2.clustering.context.ClusteringContextListener;
import org.apache.axis2.clustering.context.ContextManager;
import org.apache.axis2.clustering.context.DefaultContextManager;
import org.apache.axis2.clustering.control.ControlCommand;
import org.apache.axis2.clustering.control.GetConfigurationCommand;
import org.apache.axis2.clustering.control.GetStateCommand;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.HandlerDescription;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.description.PhaseRule;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.engine.DispatchPhase;
import org.apache.axis2.engine.Phase;
import org.apache.catalina.tribes.Channel;
import org.apache.catalina.tribes.ChannelException;
import org.apache.catalina.tribes.ManagedChannel;
import org.apache.catalina.tribes.Member;
import org.apache.catalina.tribes.group.GroupChannel;
import org.apache.catalina.tribes.group.Response;
import org.apache.catalina.tribes.group.RpcChannel;
import org.apache.catalina.tribes.group.interceptors.DomainFilterInterceptor;
import org.apache.catalina.tribes.group.interceptors.OrderInterceptor;
import org.apache.catalina.tribes.group.interceptors.TcpFailureDetector;
import org.apache.catalina.tribes.transport.MultiPointSender;
import org.apache.catalina.tribes.transport.ReceiverBase;
import org.apache.catalina.tribes.transport.ReplicationTransmitter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

public class TribesClusterManager implements ClusterManager {
    public static final int MSG_ORDER_OPTION = 512;
    private static final Log log = LogFactory.getLog(TribesClusterManager.class);

    private DefaultConfigurationManager configurationManager;
    private DefaultContextManager contextManager;

    private HashMap parameters;
    private ManagedChannel channel;
    private RpcChannel rpcChannel;
    private ConfigurationContext configurationContext;
    private ControlCommandProcessor controlCmdProcessor;
    private ChannelListener channelListener;
    private ChannelSender channelSender;
    private MembershipManager membershipManager;

    public TribesClusterManager() {
        parameters = new HashMap();
        controlCmdProcessor = new ControlCommandProcessor(configurationContext);
    }

    public ContextManager getContextManager() {
        return contextManager;
    }

    public ConfigurationManager getConfigurationManager() {
        return configurationManager;
    }

    public void init() throws ClusteringFault {

        AxisConfiguration axisConfig = configurationContext.getAxisConfiguration();
        for (Iterator iterator = axisConfig.getInFlowPhases().iterator();
             iterator.hasNext();) {
            Phase phase = (Phase) iterator.next();
            if (phase instanceof DispatchPhase) {
                RequestBlockingHandler requestBlockingHandler = new RequestBlockingHandler();
                if (!phase.getHandlers().contains(requestBlockingHandler)) {
                    PhaseRule rule = new PhaseRule("Dispatch");
                    rule.setAfter("SOAPMessageBodyBasedDispatcher");
                    rule.setBefore("InstanceDispatcher");
                    HandlerDescription handlerDesc = requestBlockingHandler.getHandlerDesc();
                    handlerDesc.setHandler(requestBlockingHandler);
                    handlerDesc.setName(ClusteringConstants.REQUEST_BLOCKING_HANDLER);
                    handlerDesc.setRules(rule);
                    phase.addHandler(requestBlockingHandler);
                }
                break;
            }
        }
        for (Iterator iterator = axisConfig.getInFaultFlowPhases().iterator();
             iterator.hasNext();) {
            Phase phase = (Phase) iterator.next();
            if (phase instanceof DispatchPhase) {
                RequestBlockingHandler requestBlockingHandler = new RequestBlockingHandler();
                if (!phase.getHandlers().contains(requestBlockingHandler)) {
                    PhaseRule rule = new PhaseRule("Dispatch");
                    rule.setAfter("SOAPMessageBodyBasedDispatcher");
                    rule.setBefore("InstanceDispatcher");
                    HandlerDescription handlerDesc = requestBlockingHandler.getHandlerDesc();
                    handlerDesc.setHandler(requestBlockingHandler);
                    handlerDesc.setName(ClusteringConstants.REQUEST_BLOCKING_HANDLER);
                    handlerDesc.setRules(rule);
                    phase.addHandler(requestBlockingHandler);
                    break;
                }
            }
        }
        membershipManager = new MembershipManager();
        channel = new GroupChannel();
        channelSender = new ChannelSender(channel, membershipManager, synchronizeAllMembers());
        channelListener = new ChannelListener(configurationContext, configurationManager,
                                              contextManager, controlCmdProcessor);

        // Set the maximum number of retries, if message sending to a particular node fails
        Parameter maxRetriesParam = getParameter("maxRetries");
        int maxRetries = 10;
        if (maxRetriesParam != null) {
            maxRetries = Integer.parseInt((String) maxRetriesParam.getValue());
        }
        ReplicationTransmitter replicationTransmitter =
                (ReplicationTransmitter) channel.getChannelSender();
        MultiPointSender multiPointSender = replicationTransmitter.getTransport();
        multiPointSender.setMaxRetryAttempts(maxRetries);

        // Set the domain for this Node
        Parameter domainParam = getParameter(ClusteringConstants.DOMAIN);
        byte[] domain;
        if (domainParam != null) {
            domain = ((String) domainParam.getValue()).getBytes();
        } else {
            domain = "apache.axis2.domain".getBytes();
        }
        channel.getMembershipService().getProperties().setProperty("mcastClusterDomain",
                                                                   new String(domain));

        Parameter membershipSchemeParam = getParameter("membershipScheme");
        String membershipScheme = ClusteringConstants.MembershipScheme.MULTICAST_BASED;
        if (membershipSchemeParam != null) {
            membershipScheme = ((String) membershipSchemeParam.getValue()).trim();
        }

        if (membershipScheme.equals(ClusteringConstants.MembershipScheme.WKA_BASED)) {
            log.info("Using WKA based membership management scheme");
            channel.setMembershipService(new WkaMembershipService());
        } else if (membershipScheme.equals(ClusteringConstants.MembershipScheme.MULTICAST_BASED)) {
            log.info("Using multicast based membership management scheme");
            configureMulticastParameters(channel);
        }

        // Add all the ChannelInterceptors
        addInterceptors(channel, domain);

        channel.addChannelListener(channelListener);

        TribesMembershipListener membershipListener = new TribesMembershipListener(membershipManager);
        channel.addMembershipListener(membershipListener);
        try {
            channel.start(Channel.DEFAULT);
            String localHost = TribesUtil.getLocalHost(channel);
            if (localHost.startsWith("127.0.")) {
                channel.stop(Channel.DEFAULT);
                throw new ClusteringFault("Cannot join cluster using IP " + localHost +
                                          ". Please set an IP address other than " +
                                          localHost + " in your /etc/hosts file or set the " +
                                          ClusteringConstants.LOCAL_IP_ADDRESS +
                                          " System property and retry.");
            }
        } catch (ChannelException e) {
            throw new ClusteringFault("Error starting Tribes channel", e);
        }

        // RpcChannel is a ChannelListener. When the reply to a particular request comes back, it
        // picks it up. Each RPC is given a UUID, hence can correlate the request-response pair
        rpcChannel =
                new RpcChannel(domain, channel,
                               new InitializationRequestHandler(controlCmdProcessor));

        log.info("Local Member " + TribesUtil.getLocalHost(channel));
        TribesUtil.printMembers(membershipManager);

        // If configuration management is enabled, get the latest config from a neighbour
        if (configurationManager != null) {
            configurationManager.setSender(channelSender);
            initializeSystem(rpcChannel, new GetConfigurationCommand());
        }

        // If context replication is enabled, get the latest state from a neighbour
        if (contextManager != null) {
            contextManager.setSender(channelSender);
            channelListener.setContextManager(contextManager);
            initializeSystem(rpcChannel, new GetStateCommand());
            ClusteringContextListener contextListener = new ClusteringContextListener(channelSender);
            configurationContext.addContextListener(contextListener);
        }

        configurationContext.
                setNonReplicableProperty(ClusteringConstants.CLUSTER_INITIALIZED, "true");
    }

    //TODO: The order of the interceptors will depend on the membership scheme
    private void addInterceptors(ManagedChannel channel, byte[] domain) {

        // Add a DomainFilterInterceptor
        channel.getMembershipService().setDomain(domain);
        DomainFilterInterceptor dfi = new DomainFilterInterceptor();
        dfi.setDomain(domain);
        channel.addInterceptor(dfi);

        // Add the NonBlockingCoordinator. This is used for leader election
        /*nbc = new NonBlockingCoordinator() {
            public void fireInterceptorEvent(InterceptorEvent event) {
                String status = event.getEventTypeDesc();
                System.err.println("$$$$$$$$$$$$ NBC status=" + status);
                int type = event.getEventType();
            }
        };
        nbc.setPrevious(dfi);
        channel.addInterceptor(nbc);*/

        // Add the OrderInterceptor to preserve sender ordering
        OrderInterceptor orderInterceptor = new OrderInterceptor();
        orderInterceptor.setOptionFlag(MSG_ORDER_OPTION);
        channel.addInterceptor(orderInterceptor);

        // Add a AtMostOnceInterceptor to support at-most-once message processing semantics
        AtMostOnceInterceptor atMostOnceInterceptor = new AtMostOnceInterceptor();
        channel.addInterceptor(atMostOnceInterceptor);

        // Add a reliable failure detector
        TcpFailureDetector tcpFailureDetector = new TcpFailureDetector();
        tcpFailureDetector.setPrevious(dfi);
        channel.addInterceptor(tcpFailureDetector);

//        if(memberDiscoverMode = WKA){
//            TcpPing
//            TcpFailure
//            StaticMembership
//        }
        /*StaticMembershipInterceptor staticMembershipInterceptor = new StaticMembershipInterceptor();
        channel.addInterceptor(staticMembershipInterceptor);
        try {
            staticMembershipInterceptor.addStaticMember(new StaticMember("10.100.1.190", 4000, 10));
        } catch (IOException e) {
            e.printStackTrace();
        }*/
    }

    private void configureMulticastParameters(ManagedChannel channel) {
        Properties mcastProps = channel.getMembershipService().getProperties();
        Parameter mcastAddress = getParameter("multicastAddress");
        if (mcastAddress != null) {
            mcastProps.setProperty("mcastAddress", ((String) mcastAddress.getValue()).trim());
        }
        Parameter mcastBindAddress = getParameter("multicastBindAddress");
        if (mcastBindAddress != null) {
            mcastProps.setProperty("mcastBindAddress", ((String) mcastBindAddress.getValue()).trim());
        }

        Parameter mcastPort = getParameter("multicastPort");
        if (mcastPort != null) {
            mcastProps.setProperty("mcastPort", ((String) mcastPort.getValue()).trim());
        }
        Parameter mcastFrequency = getParameter("multicastFrequency");
        if (mcastFrequency != null) {
            mcastProps.setProperty("mcastFrequency", ((String) mcastFrequency.getValue()).trim());
        }
        Parameter mcastMemberDropTime = getParameter("multicastMemberDropTime");
        if (mcastMemberDropTime != null) {
            mcastProps.setProperty("memberDropTime", ((String) mcastMemberDropTime.getValue()).trim());
        }

        // Set the IP address that will be advertised by this node
        ReceiverBase receiver = (ReceiverBase) channel.getChannelReceiver();
        Parameter tcpListenHost = getParameter("tcpListenHost");
        if (tcpListenHost != null) {
            String host = ((String) tcpListenHost.getValue()).trim();
            mcastProps.setProperty("tcpListenHost", host);
            mcastProps.setProperty("bindAddress", host);
            receiver.setAddress(host);
        }
        String localIP = System.getProperty(ClusteringConstants.LOCAL_IP_ADDRESS);
        if (localIP != null) {
            receiver.setAddress(localIP);
        }

        Parameter tcpListenPort = getParameter("tcpListenPort");
        if (tcpListenPort != null) {
            String port = ((String) tcpListenPort.getValue()).trim();
            mcastProps.setProperty("tcpListenPort", port);
            receiver.setPort(Integer.parseInt(port));
        }

        /*mcastProps.setProperty("mcastClusterDomain", "catalina");*/
    }

    /**
     * Get some information from a neighbour. This information will be used by this node to
     * initialize itself
     *
     * @param rpcChannel The utility for sending RPC style messages to the channel
     * @param command    The control command to send
     * @throws ClusteringFault If initialization code failed on this node
     */
    private void initializeSystem(RpcChannel rpcChannel, ControlCommand command)
            throws ClusteringFault {
        // If there is at least one member in the cluster,
        //  get the current initialization info from a member
        int numberOfTries = 0; // Don't keep on trying indefinitely

        // Keep track of members to whom we already sent an initialization command
        // Do not send another request to these members
        List sentMembersList = new ArrayList();
        sentMembersList.add(TribesUtil.getLocalHost(channel));
        Member[] members = membershipManager.getMembers();
        if (members.length == 0) {
            return;
        }

        while (members.length > 0 && numberOfTries < 5) {
            Member member = (numberOfTries == 0) ?
                            membershipManager.getLongestLivingMember() : // First try to get from the longest member alive
                            membershipManager.getRandomMember(); // Else get from a random member
            String memberHost = TribesUtil.getHost(member);
            log.info("Trying to send intialization request to " + memberHost);
            try {
                if (!sentMembersList.contains(memberHost)) {
                    Response[] responses = rpcChannel.send(new Member[]{member},
                                                           command,
                                                           RpcChannel.FIRST_REPLY,
                                                           Channel.SEND_OPTIONS_ASYNCHRONOUS,
                                                           10000);
                    if (responses.length > 0) {
                        ((ControlCommand) responses[0].getMessage()).execute(configurationContext); // Do the initialization
                        break;
                    }
                }
            } catch (ChannelException e) {
                log.error("Cannot get initialization information from " +
                          memberHost + ". Will retry in 2 secs.", e);
                sentMembersList.add(memberHost);
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ignored) {
                    log.debug("Interrupted", ignored);
                }
            }
            numberOfTries++;
            members = membershipManager.getMembers();
            if (numberOfTries >= members.length) {
                break;
            }
        }
    }

    public void setConfigurationManager(ConfigurationManager configurationManager) {
        this.configurationManager = (DefaultConfigurationManager) configurationManager;
        this.configurationManager.setSender(channelSender);
    }

    public void setContextManager(ContextManager contextManager) {
        this.contextManager = (DefaultContextManager) contextManager;
        this.contextManager.setSender(channelSender);
    }

    public void addParameter(Parameter param) throws AxisFault {
        parameters.put(param.getName(), param);
    }

    public void deserializeParameters(OMElement parameterElement) throws AxisFault {
        throw new UnsupportedOperationException();
    }

    public Parameter getParameter(String name) {
        return (Parameter) parameters.get(name);
    }

    public ArrayList getParameters() {
        ArrayList list = new ArrayList();
        for (Iterator it = parameters.keySet().iterator(); it.hasNext();) {
            list.add(parameters.get(it.next()));
        }
        return list;
    }

    public boolean isParameterLocked(String parameterName) {
        Parameter parameter = (Parameter) parameters.get(parameterName);
        return parameter != null && parameter.isLocked();
    }

    public void removeParameter(Parameter param) throws AxisFault {
        parameters.remove(param.getName());
    }

    public void shutdown() throws ClusteringFault {
        log.debug("Enter: TribesClusterManager::shutdown");
        if (channel != null) {
            try {
                channel.removeChannelListener(rpcChannel);
                channel.removeChannelListener(channelListener);
                channel.stop(Channel.DEFAULT);
            } catch (ChannelException e) {

                if (log.isDebugEnabled()) {
                    log.debug("Exit: TribesClusterManager::shutdown");
                }

                throw new ClusteringFault(e);
            }
        }
        log.debug("Exit: TribesClusterManager::shutdown");
    }

    public void setConfigurationContext(ConfigurationContext configurationContext) {
        this.configurationContext = configurationContext;
        controlCmdProcessor.setConfigurationContext(configurationContext);
        if (channelListener != null) {
            channelListener.setConfigurationContext(configurationContext);
        }
        if (configurationManager != null) {
            configurationManager.setConfigurationContext(configurationContext);
        }
        if (contextManager != null) {
            contextManager.setConfigurationContext(configurationContext);
        }
    }

    /**
     * Method to check whether all members in the cluster have to be kep in sync at all times.
     * Typically, this will require each member in the cluster to ACKnowledge receipt of a
     * particular message, which may have a significant performance hit.
     *
     * @return true - if all members in the cluster should be kept in sync at all times, false
     *         otherwise
     */
    public boolean synchronizeAllMembers() {
        Parameter syncAllParam = getParameter(ClusteringConstants.SYNCHRONIZE_ALL_MEMBERS);
        return syncAllParam == null || Boolean.parseBoolean((String) syncAllParam.getValue());
    }
}
