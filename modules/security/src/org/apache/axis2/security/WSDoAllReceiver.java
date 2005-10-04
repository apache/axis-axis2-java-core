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

package org.apache.axis2.security;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.OperationContext;
import org.apache.axis2.om.OMException;
import org.apache.axis2.security.handler.WSDoAllHandler;
import org.apache.axis2.security.handler.WSSHandlerConstants;
import org.apache.axis2.security.util.Axis2Util;
import org.apache.axis2.soap.SOAPEnvelope;
import org.apache.axis2.soap.SOAPHeader;
import org.apache.axis2.soap.SOAPHeaderBlock;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ws.security.SOAPConstants;
import org.apache.ws.security.WSConstants;
import org.apache.ws.security.WSSecurityEngineResult;
import org.apache.ws.security.WSSecurityException;
import org.apache.ws.security.handler.RequestData;
import org.apache.ws.security.handler.WSHandlerConstants;
import org.apache.ws.security.handler.WSHandlerResult;
import org.apache.ws.security.message.token.Timestamp;
import org.apache.ws.security.util.WSSecurityUtil;
import org.apache.wsdl.WSDLConstants;
import org.w3c.dom.Document;

import java.security.cert.X509Certificate;
import java.util.Iterator;
import java.util.Vector;

import javax.security.auth.callback.CallbackHandler;

public class WSDoAllReceiver extends WSDoAllHandler {

    protected static Log log = LogFactory.getLog(WSDoAllReceiver.class.getName());
    
    
    public WSDoAllReceiver() {
    	super();
    	inHandler = true;
    }
    
