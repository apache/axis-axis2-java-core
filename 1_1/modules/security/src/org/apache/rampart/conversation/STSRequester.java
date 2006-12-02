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
import org.apache.rahas.RahasConstants;
import org.apache.rahas.TrustException;
import org.apache.rahas.TrustUtil;
import org.apache.rampart.RampartException;
import org.apache.rampart.handler.WSSHandlerConstants;
import org.apache.rampart.util.Axis2Util;
import org.apache.ws.security.conversation.ConversationConstants;
import org.apache.ws.security.util.WSSecurityUtil;
import org.w3c.dom.Element;

import javax.xml.namespace.QName;

/**
 * @deprecated
 */
public class STSRequester {
    
    
    //TODO: Remove when policy support is completed
    public static void issueRequest(ConversationConfiguration config) throws RampartException, AxisFault, TrustException  {
        MessageContext msgCtx = config.getMsgCtx();
        AxisService axisService = new AxisService("SecurityTokenService");
        QName rstQn = new QName("requestSecurityToken");
        OutInAxisOperation operation = new OutInAxisOperation(rstQn);
        axisService.addOperation(operation);
        ServiceClient client = new ServiceClient(msgCtx
                .getConfigurationContext(), axisService);
        
        Options options = new Options();
        options.setTo(new EndpointReference(config.getStsEPRAddress()));
        
        options.setAction(TrustUtil.getActionValue(config.getWstVersion(), RahasConstants.RST_ACTION_SCT));

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
            TrustUtil.createRequestTypeElement(config.getWstVersion(), rstElem, RahasConstants.REQ_TYPE_ISSUE);
            OMElement tokenTypeElem = TrustUtil.createTokenTypeElement(config.getWstVersion(), rstElem);
            tokenTypeElem.setText(ConversationConstants.getWSCNs(ConversationConstants.DEFAULT_VERSION) + ConversationConstants.TOKEN_TYPE_SECURITY_CONTEXT_TOKEN);
            
            if(config.isProvideEntropy()) {
                //TODO Option to get the nonce lenght and  
                //keysize from the the configuration
                
                // Length of nonce in bytes
                int nonceLength = 16;

                OMElement entropyElem = TrustUtil.createEntropyElement(config.getWstVersion(), rstElem);
                
                byte[] nonce = WSSecurityUtil.generateNonce(nonceLength);
                OMElement elem = TrustUtil.createBinarySecretElement(config.getWstVersion(), entropyElem, RahasConstants.BIN_SEC_TYPE_NONCE);
         
                elem.setText(Base64.encode(nonce));

                TrustUtil.createKeySizeElement(config.getWstVersion(), rstElem, nonceLength * 8);
            }

            OMElement tempResult = client.sendReceive(rstQn, rstElem);
            Axis2Util.useDOOM(true);
            OMElement tempelem = Axis2Util.toDOOM(DOOMAbstractFactory.getOMFactory(), tempResult);
            OMElement elem = (OMElement)config.getDocument().importNode((Element)tempelem, true);
            Util.processRSTR(elem, config);
            
        } catch (Exception e) {
            e.printStackTrace();
            throw new RampartException("errorInObtainingSct",
                    new String[] { config.getStsEPRAddress() }, e);
        }
    }

    
}
