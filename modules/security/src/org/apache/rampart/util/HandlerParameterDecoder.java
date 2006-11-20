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
package org.apache.rampart.util;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.Parameter;
import org.apache.rampart.handler.WSSHandlerConstants;
import org.apache.rampart.handler.config.InflowConfiguration;
import org.apache.rampart.handler.config.OutflowConfiguration;
import org.apache.ws.security.WSSecurityException;
import org.apache.ws.security.handler.WSHandlerConstants;

import javax.xml.namespace.QName;
import java.util.Iterator;

/**
 * This is used to process the security parameters from the configuration files
 * 
 * Example: <code>
 <br>

 </code>
 * 
 */
public class HandlerParameterDecoder {

	/**
	 * 
	 * @param msgCtx
	 * @param inflow
	 * @throws WSSecurityException
	 */
	public static void processParameters(MessageContext msgCtx, boolean inflow)
			throws Exception {
 		Parameter inFlowSecParam;
        	Parameter outFlowSecParam;
        
	        if(msgCtx.isServerSide()){
            		inFlowSecParam = msgCtx.getParameter(WSSHandlerConstants.INFLOW_SECURITY_SERVER);
            		outFlowSecParam = msgCtx.getParameter(WSSHandlerConstants.OUTFLOW_SECURITY_SERVER);
       		 }else{
            		inFlowSecParam = msgCtx.getParameter(WSSHandlerConstants.INFLOW_SECURITY_CLIENT);
            		outFlowSecParam = msgCtx.getParameter(WSSHandlerConstants.OUTFLOW_SECURITY_CLIENT);
        	}
        
        	//TODO: check whether policy is available 
        	if(inFlowSecParam == null){
            		inFlowSecParam = (Parameter)msgCtx.getProperty(WSSHandlerConstants.INFLOW_SECURITY);            
       		}

       	 	if(outFlowSecParam == null){
            		outFlowSecParam = (Parameter)msgCtx.getProperty(WSSHandlerConstants.OUTFLOW_SECURITY);            
       		 }
		
		//If the configs are not availabale in the file
		if(inFlowSecParam == null) {
			inFlowSecParam = msgCtx.getParameter(WSSHandlerConstants.INFLOW_SECURITY);
		}
		if(outFlowSecParam == null) {
			outFlowSecParam = msgCtx.getParameter(WSSHandlerConstants.OUTFLOW_SECURITY);
		}

		int repetitionCount = -1;

		/*
		 * Populate the inflow parameters
		 */
		if (inFlowSecParam != null && inflow) {
			OMElement inFlowParamElem = inFlowSecParam.getParameterElement();

			OMElement actionElem = inFlowParamElem
					.getFirstChildWithName(new QName(WSSHandlerConstants.ACTION));
			if (actionElem == null) {
				throw new Exception(
						"Inflow configurtion must contain an 'action' "
								+ "elementas the child of 'InflowSecurity' element");
			}

			Iterator childElements = actionElem.getChildElements();
			while (childElements.hasNext()) {
				OMElement element = (OMElement) childElements.next();
				msgCtx.setProperty(element.getLocalName(), element.getText().trim());
			}

		}

		/*
		 * Populate the outflow parameters
		 */
		if (outFlowSecParam != null && !inflow) {
			OMElement outFlowParamElem = outFlowSecParam.getParameterElement();
			
			Iterator childElements = outFlowParamElem.getChildElements();
			while (childElements.hasNext()) {
				OMElement element = (OMElement) childElements.next();
				
				if(!element.getLocalName().equals(WSSHandlerConstants.ACTION)) {
					throw new Exception(
							"Alian element '"
									+ element.getLocalName()
									+ "' in the 'OutFlowSecurity' element, " 
									+ "only 'action' elements can be present");
				}
				
                boolean signAllHeaders = false;
                boolean signBody = false;
                boolean encryptBody = false;
                
                repetitionCount++;
				Iterator paramElements = element.getChildElements();
				while (paramElements.hasNext()) {
					OMElement elem = (OMElement) paramElements.next();
                    String localName = elem.getLocalName();
                    String text = elem.getText().trim();
                    if(localName.equals(WSSHandlerConstants.SIGN_ALL_HEADERS)) {
                        signAllHeaders = true;
                    } else if(localName.equals(WSSHandlerConstants.SIGN_BODY)) {
                        signBody = true;
                    } else if(localName.equals(WSSHandlerConstants.ENCRYPT_BODY)) {
                        encryptBody = true;
                    } else {
                        msgCtx.setProperty(Axis2Util.getKey(localName,
							inflow,repetitionCount), text);
                    }
				}
                
                if(signAllHeaders || signBody || encryptBody) {
                    handleSignEncrParts(signAllHeaders, signBody, encryptBody,
                            msgCtx, repetitionCount);
                }
                
				
			}

			msgCtx.setProperty(WSSHandlerConstants.SENDER_REPEAT_COUNT,
					new Integer(repetitionCount));
		}
	}
    
