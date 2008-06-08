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

import org.apache.catalina.tribes.ChannelMessage;
import org.apache.catalina.tribes.Member;
import org.apache.catalina.tribes.group.ChannelInterceptorBase;
import org.apache.catalina.tribes.membership.MemberImpl;
import org.apache.catalina.tribes.membership.Membership;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.axis2.clustering.LoadBalanceEventHandler;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

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
     * Represents the application group across which the load is balanced
     */
    protected List<Member> applicationMembers = new ArrayList<Member>();

    /**
     * Represents the load balancer group
     */
    protected byte[] loadBalancerDomain = new byte[0];

    /**
     * Represents the group in which the applications being load balanced, are deployed
     */
    protected byte[] applicationDomain = new byte[0];

    private LoadBalanceEventHandler eventHandler;

    public LoadBalancerInterceptor(byte[] loadBalancerDomain,
                                   byte[] applicationDomain) {
        this.loadBalancerDomain = loadBalancerDomain;
        this.applicationDomain = applicationDomain;
    }

    public void messageReceived(ChannelMessage msg) {

        // Ignore all messages which are not intended for the load balancer group
        if (Arrays.equals(msg.getAddress().getDomain(), loadBalancerDomain)) {
            super.messageReceived(msg);
        }

        // TODO: Application members may inform about their HTTP/S ports
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
        if (Arrays.equals(applicationDomain, member.getDomain())) {
            log.info("Application member " + TribesUtil.getHost(member) + " joined cluster");
            if(eventHandler != null){
                org.apache.axis2.clustering.Member axis2Member =
                        new org.apache.axis2.clustering.Member(member.getName(), member.getPort());
                eventHandler.applicationMemberAdded(axis2Member);
            }
            applicationMembers.add(member);
        }

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
        if (Arrays.equals(applicationDomain, member.getDomain())) {
            log.info("Application member " + TribesUtil.getHost(member) + " left cluster");
            if(eventHandler != null){
                org.apache.axis2.clustering.Member axis2Member =
                        new org.apache.axis2.clustering.Member(member.getName(), member.getPort());
                eventHandler.applicationMemberRemoved(axis2Member);
                applicationMembers.remove(member);
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

    public byte[] getApplicationDomain() {
        return applicationDomain;
    }

    public void setApplicationDomain(byte[] applicationDomain) {
        this.applicationDomain = applicationDomain;
    }

    public byte[] getLoadBalancerDomain() {
        return loadBalancerDomain;
    }

    public void setLoadBalancerDomain(byte[] loadBalancerDomain) {
        this.loadBalancerDomain = loadBalancerDomain;
    }

    public void setEventHandler(LoadBalanceEventHandler eventHandler) {
        this.eventHandler = eventHandler;
    }
}
