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

import java.net.URL;
import java.net.URLConnection;

/**
 * URLStreamHandler for the "jms" protocol
 */
public class Handler
        extends java.net.URLStreamHandler {
    static {
        // register the JMSTransport class
        //org.apache.axis.client.Call.setTransportForProtocol(JMSConstants.PROTOCOL, org.apache.axis2.transport.jms.JMSTransport.class);
    }

    /**
     * Reassembles the URL string, in the form "jms:/<dest>?prop1=value1&prop2=value2&..."
     */
    protected String toExternalForm(URL url) {

        String destination = url.getPath().substring(1);
        String query = url.getQuery();

        StringBuffer jmsurl = new StringBuffer(JMSConstants.PROTOCOL + ":/");
        jmsurl.append(destination).append("?").append(query);

        return jmsurl.toString();
    }

    protected URLConnection openConnection(URL url) {
        return new JMSURLConnection(url);
    }
}