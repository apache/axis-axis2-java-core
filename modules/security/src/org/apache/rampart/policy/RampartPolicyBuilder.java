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
package org.apache.rampart.policy;

import org.apache.neethi.Assertion;
import org.apache.rampart.policy.model.RampartConfig;
import org.apache.ws.secpolicy.WSSPolicyException;
import org.apache.ws.secpolicy.model.AsymmetricBinding;
import org.apache.ws.secpolicy.model.Binding;
import org.apache.ws.secpolicy.model.EncryptionToken;
import org.apache.ws.secpolicy.model.Header;
import org.apache.ws.secpolicy.model.InitiatorToken;
import org.apache.ws.secpolicy.model.ProtectionToken;
import org.apache.ws.secpolicy.model.RecipientToken;
import org.apache.ws.secpolicy.model.SignatureToken;
import org.apache.ws.secpolicy.model.SignedEncryptedElements;
import org.apache.ws.secpolicy.model.SignedEncryptedParts;
import org.apache.ws.secpolicy.model.SupportingToken;
import org.apache.ws.secpolicy.model.SymmetricAsymmetricBindingBase;
import org.apache.ws.secpolicy.model.SymmetricBinding;
import org.apache.ws.secpolicy.model.TokenWrapper;
import org.apache.ws.secpolicy.model.TransportBinding;
import org.apache.ws.secpolicy.model.Trust10;
import org.apache.ws.secpolicy.model.Wss10;
import org.apache.ws.secpolicy.model.Wss11;

import java.util.Iterator;
import java.util.List;

public class RampartPolicyBuilder {
    
    /**
     * Compile the parsed security data into one Policy data block.
     * 
     * This methods loops over all top level Policy Engine data elements,
     * extracts the parsed parameters and sets them into a single data block.
     * During this processing the method prepares the parameters in a format
     * that is ready for processing by the WSS4J functions.
     * 
     * <p/>
     * 
     * The WSS4J policy enabled handler takes this data block to control the
     * setup of the security header.
     * 
     * @param topLevelAssertions
     *            The iterator of the top level policy assertions
     * @return The compile Poilcy data block.
     * @throws WSSPolicyException
     */
    public static RampartPolicyData build(List topLevelAssertions)
            throws WSSPolicyException {
        
        RampartPolicyData rpd = new RampartPolicyData();
        
        for (Iterator iter = topLevelAssertions.iterator(); iter.hasNext();) {
            Assertion assertion = (Assertion) iter.next();
            if (assertion instanceof Binding) {
                if (assertion instanceof SymmetricBinding) {
                    processSymmetricPolicyBinding((SymmetricBinding) assertion, rpd);
                } else if(assertion instanceof AsymmetricBinding) {
                    processAsymmetricPolicyBinding((AsymmetricBinding) assertion, rpd);
                } else {
                    processTransportBinding((TransportBinding) assertion, rpd);
                }
                
                /*
                 * Don't change the order of Wss11 / Wss10 instance checks
                 * because Wss11 extends Wss10 - thus first check Wss11.
                 */
            } else if (assertion instanceof Wss11) {
                processWSS11((Wss11) assertion, rpd);
            } else if (assertion instanceof Wss10) {
                processWSS10((Wss10) assertion, rpd);
            } else if (assertion instanceof SignedEncryptedElements) {
                processSignedEncryptedElements((SignedEncryptedElements) assertion,
                        rpd);
            } else if (assertion instanceof SignedEncryptedParts) {
                processSignedEncryptedParts((SignedEncryptedParts) assertion, rpd);
            } else if (assertion instanceof SupportingToken) {
                processSupportingTokens((SupportingToken) assertion, rpd);
            } else if (assertion instanceof Trust10) {
                processTrust10((Trust10)assertion, rpd);
            } else if (assertion instanceof RampartConfig) {
                processRampartConfig((RampartConfig)assertion, rpd);
            } else {
                
                System.out.println("Unknown top level PED found: "
                        + assertion.getClass().getName());
            }
        }
        
        return rpd;
    }

    /**
     * @param binding
     * @param rpd
     */
    private static void processTransportBinding(TransportBinding binding, RampartPolicyData rpd) {
        binding(binding, rpd);
        rpd.setTransportBinding(true);
    }

    /**
     * Add TRust10 assertion info into rampart policy data
     * @param trust10
     * @param rpd
     */
    private static void processTrust10(Trust10 trust10, RampartPolicyData rpd) {
        rpd.setTrust10(trust10);
    }

    /**
     * Add the rampart configuration information into rampart policy data.
     * @param config
     * @param rpd
     */
    private static void processRampartConfig(RampartConfig config, RampartPolicyData rpd) {
        rpd.setRampartConfig(config);
    }

    /**
     * Evaluate the symmetric policy binding data.
     * 
     * @param binding
     *            The binding data
     * @param rpd
     *            The WSS4J data to initialize
     * @throws WSSPolicyException
     */
    private static void processSymmetricPolicyBinding(
            SymmetricBinding symmBinding, RampartPolicyData rpd)
            throws WSSPolicyException {
        rpd.setSymmetricBinding(true);
        binding(symmBinding, rpd);
        symmAsymmBinding(symmBinding, rpd);
        symmetricBinding(symmBinding, rpd);
    }

