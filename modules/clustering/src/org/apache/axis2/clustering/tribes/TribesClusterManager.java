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
import org.apache.axis2.clustering.ClusteringFault;
import org.apache.axis2.clustering.ClusteringConstants;
import org.apache.axis2.clustering.configuration.ConfigurationManager;
import org.apache.axis2.clustering.configuration.DefaultConfigurationManager;
import org.apache.axis2.clustering.context.ContextManager;
import org.apache.axis2.clustering.context.DefaultContextManager;
import org.apache.axis2.clustering.tribes.info.TransientTribesChannelInfo;
import org.apache.axis2.clustering.tribes.info.TransientTribesMemberInfo;
import org.apache.axis2.description.Parameter;
import org.apache.catalina.tribes.Channel;
import org.apache.catalina.tribes.ChannelException;
import org.apache.catalina.tribes.ManagedChannel;
import org.apache.catalina.tribes.Member;
import org.apache.catalina.tribes.group.GroupChannel;
import org.apache.catalina.tribes.group.interceptors.DomainFilterInterceptor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class TribesClusterManager implements ClusterManager {
    private static final Log log = LogFactory.getLog(TribesClusterManager.class);

    private DefaultConfigurationManager configurationManager;
    private DefaultContextManager contextManager;

    private HashMap parameters;
    private Channel channel;

    public TribesClusterManager() {
        parameters = new HashMap();
    }

    public ContextManager getContextManager() {
        return contextManager;
    }

    public ConfigurationManager getConfigurationManager() {
        return configurationManager;
    }

    public void init() throws ClusteringFault {
        if (log.isDebugEnabled()) {
            log.debug("Enter: TribesClusterManager::init");
        }

        ChannelSender sender = new ChannelSender();

        ChannelListener listener = new ChannelListener(configurationManager, contextManager);

        TransientTribesChannelInfo channelInfo = new TransientTribesChannelInfo();
        TransientTribesMemberInfo memberInfo = new TransientTribesMemberInfo();

        contextManager.setSender(sender);
        configurationManager.setSender(sender);

        try {

            ManagedChannel channel = new GroupChannel();

            // Set the domain for this Node
            Parameter domainParam = getParameter(ClusteringConstants.DOMAIN);
            byte[] domain;
            if (domainParam != null) {
                domain = ((String)domainParam.getValue()).getBytes();
                channel.getMembershipService().setDomain(domain);
            } else {
                domain = "apache.axis2.domain".getBytes();
                channel.getMembershipService().setDomain(domain);
            }
            DomainFilterInterceptor dfi = new DomainFilterInterceptor();
            dfi.setDomain(domain);
            channel.addInterceptor(dfi);

            this.channel = channel;

            channel.addChannelListener(listener);
            channel.addChannelListener(channelInfo);
            channel.addMembershipListener(memberInfo);
            channel.addMembershipListener(new TribesMembershipListener());
            channel.start(Channel.DEFAULT);
            sender.setChannel(channel);
            contextManager.setSender(sender);
            configurationManager.setSender(sender);

            Member[] members = channel.getMembers();
            TribesUtil.printMembers(members);

            listener.setContextManager(contextManager);

        } catch (ChannelException e) {

            if (log.isDebugEnabled()) {
                log.debug("Exit: TribesClusterManager::init");
            }

            String message = "Error starting Tribes channel";
            throw new ClusteringFault(message, e);
        }

        if (log.isDebugEnabled()) {
            log.debug("Exit: TribesClusterManager::init");
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

        if (log.isDebugEnabled()) {
            log.debug("Enter: TribesClusterManager::shutdown");
        }

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

        if (log.isDebugEnabled()) {
            log.debug("Exit: TribesClusterManager::shutdown");
        }
    }
}
