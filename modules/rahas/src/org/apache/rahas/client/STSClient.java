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

package org.apache.rahas.client;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNode;
import org.apache.axiom.om.util.Base64;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.AddressingConstants;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.OutInAxisOperation;
import org.apache.neethi.Assertion;
import org.apache.neethi.Policy;
import org.apache.rahas.RahasConstants;
import org.apache.rahas.Token;
import org.apache.rahas.TrustException;
import org.apache.rahas.TrustUtil;
import org.apache.ws.secpolicy.model.AlgorithmSuite;
import org.apache.ws.secpolicy.model.Binding;
import org.apache.ws.secpolicy.model.Trust10;
import org.apache.ws.security.WSConstants;
import org.apache.ws.security.conversation.ConversationException;
import org.apache.ws.security.conversation.dkalgo.P_SHA1;
import org.apache.ws.security.util.WSSecurityUtil;

import javax.xml.namespace.QName;

import java.util.Iterator;

public class STSClient {

    private Trust10 trust10;
    
    private AlgorithmSuite algorithmSuite;
    
    private byte[] requestorEntropy;
    
    private String addressingNs = AddressingConstants.Final.WSA_NAMESPACE;
    
    private int keySize;
    
    /**
     * Life time in seconds
     * Default is 300 seconds (5 mins)
     */
    private int ttl = 300;
    
    public Token requestSecurityToken(ConfigurationContext configCtx,
            int version, Policy policy, String issuerAddress,
            OMElement rstTemplate, String requestType, String appliesTo)
            throws TrustException {
        try {
            AxisService axisService = new AxisService("SecurityTokenService");
            QName rstQn = new QName("requestSecurityToken");
            OutInAxisOperation operation = new OutInAxisOperation(rstQn);
            axisService.addOperation(operation);
            ServiceClient client = new ServiceClient(configCtx, axisService);

            //TODO Set policy in the options to be picked up by the modules 
            //such as rampart
            
            //Process the STS policy
            this.processPolicy(policy);

            OMElement result = client.sendReceive(this.createRequest(version, rstTemplate, requestType, appliesTo));
            
            return this.processResult(version, result);
        } catch (AxisFault e) {
            throw new TrustException("errorInObtainingToken", new String[]{issuerAddress});
        }
    }
    
    /**
     * @param result
     * @return
     */
    private Token processResult(int version, OMElement result) throws TrustException {
        OMElement rstr = null;
        
        rstr = result;
        
        if(version == RahasConstants.VERSION_05_12) {
            //The WS-SX result will be an RSTRC
            rstr = result.getFirstElement();
        }
        
        String ns = TrustUtil.getWSTNamespace(version);
        
        //Get the RequestedAttachedReference
        OMElement reqAttElem = rstr.getFirstChildWithName(new QName(
                ns, RahasConstants.REQUESTED_ATTACHED_REFERENCE_LN));
        OMElement reqAttRef = reqAttElem == null ? null : reqAttElem
                .getFirstElement();

        //Get the RequestedUnattachedReference
        OMElement reqUnattElem = rstr.getFirstChildWithName(new QName(
                ns, RahasConstants.REQUESTED_UNATTACHED_REFERENCE_LN));
        OMElement reqUnattRef = reqUnattElem == null ? null : reqUnattElem
                .getFirstElement();
        
        //Get the security token
        OMElement reqSecTok = rstr.getFirstChildWithName(new QName(ns, RahasConstants.REQUESTED_SECURITY_TOKEN_LN));
        if(reqSecTok == null) {
            throw new TrustException("reqestedSecTokMissing");
        }
        
        OMElement tokenElem = reqSecTok.getFirstElement();

        String id = this.findIdentifier(reqAttRef, reqUnattRef, tokenElem);

        if(id == null) {
            throw new TrustException("cannotObtainTokenIdentifier");
        }
        
        OMElement lifeTimeEle = rstr.getFirstChildWithName(new QName(ns, RahasConstants.LIFETIME_LN));
        
        Token tok = new Token(id, tokenElem, lifeTimeEle);
        tok.setAttachedReference(reqAttRef);
        tok.setUnattachedReference(reqUnattRef);
        
        //Handle proof token
        OMElement rpt = rstr.getFirstChildWithName(new QName(ns, RahasConstants.REQUESTED_PROOF_TOKEN_LN));
        
        byte[] secret = null;
        
        if(rpt != null) {
            OMElement child = rpt.getFirstElement();
            if(child == null) {
                throw new TrustException("invalidRPT");
            }
            if(child.getQName().equals(new QName(ns, RahasConstants.BINARY_SECRET_LN))) {
                //First check for the binary secret
                String b64Secret = child.getText();
                tok.setSecret(Base64.decode(b64Secret));
            }else if(child.getQName().equals(new QName(ns, WSConstants.ENC_KEY_LN))){
                //TODO Handle encrypted key
                throw new UnsupportedOperationException("TODO: Handle encrypted key");
            } else if(child.getQName().equals(new QName(ns, RahasConstants.COMPUTED_KEY_LN))) {
                //Handle the computed key

                //Get service entropy
                OMElement serviceEntrElem = rstr.getFirstChildWithName(new QName(ns, RahasConstants.ENTROPY_LN));
                if(serviceEntrElem != null && serviceEntrElem.getText() != null && !"".equals(serviceEntrElem.getText().trim())) {
                    byte[] serviceEntr = Base64.decode(serviceEntrElem.getText());
                    
                    //Right now we only use PSHA1 as the computed key algo                    
                    P_SHA1 p_sha1 = new P_SHA1();
                    
                    int length = (this.keySize != -1) ? keySize
                            : this.algorithmSuite
                                    .getMaximumSymmetricKeyLength();
                    try {
                        secret = p_sha1.createKey(this.requestorEntropy, serviceEntr, 0, length);
                    } catch (ConversationException e) {
                        throw new TrustException("keyDerivationError", e);
                    }
                } else {
                    //Service entropy missing
                    throw new TrustException("serviceEntropyMissing");
                }
            }
            
        } else {
            if(this.requestorEntropy != null) {
                //Use requestor entropy as the key
                secret = this.requestorEntropy;
            }
        }
        
        tok.setSecret(secret);
        
        return tok;
        
        
    }

