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
package userguide.springboot.security.webservices;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.GenericFilterBean;

import javax.naming.InvalidNameException;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;
import javax.security.auth.x500.X500Principal;
import java.io.IOException;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.List;

/**
 * Authenticates requests that arrive over a Tomcat mTLS connector.
 *
 * <p>Tomcat enforces {@code certificateVerification="required"} at the TLS
 * handshake — only clients presenting a certificate signed by the configured
 * CA reach this filter. This filter reads the already-verified certificate
 * from the Servlet request attribute and creates an authenticated
 * {@link SecurityContextHolder} entry so downstream filters and the
 * {@code FilterSecurityInterceptor} see an authenticated principal.
 *
 * <p>The client CN (e.g. {@code axis2-mcp-bridge}) becomes the principal
 * name. The single granted authority is {@code ROLE_X509_CLIENT}.
 */
public class X509AuthenticationFilter extends GenericFilterBean {

    private static final Logger logger = LogManager.getLogger(X509AuthenticationFilter.class);

    /** Servlet attribute name Tomcat uses to expose the verified client cert chain. */
    private static final String CERT_ATTRIBUTE = "jakarta.servlet.request.X509Certificate";

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
                         FilterChain chain) throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) servletRequest;

        X509Certificate[] certs = (X509Certificate[]) request.getAttribute(CERT_ATTRIBUTE);

        if (certs != null && certs.length > 0) {
            X509Certificate clientCert = certs[0];
            String cn = extractCN(clientCert.getSubjectX500Principal());

            List<GrantedAuthority> authorities =
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_X509_CLIENT"));

            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(cn, clientCert, authorities);

            SecurityContextHolder.getContext().setAuthentication(auth);
            request.setAttribute("x509_cn", cn);

            logger.debug("X509AuthenticationFilter: authenticated CN=" + cn
                    + " on port " + request.getLocalPort());
        } else {
            // Tomcat certificateVerification=required means this should not happen on 8443,
            // but log it defensively in case the connector config changes.
            logger.warn("X509AuthenticationFilter: no client cert on port "
                    + request.getLocalPort() + " — no authentication set");
        }

        chain.doFilter(servletRequest, servletResponse);
    }

    /**
     * Extracts the CN value from an {@link X500Principal}.
     *
     * <p>Uses {@link LdapName} to parse the RFC 2253 DN, which correctly
     * handles escaped commas in RDN values (e.g. {@code O="Example, Inc."}).
     * Falls back to the full DN string if parsing fails or no CN attribute
     * is present.
     */
    private String extractCN(X500Principal principal) {
        try {
            LdapName ldapDN = new LdapName(principal.getName());
            // Iterate in reverse — CN is typically the most-specific (last) RDN
            for (int i = ldapDN.size() - 1; i >= 0; i--) {
                Rdn rdn = ldapDN.getRdn(i);
                if ("CN".equalsIgnoreCase(rdn.getType())) {
                    return rdn.getValue().toString();
                }
            }
        } catch (InvalidNameException e) {
            logger.warn("X509AuthenticationFilter: could not parse DN, using full DN: "
                    + principal.getName());
        }
        return principal.getName();
    }
}
