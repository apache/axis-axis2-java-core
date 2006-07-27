package org.apache.axis2.transport.http;

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

import org.apache.commons.httpclient.HttpVersion;

/**
 * Utility bean for setting transport properties in runtime.
 */
public class HttpTransportProperties {
    protected boolean chunked;
    protected HttpVersion httpVersion;
    protected String protocol;

    public HttpTransportProperties() {
    }

    public boolean getChunked() {
        return chunked;
    }

    public HttpVersion getHttpVersion() {
        return httpVersion;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setChunked(boolean chunked) {
        this.chunked = chunked;
    }

    public void setHttpVersion(HttpVersion httpVerion) {
        this.httpVersion = httpVerion;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public static class ProxyProperties {
        protected int proxyPort = -1;
        protected String domain;
        protected String passWord;
        protected String proxyHostName;
        protected String userName;

        public ProxyProperties() {
        }

        public String getDomain() {
            if (domain == null || domain.length() == 0) {
                return "anonymous";
            } else {
                return domain;
            }
        }

        public String getPassWord() {
            if (passWord == null || passWord.length() == 0) {
                return "anonymous";
            } else {
                return passWord;
            }
        }

        public String getProxyHostName() {
            return proxyHostName;
        }

        public int getProxyPort() {
            return proxyPort;
        }

        public String getUserName() {
            if (userName == null || userName.length() == 0) {
                return "anonymous";
            } else {
                return userName;
            }
        }

        public void setDomain(String domain) {
            this.domain = domain;
        }

        public void setPassWord(String passWord) {
            this.passWord = passWord;
        }

        public void setProxyName(String proxyHostName) {
            this.proxyHostName = proxyHostName;
        }

        public void setProxyPort(int proxyPort) {
            this.proxyPort = proxyPort;
        }

        public void setUserName(String userName) {
            this.userName = userName;
        }
    }
    public static class BasicAuthentication{
        private String host;
        private int port = -1;
        private String realm;
        private String username;
        private String password;

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

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
    }
    public static class NTLMAuthentication{
        private String host;
        private int port = -1;
        private String realm;
        private String username;
        private String password;

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

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
    }
}
