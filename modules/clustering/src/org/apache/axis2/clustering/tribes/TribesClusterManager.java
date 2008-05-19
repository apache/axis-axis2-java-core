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
import org.apache.catalina.tribes.group.interceptors.StaticMembershipInterceptor;
import org.apache.catalina.tribes.group.interceptors.TcpFailureDetector;
import org.apache.catalina.tribes.group.interceptors.TcpPingInterceptor;
import org.apache.catalina.tribes.membership.StaticMember;
import org.apache.catalina.tribes.transport.MultiPointSender;
import org.apache.catalina.tribes.transport.ReceiverBase;
import org.apache.catalina.tribes.transport.ReplicationTransmitter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

/**
 * The main ClusterManager class for the Tribes based clustering implementation
 */
public class TribesClusterManager implements ClusterManager {
    public static final int MSG_ORDER_OPTION = 512;
    private static final Log log = LogFactory.getLog(TribesClusterManager.class);

    private DefaultConfigurationManager configurationManager;
    private DefaultContextManager contextManager;

    private HashMap<String, Parameter> parameters;
    private ManagedChannel channel;
    private RpcChannel rpcChannel;
    private ConfigurationContext configurationContext;
    private ControlCommandProcessor controlCmdProcessor;
    private ChannelListener channelListener;
    private ChannelSender channelSender;
    private MembershipManager membershipManager;
    private StaticMembershipInterceptor staticMembershipInterceptor;
    private org.apache.axis2.clustering.Member[] members;

    public TribesClusterManager() {
        parameters = new HashMap<String, Parameter>();
        controlCmdProcessor = new ControlCommandProcessor(configurationContext);
    }

    public void setMembers(org.apache.axis2.clustering.Member[] members) {
        this.members = members;
    }

    public org.apache.axis2.clustering.Member[] getMembers() {
        return members;
    }

    public ContextManager getContextManager() {
        return contextManager;
    }

    public ConfigurationManager getConfigurationManager() {
        return configurationManager;
    }

