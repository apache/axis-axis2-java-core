package org.apache.axis2.maven2.wsdl2code;

/**
 * Data class for specifying URI->Package mappings.
 */
public class NamespaceURIMapping {
	private String uri, packageName;

	/**
	 * Returns the package name, to which the URI shall be mapped.
	 */
	public String getPackageName() {
		return packageName;
	}

	/**
	 * Sets the package name, to which the URI shall be mapped.
	 */
	public void setPackageName(String pPackageName) {
		packageName = pPackageName;
	}

	/** Returns the URI, which shall be mapped.
	 */
	public String getUri() {
		return uri;
	}

	/** Sets the URI, which shall be mapped.
	 */
	public void setUri(String pUri) {
		uri = pUri;
	}
}
