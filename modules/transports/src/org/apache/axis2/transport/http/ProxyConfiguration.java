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
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.Parameter;
import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NTCredentials;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;

import javax.xml.namespace.QName;
import java.net.URL;
import java.util.StringTokenizer;


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
    public static boolean validateNonProxyHosts(String host) {
        //From system property http.nonProxyHosts
        String nonProxyHosts = System.getProperty(HTTP_NON_PROXY_HOSTS);
        return isHostInNonProxyList(host, nonProxyHosts);
    }
    
    /**
     * Check if the specified host is in the list of non proxy hosts.
     *
     * @param host host name
     * @param nonProxyHosts string containing the list of non proxy hosts
     *
     * @return true/false
     */
    public static boolean isHostInNonProxyList(String host, String nonProxyHosts) {
        if ((nonProxyHosts == null) || (host == null)) {
            return false;
        }

        /*
         * The http.nonProxyHosts system property is a list enclosed in
         * double quotes with items separated by a vertical bar.
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
     * characters:
     * '*' which means zero or more characters,
     *
     * @param pattern the (non-null) pattern to match against
     * @param str     the (non-null) string that must be matched against the
     *                pattern
     * @param isCaseSensitive
     *
     * @return <code>true</code> when the string matches against the pattern,
     *         <code>false</code> otherwise.
     */
    protected static boolean match(String pattern, String str,
                                   boolean isCaseSensitive) {

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
                return false;        // Pattern and string do not have the same size
            }
            for (int i = 0; i <= patIdxEnd; i++) {
                ch = patArr[i];
                if (isCaseSensitive && (ch != strArr[i])) {
                    return false;    // Character mismatch
                }
                if (!isCaseSensitive
                        && (Character.toUpperCase(ch)
                        != Character.toUpperCase(strArr[i]))) {
                    return false;    // Character mismatch
                }
            }
            return true;             // String matches against pattern
        }
        if (patIdxEnd == 0) {
            return true;    // Pattern contains only '*', which matches anything
        }

        // Process characters before first star
        while ((ch = patArr[patIdxStart]) != '*'
                && (strIdxStart <= strIdxEnd)) {
            if (isCaseSensitive && (ch != strArr[strIdxStart])) {
                return false;    // Character mismatch
            }
            if (!isCaseSensitive
                    && (Character.toUpperCase(ch)
                    != Character.toUpperCase(strArr[strIdxStart]))) {
                return false;    // Character mismatch
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
                return false;    // Character mismatch
            }
            if (!isCaseSensitive
                    && (Character.toUpperCase(ch)
                    != Character.toUpperCase(strArr[strIdxEnd]))) {
                return false;    // Character mismatch
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

            strLoop:
            for (int i = 0; i <= strLength - patLength; i++) {
                for (int j = 0; j < patLength; j++) {
                    ch = patArr[patIdxStart + j + 1];
                    if (isCaseSensitive
                            && (ch != strArr[strIdxStart + i + j])) {
                        continue strLoop;
                    }
                    if (!isCaseSensitive && (Character
                            .toUpperCase(ch) != Character
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
