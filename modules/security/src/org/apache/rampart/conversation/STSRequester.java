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
import org.apache.rampart.RampartException;
import org.apache.rampart.handler.WSSHandlerConstants;
import org.apache.rahas.Constants;
import org.apache.rahas.TrustUtil;
import org.apache.rahas.types.RequestSecurityTokenType;
import org.apache.rampart.util.Axis2Util;
import org.apache.axis2.util.Base64;
import org.apache.axis2.util.StreamWrapper;
import org.apache.ws.security.util.WSSecurityUtil;
import org.w3c.dom.Element;

import javax.xml.namespace.QName;

public class STSRequester {
    
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
        
        client.engageModule(new QName(WSSHandlerConstants.SECURITY_MODULE_NAME));
        
        client.setOptions(options);

        RequestSecurityTokenType rst = new RequestSecurityTokenType();
        
        try {
            rst.setRequestType(new URI(Constants.REQ_TYPE_ISSUE));
            rst.setTokenType(new URI(Constants.TOK_TYPE_SCT));
            rst.setContext(new URI("http://get.optional.attrs.working"));
            
            Axis2Util.useDOOM(false);
            StAXOMBuilder builder = new StAXOMBuilder(new StreamWrapper(rst
                    .getPullParser(new QName(Constants.WST_NS_05_02,
                            Constants.REQUEST_SECURITY_TOKEN_LN))));

            OMElement rstElem = builder.getDocumentElement();
            
            rstElem.build();
            rstElem = (OMElement)rstElem.detach();
            
            if(config.isProvideEntropy()) {
                //TODO Option to get the nonce lenght and  
                //keysize from the the configuration
                
                // Length of nonce in bytes
                int nonceLength = 16;

                OMElement entropyElem = TrustUtil.createEntropyElement(rstElem);
                
                byte[] nonce = WSSecurityUtil.generateNonce(nonceLength);
                OMElement elem = TrustUtil.createBinarySecretElement(entropyElem,
                        Constants.BIN_SEC_TYPE_NONCE);
                elem.setText(Base64.encode(nonce));

                TrustUtil.createKeySizeElement(rstElem).setText(
                        Integer.toString(nonceLength * 8));
            }

            String str = rstElem.toString();
            System.out.println(str);
            
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
    
}
