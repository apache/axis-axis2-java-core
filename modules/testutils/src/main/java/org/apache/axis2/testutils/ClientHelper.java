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
package org.apache.axis2.testutils;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.xml.namespace.QName;

import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.client.Stub;
import org.apache.axis2.context.ConfigurationContext;

public class ClientHelper extends AbstractConfigurationContextRule {
    private final AbstractAxis2Server server;

    public ClientHelper(AbstractAxis2Server server, String repositoryPath) {
        super(repositoryPath);
        this.server = server;
    }

    public ClientHelper(AbstractAxis2Server server) {
        this(server, server.getRepositoryPath());
    }

    public ClientHelper(String repositoryPath) {
        this(null, repositoryPath);
    }

    @Override
    protected final void before() throws Throwable {
        super.before();
        if (server != null) {
            SSLContext sslContext = server.getClientSSLContext();
            if (sslContext != null) {
                getConfigurationContext().setProperty(SSLContext.class.getName(), sslContext);
            }
        }
    }

    private String getEndpoint(String endpoint) throws Exception {
        if (server != null && !endpoint.startsWith("http://")) {
            return server.getEndpoint(endpoint);
        } else {
            return endpoint;
        }
    }

    public final ServiceClient createServiceClient(String serviceName) throws Exception {
        ServiceClient serviceClient = new ServiceClient(getConfigurationContext(), null);
        serviceClient.getOptions().setTo(server.getEndpointReference(serviceName));
        configureServiceClient(serviceClient);
        return serviceClient;
    }

    public final ServiceClient createServiceClient(String serviceName, QName wsdlServiceName, String portName) throws Exception {
        URLStreamHandler handler;
        if (server.isSecure()) {
            final SSLContext sslContext = server.getClientSSLContext();
            handler = new URLStreamHandler() {
                @Override
                protected URLConnection openConnection(URL url) throws IOException {
                    HttpsURLConnection conn = (HttpsURLConnection)new URL(url.toExternalForm()).openConnection();
                    conn.setSSLSocketFactory(sslContext.getSocketFactory());
                    return conn;
                }
            };
        } else {
            handler = null;
        }
        ServiceClient serviceClient = new ServiceClient(getConfigurationContext(),
                new URL(null, server.getEndpoint(serviceName) + "?wsdl", handler), wsdlServiceName, portName);
        configureServiceClient(serviceClient);
        return serviceClient;
    }

    public final <T extends Stub> T createStub(Class<T> type, String endpoint) throws Exception {
        T stub = type
                .getConstructor(ConfigurationContext.class, String.class)
                .newInstance(getConfigurationContext(), getEndpoint(endpoint));
        configureServiceClient(stub._getServiceClient());
        return stub;
    }

    protected void configureServiceClient(ServiceClient serviceClient) throws Exception {
    }
}
