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

import java.util.List;

/**
 * This is the interface through which the load balancing event are notified.
 * This will only be used when this member is running in loadBalance mode. In order to do this,
 * in the axis2.xml file, set the value of the "mode" parameter to "loadBalance" and then provide
 * the class that implements this interface using the "loadBalanceEventHandler" entry.
 */
public interface LoadBalanceEventHandler {

    /**
     * An application member joined the application group
     *
     * @param member Represents the member who joined
     */
    void applicationMemberAdded(Member member);

    /**
     * An application member left the application group
     *
     * @param member Represents the member who left
     */
    void applicationMemberRemoved(Member member);

    /** Get the list of current members
     *
     * @return List of current members
     */
    List<Member> getMembers();
    
}
