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

/**
 * This class holds the configuration parameters which are specific to Tribes
 */
public final class TribesConstants {

    public static final String LOCAL_MEMBER_HOST = "localMemberHost";
    public static final String LOCAL_MEMBER_PORT = "localMemberPort";

    public static final String MCAST_ADDRESS = "mcastAddress";
    public static final String MCAST_BIND_ADDRESS = "multicastBindAddress";
    public static final String MCAST_PORT = "mcastPort";
    public static final String MCAST_FREQUENCY = "mcastFrequency";
    public static final String MEMBER_DROP_TIME = "memberDropTime";
    public static final String MCAST_CLUSTER_DOMAIN = "mcastClusterDomain";
    public static final String TCP_LISTEN_HOST = "tcpListenHost";
    public static final String BIND_ADDRESS = "bindAddress";
    public static final String TCP_LISTEN_PORT = "tcpListenPort";
}
