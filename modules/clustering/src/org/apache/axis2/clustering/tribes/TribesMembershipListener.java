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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 */
public class TribesMembershipListener implements MembershipListener {

    private static Log log = LogFactory.getLog(TribesMembershipListener.class);

    public void memberAdded(Member member) {
        log.info("New member " + getHostSocket(member) + " added to Tribes group.");
    }

    public void memberDisappeared(Member member) {
        log.info("Member " + getHostSocket(member) + " left Tribes group");
    }

    private String getHostSocket(Member member) {
        String host = null;
        byte[] hostBytes = member.getHost();
        for (int i = 0; i < hostBytes.length; i++) {
            host = (host == null) ? ("" + hostBytes[i]) : (host + "." + hostBytes[i]);
        }
        return host + ":" + member.getPort();
    }
}
