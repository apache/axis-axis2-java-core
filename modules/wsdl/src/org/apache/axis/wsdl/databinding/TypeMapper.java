package org.apache.axis.wsdl.databinding;

import javax.xml.namespace.QName;

/**
 * @author chathura@opensource.lk
 *
 */
public interface TypeMapper {
	
	public Class getJavaTypeMapping(QName qname);
	
	public String getParameterName(QName qname);

}
