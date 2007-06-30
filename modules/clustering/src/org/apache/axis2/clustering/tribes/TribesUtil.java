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

import org.apache.catalina.tribes.Channel;
import org.apache.catalina.tribes.Member;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class TribesUtil {

    private static Log log = LogFactory.getLog(TribesUtil.class);

    public static void printMembers(Member[] members) {
        if (members != null) {
            int length = members.length;
            if (length > 0) {
                log.info("Members of current cluster");
                for (int i = 0; i < length; i++) {
                    log.info("Member" + (i + 1) + " " + getHost(members[i]));
                }
            } else {
                log.info("No members in current cluster");
            }
        }
    }

    public static String getHost(Member member) {
        byte[] hostBytes = member.getHost();
        StringBuffer host = new StringBuffer();
        if (hostBytes != null) {
            for (int i = 0; i < hostBytes.length; i++) {
                int hostByte = hostBytes[i] >= 0 ? (int) hostBytes[i] : (int) hostBytes[i] + 256;
                host.append(hostByte).append(".");
            }
        }
        return host.append(":").append(member.getPort()).toString();
    }

    public static String getLocalHost(Channel channel) {
        return getHost(channel.getLocalMember(true));
    }
}
