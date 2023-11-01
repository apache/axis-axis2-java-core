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

package org.apache.axis2.transport.http.impl.httpclient4;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.kernel.http.HTTPConstants;
import org.apache.axis2.transport.http.HTTPTransportConstants;
import org.apache.axis2.transport.http.HttpTransportProperties;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.NTCredentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;

import javax.xml.namespace.QName;
import java.net.URL;
import java.util.StringTokenizer;


public class HTTPProxyConfigurator {

    private static Log log = LogFactory.getLog(HTTPProxyConfigurator.class);

    /**
     * Configure HTTP Proxy settings of httpcomponents HostConfiguration.
     * Proxy settings can be get from axis2.xml, Java proxy settings or can be
     * override through property in message context.
     * <p/>
     * HTTP Proxy setting element format: <parameter name="Proxy">
     * <Configuration> <ProxyHost>example.org</ProxyHost>
     * <ProxyPort>3128</ProxyPort> <ProxyUser>EXAMPLE/John</ProxyUser>
     * <ProxyPassword>password</ProxyPassword> <Configuration> <parameter>
     *
     * @param messageContext
     *            in message context for
     * @param requestConfig
     *            the request configuration to fill in
     * @param clientContext
     *            the HTTP client context
     * @throws org.apache.axis2.AxisFault
     *             if Proxy settings are invalid
     */
    public static void configure(MessageContext messageContext, RequestConfig.Builder requestConfig, HttpClientContext clientContext)
            throws AxisFault {

        Credentials proxyCredentials = null;
        String proxyHost = null;
        String nonProxyHosts = null;
        Integer proxyPort = -1;
        String proxyUser = null;
        String proxyPassword = null;

        // Getting configuration values from Axis2.xml
        Parameter proxySettingsFromAxisConfig = messageContext.getConfigurationContext()
                .getAxisConfiguration().getParameter(HTTPTransportConstants.ATTR_PROXY);
        if (proxySettingsFromAxisConfig != null) {
            OMElement proxyConfiguration =
                    getProxyConfigurationElement(proxySettingsFromAxisConfig);
            proxyHost = getProxyHost(proxyConfiguration);
            proxyPort = getProxyPort(proxyConfiguration);
            proxyUser = getProxyUser(proxyConfiguration);
            proxyPassword = getProxyPassword(proxyConfiguration);
            if (proxyUser != null) {
                if (proxyPassword == null) {
                    proxyPassword = "";
                }

                proxyCredentials = new UsernamePasswordCredentials(proxyUser, proxyPassword);

                int proxyUserDomainIndex = proxyUser.indexOf("\\");
                if (proxyUserDomainIndex > 0) {
                    String domain = proxyUser.substring(0, proxyUserDomainIndex);
                    if (proxyUser.length() > proxyUserDomainIndex + 1) {
                        String user = proxyUser.substring(proxyUserDomainIndex + 1);
                        proxyCredentials = new NTCredentials(user, proxyPassword, proxyHost,
                                                             domain);
                    }
                }
            }
        }

        // If there is runtime proxy settings, these settings will override
        // settings from axis2.xml
        HttpTransportProperties.ProxyProperties proxyProperties =
                (HttpTransportProperties.ProxyProperties) messageContext
                .getProperty(HTTPConstants.PROXY);
        if (proxyProperties != null) {
            String proxyHostProp = proxyProperties.getProxyHostName();
            if (proxyHostProp == null || proxyHostProp.length() <= 0) {
                throw new AxisFault("HTTP Proxy host is not available. Host is a MUST parameter");
            } else {
                proxyHost = proxyHostProp;
            }
            proxyPort = proxyProperties.getProxyPort();

            // Overriding credentials
            String userName = proxyProperties.getUserName();
            String password = proxyProperties.getPassWord();
            String domain = proxyProperties.getDomain();

            if (userName != null && password != null && domain != null) {
                proxyCredentials = new NTCredentials(userName, password, proxyHost, domain);
            } else if (userName != null && domain == null) {
                proxyCredentials = new UsernamePasswordCredentials(userName, password);
            }

        }

        // Overriding proxy settings if proxy is available from JVM settings
        String host = System.getProperty(HTTPTransportConstants.HTTP_PROXY_HOST);
        if (host != null) {
            proxyHost = host;
        }

        String port = System.getProperty(HTTPTransportConstants.HTTP_PROXY_PORT);
        if (port != null && !port.isEmpty()) {
            proxyPort = Integer.parseInt(port);
        }

        if (proxyCredentials != null) {
            // TODO : Set preemptive authentication, but its not recommended in HC 4
            requestConfig.setAuthenticationEnabled(true);
            CredentialsProvider credsProvider = clientContext.getCredentialsProvider();
            if (credsProvider == null) {
                credsProvider = new BasicCredentialsProvider();
                clientContext.setCredentialsProvider(credsProvider);
            }
            credsProvider.setCredentials(AuthScope.ANY, proxyCredentials);
        }
        if (proxyHost != null && proxyPort > 0) {
            HttpHost proxy = new HttpHost(proxyHost, proxyPort);
            requestConfig.setProxy(proxy);
        }
    }

