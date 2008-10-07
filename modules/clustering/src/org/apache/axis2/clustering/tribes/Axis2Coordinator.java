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

import org.apache.catalina.tribes.Member;
import org.apache.catalina.tribes.group.interceptors.NonBlockingCoordinator;
import org.apache.axis2.clustering.MembershipListener;

/**
 * The non-blocking coordinator interceptor
 */
public class Axis2Coordinator extends NonBlockingCoordinator {

    private MembershipListener membershipListener;

    public Axis2Coordinator(MembershipListener membershipListener) {
        this.membershipListener = membershipListener;
    }

    public void memberAdded(Member member) {
        super.memberAdded(member);
        if (membershipListener != null) {
            membershipListener.memberAdded(TribesUtil.toAxis2Member(member), isCoordinator());
        }
    }

    public void memberDisappeared(Member member) {
        super.memberDisappeared(member);
        if (isCoordinator()) {
            if (TribesUtil.toAxis2Member(member).isActive()) {
                //TODO If an ACTIVE member disappeared, activate a passive member

            } else {
                //TODO If a PASSIVE member disappeared, we may need to startup another
                // passive node
            }
        }
        if (membershipListener != null) {
            membershipListener.memberDisappeared(TribesUtil.toAxis2Member(member), isCoordinator());
        }
    }
}
