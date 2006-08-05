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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.neethi.PolicyComponent;

public class SymmetricBinding extends SymmetricAsymmetricBindingBase {

    private EncryptionToken encryptionToken;
    
    private SignatureToken signatureToken;
    
    private ProtectionToken protectionToken;
    
    private List symmetricBindings = new ArrayList();
    
    /**
     * @return Returns the encryptionToken.
     */
    public EncryptionToken getEncryptionToken() {
        return encryptionToken;
    }

    /**
     * @param encryptionToken The encryptionToken to set.
     */
    public void setEncryptionToken(EncryptionToken encryptionToken)  {
        if(this.protectionToken != null) {
//            throw new WSSPolicyException("Cannot use an EncryptionToken in a " +
//                    "SymmetricBinding when there is a ProtectionToken");
        }
        this.encryptionToken = encryptionToken;
    }

    /**
     * @return Returns the protectionToken.
     */
    public ProtectionToken getProtectionToken() {
        return protectionToken;
    }

    /**
     * @param protectionToken The protectionToken to set.
     */
    public void setProtectionToken(ProtectionToken protectionToken)  {
        if(this.encryptionToken != null || this.signatureToken != null) {
//            throw new WSSPolicyException("Cannot use a ProtectionToken in a " +
//            "SymmetricBinding when there is a SignatureToken or an" +
//            "EncryptionToken");
        }
        this.protectionToken = protectionToken;
    }

    /**
     * @return Returns the signatureToken.
     */
    public SignatureToken getSignatureToken() {
        return signatureToken;
    }

    /**
     * @param signatureToken The signatureToken to set.
     */
    public void setSignatureToken(SignatureToken signatureToken) {
        if(this.protectionToken != null) {
//            throw new WSSPolicyException("Cannot use a SignatureToken in a " +
//                    "SymmetricBinding when there is a ProtectionToken");
        }
        this.signatureToken = signatureToken;
    }
    
    public Iterator getOptions() {
        return symmetricBindings.iterator();
    }
    
    public void addOption(SymmetricBinding symmetricBinding) {
        symmetricBindings.add(symmetricBinding);
    }

    public QName getName() {
        // TODO Auto-generated method stub
        return null;
    }

    public PolicyComponent normalize() {
        // TODO Auto-generated method stub
        return null;
    }

    public void serialize(XMLStreamWriter writer) throws XMLStreamException {
        // TODO Auto-generated method stub
        
    }
    
    
}