    private static OMElement getProxyConfigurationElement(Parameter proxySettingsFromAxisConfig)
            throws AxisFault {
        OMElement proxyConfigurationElement = proxySettingsFromAxisConfig.getParameterElement()
                .getFirstElement();
        if (proxyConfigurationElement == null) {
            log.error(HTTPTransportConstants.PROXY_CONFIGURATION_NOT_FOUND);
            throw new AxisFault(HTTPTransportConstants.PROXY_CONFIGURATION_NOT_FOUND);
        }
        return proxyConfigurationElement;
    }

    private static String getProxyHost(OMElement proxyConfiguration) throws AxisFault {
        OMElement proxyHostElement = proxyConfiguration.getFirstChildWithName(new QName(
                HTTPTransportConstants.PROXY_HOST_ELEMENT));
        if (proxyHostElement == null) {
            log.error(HTTPTransportConstants.PROXY_HOST_ELEMENT_NOT_FOUND);
            throw new AxisFault(HTTPTransportConstants.PROXY_HOST_ELEMENT_NOT_FOUND);
        }
        String proxyHost = proxyHostElement.getText();
        if (proxyHost == null) {
            log.error(HTTPTransportConstants.PROXY_HOST_ELEMENT_WITH_EMPTY_VALUE);
            throw new AxisFault(HTTPTransportConstants.PROXY_HOST_ELEMENT_WITH_EMPTY_VALUE);
        }
        return proxyHost;
    }

    private static Integer getProxyPort(OMElement proxyConfiguration) throws AxisFault {
        OMElement proxyPortElement = proxyConfiguration.getFirstChildWithName(new QName(
                HTTPTransportConstants.PROXY_PORT_ELEMENT));
        if (proxyPortElement == null) {
            log.error(HTTPTransportConstants.PROXY_PORT_ELEMENT_NOT_FOUND);
            throw new AxisFault(HTTPTransportConstants.PROXY_PORT_ELEMENT_NOT_FOUND);
        }
        String proxyPort = proxyPortElement.getText();
        if (proxyPort == null) {
            log.error(HTTPTransportConstants.PROXY_PORT_ELEMENT_WITH_EMPTY_VALUE);
            throw new AxisFault(HTTPTransportConstants.PROXY_PORT_ELEMENT_WITH_EMPTY_VALUE);
        }
        return Integer.parseInt(proxyPort);
    }

    private static String getProxyUser(OMElement proxyConfiguration) {
        OMElement proxyUserElement = proxyConfiguration.getFirstChildWithName(new QName(
                HTTPTransportConstants.PROXY_USER_ELEMENT));
        if (proxyUserElement == null) {
            return null;
        }
        String proxyUser = proxyUserElement.getText();
        if (proxyUser == null) {
            log.warn("Empty user name element in HTTP Proxy settings.");
            return null;
        }

        return proxyUser;
    }

