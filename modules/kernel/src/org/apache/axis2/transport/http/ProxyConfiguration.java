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
package org.apache.axis2.transport.http;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.Parameter;
import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.auth.AuthScope;

import javax.xml.namespace.QName;
import java.net.URL;


/**
 * The purpose of this class is to configure the proxy auth regardles of the protocol.
 * Proxy will be set only for HTTP connection
 */

public class ProxyConfiguration {

    protected String proxyHost;
    protected String nonProxyHosts;
    protected int proxyPort = -1; //If port is not set, default is set to -1
    protected String proxyUser;
    protected String proxyPassword;

    protected static final String HTTP_PROXY_HOST = "http.proxyHost";
    protected static final String HTTP_PROXY_PORT = "http.proxyPort";
    protected static final String HTTP_NON_PROXY_HOSTS = "http.nonProxyHosts";

    protected static final String ATTR_PROXY = "Proxy";
    protected static final String PROXY_HOST_ELEMENT = "ProxyHost";
    protected static final String PROXY_PORT_ELEMENT = "ProxyPort";
    protected static final String PROXY_USER_ELEMENT = "ProxyUser";
    protected static final String PROXY_PASSWORD_ELEMENT = "ProxyPassword";

    public void configure(MessageContext messageContext,
                          HttpClient httpClient,
                          HostConfiguration config) throws AxisFault {

        //        <parameter name="Proxy">
        //              <Configuration>
        //                     <ProxyHost>example.org</ProxyHost>
        //                     <ProxyPort>5678</ProxyPort>
        //                     <ProxyUser>EXAMPLE\saminda</ProxyUser>
        //                     <ProxyPassword>ppp</ProxyPassword>
        //              </Configuration>
        //        </parameter>
        Credentials proxyCred = null;

        //Getting configuration values from Axis2.xml
        Parameter param = messageContext.getConfigurationContext().getAxisConfiguration()
                .getParameter(ATTR_PROXY);

        if (param != null) {
            OMElement configurationEle = param.getParameterElement().getFirstElement();
            if (configurationEle == null) {
                throw new AxisFault(
                        ProxyConfiguration.class.getName() + " Configuration element is missing");
            }

            OMElement proxyHostEle =
                    configurationEle.getFirstChildWithName(new QName(PROXY_HOST_ELEMENT));
            OMElement proxyPortEle =
                    configurationEle.getFirstChildWithName(new QName(PROXY_PORT_ELEMENT));
            OMElement proxyUserEle =
                    configurationEle.getFirstChildWithName(new QName(PROXY_USER_ELEMENT));
            OMElement proxyPasswordEle =
                    configurationEle.getFirstChildWithName(new QName(PROXY_PASSWORD_ELEMENT));

            if (proxyHostEle == null) {
                throw new AxisFault(
                        ProxyConfiguration.class.getName() + " ProxyHost element is missing");
            }
            String text = proxyHostEle.getText();
            if (text == null) {
                throw new AxisFault(
                        ProxyConfiguration.class.getName() + " ProxyHost's value is missing");
            }

            this.setProxyHost(text);

            if (proxyPortEle != null) {
                this.setProxyPort(Integer.parseInt(proxyPortEle.getText()));
            }

            if (proxyUserEle != null) {
                this.setProxyUser(proxyUserEle.getText());
            }

            if (proxyPasswordEle != null) {
                this.setProxyPassword(proxyPasswordEle.getText());
            }

            if (this.getProxyUser() == null && this.getProxyUser() == null) {
                proxyCred = new UsernamePasswordCredentials("", "");
            } else {
                proxyCred =
                        new UsernamePasswordCredentials(this.getProxyUser(),
                                                        this.getProxyPassword());
            }

            // if the username is in the form "DOMAIN\\user"
            // then use NTCredentials instead.
            if (this.getProxyUser() != null) {
                int domainIndex = this.getProxyUser().indexOf("\\");
                if (domainIndex > 0) {
                    String domain = this.getProxyUser().substring(0, domainIndex);
                    if (this.getProxyUser().length() > domainIndex + 1) {
                        String user = this.getProxyUser().substring(domainIndex + 1);
                        proxyCred = new NTCredentials(user,
                                                      this.getProxyPassword(),
                                                      this.getProxyHost(),
                                                      domain);
                    }
                }
            }
        }

        // Overide the property setting in runtime.
        HttpTransportProperties.ProxyProperties proxyProperties =
                (HttpTransportProperties.ProxyProperties) messageContext
                        .getProperty(HTTPConstants.PROXY);

        if (proxyProperties != null) {
            String host = proxyProperties.getProxyHostName();
            if (host == null || host.length() == 0) {
                throw new AxisFault(ProxyConfiguration.class.getName() +
                                    " Proxy host is not available. Host is a MUST parameter");

            } else {
                this.setProxyHost(host);
            }


            this.setProxyPort(proxyProperties.getProxyPort());

            //Setting credentials

            String userName = proxyProperties.getUserName();
            String password = proxyProperties.getPassWord();
            String domain = proxyProperties.getDomain();

            if (userName == null && password == null) {
                proxyCred = new UsernamePasswordCredentials("", "");
            } else {
                proxyCred = new UsernamePasswordCredentials(userName, password);
            }

            if (userName != null && password != null && domain != null) {
                proxyCred = new NTCredentials(userName, password, host, domain);
            }

        }

        //Using Java Networking Properties

        String host = System.getProperty(HTTP_PROXY_HOST);
        if (host != null) {
            this.setProxyHost(host);
            proxyCred = new UsernamePasswordCredentials("","");
        }

        String port = System.getProperty(HTTP_PROXY_PORT);

        if (port != null) {
            this.setProxyPort(Integer.parseInt(port));
        }

        if (proxyCred == null) {
            throw new AxisFault(ProxyConfiguration.class.getName() +
                                    " Minimum proxy credentials are not set");
        }
        httpClient.getState().setProxyCredentials(AuthScope.ANY, proxyCred);
        config.setProxy(this.getProxyHost(), this.getProxyPort());
    }

