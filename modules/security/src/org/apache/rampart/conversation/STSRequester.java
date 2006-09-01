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

package org.apache.rampart.conversation;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.dom.DOOMAbstractFactory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.OutInAxisOperation;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.util.Base64;
import org.apache.neethi.Policy;
import org.apache.rahas.RahasConstants;
import org.apache.rahas.Token;
import org.apache.rahas.TrustException;
import org.apache.rahas.TrustUtil;
import org.apache.rampart.RampartException;
import org.apache.rampart.RampartMessageData;
import org.apache.rampart.handler.WSSHandlerConstants;
import org.apache.rampart.policy.RampartPolicyBuilder;
import org.apache.rampart.policy.RampartPolicyData;
import org.apache.rampart.util.Axis2Util;
import org.apache.rampart.util.RampartUtil;
import org.apache.ws.secpolicy.WSSPolicyException;
import org.apache.ws.secpolicy.model.Trust10;
import org.apache.ws.security.WSConstants;
import org.apache.ws.security.WSSecurityException;
import org.apache.ws.security.message.token.SecurityContextToken;
import org.apache.ws.security.processor.EncryptedKeyProcessor;
import org.apache.ws.security.util.WSSecurityUtil;
import org.w3c.dom.Element;

import javax.xml.namespace.QName;

import java.util.Iterator;
import java.util.Vector;

public class STSRequester {
    
    
    //TODO: Remove when policy support is completed
    public static void issueRequest(ConversationConfiguration config) throws RampartException, AxisFault {
        MessageContext msgCtx = config.getMsgCtx();
        AxisService axisService = new AxisService("SecurityTokenService");
        QName rstQn = new QName("requestSecurityToken");
        OutInAxisOperation operation = new OutInAxisOperation(rstQn);
        axisService.addOperation(operation);
        ServiceClient client = new ServiceClient(msgCtx
                .getConfigurationContext(), axisService);
        
        Options options = new Options();
        options.setTo(new EndpointReference(config.getStsEPRAddress()));
        if(config.getWstVersion() == RahasConstants.VERSION_05_02) {
            options.setAction(RahasConstants.V_05_02.RST_ACTON_SCT);
        } else {
            options.setAction(RahasConstants.V_05_12.RST_ACTON_SCT);
        }
        
        //Get the security configurations
        Parameter outFlowParam = msgCtx
                .getParameter(WSSHandlerConstants.STS_OUTFLOW_SECURITY);
        Parameter inFlowParam = msgCtx
                .getParameter(WSSHandlerConstants.STS_INFLOW_SECURITY);
        
        if(outFlowParam == null) {
            outFlowParam = (Parameter) msgCtx
                    .getProperty(WSSHandlerConstants.STS_OUTFLOW_SECURITY);
        }
        if(inFlowParam == null) {
            inFlowParam = (Parameter) msgCtx
                    .getProperty(WSSHandlerConstants.STS_INFLOW_SECURITY);
        }
        
        
        //Set the STS specific config config
        options.setProperty(WSSHandlerConstants.OUTFLOW_SECURITY, outFlowParam);
        options.setProperty(WSSHandlerConstants.INFLOW_SECURITY, inFlowParam);
        
        client.engageModule(new QName(WSSHandlerConstants.SECURITY_MODULE_NAME));
        
        client.setOptions(options);

        try {
            OMElement rstElem = TrustUtil.createRequestSecurityTokenElement(config.getWstVersion());
            OMElement reqTypeElem = TrustUtil.createRequestTypeElement(config.getWstVersion(), rstElem);
            OMElement tokenTypeElem = TrustUtil.createTokenTypeElement(config.getWstVersion(), rstElem);
            
            if(config.getWstVersion() == RahasConstants.VERSION_05_02) {
                reqTypeElem.setText(RahasConstants.V_05_02.REQ_TYPE_ISSUE);
                tokenTypeElem.setText(RahasConstants.V_05_02.TOK_TYPE_SCT);
            } else {
                reqTypeElem.setText(RahasConstants.V_05_12.REQ_TYPE_ISSUE);
                tokenTypeElem.setText(RahasConstants.V_05_12.TOK_TYPE_SCT);
            }
            
            if(config.isProvideEntropy()) {
                //TODO Option to get the nonce lenght and  
                //keysize from the the configuration
                
                // Length of nonce in bytes
                int nonceLength = 16;

                OMElement entropyElem = TrustUtil.createEntropyElement(config.getWstVersion(), rstElem);
                
                byte[] nonce = WSSecurityUtil.generateNonce(nonceLength);
                OMElement elem = null;
                if(config.getWstVersion() == RahasConstants.VERSION_05_02) {
                    elem = TrustUtil.createBinarySecretElement(config.getWstVersion(), entropyElem, RahasConstants.V_05_02.BIN_SEC_TYPE_NONCE);
                } else {
                    elem = TrustUtil.createBinarySecretElement(config.getWstVersion(), entropyElem, RahasConstants.V_05_12.BIN_SEC_TYPE_NONCE);
                }
                elem.setText(Base64.encode(nonce));

                TrustUtil.createKeySizeElement(config.getWstVersion(), rstElem, nonceLength * 8);
            }

            String str = rstElem.toString();
            
            OMElement tempResult = client.sendReceive(rstQn, rstElem);
            Axis2Util.useDOOM(true);
            OMElement tempelem = Axis2Util.toDOOM(DOOMAbstractFactory.getOMFactory(), tempResult);
            OMElement elem = (OMElement)config.getDocument().importNode((Element)tempelem, true);
            Util.processRSTR(elem, config);
            
        } catch (Exception e) {
            e.printStackTrace();
            throw new RampartException(e.getMessage());
        }
    }
    
