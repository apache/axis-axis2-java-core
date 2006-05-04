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

public class Wss11 extends Wss10 {
    
    private boolean MustSupportRefThumbprint;
    private boolean MustSupportRefEncryptedKey;
    private boolean RequireSignatureConfirmation;
    
    /**
     * @return Returns the mustSupportRefEncryptedKey.
     */
    public boolean isMustSupportRefEncryptedKey() {
        return MustSupportRefEncryptedKey;
    }
    /**
     * @param mustSupportRefEncryptedKey The mustSupportRefEncryptedKey to set.
     */
    public void setMustSupportRefEncryptedKey(boolean mustSupportRefEncryptedKey) {
        MustSupportRefEncryptedKey = mustSupportRefEncryptedKey;
    }
    /**
     * @return Returns the mustSupportRefThumbprint.
     */
    public boolean isMustSupportRefThumbprint() {
        return MustSupportRefThumbprint;
    }
    /**
     * @param mustSupportRefThumbprint The mustSupportRefThumbprint to set.
     */
    public void setMustSupportRefThumbprint(boolean mustSupportRefThumbprint) {
        MustSupportRefThumbprint = mustSupportRefThumbprint;
    }
    /**
     * @return Returns the requireSignatureConfirmation.
     */
    public boolean isRequireSignatureConfirmation() {
        return RequireSignatureConfirmation;
    }
    /**
     * @param requireSignatureConfirmation The requireSignatureConfirmation to set.
     */
    public void setRequireSignatureConfirmation(boolean requireSignatureConfirmation) {
        RequireSignatureConfirmation = requireSignatureConfirmation;
    }
    
    
}
