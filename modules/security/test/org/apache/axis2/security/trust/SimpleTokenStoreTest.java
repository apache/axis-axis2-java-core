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

import junit.framework.TestCase;

import java.util.ArrayList;

public class SimpleTokenStoreTest extends TestCase {

    public void testAdd() {
        Token token = new Token("id-1");
        SimpleTokenStore store = new SimpleTokenStore();
        try {
            store.add(token);
        } catch (TrustException e) {
            fail("Adding a new token to an empty store should not fail, " +
                    "message : " + e.getMessage());
        }
        try {
            store.add(token);
            fail("Adding an existing token must throw an exception");
        } catch (TrustException e) {
            assertEquals("Incorrect exception message", 
                    TrustException.getMessage("tokenAlreadyExists", 
                    new String[] {token.getId()}), e.getMessage());
        }
    }
    
    public void testGettokenIdentifiers() {
        SimpleTokenStore store = new SimpleTokenStore();
        try {
            String[] ids = store.gettokenIdentifiers();
            assertNull("There should not be any token ids at this point", ids);
        } catch (TrustException e) {
            fail(e.getMessage());
        }
        try {
            store.add(new Token("id-1"));
            store.add(new Token("id-2"));
            store.add(new Token("id-3"));
            String[] ids = store.gettokenIdentifiers();
            assertEquals("Incorrect number fo token ids", 3, ids.length);
        } catch (TrustException e) {
            fail(e.getMessage());
        }
    }
    
    public void testUpdate() {
        SimpleTokenStore store = new SimpleTokenStore();
        Token token1 = new Token("id-1");
        try {
            store.update(token1);
            fail("An exception must be thrown at this point : noTokenToUpdate");
        } catch (TrustException e) {
            assertEquals("Incorrect exception message", TrustException
                    .getMessage("noTokenToUpdate", new String[] { token1
                            .getId() }), e.getMessage());
        }
        try {
            Token token = token1;
            store.add(token);
            store.add(new Token("id-2"));
            store.add(new Token("id-3"));
            token.setState(Token.EXPIRED);
            store.update(token);
        } catch (TrustException e) {
            fail(e.getMessage());
        }
    }
    
    public void testGetValidExpiredRenewedTokens() {
        SimpleTokenStore store = new SimpleTokenStore();
        
        Token token1 = new Token("id-1");
        Token token2 = new Token("id-2");
        Token token3 = new Token("id-3");
        Token token4 = new Token("id-4");
        Token token5 = new Token("id-5");
        Token token6 = new Token("id-6");
        Token token7 = new Token("id-7");
        
        token1.setState(Token.ISSUED);
        token2.setState(Token.ISSUED);
        token3.setState(Token.ISSUED);
        token4.setState(Token.RENEWED);
        token5.setState(Token.RENEWED);
        token6.setState(Token.EXPIRED);
        token7.setState(Token.CANCELLED);
        
        try {
            store.add(token1);
            store.add(token2);
            store.add(token3);
            store.add(token4);
            store.add(token5);
            store.add(token6);
            store.add(token7);
            
            ArrayList list = store.getValidTokens();
            ArrayList list2 = store.getExpiredTokens();
            ArrayList list3 = store.getRenewedTokens();
            ArrayList list4 = store.getCancelledTokens();
            
            assertEquals("Incorrect number of valid tokens", 5, list.size());
            assertEquals("Incorrect number of expired tokens", 1, 
                    list2.size());
            assertEquals("Incorrect number of newed tokens", 2, list3.size());
            assertEquals("Incorrect number of newed tokens", 1, list4.size());
            
        } catch (TrustException e) {
            fail(e.getMessage());
        }
    }
}