    /**
     * Obtain an SCT from the given issuer
     * @param rmd RampartMessageData of the message waiting to use this SCT
     * @param issuerAddress Address of the issuer
     * @param bootstapPolicy Bootstap policy to obtain the token
     * @return The identifier of the SCT
     * @throws RampartException
     */
    public static String getSct(RampartMessageData rmd, String issuerAddress, Policy bootstapPolicy) throws RampartException {
        try {
            

            MessageContext msgCtx = rmd.getMsgContext();
            AxisService axisService = new AxisService("SecurityTokenService");
            QName rstQn = new QName("requestSecurityToken");
            OutInAxisOperation operation = new OutInAxisOperation(rstQn);
            axisService.addOperation(operation);
            ServiceClient client = new ServiceClient(msgCtx.getConfigurationContext(), axisService);
            
            Options options = new Options();
            options.setTo(new EndpointReference(issuerAddress));
            
            int wstVersion = rmd.getWstVersion();
            
            if(wstVersion == RahasConstants.VERSION_05_02) {
                options.setAction(RahasConstants.V_05_02.RST_ACTON_SCT);
            } else {
                options.setAction(RahasConstants.V_05_12.RST_ACTON_SCT);
            }
            
            //Get the security configurations
            Parameter outFlowParam = msgCtx
                    .getParameter(WSSHandlerConstants.STS_OUTFLOW_SECURITY);
            Parameter inFlowParam = msgCtx
                    .getParameter(WSSHandlerConstants.STS_INFLOW_SECURITY);
            
            if(outFlowParam == null) {
                outFlowParam = (Parameter) msgCtx
                        .getProperty(WSSHandlerConstants.STS_OUTFLOW_SECURITY);
            }
            if(inFlowParam == null) {
                inFlowParam = (Parameter) msgCtx
                        .getProperty(WSSHandlerConstants.STS_INFLOW_SECURITY);
            }
            
            
            //Set the STS specific config config
            options.setProperty(WSSHandlerConstants.OUTFLOW_SECURITY, outFlowParam);
            options.setProperty(WSSHandlerConstants.INFLOW_SECURITY, inFlowParam);
            
            client.engageModule(new QName(WSSHandlerConstants.SECURITY_MODULE_NAME));
            
            //TODO : Have to set the bootstrap policy in the options 
            
            client.setOptions(options);

            OMElement rstElem = TrustUtil.createRequestSecurityTokenElement(wstVersion);
            OMElement reqTypeElem = TrustUtil.createRequestTypeElement(wstVersion, rstElem);
            OMElement tokenTypeElem = TrustUtil.createTokenTypeElement(wstVersion, rstElem);
            
            if(wstVersion == RahasConstants.VERSION_05_02) {
                reqTypeElem.setText(RahasConstants.V_05_02.REQ_TYPE_ISSUE);
                tokenTypeElem.setText(RahasConstants.V_05_02.TOK_TYPE_SCT);
            } else {
                reqTypeElem.setText(RahasConstants.V_05_12.REQ_TYPE_ISSUE);
                tokenTypeElem.setText(RahasConstants.V_05_12.TOK_TYPE_SCT);
            }
            
            
            
            boolean serviceAsSts = rmd.getMsgContext().getOptions().getTo().getAddress().equals(issuerAddress);
            
            Trust10 trust10 = rmd.getPolicyData().getTrust10();
            
            //In the case when the service is the STS and when the service's 
            //policy requires client entropy 
            boolean useClientEntropy = serviceAsSts && trust10 != null && trust10.isRequireClientEntropy();
            
            //If above is false and service is not the STS, then 
            //check the bootstrap policy for a the assertion
            if(!useClientEntropy && !serviceAsSts) {
                RampartPolicyData bootRPD = RampartPolicyBuilder.build((Iterator)bootstapPolicy.getAlternatives().next());
                Trust10 bootTrust10 = bootRPD.getTrust10();
                useClientEntropy = bootTrust10 != null && bootTrust10.isRequireClientEntropy();
            }
            
            if(useClientEntropy) {
                //Using the maximum available key length for the SCT secret 
                //Using the same length for nonce
                int keyLength = rmd.getPolicyData().getAlgorithmSuite().getMaximumSymmetricKeyLength();
                
                // Length of nonce in bytes
                int nonceLength = keyLength/8;
                byte[] nonce = WSSecurityUtil.generateNonce(nonceLength);
                
                OMElement entropyElem = TrustUtil.createEntropyElement(wstVersion, rstElem);
                
                OMElement elem = null;
                if(wstVersion == RahasConstants.VERSION_05_02) {
                    elem = TrustUtil.createBinarySecretElement(wstVersion, entropyElem, RahasConstants.V_05_02.BIN_SEC_TYPE_NONCE);
                } else {
                    elem = TrustUtil.createBinarySecretElement(wstVersion, entropyElem, RahasConstants.V_05_12.BIN_SEC_TYPE_NONCE);
                }
                elem.setText(Base64.encode(nonce));
                
                //Create and add the KeySize element
                TrustUtil.createKeySizeElement(wstVersion, rstElem, keyLength);
            }

            //Make the request and get hold ofthe result
            OMElement tempResult = client.sendReceive(rstQn, rstElem);
            
            //Handle the RSTR(C)
            OMElement rstr = (wstVersion == RahasConstants.VERSION_05_12) ? tempResult
                    .getFirstElement()
                    : tempResult;

            //Process the RSTR
            Token tok = processRSTR(rmd, rstr, wstVersion);

            //Store the token
            rmd.getTokenStorage().add(tok);
            
            String id = tok.getId();
            rmd.setSecConvTokenId(id);
            return id;
            
            
        } catch (AxisFault e) {
            throw new RampartException("errorInObtainingSct", new String[]{issuerAddress},e);
        } catch (TrustException e) {
            throw new RampartException("errorInObtainingSct", new String[]{issuerAddress},e);
        } catch (WSSPolicyException e) {
            throw new RampartException("errorInObtainingSct", new String[]{issuerAddress},e);
        } catch (WSSecurityException e) {
            throw new RampartException("errorInObtainingSct", new String[]{issuerAddress},e);
        }
    }

