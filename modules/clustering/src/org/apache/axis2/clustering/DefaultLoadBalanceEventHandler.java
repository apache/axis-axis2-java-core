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
package org.apache.axis2.clustering;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.List;
import java.util.ArrayList;

/**
 * The default, dummy implementation of {@link LoadBalanceEventHandler}
 */
public class DefaultLoadBalanceEventHandler implements LoadBalanceEventHandler{

    private static final Log log  = LogFactory.getLog(DefaultLoadBalanceEventHandler.class);
    private List<Member> members = new ArrayList<Member>();

    public void applicationMemberAdded(Member member) {
        log.info("Application member " + member + " joined cluster.");
        members.add(member);
    }

    public void applicationMemberRemoved(Member member) {
        log.info("Application member " + member + " left cluster.");
        members.remove(member);
    }

    public List<Member> getMembers() {
        return members;
    }

}
