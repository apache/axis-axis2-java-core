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

package org.apache.rampart.policy.model;

import org.apache.neethi.Assertion;
import org.apache.neethi.Constants;
import org.apache.neethi.PolicyComponent;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 * Rampart policy model bean to capture Rampart configuration assertion info.
 * 
 * Example:
 * 
 * <pre>
 *  &lt;ramp:RampartConfig xmlns:ramp=&quot;http://ws.apache.org/rampart/policy&quot;&gt; 
 *  &lt;ramp:user&gt;alice&lt;/ramp:user&gt;
 *  &lt;ramp:encryptionUser&gt;bob&lt;/ramp:encryptionUser&gt;
 *  &lt;ramp:passwordCallbackClass&gt;org.apache.axis2.security.PWCallback&lt;/ramp:passwordCallbackClass&gt;
 *  &lt;ramp:timestampTTL&gt;300&lt;/ramp:timestampTTL&gt;
 *  &lt;ramp:tokenStoreClass&gt;org.apache.rahas.StorageImpl&lt;/ramp:tokenStoreClass&gt;
 *  
 *  &lt;ramp:signatureCrypto&gt;
 *  &lt;ramp:crypto provider=&quot;org.apache.ws.security.components.crypto.Merlin&quot;&gt;
 *  &lt;ramp:property name=&quot;keystoreType&quot;&gt;JKS&lt;/ramp:property&gt;
 *  &lt;ramp:property name=&quot;keystoreFile&quot;&gt;/path/to/file.jks&lt;/ramp:property&gt;
 *  &lt;ramp:property name=&quot;keystorePassword&quot;&gt;password&lt;/ramp:property&gt;
 *  &lt;/ramp:crypto&gt;
 *  &lt;/ramp:signatureCrypto&gt;
 *  
 *  &lt;ramp:tokenIssuerPolicy&gt;
 *  &lt;wsp:Policy&gt;
 *  ....
 *  ....
 *  &lt;/wsp:Policy&gt;
 *  &lt;/ramp:tokenIssuerPolicy&gt;
 *  &lt;/ramp:RampartConfig&gt;
 * 
 * </pre>
 * 
 */
public class RampartConfig implements Assertion {

    public static final int DEFAULT_TIMESTAMP_TTL = 300000;

    public final static String NS = "http://ws.apache.org/rampart/policy";

    public final static String PREFIX = "rampart";

    public final static String RAMPART_CONFIG_LN = "RampartConfig";

    public final static String USER_LN = "user";

    public final static String ENCRYPTION_USER_LN = "encryptionUser";

    public final static String PW_CB_CLASS_LN = "passwordCallbackClass";

    public final static String SIG_CRYPTO_LN = "signatureCrypto";

    public final static String ENCR_CRYPTO_LN = "encryptionCypto";

    public final static String DEC_CRYPTO_LN = "decryptionCrypto";

    public final static String TS_TTL_LN = "timestampTTL";

    public final static String TOKEN_STORE_CLASS_LN = "tokenStoreClass";

    private String user;

    private String encryptionUser;

    private String pwCbClass;

    private CryptoConfig sigCryptoConfig;

    private CryptoConfig encrCryptoConfig;

    private CryptoConfig decCryptoConfig;

    private String timestampTTL = Integer.toString(DEFAULT_TIMESTAMP_TTL);

    private String tokenStoreClass;

    /**
     * @return Returns the tokenStoreClass.
     */
    public String getTokenStoreClass() {
        return tokenStoreClass;
    }

    /**
     * @param tokenStoreClass
     *            The tokenStoreClass to set.
     */
    public void setTokenStoreClass(String tokenStoreClass) {
        this.tokenStoreClass = tokenStoreClass;
    }

    public CryptoConfig getDecCryptoConfig() {
        return decCryptoConfig;
    }

    public void setDecCryptoConfig(CryptoConfig decCrypto) {
        this.decCryptoConfig = decCrypto;
    }

    public CryptoConfig getEncrCryptoConfig() {
        return encrCryptoConfig;
    }

    public void setEncrCryptoConfig(CryptoConfig encrCrypto) {
        this.encrCryptoConfig = encrCrypto;
    }

    public String getEncryptionUser() {
        return encryptionUser;
    }

    public void setEncryptionUser(String encryptionUser) {
        this.encryptionUser = encryptionUser;
    }

    public String getPwCbClass() {
        return pwCbClass;
    }

    public void setPwCbClass(String pwCbClass) {
        this.pwCbClass = pwCbClass;
    }

    public CryptoConfig getSigCryptoConfig() {
        return sigCryptoConfig;
    }

    public void setSigCryptoConfig(CryptoConfig sigCryptoConfig) {
        this.sigCryptoConfig = sigCryptoConfig;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public QName getName() {
        return new QName(NS, RAMPART_CONFIG_LN);
    }

    public boolean isOptional() {
        // TODO TODO
        throw new UnsupportedOperationException("TODO");
    }

    public PolicyComponent normalize() {
        // TODO TODO
        throw new UnsupportedOperationException("TODO");
    }

    public void serialize(XMLStreamWriter writer) throws XMLStreamException {
        String prefix = writer.getPrefix(NS);

        if (prefix == null) {
            prefix = PREFIX;
            writer.setPrefix(PREFIX, NS);
        }

        writer.writeStartElement(PREFIX, RAMPART_CONFIG_LN, NS);
        writer.writeNamespace(prefix, NS);

        if (getUser() != null) {
            writer.writeStartElement(NS, USER_LN);
            writer.writeCharacters(getUser());
            writer.writeEndElement();
        }
        
        if (getEncryptionUser() != null) {
            writer.writeStartElement(NS, ENCRYPTION_USER_LN);
            writer.writeCharacters(getEncryptionUser());
            writer.writeEndElement();
        }
        
        if (getPwCbClass() != null) {
            writer.writeStartElement(NS, PW_CB_CLASS_LN);
            writer.writeCharacters(getPwCbClass());
            writer.writeEndElement();
        }
        
        if (getTimestampTTL() != null) {
            writer.writeStartElement(NS, TS_TTL_LN);
            writer.writeCharacters(getTimestampTTL());
            writer.writeEndElement();
        }
        
        if (getTokenStoreClass() != null) {
            writer.writeStartElement(NS, TOKEN_STORE_CLASS_LN);
            writer.writeCharacters(getTokenStoreClass());
            writer.writeEndElement();
        }
        
        if (encrCryptoConfig != null) {
            writer.writeStartElement(NS, ENCR_CRYPTO_LN);
            encrCryptoConfig.serialize(writer);
            writer.writeEndElement();
            
        }
        
        if (decCryptoConfig != null) {
            writer.writeStartElement(NS, DEC_CRYPTO_LN);
            decCryptoConfig.serialize(writer);
            writer.writeEndElement();
        }
        
        if (sigCryptoConfig != null) {
            writer.writeStartElement(NS, SIG_CRYPTO_LN);
            sigCryptoConfig.serialize(writer);
            writer.writeEndElement();
        }
        
        writer.writeEndElement();

    }

    public boolean equal(PolicyComponent policyComponent) {
        throw new UnsupportedOperationException("TODO");
    }

    public short getType() {
        return Constants.TYPE_ASSERTION;
    }

    /**
     * @return Returns the timestampTTL.
     */
    public String getTimestampTTL() {
        return timestampTTL;
    }

    /**
     * @param timestampTTL
     *            The timestampTTL to set.
     */
    public void setTimestampTTL(String timestampTTL) {
        this.timestampTTL = timestampTTL;
    }

}
