package org.apache.axis2.om.impl.dom;

class DOMUtil {

	public static boolean isValidChras(String value) {
		// TODO check for valid characters
		throw new UnsupportedOperationException("TODO");
	}
	
	public static boolean isValidNamespace(String namespaceURI, String qualifiedname) {
		// TODO check for valid namespace
		/**
		 * if the qualifiedName has a prefix and the namespaceURI is null, if
		 * the qualifiedName has a prefix that is "xml" and the namespaceURI is
		 * different from " http://www.w3.org/XML/1998/namespace", or if the
		 * qualifiedName, or its prefix, is "xmlns" and the namespaceURI is
		 * different from " http://www.w3.org/2000/xmlns/".
		 */
		throw new UnsupportedOperationException("TODO");
	}
	
	/**
	 * Get the local name from a qualified name 
	 * @param qualifiedName
	 * @return
	 */
	public static String getLocalName(String qualifiedName) {
		return qualifiedName.split(":")[1];
	}
	
	/**
	 * Get the prefix from a qualified name
	 * @param qualifiedName
	 * @return
	 */
	public static String getPrefix(String qualifiedName) {
		return qualifiedName.split(":")[0];
	}
}
