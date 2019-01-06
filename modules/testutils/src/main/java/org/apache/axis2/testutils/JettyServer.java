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

import java.io.File;
import java.io.FileOutputStream;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.Random;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.server.ssl.SslSelectChannelConnector;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.webapp.WebAppContext;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.transport.http.AxisServlet;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

/**
 * Support for running an embedded Jetty server
 */
public class JettyServer extends AbstractAxis2Server {
    /**
     * The alias of the certificate to configure for Jetty's ssl context factory: {@value}
     */
    private static final String CERT_ALIAS = "server";
    
    /**
     * Webapp resource base directory to use: {@value}
     */
    private static final String WEBAPP_DIR = "target" + File.separator + "webapp";
    
    private static final Log log = LogFactory.getLog(JettyServer.class);
    
    private final boolean secure;
    private File keyStoreFile;
    private SSLContext clientSslContext;
    private SslContextFactory serverSslContextFactory;
    private Server server;
    
    /**
     * Constructor.
     * 
     * @param repositoryPath
     *            The path to the Axis2 repository to use. Must not be null or empty.
     * @param secure
     *            Whether to enable HTTPS.
     */
    public JettyServer(String repositoryPath, boolean secure, AxisServiceFactory... axisServiceFactories) {
        super(repositoryPath, axisServiceFactories);
        this.secure = secure;
    }
    
    private String generatePassword(Random random) {
        char[] password = new char[8];
        for (int i=0; i<password.length; i++) {
            password[i] = (char)('0' + random.nextInt(10));
        }
        return new String(password);
    }
    
    private void writeKeyStore(KeyStore keyStore, File file, String password) throws Exception {
        FileOutputStream out = new FileOutputStream(file);
        try {
            keyStore.store(out, password.toCharArray());
        } finally {
            out.close();
        }
    }
    
    private void generateKeys() throws Exception {
        SecureRandom random = new SecureRandom();
        
        // Generate key pair
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(1024, random);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        PrivateKey privateKey = keyPair.getPrivate();
        PublicKey publicKey = keyPair.getPublic();
        
        // Generate certificate
        X500Name dn = new X500Name("cn=localhost,o=Apache");
        BigInteger serial = BigInteger.valueOf(random.nextInt());
        Date notBefore = new Date();
        Date notAfter = new Date(notBefore.getTime() + 3600000L);
        SubjectPublicKeyInfo subPubKeyInfo =  SubjectPublicKeyInfo.getInstance(publicKey.getEncoded());
        X509v3CertificateBuilder certBuilder = new X509v3CertificateBuilder(dn, serial, notBefore, notAfter, dn, subPubKeyInfo);
        X509CertificateHolder certHolder = certBuilder.build(new JcaContentSignerBuilder("SHA1WithRSA").build(privateKey));
        X509Certificate cert = new JcaX509CertificateConverter().getCertificate(certHolder);
        
        // Build key store
        keyStoreFile = File.createTempFile("keystore", "jks", null);
        String keyStorePassword = generatePassword(random);
        String keyPassword = generatePassword(random);
        KeyStore keyStore = KeyStore.getInstance("JKS");
        keyStore.load(null, null);
        keyStore.setKeyEntry(CERT_ALIAS, privateKey, keyPassword.toCharArray(), new X509Certificate[] { cert });
        writeKeyStore(keyStore, keyStoreFile, keyStorePassword);
        
        // Build trust store
        KeyStore trustStore = KeyStore.getInstance("JKS");
        trustStore.load(null, null);
        trustStore.setCertificateEntry(CERT_ALIAS, cert);
        
        serverSslContextFactory = new SslContextFactory();
        serverSslContextFactory.setKeyStorePath(keyStoreFile.getAbsolutePath());
        serverSslContextFactory.setKeyStorePassword(keyStorePassword);
        serverSslContextFactory.setKeyManagerPassword(keyPassword);
        serverSslContextFactory.setCertAlias(CERT_ALIAS);
        
        clientSslContext = SSLContext.getInstance("TLS");
        TrustManagerFactory tmfactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmfactory.init(trustStore);
        clientSslContext.init(null, tmfactory.getTrustManagers(), null);
    }
    
