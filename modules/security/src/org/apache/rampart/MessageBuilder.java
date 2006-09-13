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
import org.apache.axiom.om.impl.dom.jaxp.DocumentBuilderFactoryImpl;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.Parameter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.neethi.Policy;
import org.apache.neethi.PolicyEngine;
import org.apache.rampart.builder.TransportBindingBuilder;
import org.apache.rampart.policy.RampartPolicyBuilder;
import org.apache.rampart.policy.RampartPolicyData;
import org.apache.rampart.util.Axis2Util;
import org.apache.ws.secpolicy.WSSPolicyException;
import org.apache.ws.security.SOAPConstants;
import org.apache.ws.security.WSSecurityException;
import org.apache.ws.security.message.WSSecHeader;
import org.apache.ws.security.util.WSSecurityUtil;
import org.w3c.dom.Document;

import java.io.ByteArrayInputStream;
import java.util.List;

public class MessageBuilder {
    
    private static Log log = LogFactory.getLog(MessageBuilder.class);
    
    public void build(MessageContext msgCtx) throws WSSPolicyException,
            RampartException, WSSecurityException, AxisFault {
        

        DocumentBuilderFactoryImpl.setDOOMRequired(true);
        
        /*
         * First get the SOAP envelope as document, then create a security
         * header and insert into the document (Envelope)
         */
        Document doc = Axis2Util.getDocumentFromSOAPEnvelope(msgCtx.getEnvelope(), false);
        msgCtx.setEnvelope((SOAPEnvelope)doc.getDocumentElement());
        
        SOAPConstants soapConstants = WSSecurityUtil.getSOAPConstants(doc
                .getDocumentElement());
        
        WSSecHeader secHeader = new WSSecHeader();
        secHeader.insertSecurityHeader(doc);

        RampartMessageData rmd = new RampartMessageData(msgCtx, doc, true);

        Policy policy = null;
        /*
         * When creating the RampartMessageData instance we 
         * extract the service policy is set in the msgCtx.
         * If it is missing then try to obtain from the configuration files.
         */
        if(rmd.getServicePolicy() == null) {
            if(msgCtx.isServerSide()) {
                String policyXml = msgCtx.getEffectivePolicy().toString();
                policy = PolicyEngine.getPolicy(new ByteArrayInputStream(policyXml.getBytes()));
                
            } else {
                Parameter param = msgCtx.getParameter(RampartMessageData.KEY_RAMPART_POLICY);
                if(param != null) {
                    OMElement policyElem = param.getParameterElement().getFirstElement();
                    policy = PolicyEngine.getPolicy(policyElem);
                }
            }
            
            //Set the policy in the config ctx
            msgCtx.getConfigurationContext().setProperty(
                    RampartMessageData.getPolicyKey(msgCtx), policy);
            
            //Set the service policy
            rmd.setServicePolicy(policy);
        }
        
        List it = (List)rmd.getServicePolicy().getAlternatives().next();
        
        RampartPolicyData policyData = RampartPolicyBuilder.build(it);
     
        rmd.setPolicyData(policyData);
        rmd.setSecHeader(secHeader);
        
        processEnvelope(rmd);
    }


    
    private void processEnvelope(RampartMessageData rmd)
            throws RampartException, WSSecurityException {
        log.info("Before create Message assym....");
        
        //Nothing to do to handle the other bindings
        RampartPolicyData rpd = rmd.getPolicyData();
        if(rpd.isTransportBinding()) {
            TransportBindingBuilder building = new TransportBindingBuilder();
            building.build(rmd);
        }
        
    }
    
}
