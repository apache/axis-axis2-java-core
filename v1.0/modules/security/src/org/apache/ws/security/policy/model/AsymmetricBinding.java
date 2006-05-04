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

public class AsymmetricBinding extends SymmetricAsymmetricBindingBase {
    
    private InitiatorToken initiatorToken;
    
    private RecipientToken recipientToken;
    
    /**
     * @return Returns the initiatorToken.
     */
    public InitiatorToken getInitiatorToken() {
        return initiatorToken;
    }
    /**
     * @param initiatorToken The initiatorToken to set.
     */
    public void setInitiatorToken(InitiatorToken initiatorToken) {
        this.initiatorToken = initiatorToken;
    }
    /**
     * @return Returns the recipientToken.
     */
    public RecipientToken getRecipientToken() {
        return recipientToken;
    }
    /**
     * @param recipientToken The recipientToken to set.
     */
    public void setRecipientToken(RecipientToken recipientToken) {
        this.recipientToken = recipientToken;
    }
    
    
}
