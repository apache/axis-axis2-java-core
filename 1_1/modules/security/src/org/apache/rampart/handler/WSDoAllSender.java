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

package org.apache.rampart.handler;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.SOAPHeader;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.OperationContext;
import org.apache.axis2.description.Parameter;
import org.apache.rampart.RampartException;
import org.apache.rampart.conversation.ConversationConfiguration;
import org.apache.rampart.conversation.STSRequester;
import org.apache.rampart.conversation.Util;
import org.apache.rampart.util.Axis2Util;
import org.apache.rampart.util.HandlerParameterDecoder;
import org.apache.rampart.util.MessageOptimizer;
import org.apache.rahas.Token;
import org.apache.rahas.TrustException;
import org.apache.rahas.TrustUtil;
import org.apache.axis2.wsdl.WSDLConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ws.security.WSConstants;
import org.apache.ws.security.WSSecurityException;
import org.apache.ws.security.components.crypto.Crypto;
import org.apache.ws.security.handler.RequestData;
import org.apache.ws.security.handler.WSHandlerConstants;
import org.apache.ws.security.message.WSSecDKEncrypt;
import org.apache.ws.security.message.WSSecEncryptedKey;
import org.apache.ws.security.message.WSSecHeader;
import org.apache.ws.security.message.token.SecurityContextToken;
import org.apache.ws.security.util.WSSecurityUtil;
import org.apache.ws.security.util.XmlSchemaDateFormat;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.security.cert.X509Certificate;
import java.text.DateFormat;
import java.util.Date;
import java.util.Vector;

/**
 * @deprecated
 */
public class WSDoAllSender extends WSDoAllHandler {
    
    private static final Log log = LogFactory.getLog(WSDoAllSender.class);
    
    
    public WSDoAllSender() {
        super();
        inHandler = false;
    }
      
    public void processMessage(MessageContext msgContext) throws AxisFault {
        
        String disableDoomValue = (String)msgContext.getProperty(WSSHandlerConstants.DISABLE_DOOM);
        boolean disableDoom = disableDoomValue != null && Constants.VALUE_TRUE.equalsIgnoreCase(disableDoomValue);
        
        RequestData reqData = new RequestData();
        try {
            Parameter param = ConversationConfiguration.getParameter(msgContext);
            
            if(param == null || WSSHandlerConstants.RST_ACTON_SCT.equals(msgContext.getWSAAction()) ||
                    WSSHandlerConstants.RSTR_ACTON_SCT.equals(msgContext.getWSAAction()) ||
                    WSSHandlerConstants.RSTR_ACTON_ISSUE.equals(msgContext.getWSAAction())) {
                //If the msgs are msgs to an STS then use basic WS-Sec
                processBasic(msgContext, disableDoom, reqData);
            } else {
                processSecConv(msgContext);
            }
            
        } catch (Exception e) {
            throw new AxisFault(e.getMessage(), e);
        }
        finally {
            if(reqData != null) {
                reqData.clear();
                reqData = null;
            }
        }     
    }

    /**
     * Use WS-SecureConversation to secure the messages
     * @param msgContext
     * @throws Exception
     */
    private void processSecConv(MessageContext msgContext) throws Exception {
        //Parse the Conversation configuration
        ConversationConfiguration config = ConversationConfiguration.load(msgContext, true);
        if(config != null)
        msgContext.setEnvelope((SOAPEnvelope) config.getDocument()
                .getDocumentElement());
        
        if(!config.getMsgCtx().isServerSide()) {
            if(config.getContextIdentifier() == null && !config.getMsgCtx().isServerSide()) {
      
                String sts = config.getStsEPRAddress();
                if(sts != null) {
                  //Use a security token service
                    Axis2Util.useDOOM(false);
                    STSRequester.issueRequest(config);
                    Axis2Util.useDOOM(true);
                } else {
                    //Create an an SCT, include it in an RSTR 
                    // and add the RSTR to the header
                    this.createRSTR(config);
                }
                
            }
        }
        this.constructMessage(config);
    }
    