    public static Token processRSTR(RampartMessageData rmd, OMElement rstr, int wstVersion) throws TrustException, RampartException, WSSecurityException {
        
        Token token = null;
        
        //Convert to DOOM
        OMElement doomRstr = Axis2Util.toDOOM(DOOMAbstractFactory.getOMFactory(), rstr);
        
        String wstNs = TrustUtil.getWSTNamespace(wstVersion);
        
        OMElement rstElem = doomRstr.getFirstChildWithName(new QName(wstNs,
                RahasConstants.REQUESTED_SECURITY_TOKEN_LN));
        
        if (rstElem != null) {
            OMElement sctElem = rstElem
                    .getFirstChildWithName(SecurityContextToken.TOKEN);
            if (sctElem != null) {
                SecurityContextToken sct = new SecurityContextToken(
                        (Element) sctElem);
                token = new Token(sct.getIdentifier(), sctElem);
            } else {
                throw new RampartException("sctMissingInResponse");
            }
        } else {
            throw new TrustException("reqestedSecTokMissing");
        }

        // Process RequestedProofToken and extract the secret
        byte[] secret = null;
        OMElement rpt = rstr.getFirstChildWithName(new QName(wstNs,
                RahasConstants.REQUESTED_PROOF_TOKEN_LN));
        if (rpt != null) {
            OMElement elem = rpt.getFirstElement();

            if (WSConstants.ENC_KEY_LN.equals(elem.getLocalName())
                    && WSConstants.ENC_NS.equals(elem.getNamespace().getNamespaceURI())) {
                // Handle the xenc:EncryptedKey case
                EncryptedKeyProcessor processor = new EncryptedKeyProcessor();
                processor.handleToken((Element) elem, null, RampartUtil.getSignatureCrypto(rmd.getPolicyData().getRampartConfig()), RampartUtil.getPasswordCB(rmd), null, new Vector(), null);
                secret = processor.getDecryptedBytes();
            } else if (RahasConstants.BINARY_SECRET_LN.equals(elem.getLocalName())
                    && RahasConstants.WST_NS_05_02.equals(elem.getNamespace().getNamespaceURI())) {
                // Handle the wst:BinarySecret case
                secret = Base64.decode(elem.getText());
            } else {
                throw new TrustException("notSupported", new String[] { "{"
                        + elem.getNamespace().getNamespaceURI() + "}"
                        + elem.getLocalName() });
            }
        } else {
            throw new TrustException("rptMissing");
        }

        // Check for attached ref
        OMElement reqAttElem = rstr.getFirstChildWithName(new QName(
                RahasConstants.WST_NS_05_02, RahasConstants.REQUESTED_ATTACHED_REFERENCE_LN));
        OMElement reqAttRef = reqAttElem == null ? null : reqAttElem
                .getFirstElement();

        OMElement reqUnattElem = rstr.getFirstChildWithName(new QName(
                RahasConstants.WST_NS_05_02, RahasConstants.REQUESTED_UNATTACHED_REFERENCE_LN));
        OMElement reqUnattRef = reqUnattElem == null ? null : reqUnattElem
                .getFirstElement();

        token.setAttachedReference(reqAttRef);
        token.setUnattachedReference(reqUnattRef);
        token.setSecret(secret);
        
        return token;
    }
    
}
