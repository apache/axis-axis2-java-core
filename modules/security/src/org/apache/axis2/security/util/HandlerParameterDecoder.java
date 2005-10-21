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
