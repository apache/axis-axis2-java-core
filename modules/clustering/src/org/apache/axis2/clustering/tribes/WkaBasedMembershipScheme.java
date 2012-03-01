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

import org.apache.axis2.clustering.ClusteringFault;
import org.apache.axis2.clustering.Member;
import org.apache.axis2.clustering.MembershipScheme;
import org.apache.axis2.clustering.control.wka.JoinGroupCommand;
import org.apache.axis2.clustering.control.wka.MemberListCommand;
import org.apache.axis2.clustering.control.wka.RpcMembershipRequestHandler;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.util.Utils;
import org.apache.catalina.tribes.Channel;
import org.apache.catalina.tribes.ManagedChannel;
import org.apache.catalina.tribes.group.Response;
import org.apache.catalina.tribes.group.RpcChannel;
import org.apache.catalina.tribes.group.interceptors.OrderInterceptor;
import org.apache.catalina.tribes.group.interceptors.StaticMembershipInterceptor;
import org.apache.catalina.tribes.group.interceptors.TcpFailureDetector;
import org.apache.catalina.tribes.group.interceptors.TcpPingInterceptor;
import org.apache.catalina.tribes.membership.StaticMember;
import org.apache.catalina.tribes.transport.ReceiverBase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Implementation of the WKA(well-known address) based membership scheme. In this scheme,
 * membership is discovered using a few well-known members (who run at well-known IP addresses)
 */
public class WkaBasedMembershipScheme implements MembershipScheme {

    private static final Log log = LogFactory.getLog(WkaBasedMembershipScheme.class);

    /**
     * The Tribes channel
     */
    private final ManagedChannel channel;
    private final MembershipManager primaryMembershipManager;
    private final List<MembershipManager> applicationDomainMembershipManagers;
    private StaticMembershipInterceptor staticMembershipInterceptor;
    private final Map<String, Parameter> parameters;

    /**
     * The loadBalancerDomain to which the members belong to
     */
    private final byte[] localDomain;

    /**
     * The static(well-known) members
     */
    private final List<Member> members;

    /**
     * The mode in which this member operates such as "loadBalance" or "application"
     */
    private final OperationMode mode;

    private final boolean atmostOnceMessageSemantics;
    private final boolean preserverMsgOrder;

    public WkaBasedMembershipScheme(ManagedChannel channel,
                                    OperationMode mode,
                                    List<MembershipManager> applicationDomainMembershipManagers,
                                    MembershipManager primaryMembershipManager,
                                    Map<String, Parameter> parameters,
                                    byte[] domain,
                                    List<Member> members,
                                    boolean atmostOnceMessageSemantics,
                                    boolean preserverMsgOrder) {
        this.channel = channel;
        this.mode = mode;
        this.applicationDomainMembershipManagers = applicationDomainMembershipManagers;
        this.primaryMembershipManager = primaryMembershipManager;
        this.parameters = parameters;
        this.localDomain = domain;
        this.members = members;
        this.atmostOnceMessageSemantics = atmostOnceMessageSemantics;
        this.preserverMsgOrder = preserverMsgOrder;
    }

    /**
     * Configure the membership related to the WKA based scheme
     *
     * @throws org.apache.axis2.clustering.ClusteringFault
     *          If an error occurs while configuring this scheme
     */
    public void init() throws ClusteringFault {
        addInterceptors();
        configureStaticMembership();
    }

