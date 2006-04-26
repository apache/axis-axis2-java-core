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
import org.apache.ws.security.policy.model.AlgorithmSuite;
import org.apache.ws.security.policy.model.AlgorithmWrapper;
import org.apache.ws.security.policy.parser.SecurityPolicy;
import org.apache.ws.security.policy.parser.SecurityPolicyToken;
import org.apache.ws.security.policy.parser.SecurityProcessorContext;


public class AlgorithmSuiteProcessor {
    
    private Log log = LogFactory.getLog(getClass());
    
    private boolean initializedAlgorithmSuite = false;

	/**
	 * Intialize the AlgorithmSuite complex token.
	 * 
	 * This method creates a copy of the AlgorithmSuite token and sets the
	 * handler object to the copy. Then it creates copies of the child tokens
	 * that are allowed for AlgorithmSuite. These tokens are:
	 * 
	 * These copies are also initialized with the handler object and then set as
	 * child tokens of AlgorithmSuite.
	 * 
	 * <p/> The handler object that must contain the methods
	 * <code>doAlgorithmSuite</code>.
	 * 
	 * @param spt
	 *            The token that will hold the child tokens.
	 * @throws NoSuchMethodException
	 */

	private void initializeAlgorithmSuite(SecurityPolicyToken spt)
			throws NoSuchMethodException {

		SecurityPolicyToken tmpSpt;

		tmpSpt = SecurityPolicy.basic256.copy();
		tmpSpt.setProcessTokenMethod(this);
		spt.setChildToken(tmpSpt);

		tmpSpt = SecurityPolicy.basic192.copy();
		tmpSpt.setProcessTokenMethod(this);
		spt.setChildToken(tmpSpt);

		tmpSpt = SecurityPolicy.basic128.copy();
		tmpSpt.setProcessTokenMethod(this);
		spt.setChildToken(tmpSpt);

		tmpSpt = SecurityPolicy.tripleDes.copy();
		tmpSpt.setProcessTokenMethod(this);
		spt.setChildToken(tmpSpt);

		tmpSpt = SecurityPolicy.basic256Rsa15.copy();
		tmpSpt.setProcessTokenMethod(this);
		spt.setChildToken(tmpSpt);

		tmpSpt = SecurityPolicy.basic192Rsa15.copy();
		tmpSpt.setProcessTokenMethod(this);
		spt.setChildToken(tmpSpt);

		tmpSpt = SecurityPolicy.basic128Rsa15.copy();
		tmpSpt.setProcessTokenMethod(this);
		spt.setChildToken(tmpSpt);

		tmpSpt = SecurityPolicy.tripleDesRsa15.copy();
		tmpSpt.setProcessTokenMethod(this);
		spt.setChildToken(tmpSpt);

		tmpSpt = SecurityPolicy.basic256Sha256.copy();
		tmpSpt.setProcessTokenMethod(this);
		spt.setChildToken(tmpSpt);

		tmpSpt = SecurityPolicy.basic192Sha256.copy();
		tmpSpt.setProcessTokenMethod(this);
		spt.setChildToken(tmpSpt);

		tmpSpt = SecurityPolicy.basic128Sha256.copy();
		tmpSpt.setProcessTokenMethod(this);
		spt.setChildToken(tmpSpt);

		tmpSpt = SecurityPolicy.tripleDesSha256.copy();
		tmpSpt.setProcessTokenMethod(this);
		spt.setChildToken(tmpSpt);

		tmpSpt = SecurityPolicy.basic256Sha256Rsa15.copy();
		tmpSpt.setProcessTokenMethod(this);
		spt.setChildToken(tmpSpt);

		tmpSpt = SecurityPolicy.basic192Sha256Rsa15.copy();
		tmpSpt.setProcessTokenMethod(this);
		spt.setChildToken(tmpSpt);

		tmpSpt = SecurityPolicy.basic128Sha256Rsa15.copy();
		tmpSpt.setProcessTokenMethod(this);
		spt.setChildToken(tmpSpt);

		tmpSpt = SecurityPolicy.tripleDesSha256Rsa15.copy();
		tmpSpt.setProcessTokenMethod(this);
		spt.setChildToken(tmpSpt);

		tmpSpt = SecurityPolicy.inclusiveC14N.copy();
		tmpSpt.setProcessTokenMethod(this);
		spt.setChildToken(tmpSpt);

		tmpSpt = SecurityPolicy.soapNormalization10.copy();
		tmpSpt.setProcessTokenMethod(this);
		spt.setChildToken(tmpSpt);

		tmpSpt = SecurityPolicy.strTransform10.copy();
		tmpSpt.setProcessTokenMethod(this);
		spt.setChildToken(tmpSpt);

		tmpSpt = SecurityPolicy.xPath10.copy();
		tmpSpt.setProcessTokenMethod(this);
		spt.setChildToken(tmpSpt);

		tmpSpt = SecurityPolicy.xPathFilter20.copy();
		tmpSpt.setProcessTokenMethod(this);
		spt.setChildToken(tmpSpt);
	}

