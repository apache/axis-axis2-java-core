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
import java.util.Map;

/**
 * Configuration manager for the <code>SAMLTokenIssuer</code>
 *
 * @see SAMLTokenIssuer
 */
public class SAMLTokenIssuerConfig extends AbstractIssuerConfig {

    /**
     * The QName of the configuration element of the SAMLTokenIssuer
     */
    public final static QName SAML_ISSUER_CONFIG = new QName("saml-issuer-config");

    /**
     * Element name to include the alias of the private key to sign the response or
     * the issued token
     */
    private final static QName ISSUER_KEY_ALIAS = new QName("issuerKeyAlias");

    /**
     * Element name to include the password of the private key to sign the
     * response or the issued token
     */
    private final static QName ISSUER_KEY_PASSWD = new QName("issuerKeyPassword");

    /**
     * Element to specify the lifetime of the SAMLToken
     * Dafaults to 300000 milliseconds (5 mins)
     */
    private final static QName TTL = new QName("timeToLive");

    /**
     * Element to list the trusted services
     */
    private final static QName TRUSTED_SERVICES = new QName("trusted-services");

    private final static QName KEY_SIZE = new QName("keySize");

    private final static QName SERVICE = new QName("service");
    private final static QName ALIAS = new QName("alias");

    public final static QName USE_SAML_ATTRIBUTE_STATEMENT = new QName("useSAMLAttributeStatement");

    public final static QName ISSUER_NAME = new QName("issuerName");

    protected String issuerKeyAlias;
    protected String issuerKeyPassword;
    protected String issuerName;
    protected Map trustedServices;
    protected String trustStorePropFile;

    private SAMLTokenIssuerConfig(OMElement elem) throws TrustException {
        OMElement proofKeyElem = elem.getFirstChildWithName(PROOF_KEY_TYPE);
        if (proofKeyElem != null) {
            this.proofKeyType = proofKeyElem.getText().trim();
        }

        //The alias of the private key
        OMElement userElem = elem.getFirstChildWithName(ISSUER_KEY_ALIAS);
        if (userElem != null) {
            this.issuerKeyAlias = userElem.getText().trim();
        }

        if (this.issuerKeyAlias == null || "".equals(this.issuerKeyAlias)) {
            throw new TrustException("samlIssuerKeyAliasMissing");
        }

        OMElement issuerKeyPasswdElem = elem.getFirstChildWithName(ISSUER_KEY_PASSWD);
        if (issuerKeyPasswdElem != null) {
            this.issuerKeyPassword = issuerKeyPasswdElem.getText().trim();
        }

        if (this.issuerKeyPassword == null || "".equals(this.issuerKeyPassword)) {
            throw new TrustException("samlIssuerKeyPasswdMissing");
        }

        OMElement issuerNameElem = elem.getFirstChildWithName(ISSUER_NAME);
        if (issuerNameElem != null) {
            this.issuerName = issuerNameElem.getText().trim();
        }

        if (this.issuerName == null || "".equals(this.issuerName)) {
            throw new TrustException("samlIssuerNameMissing");
        }

        OMElement cryptoPropElem = elem.getFirstChildWithName(CRYPTO_PROPERTIES);
        if (cryptoPropElem != null) {
            if ((cryptoPropertiesElement =
                    cryptoPropElem.getFirstChildWithName(CRYPTO)) == null){
                // no children. Hence, prop file shud have been defined
                this.cryptoPropertiesFile = cryptoPropElem.getText().trim();
            }
            // else Props should be defined as children of a crypto element
        }

        OMElement keyCompElem = elem.getFirstChildWithName(KeyComputation.KEY_COMPUTATION);
        if (keyCompElem != null && keyCompElem.getText() != null && !"".equals(keyCompElem)) {
            this.keyComputation = Integer.parseInt(keyCompElem.getText());
        }

        //time to live
        OMElement ttlElem = elem.getFirstChildWithName(TTL);
        if (ttlElem != null) {
            try {
                this.ttl = Long.parseLong(ttlElem.getText().trim());
            } catch (NumberFormatException e) {
                throw new TrustException("invlidTTL");
            }
        }

        OMElement keySizeElem = elem.getFirstChildWithName(KEY_SIZE);
        if (keySizeElem != null) {
            try {
                this.keySize = Integer.parseInt(keySizeElem.getText().trim());
            } catch (NumberFormatException e) {
                throw new TrustException("invalidKeysize");
            }
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
        if (trustedServices != null) {
            //Now process the trusted services
            Iterator servicesIter = trustedServices.getChildrenWithName(SERVICE);
            while (servicesIter.hasNext()) {
                OMElement service = (OMElement) servicesIter.next();
                OMAttribute aliasAttr = service.getAttribute(ALIAS);
                if (aliasAttr == null) {
                    //The certificate alias is a must
                    throw new TrustException("aliasMissingForService",
                                             new String[]{service.getText().trim()});
                }
                if (this.trustedServices == null) {
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
        FileInputStream fis;
        StAXOMBuilder builder;
        try {
            fis = new FileInputStream(configFilePath);
            builder = new StAXOMBuilder(fis);
        } catch (Exception e) {
            throw new TrustException("errorLoadingConfigFile",
                                     new String[]{configFilePath});
        }
        return load(builder.getDocumentElement());
    }

}
