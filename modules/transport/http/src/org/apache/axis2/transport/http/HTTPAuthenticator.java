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

package org.apache.axis2.transport.http;

import java.util.List;

public abstract class HTTPAuthenticator {

    /* host that needed to be authenticated with */
    private String host;
    /* Domain needed by NTCredentials for NT Domain */
    private String domain;
    /* User for authenticate */
    private String username;
    /* Password of the user for authenticate */
    private String password;
    /* Switch to use preemptive authentication or not */
    private boolean preemptive = false;
    /* if Authentication scheme needs retry just turn on the following flag */
    private boolean allowedRetry = false;
    /* Changing the priorty or adding a custom AuthPolicy */
    private List authSchemes;

    public abstract int getPort();

    public abstract void setPort(int port);

    public abstract String getRealm();

    public abstract void setRealm(String realm);

    public abstract Object getAuthPolicyPref(String schema);

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setPreemptiveAuthentication(boolean preemptive) {
        this.preemptive = preemptive;
    }

    public boolean getPreemptiveAuthentication() {
        return this.preemptive;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public void setAuthSchemes(List authSchemes) {
        this.authSchemes = authSchemes;
    }

    public List getAuthSchemes() {
        return this.authSchemes;
    }

    public void setAllowedRetry(boolean allowedRetry) {
        this.allowedRetry = allowedRetry;
    }

    public boolean isAllowedRetry() {
        return this.allowedRetry;
    }

}
