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
import org.apache.ws.security.policy.model.Binding;
import org.apache.ws.security.policy.parser.SecurityPolicy;
import org.apache.ws.security.policy.parser.SecurityPolicyToken;
import org.apache.ws.security.policy.parser.SecurityProcessorContext;

/**
 * @author Werner Dittmann (werner@apache.org)
 * 
 */
public class AsymmetricBindingProcessor {

    private Log log = LogFactory.getLog(getClass());
    
    private boolean initializedAsymmetricBinding = false;
    
	/**
	 * Intialize the SymmetricBinding complex token.
	 * 
	 * This method creates a copy of the SymmetricBinding token and sets the
	 * handler object to the copy. Then it creates copies of the child tokens
	 * that are allowed for SymmetricBinding. These tokens are:
	 * 
	 * These copies are also initialized with the handler object and then set as
	 * child tokens of SymmetricBinding.
	 * 
	 * @param spt
	 *            The token that will hold the child tokens.
	 * @throws NoSuchMethodException
	 */
	private void initializeAsymmetricBinding(SecurityPolicyToken spt)
			throws NoSuchMethodException {

		InitiatorRecipientTokenProcessor irt = new InitiatorRecipientTokenProcessor();
		SecurityPolicyToken tmpSpt = SecurityPolicy.initiatorToken.copy();
		tmpSpt.setProcessTokenMethod(irt);
		spt.setChildToken(tmpSpt);

		tmpSpt = SecurityPolicy.recipientToken.copy();
		tmpSpt.setProcessTokenMethod(irt);
		spt.setChildToken(tmpSpt);

		tmpSpt = SecurityPolicy.algorithmSuite.copy();
		tmpSpt.setProcessTokenMethod(new AlgorithmSuiteProcessor());
		spt.setChildToken(tmpSpt);

		tmpSpt = SecurityPolicy.layout.copy();
		tmpSpt.setProcessTokenMethod(new LayoutProcessor());
		spt.setChildToken(tmpSpt);

		tmpSpt = SecurityPolicy.supportingTokens.copy();
		tmpSpt.setProcessTokenMethod(new SupportingTokensProcessor());
		spt.setChildToken(tmpSpt);

		tmpSpt = SecurityPolicy.signedSupportingTokens.copy();
		tmpSpt.setProcessTokenMethod(new SignedSupportingTokensProcessor());
		spt.setChildToken(tmpSpt);

		tmpSpt = SecurityPolicy.endorsingSupportingTokens.copy();
		tmpSpt.setProcessTokenMethod(new EndorsingSupportingTokensProcessor());
		spt.setChildToken(tmpSpt);

		tmpSpt = SecurityPolicy.signedEndorsingSupportingTokens.copy();
		tmpSpt.setProcessTokenMethod(new SignedEndorsingSupportingTokensProcessor());
		spt.setChildToken(tmpSpt);

		tmpSpt = SecurityPolicy.includeTimestamp.copy();
		tmpSpt.setProcessTokenMethod(this);
		spt.setChildToken(tmpSpt);

		tmpSpt = SecurityPolicy.encryptBeforeSigning.copy();
		tmpSpt.setProcessTokenMethod(this);
		spt.setChildToken(tmpSpt);

		tmpSpt = SecurityPolicy.encryptSignature.copy();
		tmpSpt.setProcessTokenMethod(this);
		spt.setChildToken(tmpSpt);

		tmpSpt = SecurityPolicy.protectTokens.copy();
		tmpSpt.setProcessTokenMethod(this);
		spt.setChildToken(tmpSpt);

		tmpSpt = SecurityPolicy.onlySignEntireHeadersAndBody.copy();
		tmpSpt.setProcessTokenMethod(this);
		spt.setChildToken(tmpSpt);

	}

	public Object doAsymmetricBinding(SecurityProcessorContext spc) {
		log.debug("Processing "
				+ spc.readCurrentSecurityToken().getTokenName() + ": "
				+ SecurityProcessorContext.ACTION_NAMES[spc.getAction()]);
		SecurityPolicyToken spt = spc.readCurrentSecurityToken();

		switch (spc.getAction()) {

		case SecurityProcessorContext.START:
			if (!initializedAsymmetricBinding) {
				try {
					initializeAsymmetricBinding(spt);
					initializedAsymmetricBinding = true;
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

	public Object doIncludeTimestamp(SecurityProcessorContext spc) {
		log.debug("Processing "
				+ spc.readCurrentSecurityToken().getTokenName() + ": "
				+ SecurityProcessorContext.ACTION_NAMES[spc.getAction()]);
        if(spc.getAction() == 2) {
            ((Binding)spc.readCurrentPolicyEngineData()).setIncludeTimestamp(true);
        }
		return new Boolean(true);
	}

	public Object doEncryptBeforeSigning(SecurityProcessorContext spc) {
		log.debug("Processing "
				+ spc.readCurrentSecurityToken().getTokenName() + ": "
				+ SecurityProcessorContext.ACTION_NAMES[spc.getAction()]);
		return new Boolean(true);
	}

	public Object doEncryptSignature(SecurityProcessorContext spc) {
		log.debug("Processing "
				+ spc.readCurrentSecurityToken().getTokenName() + ": "
				+ SecurityProcessorContext.ACTION_NAMES[spc.getAction()]);
		return new Boolean(true);
	}

	public Object doProtectTokens(SecurityProcessorContext spc) {
		log.debug("Processing "
				+ spc.readCurrentSecurityToken().getTokenName() + ": "
				+ SecurityProcessorContext.ACTION_NAMES[spc.getAction()]);
		return new Boolean(true);
	}

	public Object doOnlySignEntireHeadersAndBody(SecurityProcessorContext spc) {
		log.debug("Processing "
				+ spc.readCurrentSecurityToken().getTokenName() + ": "
				+ SecurityProcessorContext.ACTION_NAMES[spc.getAction()]);
		return new Boolean(true);
	}
}
