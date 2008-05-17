/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


package org.apache.axis2.clustering;

/**
 * All constants used by the Axis2 clustering implementation
 */
public final class ClusteringConstants {

    private ClusteringConstants() {
    }

    /**
     * The default domain to which this node belongs to
     */
    public static final String DEFAULT_DOMAIN = "apache.axis2.domain";

    public static final String NODE_MANAGER_SERVICE = "Axis2NodeManager";
    public static final String REQUEST_BLOCKING_HANDLER = "RequestBlockingHandler";
    public static final String CLUSTER_INITIALIZED = "local_cluster.initialized";
    public static final String RECD_CONFIG_INIT_MSG = "local_recd.config.init.method";
    public static final String RECD_STATE_INIT_MSG = "local_recd.state.init.method";
    public static final String BLOCK_ALL_REQUESTS = "local_wso2wsas.block.requests";
    public static final String LOCAL_IP_ADDRESS = "axis2.local.ip.address";

    /**
     * The main cluster configuration parameters
     */
    public static final class Parameters {

        /**
         * The membership scheme used in this setup. The only values supported at the moment are
         * "multicast" and "wka"
         */
        public static final String MEMBERSHIP_SCHEME = "membershipScheme";

        /**
         * The clustering domain/group. Nodes in the same group will belong to the same multicast
         * domain. There will not be interference between nodes in different groups.
         */
        public static final String DOMAIN = "domain";

        /**
         * When a Web service request is received, and processed, before the response is sent to the
         * client, should we update the states of all members in the cluster? If the value of
         * this parameter is set to "true", the response to the client will be sent only after
         * all the members have been updated. Obviously, this can be time consuming. In some cases,
         * such this overhead may not be acceptable, in which case the value of this parameter
         * should be set to "false"
         */
        public static final String SYNCHRONIZE_ALL_MEMBERS = "synchronizeAll";

        /**
         * Do not automatically initialize the cluster. The programmer has to explicitly initialize
         * the cluster.
         */
        public static final String AVOID_INITIATION = "AvoidInitiation";
    }

    public static final class MembershipScheme {
        /**
         * Multicast based membership discovery scheme
         */
        public static final String MULTICAST_BASED = "multicast";

        /**
         * Well-Known Address based membership management scheme
         */
        public static final String WKA_BASED = "wka";
    }
}
