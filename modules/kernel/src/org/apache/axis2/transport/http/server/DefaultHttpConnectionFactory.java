/*
 * $HeadURL:https://svn.apache.org/repos/asf/jakarta/httpcomponents/trunk/coyote-httpconnector/src/java/org/apache/http/tcconnector/impl/DefaultHttpConnectionFactory.java $
 * $Revision:379772 $
 * $Date:2006-02-22 14:52:29 +0100 (Wed, 22 Feb 2006) $
 *
 * ====================================================================
 *
 *  Copyright 1999-2006 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */

package org.apache.axis2.transport.http.server;

import org.apache.http.HttpServerConnection;
import org.apache.http.impl.DefaultHttpServerConnection;
import org.apache.http.params.HttpParams;

import java.io.IOException;
import java.net.Socket;

public class DefaultHttpConnectionFactory implements HttpConnectionFactory {

    final HttpParams params;
    
    public DefaultHttpConnectionFactory(final HttpParams params) {
        super();
        if (params == null) {
            throw new IllegalArgumentException("HTTP parameters may not be null");
        }
        this.params = params;
    }
    
    public HttpServerConnection newConnection(final Socket socket)
            throws IOException {
        DefaultHttpServerConnection conn = new Axis2HttpServerConnection();
        conn.bind(socket, this.params);
        return conn;
    }
    
    public class Axis2HttpServerConnection extends DefaultHttpServerConnection {
        public Axis2HttpServerConnection() {
            super();
        }

        public String getRemoteIPAddress() {
            java.net.SocketAddress sa = socket.getRemoteSocketAddress();
            if (sa instanceof java.net.InetSocketAddress) {
                return ((java.net.InetSocketAddress) sa).getAddress().getHostAddress();
            } else {
                return sa.toString();
            }
        }

        public String getRemoteHostName() {
            java.net.SocketAddress sa = socket.getRemoteSocketAddress();
            if (sa instanceof java.net.InetSocketAddress) {
              return ((java.net.InetSocketAddress) sa).getHostName();
          } else {
              return sa.toString(); // fail-safe and fall back to something which one can use in place of the host name
          }
        }
    }

}
