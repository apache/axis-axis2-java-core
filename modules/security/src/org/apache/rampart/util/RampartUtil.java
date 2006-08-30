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

package org.apache.rampart.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.rampart.RampartException;
import org.apache.rampart.policy.model.CryptoConfig;
import org.apache.rampart.policy.model.RampartConfig;
import org.apache.ws.security.WSConstants;
import org.apache.ws.security.WSPasswordCallback;
import org.apache.ws.security.components.crypto.Crypto;
import org.apache.ws.security.components.crypto.CryptoFactory;
import org.apache.ws.security.util.Loader;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;

import java.util.Properties;

public class RampartUtil {

    private static Log log = LogFactory.getLog(RampartUtil.class);
    
    public static CallbackHandler getPasswordCB(ClassLoader classLoader, String cbHandlerClass) 
    throws RampartException {

        log.debug("loading class : " + cbHandlerClass);
        
        CallbackHandler cbHandler = null;
        
        if (cbHandlerClass != null) {
            Class cbClass = null;
            try {
                cbClass = Loader.loadClass(classLoader, cbHandlerClass);
            } catch (ClassNotFoundException e) {
                throw new RampartException(
                       "WSHandler: cannot load password callback class: "
               + cbHandlerClass, e);
            }
            try {
                cbHandler = (CallbackHandler) cbClass.newInstance();
            } catch (java.lang.Exception e) {
                throw new RampartException(
                     "WSHandler: cannot create instance of password callback: "
             + cbHandlerClass, e);
            }
        }
        
        return cbHandler;
    }
    
    
    
    /**
     * Perform a callback to get a password.
     * <p/>
     * The called back function gets an indication why to provide a password:
     * to produce a UsernameToken, Signature, or a password (key) for a given
     * name.
     */
    public static WSPasswordCallback performCallback(CallbackHandler cbHandler,
                                               String username,
                                               int doAction)
            throws RampartException {

        WSPasswordCallback pwCb = null;
        int reason = 0;

        switch (doAction) {
        case WSConstants.UT:
        case WSConstants.UT_SIGN:
                reason = WSPasswordCallback.USERNAME_TOKEN;
                break;
            case WSConstants.SIGN:
                reason = WSPasswordCallback.SIGNATURE;
                break;
            case WSConstants.ENCR:
                reason = WSPasswordCallback.KEY_NAME;
                break;
        }
        pwCb = new WSPasswordCallback(username, reason);
        Callback[] callbacks = new Callback[1];
        callbacks[0] = pwCb;
        /*
        * Call back the application to get the password
        */
        try {
            cbHandler.handle(callbacks);
        } catch (Exception e) {
            throw new RampartException("WSHandler: password callback failed", e);
        }
        return pwCb;
    }
    
    /**
     * Create the <code>Crypto</code> instance for encryption using information 
     * from the rampart configuration assertion
     * 
     * @param config
     * @return
     * @throws RampartException
     */
    public static Crypto getEncryptionCrypto(RampartConfig config) throws RampartException{
        log.debug("Loading encryption crypto");
        
        CryptoConfig cryptoConfig = config.getEncrCryptoConfig();
        if(cryptoConfig != null) {
            String provider = cryptoConfig.getProvider();
            log.debug("Usig provider: " + provider);
            Properties prop = cryptoConfig.getProp();
            return CryptoFactory.getInstance(provider, prop);
        } else {
            log.debug("Trying the signature crypto info");
            //Try using signature crypto infomation
            cryptoConfig = config.getSigCryptoConfig();
            
            if(cryptoConfig != null) {
                String provider = cryptoConfig.getProvider();
                log.debug("Usig provider: " + provider);
                Properties prop = cryptoConfig.getProp();
                return CryptoFactory.getInstance(provider, prop);
            } else {
                throw new RampartException("missingEncryptionCrypto");
            }
        }
    }
    
    /**
     * Create the <code>Crypto</code> instance for signature using information 
     * from the rampart configuration assertion
     * 
     * @param config
     * @return
     * @throws RampartException
     */
    public static Crypto getSignatureCrypto(RampartConfig config) throws RampartException {
        log.debug("Loading Signature crypto");
        
        CryptoConfig cryptoConfig = config.getSigCryptoConfig();
        if(cryptoConfig != null) {
            String provider = cryptoConfig.getProvider();
            log.debug("Usig provider: " + provider);
            Properties prop = cryptoConfig.getProp();
            return CryptoFactory.getInstance(provider, prop);
        } else {
            throw new RampartException("missingSignatureCrypto");
        }
    }
}