    public static OutflowConfiguration getOutflowConfiguration(Parameter outflowConfigParam) throws AxisFault {
        if (outflowConfigParam != null) {
            OMElement outflowParamElem = outflowConfigParam.getParameterElement();

            OMElement actionElem = outflowParamElem
                    .getFirstChildWithName(new QName(WSSHandlerConstants.ACTION));
            if (actionElem == null) {
                throw new AxisFault(
                        "Inflow configurtion must contain an 'action' "
                                + "elementas the child of 'InflowSecurity' element");
            }

            OutflowConfiguration outflowConfiguration = new OutflowConfiguration();
            
            Iterator childElements = actionElem.getChildElements();
            while (childElements.hasNext()) {
                OMElement element = (OMElement) childElements.next();
                
                String localName = element.getLocalName();
                String text = element.getText().trim();
                if(localName.equals(WSHandlerConstants.PW_CALLBACK_CLASS)) {
                    outflowConfiguration.setPasswordCallbackClass(text);
                } else if(localName.equals(WSHandlerConstants.SIG_PROP_FILE)) {
                    outflowConfiguration.setSignaturePropFile(text);
                } else if(localName.equals(WSHandlerConstants.ENC_PROP_FILE)) {
                    outflowConfiguration.setEncryptionPropFile(text);
                } else if(localName.equals(WSHandlerConstants.ENC_CALLBACK_CLASS)) {
                    outflowConfiguration.setEmbeddedKeyCallbackClass(text);
                } else if(localName.equals(WSHandlerConstants.USER)) {
                    outflowConfiguration.setUser(text);
                } else if(localName.equals(WSHandlerConstants.ENCRYPTION_USER)) {
                    outflowConfiguration.setEncryptionUser(text);
                }
            }
            return outflowConfiguration;
        }
        return null;
    }
    
    public static InflowConfiguration getInflowConfiguration(Parameter inflowConfigParam) throws AxisFault {

        if (inflowConfigParam != null) {
            OMElement inFlowParamElem = inflowConfigParam.getParameterElement();

            OMElement actionElem = inFlowParamElem
                    .getFirstChildWithName(new QName(WSSHandlerConstants.ACTION));
            if (actionElem == null) {
                throw new AxisFault(
                        "Inflow configurtion must contain an 'action' "
                                + "elementas the child of 'InflowSecurity' element");
            }

            InflowConfiguration inflowConfiguration = new InflowConfiguration();
            
            Iterator childElements = actionElem.getChildElements();
            while (childElements.hasNext()) {
                OMElement element = (OMElement) childElements.next();
                
                String localName = element.getLocalName();
                String text = element.getText().trim();
                
                if(localName.equals(WSHandlerConstants.PW_CALLBACK_CLASS)) {
                    inflowConfiguration.setPasswordCallbackClass(text);
                } else if(localName.equals(WSHandlerConstants.SIG_PROP_FILE)) {
                    inflowConfiguration.setSignaturePropFile(text);
                } else if(localName.equals(WSHandlerConstants.DEC_PROP_FILE)) {
                    inflowConfiguration.setDecryptionPropFile(text);
                } else if (WSHandlerConstants.ENABLE_SIGNATURE_CONFIRMATION
                        .equals(localName)) {
                    if ("false".equals(text)
                            || "0".equals(text)) {
                        inflowConfiguration
                                .setEnableSignatureConfirmation(false);
                    }
                }
            }
            return inflowConfiguration;
        }
        return null;
    }

    private static void handleSignEncrParts(boolean signAllHeaders,
            boolean signBody, boolean encrBody, MessageContext msgCtx,
            int repetition) {
        String soapNs = msgCtx.getEnvelope().getNamespace().getNamespaceURI();
        if(signBody) {
            //Add body signPart
            String sigBodySigPart = "{Element}{" + soapNs + "}Body";
            addSigPart(sigBodySigPart, msgCtx, repetition);
        }
        if(encrBody) {
            //Encrypt body content
            String encrBodyEncrPart = "{}{" + soapNs + "}Body";
            addEncrPart(encrBodyEncrPart, msgCtx, repetition);
        }
        if(signAllHeaders) {
            Iterator children = msgCtx.getEnvelope().getHeader().getChildElements();
            while (children.hasNext()) {
                OMElement element = (OMElement) children.next();
                //Sign only the quilified headers
                //TODO check whether we can sign the unqualified header elements
                String ns = element.getNamespace().getNamespaceURI();
                if(ns != null && ns.length() > 0) {
                    addSigPart("{Element}{" + ns + "}" + element.getLocalName(),msgCtx, repetition);
                }
            }
        }
        
    }
    
    private static void addSigPart(String sigPart, MessageContext msgCtx, int repetition) {
        String key = Axis2Util.getKey(WSHandlerConstants.SIGNATURE_PARTS, false, repetition);
        String existingSignParts = (String) msgCtx.getProperty(key);
        if (existingSignParts != null && existingSignParts.length() > 0) {
            // If the part is not already there as a sign part
            if (existingSignParts.indexOf(sigPart) != -1) {
                msgCtx.setProperty(key, existingSignParts + ";" + sigPart);
            }
        } else {
            // If there are no signed parts
            msgCtx.setProperty(key, sigPart);
        }
    }
    
    private static void addEncrPart(String encrPart, MessageContext msgCtx, int repetition) {
        String key = Axis2Util.getKey(WSHandlerConstants.ENCRYPTION_PARTS, false, repetition);
        String existingEncrParts = (String) msgCtx.getProperty(key);
        if (existingEncrParts != null && existingEncrParts.length() > 0) {
            if (existingEncrParts.indexOf(encrPart) != -1) {
                msgCtx.setProperty(key, existingEncrParts + ";" + encrPart);
            }
        } else {
            msgCtx.setProperty(key, encrPart);
        }
    }
    
}
