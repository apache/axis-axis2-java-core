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
 * SCTIssuer Configuration processor
 */
public class SCTIssuerConfig extends AbstractIssuerConfig{

    public final static QName SCT_ISSUER_CONFIG = new QName("sct-issuer-config");
    protected byte[] requesterEntropy;

    private SCTIssuerConfig(OMElement elem) throws TrustException {
        OMElement proofKeyElem = elem.getFirstChildWithName(PROOF_KEY_TYPE);
        if (proofKeyElem != null) {
            this.proofKeyType = proofKeyElem.getText().trim();
        }

        OMElement cryptoPropertiesElem = elem
                .getFirstChildWithName(new QName("cryptoProperties"));

        if (!TokenIssuerUtil.BINARY_SECRET.equals(proofKeyType) && cryptoPropertiesElem == null) {
            throw new TrustException("sctIssuerCryptoPropertiesMissing");
        }

        this.addRequestedAttachedRef =
                elem.getFirstChildWithName(ADD_REQUESTED_ATTACHED_REF) != null;
        this.addRequestedUnattachedRef =
                elem.getFirstChildWithName(ADD_REQUESTED_UNATTACHED_REF) != null;
        if ((cryptoPropertiesElement =
                cryptoPropertiesElem.getFirstChildWithName(CRYPTO)) == null) { // no children. Hence, prop file shud have been defined
            this.cryptoPropertiesFile = cryptoPropertiesElem.getText().trim();
        }
        // else Props should be defined as children of a crypto element
        
        OMElement keyCompElem = elem.getFirstChildWithName(KeyComputation.KEY_COMPUTATION);
        if (keyCompElem != null && keyCompElem.getText() != null && !"".equals(keyCompElem)) {
            this.keyComputation = Integer.parseInt(keyCompElem.getText());
        }
    }

    public static SCTIssuerConfig load(OMElement elem) throws TrustException {
        return new SCTIssuerConfig(elem);
    }

    public static SCTIssuerConfig load(String configFilePath)
            throws TrustException {
        FileInputStream fis;
        StAXOMBuilder builder;
        try {
            fis = new FileInputStream(configFilePath);
            builder = new StAXOMBuilder(fis);
        } catch (Exception e) {
            throw new TrustException("errorLoadingConfigFile",
                    new String[] { configFilePath });
        }

        return load(builder.getDocumentElement());
    }
}