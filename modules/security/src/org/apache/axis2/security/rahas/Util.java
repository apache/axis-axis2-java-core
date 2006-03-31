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

package org.apache.axis2.security.rahas;

import org.apache.ws.security.components.crypto.Crypto;
import org.apache.ws.security.components.crypto.CryptoFactory;

public class Util {

    /**
     * Returns the crypto instance of this configuration.
     * If one is not availabale then it will try to create a <code>Crypto</code>
     * instance using available configuration information and will set it as 
     * the <code>Crypto</code> instance of the configuration.
     *  
     * @param config
     * @return
     * @throws RahasException
     */
    public static Crypto getCryptoInstace(RahasConfiguration config) throws RahasException {
        if(config.getCrypto() != null) {
            return config.getCrypto();
        } else  {
            Crypto crypto = null;
            if(config.getCryptoClassName() != null && config.getCryptoProperties() != null) {
                crypto = CryptoFactory.getInstance(config.getCryptoClassName(), config.getCryptoProperties());
            } else if(config.getCryptoPropertiesFile() != null) {
                if(config.getClassLoader() != null) {
                    crypto = CryptoFactory.getInstance(config.getCryptoPropertiesFile(), config.getClassLoader());
                } else {
                    crypto = CryptoFactory.getInstance(config.getCryptoPropertiesFile());
                }
            } else {
                throw new RahasException("cannotCrateCryptoInstance");
            }
            config.setCrypto(crypto);
            return crypto;
        }
    }
    
}
