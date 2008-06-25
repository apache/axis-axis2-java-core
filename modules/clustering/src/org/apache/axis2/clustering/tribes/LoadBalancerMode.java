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
import org.apache.catalina.tribes.Channel;
import org.apache.catalina.tribes.MembershipListener;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Represents a member running in load balance mode
 */
public class LoadBalancerMode implements Mode {

    private static final Log log = LogFactory.getLog(LoadBalancerMode.class);

    private byte[] loadBalancerDomain;
    private Map<String, LoadBalanceEventHandler> lbEventHandlers;
    private List<MembershipManager> membershipManagers = new ArrayList<MembershipManager>();

    public LoadBalancerMode(byte[] loadBalancerDomain,
                            Map<String, LoadBalanceEventHandler> lbEventHandlers) {
        this.loadBalancerDomain = loadBalancerDomain;
        this.lbEventHandlers = lbEventHandlers;
    }

    public void addInterceptors(Channel channel) {
        LoadBalancerInterceptor lbInterceptor =
                new LoadBalancerInterceptor(loadBalancerDomain);
        lbInterceptor.setOptionFlag(TribesConstants.MEMBERSHIP_MSG_OPTION);
        channel.addInterceptor(lbInterceptor);
        if (log.isDebugEnabled()) {
            log.debug("Added Load Balancer Interceptor");
        }
    }

    public void init(Channel channel) {
        // Have multiple RPC channels with multiple RPC request handlers for each domain
        // This is needed only when this member is running as a load balancer
        for (Object o : lbEventHandlers.keySet()) {
            String domain = (String) o;
            final MembershipManager membershipManager = new MembershipManager();
            membershipManager.setDomain(domain.getBytes());
            membershipManager.setLoadBalanceEventHandler(lbEventHandlers.get(domain));

            MembershipListener membershipListener = new MembershipListener() {
                public void memberAdded(org.apache.catalina.tribes.Member member) {
                    membershipManager.memberAdded(member);
                }

                public void memberDisappeared(org.apache.catalina.tribes.Member member) {
                    membershipManager.memberDisappeared(member);
                }
            };
            channel.addMembershipListener(membershipListener);
            membershipManagers.add(membershipManager);
        }
    }

    public List<MembershipManager> getMembershipManagers() {
        return membershipManagers;
    }
}
