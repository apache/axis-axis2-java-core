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
import org.apache.catalina.tribes.group.ChannelInterceptorBase;
import org.apache.catalina.tribes.membership.Membership;

/**
 * This interceptor is used when this member is part of a load balancer cluster.
 * This load balancer is responsible for load balancing across applications deployed in
 * another group.
 */
public class LoadBalancerInterceptor extends ChannelInterceptorBase {

    /**
     * Represents the load balancer group
     */
    protected Membership loadBalancerMembership = null;

    /**
     * Represents the load balancer group
     */
    protected byte[] loadBalancerDomain = new byte[0];

    public LoadBalancerInterceptor(byte[] loadBalancerDomain) {
        this.loadBalancerDomain = loadBalancerDomain;
    }

    public void messageReceived(ChannelMessage msg) {
        // Ignore all messages which are not intended for the load balancer group or which are not
        // membership messages
        if (okToProcess(msg.getOptions()) ||
            TribesUtil.isInDomain(msg.getAddress(), loadBalancerDomain)) {
            super.messageReceived(msg);
        }
    }
}
