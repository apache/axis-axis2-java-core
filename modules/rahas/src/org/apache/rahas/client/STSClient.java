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
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.impl.dom.DOOMAbstractFactory;
import org.apache.axiom.om.util.Base64;
import org.apache.axiom.soap.SOAP12Constants;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.AddressingConstants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.OutInAxisOperation;
import org.apache.axiom.om.util.UUIDGenerator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
import org.apache.ws.security.WSPasswordCallback;
import org.apache.ws.security.WSSecurityException;
import org.apache.ws.security.components.crypto.Crypto;
import org.apache.ws.security.conversation.ConversationException;
import org.apache.ws.security.conversation.dkalgo.P_SHA1;
import org.apache.ws.security.message.token.Reference;
import org.apache.ws.security.processor.EncryptedKeyProcessor;
import org.apache.ws.security.util.WSSecurityUtil;
import org.w3c.dom.Element;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.xml.namespace.QName;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

public class STSClient {

    private static final String RAMPART_POLICY = "rampartPolicy";

    private static Log log = LogFactory.getLog(STSClient.class);

    private String action;

    private OMElement rstTemplate;

    private int version = RahasConstants.VERSION_05_02;

    private Options options;

    private Trust10 trust10;

    private AlgorithmSuite algorithmSuite;

    private byte[] requestorEntropy;

    private String addressingNs = AddressingConstants.Final.WSA_NAMESPACE;

    private int keySize;
    
    private String soapVersion = SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI;

    /**
     * Life time in seconds
     * Default is 300 seconds (5 mins)
     */
    private int ttl = 300;
    private Crypto crypto;
    private CallbackHandler cbHandler;
    private ConfigurationContext configCtx;

    public STSClient(ConfigurationContext configCtx) throws TrustException {
        if (configCtx != null) {
            this.configCtx = configCtx;
        } else {
            throw new TrustException("stsClientCfgCtxNull");
        }
    }

    public Token requestSecurityToken(Policy servicePolicy,
                                      String issuerAddress,
                                      Policy issuerPolicy,
                                      String appliesTo) throws TrustException {
        try {
            QName rstQn = new QName("requestSecurityToken");
            String requestType =
                    TrustUtil.getWSTNamespace(version) + RahasConstants.REQ_TYPE_ISSUE;
            
            ServiceClient client = getServiceClient(rstQn, issuerAddress);
            
            client.getOptions().setProperty(RAMPART_POLICY, issuerPolicy);
            client.getOptions().setSoapVersionURI(this.soapVersion); 

            //Process the STS and service policy policy
            this.processPolicy(issuerPolicy, servicePolicy);
            OMElement response = client.sendReceive(rstQn,
                                                    createIssueRequest(requestType, appliesTo));

            return processIssueResponse(version, response);
        } catch (AxisFault e) {
            e.printStackTrace();
            log.error("errorInObtainingToken", e);
            throw new TrustException("errorInObtainingToken", new String[]{issuerAddress});
        }
    }

    /**
     * Cancel a particular security token
     *
     * @param issuerAddress
     * @param tokenId
     * @return true is the Token was successfully cancelled. False otherwise.
     * @throws TrustException
     */
    public boolean cancelToken(String issuerAddress,
                               String tokenId,
                               String action) throws TrustException {
        try {
            QName rstQn = new QName("cancelSecurityToken");
            ServiceClient client = getServiceClient(rstQn, issuerAddress);
            if(action != null) {
                client.getOptions().setAction(action);
            }
            
            return processCancelResponse(client.sendReceive(rstQn,
                                                            createCancelRequest(tokenId)));
        } catch (AxisFault e) {
            log.error("errorInCancelingToken", e);
            throw new TrustException("errorInCancelingToken", e);
        }
    }
    
    private ServiceClient getServiceClient(QName rstQn,
                                           String issuerAddress) throws AxisFault {
        AxisService axisService =
                new AxisService("SecurityTokenService" + UUIDGenerator.getUUID());
        axisService.setClientSide(true);
        AxisOperation operation = new OutInAxisOperation(rstQn);
        axisService.addOperation(operation);
        ServiceClient client = new ServiceClient(this.configCtx, axisService);

        if (this.options != null) {
            client.setOptions(options);
        }

        //Set the action
        client.getOptions().setAction(action);
        client.getOptions().setTo(new EndpointReference(issuerAddress));
        client.engageModule(new QName("rampart"));
        return client;
    }

