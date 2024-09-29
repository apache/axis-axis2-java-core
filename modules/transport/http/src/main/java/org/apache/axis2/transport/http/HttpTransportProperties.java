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

import java.util.Properties;

/**
 * Utility bean for setting transport properties in runtime.
 */
public abstract class HttpTransportProperties {
    
    protected boolean chunked;    
    protected String protocol;
       
    public abstract void setHttpVersion(Object httpVerion);
    
    public abstract Object getHttpVersion();

    public boolean getChunked() {
        return chunked;
    }
    public String getProtocol() {
        return protocol;
    }

    public void setChunked(boolean chunked) {
        this.chunked = chunked;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public static class ProxyProperties {
        protected int proxyPort = -1;
        protected String domain = null;
        protected String passWord = null;
        protected String proxyHostName = null;
        protected String userName = null;

        public ProxyProperties() {
        }

        public String getDomain() {
            return domain;
        }

        public String getPassWord() {
            return passWord;
        }

        public String getProxyHostName() {
            return proxyHostName;
        }

        public int getProxyPort() {
            return proxyPort;
        }

        public String getUserName() {
            return userName;
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

    /**
     * @deprecated org.apache.axis2.transport.http.HttpTransportProperties.MailProperties has been
     * deprecated and user are encourage the use of java.util.Properties instead.  
     */
    public static class MailProperties {
        final Properties mailProperties = new Properties();

        private String password;

        public void addProperty(String key, String value) {
            mailProperties.put(key, value);
        }

        public void deleteProperty(String key) {
            mailProperties.remove(key);
        }

        public Properties getProperties() {
            return mailProperties;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

    }
}
