/*
 * TODO: Put the licence text
 *
 *
 */
package org.apache.axis.xml;

/**
 * 
 * This interface provides a set of methods to compare the properties of the
 * elements, while parsing a xml document in pull pasion. Then the user
 * doesn't have to create unneccessary String objects for comparison purpose.  
 * <br><br>
 * This methods can be used to improve the performance of the applications
 * that uses the parsers which truely implement this. 
 *
 * @author Rajith Priyanga (rpriyanga@yahoo.com)
 * @date Sep 20, 2004 
 * 
 */
public interface ContentComparable {

	/**
	 * Compares the given local name with the local name of the current
	 * element. If it doesn't match or the current element is not a starting 
	 * or ending tag, this returns false. Otherwise true. 
	 * @param localName
	 * @return
	 */
	public boolean isLocalNameEqualTo(String localName);
	
	/**
	 * Compares the given string with namespace prefix of the current element.
	 * If it doesn't match or the current element is not a starting 
	 * or ending tag, this returns false. Otherwise true.
	 * @param namespacePrefix
	 * @return
	 */	
	public boolean isNamespacePrefixEqualTo(String namespacePrefix);
	
	
	/**
	 * Compares the given string with the namespace of the current element.
	 * @param namespace The string to be compared.
	 * @return true if the given string is equal to the namespace. If the 
	 * element type is not applicable for this operation or if they are not 
	 * equal, this returns false.
	 */
	public boolean isNamespaceEqualTo(String namespace);	


	/**
	 * If the element is a starting or ending tag and the namespace and local name 
	 * mathces with the given ones, this returns true. Otherwise false. 
	 * @param namespace
	 * @param localName
	 * @return
	 */
	public boolean isNameEqualTo(String namespace, String localName);


	/**
	 * If the current element is a starting tag, it returns true if the attribute 
	 * with the given namespace and local name, is found in the attribute list. 
	 * Otherwise false.
	 * @param namespace If null, only the local name is compared.
	 * @param localName	 
	 * @return
	 */
	public boolean isAttributeAvailable(String namespace, String localName);
	
	
		
	/**
	 * returns true only when all of the following are true. else false.
	 * 1. Current element is a starting tag.
	 * 2. The attribute with given namespace and local name is available.
	 * 3. The value of the attribute is equal to the given value 
	 * @param namespace  If null, only the local name is compared.
	 * @param localName 
	 * @param value
	 * @return
	 */
	public boolean isAttributeValueEqualTo(String namespace, String localName, String value);	

}
