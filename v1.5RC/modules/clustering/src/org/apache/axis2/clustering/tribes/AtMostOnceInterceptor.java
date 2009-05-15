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

import org.apache.catalina.tribes.ChannelMessage;
import org.apache.catalina.tribes.group.ChannelInterceptorBase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Message intereceptor for handling at-most-once message processing semantics
 */
public class AtMostOnceInterceptor extends ChannelInterceptorBase {          

    private static Log log = LogFactory.getLog(AtMostOnceInterceptor.class);
    private static final Map<ChannelMessage, Long> receivedMessages =
            new HashMap<ChannelMessage, Long>();

    /**
     * The time a message lives in the receivedMessages Map
     */
    private static final int TIMEOUT = 5 * 60 * 1000;

    public AtMostOnceInterceptor() {
        Thread cleanupThread = new Thread(new MessageCleanupTask());
        cleanupThread.setPriority(Thread.MIN_PRIORITY);
        cleanupThread.start();
    }

    public void messageReceived(ChannelMessage msg) {
        if (okToProcess(msg.getOptions())) {
            synchronized (receivedMessages) {
                if (receivedMessages.get(msg) == null) {  // If it is a new message, keep track of it
                    receivedMessages.put(msg, System.currentTimeMillis());
                    super.messageReceived(msg);
                } else {  // If it is a duplicate message, discard it. i.e. dont call super.messageReceived
                    log.info("Duplicate message received from " + TribesUtil.getName(msg.getAddress()));
                }
            }
        } else {
            super.messageReceived(msg);
        }
    }

    private class MessageCleanupTask implements Runnable {

        public void run() {
            while (true) { // This task should never terminate
                try {
                    Thread.sleep(TIMEOUT);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                try {
                    List<ChannelMessage> toBeRemoved = new ArrayList<ChannelMessage>();
                    Thread.yield();
                    synchronized (receivedMessages) {
                        for (ChannelMessage msg : receivedMessages.keySet()) {
                            long arrivalTime = receivedMessages.get(msg);
                            if (System.currentTimeMillis() - arrivalTime >= TIMEOUT) {
                                toBeRemoved.add(msg);
                                if (toBeRemoved.size() > 10000) { // Do not allow this thread to run for too long
                                    break;
                                }
                            }
                        }
                        for (ChannelMessage msg : toBeRemoved) {
                            receivedMessages.remove(msg);
                            if (log.isDebugEnabled()) {
                                log.debug("Cleaned up message ");
                            }
                        }
                    }
                } catch (Throwable e) {
                    log.error("Exception occurred while trying to cleanup messages", e);
                }
            }
        }
    }
}