    @Override
    public SSLContext getClientSSLContext() throws Exception {
        if (secure) {
            if (clientSslContext == null) {
                generateKeys();
            }
            return clientSslContext;
        } else {
            return null;
        }
    }

    @Override
    protected void startServer(final ConfigurationContext configurationContext) throws Throwable {
        server = new Server();
        
        if (!secure) {
            SelectChannelConnector connector = new SelectChannelConnector();
            server.addConnector(connector);
        } else {
            if (serverSslContextFactory == null) {
                generateKeys();
            }
            SslSelectChannelConnector sslConnector = new SslSelectChannelConnector(serverSslContextFactory);
            server.addConnector(sslConnector);
        }
        
        WebAppContext context = new WebAppContext();
        File webappDir = new File(WEBAPP_DIR);
        if (!webappDir.exists() && !webappDir.mkdirs()) {
            log.error("Failed to create Axis2 webapp directory: " + webappDir.getAbsolutePath());
        }
        
        context.setResourceBase(webappDir.getAbsolutePath());
        context.setContextPath("/axis2");
        context.setParentLoaderPriority(true);
        context.setThrowUnavailableOnStartupException(true);
        
        @SuppressWarnings("serial")
        ServletHolder servlet = new ServletHolder(new AxisServlet() {
            @Override
            protected ConfigurationContext initConfigContext(ServletConfig config)
                    throws ServletException {
                return configurationContext;
            }
        });
        
        //load on startup to trigger Axis2 initialization and service deployment
        //this is for backward compatibility with the SimpleHttpServer which initializes Axis2 on startup
        servlet.setInitOrder(0);
        
        context.addServlet(servlet, "/services/*");
        
        server.setHandler(context);
        
        try {
            server.start();
        }
        catch (SecurityException e) {
            if (e.getMessage().equals("class \"javax.servlet.ServletRequestListener\"'s signer information does not match signer information of other classes in the same package")) {
                log.error(
                 "It is likely your test classpath contains multiple different versions of servlet api.\n" +
                 "If you are running this test in an IDE, please configure it to exclude Rampart's core module servlet api dependency.");
                throw e;
            }
        }
        
        log.info("Server started on port " + getPort());
    }
    
    @Override
    protected void stopServer() {
        if (server != null) {
            log.info("Stop called");
            try {
                server.stop();
            } catch (Exception ex) {
                log.error("Failed to stop Jetty server", ex);
            }
            server = null;
        }
        if (keyStoreFile != null) {
            keyStoreFile.delete();
            keyStoreFile = null;
        }
        clientSslContext = null;
        serverSslContextFactory = null;
    }

    @Override
    public boolean isSecure() {
        return secure;
    }

    /**
     * @return Jetty's http connector port. 
     * @throws IllegalStateException If Jetty is not running or the http connector cannot be found.
     */
    @Override
    public int getPort() throws IllegalStateException {
        if (server == null) {
            throw new IllegalStateException("Jetty server is not initialized");
        }
        if (!server.isStarted()) {
            throw new IllegalStateException("Jetty server is not started");
        }
        
        Connector[] connectors = server.getConnectors();
        if (connectors.length == 0) {
            throw new IllegalStateException("Jetty server is not configured with any connectors");
        }
        
        for (Connector connector : connectors) {
            if (connector instanceof SelectChannelConnector) {
                //must be the http connector
                return connector.getLocalPort();
            }
        }
        
        throw new IllegalStateException("Could not find Jetty http connector");
    }

    @Override
    public String getEndpoint(String serviceName) {
        return String.format("%s://localhost:%s/axis2/services/%s", secure ? "https" : "http", getPort(), serviceName);
    }

    @Override
    public EndpointReference getEndpointReference(String serviceName) {
        return new EndpointReference(getEndpoint(serviceName));
    }
}
