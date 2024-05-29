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

package org.apache.axis2.transport.http.impl.httpclient5;

import org.apache.axis2.transport.http.HTTPAuthenticator;
import org.apache.axis2.transport.http.HttpTransportProperties;
import org.apache.hc.core5.http.HttpVersion;
import org.apache.hc.client5.http.auth.AuthScope;
import org.apache.hc.client5.http.auth.StandardAuthScheme;

public class HttpTransportPropertiesImpl extends HttpTransportProperties {

    protected HttpVersion httpVersion;

    @Override
    public void setHttpVersion(Object httpVerion) {
        this.httpVersion = (HttpVersion) httpVerion;
    }

    @Override
    public Object getHttpVersion() {
        return this.httpVersion;
    }

    /*
     * This class is responsible for holding all the necessary information
     * needed for NTML, Digest, Basic and SPNEGO(Keberos) Authentication.
     * Authentication itself is handled by httpclient. User doesn't need
     * to worry about what authentication mechanism it uses. Axis2 uses
     * httpclinet's default authentication patterns.
     */
    public static class Authenticator extends HTTPAuthenticator {

        /* port of the host that needed to be authenticated with.
	 * May be -1 if applies to any port of the host.*/
        private int port = -1;
        /* Realm for authentication scope. 
	 * May be null if applies to any realm on the host. */
        private String realm = null;
        /* Default Auth Schems */
        public static final String NTLM = StandardAuthScheme.NTLM;
        public static final String DIGEST = StandardAuthScheme.DIGEST;
        public static final String BASIC = StandardAuthScheme.BASIC;

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }

        public String getRealm() {
            return realm;
        }

        public void setRealm(String realm) {
            this.realm = realm;
        }

        @Override
        public Object getAuthPolicyPref(String scheme) {
            if (BASIC.equals(scheme)) {
                return StandardAuthScheme.BASIC;
            } else if (NTLM.equals(scheme)) {
                return StandardAuthScheme.NTLM;
            } else if (DIGEST.equals(scheme)) {
                return StandardAuthScheme.DIGEST;
            }
            return null;
        }

    }

}
