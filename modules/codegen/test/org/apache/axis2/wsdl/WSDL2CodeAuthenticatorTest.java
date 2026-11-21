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

package org.apache.axis2.wsdl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.net.Authenticator;
import java.net.Authenticator.RequestorType;
import java.net.PasswordAuthentication;
import java.net.URL;

import org.apache.axis2.util.CommandLineOptionParser;
import org.junit.jupiter.api.Test;

/**
 * Credentials given to WSDL2Code answer only the party they were given for, not
 * every server a WSDL import happens to name.
 */
public class WSDL2CodeAuthenticatorTest {

    private static Authenticator authenticatorFor(String... args) {
        return WSDL2Code.createAuthenticator(new CommandLineOptionParser(args));
    }

    private static PasswordAuthentication challenge(Authenticator authenticator, String host,
            int port, RequestorType type) throws Exception {
        return authenticator.requestPasswordAuthenticationInstance(host, null, port, "http",
                "realm", "basic", new URL("http://" + host + ":" + port + "/ext.xsd"), type);
    }

    @Test
    public void proxyCredentialsAnswerTheProxyOnly() throws Exception {
        Authenticator authenticator = authenticatorFor("-uri", "http://wsdl.example/main.wsdl",
                "-http-proxy-user", "proxyuser", "-http-proxy-password", "proxypass");
        assertNotNull(authenticator);

        PasswordAuthentication forProxy =
                challenge(authenticator, "proxy.example", 3128, RequestorType.PROXY);
        assertNotNull(forProxy);
        assertEquals("proxyuser", forProxy.getUserName());
        assertEquals("proxypass", new String(forProxy.getPassword()));

        assertNull(challenge(authenticator, "import.example", 80, RequestorType.SERVER),
                "an import server's 401 was answered with the proxy credentials");
        assertNull(challenge(authenticator, "wsdl.example", 80, RequestorType.SERVER));
    }

    @Test
    public void urlCredentialsAnswerTheWsdlHostOnly() throws Exception {
        Authenticator authenticator =
                authenticatorFor("-uri", "http://wsdluser:wsdlpass@wsdl.example:8080/main.wsdl");
        assertNotNull(authenticator);

        PasswordAuthentication forOrigin =
                challenge(authenticator, "WSDL.example", 8080, RequestorType.SERVER);
        assertNotNull(forOrigin);
        assertEquals("wsdluser", forOrigin.getUserName());
        assertEquals("wsdlpass", new String(forOrigin.getPassword()));

        assertNull(challenge(authenticator, "import.example", 8080, RequestorType.SERVER),
                "an import server's 401 was answered with the WSDL server's credentials");
        assertNull(challenge(authenticator, "wsdl.example", 9090, RequestorType.SERVER));
        assertNull(challenge(authenticator, "wsdl.example", 8080, RequestorType.PROXY));
    }

    @Test
    public void urlCredentialsUseTheDefaultPort() throws Exception {
        Authenticator authenticator =
                authenticatorFor("-uri", "http://wsdluser:wsdlpass@wsdl.example/main.wsdl");
        assertNotNull(challenge(authenticator, "wsdl.example", 80, RequestorType.SERVER));
    }

    @Test
    public void noCredentialsMeansNoAuthenticator() {
        assertNull(authenticatorFor("-uri", "http://wsdl.example/main.wsdl"));
    }
}
