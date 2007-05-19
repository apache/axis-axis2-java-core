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

package org.apache.axis2.cluster.context;

import org.apache.axis2.cluster.ClusteringFault;
import org.apache.axis2.cluster.CommandMessage;
import org.apache.axis2.context.ConfigurationContext;

public abstract class ContextCommandMessage extends CommandMessage {

    public static final int CREATE_SERVICE_GROUP_CONTEXT_MSG = 0;
    public static final int CREATE_SERVICE_CONTEXT_MSG = 1;
    public static final int CREATE_SESSION_CONTEXT_MSG = 2;
    public static final int UPDATE_SERVICE_GROUP_CONTEXT_MSG = 3;
    public static final int UPDATE_SERVICE_CONTEXT_MSG = 4;
    public static final int UPDATE_CONFIGURATION_CONTEXT_MSG = 5;
    public static final int DELETE_SERVICE_GROUP_CONTEXT_MSG = 6;
    public static final int DELETE_SERVICE_CONTEXT_MSG = 7;

    protected ContextCommandMessage() {
    }

    public abstract void execute(ConfigurationContext configContext) throws ClusteringFault;

    public abstract int getMessageType();
}