    private static void processWSS10(Wss10 wss10, RampartPolicyData rpd) {
        rpd.setWss10(wss10);
    }

    /**
     * Evaluate the asymmetric policy binding data.
     * 
     * @param binding
     *            The binding data
     * @param rpd
     *            The WSS4J data to initialize
     * @throws WSSPolicyException
     */
    private static void processAsymmetricPolicyBinding(
            AsymmetricBinding binding, RampartPolicyData rpd)
            throws WSSPolicyException {
        rpd.setSymmetricBinding(false);
        binding(binding, rpd);
        symmAsymmBinding(binding, rpd);
        asymmetricBinding(binding, rpd);
    }

    private static void processWSS11(Wss11 wss11, RampartPolicyData rpd) {
        rpd.setSignatureConfirmation(wss11.isRequireSignatureConfirmation());
        rpd.setWss11(wss11);
    }

    /**
     * Populate elements to sign and/or encrypt with the message tokens.
     * 
     * @param sep
     *            The data describing the elements (XPath)
     * @param rpd
     *            The WSS4J data to initialize
     */
    private static void processSignedEncryptedElements(
            SignedEncryptedElements see, RampartPolicyData rpd) {
        Iterator it = see.getXPathExpressions().iterator();
        if (see.isSignedElemets()) {
            while (it.hasNext()) {
                rpd.setSignedElements((String) it.next());
            }
        } else {
            while (it.hasNext()) {
                rpd.setEncryptedElements((String) it.next());
            }
        }
    }

    /**
     * Populate parts to sign and/or encrypt with the message tokens.
     * 
     * @param sep
     *            The data describing the parts
     * @param rpd
     *            The WSS4J data to initialize
     */
    private static void processSignedEncryptedParts(SignedEncryptedParts sep,
            RampartPolicyData rpd) {
        Iterator it = sep.getHeaders().iterator();
        if (sep.isSignedParts()) {
            rpd.setSignBody(sep.isBody());
            while (it.hasNext()) {
                Header header = (Header) it.next();
                rpd.setSignedParts(header.getNamespace(), header.getName());
            }
        } else {
            rpd.setEncryptBody(sep.isBody());
            while (it.hasNext()) {
                Header header = (Header) it.next();
                rpd.setEncryptedParts(header.getNamespace(), header.getName());
            }
        }
    }

    /**
     * Evaluate policy data that is common to all bindings.
     * 
     * @param binding
     *            The common binding data
     * @param rpd
     *            The WSS4J data to initialize
     */
    private static void binding(Binding binding, RampartPolicyData rpd) {
        rpd.setLayout(binding.getLayout().getValue());
        rpd.setIncludeTimestamp(binding.isIncludeTimestamp());
        rpd.setAlgorithmSuite(binding.getAlgorithmSuite());
    }

    /**
     * Evaluate policy data that is common to symmetric and asymmetric bindings.
     * 
     * @param binding
     *            The symmetric/asymmetric binding data
     * @param rpd
     *            The WSS4J data to initialize
     */
    private static void symmAsymmBinding(
            SymmetricAsymmetricBindingBase binding, RampartPolicyData rpd) {
        rpd.setEntireHeadersAndBodySignatures(binding
                .isEntireHeadersAndBodySignatures());
        rpd.setProtectionOrder(binding.getProtectionOrder());
        rpd.setSignatureProtection(binding.isSignatureProtection());
        rpd.setTokenProtection(binding.isTokenProtection());
        rpd.setAlgorithmSuite(binding.getAlgorithmSuite());
    }

    /**
     * Evaluate policy data that is specific to symmetric binding.
     * 
     * @param binding
     *            The symmetric binding data
     * @param rpd
     *            The WSS4J data to initialize
     */
    private static void symmetricBinding(SymmetricBinding binding,
            RampartPolicyData rpd) throws WSSPolicyException {
        Assertion token = binding.getProtectionToken();
        
        if (token != null) {
            rpd.setProtectionToken(((ProtectionToken)token).getProtectionToken());
        } else {
            token = binding.getEncryptionToken();
            Assertion token1 = binding.getSignatureToken();
            if (token == null && token1 == null) {
                // this is an error - throw something
            }
            rpd.setEncryptionToken(
                    ((EncryptionToken) token).getEncryptionToken());
            rpd.setSignatureToken(((SignatureToken) token).getSignatureToken());
        }
    }

    /**
     * Evaluate policy data that is specific to asymmetric binding.
     * 
     * @param binding
     *            The asymmetric binding data
     * @param rpd
     *            The WSS4J data to initialize
     */
    private static void asymmetricBinding(AsymmetricBinding binding,
            RampartPolicyData rpd) throws WSSPolicyException {
        TokenWrapper tokWrapper = binding.getRecipientToken();
        TokenWrapper tokWrapper1 = binding.getInitiatorToken();
        if (tokWrapper == null && tokWrapper1 == null) {
            // this is an error - throw something
        }
        rpd.setRecipientToken(((RecipientToken) tokWrapper).getReceipientToken());
        rpd.setInitiatorToken(((InitiatorToken) tokWrapper1).getInitiatorToken());
    }

    private static void processSupportingTokens(SupportingToken token,
            RampartPolicyData rpd) throws WSSPolicyException {
        rpd.setSupportingTokens(token);
    }
}
