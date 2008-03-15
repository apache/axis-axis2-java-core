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
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Message intereceptor for handling at-most-once message processing semantics
 */
public class AtMostOnceInterceptor extends ChannelInterceptorBase {          

    private static Log log = LogFactory.getLog(AtMostOnceInterceptor.class);
    private static final Map receivedMessages = new HashMap();

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
        synchronized (receivedMessages) {
            if (receivedMessages.get(msg) == null) {  // If it is a new message, keep track of it
                receivedMessages.put(msg, new Long(System.currentTimeMillis()));
                super.messageReceived(msg);
            } else {  // If it is a duplicate message, discard it. i.e. dont call super.messageReceived
                log.info("Duplicate message received from " + TribesUtil.getHost(msg.getAddress()));
            }
        }
    }

    private class MessageCleanupTask implements Runnable {

        public void run() {
            while (true) {
                try {
                    Thread.sleep(TIMEOUT);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                try {
                    List toBeRemoved = new ArrayList();
                    Thread.yield();
                    synchronized (receivedMessages) {
                        for (Iterator iterator = receivedMessages.keySet().iterator();
                             iterator.hasNext();) {
                            ChannelMessage msg = (ChannelMessage) iterator.next();
                            long arrivalTime = ((Long) receivedMessages.get(msg)).longValue();
                            if (System.currentTimeMillis() - arrivalTime >= TIMEOUT) {
                                toBeRemoved.add(msg);
                                if(toBeRemoved.size() > 10000){ // Do not allow this thread to run for too long
                                    break;
                                }
                            }
                        }
                        for (Iterator iterator = toBeRemoved.iterator(); iterator.hasNext();) {
                            ChannelMessage msg = (ChannelMessage) iterator.next();
                            receivedMessages.remove(msg);
                            if (log.isDebugEnabled()) {
                                log.debug("Cleaned up message ");
                            }
                        }
                    }
                } catch (Exception e) {
                    log.error("Exception occurred while trying to cleanup messages", e);
                }
            }
        }
    }
}
