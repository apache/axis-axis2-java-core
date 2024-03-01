package org.apache.axis2.transport.http.impl.httpclient4;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.llom.AxiomElementImpl;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.transport.http.HTTPTransportConstants;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.protocol.HttpClientContext;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class TestHTTPProxyConfigurator {

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
        final Credentials credentials = provider.getCredentials(AuthScope.ANY);
        assertNotNull(credentials);
        assertEquals(user, credentials.getUserPrincipal().getName());
        assertEquals(pass, credentials.getPassword());
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
    }
}
