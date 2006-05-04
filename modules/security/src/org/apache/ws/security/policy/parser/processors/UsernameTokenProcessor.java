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
import org.apache.ws.security.policy.Constants;
import org.apache.ws.security.policy.WSSPolicyException;
import org.apache.ws.security.policy.model.TokenWrapper;
import org.apache.ws.security.policy.model.UsernameToken;
import org.apache.ws.security.policy.parser.SecurityPolicy;
import org.apache.ws.security.policy.parser.SecurityPolicyToken;
import org.apache.ws.security.policy.parser.SecurityProcessorContext;

import javax.xml.namespace.QName;


public class UsernameTokenProcessor {
    
	private static final Log log = LogFactory.getLog(UsernameTokenProcessor.class);

	private boolean initializedUsernameToken = false;

	/**
	 * Intialize the UsernameToken complex token.
	 * 
	 * This method creates copies of the child tokens that are allowed for
	 * UsernameToken. These tokens are WssUsernameToken10 and
	 * WssUsernameToken11. These copies are also initialized with the handler
	 * object and then set as child tokens of UsernameToken.
	 * 
	 * <p/> The handler object must define the methods
	 * <code>doWssUsernameToken10, doWssUsernameToken11</code>.
	 * 
	 * @param spt
	 *            The token that will hold the child tokens.
	 * @throws NoSuchMethodException
	 */
	public void initializeUsernameToken(SecurityPolicyToken spt)
			throws NoSuchMethodException {

		SecurityPolicyToken tmpSpt = SecurityPolicy.wssUsernameToken10.copy();
		tmpSpt.setProcessTokenMethod(this);
		spt.setChildToken(tmpSpt);

		tmpSpt = SecurityPolicy.wssUsernameToken11.copy();
		tmpSpt.setProcessTokenMethod(this);
		spt.setChildToken(tmpSpt);
	}

	public Object doUsernameToken(SecurityProcessorContext spc) {
		log.debug("Processing "
				+ spc.readCurrentSecurityToken().getTokenName() + ": "
				+ SecurityProcessorContext.ACTION_NAMES[spc.getAction()]);

		SecurityPolicyToken spt = spc.readCurrentSecurityToken();
		switch (spc.getAction()) {

		case SecurityProcessorContext.START:
			if (!initializedUsernameToken) {
				try {
					initializeUsernameToken(spt);
                    UsernameToken unt = (UsernameToken)spc.readCurrentPolicyEngineData();
                    
                    //Get the includeToken attr info
                    String includetokenUri = spc.getAssertion().getAttribute(
                            new QName(Constants.SP_NS,
                                    Constants.ATTR_INCLUDE_TOKEN));
                    try {
                        if(includetokenUri != null) { //since its optional
                            unt.setInclusion(includetokenUri);
                        }
                        ((TokenWrapper)spc.readPreviousPolicyEngineData()).setToken(unt);
                    } catch (WSSPolicyException e) {
                        log.error(e.getMessage(), e);
                        return new Boolean(false);
                    }
					initializedUsernameToken = true;
				} catch (NoSuchMethodException e) {
                    log.error(e.getMessage(), e);
					return new Boolean(false);
				}
			}
			log.debug(spt.getTokenName());
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

	public Object doWssUsernameToken10(SecurityProcessorContext spc) {
		log.debug("Processing wssUsernameToken10");
        if(spc.getAction() == SecurityProcessorContext.START) {
            ((UsernameToken)spc.readCurrentPolicyEngineData()).setUseUTProfile11(false);
        }
		return new Boolean(true);
	}

	public Object doWssUsernameToken11(SecurityProcessorContext spc) {
		log.debug("Processing wssUsernameToken11");
        if(spc.getAction() == SecurityProcessorContext.START) {
            ((UsernameToken)spc.readCurrentPolicyEngineData()).setUseUTProfile11(true);
        }
		return new Boolean(true);
	}

}
