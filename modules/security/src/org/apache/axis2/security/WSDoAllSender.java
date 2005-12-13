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

import java.util.Vector;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.OperationContext;
import org.apache.axis2.om.impl.dom.DocumentImpl;
import org.apache.axis2.om.impl.dom.jaxp.DocumentBuilderFactoryImpl;
import org.apache.axis2.security.handler.WSDoAllHandler;
import org.apache.axis2.security.handler.WSSHandlerConstants;
import org.apache.axis2.security.util.Axis2Util;
import org.apache.axis2.security.util.HandlerParameterDecoder;
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

	private static final long serialVersionUID = 3016802164501419165L;

	protected static Log log = LogFactory.getLog(WSDoAllSender.class.getName());
	
	/**
	 * TODO: This is not handled right now since converting to DOOM does not preserve
	 * the optimization information of the text nodes
	 */
	private boolean preserveOriginalEnvelope = false;
	
    public WSDoAllSender() {
    	super();
    	inHandler = false;
    }
	
	public void invoke(MessageContext msgContext) throws AxisFault {
		
		//Set the DOM impl to DOOM
		String originalDOcumentBuilderFactory = System.getProperty(DocumentBuilderFactory.class.getName());
		System.setProperty(DocumentBuilderFactory.class.getName(),DocumentBuilderFactoryImpl.class.getName());
		
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
        
        reqData = new RequestData();
        
        reqData.setNoSerialization(false);
        reqData.setMsgContext(msgContext);
        
    	//Figureout if the handler should run
    	Object outFlowSecurity;
    	if((outFlowSecurity = getOption(WSSHandlerConstants.OUTFLOW_SECURITY)) == null) {
    		outFlowSecurity = getProperty(msgContext, WSSHandlerConstants.OUTFLOW_SECURITY);
    	}

    	if(outFlowSecurity == null) {
    		return;
    	}
    	
        try {
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
				if (reqData.getUsername() == null
						|| reqData.getUsername().equals("")) {
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
            		doc = Axis2Util.getDocumentFromSOAPEnvelope(msgContext.getEnvelope());
            	} catch (WSSecurityException wssEx) {
            		throw new AxisFault("WSDoAllReceiver: Error in converting to Document", wssEx);
            	}
            }
	     

        	doSenderAction(doAction, doc, reqData, actions, !msgContext.isServerSide());

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

    	        String preserve = null;
                if ((preserve = (String) getOption(WSSHandlerConstants.PRESERVE_ORIGINAL_ENV)) == null) {
                    preserve = (String) getProperty(msgContext, WSSHandlerConstants.PRESERVE_ORIGINAL_ENV);
                }
                if(preserve != null) {
                	this.preserveOriginalEnvelope = "true".equalsIgnoreCase(preserve);
                }
                
            	msgContext.setEnvelope((SOAPEnvelope)doc.getDocumentElement());
            	
            	((MessageContext)reqData.getMsgContext()).setProperty(WSHandlerConstants.SND_SECURITY, null);
            }
    		
//	        log.debug("Creating LLOM Structure");
//	        OMElement docElem = (OMElement)doc.getDocumentElement();
//	        StAXSOAPModelBuilder stAXSOAPModelBuilder = new StAXSOAPModelBuilder(docElem.getXMLStreamReader(), SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI);
//	        log.debug("Creating LLOM Structure - DONE");
//			msgContext.setEnvelope(stAXSOAPModelBuilder.getSOAPEnvelope());
            msgContext.setEnvelope(Axis2Util.getSOAPEnvelopeFromDOOMDocument((DocumentImpl)doc));
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
				/**
				 * Preserving the OM stuff doesn't work for the repeting case
				 */
				this.preserveOriginalEnvelope = false;

				this.invoke(msgContext);
	        }
	        
	        if (doDebug) {
				log.debug("WSDoAllSender: exit invoke()");
			}
        } catch (WSSecurityException e) {
            throw new AxisFault(e.getMessage(), e);
        } finally {
            if(reqData != null) {
            	reqData.clear();
            	reqData = null;
            }
            
            //Reset the document builder factory
            String docBuilderFactory = System.getProperty(DocumentBuilderFactory.class.getName());
            if(docBuilderFactory != null && docBuilderFactory.equals(DocumentBuilderFactoryImpl.class.getName())) {
				if(originalDOcumentBuilderFactory != null) {
					System.getProperties().remove(docBuilderFactory);
				} else {
					System.getProperties().remove(docBuilderFactory);
				}
            }
        }     
    }
}

