package org.apache.axis2.om.impl.dom;

import java.util.Vector;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class NodeListImpl implements NodeList  {
	
    protected NodeImpl rootNode; 
    protected String tagName;  
    protected Vector nodes;
    
    protected String nsName;
    protected boolean enableNS = false;
	
	
    /** Constructor. */
    public NodeListImpl(NodeImpl rootNode, String tagName) {
        this.rootNode = rootNode;
        this.tagName  = tagName;
        nodes = new Vector();
    }  

    /** Constructor for Namespace support. */
    public NodeListImpl(NodeImpl rootNode,
                            String nsName, String tagName) {
        this(rootNode, tagName);
        this.nsName = (nsName != null && !nsName.equals("")) ? nsName : null;
        enableNS = true;
    }

	/* (non-Javadoc)
	 * @see org.w3c.dom.NodeList#getLength()
	 */
	public int getLength() {
		//TODO
		throw new UnsupportedOperationException("TODO");
	}

	/* (non-Javadoc)
	 * @see org.w3c.dom.NodeList#item(int)
	 */
	public Node item(int arg0) {
		//TODO
		throw new UnsupportedOperationException("TODO");
	}
}
