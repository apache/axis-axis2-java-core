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
import org.apache.neethi.PolicyComponent;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 * Rampart policy model bean to capture Rampart configuration assertion info.
 * 
 * Example:
<pre>
    <ramp:RampartConfig xmlns:ramp="http://ws.apache.org/rampart/policy"> 
        <ramp:user>alice</ramp:user>
        <ramp:encryptionUser>bob</ramp:encryptionUser>
        <ramp:passwordCallbackClass>org.apache.axis2.security.PWCallback</ramp:passwordCallbackClass>
        
        <ramp:signatureCryto>
            <ramp:crypto provider="org.apache.ws.security.components.crypto.Merlin">
                <ramp:property name="keystoreType">JKS</ramp:property>
                <ramp:property name="keystoreFile">/path/to/file.jks</ramp:property>
                <ramp:property name="keystorePassword">password</ramp:property>
            </ramp:crypto>
        </ramp:signatureCryto>
    </ramp:RampartConfig>
</pre>
 * 
 */
public class RampartConfig implements Assertion {

    public final static String NS = "http://ws.apache.org/rampart/policy";
    
    public final static String RAMPART_CONFIG_LN = "RampartConfig";
    
    public final static String USER_LN = "user";
    
    public final static String ENCRYPTION_USER_LN = "encryptionUser";
    
    public final static String PW_CB_CLASS_LN = "passwordCallbackClass";
    
    public final static String SIG_CRYPTO_LN = "signatureCryto";
    
    public final static String ENCR_CRYPTO_LN = "encryptionCypto";
    
    public final static String DEC_CRYPTO_LN = "decryptionCrypto";
    
    public final static String TS_TTL_LN = "timestampTTL";
    
    private String user;
    private String encryptionUser;
    private String pwCbClass;
    private CryptoConfig sigCryptoConfig;
    private CryptoConfig encrCryptoConfig;
    private CryptoConfig decCryptoConfig;
    private String timestampTTL;
    
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
        return new QName(NS,RAMPART_CONFIG_LN);
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
        // TODO TODO
        throw new UnsupportedOperationException("TODO");
    }

    public short getType() {
        // TODO TODO
        throw new UnsupportedOperationException("TODO");
    }

    /**
     * @return Returns the timestampTTL.
     */
    public String getTimestampTTL() {
        return timestampTTL;
    }

    /**
     * @param timestampTTL The timestampTTL to set.
     */
    public void setTimestampTTL(String timestampTTL) {
        this.timestampTTL = timestampTTL;
    }

}
