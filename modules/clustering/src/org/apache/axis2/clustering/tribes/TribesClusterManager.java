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

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.clustering.ClusterManager;
import org.apache.axis2.clustering.ClusteringConstants;
import org.apache.axis2.clustering.ClusteringFault;
import org.apache.axis2.clustering.configuration.ConfigurationManager;
import org.apache.axis2.clustering.configuration.DefaultConfigurationManager;
import org.apache.axis2.clustering.context.ContextManager;
import org.apache.axis2.clustering.context.DefaultContextManager;
import org.apache.axis2.clustering.control.GetStateCommand;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.Parameter;
import org.apache.catalina.tribes.Channel;
import org.apache.catalina.tribes.ChannelException;
import org.apache.catalina.tribes.ManagedChannel;
import org.apache.catalina.tribes.Member;
import org.apache.catalina.tribes.group.GroupChannel;
import org.apache.catalina.tribes.group.interceptors.DomainFilterInterceptor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.*;

public class TribesClusterManager implements ClusterManager {
    private static final Log log = LogFactory.getLog(TribesClusterManager.class);

    private DefaultConfigurationManager configurationManager;
    private DefaultContextManager contextManager;

    private HashMap parameters;
    private ManagedChannel channel;
    private ConfigurationContext configurationContext;
    private TribesControlCommandProcessor controlCmdProcessor;
    private ChannelListener channelListener;

    public TribesClusterManager() {
        parameters = new HashMap();
        controlCmdProcessor = new TribesControlCommandProcessor(configurationContext);
    }

    public ContextManager getContextManager() {
        return contextManager;
    }

    public ConfigurationManager getConfigurationManager() {
        return configurationManager;
    }

    public void init() throws ClusteringFault {
        ChannelSender sender = new ChannelSender();

        channelListener = new ChannelListener(configurationContext,
                                              configurationManager,
                                              contextManager,
                                              controlCmdProcessor,
                                              sender);

        if (configurationManager != null) {
            configurationManager.setSender(sender);
        }
        controlCmdProcessor.setChannelSender(sender);

        try {

            channel = new GroupChannel();

            // Set the domain for this Node
            Parameter domainParam = getParameter(ClusteringConstants.DOMAIN);
            byte[] domain;
            if (domainParam != null) {
                domain = ((String) domainParam.getValue()).getBytes();
            } else {
                domain = "apache.axis2.domain".getBytes();
            }
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

            /*Properties mcastProps = channel.getMembershipService().getProperties();
            mcastProps.setProperty("mcastPort", "5555");
            mcastProps.setProperty("mcastAddress", "224.10.10.10");
            mcastProps.setProperty("mcastClusterDomain", "catalina");
            mcastProps.setProperty("bindAddress", "localhost");
            mcastProps.setProperty("memberDropTime", "20000");
            mcastProps.setProperty("mcastFrequency", "500");
            mcastProps.setProperty("tcpListenPort", "4000");
            mcastProps.setProperty("tcpListenHost", "127.0.0.1");*/

            /*TcpFailureDetector tcpFailureDetector = new TcpFailureDetector();
            tcpFailureDetector.setPrevious(nbc);
            channel.addInterceptor(tcpFailureDetector);
            tcpFailureDetector.*/

            channel.addChannelListener(channelListener);
            TribesMembershipListener membershipListener = new TribesMembershipListener();
            channel.addMembershipListener(membershipListener);
            channel.start(Channel.DEFAULT);
            sender.setChannel(channel);

            if (contextManager != null) {
                contextManager.setSender(sender);
                channelListener.setContextManager(contextManager);

                Member[] members = channel.getMembers();
                log.info("Local Tribes Member " + TribesUtil.getLocalHost(channel));
                TribesUtil.printMembers(members);

                // If there is at least one member in the Tribe, get the current state from a member
                Random random = new Random();
                int numberOfTries = 0; // Don't keep on trying infinitely

                // Keep track of members to whom we already sent a GetStateCommand
                // Do not send another request to these members
                List sentMembersList = new ArrayList();
                while (members.length > 0 &&
                       configurationContext.
                               getPropertyNonReplicable(ClusteringConstants.CLUSTER_INITIALIZED) == null
                       && numberOfTries < 50) {

                    // While there are members and GetStateResponseCommand is not received do the following
                    try {
                        members = channel.getMembers();
                        int memberIndex = random.nextInt(members.length);
                        Member member = members[memberIndex];
                        if (!sentMembersList.contains(TribesUtil.getHost(member))) {
                            long tts = sender.sendToMember(new GetStateCommand(), member);
                            configurationContext.
                                    setNonReplicableProperty(ClusteringConstants.TIME_TO_SEND,
                                                             new Long(tts));
                            sentMembersList.add(TribesUtil.getHost(member));
                            log.debug("WAITING FOR STATE UPDATE...");
                            Thread.sleep(tts + 5);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        break;
                    }
                    numberOfTries ++;
                }
                configurationContext.
                        setNonReplicableProperty(ClusteringConstants.CLUSTER_INITIALIZED,
                                                 "true");
            }
        } catch (ChannelException e) {
            String message = "Error starting Tribes channel";
            throw new ClusteringFault(message, e);
        }
    }

    public void setConfigurationManager(ConfigurationManager configurationManager) {
        this.configurationManager = (DefaultConfigurationManager) configurationManager;
    }

    public void setContextManager(ContextManager contextManager) {
        this.contextManager = (DefaultContextManager) contextManager;
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
        if (parameter != null) {
            return parameter.isLocked();
        }

        return false;
    }

    public void removeParameter(Parameter param) throws AxisFault {
        parameters.remove(param.getName());
    }

    public void shutdown() throws ClusteringFault {
        log.debug("Enter: TribesClusterManager::shutdown");
        if (channel != null) {
            try {
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
    }
}
