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

package org.apache.rampart.action;

import org.apache.rampart.RampartException;
import org.apache.rampart.RampartMessageData;
import org.apache.ws.security.message.WSSecTimestamp;

/**
 * Timestamp action
 */
public class TimestampAction implements Action {

    public void execute(RampartMessageData messageData) throws RampartException {
        WSSecTimestamp timeStampBuilder = new WSSecTimestamp();
        timeStampBuilder.setWsConfig(messageData.getConfig());

        timeStampBuilder.setTimeToLive(getTimeToLive(messageData));
        // add the Timestamp to the SOAP Enevelope
        timeStampBuilder.build(messageData.getDocument(), messageData
                .getSecHeader());
    }

    private int getTimeToLive(RampartMessageData messageData) {

        String ttl = messageData.getPolicyData().getRampartConfig()
                .getTimestampTTL();
        int ttl_i = 0;
        if (ttl != null) {
            try {
                ttl_i = Integer.parseInt(ttl);
            } catch (NumberFormatException e) {
                ttl_i = messageData.getTimeToLive();
            }
        }
        if (ttl_i <= 0) {
            ttl_i = messageData.getTimeToLive();
        }
        return ttl_i;
    }
}
