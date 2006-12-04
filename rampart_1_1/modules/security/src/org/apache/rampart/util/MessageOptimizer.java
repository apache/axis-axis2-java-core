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
import org.apache.axiom.om.OMText;
import org.apache.axiom.om.xpath.AXIOMXPath;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.ws.security.WSConstants;
import org.apache.ws.security.WSSecurityException;
import org.jaxen.JaxenException;
import org.jaxen.SimpleNamespaceContext;
import org.jaxen.XPath;

import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Utility class to handle MTOM-Optimizing Base64 Text values
 */
public class MessageOptimizer {

	/**
	 * Mark the requied Base64 text values as optimized
	 * @param env
	 * @param optimizeParts This is a set of xPath expressions
	 *  
	 * @throws WSSecurityException
	 */
	public static void optimize(SOAPEnvelope env, String optimizeParts) throws WSSecurityException {
		String separater = "<>";
		StringTokenizer tokenizer = new StringTokenizer(optimizeParts, separater);
		
		while(tokenizer.hasMoreTokens()) {
			
			String xpathExpr = tokenizer.nextToken(); 
			
			//Find binary content
			List list = findElements(env,xpathExpr);
			
			Iterator cipherValueElements = list.iterator();
			
			while (cipherValueElements.hasNext()) {
				OMElement element = (OMElement) cipherValueElements.next();
				OMText text = (OMText)element.getFirstOMChild();
                System.out.println(text.getText().length());
				text.setOptimize(true);
			}
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