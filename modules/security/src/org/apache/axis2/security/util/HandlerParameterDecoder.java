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
package org.apache.axis2.security.util;

import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.om.OMElement;
import org.apache.axis2.security.handler.WSSHandlerConstants;
import org.apache.ws.security.WSSecurityException;

import java.util.Iterator;

/**
 * This is used to process the security parameters from the
 * configuration files
 * 
 * Example:
 <code>
 <br>
 &nbsp;&lt;parameter name="InflowSecurity"&gt;<br>
 &nbsp;&nbsp;&nbsp;&nbsp;&lt;action&gt;Timestamp Signature Encrypt&lt;/action&gt;<br>
 &nbsp;&nbsp;&nbsp;&nbsp;&lt;passwordCallbackClass&gt;org.apache.axis2.security.PWCallback&lt;/passwordCallbackClass&gt;<br>
 &nbsp;&nbsp;&nbsp;&nbsp;&lt;signaturePropFile&gt;interop.properties&lt;/signaturePropFile&gt;<br>
 &nbsp;&lt;/parameter&gt;<br>


 &nbsp;&lt;parameter name="OutflowSecurity"&gt;<br>
 &nbsp;&nbsp;&nbsp;&nbsp;&lt;action&gt;Timestamp Signature Encrypt&lt;/action&gt;<br>
 &nbsp;&nbsp;&nbsp;&nbsp;&lt;user&gt;bob&lt;/user&gt;<br>
 &nbsp;&nbsp;&nbsp;&nbsp;&lt;passwordCallbackClass&gt;org.apache.axis2.security.PWCallback&lt;/passwordCallbackClass&gt;<br>
 &nbsp;&nbsp;&nbsp;&nbsp;&lt;signaturePropFile&gt;interop.properties&lt;/signaturePropFile&gt;<br>
 &nbsp;&nbsp;&nbsp;&nbsp;&lt;signatureKeyIdentifier&gt;SKIKeyIdentifier&lt;/signatureKeyIdentifier&gt;<br>
 &nbsp;&nbsp;&nbsp;&nbsp;&lt;encryptionKeyIdentifier&gt;SKIKeyIdentifier&lt;/encryptionKeyIdentifier&gt;<br>
 &nbsp;&nbsp;&nbsp;&nbsp;&lt;encryptionUser&gt;alice&lt;/encryptionUser&gt;<br>
 &nbsp;&nbsp;&nbsp;&nbsp;&lt;encryptionSymAlgorithm&gt;http://www.w3.org/2001/04/xmlenc#aes128-cbc&lt;/encryptionSymAlgorithm&gt;<br>
 &nbsp;&nbsp;&nbsp;&nbsp;&lt;signatureParts&gt;{Element}{http://schemas.xmlsoap.org/ws/2004/08/addressing}To;{Element}{http://schemas.xmlsoap.org/ws/2004/08/addressing}ReplyTo;{Element}{http://schemas.xmlsoap.org/ws/2004/08/addressing}From;{Element}{http://schemas.xmlsoap.org/ws/2004/08/addressing}RelatesTo;{Element}{http://schemas.xmlsoap.org/ws/2004/08/addressing}MessageID;{Element}{http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd}Timestamp&lt;/signatureParts&gt;<br>

 &nbsp;&nbsp;&nbsp;&nbsp;&lt;optimizeParts&gt;//xenc:EncryptedData/xenc:CipherData/xenc:CipherValue&lt;/optimizeParts&gt;<br>

 &nbsp;&nbsp;&nbsp;&nbsp;&lt;repetition count="1"&gt;<br>
 &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;user&gt;alice&lt;/user&gt;<br>
 &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;action&gt;Signature Timestamp&lt;/user&gt;<br>
 &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;passwordCallbackClass&gt;org.apache.axis2.security.PWCallback&lt;/user&gt;<br>
 &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;signaturePropFile&gt;interop.properties&lt;/user&gt;<br>
 &nbsp;&nbsp;&nbsp;&nbsp;&lt;/repetition&gt;<br>
 &nbsp;&lt;/parameter&gt;<br>
 </code>
 * 
 * 
 * @author Ruchith Fernando (ruchith.fernando@gmail.com)
 */
public class HandlerParameterDecoder {

	/**
	 * 
	 * @param msgCtx
	 * @param inflow
	 * @throws WSSecurityException
	 */
	public static void processParameters(MessageContext msgCtx, boolean inflow) throws Exception {
		
		Parameter inFlowSecParam = msgCtx.getParameter(WSSHandlerConstants.INFLOW_SECURITY);
		
		Parameter outFlowSecParam = msgCtx.getParameter(WSSHandlerConstants.OUTFLOW_SECURITY);
		
		int repetitionCount = 0;

		/*
		 * Populate the inflow parameters
		 */
		if(inFlowSecParam != null && inflow) {
			OMElement inFlowParamElem = inFlowSecParam.getParameterElement();
			Iterator childElements = inFlowParamElem.getChildElements();
			while (childElements.hasNext()) {
				OMElement element = (OMElement) childElements.next();
				msgCtx.setProperty(element.getLocalName(),element.getText());
			}
		}
		
		/*
		 * Populate the ourflow parameters
		 */
		if(outFlowSecParam != null && !inflow) {
			OMElement outFlowParamElem = outFlowSecParam.getParameterElement();
			Iterator childElements = outFlowParamElem.getChildElements();
			while (childElements.hasNext()) {
				OMElement element = (OMElement) childElements.next();
				if(!element.getLocalName().equals("repetition")) {
					msgCtx.setProperty(element.getLocalName(),element.getText());
				} else {
					//Handle the repetition configuration
					repetitionCount++;
					Iterator repetitionParamElems = element.getChildElements();
					while (repetitionParamElems.hasNext()) {
						OMElement elem = (OMElement) repetitionParamElems.next();
						msgCtx.setProperty(elem.getLocalName()+1,elem.getText());
					}
					
				}
			}
		}
		
		msgCtx.setProperty(WSSHandlerConstants.SENDER_REPEAT_COUNT,new Integer(repetitionCount));
		
	}
	
}