    /**
     * @param result
     * @return Token
     */
    private Token processIssueResponse(int version, OMElement result) throws TrustException {
        OMElement rstr = result;
        if (version == RahasConstants.VERSION_05_12) {
            //The WS-SX result will be an RSTRC
            rstr = result.getFirstElement();
        }

        String ns = TrustUtil.getWSTNamespace(version);

        //Get the RequestedAttachedReference
        OMElement reqAttElem = rstr.getFirstChildWithName(new QName(
                ns, RahasConstants.IssuanceBindingLocalNames.REQUESTED_ATTACHED_REFERENCE));
        OMElement reqAttRef = reqAttElem == null ? null : reqAttElem.getFirstElement();

        //Get the RequestedUnattachedReference
        OMElement reqUnattElem =
                rstr.getFirstChildWithName(new QName(ns,
                                                     RahasConstants.IssuanceBindingLocalNames.
                                                             REQUESTED_UNATTACHED_REFERENCE));
        OMElement reqUnattRef = reqUnattElem == null ? null : reqUnattElem.getFirstElement();

        //Get the security token
        OMElement reqSecTok =
                rstr.getFirstChildWithName(new QName(ns,
                                                     RahasConstants.IssuanceBindingLocalNames.
                                                             REQUESTED_SECURITY_TOKEN));
        if (reqSecTok == null) {
            throw new TrustException("reqestedSecTokMissing");
        }

        OMElement tokenElem = reqSecTok.getFirstElement();

        String id = this.findIdentifier(reqAttRef, reqUnattRef, tokenElem);

        if (id == null) {
            throw new TrustException("cannotObtainTokenIdentifier");
        }

        OMElement lifeTimeEle =
                rstr.getFirstChildWithName(new QName(ns,
                                                     RahasConstants.IssuanceBindingLocalNames.
                                                             LIFETIME));

        Token token = new Token(id, tokenElem, lifeTimeEle);
        token.setAttachedReference(reqAttRef);
        token.setUnattachedReference(reqUnattRef);

        //Handle proof token
        OMElement rpt =
                rstr.getFirstChildWithName(new QName(ns,
                                                     RahasConstants.LocalNames.
                                                             REQUESTED_PROOF_TOKEN));

        byte[] secret = null;

        if (rpt != null) {
            OMElement child = rpt.getFirstElement();
            if (child == null) {
                throw new TrustException("invalidRPT");
            }
            if (child.getQName().equals(new QName(ns,
                                                  RahasConstants.LocalNames.
                                                          BINARY_SECRET))) {
                //First check for the binary secret
                String b64Secret = child.getText();
                secret = Base64.decode(b64Secret);
            } else if (child.getQName().equals(new QName(ns, WSConstants.ENC_KEY_LN))) {
                try {
                    Element domChild = (Element) new StAXOMBuilder(
                            DOOMAbstractFactory.getOMFactory(), child
                            .getXMLStreamReader()).getDocumentElement();

                    EncryptedKeyProcessor processor = new EncryptedKeyProcessor();

                    processor.handleToken(domChild, null, this.crypto,
                                          this.cbHandler, null, new Vector(),
                                          null);

                    secret = processor.getDecryptedBytes();
                } catch (WSSecurityException e) {
                    throw new TrustException("errorInProcessingEncryptedKey", e);
                }
            } else if (child.getQName().equals(new QName(ns,
                                                         RahasConstants.IssuanceBindingLocalNames.
                                                                 COMPUTED_KEY))) {
                //Handle the computed key

                //Get service entropy
                OMElement serviceEntrElem = rstr
                        .getFirstChildWithName(new QName(ns,
                                                         RahasConstants.IssuanceBindingLocalNames.
                                                                 ENTROPY));

                OMElement binSecElem = serviceEntrElem.getFirstElement();

                if (binSecElem != null && binSecElem.getText() != null
                    && !"".equals(binSecElem.getText().trim())) {

                    byte[] serviceEntr = Base64.decode(binSecElem.getText());

                    //Right now we only use PSHA1 as the computed key algo                    
                    P_SHA1 p_sha1 = new P_SHA1();

                    int length = (this.keySize > 0) ? keySize
                                 : this.algorithmSuite
                            .getMaximumSymmetricKeyLength();
                    try {
                        secret = p_sha1.createKey(this.requestorEntropy, serviceEntr, 0, length/8);
                    } catch (ConversationException e) {
                        throw new TrustException("keyDerivationError", e);
                    }
                } else {
                    //Service entropy missing
                    throw new TrustException("serviceEntropyMissing");
                }
            }

        } else {
            if (this.requestorEntropy != null) {
                //Use requestor entropy as the key
                secret = this.requestorEntropy;
            }
        }
        token.setSecret(secret);
        return token;
    }

