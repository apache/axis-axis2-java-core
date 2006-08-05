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

package org.apache.ws.security.policy.model;

public class Wss10 {
    
    private boolean mustSupportRefKeyIdentifier;
    private boolean MustSupportRefIssuerSerial;
    private boolean MustSupportRefExternalURI;
    private boolean MustSupportRefEmbeddedToken;
    
    /**
     * @return Returns the mustSupportRefEmbeddedToken.
     */
    public boolean isMustSupportRefEmbeddedToken() {
        return MustSupportRefEmbeddedToken;
    }
    /**
     * @param mustSupportRefEmbeddedToken The mustSupportRefEmbeddedToken to set.
     */
    public void setMustSupportRefEmbeddedToken(boolean mustSupportRefEmbeddedToken) {
        MustSupportRefEmbeddedToken = mustSupportRefEmbeddedToken;
    }
    /**
     * @return Returns the mustSupportRefExternalURI.
     */
    public boolean isMustSupportRefExternalURI() {
        return MustSupportRefExternalURI;
    }
    /**
     * @param mustSupportRefExternalURI The mustSupportRefExternalURI to set.
     */
    public void setMustSupportRefExternalURI(boolean mustSupportRefExternalURI) {
        MustSupportRefExternalURI = mustSupportRefExternalURI;
    }
    /**
     * @return Returns the mustSupportRefIssuerSerial.
     */
    public boolean isMustSupportRefIssuerSerial() {
        return MustSupportRefIssuerSerial;
    }
    /**
     * @param mustSupportRefIssuerSerial The mustSupportRefIssuerSerial to set.
     */
    public void setMustSupportRefIssuerSerial(boolean mustSupportRefIssuerSerial) {
        MustSupportRefIssuerSerial = mustSupportRefIssuerSerial;
    }
    /**
     * @return Returns the mustSupportRefKeyIdentifier.
     */
    public boolean isMustSupportRefKeyIdentifier() {
        return mustSupportRefKeyIdentifier;
    }
    /**
     * @param mustSupportRefKeyIdentifier The mustSupportRefKeyIdentifier to set.
     */
    public void setMustSupportRefKeyIdentifier(boolean mustSupportRefKeyIdentifier) {
        this.mustSupportRefKeyIdentifier = mustSupportRefKeyIdentifier;
    }
    
}
