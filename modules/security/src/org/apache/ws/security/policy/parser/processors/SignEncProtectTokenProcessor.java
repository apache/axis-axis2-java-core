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
package org.apache.ws.security.policy.parser.processors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ws.security.policy.WSSPolicyException;
import org.apache.ws.security.policy.model.ProtectionToken;
import org.apache.ws.security.policy.model.SymmetricBinding;
import org.apache.ws.security.policy.parser.SecurityPolicy;
import org.apache.ws.security.policy.parser.SecurityPolicyToken;
import org.apache.ws.security.policy.parser.SecurityProcessorContext;


/**
 * @author Werner Dittmann (werner@apache.org)
 * 
 */
public class SignEncProtectTokenProcessor {
    
    private Log log = LogFactory.getLog(getClass());
    
    private boolean initializedSignatureToken = false;

	private boolean initializedEncryptionToken = false;

	private boolean initializedProtectionToken = false;

	/**
	 * Intialize the SignatureToken complex token.
	 * 
	 * This method creates a copy of the SignatureToken token and sets the
	 * handler object to the copy. Then it creates copies of the child tokens
	 * that are allowed for SignatureToken. These tokens are:
	 * 
	 * These copies are also initialized with the handler object and then set as
	 * child tokens of SignatureToken.
	 * 
	 * @param spt
	 *            The token that will hold the child tokens.
	 * @throws NoSuchMethodException
	 */
	private void initializeSignatureToken(SecurityPolicyToken spt)
			throws NoSuchMethodException {
		SecurityPolicyToken tmpSpt = SecurityPolicy.x509Token.copy();
		tmpSpt.setProcessTokenMethod(new X509TokenProcessor());
		spt.setChildToken(tmpSpt);
	}

	/**
	 * Intialize the EncryptionToken complex token.
	 * 
	 * This method creates a copy of the EncryptionToken token and sets the
	 * handler object to the copy. Then it creates copies of the child tokens
	 * that are allowed for EncryptionToken. These tokens are:
	 * 
	 * These copies are also initialized with the handler object and then set as
	 * child tokens of SignatureToken.
	 * 
	 * @param spt
	 *            The token that will hold the child tokens.
	 * @throws NoSuchMethodException
	 */
	private void initializeEncryptionToken(SecurityPolicyToken spt)
			throws NoSuchMethodException {
		SecurityPolicyToken tmpSpt = SecurityPolicy.x509Token.copy();
		tmpSpt.setProcessTokenMethod(new X509TokenProcessor());
		spt.setChildToken(tmpSpt);
	}

	/**
	 * Intialize the ProtectionToken complex token.
	 * 
	 * This method creates a copy of the ProtectionToken token and sets the
	 * handler object to the copy. Then it creates copies of the child tokens
	 * that are allowed for ProtectionToken. These tokens are:
	 * 
	 * These copies are also initialized with the handler object and then set as
	 * child tokens of ProtectionToken.
	 * 
	 * @param spt
	 *            The token that will hold the child tokens.
	 * @throws NoSuchMethodException
	 */
	private void initializeProtectionToken(SecurityPolicyToken spt)
			throws NoSuchMethodException {
		SecurityPolicyToken tmpSpt = SecurityPolicy.x509Token.copy();
		tmpSpt.setProcessTokenMethod(new X509TokenProcessor());
		spt.setChildToken(tmpSpt);
	}

	public Object doSignatureToken(SecurityProcessorContext spc) {
		log.debug("Processing "
				+ spc.readCurrentSecurityToken().getTokenName() + ": "
				+ SecurityProcessorContext.ACTION_NAMES[spc.getAction()]);
		SecurityPolicyToken spt = spc.readCurrentSecurityToken();

		switch (spc.getAction()) {

		case SecurityProcessorContext.START:
			if (!initializedSignatureToken) {
				try {
					initializeSignatureToken(spt);
					initializedSignatureToken = true;
				} catch (NoSuchMethodException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return new Boolean(false);
				}
			}
			break;
		case SecurityProcessorContext.COMMIT:
			break;
		case SecurityProcessorContext.ABORT:
			break;
		}
		return new Boolean(true);
	}

	public Object doEncryptionToken(SecurityProcessorContext spc) {
		log.debug("Processing "
				+ spc.readCurrentSecurityToken().getTokenName() + ": "
				+ SecurityProcessorContext.ACTION_NAMES[spc.getAction()]);
		SecurityPolicyToken spt = spc.readCurrentSecurityToken();
		switch (spc.getAction()) {

		case SecurityProcessorContext.START:
			if (!initializedEncryptionToken) {
				try {
					initializeEncryptionToken(spt);
					initializedEncryptionToken = true;
				} catch (NoSuchMethodException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return new Boolean(false);
				}
			}
			break;
		case SecurityProcessorContext.COMMIT:
			break;
		case SecurityProcessorContext.ABORT:
			break;
		}
		return new Boolean(true);
	}

	public Object doProtectionToken(SecurityProcessorContext spc) {
		log.debug("Processing "
				+ spc.readCurrentSecurityToken().getTokenName() + ": "
				+ SecurityProcessorContext.ACTION_NAMES[spc.getAction()]);
		SecurityPolicyToken spt = spc.readCurrentSecurityToken();
		switch (spc.getAction()) {

		case SecurityProcessorContext.START:
			if (!initializedProtectionToken) {
				try {
					initializeProtectionToken(spt);
                    ProtectionToken protectionToken = (ProtectionToken) spc
                            .readCurrentPolicyEngineData();
                    try {
                        ((SymmetricBinding) spc.readPreviousPolicyEngineData())
                                .setProtectionToken(protectionToken);
                    } catch (WSSPolicyException e) {
                        // TODO Throw this out
                        e.printStackTrace();
                    }
					initializedProtectionToken = true;
				} catch (NoSuchMethodException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return new Boolean(false);
				}
			}
			break;
		case SecurityProcessorContext.COMMIT:
			break;
		case SecurityProcessorContext.ABORT:
			break;
		}
		return new Boolean(true);
	}
}