    private boolean processCancelResponse(OMElement response) {
        /*
        <wst:RequestSecurityTokenResponse>
            <wst:RequestedTokenCancelled/>
        </wst:RequestSecurityTokenResponse>
        */
        return response.
                getFirstChildWithName(new QName(RahasConstants.
                        CancelBindingLocalNames.REQUESTED_TOKEN_CANCELED)) != null;
    }

    /**
     * Find the token identifier.
     *
     * @param reqAttRef
     * @param reqUnattRef
     * @param token
     * @return id
     */
    private String findIdentifier(OMElement reqAttRef,
                                  OMElement reqUnattRef,
                                  OMElement token) {
        String id;
        if (reqAttRef != null) {
            //First try the attached ref
            id = this.getIdFromSTR(reqAttRef);
        } else if (reqUnattRef != null) {
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
     *
     * @param refElem
     * @return id
     */
    private String getIdFromSTR(OMElement refElem) {
        //ASSUMPTION:SecurityTokenReference/KeyIdentifier
        OMElement child = refElem.getFirstElement();
        if(child == null) {
            return null;
        }
        
        if (child.getQName().equals(new QName(WSConstants.SIG_NS, "KeyInfo"))) {
            return child.getText();
        } else if(child.getQName().equals(Reference.TOKEN)) {
            return child.getAttributeValue(new QName("URI"));
        } else {
            return null;
        }

    }

    /**
     * Process the goven service policy and extract the info required to create
     * the RST.
     *
     * @param servicePolicy
     */
    private void processPolicy(Policy issuerPolicy, Policy servicePolicy) {
        //Get the policy assertions
        //Assumption: there's only one alternative

        if (issuerPolicy != null) {
            log.debug("Processing Issuer policy");

            List issuerAssertions = (List) issuerPolicy.getAlternatives().next();

            for (Iterator iter = issuerAssertions.iterator(); iter.hasNext();) {
                Assertion tempAssertion = (Assertion) iter.next();
                //find the AlgorithmSuite assertion
                if (tempAssertion instanceof Binding) {

                    log.debug("Extracting algo suite from issuer " +
                              "policy binding");

                    this.algorithmSuite = ((Binding) tempAssertion)
                            .getAlgorithmSuite();
                }
            }
        }

        if (servicePolicy != null) {

            log.debug("Processing service policy to find Trust10 assertion");

            List assertions = (List) servicePolicy.getAlternatives().next();

            for (Iterator iter = assertions.iterator(); iter.hasNext();) {
                Assertion tempAssertion = (Assertion) iter.next();
                //find the Trust10 assertion
                if (tempAssertion instanceof Trust10) {
                    log.debug("Extracting Trust10 assertion from " +
                              "service policy");
                    this.trust10 = (Trust10) tempAssertion;
                }
            }
        }
    }

    /**
     * Create the RST request.
     *
     * @param requestType
     * @param appliesTo
     * @return OMElement
     * @throws TrustException
     */
    private OMElement createIssueRequest(String requestType,
                                         String appliesTo) throws TrustException {

        log.debug("Creating request with request type: " + requestType +
                  " and applies to: " + appliesTo);

        OMElement rst = TrustUtil.createRequestSecurityTokenElement(version);

        TrustUtil.createRequestTypeElement(this.version, rst, requestType);
        if (appliesTo != null) {
            TrustUtil.createAppliesToElement(rst, appliesTo, this.addressingNs);
        }
        TrustUtil.createLifetimeElement(this.version, rst, this.ttl * 1000);

        //Copy over the elements from the template
        if (this.rstTemplate != null) {

            log.debug("Using RSTTemplate: " + this.rstTemplate.toString());

            Iterator templateChildren = rstTemplate.getChildElements();
            while (templateChildren.hasNext()) {
                OMNode child = (OMNode) templateChildren.next();
                rst.addChild(child);
                //Look for the key size element
                if (child instanceof OMElement
                    && ((OMElement) child).getQName().equals(
                        new QName(TrustUtil.getWSTNamespace(this.version),
                                  RahasConstants.IssuanceBindingLocalNames.KEY_SIZE))) {
                    log.debug("Extracting key size from the RSTTemplate: ");
                    OMElement childElem = (OMElement) child;
                    this.keySize =
                            (childElem.getText() != null && !"".equals(childElem.getText())) ?
                            Integer.parseInt(childElem.getText()) :
                            -1;
                    log.debug("Key size from RSTTemplate: " + this.keySize);
                }
            }
        }

        try {
            // Handle entropy
            if (this.trust10 != null) {

                log.debug("Processing Trust10 assertion");

                if (this.trust10.isRequireClientEntropy()) {

                    log.debug("Requires client entropy");

                    // setup requestor entropy
                    OMElement ent = TrustUtil.createEntropyElement(this.version, rst);
                    OMElement binSec =
                            TrustUtil.createBinarySecretElement(this.version,
                                                                ent,
                                                                RahasConstants.BIN_SEC_TYPE_NONCE);
                    this.requestorEntropy =
                            WSSecurityUtil.generateNonce(this.algorithmSuite.
                                    getMaximumSymmetricKeyLength());
                    binSec.setText(Base64.encode(this.requestorEntropy));

                    log.debug("Clien entropy : "
                              + Base64.encode(this.requestorEntropy));

                    // Add the ComputedKey element
                    TrustUtil.createComputedKeyAlgorithm(this.version, rst,
                                                         RahasConstants.COMPUTED_KEY_PSHA1);

                }
            }
        } catch (Exception e) {
            throw new TrustException("errorSettingUpRequestorEntropy", e);
        }

        
        return rst;
        
    }

    private OMElement createCancelRequest(String tokenId) throws TrustException {

        return TrustUtil.createCancelRequest(tokenId, version);
    }

    /**
     * Set this to set the entropy configurations.
     * If this is provided in the given policy it will be overridden.
     *
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
     *
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

    /**
     * Sets the crypto information required to process the RSTR.
     *
     * @param crypto    Crypto information
     * @param cbHandler Callback handler to provide the private key password to
     *                  decrypt
     */
    public void setCryptoInfo(Crypto crypto, CallbackHandler cbHandler) {
        this.crypto = crypto;
        this.cbHandler = cbHandler;
    }

    /**
     * Sets the crypto information required to process the RSTR.
     *
     * @param crypto        The crypto information
     * @param privKeyPasswd Private key password to decrypt
     */
    public void setCryptoInfo(Crypto crypto, String privKeyPasswd) {
        this.crypto = crypto;
        this.cbHandler = new CBHandler(privKeyPasswd);
    }

    /**
     * @param action The action to set.
     */
    public void setAction(String action) {
        this.action = action;
    }

    /**
     * @param options The options to set.
     */
    public void setOptions(Options options) {
        this.options = options;
    }

    /**
     * @param rstTemplate The rstTemplate to set.
     */
    public void setRstTemplate(OMElement rstTemplate) {
        this.rstTemplate = rstTemplate;
    }

    private class CBHandler implements CallbackHandler {

        private String passwd;

        private CBHandler(String passwd) {
            this.passwd = passwd;
        }

        public void handle(Callback[] cb) throws IOException,
                                                 UnsupportedCallbackException {
            ((WSPasswordCallback) cb[0]).setPassword(this.passwd);
        }

    }

    /**
     * @param version The version to set.
     */
    public void setVersion(int version) {
        this.version = version;
    }

    public void setSoapVersion(String soapVersion) {
        this.soapVersion = soapVersion;
    }

}