    /**
     * Find the token identifier. 
     * @param reqAttRef
     * @param reqUnattRef
     * @param token
     * @return
     */
    private String findIdentifier(OMElement reqAttRef, OMElement reqUnattRef, OMElement token) throws TrustException {
        String id = null;

        if(reqAttRef != null) {
            //First try the attached ref
            id = this.getIdFromSTR(reqAttRef);
        } else if(reqUnattRef != null) {
            //then try the unattached ref
            id = this.getIdFromSTR(reqUnattRef);
        } else {
            //Return wsu:Id of the token element
            id = token.getAttributeValue(new QName(WSConstants.WSU_NS, "Id"));
        }
        return id;
    }

    
    
    /**
     * Process the given STR to find the id it refers to
     * @param reqAttRef
     * @return
     */
    private String getIdFromSTR(OMElement refElem) {
        //ASSUMPTION:SecurityTokenReference/KeyIdentifier
        OMElement ki = refElem.getFirstElement();
        if(ki != null) {
            return ki.getText();
        } else {
            return null;
        }

    }

    /**
     * Process the goven service policy and extract the info required to create
     * the RST.
     * @param policy
     */
    private void processPolicy(Policy policy) {
        //Get the policy assertions
        //Assumption: there's only one alternative
        Iterator assertions = (Iterator)policy.getAlternatives().next();
        
        while (assertions.hasNext()) {
            Assertion tempAssertion = (Assertion) assertions.next();
            //find the Trust10 assertion
            if(tempAssertion instanceof Trust10) {
                this.trust10 = (Trust10) tempAssertion;
            } else if(tempAssertion instanceof Binding) {
                this.algorithmSuite = ((Binding) tempAssertion)
                            .getAlgorithmSuite();    
            }
        }
        
    }
    
    /**
     * Create the RST request.
     * @param version 
     * @param rstTemplate 
     * @return
     * @throws TrustException
     */
    private OMElement createRequest(int version, OMElement rstTemplate, String requestType, String appliesTo) throws TrustException {
        OMElement rst = TrustUtil.createRequestSecurityTokenElement(version);

        TrustUtil.createRequestTypeElement(version, rst, requestType);
        TrustUtil.createAppliesToElement(rst, requestType, this.addressingNs);
        TrustUtil.createLifetimeElement(version, rst, this.ttl * 1000);
        
        //Copy over the elements from the template
        Iterator templateChildren = rstTemplate.getChildElements();
        while (templateChildren.hasNext()) {
            OMNode child = (OMNode) templateChildren.next();
            rst.addChild(child);
            
            //Look for the key size element
            if (child instanceof OMElement
                    && ((OMElement) child).getQName().equals(
                            new QName(TrustUtil.getWSTNamespace(version),
                                    RahasConstants.KEY_SIZE_LN))) {
                OMElement childElem = (OMElement)child;
                this.keySize = (childElem.getText() != null && !""
                        .equals(childElem.getText())) ? 
                                Integer.parseInt(childElem.getText()) : -1;
            }
        }
        
        try {
            //Handle entropy
            if(this.trust10 != null) {
                if(this.trust10.isRequireClientEntropy()) {
                    //setup requestor entropy
                    OMElement ent = TrustUtil.createEntropyElement(version, rst);
                    OMElement binSec = TrustUtil.createBinarySecretElement(version, ent, RahasConstants.BIN_SEC_TYPE_NONCE);
                    this.requestorEntropy = WSSecurityUtil.generateNonce(this.algorithmSuite.getMaximumSymmetricKeyLength());
                    binSec.setText(Base64.encode(this.requestorEntropy));
                    
                    //Add the ComputedKey element
                    TrustUtil.createComputedKeyAlgorithm(version, rst, RahasConstants.COMPUTED_KEY_PSHA1);
                    
                }
            }
        } catch (Exception e) {
            throw new TrustException("errorSettingUpRequestorEntropy");
        }

        return rst;
    }

    /**
     * Set this to set the entropy configurations.
     * If this is provided in the given policy it will be overridden.
     * @param trust10 The trust10 to set.
     */
    public void setTrust10(Trust10 trust10) {
        this.trust10 = trust10;
    }

    /**
     * This can be used in the case where the AlgorithmSuite is not specified in
     * the given policy. 
     * If the AlgorithmSuite exists in a binding in the policy then the value
     * set will be overridden.
     * @param algorithmSuite The algorithmSuite to set.
     */
    public void setAlgorithmSuite(AlgorithmSuite algorithmSuite) {
        this.algorithmSuite = algorithmSuite;
    }

    /**
     * @param addressingNs The addressingNs to set.
     */
    public void setAddressingNs(String addressingNs) {
        this.addressingNs = addressingNs;
    }

    /**
     * @param ttl The ttl to set.
     */
    public void setTtl(int ttl) {
        this.ttl = ttl;
    }
}
