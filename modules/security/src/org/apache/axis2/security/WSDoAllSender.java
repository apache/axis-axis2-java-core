/*
 * Copyright 2004,2005 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 * 
 *  
 */

package org.apache.axis2.security;

import java.util.Vector;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.OperationContext;
import org.apache.axis2.security.handler.WSDoAllHandler;
import org.apache.axis2.security.handler.WSSHandlerConstants;
import org.apache.axis2.security.util.Axis2Util;
import org.apache.axis2.security.util.MessageOptimizer;
import org.apache.axis2.soap.SOAPEnvelope;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ws.security.WSConstants;
import org.apache.ws.security.WSSecurityException;
import org.apache.ws.security.handler.RequestData;
import org.apache.ws.security.handler.WSHandlerConstants;
import org.apache.ws.security.util.WSSecurityUtil;
import org.apache.wsdl.WSDLConstants;
import org.w3c.dom.Document;

public class WSDoAllSender extends WSDoAllHandler {

	protected static Log log = LogFactory.getLog(WSDoAllSender.class.getName());
	
	/**
	 * Right now we convert the processed DOM - SOAP Envelope into
	 * and OM-SOAPEnvelope
	 * But in the simple case where only the wsse:Security header is inserted into the document
	 * we can insert only the wsse:Security header into the OM-SOAPEnvelope and preserve the 
	 * metadata of OM such as base64 MTOM optimization
	 */
	private boolean preserveOriginalEnvelope = true;
	
    public WSDoAllSender() {
    	super();
    	inHandler = false;
    }
	
	public void invoke(MessageContext msgContext) throws AxisFault {
		
        doDebug = log.isDebugEnabled();
        
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
        		(inMsgCtx = opCtx.getMessageContext(WSDLConstants.MESSAGE_LABEL_IN)) != null) {
        	msgContext.setProperty(WSHandlerConstants.RECV_RESULTS, 
        			inMsgCtx.getProperty(WSHandlerConstants.RECV_RESULTS));
        }
        
        reqData = new RequestData();
        