    /**
     * This will carryout the WS-Security related operations.
     * 
     * @param msgContext
     * @param disableDoom
     * @throws WSSecurityException
     * @throws AxisFault
     */
    private void processBasic(MessageContext msgContext, boolean disableDoom,
            RequestData reqData) throws WSSecurityException, AxisFault {
        boolean doDebug = log.isDebugEnabled();
        
        try {
            HandlerParameterDecoder.processParameters(msgContext,false);
        } catch (Exception e) {
            throw new AxisFault("Configureation error", e);
        }
        
        if (doDebug) {
            log.debug("WSDoAllSender: enter invoke()");
        }
        
        /*
         * Copy the RECV_RESULTS over to the current message context
         * - IF available 
         */
        OperationContext opCtx = msgContext.getOperationContext();
        MessageContext inMsgCtx;
        if(opCtx != null && 
                (inMsgCtx = opCtx.getMessageContext(WSDLConstants.MESSAGE_LABEL_IN_VALUE)) != null) {
            msgContext.setProperty(WSHandlerConstants.RECV_RESULTS, 
                    inMsgCtx.getProperty(WSHandlerConstants.RECV_RESULTS));
        }
        
        
        
        reqData.setNoSerialization(false);
        reqData.setMsgContext(msgContext);
        
        if (((getOption(WSSHandlerConstants.OUTFLOW_SECURITY)) == null) &&
                ((getProperty(msgContext, WSSHandlerConstants.OUTFLOW_SECURITY)) == null)) {
                
                if (msgContext.isServerSide() && 
                    ((getOption(WSSHandlerConstants.OUTFLOW_SECURITY_SERVER)) == null) &&
                    ((getProperty(msgContext, WSSHandlerConstants.OUTFLOW_SECURITY_SERVER)) == null)) {
                
                    return;
                } else if (((getOption(WSSHandlerConstants.OUTFLOW_SECURITY_CLIENT)) == null) &&
                        ((getProperty(msgContext, WSSHandlerConstants.OUTFLOW_SECURITY_CLIENT)) == null))  {
                    
                    return;
                }
            }
        
        Vector actions = new Vector();
        String action = null;
        if ((action = (String) getOption(WSSHandlerConstants.ACTION_ITEMS)) == null) {
            action = (String) getProperty(msgContext, WSSHandlerConstants.ACTION_ITEMS);
        }
        if (action == null) {
            throw new AxisFault("WSDoAllReceiver: No action items defined");
        }
        
        int doAction = WSSecurityUtil.decodeAction(action, actions);
        if (doAction == WSConstants.NO_SECURITY) {
            return;
        }
        
        /*
         * For every action we need a username, so get this now. The
         * username defined in the deployment descriptor takes precedence.
         */
        reqData.setUsername((String) getOption(WSHandlerConstants.USER));
        if (reqData.getUsername() == null || reqData.getUsername().length() == 0) {
            String username = (String) getProperty(reqData.getMsgContext(), WSHandlerConstants.USER);
            if (username != null) {
                reqData.setUsername(username);
            }
        }
        
        /*
         * Now we perform some set-up for UsernameToken and Signature
         * functions. No need to do it for encryption only. Check if
         * username is available and then get a passowrd.
         */
        if ((doAction & (WSConstants.SIGN | WSConstants.UT | WSConstants.UT_SIGN)) != 0) {
            /*
             * We need a username - if none throw an AxisFault. For
             * encryption there is a specific parameter to get a username.
             */
            if (reqData.getUsername() == null
                    || reqData.getUsername().length() == 0) {
                throw new AxisFault(
                "WSDoAllSender: Empty username for specified action");
            }
        }
        
        /*
         * Now get the SOAPEvelope from the message context and convert it
         * into a Document
         * 
         * Now we can perform our security operations on this request.
         */
        
        
        Document doc = null;
        /*
         * If the message context property conatins a document then this is
         * a chained handler.
         */
        if ((doc = (Document) ((MessageContext)reqData.getMsgContext())
                .getProperty(WSHandlerConstants.SND_SECURITY)) == null) {
            try {
                doc = Axis2Util.getDocumentFromSOAPEnvelope(msgContext.getEnvelope(), disableDoom);
            } catch (WSSecurityException wssEx) {
                throw new AxisFault("WSDoAllReceiver: Error in converting to Document", wssEx);
            }
        }
        
        
        doSenderAction(doAction, doc, reqData, actions, !msgContext.isServerSide());
        
        /*
         * If noSerialization is false, this handler shall be the last (or
         * only) one in a handler chain. If noSerialization is true, just
         * set the processed Document in the transfer property. The next
         * Axis WSS4J handler takes it and performs additional security
         * processing steps.
         *
         */
        if (reqData.isNoSerialization()) {
            ((MessageContext)reqData.getMsgContext()).setProperty(WSHandlerConstants.SND_SECURITY,
                    doc);
        } else {
            msgContext.setEnvelope((SOAPEnvelope)doc.getDocumentElement());
            ((MessageContext)reqData.getMsgContext()).setProperty(WSHandlerConstants.SND_SECURITY, null);
        }
        

        /**
         * If the optimizeParts parts are set then optimize them
         */
        String optimizeParts;
        
        if((optimizeParts = (String) getOption(WSSHandlerConstants.OPTIMIZE_PARTS)) == null) {
            optimizeParts = (String)
            getProperty(reqData.getMsgContext(), WSSHandlerConstants.OPTIMIZE_PARTS);
        }
        if(optimizeParts != null) {
            // Optimize the Envelope
            MessageOptimizer.optimize(msgContext.getEnvelope(),optimizeParts);
        }
        
        //Enable handler repetition
        Integer repeat;
        int repeatCount;
        if ((repeat = (Integer)getOption(WSSHandlerConstants.SENDER_REPEAT_COUNT)) == null) {
            repeat = (Integer)
            getProperty(reqData.getMsgContext(), WSSHandlerConstants.SENDER_REPEAT_COUNT);
        }
        
        repeatCount = repeat.intValue();
        
        //Get the current repetition from message context
        int repetition = this.getCurrentRepetition(msgContext);
        
        if(repeatCount > 0 && repetition < repeatCount) {
            
            reqData.clear();
            reqData = null;
            
            // Increment the repetition to indicate the next repetition
            // of the same handler
            repetition++;
            msgContext.setProperty(WSSHandlerConstants.CURRENT_REPETITON,
                    new Integer(repetition));
            
            this.invoke(msgContext);
        }
        
        if (doDebug) {
            log.debug("WSDoAllSender: exit invoke()");
        }
        log.debug(msgContext.getEnvelope());
    }
    

