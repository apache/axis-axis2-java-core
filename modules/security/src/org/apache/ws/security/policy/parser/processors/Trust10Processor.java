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
import org.apache.ws.policy.PrimitiveAssertion;
import org.apache.ws.security.policy.parser.SecurityPolicy;
import org.apache.ws.security.policy.parser.SecurityPolicyToken;
import org.apache.ws.security.policy.parser.SecurityProcessorContext;


/**
 * @author Werner Dittmann (werner@apache.org)
 * 
 */
public class Trust10Processor {
    
    private Log log = LogFactory.getLog(getClass());
    
	private boolean initializedTrust10 = false;

	/**
	 * Intialize the Trust10 complex token.
	 * 
	 * This method creates a copy of the Trust10 token and sets the handler object
	 * to the copy. Then it creates copies of the child tokens that are allowed
	 * for Trust10. These tokens are:
	 * 
	 * These copies are also initialized with the handler object and then set as
	 * child tokens of Trust10.
	 * 
	 * <p/> The handler object that must contain the methods
	 * <code>doTrust10</code>.
	 * 
	 * @param spt
	 *            The token that will hold the child tokens.
	 * @throws NoSuchMethodException
	 */
	public void initializeTrust10(SecurityPolicyToken spt)
			throws NoSuchMethodException {
		SecurityPolicyToken tmpSpt = SecurityPolicy.mustSupportClientChallenge
				.copy();
		tmpSpt.setProcessTokenMethod(this);
		spt.setChildToken(tmpSpt);

		tmpSpt = SecurityPolicy.mustSupportServerChallenge.copy();
		tmpSpt.setProcessTokenMethod(this);
		spt.setChildToken(tmpSpt);

		tmpSpt = SecurityPolicy.requireClientEntropy.copy();
		tmpSpt.setProcessTokenMethod(this);
		spt.setChildToken(tmpSpt);

		tmpSpt = SecurityPolicy.requireServerEntropy.copy();
		tmpSpt.setProcessTokenMethod(this);
		spt.setChildToken(tmpSpt);

		tmpSpt = SecurityPolicy.mustSupportIssuedTokens.copy();
		tmpSpt.setProcessTokenMethod(this);
		spt.setChildToken(tmpSpt);
	}

	public Object doTrust10(SecurityProcessorContext spc) {
		log.debug("Processing "
				+ spc.readCurrentSecurityToken().getTokenName() + ": "
				+ SecurityProcessorContext.ACTION_NAMES[spc.getAction()]);

		SecurityPolicyToken spt = spc.readCurrentSecurityToken();

		switch (spc.getAction()) {

		case SecurityProcessorContext.START:
			if (!initializedTrust10) {
				try {
					initializeTrust10(spt);
					initializedTrust10 = true;
				} catch (NoSuchMethodException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return new Boolean(false);
				}
			}
			PrimitiveAssertion pa = spc.getAssertion();
			String text = pa.getStrValue();
			if (text != null) {
				text = text.trim();
				log.debug("Value: '" + text.toString() + "'");
			}
		case SecurityProcessorContext.COMMIT:
			break;
		case SecurityProcessorContext.ABORT:
			break;
		}
		return new Boolean(true);
	}
	
	public Object doMustSupportClientChallenge(SecurityProcessorContext spc) {
		log.debug("Processing "
				+ spc.readCurrentSecurityToken().getTokenName() + ": "
				+ SecurityProcessorContext.ACTION_NAMES[spc.getAction()]);
		return new Boolean(true);
	}

	public Object doMustSupportServerChallenge(SecurityProcessorContext spc) {
		log.debug("Processing "
				+ spc.readCurrentSecurityToken().getTokenName() + ": "
				+ SecurityProcessorContext.ACTION_NAMES[spc.getAction()]);
		return new Boolean(true);
	}

	public Object doRequireClientEntropy(SecurityProcessorContext spc) {
		log.debug("Processing "
				+ spc.readCurrentSecurityToken().getTokenName() + ": "
				+ SecurityProcessorContext.ACTION_NAMES[spc.getAction()]);
		return new Boolean(true);
	}

	public Object doRequireServerEntropy(SecurityProcessorContext spc) {
		log.debug("Processing "
				+ spc.readCurrentSecurityToken().getTokenName() + ": "
				+ SecurityProcessorContext.ACTION_NAMES[spc.getAction()]);
		return new Boolean(true);
	}

	public Object doMustSupportIssuedTokens(SecurityProcessorContext spc) {
		log.debug("Processing "
				+ spc.readCurrentSecurityToken().getTokenName() + ": "
				+ SecurityProcessorContext.ACTION_NAMES[spc.getAction()]);
		return new Boolean(true);
	}
}