        reqData.setNoSerialization(false);
        reqData.setMsgContext(msgContext);
        try {
	        Vector actions = new Vector();
	        String action = null;
	        if ((action = (String) getOption(WSHandlerConstants.ACTION)) == null) {
	            action = (String) ((MessageContext)reqData.getMsgContext())
	                    .getProperty(WSHandlerConstants.ACTION);
	        }
	        if (action == null) {
	            throw new AxisFault("WSDoAllSender: No action defined");
	        }
	        int doAction = WSSecurityUtil.decodeAction(action, actions);
	        if (doAction == WSConstants.NO_SECURITY) {
	            return;
	        }
	
	        boolean mu = decodeMustUnderstand(reqData);
	
	        secEngine.setPrecisionInMilliSeconds(decodeTimestampPrecision(reqData));
	
	        String actor = null;
	        if ((actor = (String) getOption(WSHandlerConstants.ACTOR)) == null) {
	            actor = (String)
	                    getProperty(reqData.getMsgContext(), WSHandlerConstants.ACTOR);
	        }
	        reqData.setActor(actor);
	        		
            /*
             * For every action we need a username, so get this now. The
             * username defined in the deployment descriptor takes precedence.
             */
         reqData.setUsername((String) getOption(WSHandlerConstants.USER));
         if (reqData.getUsername() == null || reqData.getUsername().equals("")) {
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
             if (reqData.getUsername() == null || reqData.getUsername().equals("")) {
                 throw new AxisFault(
                         "WSDoAllSender: Empty username for specified action");
             }
         }
         
         if (doDebug) {
             log.debug("Action: " + doAction);
             log.debug("Actor: " + reqData.getActor() + ", mu: " + mu);
         }
         /*
		  * Now get the SOAPEvelope from the message context and convert it into
		  * a Document
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
            	doc = Axis2Util.getDocumentFromSOAPEnvelope(msgContext.getEnvelope());
            	} catch (WSSecurityException wssEx) {
            		throw new AxisFault("WSDoAllReceiver: Error in converting to Document", wssEx);
            	}
            }
	     	
            reqData.setSoapConstants(WSSecurityUtil.getSOAPConstants(doc
                    .getDocumentElement()));
            /*
                * Here we have action, username, password, and actor,
                * mustUnderstand. Now get the action specific parameters.
                */
            if ((doAction & WSConstants.UT) == WSConstants.UT) {
                decodeUTParameter(reqData);
            }
            /*
                * Here we have action, username, password, and actor,
                * mustUnderstand. Now get the action specific parameters.
                */
            if ((doAction & WSConstants.UT_SIGN) == WSConstants.UT_SIGN) {
                decodeUTParameter(reqData);
                decodeSignatureParameter(reqData);
            }
            /*
                * Get and check the Signature specific parameters first because
                * they may be used for encryption too.
                */
            if ((doAction & WSConstants.SIGN) == WSConstants.SIGN) {
                reqData.setSigCrypto(loadSignatureCrypto(reqData));
                decodeSignatureParameter(reqData);
            }
            /*
                * If we need to handle signed SAML token then we need may of the
                * Signature parameters. The handle procedure loads the signature
                * crypto file on demand, thus don't do it here.
                */
            if ((doAction & WSConstants.ST_SIGNED) == WSConstants.ST_SIGNED) {
                decodeSignatureParameter(reqData);
            }
            /*
                * Set and check the encryption specific parameters, if necessary
                * take over signature parameters username and crypto instance.
                */
            if ((doAction & WSConstants.ENCR) == WSConstants.ENCR) {
                reqData.setEncCrypto(loadEncryptionCrypto(reqData));
                decodeEncryptionParameter(reqData);
            }
            /*
                * Here we have all necessary information to perform the requested
                * action(s).
                */
            for (int i = 0; i < actions.size(); i++) {

                int actionToDo = ((Integer) actions.get(i)).intValue();
                if (doDebug) {
                    log.debug("Performing Action: " + actionToDo);
                }

                switch (actionToDo) {
                case WSConstants.UT:
                    performUTAction(actionToDo, mu, doc, reqData);
                    break;

                case WSConstants.ENCR:
                    performENCRAction(actionToDo, mu, doc, reqData);
                    this.preserveOriginalEnvelope = false;
                    break;

                case WSConstants.SIGN:
                    performSIGNAction(actionToDo, mu, doc, reqData);
                    break;

                case WSConstants.ST_SIGNED:
                    performST_SIGNAction(actionToDo, mu, doc, reqData);
                    break;

                case WSConstants.ST_UNSIGNED:
                    performSTAction(actionToDo, mu, doc, reqData);
                    break;

                case WSConstants.TS:
                    performTSAction(actionToDo, mu, doc, reqData);
                    break;

                case WSConstants.UT_SIGN:
                    performUT_SIGNAction(actionToDo, mu, doc, reqData);
                    break;

                case WSConstants.NO_SERIALIZE:
                    reqData.setNoSerialization(true);
                    break;
                }
            }

            /*
                * If required convert the resulting document into a message first.
                * The outputDOM() method performs the necessary c14n call. After
                * that we extract it as a string for further processing.
                *
                * Set the resulting byte array as the new SOAP message.
                *
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
            	SOAPEnvelope processedEnv = null;
            	if(preserveOriginalEnvelope) {
            		processedEnv = Axis2Util.getSOAPEnvelopeFromDocument(doc,reqData.getSoapConstants(), msgContext.getEnvelope());
            	} else {
            		processedEnv = Axis2Util.getSOAPEnvelopeFromDocument(doc, reqData.getSoapConstants().getEnvelopeURI());
            	}
            	msgContext.setEnvelope(processedEnv);
            	((MessageContext)reqData.getMsgContext()).setProperty(WSHandlerConstants.SND_SECURITY, null);
            }
            
            msgContext.getEnvelope().build();
            
    		
            /**
             * If the optimizeParts parts are set then optimize them
             */
			String optimizeParts;
			
			if((optimizeParts = (String) getOption(WSSHandlerConstants.Out.OPTIMIZE_PARTS)) == null) {
				optimizeParts = (String)
                	getProperty(reqData.getMsgContext(), WSSHandlerConstants.Out.OPTIMIZE_PARTS);
			}
            if(optimizeParts != null) {
	            // Optimize the Envelope
	            MessageOptimizer.optimize(msgContext.getEnvelope(),optimizeParts);
            }
            
            
            //Enable handler repetition
            String repeat;
            int repeatCount;
	        if ((repeat = (String) getOption(WSSHandlerConstants.Out.SENDER_REPEAT_COUNT)) == null) {
	            repeat = (String)
	                    getProperty(reqData.getMsgContext(), WSSHandlerConstants.Out.SENDER_REPEAT_COUNT);
	        }
	        
	        if(repeat != null) {
		        try {
		        	repeatCount = Integer.parseInt(repeat);
		        } catch (NumberFormatException nfex) {
		        	throw new AxisFault("Repetition count of WSDoAllSender should be an integer");
		        }
	            
		        //Get the current repetition from message context
		        int repetition = this.getRepetition(msgContext);
		        
		        if(repeatCount > 0 && repetition < repeatCount) {
		        	reqData.clear();
		        	reqData = null;
		        	
		        	//Increment the repetition to indicate the next repetition 
		        	//of the same handler
		        	repetition++;
		        	msgContext.setProperty(WSSHandlerConstants.Out.REPETITON,new Integer(repetition));
		        	
		        	/**
		        	 * eserving the OM stuff doesn't work for the repeting case
		        	 */
		        	this.preserveOriginalEnvelope = false;
		        	
		        	this.invoke(msgContext);
		        }
	        }

            if (doDebug) {
				log.debug("WSDoAllSender: exit invoke()");
			}
        } catch (WSSecurityException e) {
        	e.printStackTrace();
            throw new AxisFault(e.getMessage(), e);
        } finally {
            if(reqData != null) {
            	reqData.clear();
            	reqData = null;
            }
        }        
    }
}