    private void configureStaticMembership() throws ClusteringFault {
        channel.setMembershipService(new WkaMembershipService(primaryMembershipManager));
        StaticMember localMember = new StaticMember();
        primaryMembershipManager.setLocalMember(localMember);
        ReceiverBase receiver = (ReceiverBase) channel.getChannelReceiver();

        // ------------ START: Configure and add the local member ---------------------
        Parameter localHost = getParameter(TribesConstants.LOCAL_MEMBER_HOST);
        String host;
        if (localHost != null) {
            host = ((String) localHost.getValue()).trim();
        } else { // In cases where the localhost needs to be automatically figured out
            try {
                try {
                    host = Utils.getIpAddress();
                } catch (SocketException e) {
                    String msg = "Could not get local IP address";
                    log.error(msg, e);
                    throw new ClusteringFault(msg, e);
                }
            } catch (Exception e) {
                String msg = "Could not get the localhost name";
                log.error(msg, e);
                throw new ClusteringFault(msg, e);
            }
        }
        receiver.setAddress(host);
        try {
            localMember.setHostname(host);
        } catch (IOException e) {
            String msg = "Could not set the local member's name";
            log.error(msg, e);
            throw new ClusteringFault(msg, e);
        }

        Parameter localPort = getParameter(TribesConstants.LOCAL_MEMBER_PORT);
        int port;
        try {
            if (localPort != null) {
                port = Integer.parseInt(((String) localPort.getValue()).trim());
                port = getLocalPort(new ServerSocket(), localMember.getHostname(), port, 4000, 1000);
            } else { // In cases where the localport needs to be automatically figured out
                port = getLocalPort(new ServerSocket(), localMember.getHostname(), -1, 4000, 1000);
            }
        } catch (IOException e) {
            String msg =
                    "Could not allocate the specified port or a port in the range 4000-5000 " +
                    "for local host " + localMember.getHostname() +
                    ". Check whether the IP address specified or inferred for the local " +
                    "member is correct.";
            log.error(msg, e);
            throw new ClusteringFault(msg, e);
        }

        byte[] payload = "ping".getBytes();
        localMember.setPayload(payload);
        receiver.setPort(port);
        localMember.setPort(port);
        localMember.setDomain(localDomain);
        staticMembershipInterceptor.setLocalMember(localMember);

        // ------------ END: Configure and add the local member ---------------------

        // ------------ START: Add other members ---------------------
        for (Member member : members) {
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
                tribesMember.setDomain(localDomain);

                // We will add the member even if it is offline at this moment. When the
                // member comes online, it will be detected by the GMS
                staticMembershipInterceptor.addStaticMember(tribesMember);
                primaryMembershipManager.addWellKnownMember(tribesMember);
                if (canConnect(member)) {
                    primaryMembershipManager.memberAdded(tribesMember);
                    log.info("Added static member " + TribesUtil.getName(tribesMember));
                } else {
                    log.info("Could not connect to member " + TribesUtil.getName(tribesMember));
                }
            }
        }
    }

    /**
     * Before adding a static member, we will try to verify whether we can connect to it
     *
     * @param member The member whose connectvity needs to be verified
     * @return true, if the member can be contacted; false, otherwise.
     */
    private boolean canConnect(org.apache.axis2.clustering.Member member) {
        for (int retries = 5; retries > 0; retries--) {
            try {
                InetAddress addr = InetAddress.getByName(member.getHostName());
                SocketAddress sockaddr = new InetSocketAddress(addr,
                                                               member.getPort());
                new Socket().connect(sockaddr, 500);
                return true;
            } catch (IOException e) {
                String msg = e.getMessage();
                if (!msg.contains("Connection refused") && !msg.contains("connect timed out")) {
                    log.error("Cannot connect to member " +
                              member.getHostName() + ":" + member.getPort(), e);
                }
            }
        }
        return false;
    }

    protected int getLocalPort(ServerSocket socket, String hostname,
                               int preferredPort, int portstart, int retries) throws IOException {
        if (preferredPort != -1) {
            try {
                return getLocalPort(socket, hostname, preferredPort);
            } catch (IOException ignored) {
                // Fall through and try a default port
            }
        }
        InetSocketAddress addr = null;
        if (retries > 0) {
            try {
                return getLocalPort(socket, hostname, portstart);
            } catch (IOException x) {
                retries--;
                if (retries <= 0) {
                    log.error("Unable to bind server socket to:" + addr + " throwing error.");
                    throw x;
                }
                portstart++;
                try {
                    Thread.sleep(50);
                } catch (InterruptedException ignored) {
                    ignored.printStackTrace();
                }
                portstart = getLocalPort(socket, hostname, portstart, retries, -1);
            }
        }
        return portstart;
    }

    private int getLocalPort(ServerSocket socket, String hostname, int port) throws IOException {
        InetSocketAddress addr;
        addr = new InetSocketAddress(hostname, port);
        socket.bind(addr);
        log.info("Receiver Server Socket bound to:" + addr);
        socket.setSoTimeout(5);
        socket.close();
        try {
            Thread.sleep(100);
        } catch (InterruptedException ignored) {
            ignored.printStackTrace();
        }
        return port;
    }

    /**
     * Add ChannelInterceptors. The order of the interceptors that are added will depend on the
     * membership management scheme
     */
    private void addInterceptors() {

        if (log.isDebugEnabled()) {
            log.debug("Adding Interceptors...");
        }
        TcpPingInterceptor tcpPingInterceptor = new TcpPingInterceptor();
        tcpPingInterceptor.setInterval(10000);
        channel.addInterceptor(tcpPingInterceptor);
        if (log.isDebugEnabled()) {
            log.debug("Added TCP Ping Interceptor");
        }

        // Add a reliable failure detector
        TcpFailureDetector tcpFailureDetector = new TcpFailureDetector();
//        tcpFailureDetector.setPrevious(dfi); //TODO: check this
        tcpFailureDetector.setReadTestTimeout(120000);
        tcpFailureDetector.setConnectTimeout(180000);
        channel.addInterceptor(tcpFailureDetector);
        if (log.isDebugEnabled()) {
            log.debug("Added TCP Failure Detector");
        }

        // Add the NonBlockingCoordinator.
//        channel.addInterceptor(new Axis2Coordinator(membershipListener));

        staticMembershipInterceptor = new StaticMembershipInterceptor();
        staticMembershipInterceptor.setLocalMember(primaryMembershipManager.getLocalMember());
        primaryMembershipManager.setupStaticMembershipManagement(staticMembershipInterceptor);
        channel.addInterceptor(staticMembershipInterceptor);
        if (log.isDebugEnabled()) {
            log.debug("Added Static Membership Interceptor");
        }

        channel.getMembershipService().setDomain(localDomain);
        mode.addInterceptors(channel);

        if (atmostOnceMessageSemantics) {
            // Add a AtMostOnceInterceptor to support at-most-once message processing semantics
            AtMostOnceInterceptor atMostOnceInterceptor = new AtMostOnceInterceptor();
            atMostOnceInterceptor.setOptionFlag(TribesConstants.AT_MOST_ONCE_OPTION);
            channel.addInterceptor(atMostOnceInterceptor);
            if (log.isDebugEnabled()) {
                log.debug("Added At-most-once Interceptor");
            }
        }

        if (preserverMsgOrder) {
            // Add the OrderInterceptor to preserve sender ordering
            OrderInterceptor orderInterceptor = new OrderInterceptor();
            orderInterceptor.setOptionFlag(TribesConstants.MSG_ORDER_OPTION);
            channel.addInterceptor(orderInterceptor);
            if (log.isDebugEnabled()) {
                log.debug("Added Message Order Interceptor");
            }
        }
    }

    /**
     * JOIN the group and get the member list
     *
     * @throws ClusteringFault If an error occurs while joining the group
     */
    public void joinGroup() throws ClusteringFault {

        // Have multiple RPC channels with multiple RPC request handlers for each localDomain
        // This is needed only when this member is running as a load balancer
        for (MembershipManager appDomainMembershipManager : applicationDomainMembershipManagers) {
            appDomainMembershipManager.setupStaticMembershipManagement(staticMembershipInterceptor);

            // Create an RpcChannel for each localDomain
            String domain = new String(appDomainMembershipManager.getDomain());
            RpcChannel rpcMembershipChannel =
                    new RpcChannel(TribesUtil.getRpcMembershipChannelId(appDomainMembershipManager.getDomain()),
                                   channel,
                                   new RpcMembershipRequestHandler(appDomainMembershipManager,
                                                                   this));
            appDomainMembershipManager.setRpcMembershipChannel(rpcMembershipChannel);
            if (log.isDebugEnabled()) {
                log.debug("Created RPC Membership Channel for application domain " + domain);
            }
        }

        // Create a Membership channel for handling membership requests
        RpcChannel rpcMembershipChannel =
                new RpcChannel(TribesUtil.getRpcMembershipChannelId(localDomain),
                               channel, new RpcMembershipRequestHandler(primaryMembershipManager,
                                                                        this));
        if (log.isDebugEnabled()) {
            log.debug("Created primary membership channel " + new String(localDomain));
        }
        primaryMembershipManager.setRpcMembershipChannel(rpcMembershipChannel);

        // Send JOIN message to a WKA member
        if (primaryMembershipManager.getMembers().length > 0) {
            org.apache.catalina.tribes.Member[] wkaMembers = primaryMembershipManager.getMembers(); // The well-known members
            /*try {
                Thread.sleep(3000); // Wait for sometime so that the WKA members can receive the MEMBER_LIST message, if they have just joined the group
            } catch (InterruptedException ignored) {
            }*/   //TODO: #### Need to double check whether this sleep is necessary
            Response[] responses = null;
            do {
                try {
                    log.info("Sending JOIN message to WKA members...");
                    responses = rpcMembershipChannel.send(wkaMembers,
                                                          new JoinGroupCommand(),
                                                          RpcChannel.ALL_REPLY,
                                                          Channel.SEND_OPTIONS_ASYNCHRONOUS |
                                                          TribesConstants.MEMBERSHIP_MSG_OPTION,
                                                          10000);
                    if (responses.length == 0) {
                        try {
                            log.info("No responses received from WKA members");
                            Thread.sleep(5000);
                        } catch (InterruptedException ignored) {
                        }
                    }
                } catch (Exception e) {
                    String msg = "Error occurred while trying to send JOIN request to WKA members";
                    log.error(msg, e);
                    wkaMembers = primaryMembershipManager.getMembers();
                    if (wkaMembers.length == 0) {
                        log.warn("There are no well-known members");
                        break;
                    }
                }

                // TODO: If we do not get a response within some time, try to recover from this fault
            }
            while (responses == null || responses.length == 0);  // Wait until we've received at least one response

            for (Response response : responses) {
                MemberListCommand command = (MemberListCommand) response.getMessage();
                command.setMembershipManager(primaryMembershipManager);
                command.execute(null); // Set the list of current members

                // If the WKA member is not part of this group, remove it
                if (!TribesUtil.areInSameDomain(response.getSource(),
                                                primaryMembershipManager.getLocalMember())) {
                    primaryMembershipManager.memberDisappeared(response.getSource());
                    if (log.isDebugEnabled()) {
                        log.debug("Removed member " + TribesUtil.getName(response.getSource()) +
                                  " since it does not belong to the local domain " +
                                  new String(primaryMembershipManager.getLocalMember().getDomain()));
                    }
                }
            }
        }
    }

    /**
     * When a JOIN message is received from some other member, it is notified using this method,
     * so that membership scheme specific processing can be carried out
     *
     * @param member The member who just joined
     */
    public void processJoin(org.apache.catalina.tribes.Member member) {
        mode.notifyMemberJoin(member);
    }


    public Parameter getParameter(String name) {
        return parameters.get(name);
    }
}