    /**
     * Check first if the proxy is configured or active.
     * If yes this will return true. This is not a deep check
     *
     * @param messageContext
     * @return boolean
     */

    public static boolean isProxyEnabled(MessageContext messageContext, URL targetURL)
            throws AxisFault {

        boolean state = false;


        Parameter param = messageContext.getConfigurationContext().getAxisConfiguration()
                .getParameter(ATTR_PROXY);

        //If configuration is over ridden
        Object obj = messageContext.getProperty(HTTPConstants.PROXY);

        //From Java Networking Properties
        String sp = System.getProperty(HTTP_PROXY_HOST);

        if (param != null || obj != null || sp != null) {
            state = true;
        }

        boolean isNonProxyHost = validateNonProxyHosts(targetURL.getHost());

        return state && !isNonProxyHost;

    }

    /**
     * Validates for names that shouldn't be listered as proxies.
     * The http.nonProxyHosts can be set to specify the hosts which should be
     * connected to directly (not through the proxy server).
     * The value of the http.nonProxyHosts property can be a list of hosts,
     * each separated by a |; it can also take a regular expression for matches;
     * for example: *.sfbay.sun.com would match any fully qualified hostname in the sfbay domain.
     *
     * For more information refer to : http://java.sun.com/features/2002/11/hilevel_network.html
     *
     * false : validation fail : User can use the proxy
     * true : validation pass ; User can't use the proxy
     *
     * @return boolean
     */
    private static boolean validateNonProxyHosts(String targetHost) {

        //From system property http.nonProxyHosts
        String nonProxyHosts = System.getProperty(HTTP_NON_PROXY_HOSTS);

        if (nonProxyHosts == null) {
            return false;
        }

        String[] nonProxyHostsArray = nonProxyHosts.split("\\|");

        if (nonProxyHostsArray.length == 1) {
            return targetHost.matches(nonProxyHosts);
        } else {
            boolean pass = false;
            for (int i = 0; i < nonProxyHostsArray.length; i++) {
                String a = nonProxyHostsArray[i];
                if (targetHost.matches(a)) {
                    pass = true;
                    break;
                }
            }
            return pass;
        }
    }

    /**
     * Retrun proxy host
     *
     * @return String
     */
    public String getProxyHost() {
        return proxyHost;
    }

    /**
     * set proxy host
     *
     * @param proxyHost
     */

    public void setProxyHost(String proxyHost) {
        this.proxyHost = proxyHost;
    }

    /**
     * retrun proxy port
     *
     * @return String
     */
    public int getProxyPort() {
        return proxyPort;
    }

    /**
     * set proxy port
     *
     * @param proxyPort
     */
    public void setProxyPort(int proxyPort) {
        this.proxyPort = proxyPort;
    }

    /**
     * return proxy user. Proxy user can be user/domain or user
     *
     * @return String
     */
    public String getProxyUser() {
        return proxyUser;
    }

    /**
     * get proxy user
     *
     * @param proxyUser
     */
    public void setProxyUser(String proxyUser) {
        this.proxyUser = proxyUser;
    }

    /**
     * set password
     *
     * @return String
     */
    public String getProxyPassword() {
        return proxyPassword;
    }

    /**
     * get password
     *
     * @param proxyPassword
     */
    public void setProxyPassword(String proxyPassword) {
        this.proxyPassword = proxyPassword;
    }


}
