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
import org.apache.ws.security.policy.WSSPolicyException;
import org.apache.ws.security.policy.model.Binding;
import org.apache.ws.security.policy.model.Layout;
import org.apache.ws.security.policy.parser.SecurityPolicy;
import org.apache.ws.security.policy.parser.SecurityPolicyToken;
import org.apache.ws.security.policy.parser.SecurityProcessorContext;


/**
 * @author Werner Dittmann (werner@apache.org)
 * 
 */
public class LayoutProcessor {

    private Log log = LogFactory.getLog(getClass());

    private boolean initializedLayout = false;
    
	/**
	 * Intialize the Layout complex token.
	 * 
	 * This method creates a copy of the Layout token and sets the handler
	 * object to the copy. Then it creates copies of the child tokens that are
	 * allowed for Layout. These tokens are:
	 * 
	 * These copies are also initialized with the handler object and then set as
	 * child tokens of Layout.
	 * 
	 * <p/> The handler object that must contain the methods
	 * <code>doLayout</code>.
	 * 
	 * @param spt
	 *            The token that will hold the child tokens.
	 * @throws NoSuchMethodException
	 */
	private void initializeLayout(SecurityPolicyToken spt)
			throws NoSuchMethodException {

		SecurityPolicyToken tmpSpt = SecurityPolicy.strict.copy();
		tmpSpt.setProcessTokenMethod(this);
		spt.setChildToken(tmpSpt);

		tmpSpt = SecurityPolicy.lax.copy();
		tmpSpt.setProcessTokenMethod(this);
		spt.setChildToken(tmpSpt);

		tmpSpt = SecurityPolicy.laxTsFirst.copy();
		tmpSpt.setProcessTokenMethod(this);
		spt.setChildToken(tmpSpt);

		tmpSpt = SecurityPolicy.laxTsLast.copy();
		tmpSpt.setProcessTokenMethod(this);
		spt.setChildToken(tmpSpt);
	}

	public Object doLayout(SecurityProcessorContext spc) {
		log.debug("Processing "
				+ spc.readCurrentSecurityToken().getTokenName() + ": "
				+ SecurityProcessorContext.ACTION_NAMES[spc.getAction()]);

		SecurityPolicyToken spt = spc.readCurrentSecurityToken();
		switch (spc.getAction()) {

		case SecurityProcessorContext.START:
			if (!initializedLayout) {
				try {
					initializeLayout(spt);
					initializedLayout = true;
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

	public Object doStrict(SecurityProcessorContext spc) {
		log.debug("Processing "
				+ spc.readCurrentSecurityToken().getTokenName() + ": "
				+ SecurityProcessorContext.ACTION_NAMES[spc.getAction()]);
        try {
            if(spc.getAction() == SecurityProcessorContext.START) {
                Layout layout = (Layout) spc.readCurrentPolicyEngineData();
                layout.setValue(spc.getAssertion().getName().getLocalPart());
                ((Binding)spc.readPreviousPolicyEngineData()).setLayout(layout);
            }
        } catch (WSSPolicyException e) {
            log.error(e.getMessage(), e);
            return new Boolean(false);
        }
		return new Boolean(true);
	}

	public Object doLax(SecurityProcessorContext spc) {
		log.debug("Processing "
				+ spc.readCurrentSecurityToken().getTokenName() + ": "
				+ SecurityProcessorContext.ACTION_NAMES[spc.getAction()]);
        try {
            if(spc.getAction() == SecurityProcessorContext.START) {
                Layout layout = (Layout) spc.readCurrentPolicyEngineData();
                layout.setValue(spc.getAssertion().getName().getLocalPart());
                ((Binding)spc.readPreviousPolicyEngineData()).setLayout(layout);
            }
        } catch (WSSPolicyException e) {
            log.error(e.getMessage(), e);
            return new Boolean(false);
        }
		return new Boolean(true);
	}

	public Object doLaxTsFirst(SecurityProcessorContext spc) {
		log.debug("Processing "
				+ spc.readCurrentSecurityToken().getTokenName() + ": "
				+ SecurityProcessorContext.ACTION_NAMES[spc.getAction()]);
        try {
            if(spc.getAction() == SecurityProcessorContext.START) {
                Layout layout = (Layout) spc.readCurrentPolicyEngineData();
                layout.setValue(spc.getAssertion().getName().getLocalPart());
                ((Binding)spc.readPreviousPolicyEngineData()).setLayout(layout);
            }
        } catch (WSSPolicyException e) {
            log.error(e.getMessage(), e);
            return new Boolean(false);
        }
		return new Boolean(true);
	}

	public Object doLaxTsLast(SecurityProcessorContext spc) {
		log.debug("Processing "
				+ spc.readCurrentSecurityToken().getTokenName() + ": "
				+ SecurityProcessorContext.ACTION_NAMES[spc.getAction()]);
        try {
            if(spc.getAction() == SecurityProcessorContext.START) {
                Layout layout = (Layout) spc.readCurrentPolicyEngineData();
                layout.setValue(spc.getAssertion().getName().getLocalPart());
                ((Binding)spc.readPreviousPolicyEngineData()).setLayout(layout);
            }
        } catch (WSSPolicyException e) {
            log.error(e.getMessage(), e);
            return new Boolean(false);
        }
		return new Boolean(true);
	}
}
