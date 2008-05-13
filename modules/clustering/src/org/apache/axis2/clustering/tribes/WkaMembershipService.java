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
import org.apache.catalina.tribes.MembershipListener;
import org.apache.catalina.tribes.MembershipService;
import org.apache.catalina.tribes.membership.MemberImpl;

import java.io.IOException;
import java.util.Properties;

/**
 * This is the MembershipService which manages group membership based on a Well-Known Addressing (WKA)
 * scheme.
 */
public class WkaMembershipService implements MembershipService {

    /**
     * The descriptive information about this implementation.
     */
    private static final String info = "McastService/2.1";

    /**
     * The implementation specific properties
     */
    protected Properties properties = new Properties();
    
    protected byte[] payload;

    protected byte[] domain;

    public void setProperties(Properties properties) {
        //TODO: Method implementation

    }

    public Properties getProperties() {
        //TODO: Method implementation
        return null;
    }

    public void start() throws Exception {
        //TODO: Method implementation

    }

    public void start(int i) throws Exception {
        //TODO: Method implementation

    }

    public void stop(int i) {
        //TODO: Method implementation

    }

    public boolean hasMembers() {
        //TODO: Method implementation
        return false;
    }

    public Member getMember(Member member) {
        //TODO: Method implementation
        try {
            return new MemberImpl("127.0.0.1", 11, 111);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return member;
    }

    public Member[] getMembers() {
        //TODO: Method implementation
        return new Member[0];
    }

    public Member getLocalMember(boolean b) {
        //TODO: Method implementation
        return new MemberImpl();
    }

    public String[] getMembersByName() {
        //TODO: Method implementation
        return new String[0];
    }

    public Member findMemberByName(String s) {
        //TODO: Method implementation
        try {
            return new MemberImpl("127.0.0.1", 11, 111);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void setLocalMemberProperties(String s, int i) {
        //TODO: Method implementation

    }

    public void setMembershipListener(MembershipListener membershipListener) {
        //TODO: Method implementation

    }

    public void removeMembershipListener() {
        //TODO: Method implementation

    }

    public void setPayload(byte[] bytes) {
        //TODO: Method implementation

    }

    public void setDomain(byte[] bytes) {
        //TODO: Method implementation

    }
}
