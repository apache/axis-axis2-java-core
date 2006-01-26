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
import org.apache.ws.security.policy.model.Wss10;
import org.apache.ws.security.policy.parser.SecurityPolicy;
import org.apache.ws.security.policy.parser.SecurityPolicyToken;
import org.apache.ws.security.policy.parser.SecurityProcessorContext;


/**
 * @author Werner Dittmann (werner@apache.org)
 * 
 */
public class Wss10Processor {

    private Log log = LogFactory.getLog(getClass());
    
	private boolean initializedWss10 = false;

	/**
	 * Intialize the Wss10 complex token.
	 * 
	 * This method creates a copy of the Wss10 token and sets the handler object
	 * to the copy. Then it creates copies of the child tokens that are allowed
	 * for Wss10. These tokens are:
	 * 
	 * These copies are also initialized with the handler object and then set as
	 * child tokens of Wss10.
	 * 
	 * <p/> The handler object that must contain the methods
	 * <code>doWss10</code>.
	 * 
	 * @param spt
	 *            The token that will hold the child tokens.
	 * @throws NoSuchMethodException
	 */
	public void initializeWss10(SecurityPolicyToken spt)
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
	}

	
	public Object doWss10(SecurityProcessorContext spc) {
		log.debug("Processing "
				+ spc.readCurrentSecurityToken().getTokenName() + ": "
				+ SecurityProcessorContext.ACTION_NAMES[spc.getAction()]);

		SecurityPolicyToken spt = spc.readCurrentSecurityToken();

		switch (spc.getAction()) {

		case SecurityProcessorContext.START:
			if (!initializedWss10) {
				try {
					initializeWss10(spt);
					initializedWss10 = true;
				} catch (NoSuchMethodException e) {
                    log.error(e.getMessage(), e);
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
	
	public Object doMustSupportRefKeyIdentifier(SecurityProcessorContext spc) {
		log.debug("Processing "
				+ spc.readCurrentSecurityToken().getTokenName() + ": "
				+ SecurityProcessorContext.ACTION_NAMES[spc.getAction()]);
        if(spc.getAction() == SecurityProcessorContext.START) {
            ((Wss10)spc.readCurrentPolicyEngineData()).setMustSupportRefKeyIdentifier(true);
        }
		return new Boolean(true);
	}

	public Object doMustSupportRefIssuerSerial(SecurityProcessorContext spc) {
		log.debug("Processing "
				+ spc.readCurrentSecurityToken().getTokenName() + ": "
				+ SecurityProcessorContext.ACTION_NAMES[spc.getAction()]);
        if(spc.getAction() == SecurityProcessorContext.START) {
            ((Wss10)spc.readCurrentPolicyEngineData()).setMustSupportRefIssuerSerial(true);
        }
        return new Boolean(true);
	}

	public Object doMustSupportRefExternalURI(SecurityProcessorContext spc) {
		log.debug("Processing "
				+ spc.readCurrentSecurityToken().getTokenName() + ": "
				+ SecurityProcessorContext.ACTION_NAMES[spc.getAction()]);
        if(spc.getAction() == SecurityProcessorContext.START) {
            ((Wss10)spc.readCurrentPolicyEngineData()).setMustSupportRefExternalURI(true);
        }
        return new Boolean(true);
	}

	public Object doMustSupportRefEmbeddedToken(SecurityProcessorContext spc) {
		log.debug("Processing "
				+ spc.readCurrentSecurityToken().getTokenName() + ": "
				+ SecurityProcessorContext.ACTION_NAMES[spc.getAction()]);
        if(spc.getAction() == SecurityProcessorContext.START) {
            ((Wss10)spc.readCurrentPolicyEngineData()).setMustSupportRefEmbeddedToken(true);
        }
        return new Boolean(true);
	}
}
