/*
 * Created on May 20, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.apache.axis.util;

import org.apache.axis.om.OMNode;
import org.apache.axis.saaj.NodeImpl;
import org.w3c.dom.Node;

/**
 * @author shaas02
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class Dom2OmUtils {
	
	private OMNode omNode;
	public static OMNode toOM(Node node){
		if(node instanceof NodeImpl){
			return ((NodeImpl)node).getOMNode();
		}
		//ELSE Assumes an implemenattion of DOM to be present
		//so, here we convert DOM Node to a OMNode and add it as a
		//child to the omNode member of this NodeImpl
		return null;
	}

}
