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

    public static final String AVOID_INITIATION_KEY = "AvoidInitiation";

    /**
     * The clustering domain/group. Nodes in the same group will belong to the same multicast domain.
     * There will not be interference between nodes in different group.
     */
    public static final String DOMAIN = "domain";

    public static final String NODE_MANAGER_SERVICE = "Axis2NodeManager";
    public static final String REQUEST_BLOCKING_HANDLER = "RequestBlockingHandler";
    public static final String CLUSTER_INITIALIZED = "local_cluster.initialized";
    public static final String RECD_CONFIG_INIT_MSG = "local_recd.config.init.method";
    public static final String RECD_STATE_INIT_MSG = "local_recd.state.init.method";
    public static final String BLOCK_ALL_REQUESTS = "local_wso2wsas.block.requests";
    public static final String LOCAL_IP_ADDRESS = "axis2.local.ip.address";

    /**
     * Synchronize the states of all members in the cluster
     */
    public static final String SYNCHRONIZE_ALL_MEMBERS = "synchronizeAll";

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