    private static String getProxyPassword(OMElement proxyConfiguration) {
        OMElement proxyPasswordElement = proxyConfiguration.getFirstChildWithName(new QName(
                HTTPTransportConstants.PROXY_PASSWORD_ELEMENT));
        if (proxyPasswordElement == null) {
            return null;
        }
        String proxyUser = proxyPasswordElement.getText();
        if (proxyUser == null) {
            log.warn("Empty user name element in HTTP Proxy settings.");
            return null;
        }

        return proxyUser;
    }

    /**
     * Check whether http proxy is configured or active. This is not a deep
     * check.
     *
     * @param messageContext
     *            in message context
     * @param targetURL
     *            URL of the edpoint which we are sending the request
     * @return true if proxy is enabled, false otherwise
     */
    public static boolean isProxyEnabled(MessageContext messageContext, URL targetURL) {
        boolean proxyEnabled = false;

        Parameter param = messageContext.getConfigurationContext().getAxisConfiguration()
                .getParameter(HTTPTransportConstants.ATTR_PROXY);

        // If configuration is over ridden
        Object obj = messageContext.getProperty(HTTPConstants.PROXY);

        // From Java Networking Properties
        String sp = System.getProperty(HTTPTransportConstants.HTTP_PROXY_HOST);

        if (param != null || obj != null || sp != null) {
            proxyEnabled = true;
        }

        boolean isNonProxyHost = validateNonProxyHosts(targetURL.getHost());

        return proxyEnabled && !isNonProxyHost;
    }

    /**
     * Validates for names that shouldn't be listered as proxies. The
     * http.nonProxyHosts can be set to specify the hosts which should be
     * connected to directly (not through the proxy server). The value of the
     * http.nonProxyHosts property can be a list of hosts, each separated by a
     * |; it can also take a regular expression for matches; for example:
     * *.sfbay.sun.com would match any fully qualified hostname in the sfbay
     * domain.
     * <p/>
     * For more information refer to :
     * http://java.sun.com/features/2002/11/hilevel_network.html
     * <p/>
     * false : validation fail : User can use the proxy true : validation pass ;
     * User can't use the proxy
     *
     * @return boolean
     */
    private static boolean validateNonProxyHosts(String host) {
        // From system property http.nonProxyHosts
        String nonProxyHosts = System.getProperty(HTTPTransportConstants.HTTP_NON_PROXY_HOSTS);
        return isHostInNonProxyList(host, nonProxyHosts);
    }

