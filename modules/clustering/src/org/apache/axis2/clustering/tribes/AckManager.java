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

import org.apache.axis2.clustering.ClusteringFault;
import org.apache.axis2.clustering.context.ContextClusteringCommand;
import org.apache.catalina.tribes.Member;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * 
 */
public final class AckManager {

    private static Map messageAckTable = new Hashtable();

    public static void addInitialAcknowledgement(ContextClusteringCommand command) {
        messageAckTable.put(command.getUniqueId(), new MessageACK(command));
    }

    public static void addAcknowledgement(String messageUniqueId,
                                          String memberId) {
        System.err.println("######### Added ACK for msg " + messageUniqueId + " from member " + memberId);
        MessageACK ack = (MessageACK) messageAckTable.get(messageUniqueId);
        if (ack != null) {
            List memberList = ack.getMemberList();
            memberList.add(memberId);
        }
    }

    public static boolean isMessageAcknowledged(String messageUniqueId,
                                                ChannelSender sender) throws ClusteringFault {
        System.err.println("########## Call to isMessageAcknowledged for msg UUID " + messageUniqueId);
        boolean isAcknowledged = false;
        MessageACK ack = (MessageACK) messageAckTable.get(messageUniqueId);
        List memberList = ack.getMemberList();

        // Check that all members in the memberList are same as the total member list,
        // which will indicate that all members have ACKed the message
        Member[] members = sender.getChannel().getMembers();
        for (int i = 0; i < members.length; i++) {
            Member member = members[i];
            if (!memberList.contains(member.getName())) {

                // At this point, resend the original message back to the node which has not
                // sent an ACK
                sender.sendToMember(ack.getCommand(), member);
                isAcknowledged = false;
                break;
            } else {
                isAcknowledged = true;
            }
        }

        // If a message is ACKed, we don't have to keep track of it in our ackTbl anymore
        if (isAcknowledged) {
            messageAckTable.remove(messageUniqueId);
        }
        return isAcknowledged;
    }

    private static class MessageACK {
        private ContextClusteringCommand command;
        private List memberList = new Vector();

        public MessageACK(ContextClusteringCommand command) {
            this.command = command;
        }

        public ContextClusteringCommand getCommand() {
            return command;
        }

        public List getMemberList() {
            return memberList;
        }
    }
}
