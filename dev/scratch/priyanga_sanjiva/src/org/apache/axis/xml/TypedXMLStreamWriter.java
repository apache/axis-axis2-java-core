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
import javax.xml.stream.XMLStreamException;


/**
 * This interface provides mthods to serialize Java Typed values 
 * easily to a xml document in appropriate xsd canonical formats.
 * 
 * A serializer which truely implement this interface can be used to 
 * improve the performance of an application. 
 *  
 * @author Rajith Priyanga
 * @date Sep 23, 2004 
 * 
 */

public interface TypedXMLStreamWriter extends javax.xml.stream.XMLStreamWriter {
	
	/**
	 * Writes the given boolean value in the xsd:boolean canonical form 
	 */
	public void writeCharacters(boolean value) throws XMLStreamException;
	
	/**
	 * Writes the given byte value in the xsd:byte canonical form 
	 */
	public void writeCharacters(byte value) throws XMLStreamException;
	
	/**
	 * Writes the given short value in the xsd:short canonical form 
	 */
	public void writeCharacters(short value) throws XMLStreamException;
	
	/**
	 * Writes the given int value in the xsd:int canonical form 
	 */	
	public void writeCharacters(int value) throws XMLStreamException;
	
	/**
	 * Writes the given long value in the xsd:long canonical form 
	 */
	public void writeCharacters(long value) throws XMLStreamException;
	
	/**
	 * Writes the given float value in the xsd:float canonical form 
	 */
	public void writeCharacters(float value) throws XMLStreamException;
	
	/**
	 * Writes the given double value in the xsd:double canonical form 
	 */
	public void writeCharacters(double value) throws XMLStreamException;
	
	/**
	 * Writes the given bytes array in the xsd:Base64 canonical form 
	 */
	public void writeCharacters(byte[] value) throws XMLStreamException;
	
	/**
	 * Writes the value of the given BigInteger object in the xsd:Integer canonical form 
	 */
	public void writeCharacters(BigInteger value) throws XMLStreamException;
	
	/**
	 * Writes the value of the given BugDecimal object in the xsd:Decimal canonical form 
	 */
	public void writeCharacters(BigDecimal value) throws XMLStreamException;
	
	/**
	 * Writes the given URI in the xsd:AnyURI canonical form 
	 */
	public void writeCharacters(URI value) throws XMLStreamException;
	
	/**
	 * Writes the given QName in the xsd:QName canonical form 
	 */
	public void writeCharacters(QName value) throws XMLStreamException;
	
	


	/**
	 * Writes the given boolean value in the xsd:boolean canonical form as 
	 * an attribute. 
	 * 
	 * @param prefix The namespace prefix of the attribute. 
	 * If null the prefix which the given namespace is already bound is taken.
	 *  
	 * @param namespace The namespace of the attribute.
	 * 
	 * If prefix==null and namespace==null, no prefix will be written
	 * If prefix!=null and namespace==null, given prefix will be written
	 * If prefix==null and namespace!=null, The prefix which the namespcae already bound will be written
	 * If prefix!=null and namespace!=null, given prefix will be written and xmlns:prefix = "namespace" will also be written.
	 *      
	 * @param localName The local name of the atribute. It can't be null.
	 * @param value The value of the attribute.
	 * 
	 * @throws XMLStreamException
	 */
	public void writeAttribute(String prefix, String namespace, String localName, boolean value) throws XMLStreamException;

	/**
	 * Writes the given byte value in the xsd:byte canonical form as 
	 * an attribute. 
	 * 
	 * @param prefix The namespace prefix of the attribute. 
	 * If null the prefix which the given namespace is already bound is taken.
	 *  
	 * @param namespace The namespace of the attribute.
	 * 
	 * If prefix==null and namespace==null, no prefix will be written
	 * If prefix!=null and namespace==null, given prefix will be written
	 * If prefix==null and namespace!=null, The prefix which the namespcae already bound will be written
	 * If prefix!=null and namespace!=null, given prefix will be written and xmlns:prefix = "namespace" will also be written.
	 *      
	 * @param localName The local name of the atribute. It can't be null.
	 * @param value The value of the attribute.
	 * 
	 * @throws XMLStreamException
	 */	
	public void writeAttribute(String prefix, String namespace, String localName, byte value) throws XMLStreamException;
	
