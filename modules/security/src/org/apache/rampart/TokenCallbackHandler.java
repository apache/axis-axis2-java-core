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

package org.apache.rampart;

import org.apache.rahas.Token;
import org.apache.rahas.TokenStorage;
import org.apache.ws.security.WSPasswordCallback;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;

import java.io.IOException;


public class TokenCallbackHandler implements CallbackHandler {

    private TokenStorage store;
    private CallbackHandler handler;
    
    public TokenCallbackHandler(TokenStorage store, CallbackHandler handler) {
        this.store = store;
        this.handler = handler;
    }
    
    public void handle(Callback[] callbacks) 
    throws IOException, UnsupportedCallbackException {
        
        for (int i = 0; i < callbacks.length; i++) {

            if (callbacks[i] instanceof WSPasswordCallback) {
                WSPasswordCallback pc = (WSPasswordCallback) callbacks[i];
                if(pc.getUsage() == WSPasswordCallback.SECURITY_CONTEXT_TOKEN &&
                        this.store != null) {
                    String id = pc.getIdentifer();
                    Token tok;
                    try {
                        //Pick up the token from the token store
                        tok = this.store.getToken(id);
                        if(tok != null) {
                            //Get the secret and set it in the callback object
                            pc.setKey(tok.getSecret());
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        throw new IOException(e.getMessage());
                    }
                } else {
                    //Handle other types of callbacks with the usual handler
                    if(this.handler != null) {
                        handler.handle(new Callback[]{pc});
                    }
                }

            } else {
                throw new UnsupportedCallbackException(callbacks[i],
                        "Unrecognized Callback");
            }
        }
    }
    


}
