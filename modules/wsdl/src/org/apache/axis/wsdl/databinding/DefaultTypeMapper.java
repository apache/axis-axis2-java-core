package org.apache.axis.wsdl.databinding;

import javax.xml.namespace.QName;

import org.apache.axis.om.OMElement;

/**
 * @author chathura@opensource.lk
 *
 */
public class DefaultTypeMapper implements TypeMapper {

	
	public Class getJavaTypeMapping(QName qname) {
		return OMElement.class;
	}
	
	public String getParameterName(QName qname){
		return "omElement";
	}

}