	public void invoke(MessageContext msgContext) throws AxisFault {
    	boolean doDebug = log.isDebugEnabled();

    	/**
    	 * Cannot do the following right now since we cannot access the req 
    	 * mc when this handler runs in the client side.
    	 */
    	
    	//Copy the WSHandlerConstants.SEND_SIGV over to the new message 
    	//context - if it exists
    	if(!msgContext.isServerSide()) {//To make sure this is a response message 
    		OperationContext opCtx = msgContext.getOperationContext();
    		MessageContext outMsgCtx = opCtx.getMessageContext(WSDLConstants.MESSAGE_LABEL_IN_VALUE);
    		msgContext.setProperty(WSHandlerConstants.SEND_SIGV,outMsgCtx.getProperty(WSHandlerConstants.SEND_SIGV));
    	}
    	
        if (doDebug) {
            log.debug("WSDoAllReceiver: enter invoke() ");
        }
        reqData = new RequestData();
        
        try {
        	reqData.setMsgContext(msgContext);
        	
        	//Figureout if the handler should run
        	String inFlowSecurity = null;
        	if((inFlowSecurity = (String) getOption(WSSHandlerConstants.INFLOW_SECURITY)) == null) {
        		inFlowSecurity = (String) getProperty(msgContext, WSSHandlerConstants.INFLOW_SECURITY);
        	}
        	//If the option is not specified or if it is set to false do not do
        	//any security processing
        	if(inFlowSecurity == null || inFlowSecurity.equals(WSSHandlerConstants.OFF_OPTION)) {
        		return;
        	}
        	
            Vector actions = new Vector();
            String action = null;
            if ((action = (String) getOption(WSHandlerConstants.ACTION)) == null) {
                action = (String) getProperty(msgContext, WSHandlerConstants.ACTION);
            }
            if (action == null) {
                throw new AxisFault("WSDoAllReceiver: No action defined");
            }
            int doAction = WSSecurityUtil.decodeAction(action, actions);

            if(doAction == WSConstants.NO_SECURITY) {
            	return;
            }
            
            String actor = (String) getOption(WSHandlerConstants.ACTOR);

            Document doc = null;

            ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();
            
           //Setting the class loader
            if(msgContext.isServerSide()) {
            	Thread.currentThread().setContextClassLoader(msgContext.getServiceDescription().getClassLoader());
            } else {
            	//Thread.currentThread().setContextClassLoader(msgContext.getClass().getClassLoader());
            }
            
            try {
            	doc = Axis2Util.getDocumentFromSOAPEnvelope(msgContext.getEnvelope());
            } catch (WSSecurityException wssEx) {
            	throw new AxisFault("WSDoAllReceiver: Error in converting to Document", wssEx);
            }
        	
            //Do not process faults
            SOAPConstants soapConstants = WSSecurityUtil.getSOAPConstants(doc.getDocumentElement());
            if (WSSecurityUtil.findElement(doc.getDocumentElement(), "Fault",
					soapConstants.getEnvelopeURI()) != null) {
				return;
			}

            
            /*
            * To check a UsernameToken or to decrypt an encrypted message we
            * need a password.
            */
            CallbackHandler cbHandler = null;
            if ((doAction & (WSConstants.ENCR | WSConstants.UT)) != 0) {
                cbHandler = getPasswordCB(reqData);
            }
            

            
            /*
            * Get and check the Signature specific parameters first because
            * they may be used for encryption too.
            */

            doReceiverAction(doAction, reqData);

            Vector wsResult = null;
            try {
                wsResult = secEngine.processSecurityHeader(doc, actor,
                        cbHandler, reqData.getSigCrypto(), reqData.getDecCrypto());
            } catch (WSSecurityException ex) {
                ex.printStackTrace();
                throw new AxisFault(
                        "WSDoAllReceiver: security processing failed", ex);
            }
            if (wsResult == null) { // no security header found
                if (doAction == WSConstants.NO_SECURITY) {
                    return;
                } else {
                    throw new AxisFault(
                            "WSDoAllReceiver: Request does not contain required Security header");
                }
            }

            if (reqData.getWssConfig().isEnableSignatureConfirmation() && !msgContext.isServerSide()) {
                checkSignatureConfirmation(reqData, wsResult);
            }
            
            //Setting the original class loader
            Thread.currentThread().setContextClassLoader(originalClassLoader);
            
            //TODO: Copy the processed headers
            
            
            /**
             * Set the new SOAPEnvelope
             */
            
            try {
            	SOAPEnvelope envelope = Axis2Util.getSOAPEnvelopeFromDocument(doc,soapConstants.getEnvelopeURI());
            	msgContext.setEnvelope(envelope);
            } catch (WSSecurityException e) {
            	throw new AxisFault(
						"WSDoAllReceiver: Error in converting into a SOAPEnvelope",e);            	
            }
            
            /*
             * After setting the new current message, probably modified because
             * of decryption, we need to locate the security header. That is, we
             * force Axis (with getSOAPEnvelope()) to parse the string, build
             * the new header. Then we examine, look up the security header and
             * set the header as processed.
             *
             * Please note: find all header elements that contain the same actor
             * that was given to processSecurityHeader(). Then check if there is
             * a security header with this actor.
             */
            SOAPHeader header = null;
            try {
				header = msgContext.getEnvelope().getHeader();
			} catch (OMException ex) {
                throw new AxisFault(
                        "WSDoAllReceiver: cannot get SOAP header after security processing",
                        ex);
			}
			
			Iterator headers = header.examineHeaderBlocks(actor);
			
			SOAPHeaderBlock headerBlock = null;
			
			while(headers.hasNext()) { //Find the wsse header
				SOAPHeaderBlock hb = (SOAPHeaderBlock)headers.next();
                if (hb.getLocalName().equals(WSConstants.WSSE_LN)
                        && hb.getNamespace().getName().equals(WSConstants.WSSE_NS)) {
                    headerBlock = hb;
                    break;
                }
			}
            
			headerBlock.setProcessed();
			

            /*
            * Now we can check the certificate used to sign the message. In the
            * following implementation the certificate is only trusted if
            * either it itself or the certificate of the issuer is installed in
            * the keystore.
            *
            * Note: the method verifyTrust(X509Certificate) allows custom
            * implementations with other validation algorithms for subclasses.
            */

            // Extract the signature action result from the action vector
            WSSecurityEngineResult actionResult = WSSecurityUtil
                    .fetchActionResult(wsResult, WSConstants.SIGN);

            if (actionResult != null) {
                X509Certificate returnCert = actionResult.getCertificate();

                if (returnCert != null) {
                    if (!verifyTrust(returnCert, reqData)) {
                        throw new AxisFault(
                                "WSDoAllReceiver: The certificate used for the signature is not trusted");
                    }
                }
            }
            
            /*
             * Perform further checks on the timestamp that was transmitted in
             * the header. In the following implementation the timestamp is
             * valid if it was created after (now-ttl), where ttl is set on
             * server side, not by the client.
             *
             * Note: the method verifyTimestamp(Timestamp) allows custom
             * implementations with other validation algorithms for subclasses.
             */

             // Extract the timestamp action result from the action vector
             actionResult = WSSecurityUtil.fetchActionResult(wsResult,
                     WSConstants.TS);

             if (actionResult != null) {
                 Timestamp timestamp = actionResult.getTimestamp();

                 if (timestamp != null) {
                     String ttl = null;
                     if ((ttl = (String) getOption(WSHandlerConstants.TTL_TIMESTAMP)) == null) {
                         ttl = (String) getProperty(msgContext,WSHandlerConstants.TTL_TIMESTAMP);
                     }
                     int ttl_i = 0;
                     if (ttl != null) {
                         try {
                             ttl_i = Integer.parseInt(ttl);
                         } catch (NumberFormatException e) {
                             ttl_i = reqData.getTimeToLive();
                         }
                     }
                     if (ttl_i <= 0) {
                         ttl_i = reqData.getTimeToLive();
                     }

                     if (!verifyTimestamp(timestamp, reqData.getTimeToLive())) {
                         throw new AxisFault(
                                 "WSDoAllReceiver: The timestamp could not be validated");
                     }
                 }
             }
       
             /*
             * now check the security actions: do they match, in right order?
             */
             if (!checkReceiverResults(wsResult, actions)) {
                        throw new AxisFault(
                                "WSDoAllReceiver: security processing failed (actions mismatch)");

            }
            /*
            * All ok up to this point. Now construct and setup the security
            * result structure. The service may fetch this and check it.
            * Also the DoAllSender will use this in certain situations such as:
            * USE_REQ_SIG_CERT to encrypt
            */
             Vector results = null;
             if ((results = (Vector) getProperty(msgContext, WSHandlerConstants.RECV_RESULTS)) == null) {
                 results = new Vector();
                 msgContext
                         .setProperty(WSHandlerConstants.RECV_RESULTS, results);
             }
             WSHandlerResult rResult = new WSHandlerResult(actor, wsResult);
             results.add(0, rResult);
             if (doDebug) {
                 log.debug("WSDoAllReceiver: exit invoke()");
             }
             
        } catch (WSSecurityException wssEx) {
        	throw new AxisFault(wssEx);
        } finally {
            reqData.clear();
            reqData = null;
        }
        
    }


}
