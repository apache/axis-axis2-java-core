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

package org.apache.axis2.clustering.tribes;

import org.apache.axis2.clustering.ClusteringFault;
import org.apache.axis2.clustering.control.ControlCommand;
import org.apache.axis2.clustering.control.GetConfigurationCommand;
import org.apache.axis2.clustering.control.GetStateCommand;
import org.apache.axis2.clustering.control.wka.JoinGroupCommand;
import org.apache.catalina.tribes.Member;
import org.apache.catalina.tribes.RemoteProcessException;
import org.apache.catalina.tribes.group.RpcCallback;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.Serializable;

/**
 * Handles initialization requests(GetConfiguration & GetState) from newly joining members
 */
public class InitializationRequestHandler implements RpcCallback {

    private static Log log = LogFactory.getLog(InitializationRequestHandler.class);
    private ControlCommandProcessor controlCommandProcessor;


    public InitializationRequestHandler(ControlCommandProcessor controlCommandProcessor) {
        this.controlCommandProcessor = controlCommandProcessor;
    }

    public Serializable replyRequest(Serializable msg, Member member) {
        if (msg instanceof GetStateCommand ||
            msg instanceof GetConfigurationCommand) {
            try {
                log.info("Received " + msg + " initialization request message from " +
                         TribesUtil.getHost(member));
                return controlCommandProcessor.process((ControlCommand) msg); // response is either GetConfigurationResponseCommand or GetStateResponseCommand
            } catch (ClusteringFault e) {
                String errMsg = "Cannot handle initialization request";
                log.error(errMsg, e);
                throw new RemoteProcessException(errMsg, e);
            }
        } else if (msg instanceof JoinGroupCommand) {
            log.info("Received " + msg + " from " + TribesUtil.getHost(member));
//            JoinGroupCommand command = (JoinGroupCommand) msg;
            try {
                return controlCommandProcessor.process((ControlCommand) msg); // response is 
            } catch (ClusteringFault e) {
                String errMsg = "Cannot handle JOIN request";
                log.error(errMsg, e);
                throw new RemoteProcessException(errMsg, e);
            }
        }
        return null;
    }

    public void leftOver(Serializable msg, Member member) {
        //TODO: Method implementation

    }
}