	public Object doAlgorithmSuite(SecurityProcessorContext spc) {
		log.debug("Processing "
				+ spc.readCurrentSecurityToken().getTokenName() + ": "
				+ SecurityProcessorContext.ACTION_NAMES[spc.getAction()]);

		SecurityPolicyToken spt = spc.readCurrentSecurityToken();
		switch (spc.getAction()) {

		case SecurityProcessorContext.START:
			if (!initializedAlgorithmSuite) {
				try {
					initializeAlgorithmSuite(spt);
					initializedAlgorithmSuite = true;
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

	public Object doBasic256(SecurityProcessorContext spc) {
		log.debug("Processing "
				+ spc.readCurrentSecurityToken().getTokenName() + ": "
				+ SecurityProcessorContext.ACTION_NAMES[spc.getAction()]);
		return this.setAlgoGroup(spc);
	}

	public Object doBasic192(SecurityProcessorContext spc) {
		log.debug("Processing "
				+ spc.readCurrentSecurityToken().getTokenName() + ": "
				+ SecurityProcessorContext.ACTION_NAMES[spc.getAction()]);
        return this.setAlgoGroup(spc);
	}

	public Object doBasic128(SecurityProcessorContext spc) {
		log.debug("Processing "
				+ spc.readCurrentSecurityToken().getTokenName() + ": "
				+ SecurityProcessorContext.ACTION_NAMES[spc.getAction()]);
        return this.setAlgoGroup(spc);
	}

	public Object doTripleDes(SecurityProcessorContext spc) {
		log.debug("Processing "
				+ spc.readCurrentSecurityToken().getTokenName() + ": "
				+ SecurityProcessorContext.ACTION_NAMES[spc.getAction()]);
        return this.setAlgoGroup(spc);
	}

	public Object doBasic256Rsa15(SecurityProcessorContext spc) {
		log.debug("Processing "
				+ spc.readCurrentSecurityToken().getTokenName() + ": "
				+ SecurityProcessorContext.ACTION_NAMES[spc.getAction()]);
        return this.setAlgoGroup(spc);
	}

	public Object doBasic192Rsa15(SecurityProcessorContext spc) {
		log.debug("Processing "
				+ spc.readCurrentSecurityToken().getTokenName() + ": "
				+ SecurityProcessorContext.ACTION_NAMES[spc.getAction()]);
        return this.setAlgoGroup(spc);
	}

	public Object doBasic128Rsa15(SecurityProcessorContext spc) {
		log.debug("Processing "
				+ spc.readCurrentSecurityToken().getTokenName() + ": "
				+ SecurityProcessorContext.ACTION_NAMES[spc.getAction()]);
        return this.setAlgoGroup(spc);
	}

	public Object doTripleDesRsa15(SecurityProcessorContext spc) {
		log.debug("Processing "
				+ spc.readCurrentSecurityToken().getTokenName() + ": "
				+ SecurityProcessorContext.ACTION_NAMES[spc.getAction()]);
        return this.setAlgoGroup(spc);
	}

	public Object doBasic256Sha256(SecurityProcessorContext spc) {
		log.debug("Processing "
				+ spc.readCurrentSecurityToken().getTokenName() + ": "
				+ SecurityProcessorContext.ACTION_NAMES[spc.getAction()]);
        return this.setAlgoGroup(spc);
	}

	public Object doBasic192Sha256(SecurityProcessorContext spc) {
		log.debug("Processing "
				+ spc.readCurrentSecurityToken().getTokenName() + ": "
				+ SecurityProcessorContext.ACTION_NAMES[spc.getAction()]);
        return this.setAlgoGroup(spc);
	}

	public Object doBasic128Sha256(SecurityProcessorContext spc) {
		log.debug("Processing "
				+ spc.readCurrentSecurityToken().getTokenName() + ": "
				+ SecurityProcessorContext.ACTION_NAMES[spc.getAction()]);
        return this.setAlgoGroup(spc);
	}

	public Object doTripleDesSha256(SecurityProcessorContext spc) {
		log.debug("Processing "
				+ spc.readCurrentSecurityToken().getTokenName() + ": "
				+ SecurityProcessorContext.ACTION_NAMES[spc.getAction()]);
        return this.setAlgoGroup(spc);
	}

	public Object doBasic256Sha256Rsa15(SecurityProcessorContext spc) {
		log.debug("Processing "
				+ spc.readCurrentSecurityToken().getTokenName() + ": "
				+ SecurityProcessorContext.ACTION_NAMES[spc.getAction()]);
		return this.setAlgoGroup(spc);
	}

	public Object doBasic192Sha256Rsa15(SecurityProcessorContext spc) {
		log.debug("Processing "
				+ spc.readCurrentSecurityToken().getTokenName() + ": "
				+ SecurityProcessorContext.ACTION_NAMES[spc.getAction()]);
		return this.setAlgoGroup(spc);
	}

	public Object doBasic128Sha256Rsa15(SecurityProcessorContext spc) {
		log.debug("Processing "
				+ spc.readCurrentSecurityToken().getTokenName() + ": "
				+ SecurityProcessorContext.ACTION_NAMES[spc.getAction()]);
        
		return this.setAlgoGroup(spc);
	}

	public Object doTripleDesSha256Rsa15(SecurityProcessorContext spc) {
		log.debug("Processing "
				+ spc.readCurrentSecurityToken().getTokenName() + ": "
				+ SecurityProcessorContext.ACTION_NAMES[spc.getAction()]);
		return this.setAlgoGroup(spc);
	}

	public Object doInclusiveC14N(SecurityProcessorContext spc) {
		log.debug("Processing "
				+ spc.readCurrentSecurityToken().getTokenName() + ": "
				+ SecurityProcessorContext.ACTION_NAMES[spc.getAction()]);
        AlgorithmSuite algoSuite = (AlgorithmSuite)spc.readCurrentPolicyEngineData();
        if(spc.getAction() == SecurityProcessorContext.START) {
            try {
                algoSuite.setC14n(Constants.C14N);
            } catch (WSSPolicyException e) {
                log.error(e.getMessage(), e);
                return new Boolean(false);
            }
        }
        return new Boolean(true);
	}

	public Object doSoapNormalization10(SecurityProcessorContext spc) {
		log.debug("Processing "
				+ spc.readCurrentSecurityToken().getTokenName() + ": "
				+ SecurityProcessorContext.ACTION_NAMES[spc.getAction()]);
        AlgorithmSuite algoSuite = (AlgorithmSuite)spc.readCurrentPolicyEngineData();
        if(spc.getAction() == SecurityProcessorContext.START) {
            try {
                algoSuite.setSoapNormalization(Constants.SNT);
            } catch (WSSPolicyException e) {
                log.error(e.getMessage(), e);
                return new Boolean(false);
            }
        }
		return new Boolean(true);
	}

	public Object doStrTransform10(SecurityProcessorContext spc) {
		log.debug("Processing "
				+ spc.readCurrentSecurityToken().getTokenName() + ": "
				+ SecurityProcessorContext.ACTION_NAMES[spc.getAction()]);
        AlgorithmSuite algoSuite = (AlgorithmSuite)spc.readCurrentPolicyEngineData();
        if(spc.getAction() == SecurityProcessorContext.START) {
            try {
                algoSuite.setStrTransform(Constants.STRT10);
            } catch (WSSPolicyException e) {
                log.error(e.getMessage(), e);
                return new Boolean(false);
            }
        }
		return new Boolean(true);
	}

	public Object doXPath10(SecurityProcessorContext spc) {
		log.debug("Processing "
				+ spc.readCurrentSecurityToken().getTokenName() + ": "
				+ SecurityProcessorContext.ACTION_NAMES[spc.getAction()]);
        AlgorithmSuite algoSuite = (AlgorithmSuite)spc.readCurrentPolicyEngineData();
        if(spc.getAction() == SecurityProcessorContext.START) {
            try {
                algoSuite.setXPath(Constants.XPATH);
            } catch (WSSPolicyException e) {
                log.error(e.getMessage(), e);
                return new Boolean(false);
            }
        }
		return new Boolean(true);
	}

	public Object doXPathFilter20(SecurityProcessorContext spc) {
		log.debug("Processing "
				+ spc.readCurrentSecurityToken().getTokenName() + ": "
				+ SecurityProcessorContext.ACTION_NAMES[spc.getAction()]);
        AlgorithmSuite algoSuite = (AlgorithmSuite)spc.readCurrentPolicyEngineData();
        if(spc.getAction() == SecurityProcessorContext.START) {
            try {
                algoSuite.setXPath(Constants.XPATH20);
            } catch (WSSPolicyException e) {
                log.error(e.getMessage(), e);
                return new Boolean(false);
            }
        }
		return new Boolean(true);
	}

    private Boolean setAlgoGroup(SecurityProcessorContext spc) {
        if(spc.getAction() == SecurityProcessorContext.START) {
            try {
                AlgorithmSuite algoSuite = (AlgorithmSuite)spc.readCurrentPolicyEngineData();
                algoSuite.setAlgorithmSuite(spc.getAssertion().getName().getLocalPart());
                ((AlgorithmWrapper)spc.readPreviousPolicyEngineData()).setAlgorithmSuite(algoSuite);
            } catch (WSSPolicyException e) {
                log.error(e.getMessage(), e);
                return new Boolean(false);
            }
        }    
        return new Boolean(true);
    }
    
}