	/**
	 * Writes the given short value in the xsd:short canonical form as 
	 * an attribute. 
	 * 
	 * @param prefix The namespace prefix of the attribute. 
	 * If null the prefix which the given namespace is already bound is taken.
	 *  
	 * @param namespace The namespace of the attribute.
	 * 
	 * If prefix==null and namespace==null, no prefix will be written
	 * If prefix!=null and namespace==null, given prefix will be written
	 * If prefix==null and namespace!=null, The prefix which the namespcae already bound will be written
	 * If prefix!=null and namespace!=null, given prefix will be written and xmlns:prefix = "namespace" will also be written.
	 *      
	 * @param localName The local name of the atribute. It can't be null.
	 * @param value The value of the attribute.
	 * 
	 * @throws XMLStreamException
	 */
	public void writeAttribute(String prefix, String namespace, String localName, short value) throws XMLStreamException;
	
	/**
	 * Writes the given int value in the xsd:int canonical form as 
	 * an attribute. 
	 * 
	 * @param prefix The namespace prefix of the attribute. 
	 * If null the prefix which the given namespace is already bound is taken.
	 *  
	 * @param namespace The namespace of the attribute.
	 * 
	 * If prefix==null and namespace==null, no prefix will be written
	 * If prefix!=null and namespace==null, given prefix will be written
	 * If prefix==null and namespace!=null, The prefix which the namespcae already bound will be written
	 * If prefix!=null and namespace!=null, given prefix will be written and xmlns:prefix = "namespace" will also be written.
	 *      
	 * @param localName The local name of the atribute. It can't be null.
	 * @param value The value of the attribute.
	 * 
	 * @throws XMLStreamException
	 */	
	public void writeAttribute(String prefix, String namespace, String localName, int value) throws XMLStreamException;
	
	/**
	 * Writes the given long value in the xsd:long canonical form as 
	 * an attribute. 
	 * 
	 * @param prefix The namespace prefix of the attribute. 
	 * If null the prefix which the given namespace is already bound is taken.
	 *  
	 * @param namespace The namespace of the attribute.
	 * 
	 * If prefix==null and namespace==null, no prefix will be written
	 * If prefix!=null and namespace==null, given prefix will be written
	 * If prefix==null and namespace!=null, The prefix which the namespcae already bound will be written
	 * If prefix!=null and namespace!=null, given prefix will be written and xmlns:prefix = "namespace" will also be written.
	 *      
	 * @param localName The local name of the atribute. It can't be null.
	 * @param value The value of the attribute.
	 * 
	 * @throws XMLStreamException
	 */
	public void writeAttribute(String prefix, String namespace, String localName, long value) throws XMLStreamException;
	
	
	/**
	 * Writes the given float value in the xsd:float canonical form as 
	 * an attribute. 
	 * 
	 * @param prefix The namespace prefix of the attribute. 
	 * If null the prefix which the given namespace is already bound is taken.
	 *  
	 * @param namespace The namespace of the attribute.
	 * 
	 * If prefix==null and namespace==null, no prefix will be written
	 * If prefix!=null and namespace==null, given prefix will be written
	 * If prefix==null and namespace!=null, The prefix which the namespcae already bound will be written
	 * If prefix!=null and namespace!=null, given prefix will be written and xmlns:prefix = "namespace" will also be written.
	 *      
	 * @param localName The local name of the atribute. It can't be null.
	 * @param value The value of the attribute.
	 * 
	 * @throws XMLStreamException
	 */
	public void writeAttribute(String prefix, String namespace, String localName, float value) throws XMLStreamException;
	
	/**
	 * Writes the given double value in the xsd:double canonical form as 
	 * an attribute. 
	 * 
	 * @param prefix The namespace prefix of the attribute. 
	 * If null the prefix which the given namespace is already bound is taken.
	 *  
	 * @param namespace The namespace of the attribute.
	 * 
	 * If prefix==null and namespace==null, no prefix will be written
	 * If prefix!=null and namespace==null, given prefix will be written
	 * If prefix==null and namespace!=null, The prefix which the namespcae already bound will be written
	 * If prefix!=null and namespace!=null, given prefix will be written and xmlns:prefix = "namespace" will also be written.
	 *      
	 * @param localName The local name of the atribute. It can't be null.
	 * @param value The value of the attribute.
	 * 
	 * @throws XMLStreamException
	 */
	public void writeAttribute(String prefix, String namespace, String localName, double value) throws XMLStreamException;
	
