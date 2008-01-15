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

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Responsible for managing the membership
 */
public class MembershipManager {
    private final List members = new ArrayList();

    public synchronized void memberAdded(Member member) {
        members.add(member);
    }

    public synchronized void memberDisappeared(Member member) {
        members.remove(member);
    }

    public synchronized Member[] getMembers() {
        return (Member[]) members.toArray(new Member[members.size()]);
    }

    public synchronized Member getLongestLivingMember() {
        Member longestLivingMember = null;
        if (members.size() > 0) {
            Member member0 = (Member) members.get(0);
            long longestAliveTime = member0.getMemberAliveTime();
            longestLivingMember = member0;
            for (int i = 0; i < members.size(); i++) {
                Member member = (Member) members.get(i);
                if (longestAliveTime < member.getMemberAliveTime()) {
                    longestAliveTime = member.getMemberAliveTime();
                    longestLivingMember = member;
                }
            }
        }
        return longestLivingMember;
    }

    public synchronized Member getRandomMember() {
        if (members.size() == 0) {
            return null;
        }
        int memberIndex = new Random().nextInt(members.size());
        return (Member) members.get(memberIndex);
    }

}
