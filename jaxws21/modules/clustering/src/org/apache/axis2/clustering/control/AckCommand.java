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
package org.apache.axis2.clustering.control;

import org.apache.axis2.clustering.ClusteringFault;
import org.apache.axis2.clustering.tribes.AckManager;
import org.apache.axis2.context.ConfigurationContext;

/**
 * ACK for the message with id <code>uniqueId</code>
 */
public class AckCommand extends ControlCommand {
    private String uniqueId;
    private String memberId;

    public AckCommand(String messageUniqueId) {
        this.uniqueId = messageUniqueId;
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    public int getCommandType() {
        return Integer.MAX_VALUE;
    }

    public void execute(ConfigurationContext configurationContext) throws ClusteringFault {
        AckManager.addAcknowledgement(uniqueId, memberId);
    }

    public String toString() {
        return "ACK for message with UUID " + uniqueId;
    }
}
