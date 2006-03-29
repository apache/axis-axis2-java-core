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

package org.apache.axis2.security.trust;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;

/**
 * In-memory implementation of the token storage
 */
public class SimpleTokenStore implements TokenStorage {

    private Hashtable tokens = new Hashtable();

    public void add(Token token) throws TrustException {
        if (token != null && !"".equals(token.getId()) && 
                token.getId() != null) {
            if (this.tokens.keySet().size() == 0
                    || (this.tokens.keySet().size() > 0 && !this.tokens
                            .keySet().contains(token.getId()))) {
                tokens.put(token.getId(), token);
            } else {
                throw new TrustException("tokenAlreadyExists",
                        new String[] { token.getId() });
            }

        }
    }

    public void update(Token token) throws TrustException {
        if (token != null && !"".equals(token.getId()) && 
                token.getId() != null) {
            if(this.tokens.keySet().size() == 0 ||
                    (this.tokens.keySet().size() > 0 && 
                            !this.tokens.keySet().contains(token.getId()))) {
                    throw new TrustException("noTokenToUpdate",
                        new String[] { token.getId() });
            }
            this.tokens.remove(this.tokens.get(token.getId()));
            this.tokens.put(token.getId(), token);
        }
    }

    public String[] gettokenIdentifiers() throws TrustException {
        if (this.tokens.size() == 0) {
            return null;
        }
        String[] ids = new String[this.tokens.size()];
        Iterator iter = this.tokens.keySet().iterator();
        for (int i = 0; i < ids.length; i++) {
            ids[i] = (String) iter.next();
        }
        return ids;
    }

    public ArrayList getExpiredTokens() throws TrustException {
        return getTokens(Token.EXPIRED);
    }

    public ArrayList getCancelledTokens() throws TrustException {
        return getTokens(Token.CANCELLED);
    }
    
    public ArrayList getValidTokens() throws TrustException {
        ArrayList issued = getTokens(Token.ISSUED);
        ArrayList renewed = getTokens(Token.RENEWED);
        Iterator renewedIter = renewed.iterator();
        while (renewedIter.hasNext()) {
            issued.add(renewedIter.next());
        }
        return issued;
    }

    public ArrayList getRenewedTokens() throws TrustException {
        return getTokens(Token.RENEWED);
    }
    
    private ArrayList getTokens(int state) throws TrustException {
        if (this.tokens.size() == 0) {
            return null;
        }
        Iterator iter = this.tokens.keySet().iterator();
        ArrayList list = new ArrayList();
        while (iter.hasNext()) {
            String id = (String) iter.next();
            Token tok = (Token)this.tokens.get(id);
            if(tok.getState() == state) {
                list.add(tok);
            }
        }
        if(list.size() > 0) {
            return list;
        } else {
            return null;
        }
             
    }

    public Token getToken(String id) throws TrustException {
        return (Token)this.tokens.get(id);
    }
}
