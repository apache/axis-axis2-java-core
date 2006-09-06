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

package org.apache.rahas;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;

/**
 * In-memory implementation of the token storage
 */
public class SimpleTokenStore implements TokenStorage {

    protected Map tokens = new Hashtable();

    public void add(Token token) throws TrustException {
        if (token != null && !"".equals(token.getId()) &&
            token.getId() != null) {
            if (this.tokens.keySet().size() == 0
                || (this.tokens.keySet().size() > 0 && !this.tokens
                    .keySet().contains(token.getId()))) {
                tokens.put(token.getId(), token);
            } else {
                throw new TrustException("tokenAlreadyExists",
                                         new String[]{token.getId()});
            }

        }
    }

    public void update(Token token) throws TrustException {
        if (token != null && token.getId() != null && token.getId().trim().length() != 0) {

            if (!this.tokens.keySet().contains(token.getId())) {
                throw new TrustException("noTokenToUpdate", new String[]{token.getId()});
            }
            this.tokens.remove(this.tokens.get(token.getId()));
            this.tokens.put(token.getId(), token);
        }
    }

     public String[] getTokenIdentifiers() throws TrustException {
        List identifiers = new ArrayList();
        for (Iterator iterator = tokens.keySet().iterator(); iterator.hasNext();) {
            identifiers.add(iterator.next());
        }
        return (String[]) identifiers.toArray(new String[identifiers.size()]);
    }

    public List getValidTokens() throws TrustException {
        checkTokenExpiry();
        ArrayList issued = getTokens(Token.ISSUED);
        List renewed = getTokens(Token.RENEWED);
        for (Iterator iterator = renewed.iterator(); iterator.hasNext();) {
            issued.add(iterator.next());
        }
        return issued;
    }

    public List getRenewedTokens() throws TrustException {
        checkTokenExpiry();
        return getTokens(Token.RENEWED);
    }


    public List getCancelledTokens() throws TrustException {
        checkTokenExpiry();
        return getTokens(Token.CANCELLED);
    }

    public List getExpiredTokens() throws TrustException {
        checkTokenExpiry();
        return getTokens(Token.EXPIRED);
    }

    private ArrayList getTokens(int state) {
        ArrayList expiredTokens = new ArrayList();
        for (Iterator iterator = this.tokens.values().iterator(); iterator.hasNext();) {
            Token token = (Token) iterator.next();
            if (token.getState() == state) {
                expiredTokens.add(token);
            }
        }
        return expiredTokens;
    }

    public Token getToken(String id) throws TrustException {
        return (Token) this.tokens.get(id);
    }

    protected void checkTokenExpiry() throws TrustException {
        for (Iterator iterator = tokens.values().iterator(); iterator.hasNext();) {
            Token token = (Token) iterator.next();
            if (token.getExpires() != null &&
                token.getExpires().getTime() < System.currentTimeMillis()) {
                token.setState(Token.EXPIRED);
                update(token);
            }
        }
    }
}