    /**
     * Create the self created <code>wsc:SecurityContextToken</code> and 
     * add it to a <code>wst:RequestSecurityTokenResponse</code>.
     * 
     * This is called in the case where the security context establishment 
     * is done by one of the parties with out the use of an STS
     * and the creted SCT is sent across to the other party in an unsolicited 
     * <code>wst:RequestSecurityTokenResponse</code>
     * 
     * @param config
     * @throws Exception
     */
    private void createRSTR(ConversationConfiguration config) throws Exception {
        
        WSSecEncryptedKey encrKeyBuilder = new WSSecEncryptedKey();
        Crypto crypto = org.apache.rampart.conversation.Util.getCryptoInstace(config);
        String encryptionUser = config.getEncryptionUser();
        if(encryptionUser == null) {
            throw new RampartException("missingEncryptionUser");
        }
        X509Certificate cert = crypto.getCertificates(encryptionUser)[0];
        
        encrKeyBuilder.setKeyIdentifierType(WSConstants.THUMBPRINT_IDENTIFIER);
        try {
            encrKeyBuilder.setUseThisCert(cert);
            encrKeyBuilder.prepare(config.getDocument(), crypto);
        } catch (WSSecurityException e) {
            throw new TrustException(
                    "errorInBuildingTheEncryptedKeyForPrincipal",
                    new String[] { cert.getSubjectDN().getName()}, e);
        }
        
        SecurityContextToken sct = new SecurityContextToken(config.getDocument());
        Util.resgisterContext(sct.getIdentifier(), config);
        
        //Creation and expiration times
        Date creationTime = new Date();
        Date expirationTime = new Date();
        
        expirationTime.setTime(creationTime.getTime() + 300000);
        
        Token token = new Token(sct.getIdentifier(), (OMElement)sct.getElement(), creationTime, expirationTime);
        token.setSecret(encrKeyBuilder.getEphemeralKey());
        
        config.getTokenStore().add(token);
        
        SOAPEnvelope env = config.getMsgCtx().getEnvelope();

        SOAPHeader header = env.getHeader();
        if(header == null) {
            header = ((SOAPFactory)env.getOMFactory()).createSOAPHeader(env);
        }
        
        OMElement rstrElem = TrustUtil.createRequestSecurityTokenResponseElement(config.getWstVersion(), header);

        OMElement rstElem = TrustUtil.createRequestedSecurityTokenElement(config.getWstVersion(), rstrElem);

        // Use GMT time in milliseconds
        DateFormat zulu = new XmlSchemaDateFormat();
        
        // Add the Lifetime element
        TrustUtil.createLifetimeElement(config.getWstVersion(), rstrElem, zulu
                .format(creationTime), zulu.format(expirationTime));
        
        rstElem.addChild((OMElement)sct.getElement());
        
        TrustUtil.createRequestedAttachedRef(config.getWstVersion(), rstrElem,
                "#" + sct.getID(), WSSHandlerConstants.TOK_TYPE_SCT);

        TrustUtil
                .createRequestedUnattachedRef(config.getWstVersion(), rstrElem,
                        sct.getIdentifier(), WSSHandlerConstants.TOK_TYPE_SCT);
        
        Element encryptedKeyElem = encrKeyBuilder.getEncryptedKeyElement();
        Element bstElem = encrKeyBuilder.getBinarySecurityTokenElement();
        
        OMElement reqProofTok = TrustUtil.createRequestedProofTokenElement(
                config.getWstVersion(), rstrElem);

        if(bstElem != null) {
            reqProofTok.addChild((OMElement)bstElem);
        }
        
        reqProofTok.addChild((OMElement)encryptedKeyElem);
        
    }
    
    private void constructMessage(ConversationConfiguration config) throws Exception {

        Document doc = config.getDocument();

        WSSecHeader secHeader = new WSSecHeader();
        secHeader.insertSecurityHeader(doc);

        Token tempToken = config.getTokenStore().getToken(
                config.getContextIdentifier());
        byte[] tempSecret = tempToken.getSecret();

        SecurityContextToken sct = new SecurityContextToken((Element) doc
                .importNode((Element) tempToken.getToken(), true));

        // Derived key encryption
        WSSecDKEncrypt encrBuilder = new WSSecDKEncrypt();
        encrBuilder.setSymmetricEncAlgorithm(WSConstants.AES_128);
        OMElement attachedReference = tempToken.getAttachedReference();
        if(attachedReference != null) {
            encrBuilder.setExternalKey(tempSecret, (Element) doc.importNode(
                    (Element) attachedReference, true));
        } else {
            String tokenId = sct.getID();
            encrBuilder.setExternalKey(tempSecret, tokenId);
        }
        encrBuilder.build(doc, secHeader);

        WSSecurityUtil.prependChildElement(doc, secHeader.getSecurityHeader(),
                sct.getElement(), false);
    }
    
}
