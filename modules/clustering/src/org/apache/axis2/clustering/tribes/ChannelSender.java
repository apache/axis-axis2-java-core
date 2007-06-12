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

import org.apache.axis2.clustering.ClusteringCommand;
import org.apache.axis2.clustering.ClusteringFault;
import org.apache.axis2.clustering.MessageSender;
import org.apache.catalina.tribes.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ChannelSender implements MessageSender {

    private Log log = LogFactory.getLog(ChannelSender.class);
    private Channel channel;

    public void sendToGroup(ClusteringCommand msg) throws ClusteringFault {
        if (channel == null) {
            return;
        }

        // Keep retrying, since at the point of trying to send the msg, a member may leave the group
        // causing a view change. All nodes in a view should get the msg
        while (true) {
            if (channel.getMembers().length > 0) {
                try {
                    channel.send(channel.getMembers(), msg, 0); 
                    log.debug("Sent " + msg + " to group");
                    break;
                } catch (ChannelException e) {
                    String message = "Error sending command message : " + msg +
                                     ". Reason " + e.getMessage();
                    log.warn(message);
                }
            } else {
                break;
            }
        }
    }

    public void sendToSelf(ClusteringCommand msg) throws ClusteringFault {
        if (channel == null) {
            return;
        }
        try {
            channel.send(new Member[]{channel.getLocalMember(true)},
                         msg,
                         0);
            log.debug("Sent " + msg + " to self");
        } catch (ChannelException e) {
            throw new ClusteringFault(e);
        }
    }

    public void sendToGroup(Throwable throwable) throws ClusteringFault {
        if (channel == null) {
            return;
        }

        // Keep retrying, since at the point of trying to send the msg, a member may leave the group
        while (true) {
            if (channel.getMembers().length > 0) {
                try {
                    channel.send(channel.getMembers(), throwable, 0);
                    log.debug("Sent " + throwable + " to group");
                } catch (ChannelException e) {
                    String message = "Error sending exception message : " + throwable +
                                     ". Reason " + e.getMessage();
                    log.warn(message);
                }
            } else {
                break;
            }
        }
    }

    public void sendToMember(ClusteringCommand cmd, Member member) throws ClusteringFault {
        try {
            if (member.isReady()) {
                channel.send(new Member[]{member}, cmd, 0);
                log.debug("Sent " + cmd + " to " + member.getName());
            }
        } catch (ChannelException e) {
            String message = "Could not send message to " + member.getName() +
                             ". Reason " + e.getMessage();
            log.warn(message);
        }
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }
}
