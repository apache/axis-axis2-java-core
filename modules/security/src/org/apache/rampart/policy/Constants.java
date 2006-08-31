/*
 * Copyright 2001-2004 The Apache Software Foundation.
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
package org.apache.rampart.policy;

import javax.xml.namespace.QName;

public interface Constants {
    
    public static final String RAMPART_NS = "http://ws.apache.org/rampart/policy";
    
   
    
    public interface RampartConifg {
        
        public static final QName NAME = new QName(RAMPART_NS, "RampartConfig");
        
        public static final QName USER = new QName(RAMPART_NS, "user");
        
        public static final QName ENCRYPTION_USER = new QName(RAMPART_NS, "encryptionUser");
        
        public static final QName PASSWD_CALLBACK_CLASS = new QName(RAMPART_NS, "passwordCallbackClass");
        
        public static final QName SIGNATURE_CRYPTO = new QName(RAMPART_NS, "signatureCrypto");
                
    }
    
    public interface Crypto {
        
        public static final QName NAME = new QName(RAMPART_NS, "crypto");
        
        public static final QName PROVIDER_ATTR = new QName("provider");
        
        public static final QName PROPERTY = new QName(RAMPART_NS, "property");
        
        public static final QName PROPERTY_NAME_ATTR = new QName("name");
        
    }
    

}
