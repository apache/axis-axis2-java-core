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

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.llom.AxiomElementImpl;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.transport.http.HTTPTransportConstants;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.client5.http.auth.AuthScope;
import org.apache.hc.client5.http.auth.Credentials;
import org.apache.hc.client5.http.auth.CredentialsProvider;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.apache.axis2.transport.http.impl.httpclient5.HTTPProxyConfigurator;

// See AXIS2-5948: Proxy settings ignored if username not specified
public class HTTPProxyConfiguratorTest {

    @Test
    public void testProxyWithCredentials() throws AxisFault {
        final OMElement configurationElement = new AxiomElementImpl();

        final String hostname = "http://host";
        final OMElement host = new AxiomElementImpl();
        host.setText(hostname);
        host.setLocalName(HTTPTransportConstants.PROXY_HOST_ELEMENT);
        configurationElement.addChild(host);

        final int portNumber = 8080;
        final OMElement port = new AxiomElementImpl();
        port.setText(String.valueOf(portNumber));
        port.setLocalName(HTTPTransportConstants.PROXY_PORT_ELEMENT);
        configurationElement.addChild(port);

        final String user = "user";
        final OMElement username = new AxiomElementImpl();
        username.setText(user);
        username.setLocalName(HTTPTransportConstants.PROXY_USER_ELEMENT);
        configurationElement.addChild(username);

        final String pass = "password";
        final OMElement password = new AxiomElementImpl();
        password.setText(pass);
        password.setLocalName(HTTPTransportConstants.PROXY_PASSWORD_ELEMENT);
        configurationElement.addChild(password);

        final OMElement element = new AxiomElementImpl();
        element.addChild(configurationElement);
        final Parameter param = new Parameter();
        param.setParameterElement(element);
        param.setName(HTTPTransportConstants.ATTR_PROXY);
        final AxisConfiguration configuration = new AxisConfiguration();
        configuration.addParameter(param);
        final MessageContext messageContext = new MessageContext();
        final ConfigurationContext configurationContext = new ConfigurationContext(configuration);
        messageContext.setConfigurationContext(configurationContext);
        final RequestConfig.Builder builder = RequestConfig.custom();

        final HttpClientContext clientContext = new HttpClientContext();
        HTTPProxyConfigurator.configure(messageContext, builder, clientContext);
        final RequestConfig config = builder.build();
        final HttpHost proxyHost = config.getProxy();
        assertNotNull(proxyHost);
        assertEquals(hostname, proxyHost.getHostName());
        assertEquals(portNumber, proxyHost.getPort());

        final CredentialsProvider provider = clientContext.getCredentialsProvider();
        assertNotNull(provider);
        final Credentials credentials = provider.getCredentials(new AuthScope(null, -1), clientContext);
        assertNotNull(credentials);
        assertEquals(user, credentials.getUserPrincipal().getName());
        assertEquals(pass, new String(credentials.getPassword()));
    }

    @Test
    public void testProxyWithoutCredentials() throws AxisFault {
        final OMElement configurationElement = new AxiomElementImpl();

        final String hostname = "http://host";
        final OMElement host = new AxiomElementImpl();
        host.setText(hostname);
        host.setLocalName(HTTPTransportConstants.PROXY_HOST_ELEMENT);
        configurationElement.addChild(host);

        final int portNumber = 8080;
        final OMElement port = new AxiomElementImpl();
        port.setText(String.valueOf(portNumber));
        port.setLocalName(HTTPTransportConstants.PROXY_PORT_ELEMENT);
        configurationElement.addChild(port);

        final OMElement element = new AxiomElementImpl();
        element.addChild(configurationElement);
        final Parameter param = new Parameter();
        param.setParameterElement(element);
        param.setName(HTTPTransportConstants.ATTR_PROXY);
        final AxisConfiguration configuration = new AxisConfiguration();
        configuration.addParameter(param);
        final MessageContext messageContext = new MessageContext();
        final ConfigurationContext configurationContext = new ConfigurationContext(configuration);
        messageContext.setConfigurationContext(configurationContext);
        final RequestConfig.Builder builder = RequestConfig.custom();

        final HttpClientContext clientContext = new HttpClientContext();
        HTTPProxyConfigurator.configure(messageContext, builder, clientContext);
        final RequestConfig config = builder.build();
        final HttpHost proxyHost = config.getProxy();
        assertNotNull(proxyHost);
        assertEquals(hostname, proxyHost.getHostName());
        assertEquals(portNumber, proxyHost.getPort());
	System.out.println("testProxyWithoutCredentials() passed");
    }
}

