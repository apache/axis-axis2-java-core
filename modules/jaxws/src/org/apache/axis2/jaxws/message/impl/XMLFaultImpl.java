package org.apache.axis2.jaxws.message.impl;

import javax.xml.namespace.QName;

import org.apache.axis2.jaxws.message.Block;
import org.apache.axis2.jaxws.message.XMLFault;


public class XMLFaultImpl implements XMLFault {
    
	private QName code;
	
    // SOAP 1.2 only
    private String reason;
    
    private Block[] detailBlocks = null;


    public XMLFaultImpl(QName code, String string, Block[] detailBlocks, Throwable userException) {

    	this.code = code;
    	this.reason = string;
    	this.detailBlocks = detailBlocks;

	}

    public QName getCode() {
    	return code;
    }
    
    public void setCode(QName code) {
    	this.code = code;
    }

    public String getString() {
    	return reason;
    }
    
    public void setString(String faultstring) {
    	this.reason = faultstring;
    }

    public Block[] getDetailBlocks() {
    	return detailBlocks;
    }
    
    public void setDetailBlocks(Block[] detailBlocks) {
    	this.detailBlocks = detailBlocks;
    }

    /*
     * TODO Code below this point will probably be needed once we start
     * taking faultCode as a parameter to the constructor.
     */
    
    /*
    private void setFaultCodeAsString(String code) {
        if (code != null && code.startsWith("{")) {
            int endCurly = code.indexOf("}");
            if (endCurly > 0) {
                String namespace = code.substring(1, endCurly);
                String localPart = code.substring(endCurly + 1);
                this.code = new QName(namespace, localPart);
                return;
            }
        }
        this.code = new QName("", code);
    }
    */
    
	/**
	 * Convert qualified name to QName
	 * @param se SOAPElement
	 * @param qualifiedName
	 * @return
	 */
    /*
	static private QName toQName(OMElement se, String qualifiedName) {
		String prefix;
		String localPart;
		String namespace;
		int indexColon = qualifiedName.indexOf(":");
		if (indexColon > 0) {
			prefix = qualifiedName.substring(0, indexColon);
			localPart = qualifiedName.substring(indexColon+1);
		} else {
			prefix = "";
			localPart = qualifiedName;
		}
	
		// Get the namespace for this prefix
		OMNamespace omNamespace = se.findNamespaceURI(prefix);
		if (omNamespace == null) {
			if (prefix.length() == 0) {
				namespace = ""; // Unqualified Namespace
			} else {
				// Namespace is not defined for this prefix
				throw new IllegalArgumentException();
			}
		} else {
			namespace = omNamespace.getNamespaceURI();
		}

		return new QName(namespace, localPart, prefix);
	}
	*/

	
	/**
	 * Convert QName to qualified name.  This has a side effect of 
	 * adding the namespace declaration to the SOAPElement if necessary
	 * @param se
	 * @param qName
	 * @return
	 */
	/*
	static String toQualifiedName(SOAPElement se, QName qName) {
		String prefix = "";
		MappingScope ms = se._getMappingScope();
		
		if (qName.getNamespaceURI().length() > 0) {
			// Namespace Qualified, so we need a prefix
			
			// Prefer using the prefix on the QName
			String preferPrefix = null;
			if (qName.getPrefix() != null ||
					qName.getPrefix().length() > 0) {
				preferPrefix = qName.getPrefix();
			}
		    // Get or create a pre-existing
			prefix = ms.getOrCreatePrefix(qName.getNamespaceURI(), preferPrefix, true);
		} else {
			// Unqualifed Namespace
			
			// If the default prefix is specified, we need to disable it by adding xmlns=""
			Mapping entry = ms.getMappingForPrefix("", false);
			if (entry != null && entry.getNamespaceURI().length() > 0) {
				ms.addMapping("", "");
			}
		}
		
		// Return the qualified
		if (prefix.length() == 0) {
			return qName.getLocalPart();
		} else {
			return prefix + ":" + qName.getLocalPart();
		}
	}
*/

}
