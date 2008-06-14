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

import org.apache.axis2.clustering.LoadBalanceEventHandler;
import org.apache.catalina.tribes.ChannelMessage;
import org.apache.catalina.tribes.Member;
import org.apache.catalina.tribes.group.ChannelInterceptorBase;
import org.apache.catalina.tribes.membership.MemberImpl;
import org.apache.catalina.tribes.membership.Membership;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.Properties;

/**
 * This interceptor is used when this member is part of a load balancer cluster.
 * This load balancer is responsible for load balancing across applications deployed in
 * another group.
 */
public class LoadBalancerInterceptor extends ChannelInterceptorBase {
    private static final Log log = LogFactory.getLog(LoadBalancerInterceptor.class);

    /**
     * Represents the load balancer group
     */
    protected Membership loadBalancerMembership = null;

    /**
     * Represents the load balancer group
     */
    protected byte[] loadBalancerDomain = new byte[0];

    /**
     * Represents the group in which the applications being load balanced, are deployed and their
     * respective load balancer event handlers
     */
    private Map<byte[], LoadBalanceEventHandler> lbEventHandlers;

    public LoadBalancerInterceptor(byte[] loadBalancerDomain,
                                   Map<byte[], LoadBalanceEventHandler> lbEventHandlers) {
        this.loadBalancerDomain = loadBalancerDomain;
        this.lbEventHandlers = lbEventHandlers;
    }

    public void setLbEventHandlers(Map<byte[], LoadBalanceEventHandler> lbEventHandlers) {
        this.lbEventHandlers = lbEventHandlers;
    }

    public void messageReceived(ChannelMessage msg) {

        // Ignore all messages which are not intended for the load balancer group
        if (Arrays.equals(msg.getAddress().getDomain(), loadBalancerDomain)) {
            super.messageReceived(msg);
        }
    }

    public void memberAdded(Member member) {
        if (loadBalancerMembership == null) {
            setupMembership();
        }
        boolean notify;
        synchronized (loadBalancerMembership) {
            notify = Arrays.equals(loadBalancerDomain, member.getDomain());
            if (notify) {
                notify = loadBalancerMembership.memberAlive((MemberImpl) member);
            }
        }
        if (notify) {
            super.memberAdded(member);

        }

        // Is this an application domain member?
        for (byte[] applicationDomain : lbEventHandlers.keySet()) {
            if (Arrays.equals(applicationDomain, member.getDomain())) {
                log.info("Application member " + TribesUtil.getName(member) + " joined group " +
                         new String(applicationDomain));
                LoadBalanceEventHandler eventHandler = lbEventHandlers.get(applicationDomain);
                if (eventHandler != null) {
                    eventHandler.applicationMemberAdded(toAxis2Member(member));
                }
                break;
            }
        }
    }

    private org.apache.axis2.clustering.Member toAxis2Member(Member member) {
        org.apache.axis2.clustering.Member axis2Member =
                new org.apache.axis2.clustering.Member(TribesUtil.getHost(member),
                                                       member.getPort());
        Properties props = getProperties(member.getPayload());
        int httpPort = Integer.parseInt(props.getProperty("HTTP"));
        int httpsPort = Integer.parseInt(props.getProperty("HTTPS"));
        axis2Member.setHttpPort(httpPort);
        axis2Member.setHttpsPort(httpsPort);
        return axis2Member;
    }

    private Properties getProperties(byte[] payload) {
        Properties props = null;
        try {
            ByteArrayInputStream bin = new ByteArrayInputStream(payload);
            props = new Properties();
            props.load(bin);
        } catch (IOException ignored) {
            // This error will never occur
        }
        return props;
    }

    public void memberDisappeared(Member member) {
        if (loadBalancerMembership == null) {
            setupMembership();
        }
        boolean notify;
        synchronized (loadBalancerMembership) {
            notify = Arrays.equals(loadBalancerDomain, member.getDomain());
            loadBalancerMembership.removeMember((MemberImpl) member);
        }
        if (notify) {
            super.memberDisappeared(member);
        }

        // Is this an application domain member?
        for (byte[] applicationDomain : lbEventHandlers.keySet()) {
            if (Arrays.equals(applicationDomain, member.getDomain())) {
                log.info("Application member " + TribesUtil.getName(member) + " left group " +
                         new String(applicationDomain));
                LoadBalanceEventHandler eventHandler = lbEventHandlers.get(applicationDomain);
                if (eventHandler != null) {
                    eventHandler.applicationMemberRemoved(toAxis2Member(member));
                }
                break;
            }
        }
    }

    public boolean hasMembers() {
        if (loadBalancerMembership == null) {
            setupMembership();
        }
        return loadBalancerMembership.hasMembers();
    }

    public Member[] getMembers() {
        if (loadBalancerMembership == null) {
            setupMembership();
        }
        return loadBalancerMembership.getMembers();
    }

    public Member getMember(Member mbr) {
        if (loadBalancerMembership == null) {
            setupMembership();
        }
        return loadBalancerMembership.getMember(mbr);
    }

    public Member getLocalMember(boolean incAlive) {
        return super.getLocalMember(incAlive);
    }

    protected synchronized void setupMembership() {
        if (loadBalancerMembership == null) {
            loadBalancerMembership = new Membership((MemberImpl) super.getLocalMember(true));
        }
    }
}
