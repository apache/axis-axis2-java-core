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

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.rahas.TrustException;

import javax.xml.namespace.QName;

import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Iterator;

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
     * Element name to include the alias of the private key to sign the response or
     * the issued token
     */
    private final static QName USER = new QName("user");

    /**
     * Element name to include the crypto properties used to load the 
     * information used securing the response
     */
    private final static QName CRYPTO_PROPERTIES = new QName("cryptoProperties");
    
    private final static QName TRUSTED_SERVICES = new QName("trusted-services");
    
    private final static QName SERVICE = new QName("service");
    private final static QName ALIAS = new QName("alias");

    public final static QName ADD_REQUESTED_ATTACHED_REF = new QName("addRequestedAttachedRef");
    public final static QName ADD_REQUESTED_UNATTACHED_REF = new QName("addRequestedUnattachedRef");
    
    protected String cryptoPropFile;
    protected String user;

    protected HashMap trustedServices;
    protected String trustStorePropFile;

    protected boolean addRequestedAttachedRef;

    protected boolean addRequestedUnattachedRef;
    
    private SAMLTokenIssuerConfig(OMElement elem) throws TrustException {
        
        //The alias of the private key 
        OMElement userElem = elem.getFirstChildWithName(USER);
        if(userElem != null) {
            this.user = userElem.getText().trim();
        }

        OMElement cryptoPropElem = elem.getFirstChildWithName(CRYPTO_PROPERTIES);
        if(cryptoPropElem != null) {
            this.cryptoPropFile = cryptoPropElem.getText().trim();
        }
        
        this.addRequestedAttachedRef = elem
                .getFirstChildWithName(ADD_REQUESTED_ATTACHED_REF) != null;
        this.addRequestedUnattachedRef = elem
                .getFirstChildWithName(ADD_REQUESTED_UNATTACHED_REF) != null;
        
        //Process trusted services
        OMElement trustedServices = elem.getFirstChildWithName(TRUSTED_SERVICES);
        
        /*
         * If there are trusted services add them to a list
         * Only trusts myself to issue tokens to :
         * In this case the STS is embedded in the service as well and 
         * the issued token can only be used with that particular service
         * since the response secret is encrypted by the service's public key
         */
        if(trustedServices != null) {
            //Now process the trusted services
            Iterator servicesIter = trustedServices.getChildrenWithName(SERVICE);
            while (servicesIter.hasNext()) {
                OMElement service = (OMElement) servicesIter.next();
                OMAttribute aliasAttr = service.getAttribute(ALIAS);
                if(aliasAttr == null) {
                    //The certificate alias is a must
                    throw new TrustException("aliasMissingForService", new String[]{service.getText().trim()});
                }
                if(this.trustedServices == null) {
                    this.trustedServices = new HashMap();
                }
                
                //Add the trusted service and the alias to the map of services
                this.trustedServices.put(service.getText().trim(), aliasAttr.getAttributeValue());
            }
            
            //There maybe no trusted services as well, Therefore do not 
            //throw an exception when there are no trusted in the list at the 
            //moment
            
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
