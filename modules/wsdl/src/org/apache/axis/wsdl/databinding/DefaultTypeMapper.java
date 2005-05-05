package org.apache.axis.wsdl.databinding;

import javax.xml.namespace.QName;

import org.apache.axis.om.OMElement;

/**
 * @author chathura@opensource.lk
 *
 */
public class DefaultTypeMapper extends TypeMappingAdapter {

	
	public Class getTypeMapping(QName qname) {
		return OMElement.class;
	}
	
	public String getParameterName(QName qname){
		return "omElement";
	}

}
