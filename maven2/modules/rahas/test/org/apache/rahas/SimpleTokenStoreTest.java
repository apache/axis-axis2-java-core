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

import junit.framework.TestCase;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.impl.dom.DOOMAbstractFactory;

import java.util.Date;

public class SimpleTokenStoreTest extends TestCase {

    public void testAdd() {
        SimpleTokenStore store = new SimpleTokenStore();
        try {
            store.add(getTestToken("id-1"));
        } catch (TrustException e) {
            fail("Adding a new token to an empty store should not fail, " +
                 "message : " + e.getMessage());
        }
        Token token = null;
        try {
            token = getTestToken("id-1");
            store.add(token);
            fail("Adding an existing token must throw an exception");
        } catch (TrustException e) {
            assertEquals("Incorrect exception message",
                         TrustException.getMessage("tokenAlreadyExists",
                                                   new String[]{token.getId()}), e.getMessage());
        }
    }

    public void testGettokenIdentifiers() {
        SimpleTokenStore store = new SimpleTokenStore();
        try {
            String[] ids = store.getTokenIdentifiers();
            assertEquals("There should not be any token ids at this point", 0, ids.length);
        } catch (TrustException e) {
            fail(e.getMessage());
        }
        try {
            store.add(getTestToken("id-1"));
            store.add(getTestToken("id-2"));
            store.add(getTestToken("id-3"));
            String[] ids = store.getTokenIdentifiers();
            assertEquals("Incorrect number fo token ids", 3, ids.length);
        } catch (TrustException e) {
            fail(e.getMessage());
        }
    }

    public void testUpdate() {
        SimpleTokenStore store = new SimpleTokenStore();
        Token token1 = null;
        try {
            token1 = getTestToken("id-1");
        } catch (TrustException e) {
            fail();
        }
        try {
            store.update(token1);
            fail("An exception must be thrown at this point : noTokenToUpdate");
        } catch (TrustException e) {
            assertEquals("Incorrect exception message", TrustException
                    .getMessage("noTokenToUpdate", new String[]{token1
                    .getId()}), e.getMessage());
        }
        try {
            store.add(token1);
            store.add(getTestToken("id-2"));
            store.add(getTestToken("id-3"));
            token1.setState(Token.EXPIRED);
            store.update(token1);
        } catch (TrustException e) {
            fail(e.getMessage());
        }
    }

    public void testGetValidExpiredRenewedTokens() {
        SimpleTokenStore store = new SimpleTokenStore();
        try {
            Token token1 = getTestToken("id-1", new Date(System.currentTimeMillis() + 10000));
            Token token2 = getTestToken("id-2", new Date(System.currentTimeMillis() + 10000));
            Token token3 = getTestToken("id-3", new Date(System.currentTimeMillis() + 10000));
            Token token4 = getTestToken("id-4", new Date(System.currentTimeMillis() + 10000));
            Token token5 = getTestToken("id-5", new Date(System.currentTimeMillis() + 10000));
            Token token6 = getTestToken("id-6", new Date(System.currentTimeMillis() + 10000));
            Token token7 = getTestToken("id-7", new Date(System.currentTimeMillis() + 10000));

            token1.setState(Token.ISSUED);
            token2.setState(Token.ISSUED);
            token3.setState(Token.ISSUED);
            token4.setState(Token.RENEWED);
            token5.setState(Token.RENEWED);
            token6.setState(Token.EXPIRED);
            token7.setState(Token.CANCELLED);

            store.add(token1);
            store.add(token2);
            store.add(token3);
            store.add(token4);
            store.add(token5);
            store.add(token6);
            store.add(token7);

            Token[] list = store.getValidTokens();
            Token[] list2 = store.getExpiredTokens();
            Token[] list3 = store.getRenewedTokens();
            Token[] list4 = store.getCancelledTokens();

            assertEquals("Incorrect number of valid tokens", 5, list.length);
            assertEquals("Incorrect number of expired tokens", 1, list2.length);
            assertEquals("Incorrect number of newed tokens", 2, list3.length);
            assertEquals("Incorrect number of newed tokens", 1, list4.length);

        } catch (TrustException e) {
            fail(e.getMessage());
        }
    }

    private Token getTestToken(String tokenId) throws TrustException {
        return getTestToken(tokenId, new Date());
    }

    private Token getTestToken(String tokenId, Date expiry) throws TrustException {
        OMFactory factory = DOOMAbstractFactory.getOMFactory();
        OMElement tokenEle = factory.createOMElement("testToken", "", "");
        Token token = new Token(tokenId, tokenEle, new Date(), expiry);
        token.setAttachedReference(tokenEle);
        token.setPreviousToken(tokenEle);
        token.setState(Token.ISSUED);
        token.setSecret("Top secret!".getBytes());
        return token;
    }
}
