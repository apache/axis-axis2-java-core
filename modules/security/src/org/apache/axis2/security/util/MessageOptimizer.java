
package org.apache.axis2.security.util;

import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.apache.axis2.om.OMElement;
import org.apache.axis2.om.OMNode;
import org.apache.axis2.om.OMText;
import org.apache.axis2.om.xpath.AXIOMXPath;
import org.apache.axis2.soap.SOAPEnvelope;
import org.apache.ws.security.WSConstants;
import org.apache.ws.security.WSSecurityException;
import org.jaxen.JaxenException;
import org.jaxen.SimpleNamespaceContext;
import org.jaxen.XPath;

public class MessageOptimizer {

	/**
	 * 
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
			SimpleNamespaceContext encNsCtx = new SimpleNamespaceContext();
			encNsCtx.addNamespace(WSConstants.ENC_PREFIX,WSConstants.ENC_NS);
			
//			SimpleNamespaceContext sigNsCtx = new SimpleNamespaceContext();
//			encNsCtx.addNamespace(WSConstants.SIG_PREFIX,WSConstants.SIG_NS);
//			
//			SimpleNamespaceContext wsseNsCtx = new SimpleNamespaceContext();
//			encNsCtx.addNamespace(WSConstants.WSSE_PREFIX,WSConstants.WSSE_NS);
			
			xp.setNamespaceContext(encNsCtx);
//			xp.setNamespaceContext(sigNsCtx);
//			xp.setNamespaceContext(wsseNsCtx);
			
			return xp.selectNodes(elem);
			
		} catch (JaxenException e) {
			throw new WSSecurityException(e.getMessage(), e);
		}
		
	}
	
	
	/**
	 * Returns all the child elements under the given parent
	 * It is assumend that the children of the given of elements
	 * will not be of the same 
	 * @param env
	 * @param ln
	 * @param ns
	 * @return
	 */
	public void findElements(OMNode elem, String ln, String ns, Vector elements ) {
		
		if(elem == null || ln == null || ns == null) {
			return;
		}
		
		OMNode startNode = elem;
		if (startNode.getType() == OMNode.ELEMENT_NODE
				&& ((OMElement) startNode).getLocalName().equals(ln)
				&& ns.equals(((OMElement) startNode).getNamespace().getName())) {

			// An element found
			elements.add(startNode);

			// move to the next sibling
			OMNode node = startNode.getNextSibling();
			
			if (startNode != null) {
				findElements(node, ln, ns, elements);
			}
		} else {
			if(startNode.getType() == OMNode.ELEMENT_NODE) {
				OMNode node = ((OMElement)startNode).getFirstChild();
				findElements(node, ln, ns, elements);
			}
		}
		
		
	}
	
}