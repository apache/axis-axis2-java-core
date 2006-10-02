/*
 * Copyright 2004,2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.rahas.impl;

import org.apache.axiom.om.OMElement;

import javax.xml.namespace.QName;

/**
 *
 */
public abstract class AbstractIssuerConfig {

    /**
     * The key computation policy when clien't entropy is provided
     */
    public static class KeyComputation {
        public static final QName KEY_COMPUTATION = new QName("keyComputation");
        public final static int KEY_COMP_USE_REQ_ENT = 1;
        public final static int KEY_COMP_PROVIDE_ENT = 2;
        public final static int KEY_COMP_USE_OWN_KEY = 3;
    }

    public final static QName ADD_REQUESTED_ATTACHED_REF = new QName("addRequestedAttachedRef");
    public final static QName ADD_REQUESTED_UNATTACHED_REF = new QName("addRequestedUnattachedRef");
    public static final QName PROOF_KEY_TYPE = new QName("proofKeyType");

    /**
     * Element name to include the crypto properties used to load the
     * information used securing the response
     */
    public final static QName CRYPTO_PROPERTIES = new QName("cryptoProperties");
    public static final QName CRYPTO = new QName("crypto");

    protected int keyComputation = KeyComputation.KEY_COMP_PROVIDE_ENT;
    protected String proofKeyType = TokenIssuerUtil.ENCRYPTED_KEY;
    protected boolean addRequestedAttachedRef;
    protected boolean addRequestedUnattachedRef;
    protected long ttl = 300000;
    protected String cryptoPropertiesFile;
    protected OMElement cryptoPropertiesElement;
    protected int keySize = 256;

}
