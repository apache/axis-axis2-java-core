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

package org.apache.axis2.clustering.tribes.info;

import org.apache.axis2.clustering.context.ContextClusteringCommand;
import org.apache.catalina.tribes.ChannelListener;
import org.apache.catalina.tribes.Member;

import java.io.Serializable;

public class TransientTribesChannelInfo implements ChannelListener {

    private long messageCount = 0;

    private long grpCtxCreationCount = 0;

    private long srvCtxCreationCount = 0;

    private long grpCtxRemoveCount = 0;

    private long srvCtxRemoveCount = 0;

    private long updateConfigCtxCount = 0;

    public long getUpdateServiceCtxCount() {
        return updateServiceCtxCount;
    }

    public long getUpdateServiceGroupCtxCount() {
        return updateServiceGroupCtxCount;
    }

    private long updateServiceCtxCount = 0;
    private long updateServiceGroupCtxCount = 0;

    public boolean accept(Serializable msg, Member sender) {
        return msg instanceof String;
    }

    public void messageReceived(Serializable msg, Member sender) {
        messageCount++;

        System.out.println("Tribes message " + msg);

        if (msg instanceof ContextClusteringCommand) {
            ContextClusteringCommand cmd = (ContextClusteringCommand) msg;

            if (cmd.getCommandType() == ContextClusteringCommand.CREATE_SERVICE_GROUP_CONTEXT) {
                grpCtxCreationCount++;
            } else if (cmd.getCommandType() == ContextClusteringCommand.CREATE_SERVICE_CONTEXT) {
                srvCtxCreationCount++;
            } else if (cmd.getCommandType() ==
                       ContextClusteringCommand.DELETE_SERVICE_GROUP_CONTEXT) {
                grpCtxRemoveCount++;
            } else if (cmd.getCommandType() ==
                       ContextClusteringCommand.DELETE_SERVICE_CONTEXT) {
                srvCtxRemoveCount++;
            } else if (cmd.getCommandType() ==
                       ContextClusteringCommand.UPDATE_CONFIGURATION_CONTEXT) {
                updateConfigCtxCount++;
            } else if (cmd.getCommandType() ==
                       ContextClusteringCommand.UPDATE_SERVICE_CONTEXT) {
                updateServiceCtxCount++;
            }else if (cmd.getCommandType() ==
                      ContextClusteringCommand.UPDATE_SERVICE_GROUP_CONTEXT) {
                updateServiceGroupCtxCount++;
            }
        }
    }

    public long getGrpCtxCreationCount() {
        return grpCtxCreationCount;
    }

    public long getGrpCtxRemoveCount() {
        return grpCtxRemoveCount;
    }

    public long getMessageCount() {
        return messageCount;
    }

    public long getSrvCtxCreationCount() {
        return srvCtxCreationCount;
    }

    public long getSrvCtxRemoveCount() {
        return srvCtxRemoveCount;
    }

    public long getUpdateConfigCtxCount() {
        return updateConfigCtxCount;
    }

}
