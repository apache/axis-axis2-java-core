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

import org.apache.commons.httpclient.auth.*;
import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.NTCredentials;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
/*
 * 
 */

public class HTTPCredentialProvider implements CredentialsProvider {

    private static Log log = LogFactory.getLog(HTTPCredentialProvider.class);

    private String host;
    private String realm;
    private String username;
    private String password;


    public HTTPCredentialProvider(String host, String realm, String username, String password) {
        this.host = host;
        this.realm = realm;
        this.username = username;
        this.password = password;

    }

    public Credentials getCredentials(AuthScheme authscheme, String string, int i, boolean b)
            throws CredentialsNotAvailableException {
        if (authscheme == null) {
            return null;
        }
        try {
            if (authscheme instanceof NTLMScheme) {
                log.debug("NTLM Authentication authentication");
                if (username == null || password == null || host == null || realm == null) {
                    throw new CredentialsNotAvailableException(
                            "user or password or host or realm cannot be Null");
                }
                return new NTCredentials(username, password, host, realm);
            } else if (authscheme instanceof RFC2617Scheme) {
                log.debug(host + " : " + " requires authentication with the realm '"
                          + authscheme.getRealm() + "'");
                return new UsernamePasswordCredentials(username, password);
            } else {
                throw new CredentialsNotAvailableException("Unsupported authentication scheme: " +
                                                           authscheme.getSchemeName());
            }
        } catch (IOException e) {
            throw new CredentialsNotAvailableException(e.getMessage(), e);
        }

    }
}
