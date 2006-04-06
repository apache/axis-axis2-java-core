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

package org.apache.axis2.security.rahas;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.impl.dom.DOOMAbstractFactory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.databinding.types.URI;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.OutInAxisOperation;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.security.handler.WSSHandlerConstants;
import org.apache.axis2.security.trust.Constants;
import org.apache.axis2.security.trust.Token;
import org.apache.axis2.security.trust.TrustException;
import org.apache.axis2.security.trust.types.RequestSecurityTokenType;
import org.apache.axis2.security.util.Axis2Util;
import org.apache.axis2.util.Base64;
import org.apache.axis2.util.Loader;
import org.apache.axis2.util.StreamWrapper;
import org.apache.ws.security.WSConstants;
import org.apache.ws.security.message.token.SecurityContextToken;
import org.apache.ws.security.processor.EncryptedKeyProcessor;
import org.w3c.dom.Element;

import javax.security.auth.callback.CallbackHandler;
import javax.xml.namespace.QName;

import java.util.Vector;

public class STSRequester {
    
    public static void issueRequest(RahasConfiguration config) throws RahasException, AxisFault {
        MessageContext msgCtx = config.getMsgCtx();
        AxisService axisService = new AxisService("SecurityTokenService");
        QName rstQn = new QName("requestSecurityToken");
        OutInAxisOperation operation = new OutInAxisOperation(rstQn);
        axisService.addOperation(operation);
        ServiceClient client = new ServiceClient(msgCtx
                .getConfigurationContext(), axisService);
        
        Options options = new Options();
        options.setTo(new EndpointReference(config.getStsEPRAddress()));
        options.setAction(Constants.RST_ACTON_SCT);
        
        //Get the security configurations
        Parameter outFlowParam = msgCtx
                .getParameter(WSSHandlerConstants.OUTFLOW_SECURITY);
        Parameter inFlowParam = msgCtx
                .getParameter(WSSHandlerConstants.INFLOW_SECURITY);
        
        if(outFlowParam == null) {
            outFlowParam = (Parameter) msgCtx
                    .getProperty(WSSHandlerConstants.OUTFLOW_SECURITY);
        }
        if(inFlowParam == null) {
            inFlowParam = (Parameter) msgCtx
                    .getProperty(WSSHandlerConstants.INFLOW_SECURITY);
        }
        
        options.setProperty(WSSHandlerConstants.OUTFLOW_SECURITY, outFlowParam);
        options.setProperty(WSSHandlerConstants.INFLOW_SECURITY, inFlowParam);
        
        client.setOptions(options);

        RequestSecurityTokenType rst = new RequestSecurityTokenType();
        
        try {
            rst.setRequestType(new URI(Constants.REQ_TYPE_ISSUE));
            rst.setTokenType(new URI(Constants.TOK_TYPE_SCT));
            rst.setContext(new URI("http://get.optional.attrs.working"));
            
            Axis2Util.useDOOM(false);
            StAXOMBuilder builder = new StAXOMBuilder(new StreamWrapper(rst
                    .getPullParser(new QName(Constants.WST_NS,
                            Constants.REQUEST_SECURITY_TOKEN_LN))));

            OMElement tempResult = client.sendReceive(rstQn, builder.getDocumentElement());
            Axis2Util.useDOOM(true);
            OMElement tempelem = Axis2Util.toDOOM(DOOMAbstractFactory.getOMFactory(), tempResult);
            OMElement elem = (OMElement)config.getDocument().importNode((Element)tempelem, true);
            processRSTR(elem, config);
            
        } catch (Exception e) {
            e.printStackTrace();
            throw new RahasException(e.getMessage());
        }
    }
    
    private static void processRSTR(OMElement rstr, RahasConfiguration config)
            throws Exception {
        //Extract the SecurityContextToken
        OMElement rstElem = rstr.getFirstChildWithName(new QName(
                Constants.WST_NS, Constants.REQUESTED_SECURITY_TOKEN_LN));
        Token token = null;
        if(rstElem != null) {
            OMElement sctElem = rstElem.getFirstChildWithName(SecurityContextToken.TOKEN);
            if(sctElem != null) {
                SecurityContextToken sct = new SecurityContextToken((Element)sctElem);
                token = new Token(sct.getIdentifier(), sctElem);
                config.resgisterContext(sct.getIdentifier());
            } else {
                throw new RahasException("sctMissingInResponse");
            }
        } else {
            throw new TrustException("reqestedSecTokMissing");
        }

        // Process RequestedProofToken and extract the secret
        byte[] secret = null;
        OMElement rpt = rstr.getFirstChildWithName(new QName(Constants.WST_NS,
                Constants.REQUESTED_PROOF_TOKEN_LN));
        if (rpt != null) {
            OMElement elem = rpt.getFirstElement();
            
            if (WSConstants.ENC_KEY_LN.equals(elem.getLocalName())
                    && WSConstants.ENC_NS
                            .equals(elem.getNamespace().getName())) {
                //Handle the xenc:EncryptedKey case
                EncryptedKeyProcessor processor = new EncryptedKeyProcessor();
                processor.handleToken((Element) elem, null,
                        Util.getCryptoInstace(config),
                        getCallbackHandlerInstance(config), null, new Vector(),
                        null);
                secret = processor.getDecryptedBytes();
            } else if (Constants.BINARY_SECRET.equals(elem.getLocalName())
                    && Constants.WST_NS.equals(elem.getNamespace().getName())) {
                //Handle the wst:BinarySecret case
                secret = Base64.decode(elem.getText());
            } else {
                throw new TrustException("notSupported", new String[] { "{"
                        + elem.getNamespace().getName() + "}"
                        + elem.getLocalName() });
            }
        } else {
            throw new TrustException("rptMissing");
        }
        
        token.setSecret(secret);
        config.getTokenStore().add(token);
    }
    
    
    private static CallbackHandler getCallbackHandlerInstance(
            RahasConfiguration config) throws Exception {
        if (config.getPasswordCallbackRef() != null) {
            return config.getPasswordCallbackRef();
        } else if (config.getPasswordCallbackClass() != null) {
            if (config.getClassLoader() != null) {
                Class clazz = Loader.loadClass(config.getClassLoader(), config
                        .getPasswordCallbackClass());
                return (CallbackHandler) clazz.newInstance();
            } else {
                Class clazz = Loader.loadClass(config
                        .getPasswordCallbackClass());
                return (CallbackHandler) clazz.newInstance();
            }
        } else {
            throw new RahasException("noInfoForCBhandler");
        }
    }


}
