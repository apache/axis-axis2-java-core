/*
 * TODO: Put the licence text
 *
 *
 */
package org.apache.axis.xml;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;

import javax.xml.namespace.QName;


/**
 * This interface allows the applications to read the xml content as the 
 * required java typed values, without creating unnecessary intermediary  
 * String objects which persist in memory. Therefore a true implementation
 * of this interface can be used to optimize performance of the application.
 * 
 * 
 * @author Rajith Priyanga
 * @date Sep 20, 2004 
 * 
 */
public interface TypedXMLStreamReader extends ContentComparable, javax.xml.stream.XMLStreamReader {
	
	/**
	 * If the reader is at a Start Tag of a text only element, reads the 
	 * next text as a requested xsd typed value and returns the appropriate java
	 * typed value. If the reader is at a Text, reads it as a requested 
	 * xsd typed value and returns the appropriate java typed value. 
	 * 
	 * @return The element value in the requested type
	 * @throws TypedXMLStreamException If the current is invalid for this operation or 
	 * if the value is not in the correct canonical form of the requested xsd type, 
	 * thorws a TypedXMLStreamException..
	 */
	public boolean 	readElementAsXSDBoolean() throws TypedXMLStreamException; 
	public byte 	readElementAsXSDByte() throws TypedXMLStreamException;
	public short 	readElementAsXSDShort() throws TypedXMLStreamException;
	public int 		readElementAsXSDInt() throws TypedXMLStreamException;
	public long 	readElementAsXSDLong() throws TypedXMLStreamException;
	public float 	readElementAsXSDFloat() throws TypedXMLStreamException;
	public double 	readElementAsXSDDouble() throws TypedXMLStreamException;
	public byte[] 	readElementAsXSDBase64() throws TypedXMLStreamException;
	public BigInteger readElementAsXSDInteger() throws TypedXMLStreamException;
	public BigDecimal readElementAsXSDDecimal() throws TypedXMLStreamException;	
	public URI 		readElementAsXSDAnyURI() throws TypedXMLStreamException;
	public QName 	readElementAsXSDQName() throws TypedXMLStreamException;
	public String 	readElementAsXSDString(boolean normalize, boolean replaceEntityRefs) throws TypedXMLStreamException;
	
	/** 
	 * If the reader is at a Start Tag, reads the given attribute as a requested 
	 * xsd typed value and returns the appropriate java typed value. 
	 *  
	 * @param namespace If null only the local name is compared.
	 * @param localName
	 * @return The attribute value
	 * @throws TypedXMLStreamException If the current event is not a Start Tag 
	 * or if the given attribute is not available or it is not in the 
	 * correct canonical form of the requested xsd type, thorws a TypedXMLStreamException.
	 */
	public boolean 	readAttributeAsXSDBoolean(String namespace, String localName) throws TypedXMLStreamException; 
	public byte 	readAttributeAsXSDByte(String namespace, String localName) throws TypedXMLStreamException;
	public short 	readAttributeAsXSDShort(String namespace, String localName) throws TypedXMLStreamException;
	public int 		readAttributeAsXSDInt(String namespace, String localName) throws TypedXMLStreamException;
	public long 	readAttributeAsXSDLong(String namespace, String localName) throws TypedXMLStreamException;
	public float 	readAttributeAsXSDFloat(String namespace, String localName) throws TypedXMLStreamException;
	public double 	readAttributeAsXSDDouble(String namespace, String localName) throws TypedXMLStreamException;
	public byte[] 	readAttributeAsXSDBase64(String namespace, String localName) throws TypedXMLStreamException;
	public BigInteger readAttributeAsXSDInteger(String namespace, String localName) throws TypedXMLStreamException;
	public BigDecimal readAttributeAsXSDDecimal(String namespace, String localName) throws TypedXMLStreamException;
	public URI 		readAttributeAsXSDAnyURI(String namespace, String localName) throws TypedXMLStreamException;
	public QName 	readAttributeAsXSDQName(String namespace, String localName) throws TypedXMLStreamException;
	public String 	readAttributeAsXSDString(String namespace, String localName, boolean normalize, boolean replaceEntityRefs) throws TypedXMLStreamException;
	
}
