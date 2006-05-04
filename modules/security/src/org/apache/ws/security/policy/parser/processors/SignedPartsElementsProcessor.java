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
import org.apache.ws.security.policy.model.Header;
import org.apache.ws.security.policy.model.PolicyEngineData;
import org.apache.ws.security.policy.model.SignedEncryptedElements;
import org.apache.ws.security.policy.model.SignedEncryptedParts;
import org.apache.ws.security.policy.model.SupportingToken;
import org.apache.ws.security.policy.parser.SecurityPolicy;
import org.apache.ws.security.policy.parser.SecurityPolicyToken;
import org.apache.ws.security.policy.parser.SecurityProcessorContext;

import javax.xml.namespace.QName;


public class SignedPartsElementsProcessor {
    
	private static final Log log = LogFactory.getLog(SignedPartsElementsProcessor.class);
    
	private boolean initializedSignedParts = false;

	private boolean initializedSignedElements = false;

	public SignedPartsElementsProcessor() {
	}

	/**
	 * Intialize the SignedParts complex token.
	 * 
	 * This method creates copies of the child tokens that are allowed for
	 * SignedParts. These tokens are Body and Header. These copies are
	 * initialized with handler object and then set as child tokens of
	 * SignedParts. <p/> The handler object must define the methods
	 * <code>doSignedParts, doBody, doHeader</code>.
	 * 
	 * @param spt
	 *            The token that will hold the child tokens.
	 * @throws NoSuchMethodException
	 */
	private void initializeSignedParts(SecurityPolicyToken spt)
			throws NoSuchMethodException {
		SecurityPolicyToken tmpSpt = SecurityPolicy.body.copy();
		tmpSpt.setProcessTokenMethod(this);
		spt.setChildToken(tmpSpt);

		tmpSpt = SecurityPolicy.header.copy();
		tmpSpt.setProcessTokenMethod(this);
		spt.setChildToken(tmpSpt);

	}

	/**
	 * Intialize the SignedElements complex token.
	 * 
	 * This method creates a copy of the child token that is allowed for
	 * SignedElements. The token is XPath. This copy is initialized with a
	 * handler object and then set as child token of SignedElements. <p/> The
	 * handler object must define the method <code>doXPath</code>.
	 * 
	 * @param spt
	 *            The token that will hold the child tokens.
	 * @throws NoSuchMethodException
	 */
	private void initializeSignedElements(SecurityPolicyToken spt)
			throws NoSuchMethodException {
		SecurityPolicyToken tmpSpt = SecurityPolicy.xPath.copy();
		tmpSpt.setProcessTokenMethod(this);
		spt.setChildToken(tmpSpt);
	}

	public Object doSignedParts(SecurityProcessorContext spc) {
		log.debug("Processing "
				+ spc.readCurrentSecurityToken().getTokenName() + ": "
				+ SecurityProcessorContext.ACTION_NAMES[spc.getAction()]);

		SecurityPolicyToken spt = spc.readCurrentSecurityToken();

		switch (spc.getAction()) {

		case SecurityProcessorContext.START:
			if (!initializedSignedParts) {
				try {
					initializeSignedParts(spt);
                    SignedEncryptedParts parts = (SignedEncryptedParts) spc
                            .readCurrentPolicyEngineData();
                    PolicyEngineData parent = spc
                            .readPreviousPolicyEngineData();
                    if (parent instanceof SupportingToken) {
                        // Parent is a supporting token
                        ((SupportingToken) parent).setSignedParts(parts);
                    }
					initializedSignedParts = true;
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

	public Object doSignedElements(SecurityProcessorContext spc) {
		log.debug("Processing "
				+ spc.readCurrentSecurityToken().getTokenName() + ": "
				+ SecurityProcessorContext.ACTION_NAMES[spc.getAction()]);

		SecurityPolicyToken spt = spc.readCurrentSecurityToken();

		switch (spc.getAction()) {

		case SecurityProcessorContext.START:
			if (!initializedSignedElements) {
				try {
					initializeSignedElements(spt);
                    SignedEncryptedElements elements = (SignedEncryptedElements) spc
                            .readCurrentPolicyEngineData();
                    PolicyEngineData parent = spc
                            .readPreviousPolicyEngineData();
                    if (parent instanceof SupportingToken) {
                        // Parent is a supporting token
                        ((SupportingToken) parent)
                                .setSignedElements(elements);
                    }
					initializedSignedElements = true;
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

	public Object doBody(SecurityProcessorContext spc) {
		log.debug("Processing "
				+ spc.readCurrentSecurityToken().getTokenName() + ": "
				+ SecurityProcessorContext.ACTION_NAMES[spc.getAction()]);
            if(spc.getAction() == SecurityProcessorContext.START) {
                ((SignedEncryptedParts)spc.readCurrentPolicyEngineData()).setBody(true);
            }
		return new Boolean(true);
	}

	public Object doHeader(SecurityProcessorContext spc) {
		log.debug("Processing "
				+ spc.readCurrentSecurityToken().getTokenName() + ": "
				+ SecurityProcessorContext.ACTION_NAMES[spc.getAction()]);
        if(spc.getAction() == SecurityProcessorContext.START) {
            //Extract the sp:Header/@Name and sp:Header/@Namespace attrs
            //And create a Header
            Header header = new Header();
            header.setName(spc.getAssertion().getAttribute(new QName("Name")));
            header.setNamespace(spc.getAssertion().getAttribute(new QName("Namespace")));
            ((SignedEncryptedParts)spc.readCurrentPolicyEngineData()).addHeader(header);
        }
		return new Boolean(true);
	}

	public Object doXPath(SecurityProcessorContext spc) {
		log.debug("Processing "
				+ spc.readCurrentSecurityToken().getTokenName() + ": "
				+ SecurityProcessorContext.ACTION_NAMES[spc.getAction()]);
        if(spc.getAction() == SecurityProcessorContext.START) {
            ((SignedEncryptedElements) spc.readCurrentPolicyEngineData())
                    .addXPathExpression(spc.getAssertion().getStrValue());
        }
		return new Boolean(true);
	}

}
