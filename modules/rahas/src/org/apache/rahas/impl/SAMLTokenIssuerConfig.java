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

package org.apache.rahas.impl;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.rahas.TrustException;

import javax.xml.namespace.QName;


import java.io.FileInputStream;

/**
 * Configuration manager for the <code>SAMLTokenIssuer</code>
 * 
 * @see org.apache.rahas.impl.SAMLTokenIssuer
 */
public class SAMLTokenIssuerConfig {

    /**
     * The QName of the configuration element of the SAMLTokenIssuer
     */
    public final static QName SAML_ISSUER_CONFIG = new QName("saml-issuer-config");
    
    /**
     * Element name to include the .properties file to be used to 
     * load the SAMLIssuer using WSS4J
     */
    private final static QName SAML_PROP_FILE = new QName("samlPropFile");
    
    /**
     * Element name to include the alias of the private key to sign the response or
     * the issued token
     */
    private final static QName USER = new QName("user");

    /**
     * Element name to include the crypto properties used to load the 
     * information used securing the response
     */
    private final static QName CRYPTO_PROPERTIES = new QName("cryptoProperties");
    
    protected String samlPropFile;
    protected String cryptoPropFile;
    protected String user;
    
    private SAMLTokenIssuerConfig(OMElement elem) throws TrustException {
        
        //Get the SAML_PROP_FILE
        OMElement samlPropFileElem = elem.getFirstChildWithName(SAML_PROP_FILE);
        if(samlPropFileElem != null) {
            this.samlPropFile = samlPropFileElem.getText().trim();
        }
        
        //If the SAML_PROP_FILE is missing then throw an exception
        //Without this SAMLtokenIssuer cannot create a SAML token
        if(this.samlPropFile == null || "".equals(this.samlPropFile)) {
            throw new TrustException("samlPropFileMissing");
        }
        
        OMElement userElem = elem.getFirstChildWithName(USER);
        if(userElem != null) {
            this.user = userElem.getText().trim();
        }
        
        OMElement cryptoPropElem = elem.getFirstChildWithName(CRYPTO_PROPERTIES);
        if(cryptoPropElem != null) {
            this.cryptoPropFile = cryptoPropElem.getText().trim();
        }
    }
    
    public static SAMLTokenIssuerConfig load(OMElement elem) throws TrustException {
        return new SAMLTokenIssuerConfig(elem);
    }
    
    public static SAMLTokenIssuerConfig load(String configFilePath)
            throws TrustException {
        FileInputStream fis = null;
        StAXOMBuilder builder = null;
        try {
            fis = new FileInputStream(configFilePath);
            builder = new StAXOMBuilder(fis);
        } catch (Exception e) {
            throw new TrustException("errorLoadingConfigFile",
                    new String[] { configFilePath });
        }
        
        return builder != null ? load(builder.getDocumentElement()) : null;
    }
    
}
