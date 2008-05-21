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
package org.apache.axis2.clustering.control.wka;

import org.apache.axis2.clustering.ClusteringFault;
import org.apache.axis2.clustering.control.ControlCommand;
import org.apache.axis2.clustering.tribes.MembershipManager;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.catalina.tribes.Member;
import org.apache.catalina.tribes.group.interceptors.StaticMembershipInterceptor;

import java.util.Arrays;

/**
 *
 */
public class MemberListCommand extends ControlCommand {

    private Member[] members;
    private MembershipManager membershipManager;
    private StaticMembershipInterceptor staticMembershipInterceptor;

    public void setMembershipManager(MembershipManager membershipManager) {
        this.membershipManager = membershipManager;
    }

    public void setStaticMembershipInterceptor(
            StaticMembershipInterceptor staticMembershipInterceptor) {
        this.staticMembershipInterceptor = staticMembershipInterceptor;
    }

    public void setMembers(Member[] members) {
        this.members = members;
    }

    public void execute(ConfigurationContext configurationContext) throws ClusteringFault {
        for (Member member : members) {
            Member localMember = membershipManager.getLocalMember();
            if (!(Arrays.equals(localMember.getHost(), member.getHost()) &&
                  localMember.getPort() == member.getPort())) {
                membershipManager.memberAdded(member);
                staticMembershipInterceptor.memberAdded(member);
            }
        }
    }
}
