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
import org.apache.catalina.tribes.Channel;
import org.apache.catalina.tribes.ChannelException;
import org.apache.catalina.tribes.Member;

public class ChannelSender implements MessageSender {

    private Channel channel;

    public void sendToGroup(ClusteringCommand msg) throws ClusteringFault {
        if(channel == null) return;
        Member[] members = channel.getMembers();
        if (members.length > 0) {
            try {
                channel.send(members, msg, 0);
            } catch (ChannelException e) {
                String message = "Error sending command message : " + msg;
                throw new ClusteringFault(message, e);
            }
        }
    }

    public void sendToSelf(ClusteringCommand msg) throws ClusteringFault {
        if(channel == null) return;
        try {
            channel.send(new Member[]{channel.getLocalMember(true)},
                         msg,
                         Channel.SEND_OPTIONS_USE_ACK);
        } catch (ChannelException e) {
            throw new ClusteringFault(e);
        }
    }

    public void sendToGroup(Throwable throwable) throws ClusteringFault {
        if(channel == null) return;
        Member[] group = channel.getMembers();
        if (group.length > 0) {
            try {
                channel.send(group, throwable, 0);
            } catch (ChannelException e) {
                String message = "Error sending exception message : " + throwable;
                throw new ClusteringFault(message, e);
            }
        }
    }

    public void sendToMember(ClusteringCommand cmd, Member member) throws ClusteringFault {
        try {
            channel.send(new Member[]{member}, cmd, Channel.SEND_OPTIONS_USE_ACK);
        } catch (ChannelException e) {
            throw new ClusteringFault(e);
        }
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }
}
