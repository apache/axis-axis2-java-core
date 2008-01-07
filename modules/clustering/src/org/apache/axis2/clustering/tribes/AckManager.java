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
import org.apache.axis2.clustering.context.ContextClusteringCommand;
import org.apache.catalina.tribes.Member;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * This class is responsible for handling all ACKs by the members in a cluster
 */
public final class AckManager {
    private static Log log = LogFactory.getLog(AckManager.class);
    private static Map messageAckTable = new Hashtable();

    public static void addInitialAcknowledgement(ContextClusteringCommand command) {
        messageAckTable.put(command.getUniqueId(), new MessageACK(command));
    }

    /**
     * When a particular member send an ACK for a particular message, the ACK is stored here
     *
     * @param messageUniqueId ID of the message being ACKed
     * @param memberId        The ID of the member who ACKed the above message
     */
    public static void addAcknowledgement(String messageUniqueId,
                                          String memberId) {
        MessageACK ack = (MessageACK) messageAckTable.get(messageUniqueId);
        if (ack != null) {
            if (!ack.hasACKed(memberId)) {  // If the member has not already ACKed
                ack.addACK(memberId);
            }
        }
    }

    public static void removeMessage(String messageUniqueId) {
        messageAckTable.remove(messageUniqueId);
    }

    /**
     * Check whether a particular message has been ACKed by all members in a cluster. If we find that
     * a particular message is not ACKed, we will retransmit the message to the member who did not ACK
     * and then return false.
     *
     * @param messageUniqueId ID of the message being ACKed
     * @param sender          The utility for sending the message
     * @return true - if all members have ACKed the message, false - otherwise
     * @throws ClusteringFault If an error occurs while retrannsmitting a message
     */
    public static boolean isMessageAcknowledged(String messageUniqueId,
                                                ChannelSender sender) throws ClusteringFault {

        boolean isAcknowledged = false;
        boolean isReturnValueSet = false;
        if (messageUniqueId == null) {
            return true;
        }
        MessageACK ack = (MessageACK) messageAckTable.get(messageUniqueId);
        if (ack == null) {  // If the message is not found, treat it as ACKed
            return true;
        }

        // Check that all members in the memberList are same as the total member list,
        // which will indicate that all members have ACKed the message
        Member[] members = MembershipManager.getMembers();
        if (members.length == 0) {
            isAcknowledged = true;
        } else {
            for (int i = 0; i < members.length; i++) {
                Member member = members[i];
                String memberHost = TribesUtil.getHost(member);
                if (member.isReady() && !ack.hasACKed(memberHost)) {
                    log.debug("[NO ACK] from member " + memberHost);

                    // If a new member joined the cluster recently,
                    // we need to retransmit the message to this member, if an ACK has not been
                    // received from this member. We retransmit only once.
                    if (member.getMemberAliveTime() < 5000 &&
                        !ack.isRestransmittedToMember(memberHost)) { // TODO: Check

                        sender.sendToMember(ack.getCommand(), member);
                        log.debug("Retransimitting msg " + ack.getCommand().getUniqueId() +
                                  " to member " + memberHost);
                    }

                    if (!isReturnValueSet) {
                        isAcknowledged = false;
                        isReturnValueSet = true;
                    }
                } else {
                    if (!isReturnValueSet) {
                        isAcknowledged = true;
                    }
                }
            }
        }

        // If a message is ACKed by all members, we don't have to keep track of
        // it in our ackTbl anymore
        if (isAcknowledged) {
            messageAckTable.remove(messageUniqueId);
        }
        return isAcknowledged;
    }

    /**
     * Data structure for holding the ACKs for each message
     */
    private static class MessageACK {
        private ContextClusteringCommand command;
        private List memberList = new Vector();
        private List retransmittedList = new ArrayList();

        public MessageACK(ContextClusteringCommand command) {
            this.command = command;
        }

        public void addACK(String memberId) {
            memberList.add(memberId);
        }

        public ContextClusteringCommand getCommand() {
            return command;
        }

        public boolean hasACKed(String memberId) {
            return memberList.contains(memberId);
        }

        public void addToRestransmittedList(String memberId) {
            retransmittedList.add(memberId);
        }

        public boolean isRestransmittedToMember(String memberId) {
            return retransmittedList.contains(memberId);
        }
    }
}
