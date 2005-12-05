/*
 * Copyright 2001, 2002,2004 The Apache Software Foundation.
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

package org.apache.axis2.transport.jms;

import javax.jms.MessageListener;
import java.util.HashMap;

/*
 * Subscription class holds information about a subscription
 */

public class Subscription {
    MessageListener m_listener;
    JMSEndpoint m_endpoint;
    String m_messageSelector;
    int m_ackMode;

    Subscription(MessageListener listener,
                 JMSEndpoint endpoint,
                 HashMap properties) {
        m_listener = listener;
        m_endpoint = endpoint;
        m_messageSelector = MapUtils.removeStringProperty(
                properties,
                JMSConstants.MESSAGE_SELECTOR,
                null);
        m_ackMode = MapUtils.removeIntProperty(properties,
                JMSConstants.ACKNOWLEDGE_MODE,
                JMSConstants.DEFAULT_ACKNOWLEDGE_MODE);
    }

    public int hashCode() {
        return toString().hashCode();
    }

    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof Subscription))
            return false;
        Subscription other = (Subscription) obj;
        if (m_messageSelector == null) {
            if (other.m_messageSelector != null)
                return false;
        } else {
            if (other.m_messageSelector == null ||
                    !other.m_messageSelector.equals(m_messageSelector))
                return false;
        }
        return m_ackMode == other.m_ackMode &&
                m_endpoint.equals(other.m_endpoint) &&
                other.m_listener.equals(m_listener);
    }

    public String toString() {
        return m_listener.toString();
    }

}