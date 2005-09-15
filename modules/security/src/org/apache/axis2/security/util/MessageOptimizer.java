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

import java.util.Iterator;
import java.util.List;

import org.apache.axis2.om.OMElement;
import org.apache.axis2.om.OMText;
import org.apache.axis2.om.xpath.AXIOMXPath;
import org.apache.axis2.soap.SOAPEnvelope;
import org.apache.ws.security.WSConstants;
import org.apache.ws.security.WSSecurityException;
import org.jaxen.JaxenException;
import org.jaxen.SimpleNamespaceContext;
import org.jaxen.XPath;

/**
 * Utility class to handle MTOM-Optimizing Base64 Text values
 *  
 * @author Ruchith Fernando (ruchith.fernando@gmail.com)
 */
public class MessageOptimizer {

	/**
	 * Mark the requied Base64 text values as optimized
	 * @param env
	 * @param optimizeParts This is a set of xPath expressions 
	 * (NOTE: Right now we support only one expression)
	 * @throws WSSecurityException
	 */
	public static void optimize(SOAPEnvelope env, String optimizeParts) throws WSSecurityException {
		
		//Find binary content
		List list = findElements(env,optimizeParts);
		
		Iterator cipherValueElements = list.iterator();
		
		while (cipherValueElements.hasNext()) {
			OMElement element = (OMElement) cipherValueElements.next();
			OMText text = (OMText)element.getFirstChild();
			text.setOptimize(true);
		}
	}
	
	
	private static List findElements(OMElement elem, String expression) throws WSSecurityException {
		try {
			XPath xp = new AXIOMXPath(expression);
			
			//Set namespaces
			SimpleNamespaceContext nsCtx = new SimpleNamespaceContext();
			nsCtx.addNamespace(WSConstants.ENC_PREFIX,WSConstants.ENC_NS);
			nsCtx.addNamespace(WSConstants.SIG_PREFIX,WSConstants.SIG_NS);
			nsCtx.addNamespace(WSConstants.WSSE_PREFIX,WSConstants.WSSE_NS);
			nsCtx.addNamespace(WSConstants.WSU_PREFIX,WSConstants.WSU_NS);
			
			xp.setNamespaceContext(nsCtx);
			
			return xp.selectNodes(elem);
			
		} catch (JaxenException e) {
			throw new WSSecurityException(e.getMessage(), e);
		}
		
	}
	
	
	
}