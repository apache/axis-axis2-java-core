package org.apache.axis2.om.impl.dom;

import org.apache.axis2.om.impl.OMOutputImpl;

import javax.xml.stream.XMLStreamException;

/**
 * @author Ruchith Fernando (ruchith.fernando@gmail.com)
 */
public class DOMSerializerUtil {

	
	public void serializeStartpart(ChildNode child, OMOutputImpl output) {
		
	}
	
	public void serializeEndPart(OMOutputImpl output) throws XMLStreamException {
		output.getXmlStreamWriter().writeEndElement();
	}
	
	
    //static void serializeAttribute(AttributeI attr, OMOutput omOutput) throws XMLStreamException {
	
	
}
