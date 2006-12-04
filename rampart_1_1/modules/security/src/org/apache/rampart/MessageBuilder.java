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

package org.apache.rampart;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.AddressingConstants;
import org.apache.axis2.addressing.AddressingConstants.Final;
import org.apache.axis2.addressing.AddressingConstants.Submission;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.OperationContext;
import org.apache.axis2.util.XMLUtils;
import org.apache.axis2.wsdl.WSDLConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.rahas.RahasConstants;
import org.apache.rahas.TrustUtil;
import org.apache.rampart.builder.AsymmetricBindingBuilder;
import org.apache.rampart.builder.SymmetricBindingBuilder;
import org.apache.rampart.builder.TransportBindingBuilder;
import org.apache.rampart.policy.RampartPolicyData;
import org.apache.rampart.util.Axis2Util;
import org.apache.rampart.util.RampartUtil;
import org.apache.ws.secpolicy.WSSPolicyException;
import org.apache.ws.security.WSSecurityException;
import org.apache.ws.security.handler.WSHandlerConstants;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.namespace.QName;

public class MessageBuilder {
    
    private static Log log = LogFactory.getLog(MessageBuilder.class);
    
    public void build(MessageContext msgCtx) throws WSSPolicyException,
            RampartException, WSSecurityException, AxisFault {

        Axis2Util.useDOOM(true);
        
        RampartMessageData rmd = new RampartMessageData(msgCtx, true);
        
        
        RampartPolicyData rpd = rmd.getPolicyData();
        if(rpd == null) {
            return;
        }
        
        //Copy the RECV_RESULTS if available
        if(!rmd.isClientSide()) {
            OperationContext opCtx = msgCtx.getOperationContext();
            MessageContext inMsgCtx;
            if(opCtx != null && 
                    (inMsgCtx = opCtx.getMessageContext(WSDLConstants.MESSAGE_LABEL_IN_VALUE)) != null) {
                msgCtx.setProperty(WSHandlerConstants.RECV_RESULTS, 
                        inMsgCtx.getProperty(WSHandlerConstants.RECV_RESULTS));
            }
        }
        
        
        String isCancelreq = (String)msgCtx.getProperty(RampartMessageData.CANCEL_REQUEST);
        if(isCancelreq != null && Constants.VALUE_TRUE.equals(isCancelreq)) {
            try {
                
                String cancelAction = TrustUtil.getWSTNamespace(rmd.getWstVersion()) + RahasConstants.RST_ACTION_CANCEL_SCT;
                //Set action
                msgCtx.getOptions().setAction(cancelAction);
                
                //Change the wsa:Action header
                String wsaNs = Final.WSA_NAMESPACE;
                Object addressingVersionFromCurrentMsgCtxt = msgCtx.getProperty(AddressingConstants.WS_ADDRESSING_VERSION);
                if (Submission.WSA_NAMESPACE.equals(addressingVersionFromCurrentMsgCtxt)) {
                    wsaNs = Submission.WSA_NAMESPACE;
                }
                OMElement header = msgCtx.getEnvelope().getHeader();
                if(header != null) {
                    OMElement actionElem = header.getFirstChildWithName(new QName(wsaNs, AddressingConstants.WSA_ACTION));
                    if(actionElem != null) {
                        actionElem.setText(cancelAction);
                    }
                }
                
                //set payload to a cancel request
                String ctxIdKey = RampartUtil.getContextIdentifierKey(msgCtx);
                String tokenId = (String)RampartUtil.getContextMap(msgCtx).get(ctxIdKey);
                
                if(tokenId != null && RampartUtil.isTokenValid(rmd, tokenId)) {
                    OMElement bodyElem = msgCtx.getEnvelope().getBody();
                    OMElement child = bodyElem.getFirstElement();
                    OMElement newChild = TrustUtil.createCancelRequest(tokenId, rmd.getWstVersion());
                    Element newDomChild = XMLUtils.toDOM(newChild);
                    Node importedNode = rmd.getDocument().importNode((Element) newDomChild, true);
                    ((Element) bodyElem).replaceChild(importedNode, (Element) child);
                } else {
                    throw new RampartException("tokenToBeCancelledInvalid");
                }
                
            } catch (Exception e) {
                e.printStackTrace();
                throw new RampartException("errorInTokenCancellation");
            }
        }
        
        if(rpd.isTransportBinding()) {
            log.debug("Building transport binding");
            TransportBindingBuilder building = new TransportBindingBuilder();
            building.build(rmd);
        } else if(rpd.isSymmetricBinding()) {
            log.debug("Building SymmetricBinding");
            SymmetricBindingBuilder builder = new SymmetricBindingBuilder();
            builder.build(rmd);
        } else {
            AsymmetricBindingBuilder builder = new AsymmetricBindingBuilder();
            builder.build(rmd);
        }
    }

}