    /**
     * Initialize the cluster.
     *
     * @throws ClusteringFault If initialization fails
     */
    public void init() throws ClusteringFault {
        log.info("Initializing cluster...");
        addRequestBlockingHandlerToInFlows();
        membershipManager = new MembershipManager();
        channel = new GroupChannel();
        channelSender = new ChannelSender(channel, membershipManager, synchronizeAllMembers());
        channelListener =
                new ChannelListener(configurationContext, configurationManager, contextManager);

        setMaximumRetries();
        byte[] domain = getClusterDomain();
        String membershipScheme = getMembershipScheme();

        // Add all the ChannelInterceptors
        addInterceptors(channel, domain, membershipScheme);

        // Membership scheme handling
        //TODO: if it is a WKA scheme, connect to a WKA and get a list of members. Add the members
        // TODO: to the membership manager
        configureMembershipScheme(domain, membershipScheme);

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
                                          localHost + " in the axis2.xml file"); 
            }
        } catch (ChannelException e) {
            String msg = "Error starting Tribes channel";
            log.error(msg, e);
            throw new ClusteringFault(msg, e);
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
        log.info("Cluster initialization completed.");
    }

    /**
     * Get the membership scheme applicable to this cluster
     * @return The membership scheme. Only "wka" & "multicast" are valid return values.
     */
    private String getMembershipScheme() {
        Parameter membershipSchemeParam =
                getParameter(ClusteringConstants.Parameters.MEMBERSHIP_SCHEME);
        String membershipScheme = ClusteringConstants.MembershipScheme.MULTICAST_BASED;
        if (membershipSchemeParam != null) {
            membershipScheme = ((String) membershipSchemeParam.getValue()).trim();
        }
        return membershipScheme;
    }

    /**
     * Get the clustering domain to which this node belongs to
     *
     * @return The clustering domain to which this node belongs to
     */
    private byte[] getClusterDomain() {
        Parameter domainParam = getParameter(ClusteringConstants.Parameters.DOMAIN);
        byte[] domain;
        if (domainParam != null) {
            domain = ((String) domainParam.getValue()).getBytes();
        } else {
            domain = ClusteringConstants.DEFAULT_DOMAIN.getBytes();
        }
        return domain;
    }

    /**
     * Set the maximum number of retries, if message sending to a particular node fails
     */
    private void setMaximumRetries() {
        Parameter maxRetriesParam = getParameter(TribesConstants.MAX_RETRIES);
        int maxRetries = 10;
        if (maxRetriesParam != null) {
            maxRetries = Integer.parseInt((String) maxRetriesParam.getValue());
        }
        ReplicationTransmitter replicationTransmitter =
                (ReplicationTransmitter) channel.getChannelSender();
        MultiPointSender multiPointSender = replicationTransmitter.getTransport();
        multiPointSender.setMaxRetryAttempts(maxRetries);
    }

    /**
     * A RequestBlockingHandler, which is an implementation of
     * {@link org.apache.axis2.engine.Handler} is added to the InFlow & InFaultFlow. This handler
     * is used for rejecting Web service requests until this node has been initialized. This handler
     * can also be used for rejecting requests when this node is reinitializing or is in an
     * inconsistent state (which can happen when a configuration change is taking place).
     */
    private void addRequestBlockingHandlerToInFlows() {
        AxisConfiguration axisConfig = configurationContext.getAxisConfiguration();
        for (Object o : axisConfig.getInFlowPhases()) {
            Phase phase = (Phase) o;
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

                    log.debug("Added " + ClusteringConstants.REQUEST_BLOCKING_HANDLER +
                             " between SOAPMessageBodyBasedDispatcher & InstanceDispatcher to InFlow");
                    break;
                }
            }
        }
        for (Object o : axisConfig.getInFaultFlowPhases()) {
            Phase phase = (Phase) o;
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

                    log.debug("Added " + ClusteringConstants.REQUEST_BLOCKING_HANDLER +
                             " between SOAPMessageBodyBasedDispatcher & InstanceDispatcher to InFaultFlow");
                    break;
                }
            }
        }
    }

    /**
     * Handle specific configurations related to different membership management schemes.
     *
     * @param domain           The clustering domain to which this member belongs to
     * @param membershipScheme The membership scheme. Only wka & multicast are valid values.
     * @throws ClusteringFault If the membership scheme is invalid, or if an error occurs
     *                         while configuring membership scheme
     */
    private void configureMembershipScheme(byte[] domain, String membershipScheme)
            throws ClusteringFault {

        if (membershipScheme.equals(ClusteringConstants.MembershipScheme.WKA_BASED)) {
            log.info("Using WKA based membership management scheme");
            channel.setMembershipService(new WkaMembershipService(membershipManager));
            StaticMember localMember = new StaticMember();
            membershipManager.setLocalMember(localMember);
            ReceiverBase receiver = (ReceiverBase) channel.getChannelReceiver();
            Parameter localHost = getParameter(TribesConstants.LOCAL_MEMBER_HOST);
            if (localHost != null) {
                String host = ((String) localHost.getValue()).trim();
                receiver.setAddress(host);
                localMember.setHost(host);
            }
            Parameter localPort = getParameter(TribesConstants.LOCAL_MEMBER_PORT);
            if (localPort != null) {
                String port = ((String) localPort.getValue()).trim();
                receiver.setPort(Integer.parseInt(port));
                localMember.setPort(Integer.parseInt(port));
            }
            localMember.setDomain(domain);
            byte[] payload = "ping".getBytes();
            localMember.setPayload(payload);

            for (org.apache.axis2.clustering.Member member : members) {
                StaticMember tribesMember;
                try {
                    tribesMember = new StaticMember(member.getHostName(), member.getPort(),
                                                    0, payload);
                } catch (IOException e) {
                    String msg = "Could not add static member " +
                                 member.getHostName() + ":" + member.getPort();
                    log.error(msg, e);
                    throw new ClusteringFault(msg, e);
                }
                
                // Do not add the local member to the list of members
                if (!(Arrays.equals(localMember.getHost(), tribesMember.getHost()) &&
                      localMember.getPort() == tribesMember.getPort())) {
                    tribesMember.setDomain(domain);

                    // We will add the member even if it is offline at this moment. When the
                    // member comes online, it will be detected by the GMS
                    staticMembershipInterceptor.addStaticMember(tribesMember);
                    if (canConnect(member)) {
                        membershipManager.memberAdded(tribesMember);
                        log.info("Added static member " + TribesUtil.getHost(tribesMember));
                    } else {
                        log.info("Could not connect to member " + TribesUtil.getHost(tribesMember));
                    }
                }
            }
        } else if (membershipScheme.equals(ClusteringConstants.MembershipScheme.MULTICAST_BASED)) {
            log.info("Using multicast based membership management scheme");
            configureMulticastParameters(channel, domain);
        } else {
            String msg = "Invalid membership scheme '" + membershipScheme +
                         "'. Supported schemes are multicast & wka";
            log.error(msg);
            throw new ClusteringFault(msg);
        }
    }

    /**
     * Before adding a static member, we will try to verify whether we can connect to it
     *
     * @param member The member whose connectvity needs to be verified
     * @return true, if the member can be contacted; false, otherwise.
     */
    private boolean canConnect(org.apache.axis2.clustering.Member member) {
        boolean canConnect = false;
        try {
            InetAddress addr = InetAddress.getByName(member.getHostName());
            SocketAddress sockaddr = new InetSocketAddress(addr,
                                                           member.getPort());
            new Socket().connect(sockaddr, 3000);
            canConnect = true;
        } catch (IOException e) {
            // A debug level log is sufficient here since we are only trying to verify whether
            // the member in concern is online or offline
            log.debug("Cannot connect to member " +
                      member.getHostName() + ":" + member.getPort(), e);
        }
        return canConnect;
    }

    /**
     * Add ChannelInterceptors. The order of the interceptors that are added will depend on the
     * membership management scheme
     *
     * @param channel          The Tribes channel
     * @param domain           The domain to which this node belongs to
     * @param membershipScheme The membership scheme. Only wka & multicast are valid values.
     * @throws ClusteringFault If an error occurs while adding interceptors
     */
    private void addInterceptors(ManagedChannel channel, byte[] domain, String membershipScheme)
            throws ClusteringFault {

        if (membershipScheme.equals(ClusteringConstants.MembershipScheme.WKA_BASED)) {
            TcpPingInterceptor tcpPingInterceptor = new TcpPingInterceptor();
            tcpPingInterceptor.setInterval(100);
            channel.addInterceptor(tcpPingInterceptor);
        }

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

        if (membershipScheme.equals(ClusteringConstants.MembershipScheme.WKA_BASED)) {
            staticMembershipInterceptor = new StaticMembershipInterceptor();
            channel.addInterceptor(staticMembershipInterceptor);
        }
    }

    /**
     * If a multicast based membership management scheme is used, configure the multicasting related
     * parameters
     *
     * @param channel The Tribes channel
     * @param domain  The clustering domain to which this node belongs to
     */
    private void configureMulticastParameters(ManagedChannel channel,
                                              byte[] domain) {
        Properties mcastProps = channel.getMembershipService().getProperties();
        Parameter mcastAddress = getParameter(TribesConstants.MCAST_ADDRESS);
        if (mcastAddress != null) {
            mcastProps.setProperty(TribesConstants.MCAST_ADDRESS,
                                   ((String) mcastAddress.getValue()).trim());
        }
        Parameter mcastBindAddress = getParameter(TribesConstants.MCAST_BIND_ADDRESS);
        if (mcastBindAddress != null) {
            mcastProps.setProperty(TribesConstants.MCAST_BIND_ADDRESS,
                                   ((String) mcastBindAddress.getValue()).trim());
        }

        Parameter mcastPort = getParameter(TribesConstants.MCAST_PORT);
        if (mcastPort != null) {
            mcastProps.setProperty(TribesConstants.MCAST_PORT, 
                                   ((String) mcastPort.getValue()).trim());
        }
        Parameter mcastFrequency = getParameter(TribesConstants.MCAST_FREQUENCY);
        if (mcastFrequency != null) {
            mcastProps.setProperty(TribesConstants.MCAST_FREQUENCY,
                                   ((String) mcastFrequency.getValue()).trim());
        }
        Parameter mcastMemberDropTime = getParameter(TribesConstants.MEMBER_DROP_TIME);
        if (mcastMemberDropTime != null) {
            mcastProps.setProperty(TribesConstants.MEMBER_DROP_TIME,
                                   ((String) mcastMemberDropTime.getValue()).trim());
        }

        // Set the IP address that will be advertised by this node
        ReceiverBase receiver = (ReceiverBase) channel.getChannelReceiver();
        Parameter tcpListenHost = getParameter(TribesConstants.LOCAL_MEMBER_HOST);
        if (tcpListenHost != null) {
            String host = ((String) tcpListenHost.getValue()).trim();
            mcastProps.setProperty(TribesConstants.TCP_LISTEN_HOST, host);
            mcastProps.setProperty(TribesConstants.BIND_ADDRESS, host);
            receiver.setAddress(host);
        }
        String localIP = System.getProperty(ClusteringConstants.LOCAL_IP_ADDRESS);
        if (localIP != null) {
            receiver.setAddress(localIP);
        }

        Parameter tcpListenPort = getParameter(TribesConstants.LOCAL_MEMBER_PORT);
        if (tcpListenPort != null) {
            String port = ((String) tcpListenPort.getValue()).trim();
            mcastProps.setProperty(TribesConstants.TCP_LISTEN_PORT, port);
            receiver.setPort(Integer.parseInt(port));
        }

        mcastProps.setProperty(TribesConstants.MCAST_CLUSTER_DOMAIN, new String(domain));
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
        List<String> sentMembersList = new ArrayList<String>();
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
        return parameters.get(name);
    }

    public ArrayList getParameters() {
        ArrayList<Parameter> list = new ArrayList<Parameter>();
        for (String msg : parameters.keySet()) {
            list.add(parameters.get(msg));
        }
        return list;
    }

    public boolean isParameterLocked(String parameterName) {
        Parameter parameter = parameters.get(parameterName);
        return parameter != null && parameter.isLocked();
    }

    public void removeParameter(Parameter param) throws AxisFault {
        parameters.remove(param.getName());
    }

    /**
     * Shutdown the cluster. This member will leave the cluster when this method is called.
     *
     * @throws ClusteringFault If an error occurs while shutting down
     */
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
     * Method to check whether all members in the cluster have to be kept in sync at all times.
     * Typically, this will require each member in the cluster to ACKnowledge receipt of a
     * particular message, which may have a significant performance hit.
     *
     * @return true - if all members in the cluster should be kept in sync at all times, false
     *         otherwise
     */
    public boolean synchronizeAllMembers() {
        Parameter syncAllParam = getParameter(ClusteringConstants.Parameters.SYNCHRONIZE_ALL_MEMBERS);
        return syncAllParam == null || Boolean.parseBoolean((String) syncAllParam.getValue());
    }
}
