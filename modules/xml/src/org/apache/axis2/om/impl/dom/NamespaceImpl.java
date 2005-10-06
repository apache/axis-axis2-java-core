package org.apache.axis2.om.impl.dom;

import org.apache.axis2.om.OMNamespace;

/**
 * @author Ruchith Fernando (ruchith.fernando@gmail.com)
 */
public class NamespaceImpl implements OMNamespace {

	private String nsUri;
	private String nsPrefix;
	
	public NamespaceImpl(String uri) {
		this.nsUri = uri;
	}
	
	/* (non-Javadoc)
	 * @see org.apache.axis2.om.OMNamespace#equals(java.lang.String, java.lang.String)
	 */
	public boolean equals(String uri, String prefix) {
		return (this.nsUri == uri && this.nsPrefix == prefix);
	}

	/* (non-Javadoc)
	 * @see org.apache.axis2.om.OMNamespace#getPrefix()
	 */
	public String getPrefix() {
		return this.nsPrefix;
	}

	/* (non-Javadoc)
	 * @see org.apache.axis2.om.OMNamespace#getName()
	 */
	public String getName() {
		return this.nsUri;
	}

}