    /**
     * Check if the specified host is in the list of non proxy hosts.
     *
     * @param host
     *            host name
     * @param nonProxyHosts
     *            string containing the list of non proxy hosts
     * @return true/false
     */
    public static boolean isHostInNonProxyList(String host, String nonProxyHosts) {
        if ((nonProxyHosts == null) || (host == null)) {
            return false;
        }

        /*
         * The http.nonProxyHosts system property is a list enclosed in double
         * quotes with items separated by a vertical bar.
         */
        StringTokenizer tokenizer = new StringTokenizer(nonProxyHosts, "|\"");

        while (tokenizer.hasMoreTokens()) {
            String pattern = tokenizer.nextToken();
            if (match(pattern, host, false)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Matches a string against a pattern. The pattern contains two special
     * characters: '*' which means zero or more characters,
     *
     * @param pattern
     *            the (non-null) pattern to match against
     * @param str
     *            the (non-null) string that must be matched against the pattern
     * @param isCaseSensitive
     * @return <code>true</code> when the string matches against the pattern,
     *         <code>false</code> otherwise.
     */
    private static boolean match(String pattern, String str, boolean isCaseSensitive) {

        char[] patArr = pattern.toCharArray();
        char[] strArr = str.toCharArray();
        int patIdxStart = 0;
        int patIdxEnd = patArr.length - 1;
        int strIdxStart = 0;
        int strIdxEnd = strArr.length - 1;
        char ch;
        boolean containsStar = false;

        for (int i = 0; i < patArr.length; i++) {
            if (patArr[i] == '*') {
                containsStar = true;
                break;
            }
        }
        if (!containsStar) {

            // No '*'s, so we make a shortcut
            if (patIdxEnd != strIdxEnd) {
                return false; // Pattern and string do not have the same size
            }
            for (int i = 0; i <= patIdxEnd; i++) {
                ch = patArr[i];
                if (isCaseSensitive && (ch != strArr[i])) {
                    return false; // Character mismatch
                }
                if (!isCaseSensitive
                        && (Character.toUpperCase(ch) != Character.toUpperCase(strArr[i]))) {
                    return false; // Character mismatch
                }
            }
            return true; // String matches against pattern
        }
        if (patIdxEnd == 0) {
            return true; // Pattern contains only '*', which matches anything
        }

        // Process characters before first star
        while ((ch = patArr[patIdxStart]) != '*' && (strIdxStart <= strIdxEnd)) {
            if (isCaseSensitive && (ch != strArr[strIdxStart])) {
                return false; // Character mismatch
            }
            if (!isCaseSensitive
                    && (Character.toUpperCase(ch) != Character.toUpperCase(strArr[strIdxStart]))) {
                return false; // Character mismatch
            }
            patIdxStart++;
            strIdxStart++;
        }
        if (strIdxStart > strIdxEnd) {

            // All characters in the string are used. Check if only '*'s are
            // left in the pattern. If so, we succeeded. Otherwise failure.
            for (int i = patIdxStart; i <= patIdxEnd; i++) {
                if (patArr[i] != '*') {
                    return false;
                }
            }
            return true;
        }

        // Process characters after last star
        while ((ch = patArr[patIdxEnd]) != '*' && (strIdxStart <= strIdxEnd)) {
            if (isCaseSensitive && (ch != strArr[strIdxEnd])) {
                return false; // Character mismatch
            }
            if (!isCaseSensitive
                    && (Character.toUpperCase(ch) != Character.toUpperCase(strArr[strIdxEnd]))) {
                return false; // Character mismatch
            }
            patIdxEnd--;
            strIdxEnd--;
        }
        if (strIdxStart > strIdxEnd) {

            // All characters in the string are used. Check if only '*'s are
            // left in the pattern. If so, we succeeded. Otherwise failure.
            for (int i = patIdxStart; i <= patIdxEnd; i++) {
                if (patArr[i] != '*') {
                    return false;
                }
            }
            return true;
        }

        // process pattern between stars. padIdxStart and patIdxEnd point
        // always to a '*'.
        while ((patIdxStart != patIdxEnd) && (strIdxStart <= strIdxEnd)) {
            int patIdxTmp = -1;

            for (int i = patIdxStart + 1; i <= patIdxEnd; i++) {
                if (patArr[i] == '*') {
                    patIdxTmp = i;
                    break;
                }
            }
            if (patIdxTmp == patIdxStart + 1) {

                // Two stars next to each other, skip the first one.
                patIdxStart++;
                continue;
            }

            // Find the pattern between padIdxStart & padIdxTmp in str between
            // strIdxStart & strIdxEnd
            int patLength = (patIdxTmp - patIdxStart - 1);
            int strLength = (strIdxEnd - strIdxStart + 1);
            int foundIdx = -1;

            strLoop: for (int i = 0; i <= strLength - patLength; i++) {
                for (int j = 0; j < patLength; j++) {
                    ch = patArr[patIdxStart + j + 1];
                    if (isCaseSensitive && (ch != strArr[strIdxStart + i + j])) {
                        continue strLoop;
                    }
                    if (!isCaseSensitive
                            && (Character.toUpperCase(ch) != Character
                                    .toUpperCase(strArr[strIdxStart + i + j]))) {
                        continue strLoop;
                    }
                }
                foundIdx = strIdxStart + i;
                break;
            }
            if (foundIdx == -1) {
                return false;
            }
            patIdxStart = patIdxTmp;
            strIdxStart = foundIdx + patLength;
        }

        // All characters in the string are used. Check if only '*'s are left
        // in the pattern. If so, we succeeded. Otherwise failure.
        for (int i = patIdxStart; i <= patIdxEnd; i++) {
            if (patArr[i] != '*') {
                return false;
            }
        }
        return true;
    }

}