	/**
	 * Writes the given byte array in the xsd:Base64 canonical form as 
	 * an attribute. 
	 * 
	 * @param prefix The namespace prefix of the attribute. 
	 * If null the prefix which the given namespace is already bound is taken.
	 *  
	 * @param namespace The namespace of the attribute.
	 * 
	 * If prefix==null and namespace==null, no prefix will be written
	 * If prefix!=null and namespace==null, given prefix will be written
	 * If prefix==null and namespace!=null, The prefix which the namespcae already bound will be written
	 * If prefix!=null and namespace!=null, given prefix will be written and xmlns:prefix = "namespace" will also be written.
	 *      
	 * @param localName The local name of the atribute. It can't be null.
	 * @param value The value of the attribute.
	 * 
	 * @throws XMLStreamException
	 */
	public void writeAttribute(String prefix, String namespace, String localName, byte[] value) throws XMLStreamException;
	
	
	/**
	 * Writes the given BigInteger in the xsd:Integer canonical form as 
	 * an attribute. 
	 * 
	 * @param prefix The namespace prefix of the attribute. 
	 * If null the prefix which the given namespace is already bound is taken.
	 *  
	 * @param namespace The namespace of the attribute.
	 * 
	 * If prefix==null and namespace==null, no prefix will be written
	 * If prefix!=null and namespace==null, given prefix will be written
	 * If prefix==null and namespace!=null, The prefix which the namespcae already bound will be written
	 * If prefix!=null and namespace!=null, given prefix will be written and xmlns:prefix = "namespace" will also be written.
	 *      
	 * @param localName The local name of the atribute. It can't be null.
	 * @param value The value of the attribute.
	 * 
	 * @throws XMLStreamException
	 */
	public void writeAttribute(String prefix, String namespace, String localName, BigInteger value) throws XMLStreamException;
	
	
	/**
	 * Writes the given BigDecimal in the xsd:Decimal canonical form as 
	 * an attribute. 
	 * 
	 * @param prefix The namespace prefix of the attribute. 
	 * If null the prefix which the given namespace is already bound is taken.
	 *  
	 * @param namespace The namespace of the attribute.
	 * 
	 * If prefix==null and namespace==null, no prefix will be written
	 * If prefix!=null and namespace==null, given prefix will be written
	 * If prefix==null and namespace!=null, The prefix which the namespcae already bound will be written
	 * If prefix!=null and namespace!=null, given prefix will be written and xmlns:prefix = "namespace" will also be written.
	 *      
	 * @param localName The local name of the atribute. It can't be null.
	 * @param value The value of the attribute.
	 * 
	 * @throws XMLStreamException
	 */
	public void writeAttribute(String prefix, String namespace, String localName, BigDecimal value) throws XMLStreamException;
	
	/**
	 * Writes the given URI in the xsd:AnyURI canonical form as 
	 * an attribute. 
	 * 
	 * @param prefix The namespace prefix of the attribute. 
	 * If null the prefix which the given namespace is already bound is taken.
	 *  
	 * @param namespace The namespace of the attribute.
	 * 
	 * If prefix==null and namespace==null, no prefix will be written
	 * If prefix!=null and namespace==null, given prefix will be written
	 * If prefix==null and namespace!=null, The prefix which the namespcae already bound will be written
	 * If prefix!=null and namespace!=null, given prefix will be written and xmlns:prefix = "namespace" will also be written.
	 *      
	 * @param localName The local name of the atribute. It can't be null.
	 * @param value The value of the attribute.
	 * 
	 * @throws XMLStreamException
	 */
	public void writeAttribute(String prefix, String namespace, String localName, URI value) throws XMLStreamException;
	
	/**
	 * Writes the given QName in the xsd:QName canonical form as 
	 * an attribute. 
	 * 
	 * @param prefix The namespace prefix of the attribute. 
	 * If null the prefix which the given namespace is already bound is taken.
	 *  
	 * @param namespace The namespace of the attribute.
	 * 
	 * If prefix==null and namespace==null, no prefix will be written
	 * If prefix!=null and namespace==null, given prefix will be written
	 * If prefix==null and namespace!=null, The prefix which the namespcae already bound will be written
	 * If prefix!=null and namespace!=null, given prefix will be written and xmlns:prefix = "namespace" will also be written.
	 *      
	 * @param localName The local name of the atribute. It can't be null.
	 * @param value The value of the attribute.
	 * 
	 * @throws XMLStreamException
	 */
	public void writeAttribute(String prefix, String namespace, String localName, QName value) throws XMLStreamException;
	
	
	
	
}
