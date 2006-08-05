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

import org.apache.ws.security.policy.Constants;

public abstract class SymmetricAsymmetricBindingBase extends Binding {

    private String protectionOrder = Constants.SIGN_BEFORE_ENCRYPTING;
    
    private boolean signatureProtection;
    
    private boolean tokenProtection;
    
    private boolean entireHeaderAndBodySignatures;

    /**
     * @return Returns the entireHeaderAndBodySignatures.
     */
    public boolean isEntireHeaderAndBodySignatures() {
        return entireHeaderAndBodySignatures;
    }

    /**
     * @param entireHeaderAndBodySignatures The entireHeaderAndBodySignatures to set.
     */
    public void setEntireHeaderAndBodySignatures(
            boolean entireHeaderAndBodySignatures) {
        this.entireHeaderAndBodySignatures = entireHeaderAndBodySignatures;
    }

    /**
     * @return Returns the protectionOrder.
     */
    public String getProtectionOrder() {
        return protectionOrder;
    }

    /**
     * @param protectionOrder The protectionOrder to set.
     */
    public void setProtectionOrder(String protectionOrder) {
        if(Constants.ENCRYPT_BEFORE_SIGNING.equals(protectionOrder) ||
           Constants.SIGN_BEFORE_ENCRYPTING.equals(protectionOrder)) {
            this.protectionOrder = protectionOrder;
        } else {
//            throw new WSSPolicyException("Incorrect protection order value : "
//                    + protectionOrder);
        }
    }

    /**
     * @return Returns the signatureProtection.
     */
    public boolean isSignatureProtection() {
        return signatureProtection;
    }

    /**
     * @param signatureProtection The signatureProtection to set.
     */
    public void setSignatureProtection(boolean signatureProtection) {
        this.signatureProtection = signatureProtection;
    }

    /**
     * @return Returns the tokenProtection.
     */
    public boolean isTokenProtection() {
        return tokenProtection;
    }

    /**
     * @param tokenProtection The tokenProtection to set.
     */
    public void setTokenProtection(boolean tokenProtection) {
        this.tokenProtection = tokenProtection;
    }
    
    
    
}
