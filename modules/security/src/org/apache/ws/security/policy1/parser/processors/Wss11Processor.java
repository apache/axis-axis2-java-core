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
package org.apache.ws.security.policy1.parser.processors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ws.policy.PrimitiveAssertion;
import org.apache.ws.security.policy1.model.Wss11;
import org.apache.ws.security.policy1.parser.SecurityPolicy;
import org.apache.ws.security.policy1.parser.SecurityPolicyToken;
import org.apache.ws.security.policy1.parser.SecurityProcessorContext;


public class Wss11Processor {

	private static final Log log = LogFactory.getLog(Wss11Processor.class);
    
	private boolean initializedWss11 = false;

	/**
	 * Intialize the Wss11 complex token.
	 * 
	 * This method creates a copy of the Wss11 token and sets the handler object
	 * to the copy. Then it creates copies of the child tokens that are allowed
	 * for Wss10. These tokens are:
	 * 
	 * These copies are also initialized with the handler object and then set as
	 * child tokens of Wss11.
	 * 
	 * <p/> The handler object that must contain the methods
	 * <code>doWss10</code>.
	 * 
	 * @param spt
	 *            The token that will hold the child tokens.
	 * @throws NoSuchMethodException
	 */
	public void initializeWss11(SecurityPolicyToken spt)
			throws NoSuchMethodException {
		SecurityPolicyToken tmpSpt = SecurityPolicy.mustSupportRefKeyIdentifier
				.copy();
		tmpSpt.setProcessTokenMethod(this);
		spt.setChildToken(tmpSpt);

		tmpSpt = SecurityPolicy.mustSupportRefIssuerSerial.copy();
		tmpSpt.setProcessTokenMethod(this);
		spt.setChildToken(tmpSpt);

		tmpSpt = SecurityPolicy.mustSupportRefExternalUri.copy();
		tmpSpt.setProcessTokenMethod(this);
		spt.setChildToken(tmpSpt);

		tmpSpt = SecurityPolicy.mustSupportRefEmbeddedToken.copy();
		tmpSpt.setProcessTokenMethod(this);
		spt.setChildToken(tmpSpt);

		tmpSpt = SecurityPolicy.mustSupportRefThumbprint.copy();
		tmpSpt.setProcessTokenMethod(this);
		spt.setChildToken(tmpSpt);

		tmpSpt = SecurityPolicy.mustSupportRefEncryptedKey.copy();
		tmpSpt.setProcessTokenMethod(this);
		spt.setChildToken(tmpSpt);

		tmpSpt = SecurityPolicy.requireSignatureConfirmation.copy();
		tmpSpt.setProcessTokenMethod(this);
		spt.setChildToken(tmpSpt);
	}

	public Object doWss11(SecurityProcessorContext spc) {
		log.debug("Processing "
				+ spc.readCurrentSecurityToken().getTokenName() + ": "
				+ SecurityProcessorContext.ACTION_NAMES[spc.getAction()]);

		SecurityPolicyToken spt = spc.readCurrentSecurityToken();

		switch (spc.getAction()) {

		case SecurityProcessorContext.START:
			if (!initializedWss11) {
				try {
					initializeWss11(spt);
					initializedWss11 = true;
				} catch (NoSuchMethodException e) {
                    log.error(e.getMessage(), e);
					return Boolean.FALSE;
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
		return Boolean.TRUE;
	}
	
	public Object doMustSupportRefKeyIdentifier(SecurityProcessorContext spc) {
		log.debug("Processing "
				+ spc.readCurrentSecurityToken().getTokenName() + ": "
				+ SecurityProcessorContext.ACTION_NAMES[spc.getAction()]);
        if(spc.getAction() == SecurityProcessorContext.START) {
            ((Wss11)spc.readCurrentPolicyEngineData()).setMustSupportRefKeyIdentifier(true);
        }
        return Boolean.TRUE;
	}

	public Object doMustSupportRefIssuerSerial(SecurityProcessorContext spc) {
		log.debug("Processing "
				+ spc.readCurrentSecurityToken().getTokenName() + ": "
				+ SecurityProcessorContext.ACTION_NAMES[spc.getAction()]);
        if(spc.getAction() == SecurityProcessorContext.START) {
            ((Wss11)spc.readCurrentPolicyEngineData()).setMustSupportRefIssuerSerial(true);
        }
        return Boolean.TRUE;
	}

	public Object doMustSupportRefExternalURI(SecurityProcessorContext spc) {
		log.debug("Processing "
				+ spc.readCurrentSecurityToken().getTokenName() + ": "
				+ SecurityProcessorContext.ACTION_NAMES[spc.getAction()]);
        if(spc.getAction() == SecurityProcessorContext.START) {
            ((Wss11)spc.readCurrentPolicyEngineData()).setMustSupportRefExternalURI(true);
        }
        return Boolean.TRUE;
	}

	public Object doMustSupportRefEmbeddedToken(SecurityProcessorContext spc) {
		log.debug("Processing "
				+ spc.readCurrentSecurityToken().getTokenName() + ": "
				+ SecurityProcessorContext.ACTION_NAMES[spc.getAction()]);
        if(spc.getAction() == SecurityProcessorContext.START) {
            ((Wss11)spc.readCurrentPolicyEngineData()).setMustSupportRefEmbeddedToken(true);
        }
        return Boolean.TRUE;
	}

	public Object doMustSupportRefThumbprint(SecurityProcessorContext spc) {
		log.debug("Processing "
				+ spc.readCurrentSecurityToken().getTokenName() + ": "
				+ SecurityProcessorContext.ACTION_NAMES[spc.getAction()]);
        if(spc.getAction() == SecurityProcessorContext.START) {
            ((Wss11)spc.readCurrentPolicyEngineData()).setMustSupportRefThumbprint(true);
        }
        return Boolean.TRUE;
	}

	public Object doMustSupportRefEncryptedKey(SecurityProcessorContext spc) {
		log.debug("Processing "
				+ spc.readCurrentSecurityToken().getTokenName() + ": "
				+ SecurityProcessorContext.ACTION_NAMES[spc.getAction()]);
        if(spc.getAction() == SecurityProcessorContext.START) {
            ((Wss11)spc.readCurrentPolicyEngineData()).setMustSupportRefEncryptedKey(true);
        }
		return Boolean.TRUE;
	}

	public Object doRequireSignatureConfirmation(SecurityProcessorContext spc) {
		log.debug("Processing "
				+ spc.readCurrentSecurityToken().getTokenName() + ": "
				+ SecurityProcessorContext.ACTION_NAMES[spc.getAction()]);
        if(spc.getAction() == SecurityProcessorContext.START) {
            ((Wss11)spc.readCurrentPolicyEngineData()).setRequireSignatureConfirmation(true);
        }
		return Boolean.TRUE;
	}
}
