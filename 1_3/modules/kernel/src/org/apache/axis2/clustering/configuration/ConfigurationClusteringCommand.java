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

package org.apache.axis2.clustering.configuration;

import org.apache.axis2.clustering.ClusteringCommand;
import org.apache.axis2.context.ConfigurationContext;

/**
 * This class represents the 2-phase commit protocol, where an event is processed,
 * the system is prepared to switch to a new configuration based on the processed event,
 * and finally commits the new configuration (i.e. the system switches to the new configuration).
 * As can be seen, this is a 3-step process.
 */
public abstract class ConfigurationClusteringCommand extends ClusteringCommand {

    public static final int RELOAD_CONFIGURATION = 0;
    public static final int LOAD_SERVICE_GROUPS = 1;
    public static final int UNLOAD_SERVICE_GROUPS = 2;
    public static final int APPLY_SERVICE_POLICY = 3;
    public static final int PREPARE = 4;
    public static final int COMMIT = 5;
    public static final int EXCEPTION = 6;
    public static final int ROLLBACK = 7;

    /**
     * Get the command type
     * 
     * @return The command type
     */
    public abstract int getCommandType();

    /**
     * Process the <code>event</event>. The implementer of this interface will
     * need to cache the outcome of this processing.
     *
     * @param configContext
     * @throws Exception
     */
    public abstract void process(ConfigurationContext configContext) throws Exception;

    /**
     * Prepare to switch to the new configuration
     *
     * @param configContext
     */
    public abstract void prepare(ConfigurationContext configContext);

    /**
     * Commit the new configuration. i.e. switch the system to the new configuration
     *
     * @param configContext
     * @throws Exception
     */
    public abstract void commit(ConfigurationContext configContext) throws Exception;

    /**
     * Rollback any changes carried out
     *
     * @param configContext
     * @throws Exception
     */
    public abstract void rollback(ConfigurationContext configContext) throws Exception;
}
