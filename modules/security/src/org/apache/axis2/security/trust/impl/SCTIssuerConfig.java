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

package org.apache.axis2.security.trust.impl;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axis2.security.trust.TrustException;

import javax.xml.namespace.QName;

import java.io.FileInputStream;

/**
 * SCTIssuer Configuration processor
 */
public class SCTIssuerConfig {

    public final static QName SCT_ISSUER_CONFIG = new QName("sct-issuer-config");
    
    protected String proofTokenType = SCTIssuer.ENCRYPTED_KEY;

    protected String cryptoPropertiesFile = null;

    public static SCTIssuerConfig load(OMElement elem) throws TrustException {
        return new SCTIssuerConfig(elem);
    }
    
    public static SCTIssuerConfig load(String configFilePath)
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
        
        return load(builder.getDocumentElement());
    }

    public SCTIssuerConfig(OMElement elem) throws TrustException {
        OMElement proofTokenElem = (OMElement) elem.getFirstChildWithName(
                new QName("proofToken"));
        if (proofTokenElem != null) {
            this.proofTokenType = proofTokenElem.getText();
        }

        OMElement cryptoPropertiesElem = (OMElement) elem
                .getFirstChildWithName(new QName("cryptoProperties"));

        if (!SCTIssuer.BINARY_SECRET.equals(proofTokenType)
                && cryptoPropertiesElem == null) {
            throw new TrustException("sctIssuerCryptoPropertiesMissing");
        }

        this.cryptoPropertiesFile = cryptoPropertiesElem.getText();
    }
}