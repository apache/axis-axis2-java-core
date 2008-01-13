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

import org.apache.catalina.tribes.ChannelException;
import org.apache.catalina.tribes.ChannelInterceptor;
import org.apache.catalina.tribes.ChannelMessage;
import org.apache.catalina.tribes.Member;
import org.apache.catalina.tribes.RemoteProcessException;
import org.apache.catalina.tribes.Channel;
import org.apache.catalina.tribes.ByteMessage;
import org.apache.catalina.tribes.util.UUIDGenerator;
import org.apache.catalina.tribes.io.ChannelData;
import org.apache.catalina.tribes.io.XByteBuffer;
import org.apache.catalina.tribes.membership.Membership;
import org.apache.catalina.tribes.membership.MemberImpl;
import org.apache.catalina.tribes.group.ChannelInterceptorBase;
import org.apache.catalina.tribes.group.InterceptorPayload;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.description.AxisServiceGroup;
import org.apache.axis2.description.AxisModule;

import java.util.HashMap;
import java.util.Arrays;
import java.util.Map;
import java.util.TimerTask;
import java.util.Timer;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.net.Socket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;
import java.net.ConnectException;
import java.io.Serializable;

/**
 * Message intereceptor for handling at-most-once message processing semantics
 */
public class AtMostOnceInterceptor extends ChannelInterceptorBase {

    private static Log log = LogFactory.getLog(AtMostOnceInterceptor.class);
    private static final Map receivedMessages = new HashMap();

    /**
     * The time a message lives in the receivedMessages Map
     */
    private static final int TIMEOUT = 60 * 1000;

    public AtMostOnceInterceptor() {

        TimerTask cleanupTask = new TimerTask() {
            public void run() {
                List toBeRemoved = new ArrayList();
                for (Iterator iterator = receivedMessages.keySet().iterator();
                     iterator.hasNext();) {
                    ChannelMessage msg = (ChannelMessage) iterator.next();
                    long arrivalTime = ((Long) receivedMessages.get(msg)).longValue();
                    if (System.currentTimeMillis() - arrivalTime >= TIMEOUT) {
                        toBeRemoved.add(msg);
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
        };
        new Timer().scheduleAtFixedRate(cleanupTask, TIMEOUT, TIMEOUT);
    }

    public void messageReceived(ChannelMessage msg) {
        super.messageReceived(msg);
        if (receivedMessages.get(msg) == null) {  // If it is a new message, keep track of it
            receivedMessages.put(msg, new Long(System.currentTimeMillis()));
        } else {  // If it is a duplicate message, discard it. i.e. dont call super.messageReceived
            log.info("Duplicate message received from " + TribesUtil.getHost(msg.getAddress()));
        }
    }
}
